/*
 * Copyright (c) 2012 Ingo Wiarda
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR
 * THE USE OR OTHER DEALINGS IN THE SOFTWARE
 */
package cinnamon.index

import groovyx.gpars.actor.DefaultActor
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.index.IndexReader
import org.apache.lucene.index.Term
import org.apache.lucene.queryParser.QueryParser
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.Query
import org.apache.lucene.search.TermQuery
import org.apache.lucene.search.TopDocs
import org.apache.lucene.util.Version
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import org.apache.lucene.xmlparser.CoreParser
import org.apache.lucene.xmlparser.ParserException
import cinnamon.index.queryBuilder.WildcardQueryBuilder
import cinnamon.index.queryBuilder.RegexQueryBuilder
import cinnamon.exceptions.CinnamonException
import humulus.Environment
import humulus.EnvironmentHolder
import cinnamon.ObjectSystemData
import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.analysis.LimitTokenCountAnalyzer

/**
 * Actor class which does the heavy lifting in searching and indexing.
 * This class borrows heavily from the cinnamon project (http://cinnamon-cms.de) [LGPL license]
 */
class LuceneActor extends
        DefaultActor {

    Logger log = LoggerFactory.getLogger(this.class)

    Map<String, Repository> repositories = new HashMap<String, Repository>()

    void onDeliveryError(msg) {
        log.warn("Could not deliver msg: $msg")
    }

    protected void act() {
        loop {
            react { command ->
                try {
                    LuceneResult result = new LuceneResult()
//                    log.debug("LuceneActor received: $command")
                    def env = Environment.list().find {it.dbName == command.repository}
//                    log.debug("found env: $env")
                    EnvironmentHolder.setEnvironment(env)
                    ObjectSystemData.withTransaction {
                        switch (command.type) {
                            case CommandType.ADD_TO_INDEX: addToIndex(command); break
                            case CommandType.REMOVE_FROM_INDEX: removeFromIndex(command); break
                            case CommandType.UPDATE_INDEX: removeFromIndex(command); addToIndex(command); break
                            case CommandType.SEARCH: result = search(command); break;
                        }
                    }
                    log.debug("reply & finish")
                    reply result
                }
                catch (Exception e) {
                    log.debug("Failed to act on command:", e)
                }
            }
        }
    }


    LuceneResult search(IndexCommand command) {
        def repository = repositories.get(command.repository)
        Query query

        if (command.xmlQuery){
            Analyzer standardAnalyzer = new StandardAnalyzer(Version.LUCENE_CURRENT);
            def analyzer = new LimitTokenCountAnalyzer(standardAnalyzer, Integer.MAX_VALUE);
            InputStream bais = new ByteArrayInputStream(command.query.getBytes("UTF-8"));
            CoreParser coreParser = new CoreParser("content", analyzer);
            coreParser.addQueryBuilder("WildcardQuery", new WildcardQueryBuilder());
            coreParser.addQueryBuilder("RegexQuery", new RegexQueryBuilder());
            query = coreParser.parse(bais);
        }
        else{
            QueryParser queryParser = new QueryParser(Version.LUCENE_CURRENT, "content", new StandardAnalyzer(Version.LUCENE_CURRENT))
            query = queryParser.parse(command.query);
        }
        
        IndexSearcher searcher = repository.indexSearcher
        ResultCollector collector = new ResultCollector(reader: repository.indexReader,
                searcher: repository.indexSearcher, domain: command.domain)
        searcher.search(query, collector)
        log.debug("Found: ${collector.documents.size()} documents.")

        def luceneResult = new LuceneResult(itemIdMap: collector.itemIdMap)
        return luceneResult
    }

    void removeFromIndex(IndexCommand command) {
        try {
            def repository = repositories.get(command.repository)
            def indexDir = repository.indexDir
            if (!IndexReader.indexExists(indexDir)) {
                log.debug("Index does not exist.");
                // nothing to do.
                return;
            }
            def indexable = command.indexable
            if (indexable == null) {
                log.debug("indexable is NULL");
                return;
            }
            def uniqueId = "${indexable.class.name}@${indexable.id}"
            log.debug("remove from Index: $uniqueId")
            deleteDocument(repository, new Term("uniqueId", uniqueId), 2);
        } catch (FileNotFoundException f) {
            log.warn("File not found - if the index does not yet exist, " +
                    "removeFromIndex is expected to fail", f);
        } catch (IOException e) {
            throw new RuntimeException("error.lucene.IO", e);
        }
    }

/**
 * Delete the documents found by the given Term from the given repository's index.
 *
 * @param term the search term
 * @param retries how often to retry deleting the document. Probably useful only
 *                if other programs / servlets are also interacting with the index.
 *                (which should not happen with an actor based implementation)
 * @throws java.io.IOException if anything IO-related goes wrong.
 */
    void deleteDocument(Repository repository, Term term, Integer retries) throws IOException {
        def indexWriter = repository.indexWriter
        try {
            indexWriter.deleteDocuments(term);
        } catch (Exception e) {
            log.debug("delete document failed:", e);
            if (retries != null && retries > 0) {
                log.debug("retry-delete document");
                deleteDocument(term, --retries);
            }
        } catch (OutOfMemoryError e) {
            log.warn("OOM-error during indexing:", e);
            // according to Lucene docs, we should close the writer after OOM-Problems.
        } finally {
            indexWriter.close(true)
            repository.createWriter()
//            repository.unlockIfNecessary();
        }
    }

    void addToIndex(IndexCommand command) {
        log.debug("store standard fields")
        def indexable = command.indexable
        def repository = repositories.get(command.repository)
        IndexSearcher indexSearcher = repository.indexSearcher
        try {
            // check that the document does not already exist - otherwise, remove it.
            String uniqueId = "${indexable.class.name}@${indexable.id}"
            Term t = new Term("uniqueId", uniqueId)
            Query query = new TermQuery(t)
            TopDocs docs = indexSearcher.search(query, 1)
            if (docs.totalHits > 0) {
                log.debug("delete old version")
//                def docId = docs.scoreDocs[0].doc
                deleteDocument(repository, t, 2)
            }

            // add document to index:
            Document doc = new Document()
            doc = storeStandardFields(indexable, doc)

            ContentContainer content;
            if (indexable.hasXmlContent()) {
                content = new ContentContainer(indexable, repository.name);
            }
            else {
                content = new ContentContainer(indexable, "<empty />".getBytes())
            }
//                String content = indexable.getContent(repository);
            log.debug("finished: getContent");
            ContentContainer metadata = new ContentContainer(indexable, indexable.getMetadata().getBytes());            
//                String metadata = indexable.getMetadata();
            log.debug("store systemMetadata");
//                String systemMetadata = indexable.getSystemMetadata();
            ContentContainer systemMetadata = new ContentContainer(indexable, indexable.getSystemMetadata().getBytes());
            log.debug("got sysMetadata, start indexObject loop");

            for (IndexItem item : IndexItem.list()) {
                log.debug("indexItem: $item.name")
                /*
                * At the moment, the OSDs and Folders do not cache
                * their responses to getSystemMetadata or getContent.
                * In a repository with many IndexItems, this would cause
                * quite some strain on the server's resources.
                */
                try {
//							log.debug("indexObject for field '"+item.fieldname+"' with content: "+content);
                    log.debug("item.indexType: ${item.indexType}")
                    item.indexObject(content, metadata, systemMetadata, doc);
                } catch (Exception e) {
                    log.debug("*** failed *** to execute IndexItem " + item.name, e);
                }
            }

            repository.indexWriter.addDocument(doc)
            indexable.indexOk = true
            indexable.indexed = new Date()
            //            indexWriter.commit()
        } catch (OutOfMemoryError e) {
            log.error("indexing failed: ", e)
            indexable.indexOk = false
        }
        finally {
            repository.indexWriter.close(true)
            repository.createWriter()
        }
    }

    Document storeStandardFields(Indexable indexable, Document doc) {
        String hibernateId = indexable.myId()
        String className = indexable.class.name
        String uniqueId = "${className}@${hibernateId}"
        log.debug("indexing of: ${uniqueId}")
        Field f = new Field("hibernateId", hibernateId, Field.Store.YES, Field.Index.NOT_ANALYZED)
        doc.add(f);

        doc.add(new Field("javaClass", className, Field.Store.YES, Field.Index.NOT_ANALYZED));
        doc.add(new Field("uniqueId", uniqueId, Field.Store.YES, Field.Index.NOT_ANALYZED));
//        log.debug("added standard fields.")
        return doc
    }

    /**
     * Search for all documents matching the given params, which must be an
     * Lucene XML-Query-Parser document.
     *
     * @param params input for XML-Query-Parser
     * @return a ResultCollector, which contains a collection of all documents found.
     */
    // TODO: XML-Search is not working yet in v3, refactor.
    public ResultCollector search(String params, repository) {
        log.debug("starting search");
        ResultCollector results = new ResultCollector();
        def analyzer = new StandardAnalyzer(Version.LUCENE_34)
        def searcher = repository.indexSearcher
        try {
            InputStream bais = new ByteArrayInputStream(params.getBytes("UTF-8"));
            CoreParser coreParser = new CoreParser("content", analyzer);
            coreParser.addQueryBuilder("WildcardQuery", new WildcardQueryBuilder());
            coreParser.addQueryBuilder("RegexQuery", new RegexQueryBuilder());
            Query query = coreParser.parse(bais);

            results.setSearcher(searcher);
            searcher.search(query, results);
            searcher.close();
        } catch (IOException e) {
            throw new CinnamonException("error.lucene.IO", e);
        } catch (ParserException e) {
            throw new CinnamonException("error.parsing.lucene.query", e, params);
        } finally {

        }
        log.debug("finished search; results: " + results.getDocuments().size());
        return results;
    }

}

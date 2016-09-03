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

import cinnamon.index.queryBuilder.RegexQueryBuilder
import cinnamon.index.queryBuilder.WildcardQueryBuilder
import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.index.Term
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.Query
import org.apache.lucene.search.TermQuery
import org.apache.lucene.search.TopDocs
import org.apache.lucene.store.Directory
import org.apache.lucene.store.FSDirectory
import org.apache.lucene.store.SingleInstanceLockFactory
import org.apache.lucene.queryparser.xml.CoreParser

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.file.Paths

/**
 *
 * Represents a Lucene Index Repository
 */
class Repository {

    Logger log = LoggerFactory.getLogger(this.class)

    File indexFolder
    String name
    IndexWriter indexWriter
    Directory indexDir
    Analyzer analyzer
    DirectoryReader indexReader
    IndexSearcher indexSearcher
    final Object repositoryLock = new Object()

    IndexWriter createWriter() {
        synchronized (repositoryLock) {
            try {
                
                if (indexWriter) {
                    indexWriter.commit()
                    indexWriter.close()
                }

                if (indexReader) {
                    indexReader.close()
                }
                if (indexDir) {
                    indexDir.close()
                }
                indexDir = FSDirectory.open(Paths.get(indexFolder.absolutePath), new SingleInstanceLockFactory())
                IndexWriterConfig writerConfig = new IndexWriterConfig(analyzer);
                writerConfig.openMode = IndexWriterConfig.OpenMode.CREATE_OR_APPEND;
                writerConfig.commitOnClose = true
                indexWriter = new IndexWriter(indexDir, writerConfig);
                indexWriter.commit() // to create empty index if necessary
                indexReader = DirectoryReader.open(indexDir)
                indexSearcher = new IndexSearcher(indexReader)


            } catch (IOException e) {
                throw new RuntimeException("error.lucene.IO", e);
            }

        }
        return indexWriter;
    }

    boolean termExists(Term t){
        synchronized (repositoryLock) {
            Query query = new TermQuery(t)
            TopDocs docs = indexSearcher.search(query, 1)
            return docs.totalHits > 0
        }
    }    

    LuceneResult doSearch(IndexCommand command) {
        synchronized (repositoryLock) {
            Query query
            if (command.xmlQuery) {
                InputStream bais = new ByteArrayInputStream(command.query.getBytes("UTF-8"));
                CoreParser coreParser = new CoreParser("content", analyzer);
                coreParser.addQueryBuilder("WildcardQuery", new WildcardQueryBuilder());
                coreParser.addQueryBuilder("RegexQuery", new RegexQueryBuilder());
                query = coreParser.parse(bais);
            }
            else {
                QueryParser queryParser = new QueryParser("content", new StandardAnalyzer())
                query = queryParser.parse(command.query);
            }

            log.debug("query: " + query.toString())

            IndexSearcher searcher = indexSearcher
            ResultCollector collector = new ResultCollector(searcher, command.domain)
            searcher.search(query, collector)
            TopDocs docs = searcher.search(query, Integer.MAX_VALUE)
            log.debug("docs: "+docs)
            log.debug("Found: ${collector.documents.size()} documents.")
            def luceneResult = new LuceneResult(itemIdMap: collector.itemIdMap)
            if (command.fields.size() > 0) {
                luceneResult.idFieldMap = collector.getIdFieldMap(command.domain, command.fields)
            }
            return luceneResult
        }
    }

    
    def closeIndex(){
        synchronized (repositoryLock){
            indexWriter?.commit()
            indexWriter?.close()
            indexReader?.close()
            indexDir?.close()
        }
    }
}

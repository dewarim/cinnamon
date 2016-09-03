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
import org.apache.lucene.analysis.LimitTokenCountAnalyzer
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.index.IndexReader
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.queryParser.QueryParser
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.Query
import org.apache.lucene.store.AlreadyClosedException
import org.apache.lucene.store.Directory
import org.apache.lucene.store.SimpleFSDirectory
import org.apache.lucene.store.SingleInstanceLockFactory
import org.apache.lucene.util.Version
import org.apache.lucene.xmlparser.CoreParser
import org.slf4j.Logger
import org.slf4j.LoggerFactory

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
    IndexReader indexReader
    IndexSearcher indexSearcher
    final Object repositoryLock = new Object()
//    Long ageInMillis = System.currentTimeMillis();

    IndexWriter createWriter() {
        synchronized (repositoryLock) {
            IndexWriterConfig writerConfig = new IndexWriterConfig(Version.LUCENE_34, analyzer);
            try {

                Long timeout = 10000;
                writerConfig.setWriteLockTimeout(timeout);
                writerConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
                try {
                    if (indexWriter) {
                        indexWriter.close(true)
                    }
                    removeLock()
                }
                finally {
                    if (IndexWriter.isLocked(indexDir)) {
                        IndexWriter.unlock(indexDir);
                    }
                }

                if (indexReader) {
                    indexReader.close()
                }
                if (indexSearcher) {
                    indexSearcher.close()
                }
                if (indexDir) {
                    indexDir.close()
                }

                indexDir = new SimpleFSDirectory(indexFolder, new SingleInstanceLockFactory())
                indexWriter = new IndexWriter(indexDir, writerConfig);
                indexWriter.commit() // to create empty index if necessary
                indexReader = IndexReader.open(indexDir)
                indexSearcher = new IndexSearcher(indexReader)


            } catch (IOException e) {
                throw new RuntimeException("error.lucene.IO", e);
            }

        }
        return indexWriter;
    }

    private void removeLock() {
        File indexLock = new File(indexFolder, "write.lock");
        if (indexLock.exists()) {
            log.debug("lock exists: trying to delete")
            // low level cleanup
            Boolean deleteResult = indexLock.delete();
            if (!deleteResult) {
                log.warn("It is possible that the indexLock (" + indexLock.getAbsolutePath() + ") has not been deleted.");
            }
            if (indexLock.exists()) {
                log.warn("lock still exists.")
            }
        }
        // cleanup; after server restart there could still be a lock lying around.
        if (IndexWriter.isLocked(indexDir)) {
            log.debug("IndexDir " + indexDir.toString() + " is locked - unlocking.");
            IndexWriter.unlock(indexDir);
        }
    }

    IndexSearcher getIndexSearcher() {
        synchronized (repositoryLock) {
            def indexReaderIsHealthy = true
            try {
                // If tryIncRef returns false, the indexReader is no longer usable.
                indexReaderIsHealthy = indexReader.tryIncRef() && indexReader.current
            }
            catch (AlreadyClosedException e) {
                indexReaderIsHealthy = false
            }
            finally {
                indexReader.decRef()
            }
            if (!indexReaderIsHealthy) {
                // If the reader is unusable or no longer current, try to open a new one.
                createWriter()
            }
            return indexSearcher
        }
    }


    LuceneResult doSearch(IndexCommand command){
        synchronized (repositoryLock) {
            Query query
            if (command.xmlQuery) {
                Analyzer standardAnalyzer = new StandardAnalyzer(Version.LUCENE_36);
                def analyzer = new LimitTokenCountAnalyzer(standardAnalyzer, Integer.MAX_VALUE);
                InputStream bais = new ByteArrayInputStream(command.query.getBytes("UTF-8"));
                CoreParser coreParser = new CoreParser("content", analyzer);
                coreParser.addQueryBuilder("WildcardQuery", new WildcardQueryBuilder());
                coreParser.addQueryBuilder("RegexQuery", new RegexQueryBuilder());
                query = coreParser.parse(bais);
            }
            else {
                QueryParser queryParser = new QueryParser(Version.LUCENE_36, "content", new StandardAnalyzer(Version.LUCENE_36))
                query = queryParser.parse(command.query);
            }

            IndexSearcher searcher = getIndexSearcher()
            ResultCollector collector = new ResultCollector(reader: indexReader,
                    searcher: searcher, domain: command.domain)
            searcher.search(query, collector)
            log.debug("Found: ${collector.documents.size()} documents.")
            def luceneResult = new LuceneResult(itemIdMap: collector.itemIdMap)
            if (command.fields.size() > 0) {
                luceneResult.idFieldMap = collector.getIdFieldMap(command.domain, command.fields)
            }
            return luceneResult
        }
    }

}

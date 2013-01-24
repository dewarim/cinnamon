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

import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.index.CorruptIndexException
import org.apache.lucene.index.IndexReader
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.store.Directory
import org.apache.lucene.util.Version
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

    IndexWriter createWriter() {
        IndexWriterConfig writerConfig = new IndexWriterConfig(Version.LUCENE_34, analyzer);


        try{
            removeLock()
            /*
            * Set timeout for write-locks.
            */
            Long timeout = 10000;
            writerConfig.setWriteLockTimeout(timeout);
            writerConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
            indexWriter = new IndexWriter(indexDir, writerConfig);
            indexWriter.commit() // to create empty index if necessary

            if(indexReader){
                indexReader.close()
            }
            if (indexSearcher){
                indexSearcher.close()
            }

            indexReader = IndexReader.open(indexDir)
            indexSearcher = new IndexSearcher(indexReader)


        } catch (IOException e) {
            throw new RuntimeException("error.lucene.IO", e);
        }
        return indexWriter;
    }

    void removeLock(){
        File indexLock = new File(indexFolder, "write.lock");
        if(indexLock.exists()){
            log.debug("lock exists: trying to delete")
            // low level cleanup
            Boolean deleteResult = indexLock.delete();
            if(!deleteResult){
                log.warn("It is possible that the indexLock ("+indexLock.getAbsolutePath()+") has not been deleted.");
            }
            if (indexLock.exists()){
                log.warn("lock still exists.")
            }
        }
        // cleanup; after server restart there could still be a lock lying around.
        if (IndexWriter.isLocked(indexDir)) {
            log.debug("IndexDir " + indexDir.toString() + " is locked - unlocking.");
            IndexWriter.unlock(indexDir);
        }
    }

    void unlockIfNecessary() throws IOException {
        if (IndexWriter.isLocked(indexDir)) {
            def indexDir = indexDir
            // we failed to commit or close the IndexWriter.
            try {
                log.debug("close IndexWriter")
                indexWriter.close();
            } catch (CorruptIndexException e) {
                log.error("Lucene Index has been corrupted!", e);
                throw new RuntimeException("error.lucene.IO: $indexDir", e);
            } catch (IOException e) {
                log.debug("IOException in unlockIfNecessary",e)
                throw new RuntimeException("error.lucene.IO: $indexDir", e);
            } finally {
                log.debug("Unlocking indexDir.")
                IndexWriter.unlock(indexDir);
                log.debug("create new writer")
                createWriter();
                log.debug("beyond new writer")
            }
        }
    }
}

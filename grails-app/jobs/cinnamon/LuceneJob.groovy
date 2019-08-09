package cinnamon

import cinnamon.index.ContentContainer
import cinnamon.index.IndexItem
import cinnamon.index.IndexJob
import cinnamon.index.Indexable
import cinnamon.index.Repository
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.document.FieldType
import org.apache.lucene.index.IndexOptions
import org.apache.lucene.index.Term
import org.apache.lucene.store.AlreadyClosedException

class LuceneJob {
    def concurrent = false

    static triggers = {
        simple repeatInterval: 2000l, startDelay: 30000l, name: "LuceneBackgroundJob"
    }

    def luceneService
    
    static Repository repository

    FieldType standardFieldType

    LuceneJob() {
        standardFieldType = new FieldType();
        standardFieldType.setIndexOptions(IndexOptions.DOCS_AND_FREQS);
        standardFieldType.setStored(true);
        standardFieldType.setTokenized(false);
        
        
    }

    def execute() {
        repository = luceneService.repository
        
        def osdJobs = []
        def seen = new HashSet<Long>(100)
        IndexJob.withTransaction {
            osdJobs = IndexJob.findAll("from IndexJob i where i.indexableClass=:indexableClass and i.failed = false",
                    [indexableClass: ObjectSystemData.class], [max: 100])
        }

        try {

            osdJobs.each { IndexJob job ->
                ObjectSystemData.withTransaction {
                    Long id = job.indexableId
                    if (seen.contains(id)) {
                        // remove duplicate jobs in the current transaction
                        job.delete()
                        return
                    }
                    def osd = ObjectSystemData.get(id)
                    if (osd == null) {
                        String uniqueId = "${job.indexableClass}@${job.indexableId}"
                        deleteDocument(repository, new Term("uniqueId", uniqueId), 2);
                        job.delete()
                    }
                    else {
                        doIndexJob(osd, job, repository, true)
                    }
                    seen.add(id)
                }
            }
        }
        catch (Exception e) {
            log.error("Failed to index OSDs because of:", e)
        }


        def folderJobs = []
        def seenFolders = new HashSet<Long>(100)
        IndexJob.withTransaction {
            folderJobs = IndexJob.findAll("from IndexJob i where i.indexableClass=:indexableClass and i.failed=false",
                    [indexableClass: Folder.class], [max: 100])
        }

        try {
            folderJobs.each { IndexJob job ->
                IndexJob.withTransaction {
                    Long id = job.indexableId
                    if (seenFolders.contains(id)) {
                        job.delete()
                        return
                    }
                    Folder reloadedFolder = Folder.get(id)
                    if (reloadedFolder == null) {
                        String uniqueId = "${job.indexableClass}@${job.indexableId}"
                        deleteDocument(repository, new Term("uniqueId", uniqueId), 2);
                        job.delete()
                    }
                    else {
                        doIndexJob(reloadedFolder, job, repository, true)
                    }
                    seenFolders.add(id)
                }
            }
        }
        catch (Exception e) {
            log.error("Failed to index Folders because of:", e)
        }

        repository.createWriter() // close & commit, then create new writer to prevent file leaks
    }

    def doIndexJob(Indexable indexable, job, Repository repository, Boolean removeFirst) {
        if (indexable) {
            try {
                if (removeFirst) {
                    deleteIndexableFromIndex(indexable, repository)
                }
                addToIndex(indexable, repository)
            }
            catch (Exception e) {
                log.warn("Index job for ${indexable.toString()} failed with:", e)
                job?.failed = true
                return
            }
        }
        job?.delete()
    }

    void deleteIndexableFromIndex(Indexable indexable, Repository repository) {
        def uniqueId = indexable.uniqueId()
        log.debug("remove from Index: $uniqueId")
        deleteDocument(repository, new Term("uniqueId", uniqueId), 2);
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
            try {
                indexWriter.deleteDocuments(term);
            }
            catch (AlreadyClosedException e) {
                indexWriter = repository.createWriter()
                indexWriter.deleteDocuments(term)
            }

        } catch (Exception e) {
            log.warn("delete document failed:", e);
            if (retries != null && retries > 0) {
                log.info("retry-delete document");
                deleteDocument(repository, term, --retries);
            }
            throw new RuntimeException('delete document from index failed', e)
        } catch (OutOfMemoryError e) {
            log.error("OOM-error during indexing:", e);
            throw new RuntimeException('delete document from index failed', e)
        }
    }

    void addToIndex(Indexable indexable, Repository repository) {
        try {
            // check that the document does not already exist - otherwise, remove it.
            String uniqueId = "${indexable.class.name}@${indexable.id}"
            Term uniqueTerm = new Term("uniqueId", uniqueId)
            if (repository.termExists(uniqueTerm)) {
                deleteDocument(repository, uniqueTerm, 2)
            }

            // add document to index:
            Document doc = new Document()
            doc = storeStandardFields(indexable, doc)

            ContentContainer content;
            if (indexable.hasXmlContent()) {
                content = new ContentContainer(indexable, repository.name);
            }
            else {
                content = new ContentContainer(indexable, "<empty />".bytes)
            }
            doIndex(indexable, content, repository, doc)
        } catch (OutOfMemoryError e) {
            log.error("indexing failed: ", e)
            throw new RuntimeException('Indexing failed', e)
        }
        catch (Exception e) {
            log.warn("addToIndex: ", e)
        }
    }

    void doIndex(Indexable indexable, ContentContainer content, Repository repository, Document doc) {
        ContentContainer metadata = new ContentContainer(indexable, indexable.metadata.bytes);
        String sysMeta = indexable.getSystemMetadata(true, true, true )
//        log.debug("structure:\n"+sysMeta)
        ContentContainer systemMetadata = new ContentContainer(indexable, sysMeta.bytes);
        for (IndexItem item : IndexItem.list()) {
            /*
            * At the moment, the OSDs and Folders do not cache
            * their responses to getSystemMetadata or getContent.
            * In a repository with many IndexItems, this would cause
            * quite some strain on the server's resources.
            */
            try {
                item.indexObject(content, metadata, systemMetadata, doc);
            } catch (Exception e) {
                log.debug("*** failed *** to execute IndexItem " + item.name, e);
            }
        }
        repository.indexWriter.addDocument(doc)
    }

    Document storeStandardFields(Indexable indexable, Document doc) {
        String hibernateId = indexable.myId()
        String className = indexable.class.name
        String uniqueId = "${className}@${hibernateId}"
        log.debug("indexing of: ${uniqueId}")
        Field f = new Field("hibernateId", hibernateId, standardFieldType)
        doc.add(f);

        doc.add(new Field("javaClass", className, standardFieldType))
        doc.add(new Field("uniqueId", uniqueId, standardFieldType))
        return doc
    }


}

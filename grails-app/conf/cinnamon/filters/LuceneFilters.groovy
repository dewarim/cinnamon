package cinnamon.filters

import cinnamon.LocalRepository
import cinnamon.ObjectSystemData
import cinnamon.index.IndexAction
import cinnamon.index.IndexJob
import cinnamon.index.Indexable

class LuceneFilters {

    def dependsOn = [TriggerFilters]

    def luceneService
    def userService

    def filters = {
        all(controller: '*', action: '*') {
            before = {

            }
            after = { Map model ->

            }
            afterView = { Exception e ->
                if (e) {
                    log.debug("*** After View-Exception ***", e)
                }
                else {
                    Map<Indexable, IndexAction> updatedObjects = LocalRepository.updatedObjects;
                    for (Indexable indexable : updatedObjects.keySet()) {
                        log.debug("Adding indexable #" + indexable.myId() + " to list objects which Lucene should try to update again.");
                        IndexJob.withTransaction {
                            def job = new IndexJob(indexable)
                            job.save()
                        }
                    }
                    if (updatedObjects.size() > 0) {
                        log.info("Added ${updatedObjects.size()} objects to IndexJob queue.")
                    }
                }
                LocalRepository.cleanUp()
            }
        }
    }
}

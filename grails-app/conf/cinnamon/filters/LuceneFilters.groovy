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
                if(e){    
                    log.debug("*** After View-Exception ***",e)
                }
                else{
//                    try {
//                        ObjectSystemData.withNewTransaction {
//                            Map<Indexable, IndexAction> updatedObjects = LocalRepository.getUpdatedObjects();
//                            for (Indexable indexable : updatedObjects.keySet()) {
//                                log.debug("Working on indexable #" + indexable.myId());
//                                switch (updatedObjects.get(indexable)) {
//                                    case IndexAction.ADD: luceneService.addToIndex(indexable); break;
//                                    case IndexAction.UPDATE: luceneService.updateIndex(indexable); break;
//                                    case IndexAction.REMOVE: luceneService.removeFromIndex(indexable); break;
//                                }
//                            }
//                        }
//                    }
//                    catch (Exception luceneFail){
//                        log.warn("Indexing new objects failed (Stacktrace follows), will try to recover",e)
                        Map<Indexable, IndexAction> updatedObjects = LocalRepository.getUpdatedObjects();
                        for (Indexable indexable : updatedObjects.keySet()) {
                            log.debug("Adding indexable #" + indexable.myId() + " to list objects which Lucene should try to update again.");
                            IndexJob.withTransaction {
                                def job = new IndexJob(indexable)
                                job.save()
                            }
                        }
                        log.info("Added ${updatedObjects.size()} objects to IndexJob queue.")
//                    }
                }
                LocalRepository.cleanUp()
            }
        }
    }
}

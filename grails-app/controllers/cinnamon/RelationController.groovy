package cinnamon

import cinnamon.relation.Relation
import cinnamon.relation.RelationType
import grails.plugins.springsecurity.Secured

@Secured(["isAuthenticated()"])
class RelationController extends BaseController{
    
    // AJAX method
    def create(Long id) {
        try{
            ObjectSystemData osd = fetchAndFilterOsd(id)
            def folders = osd.parent.subfolders.findAll { Folder f ->
                folderService.mayBrowseFolder(f, userService.user)
            }
            def candidates = folderService.getFolderContent(osd.parent, false)
            log.debug("candidates: ${candidates}")
            render(template: 'create', model: [osd:osd, folders:folders, folderType:'relation', candidates:candidates])            
        }
        catch (Exception e){
            log.debug("failed: relation.create",e)
            renderException(e)
        }
    }    
    
    // AJAX method
    def save(Long osd, Long rightOsd, Long leftOsd, Long type) {
        try{
            if(rightOsd != osd && leftOsd != osd){
                throw new RuntimeException('error.foreign.relations')
            }
            
            ObjectSystemData right = fetchAndFilterOsd(rightOsd)
            ObjectSystemData left = fetchAndFilterOsd(leftOsd)
            RelationType rt = RelationType.get(type)        
            def alreadyExists = Relation.findByTypeAndLeftOSDAndRightOSD(rt, left, right)
            if (! alreadyExists){
                Relation relation = new Relation(rt, left, right, '<meta/>')
                relation.save()
                luceneService.updateIndex(left, repositoryName)
                luceneService.updateIndex(right, repositoryName)
            }                  
            forward(controller: 'osd', action: 'listRelations', id: osd)
        }
        catch (Exception e){
            log.debug("failed: relation.create",e)
            renderException(e)
        }
    } 
       
    // AJAX method
    def delete(Long id) {
        try{
            Relation relation = Relation.get(id)
            if (relation){
                def leftOsd = relation.leftOSD
                def rightOsd = relation.rightOSD
                relation.delete(flush: true)
                luceneService.updateIndex(leftOsd, repositoryName)
                luceneService.updateIndex(rightOsd, repositoryName)
            }
            render(status: 200, text:'<!-- delete relation: success -->')
        }
        catch (Exception e){
            log.debug("failed: relation.create",e)
            renderException(e)
        }
    }
    
}

package cinnamon

import cinnamon.exceptions.IgnorableException
import cinnamon.global.PermissionName

/**
 * BaseController collects methods common to more than one controller which do not fit completely into a
 * dedicated service class. It also collects all fields for service injections which the inheriting 
 * controllers require.
 */
abstract class BaseController {
    
    def osdService
    def folderService
    def userService
    def springSecurityService
    def inputValidationService

    protected Set<String> loadUserPermissions(Acl acl) {
        Set<String> permissions
        try {
            log.debug("us: ${userService.user} acl: ${acl} repo: ${session.repositoryName}")
            permissions = userService.getUsersPermissions(userService.user, acl)
        } catch (RuntimeException ex) {
            log.debug("getUserPermissions failed", ex)
            render(status: 503, text: message(code: 'error.access.failed'))
            throw new IgnorableException("error.access.failed")
        }
        return permissions
    }

    protected Validator fetchValidator() {
        UserAccount user = userService.user
        return new Validator(user)
    }

  
    protected void renderException(Exception e){
        render(status:500, text:message(code:e.getMessage()))
    }

    protected ObjectSystemData fetchAndFilterOsd(id) {
        fetchAndFilterOsd(id, [PermissionName.BROWSE_OBJECT])
    }
    
    protected ObjectSystemData fetchAndFilterOsd(id, List permissions){
        def osd = ObjectSystemData.get(id)
        if (!osd) {
            throw new RuntimeException('error.object.not.found')
        }
        if (!osdService.checkPermissions(osd, userService.user, permissions)) {
            throw new RuntimeException('error.access.denied')
        }
        return osd
    }

    protected Folder fetchAndFilterFolder(id) {
        return fetchAndFilterFolder(id, [PermissionName.BROWSE_FOLDER])
    }
    
    protected Folder fetchAndFilterFolder(id, permissions) {
        def folder = Folder.get(id)
        if (!folder) {
            throw new RuntimeException('error.folder.not.found')
        }
        if (!folderService.checkPermissions(folder, userService.user, permissions)) {
            throw new RuntimeException('error.access.denied')
        }
        return folder
    }

    // not in folderService because it needs access to session.
    protected Collection<Folder> fetchChildFolders(Folder folder) {
        Collection<Folder> folderList = Folder.findAll("from Folder as f where f.parent=:parent and f.id != :id",
                [parent: folder, id: folder.id])
        Validator validator = fetchValidator()
        return validator.filterUnbrowsableFolders(folderList)
    }

}

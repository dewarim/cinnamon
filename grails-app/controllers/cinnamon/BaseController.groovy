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
    def luceneService
    def springSecurityService
    def inputValidationService
    def itemService

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


    protected void renderException(Exception e) {
        render(status: 500, text: message(code: e.getMessage()))
    }

    protected void renderExceptionXml(Exception e) {
        render(contentType: 'application/xml') {
            error {
                code(e.message)
                "message"(message(code: e.message))
            }
        }
    }

    protected ObjectSystemData fetchAndFilterOsd(id) {
        fetchAndFilterOsd(id, [PermissionName.BROWSE_OBJECT])
    }

    protected ObjectSystemData fetchAndFilterOsd(id, List permissions) {
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
        def folder
        if (id == '0') {
            log.debug("find root folder")
            folder = Folder.findRootFolder()
        }
        else {
            log.debug("looking for folder #$id")
            folder = Folder.get(id)
        }
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

    protected void setListParams() {
        params.offset = params.offset ? inputValidationService.checkAndEncodeInteger(params, "offset", "offset") : 0
        params.sort = params.sort ? inputValidationService.checkAndEncodeText(params, "sort", "sort") : 'id'
        params.max = params.max ? inputValidationService.checkAndEncodeInteger(params, 'max', 'paginate.max') : 10
        params.firstResult = params.firstResult ? inputValidationService.checkAndEncodeInteger(params, 'firstResult', 'paginate.firstResult') : 0
    }

    protected ObjectSystemData fetchLogo() {
        def logo = null
        def logoConfig = ConfigEntry.findByName('login.screen.config')
        if (logoConfig) {
            def xml = new XmlSlurper().parseText(logoConfig.config)
            logo = ObjectSystemData.get(xml.logoId?.text())
            log.debug("logo: $logo")
        }
        return logo
    }

    /**
     * Redirect to the a default page, which is /folder/index for a vanilla Cinnamon 3 installation.
     * You may configure this via the config file values defaultController and defaultAction.
     * @param myParams the parameter map for the redirect
     */
    protected void defaultRedirect(myParams) {
        def myController = grailsApplication.config.defaultController ?: 'folder'
        def myAction = grailsApplication.config.defaultAction ?: 'index'
        redirect(controller: myController, action: myAction, params: myParams)
    }

    /**
     * Lookup the name of a template in the configuration file. You may configure a
     * map of templateMappings like this:
     * <pre>
     *     templateMapping = [
     *      '/project/editName':'/osd/editName',
     *      '/project/editAcl':'/osd/editAcl',
     *      ]
     * </pre>
     * and the as a result for a given template key parameter the value will be returned.
     * @param template the name of the template
     * @return the mapping if it is defined, or the original template string value.
     */
    protected String mapTemplate(String template) {
        return grailsApplication.config.templateMapping?."$template" ?: template
    }
}

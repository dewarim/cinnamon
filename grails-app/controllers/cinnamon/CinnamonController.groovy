package cinnamon

import cinnamon.authentication.LdapConfig
import cinnamon.authentication.LoginType
import cinnamon.authentication.UnboundIdLdapConnector
import cinnamon.global.PermissionName
import grails.plugin.springsecurity.annotation.Secured

import cinnamon.exceptions.CinnamonException
import org.dom4j.Node
import cinnamon.global.Constants
import cinnamon.global.ConfThreadLocal
import cinnamon.global.Conf
import humulus.HashMaker
import cinnamon.i18n.UiLanguage

import javax.naming.ldap.LdapReferralException

// Name was chosen after the main servlet path of the v2 Cinnamon server (/cinnamon/cinnamon)

/**
 * Main controller
 */
class CinnamonController extends BaseController {

    // default response: list repositories
    @Secured(['IS_AUTHENTICATED_ANONYMOUSLY'])
    def index() {
        render(contentType: "text/xml") {
            repositories {
                def conf = ConfThreadLocal.conf
                conf.repositories.each { Node rep ->
                    repository {
                        name(rep.selectSingleNode('name').getText())
                        categories {
                            rep.selectNodes("categories/category").each { categoryNode ->
                                category(categoryNode.getText())
                            }
                        }

                    }
                }
                version(Constants.SERVER_VERSION)
            }
        }
    }

    def validateMail() {

    }

    def resetPasswordForm() {

    }

    def showLogo() {
        try {
            if (!params.id) {
                throw new RuntimeException("No id for logo object found.")
            }
            ObjectSystemData osd = ObjectSystemData.find(
                    "from ObjectSystemData o where o.id=:id and o.parent.name='config' and o.name='login.logo' ",
                    [id: Long.parseLong(params.id)]
            )
            if (!osd) {
                throw new RuntimeException("logo object ${params.id} was not found in repository.")
            }
            if (!osd.format?.contenttype?.startsWith('image/')) {
                render(status: 503, text: message(code: 'error.wrong.format'))
                return
            }
            response.setContentType(osd.format.contenttype)
            Conf conf = ConfThreadLocal.getConf()

            def repositoryName = getRepositoryName()
            if (!repositoryName) {
                def host = request.getHeader('host')
                log.debug("host: ${host}")
                def repo = host.replaceAll('([^.]+)\\..*', '$1')
                log.debug("repo from request header: $repo")
                if (repo && grailsApplication.config.repositories.find { it.name = repo }) {
                    repositoryName = repo
                }
                else {
                    log.debug("could not extract repository from hostname, using default_repository.")
                    repositoryName = grailsApplication.config.default_repository
                }
            }

            log.debug("repository: ${repositoryName}")
            def filename = conf.getDataRoot() + File.separator + repositoryName +
                    File.separator + osd.contentPath
            log.debug("filename:$filename")
            File image = new File(filename)
            if (!image.exists()) {
                log.debug("could not find: $filename")
                render(status: 503, text: message(code: 'error.image.not.found'))
                return
            }
            response.outputStream << image.readBytes()
            response.outputStream.close()
        }
        catch (Exception e) {
            log.debug("Failed to show logo:", e)
            renderException(e.message)
        }
    }

    @Secured(['IS_AUTHENTICATED_ANONYMOUSLY'])
    def connect() {
        try {
            String username = params.user
            def repository = params.repository ?: request.serverName
            String pwd = params.pwd
            def machine = params.machine ?: 'unknown'
            String language = params.language
            def failed = ""
            ['user', 'pwd'].each {
                if (!params."${it}") {
                    failed += "Request parameter ${it} is not set.\n"
                }
            }
            if (failed.length() > 0) {
                renderErrorXml(failed)
                return
            }

            def user = UserAccount.findByName(username)
            if (!user) {
                log.debug("user $username not found - trying ldap connector.")

                UnboundIdLdapConnector connector = new UnboundIdLdapConnector();
                if(!connector.initialized){
                    log.info("LdapConnector is not configured properly. Do you have a ldap-config.xml file in '${System.env.CINNAMON_HOME_DIR}'?")
                    renderErrorXml("error.user.not.found")
                    return
                }

                UnboundIdLdapConnector.LdapResult result = connector.connect(username,pwd)
                if(result.validUser) {
                    List<String> cinnamonGroups = new ArrayList<>()
                    result.groupMappings.forEach{ LdapConfig.GroupMapping mapping -> cinnamonGroups.add(mapping.cinnamonGroup)}
                    user = userService.createUserAcccount(username,cinnamonGroups,LoginType.LDAP, language)
                }
                
                if(!user){
                    renderErrorXml("error.user.not.found.and.ldap.create.failed")
                    return
                }
            }
            
            if(user.accountExpired){
                renderErrorXml("error.user.account.expired")
                return;
            }
            
            if(user.accountLocked){
                renderErrorXml("error.user.account.locked")
                return
            }
            
            if(!user.activated){
                renderErrorXml("error.user.account.not.active")
                return
            }
            
            switch (user.loginType) {
                case LoginType.LDAP: if (!isValidLdapUser(username, pwd)) {
                    renderErrorXml("error.ldap.authentication.failed")
                    return
                }; break;
                default: if (!HashMaker.compareWithHash(pwd, user.pwd)) {
                    renderErrorXml("error.wrong.password")
                    return
                }
            }

            def uiLanguage = user.language
            if (language) {
                def lang = UiLanguage.findByIsoCode(language)
                if (lang) {
                    uiLanguage = lang
                }
            }
            if (uiLanguage == null) {
                uiLanguage = UiLanguage.findByIsoCode('und')
            }
            def cinnamonSession = new Session(repository, user, machine, uiLanguage)
            cinnamonSession.lifetime = new Date().time + ConfThreadLocal.conf.getSessionExpirationTime(repository)
            cinnamonSession.save()
            log.debug("connect was successful, rendering response")
            render(contentType: 'application/xml', text: "<connection><ticket>${cinnamonSession.ticket}</ticket></connection>")
        }
        catch (Exception e) {
            log.debug("failed to connect: ", e)
            renderException(e.message)
        }
    }

    private boolean isValidLdapUser(String username, String password){
        UnboundIdLdapConnector connector = new UnboundIdLdapConnector()
        if (connector.initialized) {
            return connector.connect(username, password).validUser
        }
        else {
            log.info("LDAP connector seems uninitialized. Please check your ldap-config.xml (set host and port etc).")
            return false 
        }
    }
    
    @Secured(['IS_AUTHENTICATED_ANONYMOUSLY'])
    def disconnect(String ticket) {
        if (ticket) {
            Session.findByTicket(ticket)?.delete();
            render(text: '<success>success.disconnect</success>')
        }
        else {
            render(status: 500, "<error>ticket.unknown</error>")
        }
    }

    @Secured(['IS_AUTHENTICATED_ANONYMOUSLY'])
    def legacy() {
        try {
            log.debug("params for legacy: ${params}")
            def myAction = params.command
            if (!myAction) {
                myAction = 'index'
            }
            log.debug("reached legacy filter with command: ${myAction}")
            log.debug("params:$params")
            log.debug("header-ticket:" + request.getHeader('ticket'))
            def user = userService.user
            log.debug("user: $user")
            if (user) {
                log.debug("user is logged in")
                ConfThreadLocal.conf.currentUser = user
            }
            else if (myAction == 'connect') {
                return forward(action: 'connect') // special case.
            }
            else {
                log.debug("user is not logged in: forward to index")
                return forward(action: 'index')
            }

            switch (myAction) {
                case 'attachlifecycle': forward(controller: 'lifeCycleState', action: 'attachLifeCycle'); break
                case 'changestate': forward(controller: 'lifeCycleState', action: 'changeState'); break
                case 'checktranslation': forward(controller: 'translation', action: 'checkTranslation'); break
                case 'connect': forward(action: 'connect'); break
                case 'copy': forward(controller: 'osd', action: 'copy'); break
                case 'create': forward(controller: 'osd', action: 'createOsd'); break
                case 'createfolder': forward(controller: 'folder', action: 'createXml'); break
                case 'createlink': forward(controller: 'link', action: 'createLink'); break
                case 'createrelation': forward(controller: 'relation', action: 'createXml'); break
                case 'createtranslation': forward(controller: 'translation', action: 'createTranslation'); break
                case 'createworkflow': forward(controller: 'workflow', action: 'createWorkflow'); break
                case 'delete': forward(controller: 'osd', action: 'deleteXml'); break
                case 'deleteallversions': forward(controller: 'osd', action: 'deleteAllVersions'); break
                case 'deletefolder': forward(controller: 'folder', action: 'deleteXml'); break
                case 'deletelink': forward(controller: 'link', action: 'deleteLink'); break
                case 'deleterelation': forward(controller: 'relation', action: 'deleteXml'); break
                case 'detachlifecycle': forward(controller: 'lifeCycleState', action: 'detachLifeCycle'); break
                case 'disconnect': forward(action: 'disconnect'); break
                case 'dotransition': forward(controller: 'workflow', action: 'doTransition'); break
                case 'findopentasks': forward(controller: 'workflow', action: 'findOpenTasks'); break
                case 'forksession': forward(controller: 'cinnamon', action: 'forkSession'); break
                case 'getacls': forward(controller: 'acl', action: 'listXml'); break
                case 'getconfigentry': forward(controller: 'configEntry', action: 'getConfigEntryXml'); break
                case 'getcontent': forward(controller: 'osd', action: 'getContentXml'); break
                case 'getfolder': forward(controller: 'folder', action: 'fetchFolderXml'); break
                case 'getfolderbypath': forward(controller: 'folder', action: 'fetchFolderByPath'); break
                case 'getfoldermeta': forward(controller: 'folder', action: 'getFolderMeta'); break
                case 'getfoldertypes': forward(controller: 'folderType', action: 'listXml'); break;
                case 'getformats': forward(controller: 'format', action: 'listXml'); break
                case 'getlifecycle': forward(controller: 'lifeCycle', action: 'getLifeCycle'); break
                case 'getlifecyclestate': forward(controller: 'lifeCycleState', action: 'getLifeCycleState'); break
                case 'getlink': forward(controller: 'link', action: 'getLink'); break
                case 'getmeta': forward(controller: 'osd', action: 'getOsdMeta'); break
                case 'getmetaset': forward(controller: 'metaset', action: 'fetchMetaset'); break
                case 'getnextstates': forward(controller: 'lifeCycleState', action: 'getNextStates'); break
                case 'getobject': forward(controller: 'osd', action: 'fetchObject'); break
                case 'getobjects': forward(controller: 'osd', action: 'fetchObjects'); break
                case 'getobjectsbyid': forward(controller: 'osd', action: 'getObjectsById'); break
                case 'getobjectswithcustommetadata': forward(controller: 'osd', action: 'fetchObjectsWithCustomMetadata'); break
                case 'getobjtypes': forward(controller: 'objectType', action: 'listXml'); break
                case 'getrelations': forward(controller: 'relation', action: 'listXml'); break
                case 'getrelationtypes': forward(controller: 'relationType', action: 'listXml'); break
                case 'getsubfolders': forward(controller: 'folder', action: 'fetchSubFolders'); break
                case 'getsysmeta': forward(controller: 'osd', action: 'getSysMeta'); break
                case 'getuserbyname': forward(controller: 'userAccount', action: 'getUserByName'); break
                case 'getusers': forward(controller: 'userAccount', action: 'listXml'); break
                case 'getusersacls': forward(controller: 'acl', action: 'getUsersAcls'); break
                case 'getuserspermissions': forward(controller: 'acl', action: 'getUsersPermissions'); break
                case 'linkmetaset': forward(controller: 'metaset', action: 'linkMetaset'); break
                case 'listaclentries': forward(controller: 'aclEntry', action: 'listXml'); break
                case 'listgroups': forward(controller: 'group', action: 'listXml'); break
                case 'listindexgroups': forward(controller: 'indexGroup', action: 'listXml'); break
                case 'listindexitems': forward(controller: 'indexItem', action: 'listXml'); break
                case 'listlanguages': forward(controller: 'language', action: 'listXml'); break
                case 'listlifecycles': forward(controller: 'lifeCycle', action: 'listLifeCyclesXml'); break
                case 'listmessages': forward(controller: 'message', action: 'listXml'); break
                case 'listmetasettypes': forward(controller: 'metasetType', action: 'listXml'); break
                case 'listpermissions': forward(controller: 'permission', action: 'listXml'); break
                case 'listuilanguages': forward(controller: 'uiLanguage', action: 'listXml'); break
                case 'lock': forward(controller: 'osd', action: 'lockXml'); break
                case 'reindex': if (params.type?.equals('osd')) {
                    forward(controller: 'osd', action: 'reindex')
                }
                else {
                    forward(controller: 'folder', action: 'reindex')
                }
                    break
                case 'searchobjects': forward(controller: 'search', action: 'searchObjectsXml'); break
                case 'searchobjectids': forward(controller: 'search', action: 'searchObjectsXmlIdOnly'); break
                case 'searchfolders': forward(controller: 'search', action: 'searchFolders'); break
                case 'setchangedstatus': forward(controller: 'cinnamon', action: 'setChangedStatus'); break
                case 'setconfigentry': forward(controller: 'configEntry', action: 'setConfigEntryXml'); break
                case 'setcontent': forward(controller: 'osd', action: 'saveContentXml'); break
                case 'setmeta': forward(controller: 'osd', action: 'saveMetadataXml'); break
                case 'setmetaset': forward(controller: 'metaset', action: 'saveMetaset'); break
                case 'setpassword': forward(controller: 'userAccount', action: 'changePassword'); break
                case 'setsummary': if (params.type?.equals('osd')) {
                    forward(controller: 'osd', action: 'setSummaryXml')
                }
                else {
                    forward(controller: 'folder', action: 'setSummaryXml')
                };
                    break
                case 'setsysmeta': forward(controller: 'osd', action: 'updateSysMetaXml'); break
                case 'startrendertask': forward(controller: 'renderServer', action: 'createRenderTask'); break
                case 'sudo': forward(action: 'sudo'); break
                case 'test': forward(action: 'test'); break
                case 'unlinkmetaset': forward(controller: 'metaset', action: 'unlinkMetaset'); break
                case 'unlock': forward(controller: 'osd', action: 'unlockXml'); break
                case 'updatefolder': forward(controller: 'folder', action: 'updateFolder'); break
                case 'updatelink': forward(controller: 'link', action: 'updateLink'); break
                case 'version': forward(controller: 'osd', action: 'newVersionXml'); break
                default: throw new RuntimeException("$myAction has no legacy action.")
            }
        }
        catch (Exception e) {
            log.warn("Call on legacy API failed:", e)
            renderExceptionXml("Call on legacy API failed:", e)
        }
    }

    /**
     * Fork a session and receive another session ticket for the current repository.
     * This method should be used by multi-threaded clients, which must not share
     * the same ticket over parallel requests.
     *
     * @param cmd HTTP request params as Map
     *            The request contains the following parameters:
     *            <ul>
     *            <li>command=forksession</li>
     *            <li>ticket=current session ticket</li>
     * @return a Response containing
     *         <pre>
     * {@code
     *         <connection>
     *            <ticket>$ticket</ticket>
     *         </connection>
     *}
     *         </pre>
     *         or an XML error message.
     */
    def forkSession(String ticket) {
        try {
            Session session = Session.findByTicket(ticket)
            if (!session) {
                log.warn("unknown session ticket for forkSession: '$ticket'")
                throw new CinnamonException("error.unknown.ticket")
            }
            String repository = ticket.split("@")[1]
            Session forkedSession = session.copy(repository);
            forkedSession.save()
            renderTicket(forkedSession.ticket)

        } catch (Exception e) {
            log.debug("failed to fork session:", e)
            renderExceptionXml(e.message)
        }
    }

    private renderTicket(myTicket) {
        render(contentType: 'application/xml') {
            connection {
                ticket(myTicket)
            }
        }
    }

    def test() {
        log.debug("reached test method")
        render(text: 'test method')
    }

    /**
     * Sudo: create a new session for another user and receive his session ticket.<br>
     * This method can be used by external processes to work in the name of normal users. For example,
     * a user creates a task for the RenderServer. The render process now creates a rendition of an
     * object and wants to store it in the repository - but we need to check that a user cannot create
     * tasks that produce output in a form or place that he may not use. <br>
     * Note that the sudo API method can be dangerous, so it needs to be restricted. This is achieved by
     * having two fields in the User object:
     * <ul>
     * <li>sudoer = boolean value, true if the User is allowed to use the sudo command</li>
     * <li>sudoable = boolean value, true if the user may be the target of a sudo.</li>
     * </ul>
     * <p/>
     * Unless the system administrator needs to debug a specific task, administrator accounts should
     * always have the sudoable field set to false.
     *
     * @param user_id = id of the user who you want to impersonate
     * @return XML document with the ticket of the user's session.
     */
    def sudo(Long user_id) {
        try {
            // check user_id parameter
            if (!user_id) {
                throw new RuntimeException("error.missing.param.user_id")
            }
            UserAccount user = userService.user
            // check if current user may do sudo
            if (user.sudoer) {
                log.debug("User ${user.name} is sudoer - ok.")
            }
            else {
                log.debug("User ${user.name} may not do sudo.")
                throw new CinnamonException("error.sudo.forbidden")
            }

            // load user to which sudo is being done ;)
            UserAccount alias = UserAccount.get(user_id)
            if (alias == null) {
                throw new CinnamonException("error.user.not_found")
            }

            // check if target is sudoable
            if (!alias.sudoable) {
                log.warn("User ${user.name} tried to become ${alias.name} via sudo, who is not a valid target.")
                throw new CinnamonException("error.sudo.misuse")
            }

            // create session for target user:
            Session session = new Session(repositoryName, alias, "--- sudo by ${user.name} ---", user.language)
            session.save()
            log.debug("session.ticket:" + session.ticket)

            // create response:
            render(contentType: 'application/xml') {
                sudoTicket(session.ticket)
            }
        }
        catch (Exception e) {
            log.debug("failed to sudo: ", e)
            renderErrorXml(e.message)
        }
    }

    @Secured(["isAuthenticated()"])
    def setChangedStatus(String type, Long id, Boolean contentChanged, Boolean metadataChanged) {
        try {
            def user = userService.user
            if (user.changeTracking) {
                renderExceptionXml("Only users without changeTracking are allowed to use setChangedStatus.")
                return
            }
            if (type?.equals('object')) {
                def osd = fetchAndFilterOsd(id, [PermissionName.WRITE_OBJECT_SYS_METADATA])
                boolean changed = false
                if (params.containsKey('contentChanged') && contentChanged != null) {
                    osd.contentChanged = contentChanged
                    changed = true
                }
                if (params.containsKey('metadataChanged') && metadataChanged != null) {
                    osd.metadataChanged = metadataChanged
                    changed = true
                }
                if (!changed) {
                    renderExceptionXml("Failed to set changed flags: missing parameter(s)")
                    return
                }
            }
            if (type?.equals('folder')) {
                def folder = fetchAndFilterFolder(id, [PermissionName.EDIT_FOLDER])
                if (params.containsKey('metadataChanged') && metadataChanged != null) {
                    folder.metadataChanged = metadataChanged
                }
                else {
                    renderExceptionXml("Failed to set metadataChanged flag: missing parameter metadataChanged")
                    return
                }
            }

            render(contentType: 'application/xml') {
                success('success.set.changedStatus')
            }
        }
        catch (Exception e) {
            renderExceptionXml('Failed to change content/metadata flags', e)
        }


    }

}

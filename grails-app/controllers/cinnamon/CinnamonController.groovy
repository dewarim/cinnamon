package cinnamon

import org.dom4j.Node
import cinnamon.global.Constants
import cinnamon.global.ConfThreadLocal
import cinnamon.global.Conf
import humulus.EnvironmentHolder
import humulus.Environment
import humulus.HashMaker
import cinnamon.i18n.UiLanguage
import grails.plugins.springsecurity.Secured

// Name was chosen after the main servlet path of the v2 Cinnamon server (/cinnamon/cinnamon)

/**
 * Main controller
 */
class CinnamonController extends BaseController {

    // default response: list repositories
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
                return render(status: 503, text: message(code: 'error.wrong.format'))
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
                return render(status: 503, text: message(code: 'error.image.not.found'))
            }
            response.outputStream << image.readBytes()
            response.outputStream.close()
            return null
        }
        catch (Exception e) {
            log.debug("Failed to show logo:", e)
            renderException(e)
        }
    }

    def connect() {
        try {
            def username = params.user
            def repository = params.repository
            def pwd = params.pwd
            def machine = params.machine ?: 'unknown'
            def language = params.language

            ['user', 'repository', 'pwd'].each {
                if (!params."${it}") {
                    throw new RuntimeException("Request parameter ${it} is not set.")
                }
            }

            def env = Environment.list().find { it.dbName == repository }
            if (!env) {
                log.debug("could not find environment for repository ${repository}")
                throw new RuntimeException("error.no.environment")
            }
            EnvironmentHolder.setEnvironment(env)
//            log.debug("${grailsApplication.mainContext.getBean('demoDataSource')}")
//            def ds = grailsApplication.getMainContext().getBean('dataSource') //getBean("${repository}DataSource")
            def ds = grailsApplication.getMainContext().dataSource
            ds.getConnection()
            def user = UserAccount.findByName(username)
            if (!user) {
                log.debug("user $username not found")
                throw new RuntimeException("error.user.not.found")
            }
            if (!HashMaker.compareWithHash(pwd, user.pwd)) {
                throw new RuntimeException("error.wrong.password")
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
            render(text: "<connection><ticket>${cinnamonSession.ticket}</ticket></connection>")
        }
        catch (Exception e) {
            log.debug("failed to connect: ", e)
            renderException(e)
        }
    }

    def disconnect() {
        def ticket = params.ticket;
        if (ticket) {
            Session.findByTicket(ticket)?.delete();
            render(text: '<success>success.disconnect</success>')
        }
        else {
            render(status: 500, "<error>ticket.unknown</error>")
        }
    }

    def legacy() {
        try {
            def myAction = params.command
            if (!myAction) {
                myAction = 'index'
            }
            log.debug("reached legacy filter with command: ${myAction}")
            log.debug("params:$params")
            if (userService.user) {
                log.debug("user is logged in")
            }
            else if (myAction == 'connect') {
                return forward(action: 'connect') // special case.
            }
            else {
                log.debug("user is not logged in: forward to index")
                return forward(action: 'index')
            }

            switch (myAction) {
                case 'connect': forward(action: 'connect'); break
                case 'copy': forward(controller: 'osd', action: 'copy'); break
                case 'create': forward(controller: 'osd', action: 'createOsd'); break
                case 'createfolder': forward(controller: 'folder', action: 'createXml'); break
                case 'createlink': forward(controller: 'link', action: 'createLink'); break
                case 'createrelation': forward(controller: 'relation', action: 'createXml'); break
                case 'delete': forward(controller: 'osd', action: 'deleteXml'); break
                case 'deleteallversions': forward(controller: 'osd', action: 'deleteAllVersions'); break
                case 'deletefolder': forward(controller: 'folder', action: 'deleteXml'); break
                case 'deletelink': forward(controller: 'link', action: 'deleteLink'); break
                case 'deleterelation': forward(controller: 'relation', action: 'deleteXml'); break
                case 'disconnect': forward(action: 'disconnect'); break
                case 'getacls': forward(controller: 'acl', action: 'listXml'); break
                case 'getcontent': forward(controller: 'osd', action: 'getContent'); break
                case 'getfoldertypes': forward(controller: 'folderType', action: 'listXml'); break;
                case 'getformats': forward(controller: 'format', action: 'listXml'); break
                case 'getfolderbypath': forward(controller: 'folder', action: 'fetchFolderByPath'); break
                case 'getfolder': forward(controller: 'folder', action: 'fetchFolderXml'); break
                case 'getfoldermeta': forward(controller: 'folder', action: 'getFolderMeta'); break
                case 'getlifecycle': forward(controller: 'lifeCycle', action: 'getLifeCycle'); break
                case 'getlink': forward(controller: 'link', action: 'getLink'); break
                case 'getmeta': forward(controller: 'osd', action: 'getOsdMeta'); break
                case 'getobject': forward(controller: 'osd', action: 'fetchObject'); break
                case 'getobjects': forward(controller: 'osd', action: 'fetchObjects'); break
                case 'getobjectsbyid': forward(controller: 'osd', action: 'getObjectsById'); break
                case 'getobjectswithcustommetadata': forward(controller: 'osd', action: 'fetchObjectsWithCustomMetadata'); break
                case 'getobjtypes': forward(controller: 'objectType', action: 'listXml'); break
                case 'getrelations': forward(controller: 'relation', action: 'listXml'); break
                case 'getrelationtypes': forward(controller: 'relationType', action: 'listXml'); break
                case 'getsubfolders': forward(controller: 'folder', action: 'fetchSubFolders'); break
                case 'getsysmeta': forward(controller: 'osd', action: 'getSysMeta'); break
                case 'getusers': forward(controller: 'userAccount', action: 'listXml'); break
                case 'getusersacls': forward(controller: 'acl', action: 'getUsersAcls'); break
                case 'getuserspermissions': forward(controller: 'acl', action: 'getUsersPermissions'); break
                case 'listaclentries': forward(controller: 'aclEntry', action: 'listXml'); break
                case 'listgroups': forward(controller: 'group', action: 'listXml'); break
                case 'listindexitems': forward(controller: 'indexItem', action: 'listXml'); break
                case 'listindexgroups': forward(controller: 'indexGroup', action: 'listXml'); break
                case 'listlanguages': forward(controller: 'language', action: 'listLanguages'); break                
                case 'listlifecycles': forward(controller: 'lifeCycle', action: 'listLifeCyclesXml'); break                
                case 'listmessages': forward(controller: 'message', action: 'listXml'); break
                case 'listpermissions': forward(controller: 'permission', action: 'listXml'); break
                case 'listuilanguages': forward(controller: 'uiLanguage', action: 'listUiLanguages'); break
                case 'lock': forward(controller: 'osd', action: 'lockXml'); break
                case 'searchobjects': forward(controller: 'search', action: 'searchObjectsXml'); break
                case 'searchfolders': forward(controller: 'search', action: 'searchFolders'); break
                case 'setcontent': forward(controller: 'osd', action: 'saveContentXml'); break
                case 'setmeta': forward(controller: 'osd', action: 'saveMetadataXml'); break
                case 'setsysmeta': forward(controller: 'osd', action: 'updateSysMetaXml'); break
                case 'test': forward(action: 'test'); break
                case 'unlock': forward(controller: 'osd', action: 'unlockXml'); break
                case 'updatefolder': forward(controller: 'folder', action: 'updateFolder'); break
                case 'updatelink': forward(controller: 'link', action: 'updateLink'); break
                default: throw new RuntimeException("$myAction has no legacy action.")
            }
        }
        catch (Exception e) {
            log.warn("Call on legacy API failed:", e)
            renderExceptionXml("Call on legacy API failed:", e)
        }
    }

    def test() {
        log.debug("reached test method")
        return render(text: 'test method')
    }
}

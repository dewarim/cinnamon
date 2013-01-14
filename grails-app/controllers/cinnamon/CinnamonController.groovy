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
                conf.repositories.each {Node rep ->
                    repository {
                        name(rep.selectSingleNode('name').getText())
                        categories {
                            rep.selectNodes("categories/category").each {categoryNode ->
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

            def repositoryName = session.repositoryName
            if (!repositoryName) {
                def host = request.getHeader('host')
                log.debug("host: ${host}")
                def repo = host.replaceAll('([^.]+)\\..*', '$1')
                log.debug("repo from request header: $repo")
                if (repo && grailsApplication.config.repositories.find {it.name = repo}) {
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

            def env = Environment.list().find {it.dbName == repository}
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


    def legacy() {
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
            case 'test': forward(action: 'test'); break
            case 'getusers': forward(controller: 'userAccount', action: 'listXml'); break
            case 'searchobjects': params.xmlQuery = true; forward(controller: 'search', action: 'searchObjects'); break
            case 'getformats': forward(controller: 'format', action: 'listXml'); break
            case 'getacls': forward(controller: 'acl', action: 'list'); break
            case 'getfoldertypes': forward(controller: 'folderType', action: 'listXml'); break;
            case 'getsubfolders': forward(controller: 'folder', action: 'fetchSubFolders'); break
            case 'getobjtypes' : forward(controller:'objectType', action: 'listXml');break
            case 'getfolderbypath' : forward(controller:'folder', action: 'fetchFolderByPath');break
            case 'getobjects' : forward(controller:'osd', action: 'fetchObjects');break
            case 'getfolder': forward(controller: 'folder', action: 'fetchFolderXml');break
            case 'createlink': forward(controller: 'link', action: 'createLink');break
            case 'updatelink': forward(controller: 'link', action: 'updateLink');break
            case 'getlink': forward(controller: 'link', action: 'getLink');break
            case 'deletelink': forward(controller: 'link', action: 'deleteLink');break
            
            default: log.debug("*********************************************************************************\n"+
                    "$myAction has no legacy action => forward to index"); forward(action: 'index')
        }
    }

    def test() {
        log.debug("reached test method")
        return render(text: 'test method')
    }
}

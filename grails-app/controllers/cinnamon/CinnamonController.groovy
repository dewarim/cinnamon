package cinnamon

import org.dom4j.Node
import cinnamon.global.Constants
import cinnamon.global.ConfThreadLocal
import cinnamon.global.Conf

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
            if (! params.id){
                throw new RuntimeException("No id for logo object found.")
            }
            ObjectSystemData osd = ObjectSystemData.find(
                    "from ObjectSystemData o where o.id=:id and o.parent.name='config' and o.name='login.logo' ",
                    [id: Long.parseLong(params.id)]
            )
            if (! osd){
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
            log.debug("Failed to show logo:",e)
            renderException(e)
        }
    }
    
}

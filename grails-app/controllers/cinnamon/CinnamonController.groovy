package cinnamon

import org.dom4j.Node
import cinnamon.global.Constants
import cinnamon.global.ConfThreadLocal

// Name was chosen after the main servlet path of the v2 Cinnamon server (/cinnamon/cinnamon)

/**
 * Main controller
 */
class CinnamonController {

    // default response: list repositories
    def index() {
        render(contentType: "text/xml") {
            repositories{
                def conf = ConfThreadLocal.conf
                conf.repositories.each{Node rep ->
                    repository{
                        name(rep.selectSingleNode('name').getText())
                        categories{
                            rep.selectNodes("categories/category").each{categoryNode ->
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

}

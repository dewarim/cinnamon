package cinnamon

import grails.plugins.springsecurity.Secured
import org.dom4j.DocumentHelper
import org.dom4j.Element

@Secured(["isAuthenticated()"])
class PermissionController extends BaseController {

    def listXml() {
        def doc = DocumentHelper.createDocument()
        Element root = doc.addElement("permissions");
        Permission.list().each { permission ->
            permission.toXmlElement(root);
        }
        render(contentType: 'application/xml', text: doc.asXML())
    }
}

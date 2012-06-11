package cinnamon

import org.dom4j.Element
import org.dom4j.DocumentHelper
import grails.plugins.springsecurity.Secured

@Secured(["isAuthenticated()"])
class ObjectTypeController extends BaseController{

    def list(){
        def doc = DocumentHelper.createDocument()
        Element root = doc.addElement("objectTypes");
        ObjectType.list().each{ot ->
            root.add(ObjectType.asElement('objectType',ot))
        }
        return render(contentType: 'application/xml', text:doc.asXML())
    }
}

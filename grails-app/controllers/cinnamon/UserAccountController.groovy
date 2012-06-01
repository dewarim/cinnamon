package cinnamon

import org.dom4j.Element
import org.dom4j.Document
import org.dom4j.DocumentHelper
import grails.plugins.springsecurity.Secured

@Secured(["hasRole('_users')"])
class UserAccountController {

    def list(){
        Document doc = DocumentHelper.createDocument()
        Element root = doc.addElement("users");
        UserAccount.list().each{user ->
            root.add(UserAccount.asElement("user", user));
        }
        return render(contentType: 'application/xml', text: doc.asXML())        
    }
    
}

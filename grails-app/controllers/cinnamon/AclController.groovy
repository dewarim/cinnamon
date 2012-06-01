package cinnamon

import org.dom4j.Element
import org.dom4j.DocumentHelper

class AclController {

    def list(){
        List<Acl> results = new ArrayList<Acl>();
        if (params.id) {
            results.add(Acl.get(params.id))
        } else {
            results = Acl.list();
        }
        def doc = DocumentHelper.createDocument()
        Element root = doc.addElement("acls");
        for (Acl acl : results) {
            acl.toXmlElement(root);
        }
        return render(contentType: 'application/xml', text: doc.asXML())
    }
    
}

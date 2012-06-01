package cinnamon

import org.dom4j.Element
import org.dom4j.DocumentHelper

class FolderTypeController {

    def list() {
        def doc = DocumentHelper.createDocument()
        Element root = doc.addElement("folderTypes");
        FolderType.list().each{folderType ->
            root.add(FolderType.asElement("folderType", folderType));
        }
        return render(contentType: 'application/xml', text: doc.asXML())
    }
    
}

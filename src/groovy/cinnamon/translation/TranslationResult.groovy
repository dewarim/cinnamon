package cinnamon.translation

import cinnamon.ObjectSystemData
import org.dom4j.DocumentHelper
import org.dom4j.Element

/**
 * Result class for translationService.createTranslation
 */
class TranslationResult {
    
    ObjectSystemData targetNode
    HashSet<ObjectSystemData> newObjects = []
    
    String toXml(){
        def doc = DocumentHelper.createDocument()
        Element root = doc.addElement("createTranslation")
        Element translationId = root.addElement("translationId")
        translationId.addText(String.valueOf(targetNode.id))
        newObjects.add(targetNode)
        Element objectsNode = root.addElement("objects")
        for (ObjectSystemData object : newObjects) {
            object.toXmlElement(objectsNode)
        }
        return doc.asXML()
    }
}

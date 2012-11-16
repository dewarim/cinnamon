package cinnamon.index.indexer

import cinnamon.utils.ParamParser
import org.dom4j.Node

/**
 * Index nodes which contain encoded XML / XHTML fragments.
 * This indexer extracts the text content of the node selected by the XPath
 * statement of the index item and uses the Grails default decodeHTML method 
 * on the string.
 * The decoded data is then parsed as XML and the text content will be indexed. 
 *
 */
class EncodedFieldIndexer extends DescendingStringIndexer {
    
    public String convertNodeToString(Node node) {
        def textContent = node.getText()
        if (!textContent) {
            return ''
        }
        textContent = textContent.trim().decodeHTML()
        if (! textContent.matches('^<(?:\\?|!DOCTYPE|ENTITY).*')) {
            log.debug("encountered xml fragment")
            // if the textContent is only an XML / XHTML fragment without header, add a simple xml tag
            // before parsing. Otherwise, we may get in trouble with 
            // preceding / trailing character content like 
            // " some text <h1>a headline</h1> other text "
            textContent = "<xml>${textContent}</xml>"
        }

        Node contentNode = ParamParser.parseXml(textContent, 'error.malformed.xml')
        StringBuilder builder = descendIntoNodes(contentNode)
        return builder.toString().trim()
    }

}

package cinnamon.index.indexer;

import org.dom4j.Node;

/**
 * <p>The ReverseCompleteStringIndexer expects an XPath parameter as searchString and will store
 * the results of this search in the Lucene document.</p>
 * <p>Example: name="index.name", searchString="//name" will find all name-elements.
 * and store the <i>raw</i> results of StringBuilder(node.getText()).reverse()</p>
 * <p>Note: this may create strange Unicode results.</p>
 */
public class ReverseCompleteStringIndexer extends CompleteStringIndexer{
	
	protected String convertNodeToString(Node node){
		String value = node.getText();
		StringBuilder builder = new StringBuilder(value);
		return builder.reverse().toString();		
	}
}

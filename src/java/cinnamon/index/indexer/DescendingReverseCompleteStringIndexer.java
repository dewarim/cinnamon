package cinnamon.index.indexer;

import org.dom4j.Node;

/**
 * <p>The ReverseCompleteStringIndexer expects an XPath parameter as searchString and will store
 * the results of this search in the Lucene document.</p>
 * <p>Example: name="index.name", searchString="//name" will find all name-elements.
 * and store the <i>raw</i> results of StringBuilder(node.getText()).reverse()</p>
 * <p>Note-1: this may create strange Unicode results.</p>
 * <p>Note-2: the individual text strings of the descendant nodes will be separated by
 * a space.</p>
 */
public class DescendingReverseCompleteStringIndexer extends ReverseCompleteStringIndexer{
	
	public String convertNodeToString(Node node){
		StringBuilder builder = descendIntoNodes(node);		
		return builder.reverse().toString().trim();		
	}
}

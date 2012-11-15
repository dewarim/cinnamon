package cinnamon.index.indexer;

import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.dom4j.Node;

/**
 * <p>The DescendingCompleteStringIndexer expects an XPath parameter as searchString and will store
 * the results of this search in the Lucene document.</p>
 * <p>Example: name="index.name", searchString="//name" will find all name-elements.
 * and store the <i>raw</i> results of node.getText()</p>
 * <p>Note: The descending part of this class will go through all nodes, extract any text
 * and add a space between it and the next element. Your search strings for the Lucene search
 * should reflect this.</p> 
 * 
 */
public class DescendingCompleteStringIndexer extends CompleteStringIndexer{
	
	public DescendingCompleteStringIndexer(){
		index = Index.NOT_ANALYZED;
		store = Store.NO;
	}
	
	public String convertNodeToString(Node node){
		StringBuilder builder = descendIntoNodes(node);
		return builder.toString().trim().toLowerCase();
	}
}

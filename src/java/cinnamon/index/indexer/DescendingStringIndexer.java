package cinnamon.index.indexer;

import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.dom4j.Node;

/**
 * <p>The TreeStringIndexer expects an XPath parameter as searchString and will search for
 * one or all nodes matching the XPath expression. Those nodes and all their descendant nodes 
 * are then converted into their respective text representation and the result is indexed 
 * in the Lucene document.
 * <p>Example: name="index.name", searchString="//name" will find all name-elements.
 * and store the <i>analyzed</i> results of it and its descendants.</p>
 * Note: this is similar to the DefaultIndexer, but does add a space between all individual
 * text nodes. Where the DefaultIndexer would turn 
 * <pre>
 * {@code <p>This is <b>important</b>!</p>}
 * </pre>
 * into "This isimportant!", the TreeStringIndexer would return / index it as
 * "This is important !".
 * 
 */
public class DescendingStringIndexer extends DefaultIndexer {
	
//	Index index;
//	Store store;
	
	public DescendingStringIndexer(){
		index = Index.ANALYZED;
		store = Store.NO;
	}

	public String convertNodeToString(Node node){
		StringBuilder builder = descendIntoNodes(node);
		return builder.toString().trim();
	}
}

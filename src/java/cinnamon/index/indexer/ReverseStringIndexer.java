package cinnamon.index.indexer;

import org.dom4j.Node;

/**
 * <p>The ReverseStringIndexer expects an XPath parameter as searchString and will store
 * the results of this search in the Lucene document.</p>
 * <p>Example: an IndexItem name="index.name", searchString="//name" will find all name-elements,
 * and feed the nodes content into the ReverseStringIndexer, which will reverse the
 * text content of each node found and store the <i>analyzed</i> results of node.getText()()</p>
 * <p>This class can be used to enable easier endsWith-searches, because it is harder for
 * Lucene to find all documents which contain *.xml than name.*. By storing "foo.xml" reversed
 * as "lmx.oof", we can supply a search for "lmx.*" which finds all strings which contain
 * "*.xml".
 * </p>
 * <p>Note: this may create strange Unicode results.</p>
 */
public class ReverseStringIndexer extends DefaultIndexer{
	
	String convertNodeToString(Node node){
		String value = node.getText();
		StringBuilder builder = new StringBuilder(value);
		return builder.reverse().toString();		
	}
}

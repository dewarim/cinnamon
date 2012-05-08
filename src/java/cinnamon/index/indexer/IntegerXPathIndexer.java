package cinnamon.index.indexer;

import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;

/**
 * <p>The IntegerXPathIndexer is based upon the DefaultIndexer and expects an XPath parameter as searchString.
 * It stores the results in the Lucene document under the given name as XPath-StringValue representations.</p>
 * If the string found cannot be converted into a valid Long, no result is saved.
 */
public class IntegerXPathIndexer extends DefaultIndexer{

	transient Logger log = LoggerFactory.getLogger(this.getClass());

	 /* 
	  * length == 20, enough for 1 ExaByte
	  * Just in case you need to index the national debt database.  
	  */
	private static final DecimalFormat formatter =		
	    new DecimalFormat("00000000000000000000");

	public IntegerXPathIndexer(){
	}
	
	public static String pad(Long n) {
	  return formatter.format(n);
	}
	
	protected String convertNodeToString(Node node){
		String myLong = node.getStringValue();
		try{
			// NP-check on myLong not needed because dom4j returns at least "".
			return pad(Long.parseLong(myLong));
		}
		catch (NumberFormatException e) {
			return null;
		}
	}

}

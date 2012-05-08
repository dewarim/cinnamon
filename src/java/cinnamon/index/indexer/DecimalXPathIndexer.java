package cinnamon.index.indexer;

import org.apache.lucene.document.Field.Index;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;

/**
 * <p>The DecimalXPathIndexer is based upon the DefaultIndexer and expects an XPath parameter as searchString.
 * It stores the results in the Lucene document under the given name as XPath-StringValue representations.</p>
 * If the string found cannot be converted into a valid Double, no result is saved.
 * <p>Note: the given number is indexed with a "," as decimal separator. If you index
 * a value "1,23", you must query it as "00000000001,23000000". This way, you can safely index
 * and search decimal values without having to worry about some people using "." or ",", whether that
 * corresponds correctly to <i>their and the server's</i> locale.</p>
 * <p>This class will not create proper index entries for decimal values with "." like 1.23.</p> 
 */
public class DecimalXPathIndexer extends DefaultIndexer{

	transient Logger log = LoggerFactory.getLogger(this.getClass());

	 /* 
	  * length == 11,8
	  */
	private static final DecimalFormat formatter;

	static{
		formatter = new DecimalFormat("00000000000.00000000");
		DecimalFormatSymbols dfs = formatter.getDecimalFormatSymbols();
		dfs.setDecimalSeparator('.');
		formatter.setDecimalFormatSymbols(dfs);
	}

	public DecimalXPathIndexer(){
		  index = Index.NOT_ANALYZED;
	}
	
	
	public static String pad(Double n) {		
	  return formatter.format(n);
	}
	
	String convertNodeToString(Node node){
		String number = node.getStringValue();
		log.debug("input to decimal conversion: "+number);
		try{			
			Number n = formatter.parse(number);
			Double myDouble = n.doubleValue();
			String result = pad(myDouble);
			log.debug("result:"+result);
			return result;
		}
		catch (NumberFormatException e) {
			log.debug("decimal parsing failed.");
			return null;
		}
		catch (ParseException e) {
			log.debug("parse-Exception - decimal parsing failed.");
			return null;
		}
	}

}

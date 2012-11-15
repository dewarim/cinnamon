package cinnamon.index.indexer;

import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * <p>The DateTimeIndexer expects an XPath parameter as searchString and will store
 * the results of this search in the Lucene document.</p>
 * <p>Dates must be formatted as YYYY-MM-DDThh:mm:ss.</p> 
 */
public class DateTimeIndexer extends DefaultIndexer{

	transient Logger log = LoggerFactory.getLogger(this.getClass());
	
	public DateTimeIndexer(){
	}
	
	/**
	 * Convert a node containing a date formated as
	 * "2009-10-01T16:10:30" into an indexable string,
	 * which is the time in 
	 */
    public String convertNodeToString(Node node){
		String val = node.getStringValue();		
		log.debug("Trying to index: "+val);
		
		String result = null;
		try{
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			Date date = sdf.parse(val);
			Long millis = date.getTime();
			result = pad(millis);
		}catch (Exception e) {
			log.debug("failed to parse date:",e);
		}
		log.debug("Result of date conversion: "+result);
		return result;
	}

	private static final DecimalFormat formatter =		
	    new DecimalFormat("00000000000000000000");

	public static String pad(Long n) {
	  return formatter.format(n);
	}
	
}

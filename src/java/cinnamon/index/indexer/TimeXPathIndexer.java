package cinnamon.index.indexer;

import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>The TimeXpathIndexer expects an XPath parameter as searchString and will store
 * the results of this search in the Lucene document.</p>
 * <p>Timestamps must be formatted as YYYY-MM-DDThh:mm:ss.</p> 
 */
public class TimeXPathIndexer extends DefaultIndexer{

	transient Logger log = LoggerFactory.getLogger(this.getClass());

	
	public TimeXPathIndexer(){
	}
	
	/**
	 * Convert a node containing a date formated as
	 * "2009-10-01T16:10:30" into an indexable string,
	 * omitting the date.
	 */
    public String convertNodeToString(Node node){
		String val = node.getStringValue();		
		log.debug("Trying to index: "+val);
		
		String result = null;
		try{
			// currently, index with a resolution of DAY
			String[] parts = val.split("T");
			result = parts[1]; 
			// remove - as it just takes up space in index.
			result = result.replace(":", ""); 
		}catch (Exception e) {
			log.debug("failed to split Timestamp:",e);
		}
		log.debug("Result of Timestamp conversion: "+result);
		return result; 
	}

}

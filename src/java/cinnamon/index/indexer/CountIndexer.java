package cinnamon.index.indexer;

import cinnamon.index.ContentContainer;
import cinnamon.index.Indexer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>The CountIndexer expects an XPath parameter as searchString that returns n nodes.
 * It will store the number of nodes found by this search in the Lucene document.</p>
 * <p>Example: name="index.count", searchString="//name" will find all name-elements.
 * and store the number.</p>
 */
public class CountIndexer implements Indexer {

	protected Index index;
	protected Store store;

	public CountIndexer(){
		index = Index.ANALYZED;
		store = Store.NO;
	}

	transient Logger log = LoggerFactory.getLogger(this.getClass());
	
	@SuppressWarnings("unchecked")
	@Override
	public void indexObject(ContentContainer data, Document doc, String fieldname,
			String searchString, Boolean multipleResults) {

		org.dom4j.Document indexObject = data.asDocument();
		List<Node> hits = new ArrayList<>();
		
		if(multipleResults){
			hits = indexObject.selectNodes(searchString);
		}
		else{
			Node node = indexObject.selectSingleNode(searchString);
			if(node != null){
				hits.add(node);
			}
		}
		log.debug("fieldname: "+fieldname+" count:"+ pad(hits.size()));
		doc.add(new Field(fieldname, pad(hits.size()), store, index));

	}

    private static final DecimalFormat formatter =
	    new DecimalFormat("00000000000000000000");

	public static String pad(Integer n) {
	  return formatter.format(n);
	}

    public Store getStore() {
        return store;
    }

    public void setStore(Store store) {
        this.store = store;
    }
}

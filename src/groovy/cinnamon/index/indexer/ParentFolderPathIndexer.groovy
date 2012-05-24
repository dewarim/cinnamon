package cinnamon.index.indexer;

import cinnamon.Folder;
import cinnamon.index.ContentContainer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Index the parent folder path of an object or folder.
 *
 */
public class ParentFolderPathIndexer extends DefaultIndexer{

	transient Logger log = LoggerFactory.getLogger(this.getClass());
	public ParentFolderPathIndexer() {
		index = Index.NOT_ANALYZED;
		store = Store.NO;
	}
	
	@SuppressWarnings("unchecked")
	public void indexObject(ContentContainer data, Document doc, String fieldname,
			String searchString, Boolean multipleResults) {

		org.dom4j.Document indexObject = data.asDocument();
		List<Node> hits = new ArrayList<Node>();
		
		if(multipleResults){
			hits = indexObject.selectNodes(searchString);
		}
		else{
			Node node = indexObject.selectSingleNode(searchString);
			if(node != null){
				hits.add(node);
			}
		}
		
		for(Node node : hits){
			String nodeValue = convertNodeToString(node);
			if(nodeValue != null){
								
				// fieldValue should be: osd.parent or folder.parent
				Folder folder = (Folder) data.indexable.get(nodeValue);
                String path = folder.fetchPath();
				log.debug("fieldname: "+fieldname+" value: "+ path);
				doc.add(new Field(fieldname, path.toLowerCase(), store, index));
			}
			else{
				log.debug("nodeValue == null");
			}
		} 	
	}
		
}

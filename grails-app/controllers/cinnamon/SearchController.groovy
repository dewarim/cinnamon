package cinnamon

import org.dom4j.Element
import cinnamon.interfaces.XmlConvertable
import org.dom4j.DocumentHelper
import cinnamon.utils.ParamParser
import org.dom4j.Document
import org.dom4j.Node
import cinnamon.index.Indexable
import cinnamon.index.ResultCollector

class SearchController extends BaseController {

    def searchObjects(){
        try{
        Set<XmlConvertable> resultStore;        
        resultStore = fetchSearchResults(ObjectSystemData.class);
        def doc = DocumentHelper.createDocument()
        Element root = doc.addElement("objects");
        root.addAttribute("total-results", String.valueOf(resultStore.size()));

        if (params.containsKey("page_size")) {
            addPagedResultsToElement(root, resultStore);
        } else {
            for (XmlConvertable conv : resultStore) {
                conv.toXmlElement(root);
            }
        }

            addPathFolders(doc);
            log.debug("searchObjects result: \n${doc.asXML()}")
            return render(contentType: 'application/xml', text: doc.asXML())
        }
        catch (Exception e){
            log.debug("failed searchObjects: ",e)
            renderExceptionXml(e)
        }
    }

    protected void addPagedResultsToElement(Element root, Set<XmlConvertable> resultStore) {
        List<XmlConvertable> itemList = new ArrayList<XmlConvertable>();
        itemList.addAll(resultStore);

        if (itemList.isEmpty()) {
            // if result list is empty, we can skip further sorting and serializing.
            return;
        }

        Collections.sort(itemList); // sort by id

        int pageSize = ParamParser.parseLong(params.page_size, "error.param.max_results").intValue();
        int page = 1;
        if (params.page ){
            page = ParamParser.parseLong(params.page, "error.param.page").intValue();
        }

        // to prevent index-out-of-bound exception:
        if (page <= 0) {
            page = 1;
        }
        if (pageSize <= 0) {
            pageSize = 1;
        }

        int start = pageSize * (page - 1);
        int end = pageSize * page;
        for (int x = start; x < end && x < itemList.size(); x++) {
            itemList.get(x).toXmlElement(root);
        }
    }

    // TODO: move to service?
    /**
     * Add a parentFolders node to the document's root node which contains
     * all referenced folder's ancestors up to the root node.
     *
     * @param doc document with serialized Folders and or OSDs.
     */
    protected void addPathFolders(Document doc) {
        List<Node> parentFolders = doc.selectNodes("//folder/parentId|//object/parentId");
        log.debug("# of parentFolderNodes: " + parentFolders.size());
        /*
         * The second set "ids" is used so we do not have to perform full equals() on a potentially
         * large number of Folder objects.
         */
        Set<Long> ids = new HashSet<Long>();
        Set<XmlConvertable> folders = new HashSet<XmlConvertable>();
        for (Node node : parentFolders) {
            Long id = Long.parseLong(node.getText());
            if (ids.contains(id)) {
                // folder is already in result set.
                continue;
            }
            Folder folder = Folder.get(id);
            folders.add(folder);
            // check if we need to add the folder's parent:
            if (!ids.contains(folder.getParent().getId())) {
                List<Folder> parents = folder.getParentFolders(folder);
                folders.addAll(parents);
                for (Folder parent : parents) {
                    ids.add(parent.getId());
                }
            }
        }

        // add serialized elements to response document:
        Element root = doc.getRootElement();
        Element pathFolderNode = root.addElement("parentFolders");
        for (XmlConvertable folder : folders) {
            folder.toXmlElement(pathFolderNode);
        }
    }

    // TODO: move to service?
    protected Set<XmlConvertable> fetchSearchResults(Class<? extends Indexable> indexable) {
        log.debug("start search");
        def result
        if (params.xmlQuery){
            result = luceneService.searchXml(params.query, session.repositoryName, null)
        }
        else{
            result = luceneService.search(params.query, session.repositoryName, null)
        }
        def itemMap = result.filterResultToMap(null, itemService)
        log.debug("Received search results, now filtering");
        Validator val = new Validator(userService.user);
        
        log.debug("before filterResults");
        HashSet<XmlConvertable> resultSet = new HashSet<XmlConvertable>()
        log.debug("keys in itemMap: ${itemMap.keySet()}")
        resultSet.addAll(val.filterUnbrowsableObjects(itemMap.get(ObjectSystemData.class.name)))
        resultSet.addAll(val.filterUnbrowsableFolders(itemMap.get(Folder.class.name)))
        return resultSet
    }
}

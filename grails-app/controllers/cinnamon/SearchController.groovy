package cinnamon

import cinnamon.index.SearchableDomain
import org.dom4j.Element
import cinnamon.interfaces.XmlConvertable
import org.dom4j.DocumentHelper
import org.dom4j.Document
import org.dom4j.Node
import cinnamon.index.Indexable
import grails.plugin.springsecurity.annotation.Secured

@Secured(["isAuthenticated()"])
class SearchController extends BaseController {

    def searchObjects(Integer page_size, Integer page, Boolean include_summary) {
        try {
            Set<XmlConvertable> resultStore;
            resultStore = fetchSearchResults(ObjectSystemData.class);
            def doc = DocumentHelper.createDocument()
            Element root = doc.addElement("objects");
            root.addAttribute("total-results", String.valueOf(resultStore.size()));

            if (params.containsKey("page_size")) {
                addPagedResultsToElement(root, resultStore, page_size, page, null, include_summary);
            }
            else {
                for (XmlConvertable conv : resultStore) {
                    conv.toXmlElement(root, include_summary);
                }
            }

            addPathFolders(doc, include_summary);
            log.debug("searchObjects result: \n${doc.asXML()}")
            render(contentType: 'application/xml', text: doc.asXML())
        }
        catch (Exception e) {
            log.debug("failed searchObjects: ", e)
            renderExceptionXml(e.message)
        }
    }
    
    def searchObjectsXml(String query, Integer page_size, Integer page, String metaset_list, Boolean include_summary) {
        try {
            List metasets = []
            if(metaset_list){
                metasets = metaset_list.split(/,\s*/)
            }
            log.debug("metasets: $metasets")
            doSearch(query, page_size, page, SearchableDomain.OSD, metasets, include_summary)                      
        }
        catch (Exception e) {
            log.debug("failed searchObjects: ", e)
            renderExceptionXml(e.message)
        }
    }
    
    protected void doSearch(query, pageSize, page, domain, List metasets, Boolean include_summary){
        def fields = params.list('field')
        Set<XmlConvertable> resultStore = luceneService.fetchSearchResults(query, repositoryName, userService.user, domain, fields);
        def doc = DocumentHelper.createDocument()
        Element root = doc.addElement(domain.xmlRoot);
        root.addAttribute("total-results", String.valueOf(resultStore.size()));
        log.debug("search, total-results: "+resultStore.size())
        if (pageSize) {
            addPagedResultsToElement(root, resultStore, pageSize, page, metasets, include_summary);
        }
        else {
            resultStore.each { convertable ->
                convertable.toXmlElement(root, metasets, include_summary);
            }
        }
        // add parent folders of search results to enable display of folder structure without
        // repeated path reloads.
        addPathFolders(doc, include_summary);
        render(contentType: 'application/xml', text: doc.asXML())
    }

    protected void addPagedResultsToElement(Element root, Set<XmlConvertable> resultStore, Integer pageSize, Integer 
            currentPage, List metasets, Boolean includeSummary) {
        List<XmlConvertable> itemList = new ArrayList<XmlConvertable>();
        itemList.addAll(resultStore);

        if (itemList.isEmpty()) {
            // if result list is empty, we can skip further sorting and serializing.
            return;
        }

        Collections.sort(itemList); // sort by id
        int page = currentPage ?: 1

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
            itemList[x].toXmlElement(root, metasets, includeSummary)
        }
    }

    // TODO: move to service?
    /**
     * Add a parentFolders node to the document's root node which contains
     * all referenced folder's ancestors up to the root node.
     *
     * @param doc document with serialized Folders and or OSDs.
     */
    protected void addPathFolders(Document doc, Boolean includeSummary) {
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
            if (!ids.contains(folder.parent.id)) {
                List<Folder> parents = folder.getParentFolders(folder);
                folders.addAll(parents);
                for (Folder parent : parents) {
                    ids.add(parent.getId());
                }
            }
        }

        // add serialized elements to response document:
        Element root = doc.rootElement;
        Element pathFolderNode = root.addElement("parentFolders");
        for (XmlConvertable folder : folders) {
            folder.toXmlElement(pathFolderNode, includeSummary);
        }
    }

    // TODO: move to service?
    protected Set<XmlConvertable> fetchSearchResults(Class<? extends Indexable> indexable) {
        log.debug("start search");
        def result
        if (params.xmlQuery) {
            result = luceneService.searchXml(params.query, repositoryName, null)
        }
        else {
            result = luceneService.search(params.query, repositoryName, null)
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

    /**
     * Search for items indexed by Lucene.
     * <h2>Needed permissions</h2>
     * BROWSE_FOLDER (per folder, otherwise it will be filtered)
     *
     * @param query xml string with the Lucene-xml-query
     * @param page_size optional: an integer value to use paged results
     * @param page optional: select which page of paged results to return.
     *            Defaults to 1 if [page_size] is used without
     *            a valid [page] value. Parameter is ignored if no page_size is set.
     * @param field list of stored field values to return - this parameter may be supplied multiple times.
     * @return XML-Response:
     *         <pre>
     * {@code
     *              <folders>
     *                <folder><id>5</id>...</folder>
     *                <folder>...</folder>
     *                <parentFolders>
     *                  <folder>...</folder>
     *                </parentFolders>
     *              </folders>
     *}
     *         </pre>
     */
    def searchFolders(String query, Integer page_size, Integer page, String metaset_list, Boolean include_summary) {
        try{
            def metasets = []
            if(metaset_list){
                metasets = metaset_list.split(/,\s*/)
            }
            doSearch(query, page_size, page, SearchableDomain.FOLDER, metasets as List, include_summary)
        }
        catch (Exception e){
            renderExceptionXml('Failed to searchFolders',e)
        }
    }

}

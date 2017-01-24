package cinnamon

import cinnamon.references.Link
import cinnamon.references.LinkType

import grails.plugin.springsecurity.annotation.Secured
import cinnamon.global.PermissionName
import cinnamon.exceptions.CinnamonException
import cinnamon.global.Constants
import cinnamon.relation.RelationType
import org.dom4j.DocumentHelper
import org.dom4j.Element
import cinnamon.index.LuceneResult
import cinnamon.index.SearchableDomain
import grails.gsp.PageRenderer

@Secured(["isAuthenticated()"])
class FolderController extends BaseController {
    
    PageRenderer groovyPageRenderer;

    @Secured(["isAuthenticated()"])
    def index() {
        try {
            Folder rootFolder = folderService.findRootFolder()
            if (!rootFolder) {
                def logoutMessage = message(code: "error.no.rootFolder")
                return redirect(controller: 'logout', action: 'info', params: [logoutMessage: logoutMessage])
            }
            Collection childFolders = fetchChildFolders(rootFolder)
            Map grandChildren = [:]
            Map folderConfigs = [:]
            Set<Folder> contentSet = new HashSet<Folder>()
            childFolders.each { Folder child ->
                folderConfigs = folderService.addToFolderConfigs(child, folderConfigs)
                Collection<Folder> gc = fetchChildFolders(child)
                grandChildren.put(child, gc)

                if (folderService.hasContent(child)) {
                    contentSet.add(child)
                }

                def grandChildrenWithContent = fetchChildrenWithContent(child)
                contentSet.addAll(grandChildrenWithContent)
            }
            def triggerSet = folderService.createTriggerSet(params.folder, params.osd)
            session.triggerFolder = params.folder
            session.triggerOsd = params.osd                       
            
            return [rootFolder: rootFolder,
                    contentSet: contentSet,
                    grandChildren: grandChildren,                    
                    children: childFolders,
                    triggerSet: triggerSet,
                    triggerFolder: params.folder,
                    msgList: flash.msgList,
                    folderConfigs: folderConfigs,
            ]

        }
        catch (Exception e) {
            log.warn("failed to show index:", e)
            def logoutMessage = message(code: 'error.loading.folders', args: [e.getMessage()])
            return redirect(controller: 'logout', action: 'info', params: [logoutMessage: logoutMessage])
        }


    }

    // not in folderService because it needs access to session.
    protected Collection<Folder> fetchChildrenWithContent(Folder folder) {
        Collection<Folder> folderList =
            Folder.findAll("from Folder as f where f.parent=:parent and f in (select p.parent from ObjectSystemData as p where p.parent.parent=:parent2)",
                    [parent: folder, parent2: folder])
        Validator validator = fetchValidator()
        return validator.filterUnbrowsableFolders(folderList)
    }

    def fetchFolder() {
        try {
            Folder folder = fetchAndFilterFolder(params.folder)

            def childFolders = fetchChildFolders(folder)
            def grandChildren = [:]

            def childrenWithContent = fetchChildrenWithContent(folder)
            Set<Folder> contentSet = new HashSet<Folder>()
            contentSet.addAll(childrenWithContent)
            def folderConfigs = [:]
            childFolders.each {child ->
                folderConfigs = folderService.addToFolderConfigs(child, folderConfigs)
                def gc = fetchChildFolders(child)
                if (gc.isEmpty()) {
                    log.debug("${child.name} has no subfolders.")
                }
                else {
                    log.debug("${child.name} has subfolders.")
                }
                grandChildren.put(child, gc)

                def grandChildrenWithContent = fetchChildrenWithContent(child)
                contentSet.addAll(grandChildrenWithContent)

            }

            def triggerSet = null
            if (session.triggerFolder) {
                triggerSet = folderService.createTriggerSet(session.triggerFolder, session.triggerOsd)
            }

            render(
                    template: "/folder/subFolders",
                    model: [folder: folder,
                            children: childFolders,
                            grandChildren: grandChildren,
                            contentSet: contentSet,
                            triggerSet: triggerSet,
                            triggerFolder: session.triggerFolder,
                            folderConfigs: folderConfigs
                    ])
        }
        catch (Exception e) {
            log.debug("fetchFolder failed", e)
            renderException(e.message)
        }
    }

    def fetchFolderContent() {
        def repositoryName = repositoryName
        def user = userService.user
        Folder folder
        try {
            folder = fetchAndFilterFolder(params.folder ?: params.id)

            log.debug("found folder. ${params.folder}: $folder")
            log.debug("fetch OSDs")
            def osdList = folderService.getObjects(user, folder, repositoryName, params.versions)
            def previews = osdService.fetchPreviews(osdList, grailsApplication.config.previewSize ?: 64)
            
            /*
            * if this folder contains the triggerOsd, we add it to the osdList even if it
            * is not of the default version (all/head/branch).
            */
            def triggerOsd = session.triggerOsd
            if (triggerOsd && folder.id.toString().equals(session.triggerFolder)) {
                def id = Long.parseLong(triggerOsd)
                if (!osdList.find {it.id.equals(id)}) {
                    osdList.add(ObjectSystemData.get(triggerOsd))
                    session.triggerOsd = null
                    session.triggerFolder = null
                }
            }
            
            Set<String> permissions
            try {
                permissions = loadUserPermissions(folder.acl)
            } catch (RuntimeException ex) {
                log.debug("getUserPermissions failed", ex)
                throw new RuntimeException('error.access.failed')
            }
            
            def folderTemplate = folderService.fetchFolderTemplate(folder.type.config)            
            def osdListTemplate = folderService.fetchOsdListTemplate(folder.type.config)
            
            render(template: folderTemplate, model: [folder: folder,
                    osdList: osdList,
                    previews:previews,
                    permissions: permissions,
                    folders: folderService.getFoldersInside(user, folder),
                    superuserStatus: userService.isSuperuser(user),
                    selectedVersion: params.versions,
                    osdListTemplate:osdListTemplate,
                    versions: [all: 'folder.version.all', head: 'folder.version.head', branch: 'folder.version.branch']
            ])
        }
        catch (Exception e) {
            log.debug("fetchFolderContent failed", e)
            renderException(e.message)
        }
    }

    def fetchFolderMeta() {
        try {
            Folder folder = fetchAndFilterFolder(params.folder)

            Set<String> permissions
            try {
                permissions = loadUserPermissions(folder.acl)
                log.debug("permissions. $permissions")
            } catch (RuntimeException ex) {
                log.debug("getUserPermissions failed", ex)
                renderErrorXml('error.access.failed')
                return
            }

            render(template: '/folder/folderMeta', model: [folder: folder, permissions: permissions])
        }
        catch (Exception e) {
            log.debug("renderMetadata failed", e)
            renderException(e.message)
        }
    }

    def renderMetadata() {
        try {
            Folder folder = fetchAndFilterFolder(params.folder)
            render(template: 'renderMetadata', model: [folder: folder])
        }
        catch (Exception e) {
            log.debug("renderMetadata failed", e)
            renderException(e.message)
        }

    }

    def editMetadata() {
        try {
            Folder folder = fetchAndFilterFolder(params.folder)
            render(template: '/folder/editMetadata', model: [folder: folder])
        }
        catch (Exception e) {
            log.debug("editMetadata failed", e)
            renderException(e.message)
        }
    }

    def saveMetadata() {
        Folder folder = null
        try {
            folder = fetchAndFilterFolder(params.folder)

            def metadata = params.metadata
            if (!metadata || metadata.trim().length() == 0) {
                metadata = '<meta/>'
            }

            // only save if folder has changed:
            if (!folder.metadata.equals(metadata)) {
                log.debug("trying to save metadata '$metadata'")
                folder.metadata = metadata
            }
            else {
                log.debug("metadata is unchanged")
            }
            render(template: 'renderMetadata',
                    model: [folder: folder, permissions: loadUserPermissions(folder.acl)])
        }
        catch (Exception e) {
            LocalRepository.cleanUp()
            log.debug("failed to update folder metadata: ", e)
            if (folder) {
                render(template: 'editMetadata', model: [folder: folder, saveMetaError: message(code: e.message),
                        metadata: params.metadata
                ])
            }
            else {
                renderException(e.message)
            }
        }

    }

    def editName() {
        try {
            def folder = fetchAndFilterFolder(params.folder)
            render(template: 'editName', model: [folder: folder])
        }
        catch (Exception e) {
            log.debug("failed: editName", e)
            renderException(e.message)
        }
    }

    def editAcl() {
        try {
            def folder = fetchAndFilterFolder(params.folder)
            render(template: 'editAcl', model: [folder: folder])
        }
        catch (Exception e) {
            log.debug("failed: editAcl", e)
            renderException(e.message)
        }
    }

    def editOwner() {
        try {
            def folder = fetchAndFilterFolder(params.folder)
            render(template: 'editOwner', model: [folder: folder])
        }
        catch (Exception e) {
            log.debug("failed: editOwner", e)
            renderException(e.message)
        }
    }

    def editType(){
        try {
            def folder = fetchAndFilterFolder(params.folder)
            render(template: 'editType', model: [folder: folder])
        }
        catch (Exception e) {
            log.debug("failed: editType", e)
            renderException(e.message)
        }
    }

    static List<String> allowedFields = ['name', 'acl', 'type', 'owner']

    protected Boolean fieldNameAllowed(String name) {
        return allowedFields.contains(name)
    }

    def saveField() {
        try {
            def folder = fetchAndFilterFolder(params.folder, [PermissionName.EDIT_FOLDER])


            if (fieldNameAllowed(params.fieldName)) {
                def id = params.fieldValue
                switch (params.fieldName) {
                    case 'name': folder.name = params.fieldValue; break;
                    case 'owner': folder.owner = UserAccount.get(id); break;
//                    case 'format':osd.format= Format.get(id);break;
                    case 'type': folder.type = FolderType.get(id); break;
                    case 'acl': fetchAndFilterFolder(params.folder, [PermissionName.SET_ACL]).acl = Acl.get(id); break;
                }
                fetchFolderMeta()
            }
            else {
                render(status: 401, text: message(code: 'error.illegal.parameter', args: [params.fieldName?.encodeAsHTML()]))
            }
        } catch (Exception e) {
            log.debug("failed to save field: ", e)
            renderException(e.message)
        }
    }

    def create() {
        def parent = null
        try {
            parent = fetchAndFilterFolder(params.parent, [PermissionName.CREATE_FOLDER])
            render(template: '/folder/create', model: [parent: parent])
        }
        catch (Exception e) {
            log.debug("create folder failed: ", e)
            flash.message = message(code: e.message)
            return redirect(controller: 'folder', action: 'index', model: [folder: parent?.id])
        }
    }

    def save() {
        def parentFolder = null
        try {
            // TODO: validation of new folder.
            parentFolder = fetchAndFilterFolder(params.parent, [PermissionName.CREATE_FOLDER])
            def folder = new Folder()
            folder.parent = parentFolder
            folder.name = params.name
            folder.type = FolderType.get(params.folderType)
            folder.acl = parentFolder.acl
            folder.owner = userService.user
            folder.save()
            return redirect(controller: 'folder', action: 'index', params: [folder: folder.id])
        }
        catch (Exception e) {
            log.debug("save folder failed: ", e)
            flash.message = message(code: e.message)
            LocalRepository.cleanUp()
            return redirect(controller: 'folder', action: 'index', params: [folder: parentFolder?.id])
        }
    }

    def zipFolder() {
        def folder = null
        try {
            folder = fetchAndFilterFolder(params.folder)
            def latestHead = params.versions == 'head' || params.latest_head == 'true' ?: false
            def latestBranch = params.versions == 'branch' || params.latest_branch == 'true' ?: false
            if (params.versions == 'all') {
                latestBranch = null
                latestHead = null
            }
            log.debug("versions: latestHead = ${latestHead} latestBranch = ${latestBranch}")
            def repositoryName = repositoryName
            def user = userService.user
            def validator = new Validator(user)
            File zipFile = folderService.createZippedFolder(folder, latestHead, latestBranch, validator);
            if (params.target_folder_id) {
                // create an OSD and store zip file.
                Folder targetFolder = fetchAndFilterFolder(params.target_folder_id, [PermissionName.CREATE_OBJECT])
                validator.validateCreate(targetFolder);
                ObjectType objectType = ObjectType.findByName(params.object_type_name ?: Constants.OBJTYPE_DEFAULT)
                Format format = Format.findByName("zip");
                if (format == null) {
                    throw new CinnamonException("error.missing.format", "zip");
                }
                final String filename = folder.getName() + "_archive";
                ObjectSystemData osd = new ObjectSystemData(filename, user, targetFolder);
                osd.setType(objectType);
                if (params.object_meta) {
                    osd.storeMetadata(params.object_meta)
                }
                try {
                    String contentPath = ContentStore.copyToContentStore(zipFile.getAbsolutePath(), repositoryName);
                    if (contentPath.length() == 0) {
                        throw new CinnamonException("error.store.upload");
                    }
                    osd.setContentPathAndFormat(contentPath, format, repositoryName);
                }
                catch (IOException e) {
                    throw new CinnamonException("error.store.upload", e);
                }
                osd.save()
            }
            else {
                response.setHeader("Content-disposition", "attachment; filename=${zipFile.name.encodeAsURL()}.zip");
                response.contentType = 'application/zip'
                response.outputStream << zipFile.newInputStream()
                response.outputStream.flush()
                return null
            }

        }
        catch (Exception e) {
            LocalRepository.cleanUp()
            log.debug("zip folder failed: ", e)
            flash.message = message(code: 'zip.folder.fail', args: [e.message])
            return redirect(controller: 'folder', action: 'index', params: [folder: folder?.id])
        }
    }

    // AJAX
    def fetchRelationTypeDialog(Long id) {
        RelationType rt = RelationType.get(id)
        render(template: 'fetchRelationTypeDialog', model: [relationType: rt])
    }

    def loadSelectionFolders(Long id, Long osd){
        try {
            Folder folder = fetchAndFilterFolder(id)
            def folders = folder.subfolders.findAll { Folder f ->
                folderService.mayBrowseFolder(f, userService.user)
            }
            folders.remove(folder) // remove root folder which includes itself.
            render(template: 'contentFolder', model: [currentFolder: folder,
                    folderType: params.folderType?.encodeAsHTML(),
                    osd: ObjectSystemData.get(osd),
                    folders: folders])
        }
        catch (Exception e) {
            log.debug(" failed", e)
            renderException(e.message)
        }
    }
    
    def loadFolderContent(Long osd, Long folder, String folderType){
        try {
            Folder currentFolder = fetchAndFilterFolder(folder)
            List osds = folderService.getFolderContent(currentFolder, false).findAll{
                folderService.mayBrowseFolder(currentFolder, userService.user) 
            }
            switch(folderType){
                case 'relation':
                    def relOsd = ObjectSystemData.get(osd)
                    osds.add(0,relOsd)
                    render(template: "/folder/folderContent/relationFolderContent",
                            model: [osds: osds,                                    
                                    folderType: folderType?.encodeAsHTML(),
                            ])
                    break;
                default: throw new RuntimeException('forbidden folderType.')
            }
        }
        catch (Exception e) {
            log.debug(" failed", e)
            renderException(e.message)
        }
    }
    
    
    //-----------------------------------------------------------------
    // Methods used by the desktop client (expecting XML responses)
    def fetchSubFolders(Boolean include_summary) {
        def user = userService.user
        Folder folder
        try {
            def id = [params.folder, params.id, params.parentid].find {it}
            folder = fetchAndFilterFolder(id)

            log.debug("found folder. ${params.folder}: $folder")

            Set<String> permissions
            try {
                permissions = loadUserPermissions(folder.acl)
            } catch (RuntimeException ex) {
                log.debug("getUserPermissions failed", ex)
                throw new RuntimeException('error.access.failed')
            }
            def val = new Validator(user)
            def folders = val.filterUnbrowsableFolders(folderService.getSubfolders(folder))
            def doc = DocumentHelper.createDocument()
            def root = doc.addElement('folders')
            root.addAttribute('folderId', folder.id.toString())
            root.addAttribute('parentId', folder.parent.id.toString())
            folders.each {f ->
                f.toXmlElement(root, include_summary)
            }

            log.debug("Looking for links.");
            Collection<Link> links = linkService.findLinksIn(Folder.get(params.parentid), LinkType.FOLDER);
            log.debug("Found "+links.size()+" links.");
            for(Link link : links){
                try{
                    val.validatePermission(link.acl, PermissionName.BROWSE_FOLDER)
                    val.validatePermission(link.folder.acl, PermissionName.BROWSE_FOLDER)
                    Element folderNode = link.folder.toXmlElement(root, include_summary);
                    linkService.addLinkToElement(link, folderNode);
                }
                catch (CinnamonException e){
                    log.debug("filter unbrowsable link / linked folder:",e);
                }
            }
            log.debug("fetchSubFolders.result:\n${doc.asXML()}")
            render(contentType: 'application/xml', text: doc.asXML())
        }
        catch (Exception e) {
            log.debug("fetchFolderContent failed for ${params}", e)
            renderExceptionXml(e.message)
        }
    }

    /**
     * Retrieve the folder with the given path.
     * Do not specify the root folder in the path parameter, it will be
     * automatically prepended.
     * <h2>Needed permissions</h2>
     * BROWSE_FOLDER (for each individual folder, else it will be filtered.)
     *
     * @param path path in the form /folder1/folder2/...
     * @param autocreate optional boolean parameter (true,false),
     *         default:false, will create missing folders if allowed)
     * @return XML-Response
     *         <pre>
     *           {@code
     *           <folders>
     *              <folder><id>5</id>...</folder>
     *               ...
     *           </folders>
     *          }
     *          </pre>
     */
    def fetchFolderByPath(String path, Boolean include_summary) {
        try {
            Boolean autoCreate = params.autocreate && params.autocreate.equals("true");
            Validator validator = new Validator(userService.user);
            List<Folder> folderList = folderService.findAllByPath(path, autoCreate, validator);
            log.debug("*** folder list ***")
            log.debug "$folderList"
            def doc = DocumentHelper.createDocument()
            def root = doc.addElement('folders')
            folderList.each {folder ->
                try {
                    validator.validateGetFolder(folder);
                } catch (Exception e) {
                    log.debug("", e);
                    return
                }
                folder.toXmlElement(root, include_summary);
            }
            log.debug("result of fetchFolderByPath: ${doc.asXML()}")
            render(contentType: 'application/xml', text: doc.asXML())
        }
        catch (Exception e) {
            log.debug("fetchFolderByPath: ",e)
            renderExceptionXml(e.message)
        }
    }

    /**
     * Fetch an XML representation of the given folder, provided the current user has read-access.
     * @param id the id of the folder. If 0, return the root folder.
     * @return an XML document in the form /folders/folder, containing the requested folder 
     * as well as its ancestors, if any.
     */
    def fetchFolderXml(Long id, Boolean include_summary) {
        log.debug("Getfolderbyid: " + id);
        try {            
            Folder folder
            if(id == 0L){
                folder = folderService.findRootFolder()
            }
            else{
                folder = Folder.get(id);
            }
            def validator = new Validator(userService.user)
            validator.validateGetFolder(folder);
            List<Folder> folderList = new ArrayList<Folder>();
            folderList.add(folder);
            // TODO: permission check on ancestor folders?
            folderList.addAll(folder.getAncestors());
            // validator.filterUnbrowsableFolders()

            def doc = DocumentHelper.createDocument()
            Element root = doc.addElement("folders");
            folderList.each {
                it.toXmlElement(root, include_summary)
            }      
//            log.debug"fetchFolderXml output:\n${doc.asXML()}"
            render(contentType: 'application/xml', text: doc.asXML())
        }
        catch (Exception e) {
            log.debug("fetchFolderXml failed: ", e)
            renderExceptionXml(e.message)
        }
    }

    def searchSimple () {
        def query = params.query
        try {
            def xmlQuery = groovyPageRenderer.render(template:'/search/simpleSearchQuery', model:[query:query])
            LuceneResult result = luceneService.searchXml(xmlQuery, repositoryName, null)
            def itemMap = result.filterResultToMap(null, itemService)
            log.debug("itemMap: $itemMap")
            def folders = itemMap.get(SearchableDomain.FOLDER.name)
            def objects = itemMap.get(SearchableDomain.OSD.name)
            
            render(template: 'searchResult', model: [searchResult: result, folders: folders, objects: objects])
        }
        catch (Exception e) {
            renderException(e.message)
        }
    }
    
    
    //------------------- XML API commands -----------------------
    /**
     * The createfolder command creates a folder in the repository with the given name.
     * The folder name must be unique for the same parent. To create a subfolder of root,
     * specify 0 as parent id. The id of the newly created folder is returned.<br>
     * <br>
     * The metadata must be specified in one row.
     *
     * @param cmd HTTP request parameter map:
     *            <ul>
     *            <li>command=createfolder</li>
     *            <li>metadata=xml metadata (optional)</li>
     *            <li>name=folder name</li>
     *            <li>parentid=parent folder id</li>
     *            <li>aclid=id of the new folder's acl</li>
     *            <li>ownerid=id of the folder's owner</li>
     *            <li>[typeid]= id of the folderType (if not set, use default_folder_type)</li>
     *            <li>summary= xml summary of folder or folder content, defaults to empty 'summary' element</li>
     *            </ul>
     * @return XML-Response:
     *         Folder serialized to XML.
     *         <h2>Needed permissions</h2>
     *         CREATE_FOLDER
     */
    @Secured(["isAuthenticated()"])
    def createXml(String name, Long parentid, Long aclid, Long ownerid, Long typeid, String metadata, String summary,
                  Boolean include_summary) {
        try {
            Folder parentFolder;
            if (parentid == 0L) { // 0 is considered the root folder.
                parentFolder = folderService.findRootFolder()
            }
            else {
                parentFolder = Folder.get(parentid)
            }
            
            def folderType
            if(typeid){
                folderType = FolderType.get(typeid)
            }
            else{
                folderType = FolderType.findByName(Constants.FOLDER_TYPE_DEFAULT)
            }
            def user = userService.user
            def owner = ownerid ? UserAccount.get(ownerid) : user
            def acl = aclid ? Acl.get(aclid) : parentFolder.acl
            (new Validator(user)).validateCreateFolder(parentFolder);
            Folder folder = new Folder(name: name, owner: owner, parent:parentFolder,
                                type: folderType, acl: acl, summary: summary ?: '<summary />'
            )
            folder.save()

            def doc = DocumentHelper.createDocument()
            Element root = doc.addElement("folders");
            def folderElement = folder.toXmlElement(root, include_summary);
            render(contentType: 'application/xml', text: doc.asXML())
        }
        catch (Exception e) {
            renderExceptionXml('Failed to create folder.', e)
        }
    }

    /**
     * Delete an empty folder specified by the id-parameter.
     * <h2>Needed permissions</h2>
     * DELETE_FOLDER
     *
     * @param id folder id
     * @return XML-Response:
     *         {@code
     *         <success>success.delete.folder</success>
     *         }
     */
    @Secured(["isAuthenticated()"])
    def deleteXml(Long id) {
        def folder = Folder.get(id)
        try{
            (new Validator(userService.user)).validateDeleteFolder(folder)
            folderService.deleteFolder(id, repositoryName, false)
            render(contentType: 'application/xml'){
                success('success.delete.folder')
            }
        }
        catch (Exception e){        
            renderExceptionXml('Failed to delete folder.',e)
        }
    }

    /**
     * The getFolderMeta command retrieves the metadata of the specified folder.
     * <h2>Needed permissions</h2>
     * READ_OBJECT_CUSTOM_METADATA
     *
     * @param id folder id</li>
     * @return XML-Response:
     *         The metadata of the specified folder or an XML error node.
     */
    @Secured(["isAuthenticated()"])
    def getFolderMeta(Long id) {
        try{
            def folder = Folder.get(id)
            if(! folder){
                throw new CinnamonException('error.folder.not.found')
            }
            (new Validator(userService.user)).validateGetFolderMeta(folder);
            def xml = folder.getMetadata()
            render(contentType: 'application/xml', text:xml)            
        }
        catch (Exception e){
            renderExceptionXml('Failed to fetch folder custom metadata', e)
        }
    }


    /**
     * Set name, parent folder, metadata, owner and/or ACL of a folder.
     *
     * @param id the Id of the folder.
     * @param parentid optional: id of new parent folder
     * @param name optional: new name for this folder
     * @param aclid optional: id of new ACL
     * @param ownerid optional: id of new owner
     * @param metadata optional: new custom metadata for this folder
     * @param typeid optional: id of new folder type
     * @param summary: xml summary of folder or folder content
     * @return XML-Response:
     *         {@code
     *         <success>success.update.folder</success>
     *         }
     */
    @Secured(["isAuthenticated()"])
    def updateFolder(Long id, Long parentid, String name,
                     String metadata, Long typeid,
                     String ownerid, Long aclid, String summary) {
        try {
            def fields = [parentid: parentid, name: name,
                    ownerid: ownerid, aclid: aclid,
                    metadata: metadata, typeid: typeid, summary: summary]
            Folder folder = fetchAndFilterFolder(id)
            (new Validator(userService.user)).validateUpdateFolder(fields, folder)
            if (!folder) {
                throw new RuntimeException('error.folder.not.found')
            }
            folder.update(fields)
            render(contentType: 'application/xml') {
                success('success.update.folder')
            }
        }

        catch (Exception e) {
            renderExceptionXml('Failed to update folder.', e)
        }
    }

    /**
     * Set summary on a folder.
     * @param content the XML summary string, defaults to empty summary element.
     * @param id id of the folder
     * @return XML-Response
     *      * {@code
     *         <success>success.set.summary</success>
     *}
     * or an error message if something went wrong.
     *
     */
    def setSummaryXml(Long id, String content) {
        try {
            def user = userService.user
            Folder folder  = Folder.get(id)
            (new Validator(user)).validateUpdateFolder([summary:content], folder)
            folder.summary = content ?: '<summary />'
            log.debug("set summary - done")
            render(contentType: 'application/xml') {
                success('success.set.summary')
            }
        }
        catch (Exception e) {
            log.debug("Failed to set summary on folder #$id.", e)
            renderExceptionXml(e.message)
        }
    }
}

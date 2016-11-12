package cinnamon

import cinnamon.references.Link
import cinnamon.references.LinkType
import cinnamon.utils.ParamParser
import org.dom4j.DocumentHelper
import org.dom4j.Element
import grails.plugin.springsecurity.annotation.Secured

import cinnamon.global.PermissionName
import cinnamon.relation.Relation
import cinnamon.i18n.Language
import cinnamon.exceptions.CinnamonException
import org.dom4j.Document

/**
 *
 */
@Secured(["isAuthenticated()"])
class OsdController extends BaseController {

    def metasetService
    def infoService

    def editMetadata() {
        try {
            ObjectSystemData osd = fetchAndFilterOsd(params.osd)
            render(template: mapTemplate('/osd/editMetadata'), model: [osd: osd])
        }
        catch (Exception e) {
            renderException(e.message)
        }
    }

    def saveMetadata() {
        try {
            ObjectSystemData osd = fetchAndFilterOsd(params.osd)
            if (!osd.metadata.equals(params.metadata)) {
                osd.storeMetadata(params.metadata)
            }
            render(template: mapTemplate('/osd/objectDetails'), model: [osd: osd, permissions: loadUserPermissions(osd.acl)])
        }
        catch (Exception e) {
            renderException(e.message)
        }
    }

    def unlockOsd() {
        try {
            ObjectSystemData osd = fetchAndFilterOsd(params.osd)
            UserAccount user = userService.user
            osd.locker = null
            render(template: mapTemplate('/folder/lockStatus'), model: [user: user, osd: osd,
                    superuserStatus: userService.isSuperuser(user)])
        }
        catch (Exception e) {
            renderException(e.message)
        }
    }

    def lockOsd() {
        try {
            ObjectSystemData osd = fetchAndFilterOsd(params.osd)
            UserAccount user = userService.user
            osdService.acquireLock(osd, user)
            render(template: mapTemplate('/folder/lockStatus'), model: [user: user, osd: osd,
                    superuserStatus: userService.isSuperuser(user)])

        }
        catch (Exception e) {
            renderException(e.message)
        }
    }

    def listRelations() {
        try {
            ObjectSystemData osd = fetchAndFilterOsd(params.osd)
            def leftRelations = Relation.findAllByLeftOSD(osd)
            def rightRelations = Relation.findAllByRightOSD(osd)

            render(template: mapTemplate('/osd/listRelations'),
                    model: [leftRelations: leftRelations, rightRelations: rightRelations,
                            osd: osd
                    ]
            )
        }
        catch (Exception e) {
            renderException(e.message)
        }

    }

    def fetchObjectDetails() {
        try {
            ObjectSystemData osd = fetchAndFilterOsd(params.osd)
            def hasRelations = Relation.find("from Relation as r where r.rightOSD=:o1 or r.leftOSD=:o2",
                    [o1: osd, o2: osd]) ? true : false
            def permissions = loadUserPermissions(osd.acl)
            def user = userService.user
            render(template: mapTemplate("/osd/objectDetails"),
                    model: [osd: osd, permissions: permissions,
                            superuserStatus: userService.isSuperuser(user),
                            hasRelations: hasRelations])
        }
        catch (Exception e) {
            log.debug("failed to fetchObjectDetails: ", e)
            renderException(e.message)
        }
    }

    def renderPreview() {
        try {
            ObjectSystemData osd = fetchAndFilterOsd(params.osd)
            def osdContent = osd.getContent(repositoryName)
            render(template: mapTemplate('/osd/objectPreview'),
                    model: [osd: osd, ctype: osd.format?.contenttype, osdContent: osdContent])
        }
        catch (Exception e) {
            renderException(e.message)
        }
    }

    def renderMetadata() {
        try {
            ObjectSystemData osd = fetchAndFilterOsd(params.osd)
            render(template: mapTemplate('/osd/renderMetadata'), model: [osd: osd])
        }
        catch (Exception e) {
            renderException(e.message)
        }
    }

    /**
     * Retrieves the content with the given id. The user may then edit the file.
     * <h2>Needed permissions</h2>
     * READ_OBJECT_CONTENT
     *
     * @param id Id of the requested object
     * @param resultfile the attachment filename 
     * @return the raw content of the object as byte stream (MIME-type: binary/octet-stream)
     */
    def getContent(Long id, String resultfile) {
        Folder folder = null
        try {
            ObjectSystemData osd = fetchAndFilterOsd(id?.toString() ?: params.osd)
            folder = osd.parent

            def filename = infoService.config.data_root + File.separator + repositoryName +
                    File.separator + osd.contentPath
            log.debug("getContent called for #${osd.id} @ $filename")
            File data = new File(filename)
            if (!data.exists()) {
                log.debug("could not find: $filename")
                throw new RuntimeException('error.file.not.found')
            }

            if (osd.contentSize == null || osd.contentSize == 0L) {
                throw new RuntimeException('error.content.not.found')
            }
            def attachmentName = resultfile ?: "${osd.name.encodeAsURL()}${osd.determineExtension()}"
            response.setHeader("Content-disposition", "attachment; filename=${attachmentName}");
            response.setContentType(osd.format.contenttype)
            response.outputStream << data.newInputStream()
            response.outputStream.flush()
            return null
        }
        catch (Exception e) {
            LocalRepository.cleanUp()
            flash.mesasge = message(code: e.message)
            defaultRedirect([folder: folder?.id])
        }
    }

    def editName() {
        try {
            def osd = fetchAndFilterOsd(params.osd)
            render(template: mapTemplate('/osd/editName'), model: [osd: osd])
        }
        catch (Exception e) {
            log.debug("failed: editName", e)
            renderException(e.message)
        }
    }

    def editAcl() {
        try {
            def osd = fetchAndFilterOsd(params.osd)
            render(template: mapTemplate('/osd/editAcl'), model: [osd: osd])
        }
        catch (Exception e) {
            renderException(e.message)
        }
    }

    def editLanguage() {
        try {
            def osd = fetchAndFilterOsd(params.osd)
            render(template: mapTemplate('/osd/editLanguage'), model: [osd: osd])
        }
        catch (Exception e) {
            renderException(e.message)
        }
    }

    def editOwner() {
        try {
            def osd = fetchAndFilterOsd(params.osd)
            render(template: mapTemplate('/osd/editOwner'), model: [osd: osd])
        }
        catch (Exception e) {
            renderException(e.message)
        }
    }

    def editType() {
        try {
            def osd = fetchAndFilterOsd(params.osd)
            render(template: mapTemplate('/osd/editType'), model: [osd: osd])
        }
        catch (Exception e) {
            renderException(e.message)
        }
    }
    
    def editContent(Long osd){
        try{
            def obj = fetchAndFilterOsd(osd)
            render(template: mapTemplate('/osd/editContent'), model: [osd: obj])
        }
        catch (Exception e){
            renderException(e.message)
        }
    }

    static List<String> allowedFields = ['name', 'format', 'acl', 'objtype', 'language', 'owner']

    protected Boolean fieldNameAllowed(String name) {
        return allowedFields.contains(name)
    }

    def saveField() {
        try {
            def osd = fetchAndFilterOsd(params.osd, [PermissionName.WRITE_OBJECT_SYS_METADATA])


            if (fieldNameAllowed(params.fieldName)) {
                def id = params.fieldValue
                switch (params.fieldName) {
                    case 'name': osd.name = params.fieldValue; break;
                    case 'owner': osd.owner = UserAccount.get(id); break;
//                    case 'format':osd.format= Format.get(id);break;
                    case 'language': osd.language = Language.get(id); break;
                    case 'objtype': osd.type = ObjectType.get(id); break;
                    case 'acl': fetchAndFilterOsd(params.osd, [PermissionName.SET_ACL]).acl = Acl.get(id); break;
                }
                fetchObjectDetails()
            }
            else {
                render(status: 401, text: message(code: 'error.illegal.parameter', args: [params.fieldName?.encodeAsHTML()]))
            }
        } catch (Exception e) {
            renderException(e.message)
        }
    }

    def create() {
        def folder = null
        try {
            folder = fetchAndFilterFolder(params.folder, [PermissionName.CREATE_OBJECT])
            return [folder: folder]
        }
        catch (Exception e) {
            flash.message = message(code: e.message)
            return defaultRedirect([folder: folder?.id])
        }
    }

    def saveObject() {
        Folder folder = null
        try {
            UserAccount user = userService.user
            folder = fetchAndFilterFolder(params.folder, [PermissionName.CREATE_OBJECT])
            def osd = osdService.createOsd(request, params, repositoryName, null, user, folder)
            return defaultRedirect([folder: folder.id, osd: osd.id])
        }
        catch (Exception e) {
            flash.message = message(code: e.message)

            log.debug("Failed to save object: ", e)
            if (!folder) {
                return redirect(controller: 'folder', action: 'index')
            }
            return redirect(controller: 'osd', action: 'create', params: [folder: folder.id])
        }
    }
    
    def saveContent(Long formatId) {
        ObjectSystemData osd = null
        try {
            UserAccount user = userService.user
            osd = fetchAndFilterOsd(params.osd, [PermissionName.WRITE_OBJECT_CONTENT])
            def folder = fetchAndFilterFolder(osd.parent.id)
            osdService.saveFileUpload(request, osd, user, formatId, repositoryName)

            // on success: redirect fetchFolderContent
            log.debug("set content on object #${osd.id}")
            redirect(controller: 'folder', action: 'index', params: [folder: params.folder, osd: params.osd])
        }
        catch (RuntimeException e) {
            LocalRepository.cleanUp()
            log.debug("Failed to set content on object ${osd?.id}: ", e)
            flash.message = message(code: e.getMessage())
            if (!osd) {
                return redirect(controller: 'folder', action: 'index')
            }
            redirect(controller: 'osd', action: 'setContent', params: [folder: params.folder, osd: params.osd])
        }
    }
    
    def saveContentJson(Long formatId) {
        ObjectSystemData osd = null
        try {
            UserAccount user = userService.user
            osd = fetchAndFilterOsd(params.osd, [PermissionName.WRITE_OBJECT_CONTENT])
            def folder = fetchAndFilterFolder(osd.parent.id)
//            osdService.saveFileUpload(request, osd, user, formatId, repositoryName)
            osdService.saveFileUpload(request, osd, user, osd.format?.id, repositoryName)

            log.debug("set content on object #${osd.id}")
            // TODO: i18n "ok"
            render(contentType: "application/json"){
                result(msg:message(code: "upload.success"))
            }
        }
        catch (RuntimeException e) {
            LocalRepository.cleanUp()
            log.debug("Failed to set content on object ${osd?.id}: ", e)
            flash.message = message(code: e.message)
            render(contentType: "application/json"){
                result(msg:e.message)
            }
        }
    }

    def newVersion() {
        try {
            def user = userService.user
            ObjectSystemData pre = fetchAndFilterOsd(params.osd, [PermissionName.VERSION_OBJECT])
            ObjectSystemData osd = new ObjectSystemData(pre, user);
            osd.root = pre.root
            osd.predecessor = pre
            osd.cmnVersion = osd.createNewVersionLabel()
            osd.locker = null
            osd.save()
            osd.fixLatestHeadAndBranch([])
            log.debug("version of new osd: ${osd.cmnVersion}")
            def osdList = folderService.getObjects(user, osd.parent, repositoryName, params.versions)
            def folderContentTemplate = folderService.fetchFolderTemplate(osd.parent.type.config)
            render(template: folderContentTemplate, model: [folder: osd.parent,
                    osdList: osdList,
                    folders: fetchChildFolders(osd.parent),
                    permissions: loadUserPermissions(osd.parent.acl),
                    superuserStatus: userService.isSuperuser(user),
                    triggerOsd: osd,
                    selectedVersion: params.versions,
                    versions: [all: 'folder.version.all', head: 'folder.version.head', branch: 'folder.version.branch']
            ])
        }
        catch (RuntimeException e) {
            log.debug("Failed to version object:", e)
            renderException(e.message)
        }
    }

    def iterate() {
        def msgMap
        def msgList = []

        def selectedFolder = params.selectedFolder
        try {
            log.debug("selected folder: ${selectedFolder}")
            Folder targetFolder = Folder.get(selectedFolder)
            def user = userService.user
            if(! params.delete){
                if (!targetFolder) {
                    throw new RuntimeException('error.folder.not.found')          
                }
                if(!folderService.checkPermissions(targetFolder, user, [PermissionName.CREATE_OBJECT])) {
                        throw new RuntimeException('error.access.denied')
                }
            }

            log.debug("*** start iterate")
            def idList = params.list("osd")
            def folderList = params.list("folder")
            if (idList.isEmpty() && folderList.isEmpty()) {
                log.debug("id list is empty - redirect")
                // nothing to do
                defaultRedirect([folder: selectedFolder])
            }
            def repository = repositoryName
            def versionType = VersionType.values().find { it.name() == (params.versions ?: VersionType.ALL.name()) }

            if (params.delete) {
                msgMap = osdService.deleteList(idList, repository, versionType)
                msgList.addAll(convertMsgMap(msgMap))
                msgMap = folderService.deleteList(folderList, repository, true)
                msgList.addAll(convertMsgMap(msgMap))
            }
            else if (params.move) {
                log.debug("*** will move objects into folder: ${selectedFolder}")
                msgMap = osdService.moveToFolder(idList, selectedFolder, repository, versionType, user)
                msgList.addAll(convertMsgMap(msgMap))
                msgMap = folderService.moveToFolder(folderList, selectedFolder, repository, user)
                msgList.addAll(convertMsgMap(msgMap))
            }
            else if (params.copy) {
                log.debug("*** will copy objects into folder: ${selectedFolder}")
                msgMap = copyService.copyObjectsToFolder(idList, selectedFolder, repository, versionType, user)
                msgList.addAll(convertMsgMap(msgMap))
                msgMap = copyService.copyFoldersToFolder(folderList, selectedFolder, repository, versionType, user)
                msgList.addAll(convertMsgMap(msgMap))
            }
            log.debug("*** done iterate")
        }
        catch (Exception e) {
            LocalRepository.cleanUp()
            log.debug("Failed to iterate over ${params.osd}.", e)
            flash.message = message(code: 'iterate.fail', args: [message(code: e.message)])
        }

        flash.msgList = msgList
        defaultRedirect([folder: selectedFolder])
    }

    protected List convertMsgMap(msgMap) {
        def msgList = []
        msgMap.each { String k, List v ->
            if (v.size() == 1) {
                msgList.add(message(code: v.get(0), args: [k]))
            }
            else {
                msgList.add(message(code: v.get(0), args: [k, message(code: v.get(1))]))
            }
        }
        return msgList
    }

    //-----------------------------------------------------------------
    // commands used by the desktop client:
    //-----------------------------------------------------------------

    def fetchObjects(Boolean include_summary) {
        try {
            def folder = folderService.fetchFolder(params.parentid)
            if (!folder) {
                throw new RuntimeException('error.folder.not.found')
            }
            def user = userService.user
            List<ObjectSystemData> results = folderService.getObjects(user, folder, repositoryName, params.versions)
            Validator val = new Validator(user);
            results = val.filterUnbrowsableObjects(results);
            Document doc = osdService.generateQueryObjectResultDocument(results, include_summary);
            addLinksToObjectQuery(params.parentid, doc, val, false, include_summary)

            log.debug("objects for folder ${folder.id} / ${folder.name}:\n ${doc.asXML()}")
            render(contentType: 'application/xml', text: doc.asXML())
        }
        catch (Exception e) {
            log.debug("failed to fetch objects: ", e)
            renderExceptionXml(e.message)
        }
    }

    protected void addLinksToObjectQuery(String parentId, Document doc, Validator val, Boolean withMetadata, Boolean 
            includeSummary) {
        Folder parent = Folder.get(parentId);
        Element root = doc.getRootElement();
        Collection<Link> links = linkService.findLinksIn(parent, LinkType.OBJECT);
        log.debug("Found " + links.size() + " links.");
        for (Link link : links) {
            try {
                val.validatePermission(link.acl, PermissionName.BROWSE_OBJECT);
                val.validatePermission(link.osd.acl, PermissionName.BROWSE_OBJECT);
                if (withMetadata) {
                    val.validatePermission(link.osd.acl, PermissionName.READ_OBJECT_CUSTOM_METADATA)
                }
            } catch (Exception e) {
                log.debug("", e);
                continue;
            }
            Element osdNode = link.getOsd().toXmlElement(root, includeSummary);
            if (withMetadata) {
                osdNode.add(ParamParser.parseXml(link.getOsd().getMetadata(), null));
            }
            linkService.addLinkToElement(link, osdNode);
        }
    }

    /**
     * XML API method.
     *
     * <p>
     * The create command creates an object in the repository with the given name.
     * The object name need not be unique for the same parent. Objects can not be created in root,
     * they must be created in a folder. If a file is specified, the format must also be specified.
     * The value in the name column of the formats table must be used. The id of the newly created
     * object is returned.</p>
     * If object creation fails, an error message is returned.
     * If no file parameter is specified, an object without content is created. The setcontent
     * command can be used to add content later.<br>
     * <h2>Needed permissions</h2>
     * CREATE_OBJECT
     *
     * Request parameters (parameters in [...] are optional)
     * <ul>
     *  <li>[preid]= optional predecessor id - basically this may be used to create another (empty) version of an object.</li>
     *   <li>name = name of this object</li>
     *   <li>[appname] = internally used by desktop client to determine which DTDs etc are needed for this object</li>
     *   <li>[metadata] = XML string of metadata with required root element {@code <meta>}</li>
     *   <li>[objtype_id OR objtype]= Id or Name of object type. If no object type is specified, use Constants.OBJTYPE_DEFAULT.</li>
     *   <li>parentid = id of parent folder</li>
     *   <li>[format OR format_id] = Id or name of format (optional)</li>
     *   <li>[acl_id] = optional Id of ACL - if not specified, will use ACL of parent folder</li>
     *   <li>[language_id]=optional id of language, will use default language "und" for undetermined if not specified.</li>
     *   <li>[summary]= optional XML summary of object content or other meta information</li>
     *  </ul>
     * @return a Response which contains:
     *         <pre>
     * {@code <objectId>$id_of_new_object</objectId>}
     *         </pre>
     */
    def createOsd() {
        try {
            def user = userService.user
            ObjectSystemData osd = new ObjectSystemData(params, user, false)
            (new Validator(user)).validateCreate(osd.parent)
            osd.save()
            log.debug("osd after save: "+osd)

            if(params.metadata){
                osd.storeMetadata(params.metadata)
            }
            
            if(params.preid){
                // TODO: should probably not copy all metadata...
                // but that is currently the legacy behaviour.
                osd.storeMetadata(osd.predecessor.metadata)
            }
            log.debug("osd created: " + osd)

            if (params.containsKey("file")) {
                osdService.saveFileUpload(request, osd, user, osd.format?.id, repositoryName)
            }
            metasetService.initializeMetasets(osd, (String) params.metasets)
            render(contentType: 'application/xml') {
                objectId(osd.id.toString())
            }
        }
        catch (Exception e) {
            log.debug("failed to create OSD", e)
            renderExceptionXml(e.message)
        }
    }

    /**
     * The getobject command retrieves an object by the given id.
     * <h2>Needed permissions</h2>
     * BROWSE_OBJECT
     *
     * @param id the ide of the OSD you want to fetch.
     * @return XML-Response:
     *         XML serialized object or xml-error-doc
     */
    def fetchObject(Long id, Boolean include_summary) {
        try {
            Document doc = DocumentHelper.createDocument()
            Element root = doc.addElement("objects");
            ObjectSystemData osd = ObjectSystemData.get(id);
            if (osd == null) {
                throw new CinnamonException("error.object.not.found");
            }
            else {
                (new Validator(userService.user)).checkBrowsePermission(osd);
                osd.toXmlElement(root, include_summary)
            }
            render(contentType: 'application/xml', text: doc.asXML())
        }
        catch (Exception e) {
            log.debug("failed to fetch OSD", e)
            renderExceptionXml(e.message)
        }
    }

    //--------------------------- Cinnamon XML API methods -----------------------

    /**
     * The lock command places a lock for the session owner on an object.
     * <h2>Needed permissions</h2>
     * LOCK
     *
     * @param id = object id
     * @return XML-Response:
     * {@code
     *         <success>success.object.lock</success>
     *}
     */
    def lockXml(Long id) {
        try {
            ObjectSystemData osd = ObjectSystemData.get(id)
            def user = userService.user
            (new Validator(user)).validateLock(osd, user)
            osd.locker = user
            log.debug("lock - done.");
            render(contentType: 'application/xml') {
                success('success.object.lock')
            }
        }
        catch (Exception e) {
            log.debug("Failed to lock #$id.", e)
            renderExceptionXml(e.message)
        }
    }

    /**
     * The unlock command removes a lock from an object.
     *
     * @param id id of the object to unlock.
     * @return XML-Response
     * {@code
     *         <success>success.object.unlock</success>
     *}
     * or an error message if something went wrong.
     */
    def unlockXml(Long id) {
        try {
            def user = userService.user
            ObjectSystemData osd = ObjectSystemData.get(id)
            (new Validator(user)).validateUnlock(osd)
            osd.locker = null
            log.debug("unlock - done")
            render(contentType: 'application/xml') {
                success('success.object.unlock')
            }
        }
        catch (Exception e) {
            log.debug("Failed to unlock #$id.", e)
            renderExceptionXml(e.message)
        }
    } 

    /**
     * Set summary on an OSD.
     * @param content the XML summary string, defaults to empty summary element.
     * @param id id of the object
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
            ObjectSystemData osd = ObjectSystemData.get(id)
            (new Validator(user)).validateSetSummary(osd)
            osd.summary = content ?: '<summary />'
            log.debug("set summary - done")
            render(contentType: 'application/xml') {
                success('success.set.summary')
            }
        }
        catch (Exception e) {
            log.debug("Failed to set summary on osd #$id.", e)
            renderExceptionXml(e.message)
        }
    }

    /**
     * The copy command creates a new object in the folder specified by targetfolderid as a copy
     * of the object specified by the sourceobjid parameter.<br>
     * <br>
     * <h2>Needed permissions</h2>
     * <ul>
     * <li>READ_OBJECT_CONTENT</li>
     * <li>READ_OBJECT_CUSTOM_METADATA</li>
     * <li>READ_OBJECT_SYS_METADATA</li>
     * <li>CREATE_OBJECT</li>
     * </ul>
     *
     * @param sourceid the original object
     * @param targetfolderid = the id of the target folder
     * @param metasets optional, comma-separated list of metasetType-names which will be copied.
     * @return xml response with id of newly created object (or standard xml error message in case of error):
     * <pre>
     * {@code
     *     <objectId>12345</objectId>
     *}
     * </pre>
     */
    def copy(Long sourceid, Long targetfolderid, String metasets) {
        try {
            Folder targetFolder = Folder.get(targetfolderid)
            if (targetFolder == null) {
                throw new CinnamonException("error.folder.not_found");
            }
            // fetch source object
            ObjectSystemData osd = ObjectSystemData.get(sourceid);
            if (osd == null) {
                throw new CinnamonException("error.object.not.found");
            }
            def user = userService.user
            (new Validator(user)).validateCopy(osd, targetFolder)
            ObjectSystemData copy = osd.createClone()
            copy.setAcl(targetFolder.getAcl()) // set ACL to target folder's ACL
            copy.setParent(targetFolder)
            copy.setName("Copy_" + osd.getName())
            copy.setPredecessor(null)
            copy.setOwner(user)
            copy.setCmnVersion("1")
            copy.setLatestBranch(true)
            copy.setLatestHead(true)
            copy.setRoot(copy)
            copy.setModifier(user)
            copy.setCreator(user)
            copy.setLocker(null)
            copy.save(flush:true)
            
            copy.fixLatestHeadAndBranch([])
            osdService.copyRelations(osd, copy)
            // execute the new LifeCycleState if necessary.
            if (copy.getState() != null) {
                copy.getState().enterState(copy, copy.getState())
            }
            metasetService.copyMetasets(osd, copy, metasets)
            osdService.copyContent(repositoryName, osd, copy)
            render(contentType: 'application/xml') {
                objectId(copy.id.toString())
            }
        }
        catch (Exception e) {
            renderExceptionXml("Failed to copy OSD #$sourceid to $targetfolderid", e)
        }
    }

    /**
     * The delete command deletes the object in the repository with the given id. This operation
     * cascades over related objects, unless they are protected by the relationtype.
     * <h2>Needed permissions</h2>
     *         DELETE_OBJECT
     * @param id the id of the object that should be deleted.
     * @return XML-Response:
     *         <pre>
     * {@code <success>success.delete.object</success> }
     *         </pre> if successful, an XML-error-node if unsuccessful.
     */
    def deleteXml(Long id) {
        def osd = ObjectSystemData.get(id)
        try {
            if (!osd) {
                throw new CinnamonException('error.object.not.found')
            }
            (new Validator(userService.user)).validateDelete(osd)
            ObjectSystemData preOsd = osd.getPredecessor();
            osdService.delete(osd, repositoryName)
            if (preOsd) {
                def predecessorChildren = ObjectSystemData.findAllByPredecessor(preOsd)
                preOsd.fixLatestHeadAndBranch(predecessorChildren)
            }
            render(contentType: 'application/xml') {
                success('success.delete.object')
            }
        }
        catch (Exception e) {
            renderExceptionXml('Failed to delete object', e)
        }
    }

    /**
     * The deleteAllVersions command deletes all versions of an object.
     * This operation cascades over related objects,
     * unless they are protected by the relationtype.
     * <br/>
     * <p><i>Note</i>:
     * This command relies on a database with ascending object ids.
     * (Meaning that the HQL query will order results by id and assume that the lower an id
     * the older the object is)</p>
     *
     * @param id = object id
     * @return a HTTP response containing
     *         <pre>
     * {@code <success>success.delete.all_versions</success>}
     *         </pre> if successful, an XML-error-node if unsuccessful.
     */
    def deleteAllVersions(Long id) {
        ObjectSystemData osd = ObjectSystemData.get(id);
        def objectTree
        try {
            if (!osd) {
                throw new CinnamonException('error.object.not.found')
            }
            objectTree = osdService.findAllVersions(osd);
            // first check all objects if they may be deleted, otherwise an exception will
            // terminate the database operation but not roll back the Lucene index.
            Validator validator = new Validator(userService.user);
            for (ObjectSystemData o : objectTree) {
                validator.validateDelete(osd);
            }
            osdService.delete(osd.root, true, true, repositoryName)
            render(contentType: 'application/xml') {
                success('success.delete.all_versions')
            }
        }
        catch (Exception e) {            
            renderExceptionXml('Failed to delete object', e)
        }
    }

    /**
     * The getMeta command retrieves the metadata of the specified object.
     * <h2>Needed permissions</h2>
     * READ_OBJECT_CUSTOM_METADATA
     *
     * @param id the object id
     * @param metasets optional parameter: comma-separated list of metasetType names. 
     *        Defaults to returning the content of all metasets that are referenced by this object.
     * @return XML-Response:
     *         The metadata of the specified object.
     */
    def getOsdMeta(Long id, String metasets) {
        try {
            ObjectSystemData osd = ObjectSystemData.get(id)
            if (!osd) {
                throw new CinnamonException('error.object.not.found')
            }
            (new Validator(userService.user)).validateGetMeta(osd)

            def xml
            if (metasets) {
                List<String> metasetNames = metasets.split(",")
                log.debug("metasetnames: ${metasetNames}")
                xml = osd.getMetadata(metasetNames)
            }
            else {
                xml = osd.getMetadata()
            }
            render(contentType: 'application/xml', text: xml)
        }
        catch (Exception e) {
            renderExceptionXml("Failed to fetch OSD ${id} metadata", e)
        }
    }

    /**
     * The getObjectsById command retrieves one or more objects by the given id.
     * <p/>
     * <h2>Needed permissions</h2>
     * BROWSE_OBJECT
     *
     * @param ids xml document containing a list of object ids accessible via XPath //ids/id,
     *        for example: <pre>{@code <ids><id>2170</id><id>22182</id></ids}</pre>
     * @return XML-Response:
     *         List of XML serialized objects.
     */
    def getObjectsById(String ids, Boolean include_summary) {
        try {
            if (!ids) {
                throw new CinnamonException('error.invalid.params')
            }
            Document response = DocumentHelper.createDocument()
            Element root = response.addElement("objects");

            org.dom4j.Node rootParamNode = ParamParser.parseXml(ids, "error.param.ids.xml")
            List<org.dom4j.Node> idNodes = rootParamNode.selectNodes("id");
            Validator validator = new Validator(userService.user)
            Permission browsePermission = Permission.findByName(PermissionName.BROWSE_OBJECT);
            for (org.dom4j.Node n : idNodes) {
                try {
                    Long id = Long.parseLong(n.getText())
                    ObjectSystemData osd = ObjectSystemData.get(id);
                    validator.checkBrowsePermission(osd, browsePermission);
                    def xml = osd.toXML()
                    if(include_summary){
                        xml.add(ParamParser.parseXml(osd.summary, null))
                    }
                    root.add(xml.rootElement);
                } catch (Exception e) {
                    log.debug("failed to add OSD for '" + n.getText() + "': " + e.getMessage());
                }
            }
            render(contentType: 'application/xml', text: response.asXML())
        }
        catch (Exception e) {
            renderExceptionXml('Failed to do getObjectsById', e)
        }
    }
    /**
     * The getobjectswithmetadata command retrieves some or all objects in the folder with the given id
     * and returns their metadata and system metadata.
     * The user needs both the browse object permission and the permission to read the metadata.
     * <br>
     * The optional versions parameter allows requesting all versions (all),
     * only the newest version in the trunk of the version tree (head) or the newest
     * version including branches (branch).
     * <h2>Needed permissions</h2>
     *         <ul>
     *         <li>READ_OBJECT_CUSTOM_METADATA</li>
     *         <li>BROWSE_OBJECT</li>
     *         </ul>
     * @param parentid parent folder id
     * @param versions optional version parameter: all,branch,head  (default=head)
     * @return XML-Response:
     *         List of object data as XML document.
     */
    def fetchObjectsWithCustomMetadata(Long parentid, Boolean include_summary) {
        try {
            def folder = folderService.fetchFolder(parentid)
            if (!folder) {
                throw new RuntimeException('error.folder.not.found')
            }
            def user = userService.user
            List<ObjectSystemData> results = folderService.getObjects(user, folder, repositoryName, params.versions)
            Validator val = new Validator(user);
            results = val.filterUnbrowsableObjects(results);
            Document doc = osdService.generateQueryObjectResultDocument(results, true, include_summary);
            addLinksToObjectQuery(params.parentid, doc, val, true, include_summary)
            render(contentType: 'application/xml', text: doc.asXML())
        }
        catch (Exception e) {
            log.debug("failed to fetch objects: ", e)
            renderExceptionXml(e.message)
        }
    }

    /**
     * The getsysmeta command fetches one of the system attributes of an object specified
     * by the "parameter" value. The following parameters can be retrieved:*
     *   <h2>Needed permissions</h2>
     *            READ_OBJECT_SYS_META oder BROWSE_FOLDER     
     * @param id the object id
     * @param parameter one of the following strings:
     *   <ul>
     *      <li>preid </li>
     *      <li>locked </li>
     *      <li>owner </li>
     *      <li>contentsize </li>
     *      <li>cntformat (id of the Format)</li>
     *      <li>procstate </li>
     *      <li>creator </li>
     *      <li>created </li>
     *      <li>language_id </li>
     *      <li>modifier </li>
     *      <li>modifier_id </li>
     *      <li>modified </li>
     *      <li>version </li>
     *      <li>rootid </li>
     *      <li>objtype (name of the object type)</li>
     *      <li>objtype_id </li>
     *      <li>acl_id</li>
     *      <li>summary</li>
     *           </ul>
     * @return XML-Response: <pre>{@code <sysMetaValue>$value</sysMetaValue>}</pre>
     *         If a null value is retrieved, an xml-error-doc is returned with the message:
     *         "error.result_value_is_null"
     */
    def getSysMeta(Long id, String parameter) {
        try {
            String value
            ObjectSystemData osd = ObjectSystemData.get(id)
            if (!osd) {
                throw new CinnamonException('error.object.not.found')
            }
            (new Validator(userService.user)).validateGetSysMeta(osd);

            switch (parameter) {
                case 'objtype': value = osd.type.name; break
                case 'objtype_id': value = osd.type.id; break;
                case 'owner_id': value = osd.owner.id; break;
                case 'modifier_id': value = osd.modifier.id; break;
                case 'creator_id': value = osd.creator.id; break;
                case 'owner': value = osd.owner.name; break;
                case 'modifier': value = osd.modifier.name; break;
                case 'creator': value = osd.creator.name; break;
                case 'parentid': value = osd.parent.id; break;
                case 'preid': value = osd.predecessor?.id; break;
                case 'name': value = osd.name; break;
                case 'appname': value = osd.appName; break;
                case 'contentsize': value = osd.contentSize; break;
                case 'cntformat': value = osd.format?.id; break;
                case 'procstate': value = osd.procstate; break;
                case 'locked': value = osd.locker?.id; break;
                case 'modified': value = ParamParser.dateToIsoString(osd.modified); break;
                case 'created': value = ParamParser.dateToIsoString(osd.created); break;
                case 'acl_id': value = osd.acl.id; break;
                case 'version': value = osd.cmnVersion; break;
                case 'rootid': value = osd.root?.id; break;
                case 'language_id': value = osd.language.id; break;
                case 'summary': value = osd.summary; break;
                default: throw new CinnamonException("Parameter '$parameter' can not be read on objects.")
            }

            if (value == null) {
                throw new CinnamonException("error.result_value_is_null")
            }
            render(contentType: 'application/xml') {
                sysMetaValue(value)
            }
        }
        catch (Exception e) {
            renderExceptionXml('Failed to getSysMeta($id,$parameter).', e)
        }
    }

    /**
     * Replace the content of an object in the repository.
     * If a file is specified, the format must also be specified. 
     * The value in the name column of the formats table must be used.
     * <br>
     * If no file parameter is specified, the content is removed.
     * The setcontent command can be used to add content later.
     * <h2>Needed permissions</h2>
     * WRITE_OBJECT_CONTENT
     *
     * @param file optional: uploaded file (MultipartFile object) if not set,
     *        the content of the given object is removed.
     * @param format the format's name (with or without 'format.'-prefix)
     * @param id id of the object which receives the content
     * @return XML-Response:
     * {@code
     *         <success>success.set.content</success>
     *}
     *         if successful, xml-error-doc if unsuccessful.
     * @throws IOException if file upload fails.
     */
    def saveContentXml(String format, Long id) {
        try {
            def user = userService.user
            def osd = fetchAndFilterOsd(id, [PermissionName.WRITE_OBJECT_CONTENT])

            if (params.containsKey('file')) {
                Format myFormat = Format.find("from Format f where name=:name or name=:nameWithPrefix",
                        [name: format, nameWithPrefix: "format.$format"]
                )
                if (!myFormat) {
                    throw new CinnamonException('error.format.not.found')
                }
                String contentPath = osd.contentPath
                osdService.saveFileUpload(request, osd, user, myFormat.id, repositoryName)
                if (contentPath) {
                    ContentStore.deleteFileInRepository(contentPath, repositoryName)
                }
            }
            else {
                log.debug("User wants to remove content of $id.")
                osd.deleteContent(repositoryName)
            }

            render(contentType: 'application/xml') {
                success('success.set.content')
            }
        }
        catch (Exception e) {
            renderExceptionXml('Failed to set content', e)
        }


    }

    /**
     * The version command creates an object in the repository with the given name,
     * and links it with the preid object as a new version of the latter. The name,
     * metadata and parentid parameters are optional.
     * If they are unspecified, they are copied from preid. It is possible, but unusual
     * to have different object versions in different folders and with different names.
     * If a file is specified, the format must also be specified.
     * The value in the name column of the formats table must be used.
     * The id of the newly created object is returned.
     * <br>
     * If no file parameter is specified, an object without content is created.
     * The setcontent command can be used to add content later.
     * <br>
     * The metadata must be specified in one row.
     *
     * @param preid predecessor id
     * @param metadata xml metadata (optional)
     * @param name =object name (optional)
     * @param file =uploaded file
     * @param format =content format as formats.name value (optional - must be set if file is also set)
     * @parentid = p a r e n t folder id (optional)
     * @return XML-Response:
     * {@code
     *         <objectId>$id</objectId>
     *}
     *         Id of new version
     */
    def newVersionXml(Long preid, Long parentid, String format, String metadata, String name) {
        try {
            def user = userService.user
            ObjectSystemData pre = fetchAndFilterOsd(preid, [PermissionName.VERSION_OBJECT])
            ObjectSystemData osd = new ObjectSystemData(pre, user);
            osd.root = pre.root
            if (name) {
                osd.name = name
            }
            if (parentid) {
                osd.parent = Folder.get(parentid)
            }
            new Validator(user).validateCreate(osd.parent)
            osd.predecessor = pre
            osd.cmnVersion = osd.createNewVersionLabel()             
            osd.fixLatestHeadAndBranch([])
            Format myFormat = Format.findByName(format)
            osd.locker = null
            osd.save(flush: true)            
            if (params.containsKey('file')) {
                osdService.saveFileUpload(request, osd, user, myFormat.id, repositoryName, false)
            }
            if (metadata) {
                osd.storeMetadata(metadata)
            }
            log.debug("new osd: ${osd.toXML().asXML()}")
            log.debug("version of new osd: ${osd.cmnVersion}")
            
            render(contentType: 'application/xml') {
                objectId(osd.id.toString())
            }
        }
        catch (RuntimeException e) {
            log.debug("Failed to version object:", e)
            renderException(e.message)
        }
    }

    /**
     * The saveMeta command sets the metadata to the specified value.
     * If no metadata parameter is specified, the metadata is set to {@code <meta />}.
     * <h2>Needed permissions</h2>
     * WRITE_OBJECT_CUSTOM_METADATA
     *
     * @param id the OSD id
     * @param metadata the metadata to be set
     * @param write_policy optional write policy for metasets. Allowed values are WRITE IGNORE BRANCH,
     *      default is 'branch'
     *
     * @return {@code
     *         <cinnamon>
     *         <success>success.set.metadata</success>
     *         </cinnamon>
     *}
     *         if successful, xml-error-doc if unsuccessful.
     *         The response document may include additional elements as children of the root element
     *         (for example, {@code <warnings />}
     */
    def saveMetadataXml(Long id, String metadata, String write_policy) {
        try {
            ObjectSystemData osd = ObjectSystemData.get(id)
            def user = userService.user
            (new Validator(user)).validateSetMeta(osd)
            metadata = metadata == null ? "<meta />" : metadata.trim();
            WritePolicy policy = WritePolicy.valueOf(write_policy ?: 'BRANCH')
            osd.storeMetadata(metadata, policy);
            osd.updateAccess(user);
            render(contentType: 'application/xml') {
                cinnamon{
                    success('success.set.metadata')
                }
            }
        }
        catch (Exception e) {
            renderExceptionXml('Failed to set metadata', e)
        }
    }

    /**
     * The updateSysMeta command sets one of the system attributes of an object or folder
     * to the specified value. If an id parameter is specified, the metadata is applied to the object
     * with the specified id. If a folderid parameter is specified, the metadata is applied to the folder
     * with the specified id. Either an id or a folderid must be specified,
     * but not both or none. Folders do not have all the metadata of objects.
     * The following parameters can be set:
     * <ul>
     * <li>     parentid (= id of folder in which the object or folder resides)</li>
     * <li>     name</li>
     * <li>     owner  (=id of the owner)</li>
     * <li>     procstate </li>
     * <li>     acl_id (= id of an ACL)</li>
     * <li>     objtype  (currently, this parameter is the _name_ of an objtype, NOT an id!)</li>
     * <li>     language_id (= id of a language)</li>
     * </ul>
     * On OSDs, you can additionally set:
     * <ul>
     *     <li>appname= appname field (mostly for application type associated with this object)</li>
     * </ul>
     * <h2>Needed permissions</h2>
     * <ul>
     * <li>LOCK und (WRITE_OBJECT_SYS_METADATA oder EDIT_FOLDER)</li>
     * <li>for aclId: SET_ACL</li>
     * <li>for parent_id: MOVE</li>
     * <ul>
     *
     * @param id the object's id
     * @param parameter the name of the parameter to be set
     * @param value the value to assign to the given parameter
     * @return XML-Response:
     * {@code
     *         <success>success.set.sys_meta</success>
     *}
     */
    // TODO: owner should be an id-param, not the login name of a user
    def updateSysMetaXml(Long id, String parameter, String value) {
        try {
            if (value == null) {
                value = "";
            }
            def user = userService.user
            Validator validator = new Validator(user);
            ObjectSystemData osd = ObjectSystemData.get(id)
            if (!osd) {
                throw new CinnamonException('error.object.not.found')
            }

            switch (parameter) {
                case 'objtype':
                    validator.validateSetSysMeta(osd);
                    ObjectType type = ObjectType.findByName(value)
                    if (!type) {
                        throw new CinnamonException("error.param.objtype");
                    }
                    osd.type = type
                    break
                case 'parentid':
                    Folder folder = Folder.get(value)
                    if (!folder) {
                        throw new CinnamonException("error.parent_folder.not_found")
                    }
                    validator.validateMoveObject(osd, folder)
                    osd.parent = folder
                    break
                case 'owner':
                    def owner = UserAccount.get(value)
                    if (!owner) {
                        throw new CinnamonException("error.user.not_found");
                    }
                    osd.owner = owner
                    break
                case 'language_id': validator.validateSetSysMeta(osd)
                    def lang = Language.get(value)
                    if (!lang) {
                        throw new CinnamonException("error.object.not.found")
                    }
                    osd.language = lang
                    break
                case 'name': validator.validateSetSysMeta(osd);
                    osd.name = value
                    break
                case 'appname': validator.validateSetSysMeta(osd)
                    osd.appName = value
                    break
                case 'procstate': validator.validateSetSysMeta(osd);
                    osd.procstate = value
                    break
                case 'acl_id':validator.validateSetObjectAcl(osd)
                    Acl acl = Acl.get(value)
                    if (! acl) {
                        throw new CinnamonException("error.acl.not_found");
                    }
                    osd.acl = acl
                    break
                default: throw new CinnamonException("Parameter " + parameter + " is invalid on objects.")
            }
            osd.updateAccess(user)
            render(contentType: 'application/xml'){
                success('success.set.sys_meta')
            }
        }
        catch (Exception e) {
            renderExceptionXml('Failed to updateSysMeta', e)
        }
    }

    /**
     * Retrieves the content with the given id. The user may then edit the file.
     * <h2>Needed permissions</h2>
     * READ_OBJECT_CONTENT
     *
     * @param id Id of the requested object
     * @param resultfile the attachment filename 
     * @return the raw content of the object as byte stream (MIME-type: binary/octet-stream)
     */
    def getContentXml(Long id, String resultfile) {
        try {
            ObjectSystemData osd = fetchAndFilterOsd(id?.toString() ?: params.osd)
            def filename = infoService.config.data_root + File.separator + repositoryName +
                    File.separator + osd.contentPath
            log.debug("getContent called for #${osd.id} @ $filename")
            File data = new File(filename)
            if (!data.exists()) {
                log.debug("could not find: $filename")
                throw new RuntimeException('error.file.not.found')
            }

            if (osd.contentSize == null || osd.contentSize == 0L) {
                throw new RuntimeException('error.content.not.found')
            }
            def attachmentName = resultfile ?: "${osd.name.encodeAsURL()}${osd.determineExtension()}"
            render file:data, contentType: osd.format.contenttype, filename: attachmentName
            
        }
        catch (Exception e) {
            LocalRepository.cleanUp()
            renderExceptionXml(e.message)
        }
    }
}

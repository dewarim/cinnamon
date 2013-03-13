package cinnamon

import cinnamon.references.Link
import cinnamon.references.LinkType
import cinnamon.utils.ParamParser
import org.dom4j.DocumentHelper
import org.dom4j.Element
import org.springframework.web.multipart.MultipartFile
import grails.plugins.springsecurity.Secured
import cinnamon.global.Conf

import cinnamon.global.PermissionName
import cinnamon.relation.Relation
import cinnamon.global.ConfThreadLocal
import cinnamon.i18n.Language
import cinnamon.exceptions.CinnamonException
import org.dom4j.Document
import cinnamon.global.Constants

/**
 *
 */
@Secured(["isAuthenticated()"])
class OsdController extends BaseController {

    def metasetService
    def imageService

    def editMetadata() {
        try {
            ObjectSystemData osd = fetchAndFilterOsd(params.osd)
            return render(template: mapTemplate('/osd/editMetadata'), model: [osd: osd])
        }
        catch (Exception e) {
            renderException(e)
        }
    }

    def saveMetadata() {
        try {
            ObjectSystemData osd = fetchAndFilterOsd(params.osd)
            if (!osd.metadata.equals(params.metadata)) {
                osd.metadata = params.metadata
                luceneService.updateIndex(osd, session.repositoryName)
            }
            return render(template: mapTemplate('/osd/objectDetails'), model: [osd: osd, permissions: loadUserPermissions(osd.acl)])
        }
        catch (Exception e) {
            renderException(e)
        }
    }

    def unlockOsd() {
        try {
            ObjectSystemData osd = fetchAndFilterOsd(params.osd)
            UserAccount user = userService.user
            osd.locker = null
            luceneService.updateIndex(osd, session.repositoryName)
            render(template: mapTemplate('/folder/lockStatus'), model: [user: user, osd: osd,
                    superuserStatus: userService.isSuperuser(user)])
        }
        catch (Exception e) {
            renderException(e)
        }
    }

    def lockOsd() {
        try {
            ObjectSystemData osd = fetchAndFilterOsd(params.osd)
            UserAccount user = userService.user
            osdService.acquireLock(osd, user)
            luceneService.updateIndex(osd, session.repositoryName)
            render(template: mapTemplate('/folder/lockStatus'), model: [user: user, osd: osd,
                    superuserStatus: userService.isSuperuser(user)])

        }
        catch (Exception e) {
            renderException(e)
        }
    }

    def editContent() {

    }

    def listRelations() {
        try {
            ObjectSystemData osd = fetchAndFilterOsd(params.osd)
            def leftRelations = Relation.findAllByLeftOSD(osd)
            def rightRelations = Relation.findAllByRightOSD(osd)

            return render(template: mapTemplate('/osd/listRelations'),
                    model: [leftRelations: leftRelations, rightRelations: rightRelations,
                            osd: osd
                    ]
            )
        }
        catch (Exception e) {
            renderException(e)
        }

    }

    def fetchObjectDetails() {
        try {
            ObjectSystemData osd = fetchAndFilterOsd(params.osd)
            def hasRelations = Relation.find("from Relation as r where r.rightOSD=:o1 or r.leftOSD=:o2",
                    [o1: osd, o2: osd]) ? true : false
            def permissions = loadUserPermissions(osd.acl)
            def user = userService.user
            return render(template: mapTemplate("/osd/objectDetails"),
                    model: [osd: osd, permissions: permissions,
                            superuserStatus: userService.isSuperuser(user),
                            hasRelations: hasRelations])
        }
        catch (Exception e) {
            log.debug("failed to fetchObjectDetails: ", e)
            renderException(e)
        }
    }

    def renderPreview() {
        try {
            ObjectSystemData osd = fetchAndFilterOsd(params.osd)
            def osdContent = osd.getContent(session.repositoryName)
            return render(template: mapTemplate('/osd/objectPreview'),
                    model: [osd: osd, ctype: osd.format?.contenttype, osdContent: osdContent])
        }
        catch (Exception e) {
            renderException(e)
        }
    }

    def renderMetadata() {
        try {
            ObjectSystemData osd = fetchAndFilterOsd(params.osd)
            return render(template: mapTemplate('/osd/renderMetadata'), model: [osd: osd])
        }
        catch (Exception e) {
            renderException(e)
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

            Conf conf = ConfThreadLocal.getConf()
            def filename = conf.getDataRoot() + File.separator + repositoryName +
                    File.separator + osd.contentPath
            log.debug("getContent called for #${osd.id} @ $filename")
            File data = new File(filename)
            if (!data.exists()) {
                log.debug("could not find: $filename")
                throw new RuntimeException('error.file.not.found')
            }

            if (osd.contentSize == null || osd.contentSize == 0) {
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
            renderException(e)
        }
    }

    def editAcl() {
        try {
            def osd = fetchAndFilterOsd(params.osd)
            render(template: mapTemplate('/osd/editAcl'), model: [osd: osd])
        }
        catch (Exception e) {
            renderException(e)
        }
    }

    def editLanguage() {
        try {
            def osd = fetchAndFilterOsd(params.osd)
            render(template: mapTemplate('/osd/editLanguage'), model: [osd: osd])
        }
        catch (Exception e) {
            renderException(e)
        }
    }

    def editOwner() {
        try {
            def osd = fetchAndFilterOsd(params.osd)
            render(template: mapTemplate('/osd/editOwner'), model: [osd: osd])
        }
        catch (Exception e) {
            renderException(e)
        }
    }

    def editType() {
        try {
            def osd = fetchAndFilterOsd(params.osd)
            render(template: mapTemplate('/osd/editType'), model: [osd: osd])
        }
        catch (Exception e) {
            renderException(e)
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
                luceneService.updateIndex(osd, session.repositoryName)
                fetchObjectDetails()
            }
            else {
                render(status: 401, text: message(code: 'error.illegal.parameter', args: [params.fieldName?.encodeAsHTML()]))
            }
        } catch (Exception e) {
            renderException(e)
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
            return redirect(controller: 'folder', action: 'index', params: [folder: params.folder, osd: params.osd])
        }
        catch (RuntimeException e) {
            log.debug("Failed to set content on object ${osd?.id}: ", e)
            flash.message = message(code: e.getMessage())
            if (!osd) {
                return redirect(controller: 'folder', action: 'index')
            }

            return redirect(controller: 'osd', action: 'setContent', params: [folder: params.folder, osd: params.osd])
        }
    }

    def newVersion() {
        def repositoryName = session.repositoryName
        try {
            def user = userService.user
            ObjectSystemData pre = fetchAndFilterOsd(params.osd, [PermissionName.VERSION_OBJECT])
            ObjectSystemData osd = new ObjectSystemData(pre, user);
            osd.root = pre.root
            osd.predecessor = pre
            osd.cmnVersion = osd.createNewVersionLabel()
            osd.fixLatestHeadAndBranch([])
            osd.save()
            log.debug("version of new osd: ${osd.cmnVersion}")
            luceneService.addToIndex(osd, repositoryName)
            def osdList = folderService.getObjects(user, osd.parent, repositoryName, params.versions)
            def folderContentTemplate = folderService.fetchFolderTemplate(osd.parent.type.config)
            return render(template: folderContentTemplate, model: [folder: osd.parent,
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
            return renderException(e)
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
            if (!targetFolder) {
                throw new RuntimeException('error.folder.not.found')
            }
            if (!folderService.checkPermissions(targetFolder, user, [PermissionName.CREATE_OBJECT])) {
                throw new RuntimeException('error.access.denied')
            }

            log.debug("*** start iterate")
            def idList = params.list("osd")
            def folderList = params.list("folder")
            if (idList.isEmpty() && folderList.isEmpty()) {
                log.debug("id list is empty - redirect")
                // nothing to do
                defaultRedirect([folder: selectedFolder])
            }
            def repository = session.repositoryName
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

    def fetchObjects() {
        try {
            def folder = folderService.fetchFolder(params.parentid)
            if (!folder) {
                throw new RuntimeException('error.folder.not.found')
            }
            def user = userService.user
            List<ObjectSystemData> results = folderService.getObjects(user, folder, session.repositoryName, params.versions)
            Validator val = new Validator(user);
            results = val.filterUnbrowsableObjects(results);
            Document doc = osdService.generateQueryObjectResultDocument(results);
            addLinksToObjectQuery(params.parentid, doc, val, false)

            log.debug("objects for folder ${folder.id} / ${folder.name}:\n ${doc.asXML()}")
            return render(contentType: 'application/xml', text: doc.asXML())
        }
        catch (Exception e) {
            log.debug("failed to fetch objects: ", e)
            renderExceptionXml(e)
        }
    }

    protected void addLinksToObjectQuery(String parentId, Document doc, Validator val, Boolean withMetadata) {
        Folder parent = Folder.get(parentId);
        Element root = doc.getRootElement();
        Collection<Link> links = linkService.findLinksIn(parent, LinkType.OBJECT);
        log.debug("Found " + links.size() + " links.");
        for (Link link : links) {
            try {
                val.validatePermission(link.acl, PermissionName.BROWSE_OBJECT);
                val.validatePermission(link.osd.acl, PermissionName.BROWSE_OBJECT);
                if (withMetadata){
                    val.validatePermission(link.osd.acl, PermissionName.READ_OBJECT_CUSTOM_METADATA)
                }
            } catch (Exception e) {
                log.debug("", e);
                continue;
            }
            Element osdNode = link.getOsd().toXmlElement(root);
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
     * @param cmd HTTP request parameter map
     *            <ul>
     *            <li>command=create</li>
     *            <li>[preid]= optional predecessor id - basically this may be used to create another (empty) version of an object.</li>
     *            <li>name = name of this object</li>
     *            <li>[appname] = internally used by desktop client to determine which DTDs etc are needed for this object</li>
     *            <li>metadata = XML string of metadata with required root element {@code <meta>}</li>
     *            <li>[objtype_id OR objtype]= Id or Name of object type. If no object type is specified, use Constants.OBJTYPE_DEFAULT.</li>
     *            <li>parentid = id of parent folder</li>
     *            <li>[format OR format_id] = Id or name of format (optional)</li>
     *            <li>[acl_id] = optional Id of ACL - if not specified, will use ACL of parent folder</li>
     *            <li>[language_id]=optional id of language, will use default language "und" for undetermined if not specified.</li>
     *            </ul>
     * @return a Response which contains:
     *         <pre>
     * {@code <objectId>$id_of_new_object</objectId>}
     *                         </pre>
     */
    def createOsd() {
        try {
            def user = userService.user
            ObjectSystemData osd = new ObjectSystemData(params, user, false)
            (new Validator(user)).validateCreate(osd.parent)
            log.debug("osd created: " + osd)

            if (params.containsKey("file")) {
                osdService.saveFileUpload(request, osd, user, osd.format?.id, repositoryName)
                // new TikaParser().parse(osd, repository.getName());
            }

            osd.save(flush: true)
            metasetService.initializeMetasets(osd, (String) params.metasets)
            luceneService.addToIndex(osd, repositoryName)

            render(contentType: 'application/xml') {
                objectId(osd.id.toString())
            }
        }
        catch (Exception e) {
            log.debug("failed to create OSD", e)
            renderExceptionXml(e)
        }
    }

    /**
     * The getobject command retrieves an object by the given id.
     * <h2>Needed permissions</h2>
     * BROWSE_OBJECT
     *
     * @param cmd HTTP request parameter map:
     *            <ul>
     *            <li>command=getobject</li>
     *            <li>id=object id</li>
     *            </ul>
     * @return XML-Response:
     *         XML serialized object or xml-error-doc
     */
    def getObject(Long id) {
        try {
            Document doc = DocumentHelper.createDocument()
            Element root = doc.addElement("objects");
            ObjectSystemData osd = ObjectSystemData.get(id);
            if (osd == null) {
                throw new CinnamonException("error.object.not.found");
            }
            else {
                (new Validator(userService.user)).checkBrowsePermission(osd);
                root.add(osd.toXML().getRootElement());
            }
            render(contentType: 'application/xml', text: doc.asXML())
        }
        catch (Exception e) {
            log.debug("failed to fetch OSD", e)
            renderExceptionXml(e)
        }
    }

    //--------------------------- Cinnamon XML API methods -----------------------

    /**
     * The lock command places a lock for the session owner on an object.
     * <h2>Needed permissions</h2>
     * LOCK
     *
     * @param cmd HTTP request parameter map:
     *            <ul>
     *            <li>command=lock</li>
     *            <li>id = object id</li>
     *            </ul>
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
            luceneService.updateIndex(osd, repositoryName)
            log.debug("lock - done.");
            render(contentType: 'application/xml') {
                success('success.object.lock')
            }
        }
        catch (Exception e) {
            log.debug("Failed to lock #$id.", e)
            renderExceptionXml(e)
        }
    }

    /**
     * The unlock command removes a lock from an object.
     *
     * @param cmd HTTP request parameter map
     *            <ul>
     *            <li>command=unlock</li>
     *            <li>id=object id</li>
     *            </ul>
     * @return XML-Response
     * {@code
     *         <success>success.object.lock</success>
     *}
     */
    def unlockXml(Long id) {
        try {
            def user = userService.user
            ObjectSystemData osd = ObjectSystemData.get(id)
            (new Validator(user)).validateUnlock(osd)
            osd.locker = null
            luceneService.updateIndex(osd, repositoryName)
            log.debug("unlock - done")
            render(contentType: 'application/xml') {
                success('success.object.unlock')
            }
        }
        catch (Exception e) {
            log.debug("Failed to unlock #$id.", e)
            renderExceptionXml(e)

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
     * @param cmd parameter map from HTTP request containing:
     *            <ul>
     *            <li>command = copy</li>
     *            <li>sourceid	= source object id</li>
     *            <li>targetfolderid	= target folder id</li>
     *            <li>[metasets]=optional, comma-separated list of metasetType-names which will be copied.</li>
     *            </ul>
     * @return xml response with id of newly created object (or standard xml error message in case of error):
     * <pre>
     *     {@code
     *     <objectId>12345</objectId>
     *     }
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
            osdService.copyContent(repositoryName, osd, copy)
            copy.save()
            copy.fixLatestHeadAndBranch([])
            osdService.copyRelations(osd, copy)
            // execute the new LifeCycleState if necessary.
            if (copy.getState() != null) {
                copy.getState().enterState(copy, copy.getState())
            }
            metasetService.copyMetasets(osd, copy, metasets)
            luceneService.addToIndex(copy, repositoryName)
            render(contentType: 'application/xml'){
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
     * @param id
     * @return XML-Response:
     *         <pre>
     *         {@code <success>success.delete.object</success> }
     *         </pre> if successful, an XML-error-node if unsuccessful.
     */
    def deleteXml(Long id) {
        def osd = ObjectSystemData.get(id)
        try {
            if (!osd) {
                throw new CinnamonException('error.object.not.found')
            }
            (new Validator(userService.user)).validateDelete(osd)
            luceneService.removeFromIndex(osd, repositoryName)
            ObjectSystemData preOsd = osd.getPredecessor();
            osdService.delete(osd, repositoryName)
            if (preOsd) {
                def predecessorChildren = ObjectSystemData.findAllByPredecessor(preOsd)
                preOsd.fixLatestHeadAndBranch(predecessorChildren)
                luceneService.updateIndex(preOsd, repositoryName)
            }
            render(contentType: 'application/xml') {
                success('success.delete.object')
            }
        }
        catch (Exception e) {
            if (osd) {
                luceneService.updateIndex(osd, repositoryName)
            }
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
     *             {@code <success>success.delete.all_versions</success>}
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
            osdService.delete(osd.root, true,true, repositoryName)
            render(contentType: 'application/xml') {
                success('success.delete.all_versions')
            }
        }
        catch (Exception e) {
            if (osd) {
                luceneService.updateIndex(osd, repositoryName)
                if(objectTree){
                    objectTree.remove(osd)
                    objectTree.each{item ->                        
                        luceneService.updateIndex(item, repositoryName)
                    }
                }
            }
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
        try{
            ObjectSystemData osd = ObjectSystemData.get(id)
            if(! osd){
                throw new CinnamonException('error.object.not.found')
            }
            (new Validator(userService.user)).validateGetMeta(osd)
            
            def xml
            if(metasets){
                List<String> metasetNames = metasets.split(",")
                log.debug("metasetnames: ${metasetNames}")
                xml = osd.getMetadata(metasetNames)
            }
            else{
                xml = osd.getMetadata()
            }
            render(contentType: 'application/xml', text:xml)
        }
        catch (Exception e){
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
    def getObjectsById(String ids) {
        try {
            if(! ids){
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
                    root.add(osd.toXML().getRootElement());
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
    def fetchObjectsWithCustomMetadata(Long parentid) {
        try {
            def folder = folderService.fetchFolder(parentid)
            if (!folder) {
                throw new RuntimeException('error.folder.not.found')
            }
            def user = userService.user
            List<ObjectSystemData> results = folderService.getObjects(user, folder, session.repositoryName, params.versions)
            Validator val = new Validator(user);
            results = val.filterUnbrowsableObjects(results);            
            Document doc = osdService.generateQueryObjectResultDocument(results, true);
            addLinksToObjectQuery(params.parentid, doc, val, true)            
            return render(contentType: 'application/xml', text: doc.asXML())
        }
        catch (Exception e) {
            log.debug("failed to fetch objects: ", e)
            renderExceptionXml(e)
        }
    }
  
    /**
     * The getsysmeta command fetches one of the system attributes of an object specified
     * by the "parameter" value. The following parameters can be retrieved:*
     *            <h2>Needed permissions</h2>
     *            READ_OBJECT_SYS_META oder BROWSE_FOLDER     
     * @param id the object id
     * @param parameter one of the following strings:
     *            <ul>
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
     * The setcontent command replaces the content of an object in the repository.
     * If a file is specified, the format must also be specified. The value in the name column
     * of the formats table must be used.
     * <br>
     * If no file parameter is specified, the content is removed.
     * The setcontent command can be used to add content later.
     * <h2>Needed permissions</h2>
     * WRITE_OBJECT_CONTENT
     *
     * HTTP request parameter map:
     *            <ul>
     *            <li>command=setcontent</li>
     *            <li>file=uploaded file</li>
     *            <li>format=name of content format</li>
     *            <li>id=object id</li>
     *            </ul>
     * @return XML-Response:
     *         {@code
     *         <success>success.set.content</success>
     *         }
     *         if successful, xml-error-doc if unsuccessful.
     * @throws IOException if file upload fails.
     */
    def saveContentXml(String format, Long id){
        try {
            def user = userService.user
            def osd = fetchAndFilterOsd(params.osd, [PermissionName.WRITE_OBJECT_CONTENT])
            Format myFormat = Format.findByName(format)
            String contentPath = osd.contentPath
            if (params.containsKey('file') ){
                osdService.saveFileUpload(request, osd, user, myFormat.id, repositoryName)
            }
            if (contentPath){
                ContentStore.deleteFileInRepository(contentPath, repositoryName)
            }
            render(contentType: 'application/xml'){
                success('success.set.content')
            }
        }
        catch (Exception e) {
            renderExceptionXml('Failed to set content', e)
        }


    }

}

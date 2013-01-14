package cinnamon

import cinnamon.references.Link
import cinnamon.references.LinkType
import cinnamon.utils.ParamParser
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

    def imageLoader() {
        try {
            ObjectSystemData osd = fetchAndFilterOsd(params.id)
            if (!osd.format?.contenttype?.startsWith('image/')) {
                return render(status: 503, text: message(code: 'error.wrong.format'))
            }
            response.setContentType(osd.format.contenttype)
            Conf conf = ConfThreadLocal.getConf()
            log.debug("repository: ${session.repositoryName}")
            def filename = conf.getDataRoot() + File.separator + session.repositoryName +
                    File.separator + osd.contentPath
            log.debug("filename:$filename")
            File image = new File(filename)
            if (!image.exists()) {
                log.debug("could not find: $filename")
                return render(status: 503, text: message(code: 'error.image.not.found'))
            }
            response.outputStream << image.readBytes()
            response.outputStream.close()
            return null
        }
        catch (Exception e) {
            log.debug("imageLoader fail:",e)
            renderException(e)
        }
    }

    def getContent() {
        Folder folder = null
        try {
            ObjectSystemData osd = fetchAndFilterOsd(params.osd)
            folder = fetchAndFilterFolder(params.folder)

            Conf conf = ConfThreadLocal.getConf()
            def filename = conf.getDataRoot() + File.separator + session.repositoryName +
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

            response.setHeader("Content-disposition", "attachment; filename=${osd.name.encodeAsURL()}.${osd.format.extension}");
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
            def osd = osdService.createOsd(request, params, session.repositoryName, null, user, folder)
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

    def saveContent() {
        ObjectSystemData osd = null
        try {
            UserAccount user = userService.user
            osd = fetchAndFilterOsd(params.osd, [PermissionName.WRITE_OBJECT_CONTENT])
            def folder = fetchAndFilterFolder(osd.parent.id)

            MultipartFile file = request.getFile('file')
            if (file.isEmpty()) {
                throw new RuntimeException('error.missing.content')
            }
            else {
                // remove any image thumbnails on the content     
                metasetService.unlinkMetaset(osd, osd.fetchMetaset(Constants.METASET_THUMBNAIL))
                
                osdService.acquireLock(osd, user)
                File tempFile = File.createTempFile('illicium_upload_', null)
                file.transferTo(tempFile)
                osdService.storeContent(osd, file.contentType, params.format, tempFile, session.repositoryName)
                osdService.unlock(osd, user)
                osd.save()
                luceneService.updateIndex(osd, session.repositoryName)
            }
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

    // unfinished?    
    def setContent() {
        try {
            ObjectSystemData osd = fetchAndFilterOsd(params.osd, [PermissionName.WRITE_OBJECT_CONTENT])            
            return [
                    osd: osd,
                    folder: osd.parent
            ]
        }
        catch (RuntimeException e) {
            flash.message = message(code: e.getMessage())
            defaultRedirect([folder: params.folder, osd: params.osd])
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
            if(!targetFolder){
                throw new RuntimeException('error.folder.not.found')
            }
            if(! folderService.checkPermissions(targetFolder, user, [PermissionName.CREATE_OBJECT])){
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
            def versionType = VersionType.values().find{it.name() == (params.versions ?: VersionType.ALL.name())}
        
            if (params.delete) {
                msgMap = osdService.deleteList(idList, repository, versionType)
                msgList.addAll(convertMsgMap(msgMap))
                msgMap = folderService.deleteList(folderList, repository, true)
                msgList.addAll(convertMsgMap(msgMap))
            }
            else if (params.move){
                log.debug("*** will move objects into folder: ${selectedFolder}")
                msgMap = osdService.moveToFolder(idList, selectedFolder, repository, versionType, user)
                msgList.addAll(convertMsgMap(msgMap))                
                msgMap = folderService.moveToFolder(folderList, selectedFolder, user)
                msgList.addAll(convertMsgMap(msgMap))
            }
            else if (params.copy){
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
        msgMap.each {String k, List v ->
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
            if (! folder){
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

    protected void addLinksToObjectQuery(String parentId, Document doc, Validator val, Boolean withMetadata){
        Folder parent = Folder.get(parentId);
        Element root = doc.getRootElement();
        Collection<Link> links = linkService.findLinksIn(parent, LinkType.OBJECT);
        log.debug("Found " + links.size() + " links.");
        for (Link link : links) {
            try {
                val.validatePermission(link.getAcl(), PermissionName.BROWSE_OBJECT);
                val.validatePermission(link.getOsd().getAcl(), PermissionName.BROWSE_OBJECT);
            } catch (Exception e) {
                log.debug("", e);
                continue;
            }
            Element osdNode = link.getOsd().toXmlElement(root);
            if(withMetadata){
                osdNode.add(ParamParser.parseXml(link.getOsd().getMetadata(), null));
            }
            linkService.addLinkToElement(link, osdNode);
        }
    }

}

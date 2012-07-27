package cinnamon

import humulus.EnvironmentHolder

import cinnamon.exceptions.IgnorableException
import grails.plugins.springsecurity.Secured
import cinnamon.global.PermissionName
import cinnamon.i18n.Language
import cinnamon.exceptions.CinnamonException
import cinnamon.global.Constants
import cinnamon.utils.ZippedFolder
import cinnamon.relation.RelationType
import org.dom4j.DocumentHelper
import org.dom4j.Element
import cinnamon.index.LuceneResult
import cinnamon.index.SearchableDomain

@Secured(["isAuthenticated()"])
class FolderController extends BaseController {

    def index() {
        try {
            Folder rootFolder = Folder.findRootFolder()
            if (!rootFolder) {
                def logoutMessage = message(code: "error.no.rootFolder")
                return redirect(controller: 'logout', action: 'info', params: [logoutMessage: logoutMessage])
            }
            Collection childFolders = fetchChildFolders(rootFolder)
            Map grandChildren = [:]
            Map folderConfigs = [:]
            Set<Folder> contentSet = new HashSet<Folder>()
            childFolders.each { child ->
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
                    envId: EnvironmentHolder.getEnvironment()?.get('id'),
                    msgList: flash.msgList,
                    folderConfigs: folderConfigs,
            ]

        }
        catch (Exception e) {
            log.debug("failed to show index:", e)
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

            return render(
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
            return render(status: 500, text: message(code: e.message))
        }
    }

    def fetchFolderContent() {
        def repositoryName = session.repositoryName
        def user = userService.user
        Folder folder
        try {
            folder = fetchAndFilterFolder(params.folder ?: params.id)

            log.debug("found folder. ${params.folder}: $folder")
            if (!params.versions?.trim()?.matches('^all|head|branch$')) {
                // log.debug("params.versions: ${params.versions}")
                params.versions = 'head'
            }
            log.debug("fetch OSDs")
            def osdList = folderService.getObjects(user, folder, repositoryName, params.versions)
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
            
            def folderConfig = new XmlSlurper().parseText(folder.type.config)
            def folderTemplate = '/folder/folderContent'
            if (folderConfig.template){
                folderTemplate = folderConfig.template.text()
            }
            
            return render(template: folderTemplate, model: [folder: folder,
                    osdList: osdList,
                    permissions: permissions,
                    folders: folderService.getFoldersInside(user, folder),
                    superuserStatus: userService.isSuperuser(user),
                    selectedVersion: params.versions,
                    versions: [all: 'folder.version.all', head: 'folder.version.head', branch: 'folder.version.branch']
            ])
        }
        catch (Exception e) {
            log.debug("fetchFolderContent failed", e)
            return render(status: 500, text: message(code: e.message))
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
                return render(status: 503, text: message(code: 'error.access.failed'))
            }

            return render(template: '/folder/folderMeta', model: [folder: folder, permissions: permissions])
        }
        catch (Exception e) {
            log.debug("renderMetadata failed", e)
            return render(status: 500, text: message(code: e.message))
        }
    }

    def renderMetadata() {
        try {
            Folder folder = fetchAndFilterFolder(params.folder)
            return render(template: 'renderMetadata', model: [folder: folder])
        }
        catch (Exception e) {
            log.debug("renderMetadata failed", e)
            return render(status: 500, text: message(code: e.message))
        }

    }

    def editMetadata() {
        try {
            Folder folder = fetchAndFilterFolder(params.folder)
            return render(template: '/folder/editMetadata', model: [folder: folder])
        }
        catch (Exception e) {
            log.debug("editMetadata failed", e)
            return render(status: 500, text: message(code: e.message))
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
                luceneService.updateIndex(folder, session.repositoryName)
            }
            else {
                log.debug("metadata is unchanged")
            }
            return render(template: 'renderMetadata',
                    model: [folder: folder, permissions: loadUserPermissions(folder.acl)])
        }
        catch (Exception e) {
            log.debug("failed to update folder metadata: ", e)
            if (folder) {
                return render(template: 'editMetadata', model: [folder: folder, saveMetaError: message(code: e.message),
                        metadata: params.metadata
                ])
            }
            else {
                return render(status: 500, message(code: e.message))
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
            renderException(e)
        }
    }

    def editAcl() {
        try {
            def folder = fetchAndFilterFolder(params.folder)
            render(template: 'editAcl', model: [folder: folder])
        }
        catch (Exception e) {
            log.debug("failed: editAcl", e)
            renderException(e)
        }
    }

    def editOwner() {
        try {
            def folder = fetchAndFilterFolder(params.folder)
            render(template: 'editOwner', model: [folder: folder])
        }
        catch (Exception e) {
            log.debug("failed: editOwner", e)
            renderException(e)
        }
    }

    def editType = {
        try {
            def folder = fetchAndFilterFolder(params.folder)
            render(template: 'editType', model: [folder: folder])
        }
        catch (Exception e) {
            log.debug("failed: editType", e)
            renderException(e)
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
                luceneService.updateIndex(folder, session.repositoryName)
                fetchFolderMeta()
            }
            else {
                render(status: 401, text: message(code: 'error.illegal.parameter', args: [params.fieldName?.encodeAsHTML()]))
            }
        } catch (Exception e) {
            log.debug("failed to save field: ", e)
            renderException(e)
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
            luceneService.addToIndex(folder, session.repositoryName)
            return redirect(controller: 'folder', action: 'index', params: [folder: folder.id])
        }
        catch (Exception e) {
            log.debug("save folder failed: ", e)
            flash.message = message(code: e.message)
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
            def repositoryName = session.repositoryName
            def user = userService.user
            def validator = new Validator(user)
            ZippedFolder zf = folder.createZippedFolder(latestHead, latestBranch, validator, repositoryName);
            File zipFile = zf.getZipFile();
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
                    osd.setMetadata(params.object_meta)
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
                luceneService.addToIndex(osd, repositoryName);
//                XmlResponse resp = new XmlResponse(res);
//                resp.addTextNode("objectId", String.valueOf(osd.getId()));
//                return resp;
            }
            else {
//                return new FileResponse(res, zipFile.getAbsolutePath(), zipFile.length(), zipFile.getName());


                response.setHeader("Content-disposition", "attachment; filename=${zipFile.getName().encodeAsURL()}.zip");
                response.setContentType('application/zip')
                response.outputStream << zipFile.newInputStream()
                response.outputStream.flush()
                return null
            }

        }
        catch (Exception e) {
            log.debug("zip folder failed: ", e)
            flash.message = message(code: 'zip.folder.fail', args: [e.message])
            return redirect(controller: 'folder', action: 'index', params: [folder: folder?.id])
        }
    }

    // AJAX
    def fetchRelationTypeDialog() {
        RelationType rt = RelationType.get(params.relationType)
        return render(template: 'fetchRelationTypeDialog', model: [relationType: rt])
    }

    //-----------------------------------------------------------------
    // Methods used by the desktop client (expecting XML responses)
    def fetchSubFolders() {
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
            folders.each {f ->
                f.toXmlElement(root)
            }
            return render(contentType: 'application/xml', text: doc.asXML())
        }
        catch (Exception e) {
            log.debug("fetchFolderContent failed for ${params}", e)
            return render(status: 500, text: message(code: e.message))
        }
    }

    def fetchFolderByPath() {
        try {
            String path = params.path;
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
                folder.toXmlElement(root);
            }
            return render(contentType: 'application/xml', text: doc.asXML())
        }
        catch (Exception e) {
            log.debug("fetchFolderByPath: ",e)
            renderExceptionXml(e)
        }
    }

    def fetchFolderXml() {
        log.debug("Getfolderbyid: " + params.id);
        try {
            Folder folder = Folder.get(params.id);
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
                it.toXmlElement(root)
            }      
            log.debug"fetchFolderXml output:\n${doc.asXML()}"
            render(contentType: 'application/xml', text: doc.asXML())
        }
        catch (Exception e) {
            log.debug("fetchFolderXml failed: ", e)
            renderExceptionXml(e)
        }
    }

    def searchSimple () {
        def query = params.query
        try {
            LuceneResult result = luceneService.search(query, session.repositoryName, null)
            def itemMap = result.filterResultToMap(null, itemService)
            log.debug("itemMap: $itemMap")
            def folders = itemMap.get(SearchableDomain.FOLDER.name)
            def objects = itemMap.get(SearchableDomain.OSD.name)
            
            render(template: 'searchResult', model: [searchResult: result, folders: folders, objects: objects])
        }
        catch (Exception e) {
            render(status: 503, text: message(code: e.message))
        }
    }
    
}

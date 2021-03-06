package cinnamon

import cinnamon.exceptions.CinnamonConfigurationException
import cinnamon.global.Constants
import cinnamon.references.Link
import org.dom4j.Document
import org.dom4j.DocumentHelper
import org.dom4j.Element
import cinnamon.exceptions.CinnamonException
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream
import org.apache.commons.compress.archivers.ArchiveStreamFactory
import cinnamon.global.ConfThreadLocal
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.utils.IOUtils
import cinnamon.global.PermissionName
import org.codehaus.groovy.grails.web.pages.discovery.GrailsConventionGroovyPageLocator
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class FolderService {

    def osdService
    def luceneService
    GrailsConventionGroovyPageLocator groovyPageLocator
    static final def folderConfigProperties = ['controller', 'action', 'template']
    def grailsApplication
    def userService

    /**
     * Check if a folder has content objects (meaning OSD, not sub-folders)
     * @param folder the folder to check
     * @return true if there is at least one OSD which has this folder as parent, false otherwise.
     */

    Boolean hasContent(Folder folder) {
        return ObjectSystemData.countByParent(folder) > 0
    }

    public List<Folder> getSubfolders(Folder parent) {
        if (parent == null) {
            return [findRootFolder()];
        }
        else {
            return Folder.findAll("from Folder f where f.parent=:parent and f.parent != f order by f.name", [parent: parent])
        }
    }

    /**
     * Returns the subfolders of the folder with the given id.
     * @param parentFolder - the folder whose sub-folders will be returned.
     * @return List of folders or an empty list.
     */
    public List<Folder> getSubfolders(Folder parentFolder, Boolean recursive) {
        List<Folder> folders = Folder.findAll("from Folder f where f.parent=:parent and f.parent != f order by f.name", [parent: parentFolder])
        List<Folder> newFolders = new ArrayList<Folder>();
        if (recursive) {
            for (Folder folder : folders) {
                newFolders.addAll(getSubfolders(folder, true));
            }
        }
        folders.addAll(newFolders);
        return folders;
    }

    /**
     * Turn a collection of folders into an XML document. Any exceptions encountered during
     * serialization are turned into error-Elements which contain the exception's message.
     * @param results
     * @return Document
     */
    // TODO: is this needed anywhere?
    Document generateQueryFolderResultDocument(Collection<Folder> results) {
        Document doc = DocumentHelper.createDocument();
        Element root = doc.addElement("folders");

        for (Folder folder : results) {
            Long id = folder.getId();
            log.debug("working on object: " + id);
            try {
                folder.toXmlElement(root);// FIXME: include_summary needed.
            }
            catch (CinnamonException ex) {
                log.error("Error serializing folder: " + id + " - " + ex.getMessage());
                Element error = DocumentHelper.createElement("error").addText(ex.getLocalizedMessage());
                error.addElement("id").addText(id.toString());
                root.add(error);
            }
        }
        return doc;
    }

    /**
     *
     * @param recursive set to true if subfolders must be included in the list of OSDs.
     * @return a List of OSDs in this folder
     */
    public List<ObjectSystemData> getOSDList(Folder folder, Boolean recursive) {
        return getFolderContent(folder, recursive);
    }

    public List<ObjectSystemData> getFolderContent(Folder folder, Boolean recursive) {
        return getFolderContent(folder, recursive, null, null);
    }

    public List<ObjectSystemData> getFolderContent(Folder folder, Boolean recursive, Boolean latestHead, Boolean latestBranch) {
        List<ObjectSystemData> osds
        if (latestHead != null && latestBranch != null) {
            osds = ObjectSystemData.findAll("from ObjectSystemData o where o.parent=:parent and (o.latestHead=:latestHead or o.latestBranch=:latestBranch)",
                    [parent: folder, latestHead: latestHead, latestBranch: latestBranch]
            );
        }
        else if (latestHead != null) {
            osds = ObjectSystemData.findAll("from ObjectSystemData o where o.parent=:parent and o.latestHead=:latestHead",
                    [parent: folder, latestHead: latestHead]);
        }
        else if (latestBranch != null) {
            osds = ObjectSystemData.findAll("from ObjectSystemData o where o.parent=:parent and o.latestBranch=:latestBranch",
                    [parent: folder, latestBranch: latestBranch]
            );
        }
        else {
            osds = ObjectSystemData.findAll("from ObjectSystemData o where o.parent=:parent", [parent: folder]);
        }
        if (recursive) {
            List<Folder> subFolders = getSubfolders(folder);
            for (Folder f : subFolders) {
                osds.addAll(getFolderContent(f, true, latestHead, latestBranch));
            }
        }
        return osds;
    }

    boolean folderExists(long id) {
        Folder f = Folder.get(id);
        return f != null;
    }

    // TODO: looks weird. Where is this used?
    Boolean folderExists(Folder folder) {
        if (folder == null) {
            throw new CinnamonException("error.folder.not.found");
        }
        return folderExists(folder.id)
    }

    public void deleteFolder(Long id, String repository, Boolean descend) {
        log.debug("before loading folder");
        Folder folder;
        if (id == 0L) {
            folder = findRootFolder();
        }
        else {
            folder = Folder.get(id);
        }

        if (folder == null) {
            throw new CinnamonException("error.folder.not.found");
        }
        if (!checkPermissions(folder, userService.user, [PermissionName.DELETE_FOLDER])) {
            throw new CinnamonException("error.delete.denied")
        }

        // check for subfolders:
        def subFolderCount = Folder.countByParent(folder);
        if (subFolderCount > 0) {
            if (descend) {
                folder.fetchSubfolders(false).each { subFolder ->
                    deleteFolder(subFolder.id, repository, descend)
                }
            }
            else {
                throw new CinnamonException("error.subfolders.exist");
            }
        }

        // check for objects inside folder
        def contents = ObjectSystemData.countByParent(folder);
        if (contents > 0) {
            if (descend) {
                // this will try to delete all versions from a folder, 
                // but will fail if there are descendants in other folders (which is intended)               
                ObjectSystemData.findAll("from ObjectSystemData o where parent=:folder order by id desc",
                        [folder: folder]).each { osd ->
                    osdService.delete(osd, false, true, repository)
                }
            }
            else {
                throw new CinnamonException("error.folder.has_content");
            }
        }
        // delete metasets
        folder.storeMetadata("<meta />")

        // delete links/references
        def links = Link.findAllByFolder(folder)
        links.each {
            it.delete()
        }

        folder.delete();
    }

    public List<Folder> findAllByPath(String path) {
        return findAllByPath(path, false, null);
    }

    public List<Folder> findAllByPath(String path, Boolean autoCreate, Validator validator) {
        def segs = path.split("/");

        Folder parent = findRootFolder();

        List<Folder> ret = new ArrayList<Folder>();
        ret.add(parent);
        for (String seg : segs) {
            if (seg.length() > 0) {
                List<Folder> results = Folder.findAllByParentAndName(parent, seg)
                if (results.isEmpty()) {
                    if (autoCreate) {
                        if (validator != null) {
                            validator.validateCreateFolder(parent);
                        }
                        Folder newFolder = new Folder(seg, parent.acl, parent, parent.owner, parent.type)
                        newFolder.save()
                        ret.add(newFolder);
                        parent = newFolder;
                    }
                    else {
                        throw new CinnamonException("error.path.invalid", path);
                    }
                }
                else {
                    Folder folder = results[0];
                    parent = folder;
                    ret.add(folder);
                }
            }
        }
        return ret;
    }

    public Folder findByPath(String path) {
        List<Folder> folders = findAllByPath(path);
        if (folders.isEmpty()) {
            return null;
        }
        else {
            return folders.get(folders.size() - 1);
        }
    }

    /**
     * Find a folder by its path and optionally create the path and the folder if necessary.
     * Note: for internal use; for example if you have to create a temporary system folder.
     * For user accessible folders, use findAllByPath with a Validator param.
     * @param path the path to create
     * @param autoCreate if true, create any folders missing on this path
     * @return the last folder from the path 
     * @throws CinnamonException if autoCreate is false and the folder was not found.
     */
    public Folder findByPath(String path, Boolean autoCreate) {
        return findAllByPath(path, true, null).last()
    }

    /**
     * Create a zipped folder containing those OSDs and subfolders (recursively) which the
     * validator allows. <br/>
     * Zip file encoding compatibility is difficult to achieve.<br/>
     * Using Cp437 as encoding will generate zip archives which can be unpacked with MS Windows XP
     * system utilities and also with the Linux unzip tool v6.0 (although the unzip tool will list them
     * as corrupted filenames with "?" in place for the special characters, it should unpack them
     * correctly). In tests, 7zip was unable to unpack those archives without messing up the filenames
     * - it requires UTF8 as encoding, as far as I can tell.<br/>
     * see: http://commons.apache.org/compress/zip.html#encoding<br/>
     * to manually test this, use: https://github.com/dewarim/GrailsBasedTesting
     * @param latestHead if set to true, only add objects with latestHead=true, if set to false include only
     *                   objects with latestHead=false, if set to null: include everything regardless of
     *                   latestHead status.
     * @param latestBranch if set to true, only add objects with latestBranch=true, if set to false include only
     *                   objects with latestBranch=false, if set to null: include everything regardless of
     *                     latestBranch status.
     * @param validator a Validator object which should be configured for the current user to check if access
     *                  to objects and folders inside the given folder is allowed. The content of this folder
     *                  will be filtered before it is added to the archive.
     * @return the zip archive of the given folder
     */
    public File createZippedFolder(Folder folder, Boolean latestHead, Boolean latestBranch,
                                   Validator validator) {

        final File sysTempDir = new File(System.getProperty("java.io.tmpdir"));
        File tempFolder = new File(sysTempDir, UUID.randomUUID().toString());
        if (!tempFolder.mkdirs()) {
            throw new CinnamonException("error.create.tempFolder.fail");
        }

        List<Folder> folders = new ArrayList<Folder>();
        folders.add(folder);
        folders.addAll(getSubfolders(folder, true));
        folders = validator.filterUnbrowsableFolders(folders);
        log.debug("# of folders found: " + folders.size());
        // create zip archive:
        File zipFile = null;
        try {
            zipFile = File.createTempFile("cinnamonArchive", "zip");
            final OutputStream out = new FileOutputStream(zipFile);
            ZipArchiveOutputStream zos = (ZipArchiveOutputStream) new ArchiveStreamFactory().createArchiveOutputStream(ArchiveStreamFactory.ZIP, out);
            String encoding = ConfThreadLocal.getConf().getField("zipFileEncoding", "Cp437");

            log.debug("current file.encoding: " + System.getProperty("file.encoding"));
            log.debug("current Encoding for ZipArchive: " + zos.getEncoding() + "; will now set: " + encoding);
            zos.setEncoding(encoding);
            zos.setFallbackToUTF8(true);
            zos.setCreateUnicodeExtraFields(ZipArchiveOutputStream.UnicodeExtraFieldPolicy.ALWAYS);

            for (Folder aFolder : folders) {
                String path = folder.fetchPath().replace(aFolder.fetchPath(), folder.name);
                // do not include the parent folders up to root.
                log.debug("zipFolderPath: " + path);
                File currentFolder = new File(tempFolder, path);
                if (!currentFolder.mkdirs()) {
                    // log.warn("failed  to create folder for: "+currentFolder.getAbsolutePath());
                }
                List<ObjectSystemData> osds = validator.filterUnbrowsableObjects(
                        getFolderContent(aFolder, false, latestHead, latestBranch));
                for (ObjectSystemData osd : osds) {
                    if (osd.contentSize == null) {
                        log.debug("osd ${osd.id} ${osd.name} is empty - skip.")
                        continue;
                    }
                    File outFile = osd.createFilenameFromName(currentFolder);
                    // the name in the archive should be the path without the temp folder part prepended.
                    String zipEntryPath = outFile.getAbsolutePath().replace(tempFolder.getAbsolutePath(), "");
                    if (zipEntryPath.startsWith(File.separator)) {
                        zipEntryPath = zipEntryPath.substring(1);
                    }
                    log.debug("zipEntryPath: " + zipEntryPath);

                    zipEntryPath = zipEntryPath.replaceAll("\\\\", "/");
                    zos.putArchiveEntry(new ZipArchiveEntry(zipEntryPath));
                    IOUtils.copy(new FileInputStream(osd.getFullContentPath()), zos);
                    zos.closeArchiveEntry();
                }
            }
            zos.close();
        } catch (Exception e) {
            log.debug("Failed to create zipFolder:", e);
            throw new CinnamonException("error.zipFolder.fail", e.getLocalizedMessage());
        }
        return zipFile;
    }


    Boolean mayBrowseFolder(Folder folder, user) {
        def validator = new Validator(user)
        try {
            log.debug("validate browse permission on: ${folder.name} (acl: ${folder.acl.name})")
            validator.validatePermissionByName(folder.acl, PermissionName.BROWSE_FOLDER, folder)
        }
        catch (Exception e) {
            log.debug("user does not have browse permission.", e)
            return false
        }
        return true
    }

    /**
     * Create a set of
     * @param folderId
     * @param osdId
     * @return
     */
    Set<String> createTriggerSet(folderId, osdId) {
        log.debug("createTriggerSet")
        def folderList = new ArrayList<Folder>()
        def triggerSet = new HashSet<String>()
        if (folderId) {
            Folder folder = Folder.get(folderId)
            if (folder) {
                folderList.addAll(folder.getParentFolders(folder)?.reverse())
                triggerSet.addAll(folderList.collect { "fetchLink_" + it.id })
                folderList.add(folder)
            }
        }
        log.debug("triggerSet: ${triggerSet}")
        return triggerSet
    }


    List<ObjectSystemData> getObjects(UserAccount user, Folder parent, String repositoryName, String versions) {
        if (!versions?.trim()?.matches('^all|head|branch$')) {
            log.debug("versions param does not match all|head|branch");
            versions = 'head'
        }
        String versionPred = ObjectSystemData.fetchVersionPredicate(versions);
        def osdList = ObjectSystemData.findAll("from ObjectSystemData as o where o.parent=:parent $versionPred order by id",
                [parent: parent])
        try {
            def validator = new Validator(user)
            return validator.filterUnbrowsableObjects(osdList)
        }
        catch (RuntimeException e) {
            log.debug("Failed to load objects", e)
            throw new RuntimeException(e)
        }
    }

    List getFoldersInside(user, folder) {
        def folders = Folder.findAllByParent(folder)
        def validator = new Validator(user)
        return validator.filterUnbrowsableFolders(folders)
    }

    Map<String, List> deleteList(idList, repository, descend) {
        def msgMap = [:]
        idList.each { id ->
            try {
                log.debug("delete folder: $id")
                deleteFolder(Long.parseLong(id), repository, descend);
                msgMap.put(id, ['folder.delete.ok'])
            }
            catch (Exception e) {
                log.debug("delete folder failed: ", e)
                msgMap.put(id, ['folder.delete.fail', e.message])
            }
        }
        return msgMap
    }

    Boolean checkPermissions(Folder folder, UserAccount user, List permissions) {
        Validator val = new Validator(user)
        try {
            val.validatePermissions(folder, permissions)
        }
        catch (Exception e) {
            log.debug("${user?.name} failed permission check:", e)
            return false
        }
        return true
    }

    Folder fetchFolder(id) {
        if (id == '0' || id == 0) {
            return findRootFolder()
        }
        return Folder.get(id)
    }

    /**
     * Add a folder type's config to the map folderConfig if necessary.
     * <em>This method changes the folderConfig map</em>.
     * If xml configuration field does not supply a valid "template" element,
     * the defaultController/defaultFolderContentAction/defaultTemplate values from the app config
     * are used (and if those are not found, 
     * the base default of "folder", "fetchFolderContent", "/folder/folderContent" is used.
     *
     * @param type the FolderType
     * @folderConfig the folderConfig map, is a [folderType:FolderConfig] map.
     * @return
     */
    Map addToFolderConfigs(Folder folder, Map folderConfigs) {
        FolderType type = folder.type
        if (!folderConfigs.containsKey(type)) {
            def xml = new XmlSlurper().parseText(type.config)
            FolderConfig fc = new FolderConfig()
            def template = xml.template.text()
            if (template && groovyPageLocator.findTemplate(template)) {
                folderConfigProperties.each { field ->
                    if (xml."$field".text()) {
                        fc."$field" = xml."$field".text()
                    }
                }
                log.debug("adding folderConfig. $fc")
                folderConfigs.put(type, fc)
            }
            else {
                fc.controller = grailsApplication.config.defaultController ?: 'folder'
                fc.action = grailsApplication.config.defaultFolderContentAction ?: 'fetchFolderContent'
                fc.template = grailsApplication.config.defaultTemplate ?: '/folder/folderContent'
                folderConfigs.put(type, fc)
            }
        }
        return folderConfigs
    }

    String fetchFolderTemplate(String config) {
        def folderConfig = new XmlSlurper().parseText(config)
        def folderTemplate = grailsApplication.config.defaultTemplate ?: '/folder/folderContent'
        if (folderConfig.template && groovyPageLocator.findTemplate(folderConfig.template.text())) {
            folderTemplate = folderConfig.template.text()
        }
        return folderTemplate
    }

    String fetchOsdListTemplate(String config) {
        def folderConfig = new XmlSlurper().parseText(config)
        def defaultTemplate = grailsApplication.config.templates.osd.osdList ?: '/osd/osdList'
        def template = folderConfig?.osdListTemplate?.text()
        if (template && groovyPageLocator.findTemplate(template)) {
            log.debug("found osdList-Template: ${template}")
            return template
        }
        log.debug("fetchOsdListTemplate: return defaultTemplate")
        return defaultTemplate
    }

    /**
     * Move a list of folders into another folder.
     * @param idList List of Strings with the folder-ids
     * @param folderId the id of the targetFolder
     * @user user the account of the user who tries to move the folders (for permission checking)
     * @return a map of folder.id.toString::list of message ids and message arguments, 
     *  for example: 1:[error.access.denied, $name]
     *
     */
    Map<String, List> moveToFolder(idList, folderId, repositoryName, user) {
        def msgMap = [:]
        Folder target
        try {
            target = Folder.get(folderId)
            if (!target) {
                throw new RuntimeException('error.folder.not.found')
            }
        }
        catch (Exception e) {
            return ['moveFail': [e.message]]
        }

        def reindexList = []
        idList.each { id ->
            try {
                log.debug("move: $id")
                Folder source = Folder.get(id)
                if (source.parent == target || source == target) {
                    msgMap.put(id, ['folder.move.unnecessary'])
                }
                else {

                    if (!checkPermissions(source, user, [PermissionName.EDIT_FOLDER])) {
                        msgMap.put(id, ['error.access.denied'])
                        return
                    }

                    def parents = target.getParentFolders(target)
                    if (parents.find { it == source }) {
                        msgMap.put(id, ['error.no.move.into.self'])
                        return
                    }
                    Folder oldFolder = source.parent
                    source.parent = target
                    source.save()
                    reindexList.add(source)
                    log.debug("moved folder #${source.id} from folder #${oldFolder.id}: ${oldFolder.name} to #${target.id}: ${target.name}")
                    msgMap.put(id, ['folder.move.ok'])
                }
            }
            catch (Exception e) {
                log.debug("move failed.", e)
                msgMap.put(id, ['folder.move.fail', e.message])
            }
        }
        reindexList.each { Folder folder ->
            folder.updateIndex()
        }
        return msgMap
    }

    /**
     * Installation-Hint<br>
     * Create a Folder whose parent equals it's own id and whose name is equals the ROOT_FOLDER_NAME.
     * This is the default folder in which objects and folders are created if no parent_id is given.
     *
     * @return Folder rootFolder
     */
    Folder findRootFolder() {
        def rootFolder = Folder.find("from Folder f where f.name=:name and f.parent=f",
                [name: Constants.ROOT_FOLDER_NAME]
        )
        if (!rootFolder) {
            Logger log = LoggerFactory.getLogger(Folder.class);
            log.error("RootFolder is missing!");
            throw new CinnamonConfigurationException("Could not find the root folder. Please create a folder called " + Constants.ROOT_FOLDER_NAME
                    + " with parent_id == its own id.");
        }

        return rootFolder;
    }

    /**
     * Groovy version of FolderDAOHibernate.findAllByPath()
     */
    List<Folder> findAllByPath(String path, Boolean createMissingFolders) {
        def parent = findRootFolder()

        List<Folder> ret = new ArrayList<Folder>()
        path.split("/").each() { seg ->
            if (seg.length() > 0) {
                def folders = Folder.findAllWhere(parent: parent, name: seg)

                if (folders.size() == 0) { // create missing folders
                    if (createMissingFolders) {
                        Folder f = new Folder(name: seg,
                                owner: userService.findAdminUser(),
                                parent: parent,
                                type: FolderType.findByName(Constants.FOLDER_TYPE_DEFAULT),
                                acl: Acl.findByName(Constants.ACL_DEFAULT))
                        f.save(flush: true)
                        folders = [f]
                    }
                    else {
                        throw new RuntimeException("Invalid path '$path'")
                    }
                }
                Folder folder = folders[0]
                parent = folder
                ret << folder
            }
        }
        return ret
    }

    void createHomeFolders(UserAccount user) {
        // create home/, searches/, carts/, config/ in .../users/<username>-Folder:
        def folderPath = findAllByPath('/system/users/', true)
        log.debug "folderPath = ${folderPath.dump()}"

        def defaultAcl = Acl.findByName(Constants.ACL_DEFAULT)
        def defaultType = FolderType.findByName(Constants.FOLDER_TYPE_DEFAULT)
        Folder userFolder = null
        Folder existsCheck = Folder.findByNameAndParent(user.name,folderPath[-1])
        if(existsCheck){
            userFolder = existsCheck
            log.debug "found user folder '${userFolder.dump()}'"
        }
        else {
            userFolder = new Folder(user.name, defaultAcl, folderPath[-1], userService.findAdminUser(), defaultType)
            userFolder.save(flush: true)
            log.debug "created user folder '${userFolder.dump()}'"
        }

        // if user was deleted and is re-created, his folders may still be around
        // this most likely happens during testing. (we cannot simply delete the folders, they may contain objects)
        ['home', 'searches', 'carts', 'config'].each {
            Folder f = Folder.findByNameAndParent(it,userFolder)
            if (f) {
                log.debug("Folder ${f} already exists.")
            }
            else {
                def folder = new Folder(it, defaultAcl, userFolder, user, defaultType)
                folder.save()
                log.debug "created folder '${folder.dump()}'"
            }
        }
    }
}


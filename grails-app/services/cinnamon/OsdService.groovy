package cinnamon

import cinnamon.references.Link
import org.apache.commons.codec.digest.DigestUtils
import org.dom4j.Document
import org.dom4j.DocumentHelper
import org.dom4j.Element
import cinnamon.exceptions.CinnamonException
import cinnamon.utils.ParamParser
import cinnamon.relation.Relation
import cinnamon.relation.RelationType
import cinnamon.global.PermissionName
import cinnamon.global.Constants
import org.springframework.web.multipart.MultipartFile

class OsdService {

    def inputValidationService
    def luceneService
    def userService
    def imageService
    def metasetService
    def cinnamonTikaService
    def infoService

    /**
     * Turn a collection of data objects into an XML document. Any exceptions encountered during
     * serialization are turned into error-Elements which contain the exception's message.
     *
     * @param results
     * @return Document
     */
    Document generateQueryObjectResultDocument(Collection<ObjectSystemData> results, Boolean includeSummary) {
        return generateQueryObjectResultDocument(results, false, includeSummary);
    }

    /**
     * Turn a collection of data objects into an XML document. Any exceptions encountered during
     * serialization are turned into error-Elements which contain the exception's message.
     *
     * @param results the source collection of results to be used to generate the XML document.
     * @param withMetadata if true, include object custom metadata in the output (which can get quite large).
     * @return Document
     */
    Document generateQueryObjectResultDocument(Collection<ObjectSystemData> results,
                                               Boolean withMetadata, Boolean includeSummary) {
        Document doc = DocumentHelper.createDocument();
        Element root = doc.addElement("objects");

        for (ObjectSystemData osd : results) {
            Long id = osd.getId();
            log.debug("working on object: " + id);
            Element data;
            try {
                data = osd.convert2domElement();
                if (withMetadata) {
                    data.add(ParamParser.parseXml(osd.getMetadata(), null));
                }
                if(includeSummary){
                    data.add(ParamParser.parseXml(osd.summary, null))
                }
                root.add(data);
            } catch (CinnamonException ex) {
                /*
                     * Note: any exceptions encountered here are probably serious bugs,
                     * which could be caused by corrupted data or faulty serialization
                     * routines.
                     * So, let's report them as errors instead of debug messages.
                     */
                log.error("Error serializing object: " + id + " - " + ex.getMessage());
                Element error = DocumentHelper.createElement("error").addText(ex.getLocalizedMessage());
                error.addElement("id").addText(id.toString());
                root.add(error);
            }
        }
        return doc;
    }

    void copyContent(ObjectSystemData source, ObjectSystemData copy) {
        copyContent(infoService.repositoryName, source, copy)
    }

    void copyContent(String repositoryName, ObjectSystemData source, ObjectSystemData copy) {
        String conPath = source.getContentPath();
        if (conPath != null && conPath.length() > 0) {
            String fullContentPath = source.getFullContentPath();

            log.debug("ContentPath: " + fullContentPath +
                    " and Size is: " + source.getContentSize());
            try {
                String targetPath = ContentStore.copyToContentStore(fullContentPath, repositoryName);
                log.debug("targetPath = " + targetPath);
                if (targetPath.length() > 0) {
                    copy.setContentPath(targetPath, repositoryName);
                    cinnamonTikaService.parse(copy)
                }
                copy.setFormat(source.getFormat());
            } catch (IOException ex) {
                throw new CinnamonException(ex);
            }
        }
    }

    void copyMetadata(ObjectSystemData source, ObjectSystemData copy) {
        def meta = source.getMetadata()
        copy.storeMetadata(meta)
        copy.save()
    }

    public List<ObjectSystemData> findAllVersions(ObjectSystemData osd) {
        if (!osd.getCmnVersion().equals("1")) {
            osd = osd.root
        }
        return ObjectSystemData.findAll("from ObjectSystemData o where o.root=:root order by o.id desc",
                [root: osd])
    }

    /**
     * Copy relations of an object if the relationType demands it.
     *
     * @param source from which the relations will copied.
     * @param target for which the new relations will be created.
     */
    public void copyRelations(ObjectSystemData source, ObjectSystemData target) {
        copyRelations(source, target, CopyRelationMode.COPY)
    }

    /**
     * Copy relations of an object if the relationType demands it.
     *
     * @param source from which the relations will copied.
     * @param target for which the new relations will be created.
     * @param mode chooses whether to look at the version- or copy-related flags when determining 
     *  the need to copy relations 
     */
    public void copyRelations(ObjectSystemData source, ObjectSystemData target, CopyRelationMode mode) {
        List<Relation> relations =
            Relation.findAll("from Relation r where r.leftOSD=:left or r.rightOSD=:right",
                    [left: source, right: source]);
        for (Relation rel : relations) {
            /*
             * The relation will only be copied if the cloneOn{left,right}Copy flag is set on the
             * {left,right} part of the osd which is copied.
             * Example: html to image relation should (normally) have the clone flag set on the
             * left, but not on the right part of the relation. If the image is copied, the copy
             * will not have a relation to the html file. If the html file is copied, the copy
             * should have a relation to the image.
             */
            RelationType relationType = rel.type
            if (mode.equals(CopyRelationMode.COPY)) {
                if (relationType.cloneOnLeftCopy && rel.leftOSD.equals(source)) {
                    Relation relCopy = Relation.findOrSaveWhere(type: rel.type, leftOSD: target, rightOSD: rel.rightOSD, metadata: rel.metadata)
                    log.debug("created new Relation: " + relCopy)
                }
                if (relationType.cloneOnRightCopy && rel.rightOSD.equals(source)) {
                    Relation relCopy = Relation.findOrSaveWhere(type: rel.type, leftOSD: rel.leftOSD, rightOSD: target, metadata: rel.metadata)
                    log.debug("created new Relation: " + relCopy)
                }
            }
            else { // = CopyRelationMode.VERSION
                if (relationType.cloneOnLeftVersion && rel.leftOSD.equals(source)) {
                    Relation relCopy = Relation.findOrSaveWhere(type: rel.type, leftOSD: target, rightOSD: rel.rightOSD, metadata: rel.metadata)
                    log.debug("created new Relation: " + relCopy)
                }
                if (relationType.cloneOnRightVersion && rel.rightOSD.equals(source)) {
                    Relation relCopy = Relation.findOrSaveWhere(type: rel.type, leftOSD: rel.leftOSD, rightOSD: target, metadata: rel.metadata)
                    log.debug("created new Relation: " + relCopy)
                }
            }
        }
    }

    void delete(ObjectSystemData osd, String repository) {
        delete(osd, false, false, repository);
    }

    void delete(ObjectSystemData osd, Boolean killDescendants, Boolean removeLeftRelations, String repository) {
        log.debug("Found osd");
        if (!checkPermissions(osd, userService.user, [PermissionName.DELETE_OBJECT])) {
            throw new RuntimeException('error.delete.denied')
        }

        ObjectSystemData predecessor = osd.getPredecessor();

        log.debug("checking for descendants ");
        boolean hasDescendants = ObjectSystemData.countByPredecessor(osd) > 0;
        if (killDescendants && hasDescendants) {
            def preds = ObjectSystemData.findAll("from ObjectSystemData o where o.predecessor=:pred order by id desc", [pred: osd])
            preds.each { pre ->
                delete(pre, killDescendants, removeLeftRelations, repository);
            }
        }
        else if (hasDescendants) {
            throw new CinnamonException("error.delete.has_descendants");
        }

        // check for protected relations
        List<Relation> relations = Relation.findAllByLeftOSDOrRightOSD(osd, osd);
        for (Relation rel : relations) {
            RelationType rt = rel.getType();
            /*
             * if an object is protected by the relation type, it
             * must not be deleted.
             */
            if ((rt.rightobjectprotected && rel.getRightOSD().equals(osd)) ||
                    (rt.leftobjectprotected && rel.getLeftOSD().equals(osd) && !removeLeftRelations)
            ) {
                throw new CinnamonException("error.protected_relations");
            }
        }

        // delete relations
        for (Relation rel : relations) {
            rel.delete()
        }

        // delete links (references)
        def links = Link.findAllByOsd(osd)
        links.each {
            it.delete()
        }
        
        osd.metasets.collect{it}.each{
            metasetService.unlinkMetaset(osd, it.metaset)
        }
        log.debug("object deleted.");

        /*
           * An object is latestBranch, if it has no descendants.
           * You can only delete an object without descendants.
           * So, the predecessor's only child has been deleted and this
           * makes the predecessor latestBranch.
           *
           * An object is latestHead, if it is not of part of a branch and has no
           * descendants. As we already said, this predecessor cannot have any
           * descendants and so we can set latestHead to true if it is part of
           * the main branch (no . in version) *and* there is no other object of the
    	   * main branch which is already latestHead 
    	   *      (that is: if branch object 1.1 of a tree with versions [1,1.1,2] is deleted,
    	   *      do not make v1 to latestHead, since v2 is already latestHead).
           */
        if (predecessor != null) {
            predecessor.fixLatestHeadAndBranch(ObjectSystemData.findAllByPredecessor(predecessor))
        }

        ContentStore.deleteObjectFile(osd, repository);
        osd.delete(flush: true)
    }

    public void delete(Long id, String repository) {
        ObjectSystemData osd = ObjectSystemData.get(id);
        if (osd == null) {
            throw new CinnamonException("error.object.not.found");
        }
        delete(osd, repository);
    }
// commented out: unused?
//    Boolean mayBrowseObject(ObjectSystemData osd, user) {
//        def validator = new Validator(user)
//        try {
//            log.debug("validate browse permission on: ${osd.name} (acl: ${osd.acl.name})")
//            validator.validatePermissionByName(osd.acl, PermissionName.BROWSE_OBJECT)
//        }
//        catch (Exception e) {
//            log.debug("user does not have browse permission.", e)
//            return false
//        }
//        return true
//    }

    void acquireLock(osd, user) {
        if (osd.locker) {
            if (osd.locker == user) {
                return
            }
            throw new RuntimeException('error.locked.already')
        }
        osd.locker = user
    }

    void unlock(osd, user) {
        // should we raise an exception if this osd is not locked at all? 
        osd.locker = null
    }

    Boolean checkPermissions(ObjectSystemData osd, UserAccount user, List permissions) {
        Validator val = new Validator(user)
        try {
            val.validatePermissions(osd, permissions)
        }
        catch (Exception e) {
            log.debug("${user?.name} failed permission check:", e)
            return false
        }
        return true
    }

    void storeContent(ObjectSystemData osd, String contentType, Long formatId, File file, String repositoryName) {
        Format format = Format.get(formatId)
        if (!format) {
            throw new RuntimeException('error.missing.format')
        }
        log.debug("copy data from temporary storage to repository")
        def uploadedFile = new UploadedFile(file.absolutePath, UUID.randomUUID().toString(), osd.name, contentType, file.length())
        def contentPath = ContentStore.upload(uploadedFile, repositoryName);
        log.debug("parse data with Apache Tika")
        cinnamonTikaService.parse(osd)
        osd.setContentPathAndFormat(contentPath, format, repositoryName);
        if (osd.contentPath != null &&
                osd.contentPath.length() == 0) {
            throw new CinnamonException("error.storing.upload");
        }
        log.debug("calculate sha256hex value of new content")
        String sha256Hex = DigestUtils.sha256Hex(new FileInputStream(osd.getContentAsFile(repositoryName)))
        osd.contentHash = sha256Hex
    }

    Map<String, List> deleteList(idList, String repository, VersionType versionType) {
        def msgMap = [:]

        if (versionType == VersionType.ALL) {
            return deleteAllVersions(idList, repository)
        }

        // http://jira.grails.org/browse/GRAILS-9091
        idList.collect{it}.sort().reverse().each { id ->
            try {
                log.debug("delete: $id with version policy: ${versionType}")

                switch (versionType) {
                    case VersionType.BRANCHES: return deleteBranches(idList, repository); break;
                    case VersionType.HEAD: id = ObjectSystemData.findByIdAndLatestHead(id, true).id?.toString(); break;
                    case VersionType.SELECTED: break;
                }
                delete(Long.parseLong(id), repository);
                msgMap.put(id, ['osd.delete.ok'])
            }
            catch (Exception e) {
                log.debug("delete failed.", e)
                msgMap.put(id, ['osd.delete.fail', e.message])
            }
        }
        return msgMap
    }

    Map<String, List> deleteBranches(idList, repository) {
        def msgMap = [:]
        idList.each { id ->
            try {
                def deleteMap = [:]
                def osd = ObjectSystemData.get(id)
                if (osd) {
                    def osds = ObjectSystemData.findAll("from ObjectSystemData o where o.latestBranch=true and o.root=:root order by id desc",
                            [root: osd.root ?: osd]
                    )
                    deleteMap = deleteList(osds.collect { it.id.toString() }, repository, VersionType.SELECTED)
                }
                else {
                    log.debug("osd $id was not found - probably already deleted.")
                }
                msgMap.putAll(deleteMap)
            }
            catch (Exception e) {
                log.debug("deleteBranches: fail:", e)
                msgMap.put(id, ['osd.delete.branches.fail', e.message])
            }
        }
        return msgMap
    }

    Map<String, List> deleteAllVersions(idList, repository) {
        def msgMap = [:]
        idList.each { id ->
            try {
                def osd = ObjectSystemData.get(id)
                if (osd) {
                    delete(osd, true, false, repository)
                }
                else {
                    log.debug("osd $id was not found - probably already deleted.")
                }
                msgMap.put(id, ['osd.delete.all.ok'])
            }
            catch (Exception e) {
                log.debug("deleteAllVersions fail:", e)
                msgMap.put(id, ['osd.delete.all.fail', e.message])
            }
        }
        return msgMap

    }

    ObjectSystemData createOsd(request, params, String repositoryName, MultipartFile file, UserAccount user, Folder folder) {
        ObjectType objectType = (ObjectType) inputValidationService.checkObject(ObjectType.class, params.objectType, true)
        if (!objectType) {
            objectType = ObjectType.findByName(Constants.OBJTYPE_DEFAULT)
//                throw new RuntimeException('error.missing.objectType')
        }
        ObjectSystemData osd
        String name = params.name
        file = file ?: request.getFile('file')

        if (file.isEmpty()) {
            if (!name) {
                throw new RuntimeException('error.missing.name')
            }
            osd = new ObjectSystemData(name, user, folder)
        }
        else {
            if (!name) {
                name = file.originalFilename
            }
            osd = new ObjectSystemData(name, user, folder)
            File tempFile = File.createTempFile('cinnamon_upload_', null)
            file.transferTo(tempFile)

            Format format = (Format) inputValidationService.checkObject(Format.class, params.format, true)
            if (!format) {
                throw new RuntimeException('error.missing.format')
            }

            def uploadedFile = new UploadedFile(tempFile.absolutePath, UUID.randomUUID().toString(), name, file.contentType, tempFile.length())
            def contentPath = ContentStore.upload(uploadedFile, repositoryName);
            log.debug("contentPath: $contentPath")
            osd.setContentPathAndFormat(contentPath, format, repositoryName);
            if (osd.getContentPath() != null &&
                    osd.getContentPath().length() == 0) {
                throw new CinnamonException("error.storing.upload");
            }
        }
        osd.type = objectType
        osd.setCmnVersion('1')
        osd.save(flush: true)
        log.debug("created object with id: ${osd.id}")
        log.debug("repo: ${repositoryName}")
        cinnamonTikaService.parse(osd)
        return osd
    }

    void saveFileUpload(request, ObjectSystemData osd, UserAccount user, formatId, String repositoryName) {
        saveFileUpload(request, osd, user, formatId, repositoryName, true)
    }

    void saveFileUpload(request, ObjectSystemData osd, UserAccount user, formatId, String repositoryName, Boolean reIndex) {
        MultipartFile file = request.getFile('file')
        if (file.isEmpty()) {
            throw new RuntimeException('error.missing.content')
        }
        else {
            // remove any image thumbnails on the content
            log.info("remove metaset thumbnail (if any exist)")
            metasetService.unlinkMetaset(osd, osd.fetchMetaset(Constants.METASET_THUMBNAIL))
            log.info("remove metaset tika")
            metasetService.unlinkMetaset(osd, osd.fetchMetaset(Constants.METASET_TIKA))
            log.debug("transfer data to tempFile")
            File tempFile = File.createTempFile('cinnamon_upload_', null)
            file.transferTo(tempFile)
            storeContent(osd, file.contentType, formatId, tempFile, repositoryName)
            osd.updateAccess(user)
            osd.save()
            log.info("parse new metaset")
            cinnamonTikaService.parse(osd)
        }
    }

    /**
     * Create a new Object from an optional locally available file.
     * @param params a map which should contain the following key-value pairs:
     *        objectType: the name of the Cinnamon object type, if not specified, uses the default objectType.
     *        name: name of the new object, if not given, will try to use filename if file is valid.
     *              Cinnamon will not create an object without a name, so params.name and/or the file parameter must be set.
     *        format: name of a Cinnamon format, must be set if file parameter is set.
     *
     * @param repositoryName the name of the repository where the object will be created.
     * @param file [optional] a data file which will be uploaded to the repository datastore
     * @param folder a Cinnamon folder object which will be the parent of the new object.
     * @return the new OSD object
     */
    ObjectSystemData createOsd(params, String repositoryName, File file, Folder folder) {
        UserAccount user = userService.user
        ObjectType objectType = (ObjectType) inputValidationService.checkObject(ObjectType.class, params.objectType, true)
        if (!objectType) {
            objectType = ObjectType.findByName(Constants.OBJTYPE_DEFAULT)
        }
        ObjectSystemData osd
        String name = params.name

        if (file == null || !file.isFile()) {
            if (!name) {
                throw new RuntimeException('error.missing.name')
            }
            osd = new ObjectSystemData(name, user, folder)
        }
        else {
            if (!name) {
                name = file.name
            }
            osd = new ObjectSystemData(name, user, folder)

            Format format = (Format) inputValidationService.checkObject(Format.class, params.format, true)
            if (!format) {
                throw new RuntimeException('error.missing.format')
            }

            def uploadedFile = new UploadedFile(file.absolutePath, UUID.randomUUID().toString(), name, format.contenttype, file.length())
            def contentPath = ContentStore.upload(uploadedFile, repositoryName);
            log.debug("contentPath: $contentPath")
            osd.setContentPathAndFormat(contentPath, format, repositoryName);
            if (osd.getContentPath() != null &&
                    osd.getContentPath().length() == 0) {
                throw new CinnamonException("error.storing.upload");
            }
            cinnamonTikaService.parse(osd)
        }
        osd.type = objectType
        osd.setCmnVersion('1')
        osd.save(flush: true)
        log.debug("created object with id: ${osd.id}")
        log.debug("repo: ${repositoryName}")
        return osd
    }

    /**
     * Determine the format of an uploaded file using the extension as a guide line.
     * Extension 'jpeg' will return Format object for 'jpg'.
     * In case of an unknown file type, the format will be 'format.binary' with extension 'unknown'
     * @param file the uploaded file
     * @return Cinnamon Format object
     */
    Format determineFormat(File file) {
        def extension
        if (file.name.contains('.')) {
            extension = file.name.toLowerCase().split('\\.').last()
            if (extension == 'jpeg') {
                extension = 'jpg'
            }
        }
        else {
            extension = 'unknown'
        }
        def format = Format.findByExtension(extension)
        if (!format) {
            format = Format.findByExtension('unknown')
        }
        return format
    }

    /**
     * Create a new OSD object with content.
     * While the other createOsd methods work with request / params maps, this method takes a more
     * direct approach. You must supply valid objects for all parameters (name being optional).
     * @param repositoryName the name of the repository where the object will be created.
     * @param file a data file which will be uploaded to the repository datastore
     * @param name [optional] the name of the new object. If not given, will try to use filename.
     * @param folder a Cinnamon folder object which will be the parent of the new object.
     * @param format the format of the content (for example, JPEG or XML)
     * @param objectType the object type of the new OSD
     * @return the new OSD object
     */
    ObjectSystemData createOsd(String repositoryName, File file, String name, Folder folder, Format format, ObjectType objectType) {
        UserAccount user = userService.user
        ObjectSystemData osd
        if (!name) {
            name = file.name
        }
        osd = new ObjectSystemData(name, user, folder)

        def uploadedFile = new UploadedFile(file.absolutePath, UUID.randomUUID().toString(), name, format.contenttype, file.length())
        def contentPath = ContentStore.upload(uploadedFile, repositoryName);
        log.debug("contentPath: $contentPath")
        osd.setContentPathAndFormat(contentPath, format, repositoryName);
        if (osd.getContentPath() != null &&
                osd.getContentPath().length() == 0) {
            throw new CinnamonException("error.storing.upload");
        }
        osd.type = objectType
        osd.setCmnVersion('1')
        osd.save(flush: true)
        log.debug("created object with id: ${osd.id}")
        log.debug("repo: ${repositoryName}")
        cinnamonTikaService.parse(osd)
        return osd
    }

    Map<String, List> moveToFolder(idList, folderId, repository, VersionType versionType, UserAccount user) {
        def msgMap = [:]
        def folder
        try {
            folder = Folder.get(folderId)
            if (!folder) {
                throw new RuntimeException('error.folder.not.found')
            }
        }
        catch (Exception e) {
            return ['moveFail': [e.message]]
        }

        idList.each { id ->
            try {
                log.debug("move: $id")
                ObjectSystemData osd = ObjectSystemData.get(id)
                if (osd.parent != folder) {
                    if (checkPermissions(osd, user, [PermissionName.MOVE])) {
                        Folder oldFolder = osd.parent
                        osd.parent = folder
                        osd.save(flush: true)
                        log.debug("moved #${osd.id} from folder #${oldFolder.id}: ${oldFolder.name} to #${folder.id}: ${folder.name}")
                        msgMap.put(id, ['osd.move.ok'])
                    }
                    else {
                        msgMap.put(id, ['osd.move.forbidden'])
                    }
                }
                else {
                    msgMap.put(id, ['osd.move.unnecessary'])
                }
            }
            catch (Exception e) {
                log.debug("move failed.", e)
                msgMap.put(id, ['osd.move.fail', e.message])
            }
        }
        return msgMap
    }

    Map fetchPreviews(List<ObjectSystemData> osds, Integer previewSize) {
        def previews = [:]
        osds.each { osd ->
            if (osd.format?.contenttype?.startsWith('image')) {
                def thumbnail = imageService.fetchThumbnail(osd, infoService.repositoryName, previewSize, true)
                previews.put(osd, thumbnail)
            }
        }
        return previews
    }
}

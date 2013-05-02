package cinnamon

import cinnamon.exceptions.CinnamonException

import java.util.concurrent.ConcurrentLinkedQueue
import humulus.EnvironmentHolder

class CopyService {

    /*
     * moved from a Java class to Groovy, because Grails 2.1.1 did not find the GORM methods when 
     * using this plugin in another project.
     */
    
    def osdService
    def folderService
    def luceneService

    /**
     * Copy a root object and all of its descendants.
     *
     * @param source the source object
     * @param targetFolder the folder in which the copy will be created.
     * @param activeUser the user who will be owner / modifier of the copied objects.
     * @return the root object of the new objectTree
     */
    CopyResult copyObjectTree(session, ObjectSystemData source, Folder targetFolder, UserAccount activeUser) {
        CopyResult cr = new CopyResult();

        List<ObjectSystemData> allVersions = osdService.findAllVersions(source);

        // create copies of all versions:
        ObjectTreeCopier objectTreeCopier = new ObjectTreeCopier(activeUser, targetFolder);
        ObjectSystemData currentOsd = null;
        try {
            CopyService.log.debug("create  full copies of all versions");

            for (ObjectSystemData osd : allVersions) {
                currentOsd = osd;
                CopyService.log.debug("create full copy of: " + osd.getId());
                ObjectSystemData copy = objectTreeCopier.createFullCopy(osd);
                objectTreeCopier.getCopyCache().put(osd, copy);
                CopyService.log.debug("copy relations");
                osdService.copyRelations(osd, copy);
                CopyService.log.debug("copy content");
                osdService.copyContent(osd, copy);
                cr.addObject(copy);
            }
        } catch (Exception ex) {
            /*
             * If an exception occurs, we must terminate the whole tree,
             * as in most cases we could only get a stunted version (with missing branches or
             * missing content etc.)
             */
            for (ObjectSystemData osd : objectTreeCopier.getCopyCache().keySet()) {
                osd.deleteContent(session.getLocalRepositoryName());
                osd.delete();
            }
            cr = new CopyResult();
            cr.addFailure(currentOsd, new CinnamonException("Failed to copy tree of OSD.", ex));
        }

        return cr;
    }

    /**
     * Copy a source folder to a target folder. Copies recursively.
     * @param target the target folder in which the copy is created
     * @param croakOnError if true, do not continue past an error.
     * @param versions a versionType object that determines which OSDs inside the folder tree to copy.
     * @param user the user who issued the copyFolder command. This user is the new owner of the copy and he is also
     * used for permission checks.
     * @return a CopyResult object containing information about new folders and objects as well as error messages.
     */
    public CopyResult copyFolder(Folder source, Folder target, String repositoryName,
                                 Boolean croakOnError, VersionType versions,
                                 UserAccount user) {
        /*
         * validate read permissions on source folder
         * validate write permissions on target folder
         * create folder in target folder
         * create a CopyResult object
         * this folder has sub folders? Copy them. Add their CopyResult to the existing one.
         * this folder has OSDs? Copy them.
         *
         */
        CopyResult copyResult = new CopyResult();
        if(source == target){
            return copyResult.addFailure(target, 'error.source.is.target')
        }
        
        def parents = target.getParentFolders(target)        
        if(parents.find{it == source}){
            return copyResult.addFailure(target, 'error.no.copy.into.self')
        }
        
        Validator validator = new Validator(user);
        try {
            // we need permission to browse this folder.
            validator.validateGetFolder(source);
        }
        catch (CinnamonException ce) {
            return copyResult.addFailure((Folder) source, ce);
        }

        try {
            // we need the permission to create a folder inside the target folder.
            validator.validateCreateFolder(target);

        }
        catch (CinnamonException ce) {
            return copyResult.addFailure(target, ce);
        }
        
        Folder copy = new Folder(source);
        Boolean fixName = false
        if(copy.name == source.name && copy.parent == source.parent){
            // cannot have two folders with the same name inside a folder.
            fixName = true
            copy.name = UUID.randomUUID().toString()
        }
        copy.owner = user;
        copy.parent = target;
        copy.save(flush: true)
        copy.metadata = source.metadata
        if(fixName){
            // copy only has an id after save.
            copy.name = "${source.name}_${copy.id}"
            copy.save(flush: true)
        }

        // copy child folders
        List<Folder> children = folderService.getSubfolders(source);
        for (Folder child : children) {
            CopyResult cr = copyFolder(child, copy, repositoryName, croakOnError, versions, user);
            copyResult.addCopyResult(cr);
            if (copyResult.foundFailure() && croakOnError) {
                return copyResult;
            }
        }

        // copy content
        Collection<ObjectSystemData> folderContent = ObjectSystemData.findAllByParent(source);
        CopyService.log.debug("folderContent contains " + folderContent.size() + " objects.");

        if (versions == VersionType.ALL) {
            CopyService.log.debug("copy all versions");
            // copy all versions
            ObjectTreeCopier otc = new ObjectTreeCopier(user, copy, validator, true, repositoryName);
            copyResult.addCopyResult(copyAllVersions(folderContent, otc, croakOnError));
        }
        else if (versions == VersionType.BRANCHES) {
            CopyService.log.debug("copy newest branch objects");
            Set<ObjectSystemData> branches = new HashSet<ObjectSystemData>();
            for (ObjectSystemData osd : folderContent) {
                branches.add(osd.findLatestBranch());
            }
            copyResult.addCopyResult(createNewVersionCopies(branches, copy, repositoryName, validator, user, croakOnError));
        }
        else {
            CopyService.log.debug("copy head of object tree");
            // the default: copy head
            Set<ObjectSystemData> headSet = new HashSet<ObjectSystemData>();
            for (ObjectSystemData head : folderContent) {
                ObjectSystemData latestHead = head.findLatestHead();
                CopyService.log.debug("latestHead found for " + head.getId());
                headSet.add(latestHead);
            }
            copyResult.addCopyResult(createNewVersionCopies(headSet, copy, repositoryName, validator, user, croakOnError));
        }
        CopyService.log.debug("new folders: " + copyResult.newFolderCount());
        CopyService.log.debug("new objects: " + copyResult.newObjectCount());
        return copyResult;
    }

    /**
     * Copy all versions of the objects found in a folder. This will create the complete object tree of
     * the objects, so if an object has ancestors or descendants in other folders, those will be copied, too.
     * @param folderContent the content of the folder which should be copied completely.
     * @param otc a ObjectTreeCopier which is configured with a validator and correct activeUser.
     * @param croakOnError if true, stop in case of an error and return a CopyResult which contains the events so far.
     * @return a copyResult containing a collection of all failed and successful attempts at copying the
     * folder's contents.
     */
    CopyResult copyAllVersions(Collection<ObjectSystemData> folderContent, ObjectTreeCopier otc, Boolean croakOnError) {
        CopyResult copyResult = new CopyResult();

        ConcurrentLinkedQueue<ObjectSystemData> conQueue = new ConcurrentLinkedQueue<ObjectSystemData>();
        conQueue.addAll(folderContent);
        CopyService.log.debug("starting to copy " + conQueue.size() + " objects");

        for (ObjectSystemData source : conQueue) {
            try {
                // create a full copy of the whole object tree:
                otc.createFullCopy(source);
                copyResult.addCopyResult(otc.getCopyResult());
            }
            catch (Exception ex) {
                CopyService.log.debug("objectTreeCopy failed for id " + source.getId(), ex);
                // copy failed - now we have to cleanup and remove the already created copies:
                ObjectSystemData brokenCopy = otc.getCopyCache().get(source);
                if (brokenCopy != null) {
                    // we should nuke all other objects with the same root,
                    // as they won't be amendable to a copy operation either.
                    for (ObjectSystemData osd : conQueue) {
                        if (osd.getRoot().equals(brokenCopy.getRoot())) {
                            conQueue.remove(osd);
                        }
                    }

                    // recursively delete the broken object tree.
                    osdService.delete(brokenCopy.getRoot(), true, true, EnvironmentHolder.environment.dbName);
                }

                CopyService.log.debug("cleanup complete.");
                copyResult.addFailure(source, new CinnamonException(ex));
                if (croakOnError) {
                    return copyResult;
                }
            }
        }
        return copyResult;
    }

    CopyResult createNewVersionCopies(Collection<ObjectSystemData> sources, Folder target, String repositoryName, Validator validator,
                                      UserAccount user, Boolean croakOnError) {
        CopyResult copyResult = new CopyResult();

        for (ObjectSystemData osd : sources) {
            CopyService.log.debug("trying to copy " + osd.getId());
            // check permissions:
            try {
                validator.validateCopy(osd, target);
            }
            catch (Exception ex) {
                copyResult.addFailure(osd, new CinnamonException(ex));
                if (croakOnError) {
                    return copyResult;
                }
            }
            ObjectSystemData newCopy = copyObject(osd, target, repositoryName, user)
            copyResult.addObject(newCopy);
        }
        return copyResult;
    }

    ObjectSystemData copyObject(osd, targetFolder, repository, user) {
        new Validator(user).validateCopy(osd, targetFolder)
        ObjectSystemData newCopy = new ObjectSystemData(osd, user);
        newCopy.setCmnVersion("1");
        newCopy.setRoot(newCopy);
        newCopy.setParent(targetFolder);
        newCopy.save(flush: true)
        log.debug("new id: ${newCopy.id}")
        osdService.copyMetadata(osd, newCopy)        
        osdService.copyContent(osd, newCopy)
        osdService.copyRelations(osd, newCopy)
        return newCopy
    }
    
    Map<String, List> copyObjectsToFolder(idList, folderId, repository, VersionType versionType, UserAccount user) {
        def msgMap = [:]
        def folder
        try {
            folder = Folder.get(folderId)
            if (!folder) {
                throw new RuntimeException('error.folder.not.found')
            }
        }
        catch (Exception e) {
            return ['copyFail': [e.message]]
        }

        idList.each { id ->
            try {
                log.debug("copy OSD: $id")
                ObjectSystemData osd = ObjectSystemData.get(id)
                switch (versionType) {
                    case VersionType.ALL: msgMap.putAll(copyAllOsdVersionsToFolder(osd, folder, repository, user));break
                    case VersionType.BRANCHES: msgMap.putAll(copyAllBranchesToFolder(osd, folder, repository, user)) ;break
                    case VersionType.HEAD: copyObject(osd.findLatestHead(), folder, repository, user)
                        msgMap.put(id,['osd.copy.ok'] )
                        break
                    case VersionType.SELECTED: copyObject(osd, folder, repository, user)
                        msgMap.put(id, ['osd.copy.ok'])
                        break
                }
            }
            catch (Exception e) {
                log.debug("copyObjectsToFolder failed.", e)
                msgMap.put(id, ['osd.copy.fail', e.message])
            }
        }
        return msgMap
    }

    Map copyAllBranchesToFolder(ObjectSystemData osd, Folder folder, String repository, UserAccount user) {
        def msgMap = [:]
        def branches = osd.findLatestBranches()
        branches.each{branchOsd ->
            try{
                copyObject(branchOsd, folder, repository, user)
                msgMap.put(branchOsd.id.toString(), ['osd.copy.ok'])
            }
            catch(Exception e){
                msgMap.put(branchOsd.id.toString(),['osd.copy.fail', e.message])
            }
        }
        return msgMap
    }
    
    Map copyAllOsdVersionsToFolder(ObjectSystemData osd, Folder folder, String repositoryName, UserAccount user) {
        def msgMap = [:]
        def id = osd.id.toString()
        def copyResult = copyAllVersions([osd], new ObjectTreeCopier(user, folder, new Validator(user), true, repositoryName), false,)
        if (copyResult.objectFailures.size() > 0) {
            copyResult.getObjectFailures().each {Long k, String v ->
                msgMap.put(k.toString(), [v])
            }
            def failMsg = copyResult.objectFailures.get(osd.id)
            if (failMsg) {
                msgMap.put(id, ['osd.copy.fail', failMsg])
            }
            else {
                msgMap.put(id, ['osd.copy.fail'])
            }
        }
        else {
            msgMap.put(id, ['osd.copy.ok'])
        }
        return msgMap
    }

    Map<String, List> copyFoldersToFolder(idList, folderId, repository, VersionType versionType, UserAccount user) {
        Map<String, List> msgMap = [:]
        def targetFolder
        try {
            targetFolder = Folder.get(folderId)
            if (!targetFolder) {
                throw new RuntimeException('error.folder.not.found')
            }
        }
        catch (Exception e) {
            return ['copyFail': [e.message]]
        }

        idList.each { id ->
            try {
                log.debug("copy folder: $id")
                Folder source = Folder.get(id)
                def copyResult = copyFolder(source, targetFolder, repository, false, versionType, user)
                if(copyResult.foundFailure()){
                    copyResult.folderFailures.each{k,v ->
                        msgMap.put(k.toString(), [v])
                    }
                    copyResult.objectFailures.each{k,v ->
                        msgMap.put(k.toString(), [v])
                    }
                }
                if(copyResult.newFolderCount() > 0){
                    copyResult.newFolders.each{f ->
                        msgMap.put(f.toString(), ['folder.copy.ok'])
                    }
                }
                if(copyResult.newObjectCount() > 0){
                    copyResult.newObjects.each{ o ->
                        msgMap.put(o.toString(), ['osd.copy.ok'])
                    }
                }
            }
            catch (Exception e) {
                log.debug("copy folders failed.", e)
                msgMap.put(id, ['osd.copy.fail', e.message])
            }
        }
        return msgMap
    }


}

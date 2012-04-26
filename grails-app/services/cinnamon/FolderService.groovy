package cinnamon

import cinnamon.global.Constants
import org.dom4j.Document
import org.dom4j.DocumentHelper
import org.dom4j.Element
import cinnamon.exceptions.CinnamonException

class FolderService {

    def osdService

    /**
     * Check if a folder has content objects (meaning OSD, not sub-folders)
     * @param folder the folder to check
     * @return true if there is at least one OSD which has this folder as parent, false otherwise.
     */

    Boolean hasContent(Folder folder) {
        return ObjectSystemData.findWhere(parent:folder) != null
    }

    /**
     * @return the root folder of the repository to which the user is logged in.
     */
    Folder findRootFolder(){
        return Folder.find("from Folder as f where name=:name and f=f.parent", [name : Constants.ROOT_FOLDER_NAME])
    }

    public List<Folder> getSubfolders(Folder parent) {
        if (parent == null) {
            return [findRootFolder()];
        } else {
            return Folder.findAll("select f from Folder f where f.parent=:parent and f.parent != f order by f.name",[parent:parent])
        }
    }

    /**
     * Returns the subfolders of the folder with the given id.
     * @param parentFolder - the folder whose sub-folders will be returned.
     * @return List of folders or an empty list.
     */
    public List<Folder> getSubfolders(Folder parentFolder, Boolean recursive){
        List<Folder> folders = Folder.findAll("select f from Folder f where f.parent=:parent and f.parent != f order by f.name",[parent:parentFolder])
        List<Folder> newFolders = new ArrayList<Folder>();
        if(recursive){
            for(Folder folder : folders){
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
    Document generateQueryFolderResultDocument(Collection<Folder> results){
        Document doc = DocumentHelper.createDocument();
        Element root = doc.addElement("folders");

        for (Folder	folder : results) {
            Long id= folder.getId();
            log.debug("working on object: "+id);
            try {
                folder.toXmlElement(root);
            }
            catch(CinnamonException ex) {
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
    public List<ObjectSystemData> getOSDList(Folder folder, Boolean recursive){
        return getFolderContent(folder, recursive);
    }

    public List<ObjectSystemData> getFolderContent(Folder folder, Boolean recursive){
        return getFolderContent(folder, recursive, null, null);
    }

    public List<ObjectSystemData> getFolderContent(Folder folder, Boolean recursive, Boolean latestHead, Boolean latestBranch){
        if(latestHead != null && latestBranch != null){
            q = ObjectSystemData.findAllByParent("findOsdsByParentAndLatestHeadOrLatestBranch");
            q.setParameter("latestHead", true);
            q.setParameter("latestBranch", true);
        }
        else if(latestHead != null){
            q = getSession().createNamedQuery("findOsdsByParentAndLatestHead");
            q.setParameter("latestHead", true);
        }
        else if(latestBranch != null){
            q = getSession().createNamedQuery("findOsdsByParentAndLatestBranch");
            q.setParameter("latestBranch", true);
        }
        else{
            q = getSession().createNamedQuery("findOsdsByParent");
        }
        q.setParameter("parent", folder);
        List<ObjectSystemData> osds = q.getResultList();
        if(recursive){
            List<Folder> subFolders = getSubfolders(folder);
            for(Folder f : subFolders){
                osds.addAll(getFolderContent(f, true, latestHead, latestBranch));
            }
        }
        return osds;
    }





     boolean folderExists(long id){
		Folder f = Folder.get(id);
		return f != null;
	}

    // TODO: looks weird. Where is this used?
    Boolean folderExists(Folder folder){
        if(folder == null){
            throw new CinnamonException("error.folder.not_found");
        }
        return folderExists(folder.id)
    }

    public void deleteFolder(Long id ) {
        log.debug("before loading folder");
        Folder folder;
        if (id == 0L) {
            folder = findRootFolder();
        } else {
            folder = Folder.get(id);
        }

        if (folder == null) {
            throw new CinnamonException("error.folder.not_found");
        }

        // check for subfolders:
        def subFolderCount = Folder.countByParent(folder);
        if ( subFolderCount > 0) {
            throw new CinnamonException("error.subfolders.exist");
        }

        // check for objects inside folder
        def contents = ObjectSystemData.countByParent(folder);
        if ( contents > 0) {
            throw new CinnamonException("error.folder.has_content");
        }

        folder.delete();
    }

    public List<Folder> findAllByPath(String path){
        return findAllByPath(path, false, null);
    }

    public List<Folder> findAllByPath(String path, Boolean autoCreate, Validator validator){
        def segs = path.split("/");

        Folder parent = findRootFolder();

        List<Folder> ret = new ArrayList<Folder>();
        ret.add(parent);
        for (String seg : segs) {
            if (seg.length() > 0) {
                List<Folder> results = Folder.findAllByParentAndName(parent, seg)
                if (results.isEmpty()) {
                    if(autoCreate){
                        if(validator != null){
                            validator.validateCreateFolder(parent);
                        }
                        Folder newFolder = new Folder(seg,"<meta />", parent.getAcl(), parent, parent.getOwner(), parent.getType() );
                        newFolder.setIndexOk(null); // so the IndexServer will index it.
                        newFolder.save()
                        ret.add(newFolder);
                        parent = newFolder;
                    }
                    else{
                        throw new CinnamonException("error.path.invalid", path);
                    }
                }
                else {
                    Folder folder = results.get(0);
                    parent = folder;
                    ret.add(folder);
                }
            }
        }
        return ret;
    }

    public Folder findByPath(String path){
        List<Folder> folders = findAllByPath(path);
        if(folders.isEmpty()){
            return null;
        }
        else{
            return folders.get(folders.size()-1);
        }
    }

    /**
     * Find all Folders where index_ok is NULL. Those are the ones
     * whose index is not current.
     * @param maxResults maximum number of results
     * @return List of Folders to index (limited by maxResults).
     */
    public List<Folder> findIndexTargets(Integer maxResults){
        return Folder.findAll("from Folder f where f.indexOk is NULL",[:], [max:maxResults])
    }

    /**
     * Set the indexed-column to 0 and trigger a re-indexing by the IndexServer.
     * @return the number of affected rows.
     */
    public Integer prepareReIndex() {
        return Folder.executeUpdate("UPDATE Folder f SET f.indexOk=NULL")
    }
}


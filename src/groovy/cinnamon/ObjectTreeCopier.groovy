package cinnamon

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;

/**
 *
 */
public class ObjectTreeCopier {

    private Logger log = LoggerFactory.getLogger(this.getClass());
    private UserAccount activeUser;
    private Map<ObjectSystemData, ObjectSystemData> copyCache = new HashMap<ObjectSystemData, ObjectSystemData>();
    protected Validator validator;
    protected Folder targetFolder;
    protected Boolean createFullCopies = false;
    protected CopyResult copyResult;
    protected Acl aclForCopies;

    OsdService osdService;

    public ObjectTreeCopier() {

    }

    /**
     * @param activeUser   the user who will be the owner / modifier of the copied objects.
     * @param targetFolder the folder in which to create the emptyObject.
     * @param validator     a validator instance to check permissions.
     * @param createFullCopies if true, create full copies with complete metadata and content, if false,
     * create empty copies.
     */
    public ObjectTreeCopier(UserAccount activeUser, Folder targetFolder, Validator validator, Boolean createFullCopies){
        this(activeUser, targetFolder);
        this.validator = validator;
        this.createFullCopies = createFullCopies;
    }

    public ObjectTreeCopier(UserAccount activeUser, Folder targetFolder) {
        copyResult = new CopyResult();

        this.activeUser = activeUser;
        this.targetFolder = targetFolder;
    }

    public Map<ObjectSystemData, ObjectSystemData> getCopyCache() {
        return copyCache;
    }

    public void setCopyCache(Map<ObjectSystemData, ObjectSystemData> copyCache) {
        this.copyCache = copyCache;
    }

    /**
     * Create a copy of an empty OSD object without the content or metadata in target folder.
     *
     * @param osd          the source object
     * @return the empty OSD
     */
    public ObjectSystemData createEmptyCopy(ObjectSystemData osd) {
        if (osd == null) {
            log.debug("cec: osd is null");
            // protect from NPE while using recursion
            return null;
        }
        // if a copy already exists, return cached version:
        if (copyCache.containsKey(osd) && copyCache.get(osd) != null) {
            log.debug(String.format("cec: osd %d already exists with value: %s",
                    osd.getId(), copyCache.get(osd)));
            return copyCache.get(osd);
        }

        log.debug("create new emptyCopy of " + osd.getId());
        ObjectSystemData copy = new ObjectSystemData();
        copy.setAcl(targetFolder.getAcl());
        copy.setAppName(osd.getAppName());
        copy.setCreated(Calendar.getInstance().getTime());
        copy.setCreator(activeUser);
        copy.setOwner(osd.getOwner());
        copy.setLanguage(osd.getLanguage());
        copy.setLatestBranch(osd.getLatestBranch());
        copy.setLatestHead(osd.getLatestHead());
        copy.setModified(Calendar.getInstance().getTime());
        copy.setModifier(activeUser);
        copy.setName(osd.getName());
        copy.setParent(targetFolder);
        if (osd.getState() != null) {
            copy.setState(osd.getState().getLifeCycleStateForCopy());
        }
        ObjectSystemData predecessor = checkCopyCache(osd.getPredecessor());

        log.debug("predecessor original: " + (osd.getPredecessor() == null ? null : osd.getPredecessor().getId()));
        log.debug("predecessor copy: " + (predecessor == null ? null : predecessor.getId()));
        copy.setPredecessor(predecessor);
        copy.setProcstate("_created");

        /*
         * The root of the object tree points to itself,
         * when queried for the root object. In this case,
         * the current new copy is the root of the target tree
         * and should point to itself.
         */
        if (osd.getRoot() == osd) {
            log.debug("this is the root copy => set root=copy");
            copy.setRoot(copy);
        }
        else {
            log.debug("set root to cached copy of osd.root: " + osd.getRoot());
            copy.setRoot(checkCopyCache(osd.getRoot()));
        }
        copy.setType(osd.getType());
        copy.setCmnVersion(osd.getCmnVersion());
        copy.save();
        copyCache.put(osd, copy);
        return copy;
    }


    /**
     * Check whether an object is alredy present as an emptyCopy.
     * If not, create it and add it to the copyCache Map.
     *
     * @param osd          to be copied.
     * @return an emptyCopy of the osd.
     */
    ObjectSystemData checkCopyCache(ObjectSystemData osd) {
        if (osd == null) {
            log.debug("checkCopyCache::null");
            return null;
        }
        if (copyCache.containsKey(osd) && copyCache.get(osd) != null) {
            log.debug("checkCopyCache::" + osd.getId() + " already exists in cache.");
            return copyCache.get(osd);
        }
        else {
            log.debug("checkCopyCache::" + osd.getId() + " will be created and cached.");
            ObjectSystemData newCopy;
            if(createFullCopies){
                newCopy = createFullCopy(osd);
            }
            else{
                newCopy = createEmptyCopy(osd);
            }
            /*
            * If a ConfigEntry exists with name translation.config, look if a special acl is set.
            * Otherwise, the ACL is just the default.
            */
            if(aclForCopies != null){
                log.debug("Set acl on new copy: "+aclForCopies.getName());
                newCopy.setAcl(aclForCopies);
            }
            copyCache.put(osd, newCopy);
            return newCopy;
        }
    }

    /**
     * Create a full copy of an OSD object and its tree with content and metadata in target folder.
     * This method requires the validator to be set.
     * @param osd  the source OSD
     * @return the new OSD
     */
    public ObjectSystemData createFullCopy(ObjectSystemData osd) {
        if (osd == null) {
            log.debug("cfc: osd is null");
            // protect from NPE while using recursion
            return null;
        }
        // if a copy already exists, return cached version:
        if (copyCache.containsKey(osd) && copyCache.get(osd) != null) {
            log.debug(String.format("cfc: osd %d already exists with value: %s",
                    osd.getId(), copyCache.get(osd)));
            return copyCache.get(osd);
        }
        validator.validateCopy(osd, targetFolder);
        log.debug("create new fullCopy of " + osd.getId());
        ObjectSystemData copy = new ObjectSystemData(osd, activeUser);

        ObjectSystemData predecessor = checkCopyCache(osd.getPredecessor());
        log.debug("predecessor original: " + (osd.getPredecessor() == null ? null : osd.getPredecessor().getId()));
        log.debug("predecessor copy: " + (predecessor == null ? null : predecessor.getId()));
        copy.setPredecessor(predecessor);

        copy.setProcstate("_created");
        copy.setParent(targetFolder);
        copy.setCmnVersion(osd.getCmnVersion());

        /*
         * The root of the object tree points to itself,
         * when queried for the root object. In this case,
         * the current new copy is the root of the target tree
         * and should point to itself.
         */
        if (osd.getRoot() == osd) {
            log.debug("this is the root copy => set root=copy");
            copy.setRoot(copy);

        }
        else {
            log.debug("set root to full Copy of osd.root: " + (osd.getRoot() == null ? null : osd.getRoot().getId()));
            copy.setRoot(checkCopyCache(osd.getRoot()));
        }

        copy.save();
        osdService.copyContent(osd, copy);
        osdService.copyMetadata(osd, copy)
        osdService.copyRelations(osd, copy)
        copyResult.addObject(copy);
        copyCache.put(osd, copy);
        return copy;
    }

    /**
     * The ObjectTreeCopier tracks all new objects in a CopyResult, so users can retrieve a list of all new objects.
     * @return the copyResult containing all new objects.
     */
    public CopyResult getCopyResult() {
        return copyResult;
    }

    /**
     * Reset the copyResult between two copy operations. This is useful in case you have to revert a copy operation
     * which was only partially successful.
     */
    public void resetCopyResult(){
        copyResult = new CopyResult();
    }

    public Acl getAclForCopies() {
        return aclForCopies;
    }

    public void setAclForCopies(Acl aclForCopies) {
        this.aclForCopies = aclForCopies;
    }
}

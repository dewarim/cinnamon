package cinnamon

// cinnamon - the Open Enterprise CMS project
// Copyright (C) 2007-2013 Texolution GmbH (http://texolution.eu)
// 
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
// (or visit: http://www.gnu.org/licenses/lgpl.html)

import cinnamon.global.PermissionName;
import cinnamon.index.ResultValidator;
import org.apache.lucene.document.Document
import cinnamon.exceptions.CinnamonException
import cinnamon.interfaces.Ownable
import cinnamon.interfaces.XmlConvertable
import cinnamon.index.Indexable
import org.slf4j.LoggerFactory
import org.slf4j.Logger
import cinnamon.relation.Relation
import cinnamon.index.SearchableDomain;

class Validator implements ResultValidator {

    Logger log = LoggerFactory.getLogger(this.class)

    Map<String, Permission> permissionCache = new HashMap<String, Permission>()

    protected UserAccount user;

    public Validator() {
    }

    public Validator(UserAccount user) {
        this.user = user;
    }

    public List<ObjectSystemData> filterUnbrowsableObjects(Collection<ObjectSystemData> objects) {
        if (objects == null || objects.size() == 0) {
            return []
        }
        List<ObjectSystemData> allowedObjects = new ArrayList<ObjectSystemData>();
        if (user.verifySuperuserStatus()) {
            allowedObjects.addAll(objects);
            return allowedObjects;
        }
        Permission browseObject = fetchPermission(PermissionName.BROWSE_OBJECT);
        for (ObjectSystemData osd : objects) {
            if (check_acl_entries(osd.getAcl(), browseObject, osd)) {
                allowedObjects.add(osd);
            }
            else {
                log.debug(String.format("No browse permission found for object %d", osd.getId()));
            }
        }
        return allowedObjects;
    }

    /**
     * Filter a collection of OSDs depending on whether the user may read its custom metadata.
     * @param objects a collection of objects to be filtered.
     * @return List of OSDs the user has a READ_OBJECT_CUSTOM_METADATA-permission for.
     */
    public List<ObjectSystemData> filterForCustomMetadata(Collection<ObjectSystemData> objects) {
        List<ObjectSystemData> allowedObjects = new ArrayList<ObjectSystemData>();
        Permission browseObject = fetchPermission(PermissionName.READ_OBJECT_CUSTOM_METADATA);
        for (ObjectSystemData osd : objects) {
            if (check_acl_entries(osd.getAcl(), browseObject, osd)) {
                allowedObjects.add(osd);
            }
            else {
                log.debug(String.format("No read_object_custom_metadata permission found for OSD %d", osd.getId()));
            }
        }
        return allowedObjects;
    }

    public List<cinnamon.Folder> filterUnbrowsableFolders(Collection<cinnamon.Folder> folders) {
        if (folders == null || folders.size() == 0) {
            return []
        }
        List<cinnamon.Folder> allowedFolders = new ArrayList<cinnamon.Folder>();
        if (user.verifySuperuserStatus()) {
            log.debug("UserAccount is admin - show all folders.");
            allowedFolders.addAll(folders);
            return allowedFolders;
        }
        Permission browseFolder = fetchPermission(PermissionName.BROWSE_FOLDER);
        for (cinnamon.Folder folder : folders) {
            if (check_acl_entries(folder.getAcl(), browseFolder, folder)) {
                allowedFolders.add(folder);
            }
            else {
                log.debug(String.format("No browse permission found for folder %d", folder.getId()));
            }
        }
        return allowedFolders;
    }

    public void validatePermissions(ObjectSystemData osd, List permissions) {
        for (String perm : permissions) {
            validateAgainstAcl(osd, fetchPermission(perm));
        }
    }

    public void validatePermissions(Folder folder, List permissions) {
        for (String perm : permissions) {
            validateFolderAgainstAcl(folder, fetchPermission(perm));
        }
    }

    public void validateCreate(cinnamon.Folder folder) {
        Permission createFolderPermission = fetchPermission(PermissionName.CREATE_OBJECT);
        validateFolderAgainstAcl(folder, createFolderPermission);
    }

    public void validateCopy(ObjectSystemData sourceObject, cinnamon.Folder targetFolder) {
        Permission readContent = fetchPermission(PermissionName.READ_OBJECT_CONTENT);
        validateAgainstAcl(sourceObject, readContent);
        Permission readMeta = fetchPermission(PermissionName.READ_OBJECT_CUSTOM_METADATA);
        validateAgainstAcl(sourceObject, readMeta);
        Permission readSys = fetchPermission(PermissionName.READ_OBJECT_SYS_METADATA);
        validateAgainstAcl(sourceObject, readSys);

        Permission writeToFolder = fetchPermission(PermissionName.CREATE_OBJECT);
        validateFolderAgainstAcl(targetFolder, writeToFolder);
    }

    public void validateCreateFolder(cinnamon.Folder parentFolder) {
        Permission createFolder = fetchPermission(PermissionName.CREATE_FOLDER);
        validateFolderAgainstAcl(parentFolder, createFolder);
    }

    public void validateDelete(ObjectSystemData osd) {
        Permission deleteObject = fetchPermission(PermissionName.DELETE_OBJECT);
        validateAgainstAcl(osd, deleteObject);
        def relations = Relation.executeQuery("""select r from Relation r where 
            (r.leftOSD=:osd1 and r.type.leftobjectprotected=true) or
            (r.rightOSD=:osd2 and r.type.rightobjectprotected=true)""",
                [osd1: osd, osd2: osd]
        )
        if (relations.size() > 0) {
            String msg = "Object " + osd.getId() + " cannot be deleted, it has protected relations.";
            throw new CinnamonException(msg);
        }
    }

    public void validateDeleteFolder(cinnamon.Folder folder) {
        Permission deleteFolder = fetchPermission(PermissionName.DELETE_FOLDER);
        validateFolderAgainstAcl(folder, deleteFolder);
    }

    public void validateGetContent(ObjectSystemData osd) {
        Permission readObject = fetchPermission(PermissionName.READ_OBJECT_CONTENT);
        validateAgainstAcl(osd, readObject);

    }

    public void validateGetMeta(ObjectSystemData osd) {
        Permission readObject = fetchPermission(PermissionName.READ_OBJECT_CUSTOM_METADATA);
        validateAgainstAcl(osd, readObject);
    }

    public void validateGetFolderMeta(cinnamon.Folder folder) {
        Permission readObject = fetchPermission(PermissionName.READ_OBJECT_CUSTOM_METADATA);
        validateFolderAgainstAcl(folder, readObject);
    }


    public void validateGetSysMeta(ObjectSystemData osd) {
        Permission browseObject = fetchPermission(PermissionName.READ_OBJECT_SYS_METADATA);
        validateAgainstAcl(osd, browseObject);
    }

    public void validateGetSysMeta(cinnamon.Folder folder) {
        Permission browseFolder = fetchPermission(PermissionName.BROWSE_FOLDER);
        validateFolderAgainstAcl(folder, browseFolder);
    }

    public void validateGetObjects(cinnamon.Folder folder) {
        // currently everybody may get objects, but validation 
        // per object can suppress those without browse permission
        Permission browseFolder = fetchPermission(PermissionName.BROWSE_FOLDER);
//		validateFolderAgainstAcl(cmd.get("parentid")), browseFolder);
        validateFolderAgainstAcl(folder, browseFolder);
    }

    public void validateSetObjectAcl(ObjectSystemData osd) {
        Permission writeObject = fetchPermission(PermissionName.SET_ACL);
        validateAgainstAcl(osd, writeObject);
    }

    public void validateMoveObject(ObjectSystemData osd, cinnamon.Folder targetFolder) {
        Permission writeObject = fetchPermission(PermissionName.MOVE);
        validateAgainstAcl(osd, writeObject);
        Permission writeToFolder = fetchPermission(PermissionName.CREATE_OBJECT);
        validateFolderAgainstAcl(targetFolder, writeToFolder);
    }

    public void validateGetFolder(cinnamon.Folder folder) {
        Permission browseFolder = fetchPermission(PermissionName.BROWSE_FOLDER);
        validateFolderAgainstAcl(folder, browseFolder);
    }

    public void validateLock(ObjectSystemData osd, UserAccount user) {
        if (osd.locker != null && !osd.locker.equals(user)) {
            throw new CinnamonException("Object " + osd.getId() + " is already locked.");
        }
        else {
            log.debug("about to validateAgainstAcl");
        }
        Permission lockPermission = fetchPermission(PermissionName.LOCK);
        validateAgainstAcl(osd, lockPermission);
    }

    public void checkLockStatus(ObjectSystemData osd) {
        UserAccount locker = osd.locker
        if (locker == null || !locker.equals(user)) {
            throw new CinnamonException("Object " +
                    osd.getId() + " must be locked by session user for setting content.")
        }
    }

    public void validateSetContent(ObjectSystemData osd) {
        Permission writeObject = fetchPermission(PermissionName.WRITE_OBJECT_CONTENT);
        validateAgainstAcl(osd, writeObject);
        checkLockStatus(osd);
    }

    public void validateSetMeta(ObjectSystemData osd) {
        Permission writeObject = fetchPermission(PermissionName.WRITE_OBJECT_CUSTOM_METADATA);
        validateAgainstAcl(osd, writeObject);
        checkLockStatus(osd);
    }

    public void validateSetSysMeta(ObjectSystemData osd) {
        Permission writeObject = fetchPermission(PermissionName.WRITE_OBJECT_SYS_METADATA);
        validateAgainstAcl(osd, writeObject);
        checkLockStatus(osd);
    }

    public void validateSetSummary(ObjectSystemData osd) {
        Permission writeObject = fetchPermission(PermissionName.WRITE_OBJECT_SYS_METADATA);
        validateAgainstAcl(osd, writeObject);
    }
    
    /**
     * Note: this is the one Permission that does currently not depend on an ACL.
     * The logic is thus:
     * <ol>
     *     <li>If you are the lock owner, you may always remove the lock.</li>
     *     <li>If you are the superuser, you may always remove the lock.</li>
     *     <li>If you are not the lock owner and not a superuser,
     *     then this lock is none of your business (and you are not allowed to unlock it).</li>
     * </ol>
     * @param osd the OSD to unlock.
     */
    public void validateUnlock(ObjectSystemData osd) {
        UserAccount lockOwner = osd.locker;
        if (lockOwner == null) {
            throw new CinnamonException("error.object.not.locked");
        }
        else if (lockOwner.equals(user)) {
            // user may remove his own lock.
        }
        else if (user.verifySuperuserStatus()) {
            /*
             * owner is not null and not the user: this requires superuser status
             */
        }
        else {
            // owner is someone else and user is not superuser: forbidden.
            throw new CinnamonException(
                    String.format("Object %d can only be unlocked by the lock owner %s or a superuser.",
                            osd.getId(), lockOwner.getName())
            );
        }
    }

    public void validateVersion(ObjectSystemData osd) {
        Permission versionObject = fetchPermission(PermissionName.VERSION_OBJECT);
        validateAgainstAcl(osd, versionObject);
        if (osd.locker != null) throw new CinnamonException("Object " +
                osd.getId() + " must be unlocked for versioning.");

    }

    /**
     * Validate a Permission against an object's ACL. Do not use this if a more specific
     * test exists (for example, validateDelete).
     * @param osd The object that the user wishes to modify.
     * @param permission The Permission you need to test
     */
    public void validateAgainstAcl(ObjectSystemData osd, Permission permission) {
        if (osd == null) {
            throw new CinnamonException("error.object.not.found");
        }
        log.debug("looking up acl");
        cinnamon.Acl acl = osd.getAcl();
        if (acl == null) {
            throw new CinnamonException("error.acl.invalid");
        }

        if (check_acl_entries(acl, permission, osd)) {
            log.debug("check_acl_entries returned true.");
            return;
        }

        // no sufficient permission entry found
        throw new CinnamonException("error.missing.permission." + permission.getName());
    }

    public void validateFolderAgainstAcl(cinnamon.Folder folder, Permission permission) {
        // get acl id		
        log.debug("permission needed: " + permission.getName());
        if (folder == null) {
            throw new CinnamonException("error.folder.not.found");
        }

        if (user.verifySuperuserStatus()) {
            log.debug("Superusers are not subject to permissions ");
            return;
        }

        cinnamon.Acl acl = folder.getAcl();
        if (acl == null) {
            throw new CinnamonException("error.acl.not.found");
        }
        else {
            log.debug(String.format("found acl: %s (%s)", acl.getId(), acl.getName()));
        }

        log.debug("Looking up AclEntries");
        if (check_acl_entries(acl, permission, folder)) {
            return;
        }

        // no sufficient permission entry found
        throw new CinnamonException("error.missing.permission." + permission.getName());
    }

    public boolean check_acl_entries(cinnamon.Acl acl, Permission permission, Ownable ownable) {
        if (user.verifySuperuserStatus()) {
            log.debug("Superusers may do anything.");
            return true; // Superuses are exempt from all Permission checks.
        }
        log.debug("Looking up AclEntries");
        // create Union of Sets: user.groups and acl.groups => iterate over each group for permitlevel.

        // 2. query acl for usergroup.
        Set<AclEntry> direct_entries = new HashSet<AclEntry>();
        direct_entries.addAll(getAclEntriesByUser(acl, user));

        Set<AclEntry> aclentries = new HashSet<AclEntry>();

        log.debug("descending into groupMatches2");
        aclentries.addAll(getGroupMatches2(direct_entries, acl));
        aclentries.addAll(findAliasEntries(acl, user, ownable));

        log.debug("checking all aclentries for permission");
        // now browse all entries for the first one to permit the intended operation:
        log.debug("# of aclentries: " + aclentries.size());
        for (AclEntry entry : aclentries) {
            log.debug("check aclentry with id " + entry.getId() + " for acl " + entry.getAcl().getName()
                    + " and group " + entry.getGroup().getName());
            if (entry.findPermission(permission)) {
                log.debug("Found aclentry with required permission. id=" + entry.getId());
                return true;
            }
        }
        return false;
    }

    List<AclEntry> getAclEntriesByUser(cinnamon.Acl acl, UserAccount user) {
        return acl.getUserEntries(user)
    }

    Set<AclEntry> findAliasEntries(cinnamon.Acl acl, UserAccount user, Ownable ownable) {
        Set<AclEntry> aliasEntries = new HashSet<AclEntry>();

        /*
           * If the ACL has an AclEntry for "everyone", add its AE to the
           * set of AEs for permission checking.
           */
        for (AclEntry ae : acl.getAclEntries()) {
            CmnGroup g = ae.getGroup();
            if (g.getName().equals(CmnGroup.ALIAS_EVERYONE)) {
                aliasEntries.add(ae);
                log.debug("Found Everyone-CmnGroup");
            }
            else {
                log.debug("ACL does not have an EVERYONE-AclEntry.");
            }
        }

        if (ownable == null) {
            /* Without a valid object, the user cannot be its owner.
                */
            log.debug("object is not ownable");
            return aliasEntries;
        }

        // check Alias::Owner:
        /*
           * If the Acl has an AclEntry fron the "owner"-CmnGroup and the user
           * _is_ the owner of the object, the AE is added to the set of AEs. 
           */
        log.debug("checking owner");
        if (user.equals(ownable.getOwner())) {
            log.debug("user == owner");
            for (AclEntry ae : acl.getAclEntries()) {
                CmnGroup g = ae.getGroup();
                log.debug("group-name:" + g.getName());
                if (g.getName().equals(CmnGroup.ALIAS_OWNER)) {
                    aliasEntries.add(ae);
                    log.debug("UserAccount is the owner.");
                }
                else {
                    log.debug("UserAccount is not the owner");
                }
            }
        }

        return aliasEntries;
    }

    public void validatePermission(cinnamon.Acl acl, Permission permission) {
        if (!check_acl_entries(acl, permission, null)) {
            throw new CinnamonException("error.missing.permission." + permission.getName());
            // TODO: parameterize correctly
        }
    }

    public void validatePermission(cinnamon.Acl acl, String permissionName) {
        Permission permission = fetchPermission(permissionName);
        validatePermission(acl, permission);
    }

    Boolean containsOneOf(Map cmd, Object... alternatives) {
        Boolean found = false;
        for (Object test : alternatives) {
            if (cmd.containsKey(test)) {
                found = true;
                break;
            }
        }
        return found;
    }

    public void validateUpdateFolder(Map<String, String> cmd, cinnamon.Folder folder) {
        if (user.verifySuperuserStatus()) {
            return;    // superusers are not subject to permissions
        }

        if (cmd.containsKey("parentid")) {
            Permission p = fetchPermission(PermissionName.MOVE);
            validateFolderAgainstAcl(folder, p);
            cinnamon.Folder parentFolder = Folder.get(cmd.get("parentid"));
            if (parentFolder == null) {
                // TODO: parametrize correctly
                throw new CinnamonException("error.parent_folder.not_found");
//				throw new CinnamonException( "Parentfolder with id "+value+" was not found.");
            }
            validateCreateFolder(parentFolder);
        }
        if (cmd.containsKey("aclid")) {
            Permission p = fetchPermission(PermissionName.SET_ACL);
            validateFolderAgainstAcl(folder, p);
        }
        if (containsOneOf(cmd, "name", "metadata", "ownerid", "typeid", "summary")) {
            Permission p = fetchPermission(PermissionName.EDIT_FOLDER);
            validateFolderAgainstAcl(folder, p);
        }
    }

    /**
     * @see cinnamon.index.ResultValidator#validateAccessPermissions(org.apache.lucene.document.Document, Class)
     */
    public XmlConvertable validateAccessPermissions(Document doc, Class<? extends Indexable> filterClass) {
//		log.debug("start::validateAccessPermissions");
        String javaClass = doc.get("javaClass");
        String hibernateId = doc.get("hibernateId");
//		log.debug(String.format("filterClass: %s - javaClass: %s - hibernateId: %s", 
//				filterClass, javaClass, hibernateId));
        try {
            if (javaClass.equals(SearchableDomain.OSD.name)) {
                log.debug("load OSD from database");
                ObjectSystemData osd = ObjectSystemData.get(hibernateId);
                log.debug("...done");
                if (osd != null) {
                    Permission permission = fetchPermission(PermissionName.BROWSE_OBJECT);
//				    log.debug("validatePermission");
                    validatePermission(osd.getAcl(), permission);
                    if (filterByClass(osd, filterClass)) {
                        return osd;
                    }
                }
                else {
                    log.debug("Object with id " + hibernateId + " was not found.");
                }
            }
            else if (javaClass.equals(SearchableDomain.FOLDER.name)) {
                cinnamon.Folder folder = Folder.get(hibernateId);
                if (folder != null) {
                    Permission permission = fetchPermission(PermissionName.BROWSE_FOLDER);
                    validatePermission(folder.getAcl(), permission);
                    if (filterByClass(folder, filterClass)) {
                        return folder;
                    }
                }
                else {
                    log.debug("Folder with id " + hibernateId + " was not found.");
                }
            }
            else {
                log.debug("validateAccessPermissions does not know how to verify access to '" + javaClass + "'");
            }
        }
        catch (Exception e) {
            // skip stacktrace:
            log.debug("validateSearchResults: " + e.message)
        }
        return null;
    }

    /**
     * @param indexable an object returned by a search.
     * @param filterClass the class by which the object will be filtered. May be null.
     * @return true if the indexable's class is equal to the filterClass or null.
     */
    public Boolean filterByClass(Indexable indexable, Class<? extends Indexable> filterClass) {
        if (filterClass == null) {
            return true;
        }
        if (indexable == null) { // should not happen.
            return false;
        }
        return indexable.getClass().equals(filterClass);
    }

    public void checkBrowsePermission(ObjectSystemData osd) {
        Permission browseObject = fetchPermission(PermissionName.BROWSE_OBJECT);
        checkBrowsePermission(osd, browseObject);
    }

    public void checkBrowsePermission(ObjectSystemData osd, Permission browseObject) {
        if (!check_acl_entries(osd.getAcl(), browseObject, osd)) {
            throw new CinnamonException("error.missing.permission._browse");
        }
    }

    protected Permission fetchPermission(String permissionName) {
        if (permissionCache.containsKey(permissionName)) {
            return permissionCache.get(permissionName);
        }
        else {
            Permission permission = Permission.findByName(permissionName);
            permissionCache.put(permissionName, permission);
            return permission;
        }
    }

    public Set<AclEntry> getGroupMatches2(Set<AclEntry> direct_entries, Acl acl) {
        Set<AclEntry> aclentries = new HashSet<AclEntry>();
        for (AclEntry ae : direct_entries) {
            if (!aclentries.add(ae)) {
                continue;
            }

            CmnGroup parent = ae.getGroup().getParent();
            while (parent != null) {
                // look if the parent has a relevant aclentry for this acl:
                AclEntry a = AclEntry.find("from AclEntry ae where ae.group=:parent and acl=:acl", [group: parent, acl: acl])
                if (!aclentries.add(a)) {
                    break; // break circular parent-child-parent relations.
                }
                // continue with the parent's parent:
                parent = parent.getParent();
            }
        }
        return aclentries;
    }
}

package cinnamon

/**
 *
 */
class AclEntryService {

    /**
     * Add a permission to an AclEntry.
     * Ensures that permission and aclEntry both reference the new AclEntryPermission
     * @param ae The AclEntry
     * @param permission
     * @return the new AclEntryPermission
     */
    AclEntryPermission addAclEntryPermission(AclEntry ae, Permission permission) {
        AclEntryPermission aep = new AclEntryPermission(ae, permission);
        ae.aePermissions.add(aep)
        permission.aePermissions.add(aep)
        aep.save(flush: true)
        return aep
    }

    /**
     * Remove the link between an AclEntry and a Permission and update each object's
     * set of AclEntryPermissions.
     * @param ae the affected AclEntry
     * @param permission the Permission of this AclEntryPermission
     */
    void removeAclEntryPermission(AclEntry ae, Permission permission) {
        AclEntryPermission aep = AclEntryPermission.findByPermissionAndAclEntry(permission, ae)
        if (ae) {
            ae.aePermissions.remove(aep)
            permission.aePermissions.remove(aep)
            aep.delete()
        }
    }

    /**
     * Return those groups which are not already connected to the ACL by an AclEntry.
     * @param acl the acl whose groups are to be excluded
     * @return list of groups which have no association with the given ACL.
     */
    List<CmnGroup> fetchFreeGroups(Acl acl) {
        List<CmnGroup> groups = CmnGroup.list()
        def aclGroups = acl.aclEntries.collect {it.group}
        groups.removeAll(aclGroups)
        return groups
    }

    void fullDelete(AclEntry aclEntry) {
        def aePerms = aclEntry.aePermissions.collect{it}
        aePerms.each {aePermission ->
            aePermission.permission.aePermissions.remove aePermission
            aePermission.aclEntry.aePermissions.remove aePermission
            aePermission.delete()
        }
        aclEntry.acl.aclEntries.remove aclEntry
        aclEntry.group.aclEntries.remove aclEntry
        aclEntry.delete()
    }

}

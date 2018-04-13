package cinnamon;

import cinnamon.global.Constants;
import cinnamon.global.PermissionName;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * A utility class to filter OSDs from search results for whom the user does not have browse permissions.
 */
public class BrowseAcls {

    static Logger log = Logger.getLogger(BrowseAcls.class);

    private static List<Acl> acls;
    private static Permission browsePermission;
    private Set<Long> objectAclsWithBrowsePermissions;
    private Set<Long> ownerAclsWithBrowsePermissions;
    private static final Map<Long, Set<Long>> userAclsWithBrowsePermissionCache = new ConcurrentHashMap<>();
    private static final Map<Long, Set<Long>> ownerAclsWithBrowsePermissionCache = new ConcurrentHashMap<>();

    private static Object INITIALIZING = new Object();
    private static Boolean initialized = false;

    private BrowseAcls(UserAccount user) {
        objectAclsWithBrowsePermissions = getUserAclsWithBrowsePermissions(user);
        ownerAclsWithBrowsePermissions = getOwnerAclsWithBrowsePermissions(user);
    }

    public static BrowseAcls getInstance(UserAccount user) {
        if (!initialized) {
            synchronized (INITIALIZING) {
                if (!initialized) {
                    initialize();
                }
            }
        }
        return new BrowseAcls(user);
    }

    public boolean hasUserBrowsePermission(Long aclId) {
        return objectAclsWithBrowsePermissions.contains(aclId);
    }

    public boolean hasOwnerBrowsePermission(Long aclId) {
        return ownerAclsWithBrowsePermissions.contains(aclId);
    }

    public static void reload() {
        synchronized (INITIALIZING) {
            initialized = false;
        }
    }
    
    public static void reloadUser(UserAccount user){
        synchronized (userAclsWithBrowsePermissionCache){
            userAclsWithBrowsePermissionCache.remove(user.getId());
        }
        synchronized (ownerAclsWithBrowsePermissionCache){
            ownerAclsWithBrowsePermissionCache.remove(user.getId());
        }
    }
    
    static void initialize() {
        synchronized (INITIALIZING) {
            browsePermission = (Permission) Permission.find("from Permission p where name='" + PermissionName.BROWSE_OBJECT + "'");
            acls = Acl.findAll();
        }

    }

    static Set<Long> getUserAclsWithBrowsePermissions(UserAccount user) {
        Long userId = user.getId();
        if (!userAclsWithBrowsePermissionCache.containsKey(userId)) {
            synchronized (userAclsWithBrowsePermissionCache) {
                long startTime = System.currentTimeMillis();
                log.info("Generating object acls list with browse permissions for user " + user);
                userAclsWithBrowsePermissionCache.put(userId, generateObjectAclSet(browsePermission, user));
                long endTime = System.currentTimeMillis();
                log.info("object acl list generated in "+ (endTime-startTime) + " ms" );
            }
        }
        return userAclsWithBrowsePermissionCache.get(userId);
    }
    
    static Set<Long> getOwnerAclsWithBrowsePermissions(UserAccount user){
        Long userId = user.getId();
        if (!ownerAclsWithBrowsePermissionCache.containsKey(userId)) {
            synchronized (ownerAclsWithBrowsePermissionCache) {
                long startTime = System.currentTimeMillis();
                log.info("Generating owner acls list with browse permissions for user " + user);
                ownerAclsWithBrowsePermissionCache.put(userId, generateOwnerAclIdSet(browsePermission, user));
                long endTime = System.currentTimeMillis();
                log.info("owner acl list generated in "+ (endTime-startTime) + " ms" );

            }
        }
        return ownerAclsWithBrowsePermissionCache.get(userId);
    }

    static private Set<Long> generateObjectAclSet(Permission permission, UserAccount user) {
        if (user.verifySuperuserStatus()) {
            // Superusers are exempt from permission checking, so they automatically have BrowsePermission on all objects.
            return acls.stream().map(Acl::getId).collect(Collectors.toSet());
        }
        Set<Long> aclIds = new HashSet<>();

        for (Acl acl : acls) {
            // compute browse permissions for acls
            boolean checkAclResult = checkObjectAclEntries(acl, permission, user);
            if (checkAclResult) {
                aclIds.add(acl.getId());
            }

        }
        return aclIds;
    }
    
    static boolean checkObjectAclEntries(Acl acl, Permission permission, UserAccount user) {
        // create Union of Sets: user.groups and acl.groups => iterate over each group for permitlevel.

        // 2. query acl for usergroup.
        Set<AclEntry> directEntries = new HashSet<>();
        directEntries.addAll(acl.getUserEntries(user));
        Set<AclEntry> aclEntries = new HashSet<>();

        aclEntries.addAll(getGroupMatches2(directEntries, acl));

        Optional<AclEntry> everyoneAclEntry = acl.getAclEntries().stream().filter(aclEntry -> aclEntry.getGroup().getName().equals(CmnGroup.ALIAS_EVERYONE)).findFirst();
        everyoneAclEntry.ifPresent(aclEntries::add);

        // now browse all entries for the first one to permit the intended operation:
        for (AclEntry entry : aclEntries) {
            if (entry.findPermission(permission)) {
                return true;
            }
        }
        return false;
    }

    static private Set<Long> generateOwnerAclIdSet(Permission permission, UserAccount user) {
        if (user.verifySuperuserStatus()) {
            // Superusers are exempt from permission checking, so they automatically have BrowsePermission on all objects.
            return acls.stream().map(Acl::getId).collect(Collectors.toSet());
        }
         
        Map<String,String> params = new HashMap<>();
        params.put("name",Constants.ALIAS_OWNER);
        List<AclEntry> ownerEntries = AclEntry.findAll("from AclEntry ae where ae.group.name=:name",params);
        Set<Long> ownerAclIdsWithBrowsePermission = new HashSet<>();
        for(AclEntry entry : ownerEntries){
            if(entry.findPermission(permission)){
                ownerAclIdsWithBrowsePermission.add(entry.getAcl().id);
            }
        }
        return ownerAclIdsWithBrowsePermission;
    }


    static Set<AclEntry> getGroupMatches2(Set<AclEntry> directEntries, Acl acl) {
        Set<AclEntry> aclentries = new HashSet<>();
        Map<String,Long> queryParams = new HashMap<>();
        
        for (AclEntry ae : directEntries) {
            if (!aclentries.add(ae)) {
                continue;
            }

            CmnGroup parent = ae.getGroup().getParent();
            while (parent != null) {
                // look if the parent has a relevant aclentry for this acl:
                queryParams.put("parentId",parent.getId());
                queryParams.put("aclId",acl.getId());
                AclEntry a = (AclEntry) AclEntry.find("from AclEntry ae where ae.group.id=:parentId and acl.id=:aclId", queryParams);
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

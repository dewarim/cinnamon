# ACL API

## listAclEntries

Retrieve a list of all AclEntries for a group _or_ ACL.
An AclEntry consists of a group and an ACL object. It may be connected to any number
of permissions. By using this API call you will get a list of all
ACLs for a group or all groups for an ACL, along with the permissions for each
combination. 

A client programm may use this method to determine if it should offer a user
certain menu options like "delete object": 

* retrieve information about an OSD's or folder's ACL
* get all groups for a user (getGroupsOfUser)
* call listAclEntries for the ACL
* check if there is any match between the user's groups, the ACL and the required DELETE_OBJECT permission.

## listAclMembers

Retrieve a list of all ACLs a user is a member of.

## createAcl

Create a new ACL.

## deleteAcl

Delete an ACL. You can only delete ACLs that are not in use by folders or objects.

## editAcl

Update an ACL's name or description. The name must be unique.

## addGroupToAcl

Create an AclEntry of a group and an ACL, so you can assign permissions to it later on.
For example, if you got an ACL 'public_documents' and a group 'authors', you can add the
group to the ACL and then assign permissions to allow members of the authors group to view 
and edit all objects which use the public_documents ACL.

## removeGroupFromAcl

Unlink a group to ACL connection by removing their AclEntry. Note: groups can inherit 
permissions from their parent group(s). 

## addPermissionToAclEntry

Add a permission to an AclEntry. After creating a link between an ACL and a group (via
addGroupToAcl), you need to assign permissions to the new AclEntry, as an entry without
permissions does not allow any interaction between a group member and the objects to
which the ACL is assigned.

Note that the permission model uses a whitelist approach: In Cinnamon, you assign positive
permissions like a "CREATE_OBJECT"-permission to users of a certain group and the associated ACL(s).
A user may be a member of any number of groups and can gain a permission from any one of them.

## removePermissionFromAclEntry

Remove a permission from an AclEntry.




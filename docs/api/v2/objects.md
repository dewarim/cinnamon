# Objects 

Object in Cinnamon are instances of the class ObjectSystemData (short: OSD) and they always have

* an id (which is a long integer value, unique for this repository)
* a name
* an creator, owner and last modifier (along with timestamp)
* an object type
* an ACL
* a parent folder which contains this object
* a version label
* a processing state (a new object has "_created" as its state)

and they may have

* content (which is stored in the file system)
* a format (if content exists, a mime type based format has to be assigned)
* a lifecycle state
* relations to other objects
* metasets (that is custom metadata of different types in XML format)
* an appname field which defines the application responsible for this object
* a lock entry - if the object is locked by a user, this field contains the reference to the lock owner.

## version labels

Versions start with 1 and continue up in whole numbers for the main trunk of the version tree.
If a side branch is added, it will be as n-x.v, where n is the trunk version and x is the branch number and v is the branch version.
So, if you got a version tree of

    firstVersion  1
    secondVersion 2
    
And go on to create a new version of firstVersion, the tree will look like this:

    firstVersion     1
        newBranch    1.1-1
    secondVersion    2
    
Creating a new version of the branch will result in 

    firstVersion     1
        newBranch    1.1-1
        newBranch2   1.1-2
    secondVersion    2
    
## api methods

### create

Create a new object.

### copy

Create a copy of a Cinnamon object (OSD) in the target folder. The name of the object will be "Copy_" + the actual name.
The copy will be version number 1 and have the same content and relations as the original. The Lifecycle state of the
object is set to whatever the default lifecycle state for a copy of the source object's lifecycle is. 
You can specify a list of metasets to selectively copy parts (or all) of the source's metasets.

### delete

Delete an object. Note that this method will fail if the object has protected relations to other objects.
For example, this prevents a user from removing an image that is part of a document.
You can only delete an object without descendants (newer versions). 

### deleteAllVersions

Recursively deletes all versions of an object, starting with the newest one.
Known problem: If you get a Cinnamon exception
from this method, please re-index the objects of this version tree by setting index_ok of
the OSDs to null (needs enabled IndexServer).

### getContent

Returns the content of the OSD as a byte-stream.

### setContent

Set the content of an object. Requires the object to be locked by the current user.

### getObject

Returns the XML representation of the object.

### getObjects

The getobjects command retrieves some or all objects in the folder with the given id.
The optional versions parameter allows requesting all versions (all),
only the newest version in the trunk of the version tree (head) or the newest
version including branches (branch).

### getOBjectsById

The getObjectsById command retrieves one or more objects by the given id.

### getObjectsWithCustomMetadata

The getobjectswithmetadata command retrieves some or all objects in the folder with the given id
and returns their metadata and system metadata.
The optional versions parameter allows requesting all versions (all),
only the newest version in the trunk of the version tree (head) or the newest
version including branches (branch).

### lock

Locks an object, so its content cannot be overwritten by another user while the current
user is working on it.

### unlock

Release the lock on an object.

### searchObjects

Search for objects with the integrated Lucene library. Send an XML query to the server
and you will recceive a list of objects found. You may request pages of results to
prevent the server from returning very large sets of objects.

An example of a Lucene XML query which returns objects containing the word 'apples' or
'oranges':

    <BooleanQuery fieldname="content">
	<Clause occurs="should">
	    <TermQuery>apples</TermQuery>
	</Clause>
        <Clause occurs="should">
	    <TermQuery>oranges</TermQuery>
	</Clause>
    </BooleanQuery>


### setMeta

Set the custom metadata of an object. This consists of a list of metasets,
which will be extracted and stored separately. If you set the custom metadata 
this way, any metasets no longer specified in this document will be 
unlinked from the object.

Example of a copyright and link metaset:

    <meta>
        <metaset type="copyright">
	    <license>Apache License 2.0</license>
	</metaset>
	<metaset type="links">
	    <source>
	        <url>http://wikipedia.org</url>
            </source>
       </metaset>
    </meta>

### getMeta

Returns all metasets of the current object, collated into one XML document.

### setSysMeta

Set the system properties of an object. You can change the following properties via
this method:

* parentid: move the object to another folder
* name: rename the object
* owner: change the owner
* procstate: change the processing state. Currently used by workflow tasks
  to denote ready/finished state.
* acl_id: change the ACL
* objtype: change the object type
* appname: the appname field may contain the name of a programm associated with
  this object. This is only useful if your Cinnamon client is setup for the use of
  applications.
* language_id: set the language of an object. The default is "undetermined", but you
  can also declare an object as having no or multiple languages - or a specific one.

### version

Create a new version of an object. See above for more details about version labels..

## relations

An object may have relations to other objects. The effects of a relations are:

* an object may be protected (depending on the relation type), so that a CSS file
  belonging to an HTML document cannot be deleted for as long as the relation exists.
* upon checkout of an object, the server can bundle up all the related objects for
  the client. And, again depending on the relation type, the server will determine
  if the client needs the most recent or a specific version of a resource. 
  This enables you to have a marketing document always refer to the newest version of the
  company logo - or a legal document always refer to the copyright notice exactly as it were
  at the time the relation was first created.
* the client can display relations between objects, so you know all the places where
  a specific version of an object is used.
  
The two objects in a relation are defined as "left" and "right", so you will see some fields
labeld as "left..." and "right..." which pertain to the corresponding object.
  
### getRelationTypes

Retrieve a list of all available relation types. Relations have the following fields:

* name
* description
* left object protected: the left object of a relation of this type may not be
  deleted as long as the relation exists.
* right object protected: the right object of a relation of this type may not be
  deleted as long as the relation exists.
* clone on right copy: If the right object of a relation of this type is copied,
  the relation will also be copied if this field is true.
* clone on left copy: If the left object of a relation of this type is copied,
  the relation will also be copied if this field is true.
* left resolver: Determines the resolver class to use for the left object if one of the two objects is updated.
  After a part of a relation has been updated, all other relations which target this object's 
  version tree have to be updated also because the change to one object may require other relations to change.
  
  Example:
  An image, which is referenced by several relations from documents which
  use it, is updated and increases its version number. Now all relations which
  use the LatestHeadResolver need to update their link to this new version.  
* right resolver: determines the resolver class to use for the right object if the
  relation has to be updated after an object has changed.

### createRelation

Create a new relation between two objects. Relations may have XML metadata associated with them,
for example if you have checked out a DITA document which includes an image with a reference, the client
may store the actual filename in the metadata to ensure the correct mapping between both files.

### deleteRelation

Remove a relation.

## metasets

You may handle metadata either by using the setMeta/getMeta methods, or you can use the
new methods for handling individual metasets.

When new metaset data is set on an object (folder or OSD), you may specify the write policy.
Allowed values are write|ignore|branch, default is branch.

* write: the content is written regardless of other items linking to this metaset.
  In our example of a copyright text for all images of an author, this is probably okay.
  In case of a list of applicable machine parts for a handbook, you may want to be more
  careful before having one author change the applicability of all objects linked to this
  metaset.
* ignore: the content is ignored if there are other references to the metaset.
* branch: if other references exist, a separate metaset for this item is created. This is
  the default setting and while it leads to duplication of data, it prevents authors
  from accidentally overwriting other author's metaset data.

The decision on which write policy to use should be made by the client program, depending 
on the object type and the business rules.

### listMetasetTypes

Retrieve a list of all available metaset types. The server only allows clients to create
metasets with an existing type.

### linkMetaset

Creates a link to an existing metaset. This way you can link multiple objects to one
metaset, for example link all images by one author to the author's copyright notice.

### unlinkMetaset

Remove the link between an object and a metaset. If the metaset is no longer linked to
any objects, it will be deleted.

### setMetaset

Set the XML content of an object's metaset entry. The type of the metaset must exist in
the metaset type table.

### getMetaset

Retrieve the XML content of a metaset entry. Currently, you cannot set / get metaset
directly, you have to specify an object which is linked to this metaset and request
it by name. So if you have a repair handbook document, which is linked to a metaset containing more
information about the machine it describes, you can request the metaset via the document,
but you cannot access the metaset without. This restriction is in place because read/write
access to metasets is determined by the ACLs of the objects linking there. (If you are afraid of
users modifiying metasets of forbidden objects because they have write access to other
objects, you should use a write policy of 'branch' instead of 'write' (see above).

### deleteMetaset

Delete an object's metaset. You can set an optional delete_policy with a value of
either 'complete' or 'allowed'. This method will first try to delete all references to the metaset. 
Each connected item's ACL is checked, if the reference may  be safely deleted. 
If delete policy is set to 'complete', an exception is thrown if the process encounters
a non-accessible object. With delete policy 'allowed', the process continues, trying to delete
the other references. If the metaset is no longer used, it will be deleted completely.

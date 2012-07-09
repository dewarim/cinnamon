# Folders

This is a high-level overview of an element of the Cinnamon server API. 
Please refer to the JavaDocs for more details like the exact parameter and options list.

## copyFolder

Copy a folder and its content to a target folder. You can specify the versions (all, latest branch, latest head)
and whether the server should stop on error or continue to copy as many objects as possible. 
This method returns a list of ids of newly created folders and objects (xml error message in case of failure).

## createFolder

Create a new folder. The folder name must be unique for the same parent. 
To create a subfolder of root, specify 0 as parent id. 
The id of the newly created folder is returned.

## deleteFolder

Delete an empty folder.

## getFolder

Returns a folder with a given id and its parent folders.

## getFolderByPath

Returns a folder with a given path - useful if you do not have the folder's id at hand.

## getFoldersById

Fetch several folders at once by submitting a list of ids.

## getSubfolders

Returns a list of the folders inside a target folder. Does not recurse deeper.

## getFolderMeta

Returns a folder's custom metadata. This is an XML document collating all XML metaset
objects which are linked to this folder.

## updateFolder

Udpate a folder's system properties. You can change:

* parent: moving the folder to another place.
* name: rename folder
* owner: the owner of this folder
* metadata: set the custom metadata
* type: the folder type

## getFolderTypes

Returns the available folder types as an XML document. A folder type has the properties:

* name: the translated / localized name (or, if no localization exists, the sysName)
* sysName: the name of the folder without any localization
* description: a short description of the folder type.
* config: XML configuration (only available since Cinnamon 3), may include information
  about how to display a specific folder type.

## searchFolders

Search for folders using an XML Lucene query. Will return a list of all matching folders
along with their parents (which allows you to create tree views of the other navigational
elements).

An example of a Lucene XML query:

    <?xml version="1.0" encoding="UTF-8"?>
    <!-- Remember to lowercase the search terms. 
     Folder.name == testFolder / search term == testfolder -->
    <BooleanQuery>
        <Clause occurs="must">
	    <TermQuery fieldName="name">testfolder</TermQuery>
	</Clause>
    </BooleanQuery>

For more information about how to construct queries, see the xml-query-parser documentation
in the [Lucene  contrib package](http://lucene.apache.org/core/downloads.html), which is part
of the full download.

## zipFolder

Creates a zip archive of a repository folder. The following parameters modify the method's behavior:

* latest_head: include only the newest trunk version of each object.
* latest_branch: include only the latest branch versions of each objects.

You can also specify a target folder along with type and metadata parameter information and the
server will create an OSD object in the folder which contains the zip archive. 

# Changelog for Cinnamon 3

## 0.2.14

* Increased visibility of methods in DefaultIndexer (and child classes).  
  This enables other packages to inherit from the default indexer classes of Cinnamon.

## 0.2.13

* Fixed link to codeMirror-UI
* Removed double-encoding of content in several places.
* Try to translate format / type name in object lists.
* Fixed: Simple search field now searches on name and content field in Lucene index.
* Updated jaxen library.

## 0.2.3

* Changed/Fixed: setMetadata now deletes MetaSets that are no longer included in the metadata.
                 Previous behaviour was to silently keep those metasets. Now, setMetadata expects
                 you to supply the whole metadata (or use addMetaset-method for specific metasets).
* Improved: parameter handling in RelationTypeController.
 
## 0.2.2

* Fixed: create OSD object now works again.
* OsdController.newVersion will use the configured defaultTemplate for displaying the current folder's content.
* New: config property "defaultTemplate" which is used along with defaultController and defaultAction when
    rendering the content of a folder.
    Default values:
    
    * defaultController = 'folder'
    * defaultAction = 'fetchFolderContent'
    * defaultTemplate = '/folder/folderContent'
    
    By changing those values, or by configuring the elements "controller", "action" and "template" in a folder
    type's xml config field, you can choose which controller / action / template is used to render the content
    of a specific folder type. For example, you can have folder.type=image render an image gallery, while 
    folder.type=project list a projects folder in a separate way.
    
    If the template is not found at runtime, the default values are used. This way you can use the Cinnamon plugin
    to browse a customized repository, where the controllers / actions / templates all are located in a custom
    build war file. Previously, you could only browse such a repository by using the original (customized) application.

## 0.2.1

* Fixed: delete OSD no longer sets additional object to latestHead in cases where the deleted
         object was the last part of a branch. (This happened if deleted object was for example v1-1 in
         a version tree with v1, v2)
* Added missing administration features from Cinnamon2/Dandelion. 
  Cinnamon 3 now has all the administration features of Dandelion.

## 0.1.57

* Added Lifecycle-Log as special domain class. You will need to create a new table for it.

### MSSQL 2000

    if exists (select * from dbo.sysobjects where id = object_id(N'[dbo].[lifecycle_log]') and OBJECTPROPERTY(id, N'IsUserTable') = 1)
    drop table [dbo].[lifecycle_log]
    GO

    CREATE TABLE [dbo].[lifecycle_log] (
	[id] [bigint] IDENTITY (1, 1) NOT NULL ,
	[repository] [nvarchar] (255) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL ,
	[hibernate_id] [bigint] NOT NULL ,
	[user_name] [nvarchar] (255) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL ,
	[user_id] [bigint] NOT NULL ,
	[date_created] [timestamp] NOT NULL ,
	[lifecycle_id] [bigint] NOT NULL ,
	[lifecycle_name] [nvarchar] (255) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL ,
	[old_state_id] [bigint] NOT NULL ,
	[old_state_name] [nvarchar] (255) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL ,
	[new_state_id] [bigint] NOT NULL ,
	[new_state_name] [nvarchar] (255) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL ,
	[folder_path] [nvarchar] (4000) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL ,
	[name] [nvarchar] (255) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL 
    ) ON [PRIMARY]
    GO
    
### Postgres
    -- Table: lifecycle_log

    -- DROP TABLE lifecycle_log;

    CREATE TABLE lifecycle_log
    (
    id serial NOT NULL,
    repository character varying(255) NOT NULL,
    hibernate_id bigint NOT NULL,
    user_name character varying(255) NOT NULL,
    user_id bigint NOT NULL,
    date_created timestamp without time zone NOT NULL,
    lifecycle_id bigint NOT NULL,
    lifecycle_name character varying(255) NOT NULL,
    old_state_id bigint NOT NULL,
    old_state_name character varying(255) NOT NULL,
    new_state_id bigint NOT NULL,
    new_state_name character varying(255) NOT NULL,
    folder_path character varying(8191) NOT NULL,
    name character varying(255) NOT NULL,
    CONSTRAINT lifecycle_log_pkey PRIMARY KEY (id )
    )
    WITH (
    OIDS=FALSE
    );
    ALTER TABLE lifecycle_log
    OWNER TO postgres;
    GRANT ALL ON TABLE lifecycle_log TO postgres;
    GRANT SELECT, INSERT ON TABLE lifecycle_log TO cinnamon; 


## 0.1.56

* Moved code to create new OSD from controller to osdService for better reusability.
* Added description for some error message-ids.
* Target of "Home"-button is now configurable via config: defaultController / defaultAction parameters.
* Admin-Controller now loads custom admin links (if they are configured via config: adminController / adminAction)
    Example: 
        customAdminController = 'repository'
        customAdminAction = 'fetchAdminLinks'
    may load further administration links for a repository manager.

## 0.1.49

* fixed: you can now save changes to the admin's account.
* new: ImageService which creates Thumbnails and saves them to a metaset.

## 0.1.41

* made fetchLogoConfig depend on session instead of grailsApplication.config 
    (which would not work in multi-repository environments)
* added PageFilters to insert logo and headline info into request model, where necessary.

## 0.1.37

* added fetchLogoConfig to return the GPathResult of either parsing grailsApplication.config.logoConfig or 
    ConfigEntry.findByName('login.screen.config'). This method stores the parsed XML in a static field, so the
    controllers do not parse the xml config upon each request.
* changed fetchLogo so it uses fetchLogoConfig

## 0.1.36

* lock/unlock of OSD now works again in WebClient.
* added searchSimple action to FolderController (from Illicium)

## 0.1.35 LifecycleStateAuditLog

Added trigger class from Cinnamon 2 which can log lifecycle state
changes to a separate table or database.

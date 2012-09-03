# Changelog for Cinnamon 3

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

# Changelog for Cinnamon 3

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

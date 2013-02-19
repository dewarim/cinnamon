import cinnamon.ImageService

class CinnamonGrailsPlugin {

//    def packaging = "binary"
    def groupId = 'cinnamon'
    // the plugin version
    def version = "0.3.2.36"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "2.1 > *"
    // the other plugins this plugin depends on
    def dependsOn = [:]
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
            "grails-app/views/error.gsp"
    ]

    def title = "Cinnamon 3 CMS Plugin" // Headline display name of the plugin
    def author = "Ingo Wiarda"
    def authorEmail = "ingo.wiarda@horner-project.eu"
    def description = '''\
The Cinnamon ECMS plugin - adds a whole content management system to your application as Grails plugin,
allowing you to create custom apps based upon Cinnamon.
'''

    // URL to the plugin's documentation
    def documentation = "http://cinnamon-cms.de"

    // Extra (optional) plugin metadata

    // License: one of 'APACHE', 'GPL2', 'GPL3'
    def license = "LGPL 2.1"

    // Details of company behind the plugin (if there is one)
    def organization = [ name: "Horner GmbH", url: "http://horner-project.de/" ]

    // Any additional developers beyond the author specified above.
//    def developers = [ [ name: "Joe Bloggs", email: "joe@bloggs.net" ]]

    // Location of the plugin's issue tracker.
    def issueManagement = [ system: "github", url: "https://github.com/dewarim/cinnamon/issues" ]

    // Online location of the plugin's browseable source code.
    def scm = [ url: "https://github.com/dewarim/cinnamon" ]

    def doWithWebDescriptor = { xml ->
        // TODO Implement additions to web.xml (optional), this event occurs before
    }
    
    def doWithSpring = {
     
    }

    def doWithDynamicMethods = { ctx ->
        // TODO Implement registering dynamic methods to classes (optional)
    }

    def doWithApplicationContext = { applicationContext ->
        applicationContext.imageService.osdServiceBean = applicationContext.osdService
    }

    def onChange = { event ->
        // TODO Implement code that is executed when any artefact that this plugin is
        // watching is modified and reloaded. The event contains: event.source,
        // event.application, event.manager, event.ctx, and event.plugin.
    }

    def onConfigChange = { event ->
        // TODO Implement code that is executed when the project configuration changes.
        // The event is the same as for 'onChange'.
    }

    def onShutdown = { event ->
        // TODO Implement code that is executed when the application shuts down (optional)
    }
}

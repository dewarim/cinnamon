import cinnamon.Session

class CinnamonGrailsPlugin {

    def version = "3.8.2"
    def grailsVersion = "2.4 > *"
    def dependsOn = [:]
    def pluginExcludes = [
            "grails-app/views/error.gsp"
    ]

    def title = "Cinnamon 3 CMS Plugin" // Headline display name of the plugin
    def author = "Ingo Wiarda"
    def description = '''\
The Cinnamon ECMS plugin - adds a whole content management system to your application as Grails plugin,
allowing you to create custom apps based upon Cinnamon.
'''

    def documentation = "http://cinnamon-cms.com"
    def license = "LGPL 2.1"
    def developers = [ 
            [ name: "Ingo Wiarda", email: "ingo_wiarda@dewarim.de" ]
    ]

    def issueManagement = [ system: "github", url: "https://github.com/dewarim/cinnamon/issues" ]
    def scm = [ url: "https://github.com/dewarim/cinnamon" ]

    def doWithWebDescriptor = { xml ->
    }
    
    def doWithSpring = {
        
    }

    def doWithDynamicMethods = { ctx ->
    }

    def doWithApplicationContext = { applicationContext ->
        applicationContext.imageService.osdServiceBean = applicationContext.osdService
        Session.infoService = applicationContext.infoService
    }

    def onChange = { event ->
    }

    def onConfigChange = { event ->
    }

    def onShutdown = { event ->
    }
}

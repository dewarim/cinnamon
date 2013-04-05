class CinnamonGrailsPlugin {

    def version = "3.0.0.30"
    def grailsVersion = "2.2 > *"
    def dependsOn = [:]
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

    def documentation = "http://cinnamon-cms.de"
    def license = "LGPL 2.1"
    def organization = [ name: "Horner GmbH", url: "http://horner-project.de/" ]
    def developers = [ 
            [ name: "Ingo Wiarda", email: "ingo.wiarda@horner-project.eu" ]
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
    }

    def onChange = { event ->
    }

    def onConfigChange = { event ->
    }

    def onShutdown = { event ->
    }
}

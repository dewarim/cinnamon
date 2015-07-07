import grails.plugin.springsecurity.SecurityFilterPosition
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.util.Environment
import cinnamon.LifecycleLog

class BootStrap {

    def grailsApplication
    def luceneService
    def workflowService

    def init = { servletContext ->

        if (!grailsApplication.config.configLoaded) {
            def configFile = new File("${System.env.CINNAMON_HOME_DIR}/cinnamon-config.groovy")
            log.warn("merge config file by hand from ${configFile.absolutePath}")
            def configScript = new ConfigSlurper().parse(configFile.text)
            grailsApplication.config.merge(configScript)
        }

        try {
            def c = LifecycleLog.count()
            log.debug("*** lifecycle log count: $c")
        }
        catch (Exception e) {
            log.warn("*** dataSource for lifecycleLogging is probably not configured correctly.", e)
        }

        SpringSecurityUtils.clientRegisterFilter('requestTicketAuthenticationFilter',
                SecurityFilterPosition.PRE_AUTH_FILTER.getOrder() + 15)

        if (grails.util.Environment.currentEnvironment == Environment.TEST) {
            log.debug("Do not initialize luceneService in test environment.")
            return
        }

        luceneService.initialize()
        workflowService.initializeWorkflowMasters()

    }

    def destroy = {
        workflowService.stopWorkflowMasters()
        luceneService.stopLuceneMasters()
    }
}

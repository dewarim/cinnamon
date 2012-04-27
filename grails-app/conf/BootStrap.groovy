import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils
import org.codehaus.groovy.grails.plugins.springsecurity.SecurityFilterPosition

class BootStrap {

    def grailsApplication
    def luceneService
    
    def init = { servletContext ->


        if (!grailsApplication.config.configLoaded) {
            log.warn("merge config file by hand")
            def configFile = new File("${System.env.CINNAMON_HOME_DIR}/cinnamon-config.groovy")
            def configScript = new ConfigSlurper().parse(configFile.text)
            grailsApplication.config.merge(configScript)
        }

        SpringSecurityUtils.clientRegisterFilter('repositoryLoginFilter', 
                SecurityFilterPosition.PRE_AUTH_FILTER.getOrder() + 20 )

        luceneService.initialize()
    }
    
    def destroy = {
        
    }
}

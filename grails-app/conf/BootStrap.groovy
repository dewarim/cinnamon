import cinnamon.authentication.LdapConfig
import cinnamon.authentication.UnboundIdLdapConnector
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import grails.plugin.springsecurity.SecurityFilterPosition
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.util.Environment
import cinnamon.LifecycleLog

class BootStrap {

    def grailsApplication
    def luceneService

    def init = { servletContext ->

        if (!grailsApplication.config.configLoaded) {
            def configFile = new File("${System.env.CINNAMON_HOME_DIR}/cinnamon-config.groovy")
            log.warn("merge config file by hand from ${configFile.absolutePath}")
            def configScript = new ConfigSlurper().parse(configFile.text)
            grailsApplication.config.merge(configScript)
        }

        File ldapConfigFile = new File("${System.env.CINNAMON_HOME_DIR}/ldap-config.xml")
        try {
            if (ldapConfigFile.exists()) {
                ObjectMapper mapper = new XmlMapper();
                LdapConfig ldapConfig = mapper.readValue(ldapConfigFile, LdapConfig.class)
                StringWriter sw = new StringWriter();
                mapper.writerWithDefaultPrettyPrinter().writeValue(sw, ldapConfig)
                log.info("Using ldap-config.xml:\n" + sw)
                UnboundIdLdapConnector.config = ldapConfig
            } else {
                log.warn("${ldapConfigFile.absolutePath} does not exist. Using empty LDAP config.")
            }
        }
        catch (Exception e) {
            log.warn("Failed to load / parse the ldapConfig file at ${ldapConfigFile.absolutePath}.", e)
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

    }

    def destroy = {
//        luceneService.closeIndexes()
    }
}

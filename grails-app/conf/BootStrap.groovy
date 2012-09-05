import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils
import org.codehaus.groovy.grails.plugins.springsecurity.SecurityFilterPosition
import cinnamon.ObjectSystemData
import cinnamon.Folder
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

        try{
            def c = LifecycleLog.count()
            log.debug("*** lifecycle log count: $c")
        }
        catch (Exception e){
            log.warn("*** dataSource for lifecycleLogging is probably not configured correctly.", e)
        }
        
        SpringSecurityUtils.clientRegisterFilter('requestTicketAuthenticationFilter',
                SecurityFilterPosition.PRE_AUTH_FILTER.getOrder() + 15) 
        SpringSecurityUtils.clientRegisterFilter('repositoryLoginFilter',
                SecurityFilterPosition.PRE_AUTH_FILTER.getOrder() + 20)
        
        luceneService.initialize()

        // migrate legacy data:
        log.debug("migrating metadata to metasets")
        def osds = ObjectSystemData.executeQuery("select o.id, o.metadata from ObjectSystemData o where length(o.metadata) > 9")
        log.debug("found: ${osds.size()} OSDs")
        osds.each {row ->
            log.debug("convert OSD #${row[0]}")
            ObjectSystemData.withTransaction {
                def osd = ObjectSystemData.get(row[0])
                osd.setMetadata(row[1])
                osd.save()
            }
        }
        try {
            ObjectSystemData.executeUpdate("update ObjectSystemData o set o.metadata=:meta", [meta: '<meta/>'])
            def folders = Folder.executeQuery("select f.id, f.metadata from Folder f where length(f.metadata) > 9")
            log.debug("found: ${folders.size()} Folders")
            folders.each { row ->
                log.debug("convert Folder #${row[0]}")
                Folder.withTransaction {
                    def folder = Folder.get(row[0])
                    folder.setMetadata(row[1])
                    folder.save()
                }
            }
            Folder.executeUpdate("update Folder f set f.metadata=:meta ", [meta: '<meta/>'])
            log.debug("finished migration.")
        }
        catch (Throwable t) {
            log.error(t)
        }
    }

    def destroy = {

    }
}

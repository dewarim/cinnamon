package cinnamon.filters

import cinnamon.ConfigEntry
import cinnamon.ObjectSystemData
import groovy.util.slurpersupport.GPathResult

class PageFilters {
    
    protected GPathResult fetchLogoConfig(session) {        
        if (!session.logoConfigXml) {
            def logoConfig = ConfigEntry.findByName('login.screen.config')?.config
            if (logoConfig) {
                session.logoConfigXml = new XmlSlurper().parseText(logoConfig)
            }
        }
        return session.logoConfigXml
    }

    protected ObjectSystemData fetchLogo(session) {
        def logo = null
        def xml = fetchLogoConfig(session)
        if (xml) {
            logo = ObjectSystemData.get(xml.logoId?.text())
            log.debug("logo: $logo")
        }
        return logo
    }

    protected String fetchHeadline(session) {
        return fetchLogoConfig(session)?.headline?.text() ?: 'cinnamon.cms'
    }

    def filters = {
        all(controller: '*', action: '*', actionExclude: 'save*|update*|legacy', controllerExclude:'cinnamon') {
            before = {

            }

            after = { Map model ->
                model?.logo = fetchLogo(session)
                model?.headline = fetchHeadline(session)
                return true
            }

            afterView = { Exception e ->

            }
        }
    }
}

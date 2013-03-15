package cinnamon.test

import cinnamon.CinnamonController
import grails.test.mixin.*
import org.junit.*
import grails.plugin.spock.*
import spock.lang.* 

@TestFor(CinnamonController)
class CinnamonControllerSpec extends spock.lang.Specification {
    
    def "connect via connect method"(){
        setup:        
        params.user = 'admin'
        params.repository = 'demo'
        params.pwd = 'admin'
        params.machine = 'localhost'
        
        when:
        controller.connect()
        
        then:
        response.contentAsString.matches("<connection><ticket>.*</ticket></connection>")
        // problem: dataSource is not available in mockApplicationContext.
        
    }
}

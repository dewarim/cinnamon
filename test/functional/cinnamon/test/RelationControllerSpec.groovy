package cinnamon.test

import geb.spock.GebReportingSpec
import spock.lang.Stepwise

@Stepwise
class RelationControllerSpec extends GebReportingSpec {
    
    def "login to cinnamon"(){
        when:
        go "login/auth"
        def form = $('form')
        form.environment = 'demo'
        form.j_username = 'admin'
        form.j_password = 'admin'
        $('#loginSubmit').click()
        
        then:
        $('a.home').text() == 'Start'
    }
    
    def "listXml test"(){
        when:
        go "relation/listXml"
        
        then:
        driver.pageSource.contains('<relations/>')
    }  
    
}

package cinnamon.test

import cinnamon.test.pages.relation.CreatePage
import geb.spock.GebReportingSpec
import spock.lang.Stepwise

@Stepwise
class RelationControllerSpec extends GebReportingSpec {
    
    def "login to cinnamon"(){
        when:
        go "login/auth"
        
        then:
        $('a.home').text() == 'Start'
    }                
    
    def "listXml test"(){
        when:
        go "relation/listXml"
        
        then:
        driver.pageSource.contains('<relations/>')
    }  
    
    // TODO: implement relation-CRUD test.
    def "create a relation"(){
        to CreatePage
        // Note: the createPage returns an ajax response and requires a valid Cinnamon object. 
        
    }
}

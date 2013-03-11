package cinnamon.test

import cinnamon.test.pages.auth.LoginPage
import cinnamon.test.pages.folder.IndexPage
import geb.spock.GebReportingSpec
import spock.lang.Stepwise

/**
 * Test for the main controller.
 */
@Stepwise
class FolderControllerSpec extends GebReportingSpec{

    def "not logged in"(){
        when:
        to IndexPage
        
        then:
        ! authMod.isLoggedIn()
    }
    
    def "login to Cinnamon"(){
        when:
        to LoginPage
        authMod.login()

        then:
        $('a.home').text() == 'Start'
    }
    
    // TODO: interact with folder pages.
    
    def "logout from Cinnamon"(){
        when:
        authMod.logout()
        
        then:
        ! authMod.isLoggedIn()
    }
}

package cinnamon.test.pages.auth

import cinnamon.test.modules.AuthModule
import geb.Page

/**
 * 
 */
class LoginPage extends Page{
    
    static url = 'login/auth'
    
    static at = {
        $('h2').text() == 'Login'
    }
    
    static content = {
        authMod {module AuthModule}
    }
}

package cinnamon.test.modules

import geb.Module

/**
 *
 */
class AuthModule extends Module {

    void login(String repository = 'demo', String username = "admin", String password = "admin") {
        if (isLoggedIn()) {
            throw new IllegalStateException('User is already logged in.')
        }
        form.environment = repository
        form.j_username = username
        form.j_password = password
        loginButton.click()
    }

    void logout() {
        if (!isLoggedIn()){
            throw new IllegalStateException("already logged out")
        }
        logoutLink.click()
    }

    static content = {
        form(required: false){$('form')}
        loginButton(required: false) { $('#loginSubmit') }
        logoutLink(required: false) { $( '.logout-link' ) }        
    }

    Boolean isLoggedIn() {
        logoutLink
    }
}

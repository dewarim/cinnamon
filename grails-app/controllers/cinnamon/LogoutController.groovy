package cinnamon

import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils

class LogoutController {

    def repositoryService
    def userService

	/**
	 * Index action. Redirects to the Spring security logout uri.
	 */
	def index = {
		// TODO  put any pre-logout code here
        UserAccount user = userService.user
        repositoryService.removeUserFromCache(user, session.repositoryName)

		redirect uri: SpringSecurityUtils.securityConfig.logout.filterProcessesUrl // '/j_spring_security_logout'
	}

    def info = {
        return [logoutMessage:params.logoutMessage?.encodeAsHTML()]
    }
}

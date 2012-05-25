package cinnamon

import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils

import grails.converters.JSON
import org.springframework.security.authentication.AccountExpiredException
import org.springframework.security.authentication.CredentialsExpiredException
import org.springframework.security.authentication.DisabledException
import org.springframework.security.authentication.LockedException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import humulus.Environment

class LoginController {

	/**
	 * Dependency injection for the authenticationTrustResolver.
	 */
	def authenticationTrustResolver

	/**
	 * Dependency injection for the springSecurityService.
	 */
	def springSecurityService

	/**
	 * Default action; redirects to 'defaultTargetUrl' if logged in, /login/auth otherwise.
	 */
	def index = {
		if (springSecurityService.isLoggedIn()) {
			redirect uri: SpringSecurityUtils.securityConfig.successHandler.defaultTargetUrl
		}
		else {
			redirect action: auth, params: params
		}
	}

	/**
	 * Show the login page.
	 */
	def auth = {

		def config = SpringSecurityUtils.securityConfig

		if (springSecurityService.isLoggedIn()) {
          log.debug("user is logged in, redirecting")
			redirect uri: config.successHandler.defaultTargetUrl
			return
		}
        if(params.logoutMessage){
            flash.message = message(code:params.logoutMessage.encodeAsHTML())
        }
		String postUrl = "${request.contextPath}${config.apf.filterProcessesUrl}"
        
        def logoConfig = ConfigEntry.findByName('login.screen.config')
        def logo = null
        def localAppName = "app.${grailsApplication.metadata['app.name']}"
        if (logoConfig){
            
            def xml = new XmlSlurper().parseText(logoConfig.config)
            logo = ObjectSystemData.get(xml.logoId?.text())
            localAppName = xml.name?.text()
        }
        
        
		return [postUrl: postUrl,
                logo:logo,
                localAppName:localAppName,
		        rememberMeParameter: config.rememberMe.parameter,
                repositories: Environment.list()
        ]
	}

	/**
	 * Show denied page.
	 */
	def denied = {
		if (springSecurityService.isLoggedIn() &&
				authenticationTrustResolver.isRememberMe(SecurityContextHolder.context?.authentication)) {
			// have cookie but the page is guarded with IS_AUTHENTICATED_FULLY
			redirect action: full, params: params
		}
	}

	/**
	 * Login page for users with a remember-me cookie but accessing a IS_AUTHENTICATED_FULLY page.
	 */
	def full = {
		def config = SpringSecurityUtils.securityConfig
		render view: 'auth', params: params,
			model: [hasCookie: authenticationTrustResolver.isRememberMe(SecurityContextHolder.context?.authentication),
			        postUrl: "${request.contextPath}${config.apf.filterProcessesUrl}"]
	}

	/**
	 * Callback after a failed login. Redirects to the auth page with a warning message.
	 */
	def authfail = {

		def username = session[UsernamePasswordAuthenticationFilter.SPRING_SECURITY_LAST_USERNAME_KEY]
		String msg = ''
		def exception = session[AbstractAuthenticationProcessingFilter.SPRING_SECURITY_LAST_EXCEPTION_KEY]
		if (exception) {
			if (exception instanceof AccountExpiredException) {
				msg = SpringSecurityUtils.securityConfig.errors.login.expired
			}
			else if (exception instanceof CredentialsExpiredException) {
				msg = SpringSecurityUtils.securityConfig.errors.login.passwordExpired
			}
			else if (exception instanceof DisabledException) {
				msg = SpringSecurityUtils.securityConfig.errors.login.disabled
			}
			else if (exception instanceof LockedException) {
				msg = SpringSecurityUtils.securityConfig.errors.login.locked
			}
			else {
				msg = SpringSecurityUtils.securityConfig.errors.login.fail
			}
		}

		if (springSecurityService.isAjax(request)) {
			render([error: msg] as JSON)
		}
		else {
			flash.message = msg
			redirect action: auth, params: params
		}
	}

	/**
	 * The Ajax success redirect url.
	 */
	def ajaxSuccess = {
		render([success: true, username: springSecurityService.authentication.name] as JSON)
	}

	/**
	 * The Ajax denied redirect url.
	 */
	def ajaxDenied = {
		render(status:503, text:message(code:'error.access.denied'))
	}

    def ajaxAuth = {
        log.debug("ajaxAuth called")
        render(status:401, template:'/shared/ajax_auth', model:[environment:params.envId])
    }

    def authAjax= {
        log.debug("authAjax called")
        render(status:401, template:'/shared/ajax_auth', model:[environment:params.envId])
    }

    def dummyPage = {

    }
}

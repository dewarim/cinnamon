package cinnamon

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.GrantedAuthorityImpl

import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils
import org.codehaus.groovy.grails.plugins.springsecurity.GormUserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.core.userdetails.UserDetails
import org.codehaus.groovy.grails.plugins.springsecurity.GrailsUser

import org.codehaus.groovy.grails.plugins.springsecurity.GrailsUserDetailsService

import org.codehaus.groovy.grails.plugins.springsecurity.SecurityRequestHolder as SRH

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.codehaus.groovy.grails.plugins.springsecurity.SecurityRequestHolder
import humulus.EnvironmentHolder

/*
 This class is based upon UserDetailsService from the Grails security plugin. 
 */
class CinnamonUserDetailsService implements GrailsUserDetailsService{

    /*
    Note: this service must be injected in spring/resources,
     it will not be automatically injected.
     */
    def repositoryService

    private Logger log = LoggerFactory.getLogger(this.class)

	/**
	 * Some Spring Security classes (e.g. RoleHierarchyVoter) expect at least one role, so
	 * we give a user with no granted roles this one which gets past that restriction but
	 * doesn't grant anything.
	 */
	static final List NO_ROLES = [new GrantedAuthorityImpl(SpringSecurityUtils.NO_ROLE)]

	/**
	 * {@inheritDoc}
	 * @see org.codehaus.groovy.grails.plugins.springsecurity.GrailsUserDetailsService #loadUserByUsername(
	 * 	java.lang.String, boolean)
	 */
	UserDetails loadUserByUsername(String username, boolean loadRoles) throws UsernameNotFoundException {
        log.debug("username:$username")
        loadUser(username, loadRoles)
	}

	/**
	 * {@inheritDoc}
	 * @see org.springframework.security.core.userdetails.UserDetailsService#loadUserByUsername(java.lang.String)
	 */
	UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("username:$username")
	    loadUserByUsername username, true
	}

	protected UserDetails loadUser(String username, boolean loadRoles) {
//        HttpServletRequest  request = SecurityRequestHolder.getRequest()
//        def params = request.parameterMap

		def user = loadUser(username)
		Collection<GrantedAuthority> authorities = loadAuthorities(user, loadRoles)
		createUserDetails user, authorities
	}

	protected loadUser(String username) {
        UserAccount user = UserAccount.findByName(username)

		if (!user) {
			log.warn "UserAccount not found: $username"
//            log.warn("UserAccount list:")
//            log.warn "${UserAccount.list()}"
			throw new UsernameNotFoundException('UserAccount not found', username)
		}
        else{
            log.debug("found user : $user")
        }

        return user
	}

	protected UserDetails createUserDetails(user, Collection<GrantedAuthority> authorities) {
        log.debug("create user details")
		String username = user.name
		String password = user.pwd

        // create a client which accesses Cinnamon over the official API.
        def env = EnvironmentHolder.getEnvironment()
        String repositoryName = env?.dbname
        String url = env?.cinnamonServerUrl

        // gain access to unencrypted password which we need to login remotely to the server:
        def params = SecurityRequestHolder.request.parameterMap
//        Client client = new Client(url, username, (String) params.get('j_password')[0], repositoryName)
//        repositoryService.addClient(client)

		boolean enabled = user.activated
		boolean accountExpired = user.accountExpired
		boolean accountLocked = user.accountLocked
		boolean passwordExpired = user.passwordExpired

		new GrailsUser(username, password, enabled, !accountExpired, !passwordExpired,
				!accountLocked, authorities, user.id)
	}

	protected Collection<GrantedAuthority> loadAuthorities(UserAccount user, boolean loadRoles) {
		if (!loadRoles) {
          log.debug("do not load roles")
			return []
		}
      log.debug("loading roles / groups")
        Collection<CmnGroupUser> gusers = CmnGroupUser.findAllByUser(user)
		def authorities = gusers.collect {
            new GrantedAuthorityImpl(it.group.name)
        }
		return authorities ?: NO_ROLES
	}

}

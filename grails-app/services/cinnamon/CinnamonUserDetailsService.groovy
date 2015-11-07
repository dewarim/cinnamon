package cinnamon

import cinnamon.global.ConfThreadLocal
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.userdetails.GrailsUser
import grails.plugin.springsecurity.userdetails.GrailsUserDetailsService
import grails.transaction.Transactional
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.GrantedAuthorityImpl

import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.core.userdetails.UserDetails

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/*
 This class is based upon UserDetailsService from the Grails security plugin. 
 */
@Transactional
class CinnamonUserDetailsService implements GrailsUserDetailsService {

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
     * @see grails.plugin.springsecurity.userdetails.GrailsUserDetailsService #loadUserByUsername(
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
        else {
            log.debug("found user : $user")
        }
        
        // store user in ThreadLocal so we can access the current user when doing contentChanged/metadataChanged checks.
        ConfThreadLocal.conf.currentUser = user
        return user
    }

    protected UserDetails createUserDetails(user, Collection<GrantedAuthority> authorities) {
        log.debug("create user details")
        String username = user.name
        String password = user.pwd

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
        def authorities = null
        CmnGroupUser.withTransaction {
            Collection<CmnGroupUser> gusers = CmnGroupUser.findAllByUserAccount(user)
            authorities = gusers.collect {
                new GrantedAuthorityImpl(it.cmnGroup.name)
            }
        }
        return authorities ?: NO_ROLES
    }

}

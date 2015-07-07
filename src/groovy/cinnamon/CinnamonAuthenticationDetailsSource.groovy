package cinnamon

import grails.plugin.springsecurity.SpringSecurityUtils
import org.springframework.security.core.authority.GrantedAuthoritiesContainer
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import javax.servlet.http.HttpServletRequest
import org.springframework.security.core.authority.GrantedAuthorityImpl

/**
 * The Cinnamon desktop client communicates with the server via XML messages which are authenticated by
 * a ticket (UUID String) that is generated upon login. 
 * The CADS is responsible for loading the user account and its roles corresponding to a given ticket.
 */
class CinnamonAuthenticationDetailsSource extends WebAuthenticationDetailsSource implements GrantedAuthoritiesContainer {
    
    def grailsApplication
    
    List<GrantedAuthority> grantedAuthorities
    
    /**
     * Provided so that subclasses can populate additional information.
     *
     * @param request that the authentication request was received from
     */
    protected void doPopulateAdditionalInformation(HttpServletRequest request) {
        String ticket = request.getParameter('ticket');        
        if (!ticket) {
            return
        }
        def cmnSession = Session.findByTicket(ticket)
        if (!cmnSession) {
            return            
        }
        grantedAuthorities = loadAuthorities(cmnSession.user, true)
    }


    /**
     * Some Spring Security classes (e.g. RoleHierarchyVoter) expect at least one role, so
     * we give a user with no granted roles this one which gets past that restriction but
     * doesn't grant anything.
     */
    static final List NO_ROLES = [new GrantedAuthorityImpl(SpringSecurityUtils.NO_ROLE)]
    
    protected Collection<GrantedAuthority> loadAuthorities(UserAccount user, boolean loadRoles) {
        if (!loadRoles) {
//            log.debug("do not load roles")
            return []
        }
//        log.debug("loading roles / groups")
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

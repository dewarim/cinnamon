package cinnamon

import cinnamon.global.ConfThreadLocal
import grails.transaction.Transactional
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.web.authentication.preauth.PreAuthenticatedCredentialsNotFoundException

@Transactional
class CinnamonPreAuthUserDetailsService implements AuthenticationUserDetailsService{
    
    def grailsApplication
    def cinnamonUserDetailsService
    
//    @Override
    UserDetails loadUserDetails(Authentication token) {
        String ticket = token.credentials
        if(! ticket){
            return null
        }
        
        def repositoryName = ticket.split('@')[1]
        def cmnSession = Session.findByTicket(ticket)
        if (!cmnSession) {
            throw new PreAuthenticatedCredentialsNotFoundException("Session not found for ticket '${ticket}'")
        }
        cmnSession.renewSession(ConfThreadLocal.getConf().getSessionExpirationTime(repositoryName));
        
        return cinnamonUserDetailsService.loadUser(cmnSession.username, true)
    }
}

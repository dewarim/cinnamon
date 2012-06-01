package cinnamon

import org.springframework.security.core.userdetails.AuthenticationUserDetailsService
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.Authentication
import humulus.Environment
import org.springframework.security.web.authentication.preauth.PreAuthenticatedCredentialsNotFoundException
import humulus.EnvironmentHolder
import cinnamon.global.ConfThreadLocal

class CinnamonPreAuthUserDetailsService implements AuthenticationUserDetailsService{
    
    def grailsApplication
    def cinnamonUserDetailsService
    
    @Override
    UserDetails loadUserDetails(Authentication token) {
        String ticket = token.credentials
        if(! ticket){
            return null
        }
        
        def repositoryName = ticket.split('@')[1]
        def env = Environment.list().find {it.dbName == repositoryName}
        if (!env) {
            throw new PreAuthenticatedCredentialsNotFoundException("Could not find environment '${repositoryName}'")
        }

        //test connection
        EnvironmentHolder.setEnvironment(env)
        def ds = grailsApplication.getMainContext().dataSource
        ds.getConnection()
        def cmnSession = Session.findByTicket(ticket)
        if (!cmnSession) {
            throw new PreAuthenticatedCredentialsNotFoundException("Session not found for ticket '${ticket}'")
        }
        cmnSession.renewSession(ConfThreadLocal.getConf().getSessionExpirationTime(repositoryName));
        
        return cinnamonUserDetailsService.loadUser(cmnSession.username, true)
    }
}

import org.springframework.jdbc.datasource.DriverManagerDataSource
import humulus.CinnamonPasswordEncoder

import cinnamon.CinnamonAuthenticationDetailsSource
import cinnamon.CinnamonPreAuthUserDetailsService
import cinnamon.CinnamonUserDetailsService
import cinnamon.RequestTicketAuthenticationFilter
import cinnamon.debug.PreAuthAuthProvider
import cinnamon.debug.ProviderManager
import org.springframework.security.authentication.dao.DaoAuthenticationProvider

// Place your Spring DSL code here
beans = {
    
    userDetailsService(CinnamonUserDetailsService){
        // looks like this service is not injected automatically:
        repositoryService = ref('repositoryService')
    }
    passwordEncoder(CinnamonPasswordEncoder)

    authenticationProvider(DaoAuthenticationProvider){
        userDetailsService = ref('userDetailsService')
        passwordEncoder = ref('passwordEncoder')
    }
    // TODO: do we really need to configure both authenticationProvider & daoAuthenticationProvider?
    daoAuthenticationProvider(DaoAuthenticationProvider){
        userDetailsService = ref('userDetailsService')
        passwordEncoder = ref('passwordEncoder')
    }

    preAuthenticatedUserDetailsService(CinnamonPreAuthUserDetailsService){
        grailsApplication = ref('grailsApplication')
        cinnamonUserDetailsService = ref('cinnamonUserDetailsService')
    }

//    preauthAuthProvider(PreAuthenticatedAuthenticationProvider){
    preauthAuthProvider(PreAuthAuthProvider){ // custom provider for debugging
        preAuthenticatedUserDetailsService = ref('preAuthenticatedUserDetailsService')
    }

//    authenticationManager(org.springframework.security.authentication.ProviderManager){
    authenticationManager(ProviderManager){ // custom providerManager for debugging
        providers = ref('authenticationProvider')
    }  
    
    cinnamonAuthenticationDetailsSource(CinnamonAuthenticationDetailsSource){
        grailsApplication = ref('grailsApplication')
    }
    
    requestTicketAuthenticationFilter(RequestTicketAuthenticationFilter){
        authenticationManager = ref('authenticationManager')
        authenticationDetailsSource = ref('cinnamonAuthenticationDetailsSource')
        customProvider = ref('preauthAuthProvider') // customProvider will be added to authenticationManager during afterPropertiesSet. 
    }
    

}

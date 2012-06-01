import org.springframework.jdbc.datasource.DriverManagerDataSource
import humulus.Environment
import humulus.SwitchableDataSource
import cinnamon.CinnamonUserDetailsService
import humulus.CinnamonPasswordEncoder
import humulus.RepositoryLoginFilter

import cinnamon.RequestTicketAuthenticationFilter

import cinnamon.CinnamonPreAuthUserDetailsService
import cinnamon.CinnamonAuthenticationDetailsSource

import cinnamon.debug.PreAuthAuthProvider
import cinnamon.debug.ProviderManager

// Place your Spring DSL code here
beans = {
    
//    objectTreeCopier(ObjectTreeCopier){
//        osdService = ref('OsdService')
//    }
//
//    relationChangeTrigger(RelationChangeTrigger){
//        relationService = ref('RelationService')
//    }

    parentDataSource(DriverManagerDataSource) { bean ->
        bean.'abstract' = true;
        username = "sa"
//    	pooled = true
    }

    Environment.list().each { env ->
        "${env.prefix}DataSource"(DriverManagerDataSource) { bean ->
            bean.parent = parentDataSource
            bean.scope = 'prototype'
            url = env.dbConnectionUrl
            log.debug("url = '$url'")
            driverClassName = env.driverClassName
            if (env.username) {
                username = env.username
            }
            if (env.password) {
                password = env.password
            }
            
        }
    }

    def dataSources = [:]
    Environment.list().each {env ->
        dataSources[env.id] = ref(env.prefix + 'DataSource')
        log.debug("dataSource: ${dataSources[env.id]}")
    }

    dataSource(SwitchableDataSource) {
        targetDataSources = dataSources
    }

    userDetailsService(CinnamonUserDetailsService){
        // looks like this service is not injected automatically:
        repositoryService = ref('repositoryService')
    }
    passwordEncoder(CinnamonPasswordEncoder)

    authenticationProvider(org.springframework.security.authentication.dao.DaoAuthenticationProvider){
        userDetailsService = ref('userDetailsService')
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
    
////    preAuthManager(org.springframework.security.authentication.ProviderManager){
//    preAuthManager(org.springframework.security.authentication.ProviderManager){
//        providers = ref('preauthAuthProvider')
//    }

    repositoryLoginFilter(RepositoryLoginFilter){
        authenticationManager = ref('authenticationManager')       
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

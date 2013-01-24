package cinnamon

import javax.servlet.http.HttpServletRequest;

import org.springframework.util.Assert
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter
import org.springframework.security.web.authentication.preauth.PreAuthenticatedCredentialsNotFoundException
import org.codehaus.groovy.grails.commons.GrailsApplication
import humulus.Environment
import humulus.EnvironmentHolder
import cinnamon.global.ConfThreadLocal
import javax.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken
import org.springframework.security.core.AuthenticationException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.FilterChain
import javax.servlet.ServletException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.GenericFilterBean
import org.springframework.beans.factory.InitializingBean
import org.springframework.context.ApplicationEventPublisherAware
import org.springframework.context.ApplicationEventPublisher
import org.springframework.security.authentication.AuthenticationDetailsSource
import org.springframework.security.authentication.AuthenticationManager
import javax.servlet.http.HttpSession
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent
import org.springframework.security.web.WebAttributes
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;

/**
 *
 * Based upon: org.springframework.security.web.authentication.preauth.RequestHeaderAuthenticationFilter 
 * Check if a ticket supplied by a client is valid and retrieve the username from it.
 */
class RequestTicketAuthenticationFilter extends GenericFilterBean implements
InitializingBean, ApplicationEventPublisherAware {

    private ApplicationEventPublisher eventPublisher = null;
    private AuthenticationDetailsSource authenticationDetailsSource = new CinnamonAuthenticationDetailsSource()
    private AuthenticationManager authenticationManager = null;
    private boolean continueFilterChainOnUnsuccessfulAuthentication = true;
    private boolean checkForPrincipalChanges;
    private boolean invalidateSessionOnPrincipalChange = true;
    def customProvider
    
    /**
     * Check whether all required properties have been set.
     */
    @Override
    public void afterPropertiesSet() {
        Assert.notNull(authenticationManager, "An AuthenticationManager must be set");
        def providers = authenticationManager.providers
        providers.add(customProvider)
        authenticationManager.providers = providers
    }

    def grailsApplication

    /**
     * Read and returns the header named by {@code principalRequestHeader} from the request.
     *
     * @throws org.springframework.security.web.authentication.preauth.PreAuthenticatedCredentialsNotFoundException if the header is missing and {@code exceptionIfHeaderMissing}
     *          is set to {@code true}.
     */
    protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
        try {
            String ticket = request.getParameter('ticket');
            if (!ticket) {
                logger.debug("Request does not contain ticket.")
//                throw new PreAuthenticatedCredentialsNotFoundException("No ticket in request")
                return null
            }
            logger.debug("Request contains ticket: $ticket")
            def repositoryName = ticket.split('@')[1]

            def env = Environment.list().find {it.dbName == repositoryName}
            if (!env) {
                throw new PreAuthenticatedCredentialsNotFoundException("Could not find environment '${repositoryName}'")
            }

            //test connection
            EnvironmentHolder.setEnvironment(env)
            Session.withTransaction {
                def cmnSession = Session.findByTicket(ticket)
                if (!cmnSession) {
                    throw new PreAuthenticatedCredentialsNotFoundException("Session not found for ticket '${ticket}'")
                }
                return cmnSession.username
            }
        }
        catch (Exception e) {
            logger.info("Failed to preauthenticate request:", e)
            throw e;
        }
    }

    /**
     * Try to authenticate a pre-authenticated user with Spring Security if the user has not yet been authenticated.
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
    throws IOException, ServletException {

        if (logger.isDebugEnabled()) {
            logger.debug("Checking secure context token: " + SecurityContextHolder.getContext().getAuthentication());
        }

        if (requiresAuthentication((HttpServletRequest) request)) {
            doAuthenticate((HttpServletRequest) request, (HttpServletResponse) response);
        }
        else{
            logger.debug("no auth required, going to auth anyway for legacy reasons.")
            doAuthenticate((HttpServletRequest) request, (HttpServletResponse) response);
        }

        chain.doFilter(request, response);
    }

    /**
     * Do the actual authentication for a pre-authenticated user.
     */
    private void doAuthenticate(HttpServletRequest request, HttpServletResponse response) {
        Authentication authResult

        Object principal = getPreAuthenticatedPrincipal(request);
        Object credentials = getPreAuthenticatedCredentials(request);

        if (principal == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("No pre-authenticated principal found in request");
            }

            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("preAuthenticatedPrincipal = " + principal + ", trying to authenticate");
        }

        try {
            PreAuthenticatedAuthenticationToken authRequest = new PreAuthenticatedAuthenticationToken(principal, credentials);
            authRequest.setDetails(authenticationDetailsSource.buildDetails(request));
//            logger.debug("authRequest: $authRequest")
//            logger.debug("details: ${authRequest.getDetails()}")
//            logger.debug("authenticationManager: ${authenticationManager}")
//            logger.debug("authenticationManagerProviders: ${authenticationManager.providers}")
//            logger.debug("isAssignable: "+PreAuthenticatedAuthenticationToken.class.isAssignableFrom(authRequest.class));
            
            authResult = authenticationManager.authenticate(authRequest);
            successfulAuthentication(request, response, authResult);
        } catch (AuthenticationException failed) {
            unsuccessfulAuthentication(request, response, failed);

            if (!continueFilterChainOnUnsuccessfulAuthentication) {
                throw failed;
            }
        }
    }

    private boolean requiresAuthentication(HttpServletRequest request) {
        Authentication currentUser = SecurityContextHolder.getContext().getAuthentication();

        if (currentUser == null) {
            return true;
        }

        Object principal = getPreAuthenticatedPrincipal(request);
        if (checkForPrincipalChanges &&
                !currentUser.getName().equals(principal)) {
            logger.debug("Pre-authenticated principal has changed to " + principal + " and will be reauthenticated");

            if (invalidateSessionOnPrincipalChange) {
                HttpSession session = request.getSession(false);

                if (session != null) {
                    logger.debug("Invalidating existing session");
                    session.invalidate();
                }
            }

            return true;
        }

        return false;
    }

    /**
     * Puts the <code>Authentication</code> instance returned by the
     * authentication manager into the secure context.
     */
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, Authentication authResult) {
        if (logger.isDebugEnabled()) {
            logger.debug("Authentication success: " + authResult);
        }
        SecurityContextHolder.getContext().setAuthentication(authResult);
        // Fire event
        if (this.eventPublisher != null) {
            eventPublisher.publishEvent(new InteractiveAuthenticationSuccessEvent(authResult, this.getClass()));
        }
    }

    /**
     * Ensures the authentication object in the secure context is set to null
     * when authentication fails.
     */
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) {
        SecurityContextHolder.clearContext();
        if (logger.isDebugEnabled()) {
            logger.debug("Cleared security context due to exception", failed);
        }
        request.getSession().setAttribute(WebAttributes.AUTHENTICATION_EXCEPTION, failed);
    }

    /**
     * @param anApplicationEventPublisher
     *            The ApplicationEventPublisher to use
     */
    public void setApplicationEventPublisher(ApplicationEventPublisher anApplicationEventPublisher) {
        this.eventPublisher = anApplicationEventPublisher;
    }

    /**
     * @param authenticationDetailsSource
     *            The AuthenticationDetailsSource to use
     */
    public void setAuthenticationDetailsSource(AuthenticationDetailsSource authenticationDetailsSource) {
        Assert.notNull(authenticationDetailsSource, "AuthenticationDetailsSource required");
        this.authenticationDetailsSource = authenticationDetailsSource;
    }

    /**
     * @param authenticationManager
     *            The AuthenticationManager to use
     */
    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    public void setContinueFilterChainOnUnsuccessfulAuthentication(boolean shouldContinue) {
        continueFilterChainOnUnsuccessfulAuthentication = shouldContinue;
    }

    /**
     * If set, the pre-authenticated principal will be checked on each request and compared
     * against the name of the current <tt>Authentication</tt> object. If a change is detected,
     * the user will be reauthenticated.
     *
     * @param checkForPrincipalChanges
     */
    public void setCheckForPrincipalChanges(boolean checkForPrincipalChanges) {
        this.checkForPrincipalChanges = checkForPrincipalChanges;
    }

    /**
     * If <tt>checkForPrincipalChanges</tt> is set, and a change of principal is detected, determines whether
     * any existing session should be invalidated before proceeding to authenticate the new principal.
     *
     * @param invalidateSessionOnPrincipalChange <tt>false</tt> to retain the existing session. Defaults to <tt>true</tt>.
     */
    public void setInvalidateSessionOnPrincipalChange(boolean invalidateSessionOnPrincipalChange) {
        this.invalidateSessionOnPrincipalChange = invalidateSessionOnPrincipalChange;
    }

    /**
     * Credentials aren't usually applicable, but if a {@code credentialsRequestHeader} is set, this
     * will be read and used as the credentials value. Otherwise a dummy value will be used.
     */
    protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
        return request.getParameter('ticket');
    }
}

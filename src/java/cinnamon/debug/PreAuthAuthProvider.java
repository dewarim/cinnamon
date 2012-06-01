package cinnamon.debug;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.Ordered;
import org.springframework.security.authentication.AccountStatusUserDetailsChecker;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsChecker;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.util.Assert;

/**
 * This class is based on the Spring PreAuthAuthenticationProvider and is used for debugging.
 * Later on we should be able to replace it with the original class.
 */
public class PreAuthAuthProvider implements AuthenticationProvider, InitializingBean, Ordered {
    private static final Log logger = LogFactory.getLog(PreAuthenticatedAuthenticationProvider.class);

    private AuthenticationUserDetailsService preAuthenticatedUserDetailsService = null;
    private UserDetailsChecker userDetailsChecker = new AccountStatusUserDetailsChecker();
    private boolean throwExceptionWhenTokenRejected = false;

    private int order = -1; // default: same as non-ordered

    /**
     * Check whether all required properties have been set.
     */
    public void afterPropertiesSet() {
        Assert.notNull(preAuthenticatedUserDetailsService, "An AuthenticationUserDetailsService must be set");
    }

    /**
     * Authenticate the given PreAuthenticatedAuthenticationToken.
     * <p>
     * If the principal contained in the authentication object is null, the request will be ignored to allow other
     * providers to authenticate it.
     */
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        try{
        if (!supports(authentication.getClass())) {
            logger.warn("does not support authentication");
            return null;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("PreAuthenticated authentication request: " + authentication);
        }

        if (authentication.getPrincipal() == null) {
            logger.debug("No pre-authenticated principal found in request.");

            if (throwExceptionWhenTokenRejected) {
                throw new BadCredentialsException("No pre-authenticated principal found in request.");
            }
            return null;
        }

        if (authentication.getCredentials() == null) {
            logger.debug("No pre-authenticated credentials found in request.");

            if (throwExceptionWhenTokenRejected) {
                throw new BadCredentialsException("No pre-authenticated credentials found in request.");
            }
            return null;
        }

        UserDetails ud = preAuthenticatedUserDetailsService.loadUserDetails(authentication);

        userDetailsChecker.check(ud);

        PreAuthenticatedAuthenticationToken result =
                new PreAuthenticatedAuthenticationToken(ud, authentication.getCredentials(), ud.getAuthorities());
        result.setDetails(authentication.getDetails());
    logger.debug("PAAP-result-1: "+result);
    logger.debug("PAAP-result-2: "+result.isAuthenticated());
        return result;
        }
        catch (Exception e){
            logger.error("PAAP failed: ",e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Indicate that this provider only supports PreAuthenticatedAuthenticationToken (sub)classes.
     */
    public boolean supports(Class<? extends Object> authentication) {
        logger.debug("authentication.class: "+authentication.getClass().getName());
        return PreAuthenticatedAuthenticationToken.class.isAssignableFrom(authentication);
    }

    /**
     * Set the AuthenticatedUserDetailsServices to be used.
     *
     * @param aPreAuthenticatedUserDetailsService
     */
    public void setPreAuthenticatedUserDetailsService(AuthenticationUserDetailsService aPreAuthenticatedUserDetailsService) {
        this.preAuthenticatedUserDetailsService = aPreAuthenticatedUserDetailsService;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int i) {
        order = i;
    }

    /**
     * If true, causes the provider to throw a BadCredentialsException if the presented authentication
     * request is invalid (contains a null principal or credentials). Otherwise it will just return
     * null. Defaults to false.
     */
    public void setThrowExceptionWhenTokenRejected(boolean throwExceptionWhenTokenRejected) {
        this.throwExceptionWhenTokenRejected = throwExceptionWhenTokenRejected;
    }

    /**
     * Sets the strategy which will be used to validate the loaded <tt>UserDetails</tt> object
     * for the user. Defaults to an {@link AccountStatusUserDetailsChecker}.
     * @param userDetailsChecker
     */
    public void setUserDetailsChecker(UserDetailsChecker userDetailsChecker) {
        Assert.notNull(userDetailsChecker, "userDetailsChacker cannot be null");
        this.userDetailsChecker = userDetailsChecker;
    }
}

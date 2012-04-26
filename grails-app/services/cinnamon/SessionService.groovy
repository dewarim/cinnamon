package cinnamon

import javax.persistence.EntityManager
import cinnamon.exceptions.CinnamonException
import cinnamon.global.ConfThreadLocal

class SessionService {

    // TODO: integrate with Spring Security Plugin
    Session initSession(String ticket, String repositoryName, String command) {
        log.debug("Fetching session-Object with em: " + em);

        Session session;

        log.debug("looking up session by ticket: " + ticket);
        session = Session.findByTicket(ticket);
        // prevent a user from using an expired session, (unless he wishes to disconnect):
        if(command.equals("disconnect")){
            log.debug("disconnect requested; no need for expiration check");
            return null;
        }
        else{
            if(session == null){
                throw new CinnamonException("error.session.not_found");
            }
            session.checkForExpiration();
            log.debug("after checkForExpiration");
            Long sessionExpirationTime =
                ConfThreadLocal.getConf().getSessionExpirationTime(repositoryName);
            log.debug("Session is valid");
            session.renewSession(sessionExpirationTime);
            log.debug("Session renewed until " + session.getExpires());
        }

        log.debug("retrieved result: " + session + " with ticket " + session.getTicket());

        if (session.getUser() == null) {
            log.debug("Invalid session ticket - session.getUser() is null");
            throw new CinnamonException("error.session.invalid");
        }

        return session;
    }

}

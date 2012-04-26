package humulus

import cinnamon.utils.security.HashMaker
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.encoding.PasswordEncoder

/**
 *
 */
class CinnamonPasswordEncoder implements PasswordEncoder{

    Logger log = LoggerFactory.getLogger(this.class)

    String encodePassword(String rawPass, Object salt) {
        // The current implementation ignores the salt as it's incompatible to the
        // existing system.

        def env = EnvironmentHolder.getEnvironment()
		if( env.get('encryptPasswords')?.equals("false")){
          return rawPass
        }
        else{
		  return HashMaker.createDigest(rawPass)
        }
    }

    boolean isPasswordValid(String encPass, String rawPass, Object salt) {
//        log.debug("encPass: $encPass")
//        log.debug("rawPass: $rawPass")
//        log.debug("salt: $salt")
//        log.debug("${Environment.list()}")
//        log.debug("HashMaker.createDigest: ${HashMaker.createDigest(rawPass)}")
        
        def env = EnvironmentHolder.getEnvironment()
		if( env.get('encryptPasswords')?.equals("false")){
          return rawPass.equals(encPass)
        }
        else{
		  return HashMaker.compareWithHash(rawPass, encPass)
        }
    }


}

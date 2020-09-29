package nstic.util

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationListener
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent

/**
 * Created by brad on 3/8/16.
 */
class AuthFailureSecurityListener implements ApplicationListener<AbstractAuthenticationFailureEvent> {

    private static final Logger log = LoggerFactory.getLogger(AuthFailureSecurityListener.class);

    @Override
    void onApplicationEvent(AbstractAuthenticationFailureEvent event) {
        log.error("AUTHENTICATION ERROR: "+event.getException().toString());
    }


}

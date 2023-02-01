package nstic.web

import grails.artefact.Interceptor
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken

class UrlPrintingInterceptor implements Interceptor {

    int order = HIGHEST_PRECEDENCE

    UrlPrintingInterceptor(){
        matchAll()
    }

    boolean before(){
        try {
            if (controllerName != 'assets') {
                User user = User.findByUsername(((OAuth2AuthenticationToken) SecurityContextHolder.getContext().getAuthentication()).getName())
                log.info("URL[@|cyan ${controllerName}|@:@|green ${actionName}|@${params.id ? ':' + params.id : ''}] [user:@|yellow ${user ?: 'anonymous'}|@]")
            }
        }catch(Throwable t){}
        return true;
    }

    boolean after() {
        return true;
    }

    void afterView() {
        // no-op
    }

}//end class UrlPrintingInterceptor

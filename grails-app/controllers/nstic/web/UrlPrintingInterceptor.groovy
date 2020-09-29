package nstic.web

import grails.artefact.Interceptor

class UrlPrintingInterceptor implements Interceptor {

    int order = HIGHEST_PRECEDENCE

    UrlPrintingInterceptor(){
        matchAll()
    }

    def springSecurityService


    boolean before(){
        try {
            if (controllerName != 'assets') {
                log.info("URL[@|cyan ${controllerName}|@:@|green ${actionName}|@${params.id ? ':' + params.id : ''}] [user:@|yellow ${springSecurityService.currentUser ?: 'anonymous'}|@]")
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

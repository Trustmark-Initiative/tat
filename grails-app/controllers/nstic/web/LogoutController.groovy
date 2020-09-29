package nstic.web

import grails.plugin.springsecurity.SpringSecurityUtils
import org.springframework.security.access.annotation.Secured

class LogoutController {

    def springSecurityService

    @Secured('isFullyAuthenticated()')
    def index() {
        log.info("Logging out user @|cyan ${springSecurityService.currentUser}|@...")
        redirect uri: SpringSecurityUtils.securityConfig.logout.filterProcessesUrl // '/j_spring_security_logout'
    }

}

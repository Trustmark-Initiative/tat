package nstic.web

import grails.plugin.springsecurity.annotation.Secured
import nstic.util.PasswordUtil
import org.apache.commons.lang.StringUtils

import javax.servlet.ServletException

@Secured(["ROLE_REPORTS_ONLY", "ROLE_USER"])
class ProfileController {

    def springSecurityService;

    def index() {
        User user = springSecurityService.currentUser
        log.debug("Showing @|cyan ${user}|@ their profile page...")
        [command : new ProfileCommand(user), user: user];
    }//end index()

    // Called by the index form as a post
    def update(ProfileCommand command) {
        User user = springSecurityService.currentUser
        log.debug("Processing @|cyan ${user}|@ profile changes...")

        if (command.hasErrors()) {
            log.warn "Cannot process profile update due to command errors."
            return render(view: 'index', model: [command: command, user: user])
        }

        user.username = command.username;
        if( command.password )
            user.password = command.password;
        if( user.isUser() ) {
            user.organization = command.organization;
        }
        user.contactInformation.email = command.email;
        user.contactInformation.responder = command.responder;
        user.contactInformation.phoneNumber = command.phoneNumber;
        user.contactInformation.mailingAddress = command.mailingAddress;
        user.contactInformation.notes = command.notes;

        log.debug("Storing user changes...")
        User.withTransaction {
            user.save(failOnError: true, flush: true)
        }
        flash.message = "Successfully updated your profile."
        log.debug("Successfully updated profile for @|cyan $user|@")
        return redirect(action:'index')
    }//end upate()


}//end ProfileController()


class ProfileCommand {
    public ProfileCommand(){}
    public ProfileCommand(User user){
        this.username = user.username;
        this.organization = user.organization;
        this.email = user.contactInformation.email;
        this.responder = user.contactInformation.responder;
        this.phoneNumber = user.contactInformation.phoneNumber;
        this.mailingAddress = user.contactInformation.mailingAddress;
        this.notes = user.contactInformation.notes;
    }

    String username;
    String password;
    String password2;

    Organization organization;

    String email;
    String responder;
    String phoneNumber;
    String mailingAddress;
    String notes;

    static constraints = {
        username(blank: false, maxSize: 255) // TODO add a check for pre-existing username.
        password(nullable: true, blank: true, maxSize: 255, validator: {value, object, errors ->
            if( value && value.trim().length() > 0 && PasswordUtil.isValid(value)){
                errors.rejectValue('password', 'password.complexity.fail', 'Your password should be more than 6 characters, and contain at least 2 numbers & 2 letters.');
                return false;
            }
        })
        password2(nullable: true, maxSize: 255, validator: {value, object, errors ->
            if( (StringUtils.isNotBlank(value) || StringUtils.isNotBlank(object.password)) && !value.equals(object.password) ){
                errors.rejectValue("password2", "passwords.not.match", "The two password fields do not match.")
                return false;
            }
        })

        organization(nullable: false)

        email(email: true, blank: false, maxSize: 255)
        responder(blank: false, maxSize: 512)
        phoneNumber(blank: false, maxSize: 32)
        mailingAddress(blank: false, maxSize: 1024)
        notes(nullable: true, blank: true, maxSize: 65535)
    }

}//end ProfileCommand
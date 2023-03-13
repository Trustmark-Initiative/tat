package nstic.web

import nstic.util.PasswordUtil
import org.apache.commons.lang.StringUtils
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder


@PreAuthorize('hasAnyAuthority("tat-contributor", "tat-viewer", "tat-admin")')
class ProfileController {

    def index() {
        User user = User.findByUsername(((OAuth2AuthenticationToken) SecurityContextHolder.getContext().getAuthentication()).getName())
        log.debug("Showing @|cyan ${user}|@ their profile page...")

        ProfileCommand cmd = ProfileCommand.fromUser(user)

        [command : cmd, user: user]
    }//end index()

    // Called by the index form as a post
    def update(ProfileCommand command) {
        User user = User.findByUsername(((OAuth2AuthenticationToken) SecurityContextHolder.getContext().getAuthentication()).getName())
        log.debug("Processing @|cyan ${user}|@ profile changes...")

        if (command.hasErrors()) {
            log.warn "Cannot process profile update due to command errors."
            return render(view: 'index', model: [command: command, user: user])
        }

        user.username = command.username;
        if( user.isUser() || user.isAdmin()) {
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

    public static ProfileCommand fromUser( User user ){
        ProfileCommand cmd = new ProfileCommand();
        cmd.username = user.username;
        cmd.organization = user.organization;
        cmd.email = user.contactInformation.email;
        cmd.responder = user.contactInformation.responder;
        cmd.phoneNumber = user.contactInformation.phoneNumber;
        cmd.mailingAddress = user.contactInformation.mailingAddress;
        cmd.notes = user.contactInformation.notes;

        return cmd;
    }

    String username;
    Organization organization;

    String email;
    String responder;
    String phoneNumber;
    String mailingAddress;
    String notes;

    static constraints = {
        username(blank: false, maxSize: 255) // TODO add a check for pre-existing username.
        organization(nullable: true)
        email(email: true, blank: false, maxSize: 255)
        responder(blank: false, maxSize: 512)
        phoneNumber(nullable: true, blank: true, maxSize: 32)
        mailingAddress(nullable: true, blank: true, maxSize: 1024)
        notes(nullable: true, blank: true, maxSize: 65535)
    }

}//end ProfileCommand
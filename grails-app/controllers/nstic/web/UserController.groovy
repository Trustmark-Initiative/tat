package nstic.web

import grails.gorm.transactions.Transactional
import nstic.util.PasswordUtil
import org.apache.commons.lang.StringUtils
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.ObjectError

import javax.servlet.ServletException

@Transactional
@PreAuthorize('hasAuthority("tat-admin")')
class UserController {

    def index() {
        redirect(action: 'list')
    }


    def list() {
        log.debug("Listing users...")
        if (!params.max)
            params.max = '20';
        params.max = Math.min(100, Integer.parseInt(params.max)).toString(); // Limit to at most 100 users at a time.
        [users: User.list(params), userCountTotal: User.count()]
    }//end list

    def edit() {
        log.debug "Edit user call for user: @|cyan ${params.id}|@..."
        User user = User.get(params.id);
        if( !user ){
            log.warn "Invalid request to edit non-existant user: ${params.id}!"
            throw new ServletException("No such user: ${params.id}")
        }

        log.info("Editing user @|cyan ${user.id}|@:@|green ${user.username}|@...")
        UserEditCommand editCmd = UserEditCommand.fromUser(user);

        [userCommand: editCmd, user: user]
    }//end edit()

    def create(){
        log.debug("Request to create new user...")
        [userCommand: new UserCommand()]
    }//end create()

    @PreAuthorize('permitAll()')
    def createFromGrant() {
        log.info("Creating user form grant...");
        ContactGrant contactGrant = ContactGrant.findByGrantId(params.id);
        if (contactGrant == null) {
            log.warn("No grant with id[${params.id}]!")
            Thread.sleep(3000); // Try to prevent to many bad requests.  Is this the most effective way?
            return render(view: 'noGrant');
        }
        if( request.method.toUpperCase() == "GET") {
            log.info("Claiming grant ${params.id} for email[${contactGrant.contactInformation.email}] to see org[${contactGrant.organization.name}] reports, showing form...")

            ContactInformation ci = contactGrant.contactInformation;
            User userAccount = User.findByContactInformation(ci);
            if( userAccount ){
                [newAccount: false, contactGrant: contactGrant, user: userAccount]
            }else{
                [newAccount: true, contactGrant: contactGrant]
            }
        }else if( request.method.toUpperCase() == "POST") {
            log.info("Claiming grant ${params.id} for email[${contactGrant.contactInformation.email}] to see org[${contactGrant.organization.name}] reports, processing form...")

            if( StringUtils.isEmpty(params.password) || StringUtils.isEmpty(params.passwordAgain) ){
                flash.error = "You must enter a password in both fields.";
                return [newAccount: true, contactGrant: contactGrant];
            }else if( params.password != params.passwordAgain ){
                flash.error = "The passwords you entered are not equal.";
                return [newAccount: true, contactGrant: contactGrant];
            }else if( PasswordUtil.isValid(params.password) ){
                flash.error = "The password must be at least 8 characters, and contain at least 2 letters and 2 numbers.";
                return [newAccount: true, contactGrant: contactGrant];
            }

            log.info("Processing valid form...");
            User user = new User();
            user.organization = contactGrant.organization;
            user.contactInformation = contactGrant.contactInformation;
            user.username = contactGrant.contactInformation.email;
            user.save(failOnError: true, flush: true);

            // TODO: do we need this function? If so, how do we create a Role and assign it to the user?
//            Role reportOnlyRole = Role.findByAuthority(Role.ROLE_REPORTS_ONLY);
//            UserRole.create(user, reportOnlyRole, true);

            // TODO: Deal with springSecurityService.reauthenticate
//            springSecurityService.reauthenticate(user.username, params.password);

            flash.message = "Successfully created your user account."
            return redirect(controller:'home', action: 'index');
        }
    }


    def update(UserEditCommand userCommand){
        log.info("Updating user @|cyan ${userCommand.email}|@...")

        User user = User.findById(userCommand.existingUserId);

        if(!userCommand.validate()){
            log.warn "User edit form does not validate: "
            userCommand.errors.getAllErrors().each { ObjectError error ->
                log.warn "    ${error.defaultMessage}"
            }
            return render(view: 'edit', model: [userCommand: userCommand, user: user])
        }

        user.organization = Organization.findById(userCommand.organizationId);
        // TODO associate contact information to org?

        user.save(failOnError: true, flush: true);

        flash.message = "Successfully udpated user '${user.username}'"
        return redirect(action:'list');
    }//end update()

    def save(UserCommand userCommand){
        log.info("Request to save user: ${userCommand?.email}")
        if(!userCommand.validate()){
            log.warn "User form does not validate: "
            userCommand.errors.getAllErrors().each { ObjectError error ->
                log.warn "    ${error.defaultMessage}"
            }
            return render(view: 'create', model: [userCommand: userCommand])
        }

        log.debug("Creating contact information...");
        ContactInformation contactInformation = new ContactInformation()
        contactInformation.responder = userCommand.name
        contactInformation.email = userCommand.email
        contactInformation.mailingAddress = userCommand.mailingAddress
        contactInformation.phoneNumber = userCommand.phone
        contactInformation.save(failOnError: true, flush: true)

        log.info "Saving user: ${userCommand.email}"
        User user = new User()
        user.username = userCommand.email
        user.contactInformation = contactInformation
        user.organization = Organization.findById(userCommand.organizationId)
        // TODO We may need to associate the contact information with the organization, if it is not already.
        user.save(failOnError: true, flush: true)

//        if( userCommand.reportOnlyRole ){
//            Role userRole = Role.findByAuthority(Role.ROLE_REPORTS_ONLY);
//            UserRole.create(user, userRole, true);
//        }
//        if( userCommand.userRole ){
//            Role userRole = Role.findByAuthority(Role.ROLE_USER);
//            UserRole.create(user, userRole, true);
//        }
//        if( userCommand.adminRole ){
//            Role userRole = Role.findByAuthority(Role.ROLE_ADMIN);
//            UserRole.create(user, userRole, true);
//        }

        flash.message = "Successfully created user '${user.username}'"
        return redirect(action:'list');
    }//end save()

}//end UserController

class UserCommand {

    String name
    String email
    String phone
    String mailingAddress
    Integer organizationId
    Boolean adminRole = Boolean.FALSE
    Boolean userRole = Boolean.TRUE
    Boolean reportOnlyRole = Boolean.FALSE


    static constraints = {
        email(nullable: false, blank: false, email: true, validator: {val, obj ->
            User.withTransaction { tx ->
                User user = User.findByUsername(obj.email)
                if( user ){
                    return 'username.exists'
                }
            }
        })

        name(nullable: false, blank: false)
        phone(nullable: false, blank: false, matches: '([0-9]{3}\\-[0-9]{3}\\-[0-9]{4})|([0-9]{10})')
        mailingAddress(nullable: false, blank: false)
        organizationId(nullable: false,  validator: { val, obj, errors ->
            Organization.withTransaction { tx ->
                Organization org = Organization.findById(val);
                if( !org ){
                    errors.rejectValue("organizationId", "org.does.not.exist", [val] as String[], "No such Organizaiton[id=${val}] exists.")
                    return false;
                }
                return true;
            }
        })
    }



}

class UserEditCommand {
    public static UserEditCommand fromUser( User user ){
        UserEditCommand cmd = new UserEditCommand();
        cmd.existingUserId = user.id
        cmd.username = user.username
        cmd.name = user.contactInformation.responder;
        cmd.email = user.contactEmail
        cmd.phone = user.contactInformation?.phoneNumber;
        cmd.mailingAddress = user.contactInformation?.mailingAddress;
        cmd.organizationId = user.organization?.id;
        cmd.adminRole = user.isAdmin();
        cmd.userRole = user.isUser();
        cmd.reportOnlyRole = user.isReportOnly();
        return cmd;
    }

    Integer existingUserId;
    String username
    String name
    String email
    String phone
    String mailingAddress
    Integer organizationId
    Boolean reportOnlyRole = Boolean.FALSE
    Boolean userRole = Boolean.FALSE
    Boolean adminRole = Boolean.FALSE


    static constraints = {
        existingUserId(nullable: false)
        email(nullable: false, blank: false, email: true)

        name(nullable: false, blank: false)
        phone(nullable: true, blank: false, matches: '([0-9]{3}\\-[0-9]{3}\\-[0-9]{4})|([0-9]{10})')
        mailingAddress(nullable: true, blank: false)
        organizationId(nullable: true, validator: { val, obj, errors ->
            Organization.withTransaction { tx ->
                Organization org = Organization.findById(val);
                if( !org ){
                    errors.rejectValue("organizationId", "org.does.not.exist", [val] as String[], "No such Organizaiton[id=${val}] exists.")
                    return false;
                }
                return true;
            }
        })
    }



}

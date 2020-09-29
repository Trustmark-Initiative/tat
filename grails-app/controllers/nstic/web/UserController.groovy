package nstic.web

import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.annotation.Secured
import nstic.util.PasswordUtil
import org.apache.commons.lang.StringUtils
import org.springframework.validation.ObjectError

import javax.servlet.ServletException

@Transactional
@Secured('ROLE_ADMIN')
class UserController {

    def springSecurityService;

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

        [userCommand: editCmd]
    }//end edit()

    def create(){
        log.debug("Request to create new user...")
        [userCommand: new UserCommand()]
    }//end create()

    @Secured("permitAll()")
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
            user.password = params.password;
            user.enabled = true;
            user.passwordExpired = false;
            user.accountExpired = false;
            user.accountLocked = false;
            user.username = contactGrant.contactInformation.email;
            user.save(failOnError: true, flush: true);

            Role reportOnlyRole = Role.findByAuthority(Role.ROLE_REPORTS_ONLY);
            UserRole.create(user, reportOnlyRole, true);

            springSecurityService.reauthenticate(user.username, params.password);

            flash.message = "Successfully created your user account."
            return redirect(controller:'home', action: 'index');
        }
    }


    def update(UserEditCommand userCommand){
        log.info("Updating user @|cyan ${userCommand.email}|@...")

        if(!userCommand.validate()){
            log.warn "User edit form does not validate: "
            userCommand.errors.getAllErrors().each { ObjectError error ->
                log.warn "    ${error.defaultMessage}"
            }
            return render(view: 'edit', model: [userCommand: userCommand])
        }

        User user = User.findById(userCommand.existingUserId);
        if( user.username != userCommand.email ){
            // User has updated their email address.  This affects their username to login as well.
            User existingUser = User.findByUsername(userCommand.email);
            if( existingUser ){
                log.warn "Cannot change email[@|green ${user.username}|@] to @|yellow ${userCommand.email}|@, a user already exists with that email!";
                userCommand.errors.reject('user.email.not.unique', [user.username, userCommand.email] as Object[],
                        "Cannot change email ${user.username} to ${userCommand.email}.  A user with that email already exists.")
                return render(view: 'edit', model: [userCommand: userCommand])
            }
            user.contactInformation.email = userCommand.email
            user.username = userCommand.email
        }
        user.contactInformation.responder = userCommand.name
        user.contactInformation.mailingAddress = userCommand.mailingAddress
        user.contactInformation.phoneNumber = userCommand.phone
        user.organization = Organization.findById(userCommand.organizationId);
        // TODO associate contact information to org?
        user.enabled = userCommand.enabled
        if( userCommand.password && userCommand.password.length() > 0 ){
            user.password = userCommand.password;
        }
        user.save(failOnError: true, flush: true);

        UserRole.removeAll(user);
        if( userCommand.reportOnlyRole ){
            Role userRole = Role.findByAuthority(Role.ROLE_REPORTS_ONLY);
            UserRole.create(user, userRole, true);
        }
        if( userCommand.userRole ){
            Role userRole = Role.findByAuthority(Role.ROLE_USER);
            UserRole.create(user, userRole, true);
        }
        if( userCommand.adminRole ){
            Role userRole = Role.findByAuthority(Role.ROLE_ADMIN);
            UserRole.create(user, userRole, true);
        }

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
        user.password = userCommand.password
        user.contactInformation = contactInformation
        user.organization = Organization.findById(userCommand.organizationId)
        // TODO We may need to associate the contact information with the organization, if it is not already.
        user.enabled = true
        user.passwordExpired = false
        user.accountExpired = false
        user.accountLocked = false
        user.save(failOnError: true, flush: true)

        if( userCommand.reportOnlyRole ){
            Role userRole = Role.findByAuthority(Role.ROLE_REPORTS_ONLY);
            UserRole.create(user, userRole, true);
        }
        if( userCommand.userRole ){
            Role userRole = Role.findByAuthority(Role.ROLE_USER);
            UserRole.create(user, userRole, true);
        }
        if( userCommand.adminRole ){
            Role userRole = Role.findByAuthority(Role.ROLE_ADMIN);
            UserRole.create(user, userRole, true);
        }

        flash.message = "Successfully created user '${user.username}'"
        return redirect(action:'list');
    }//end save()

}//end UserController

class UserCommand {

    String password
    String passwordAgain
    String name
    String email
    String phone
    String mailingAddress
    Integer organizationId
    Boolean adminRole = Boolean.FALSE
    Boolean userRole = Boolean.FALSE
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
        password(nullable: false, blank: false, minSize: 6, validator: { val, obj ->
            if( val != obj.passwordAgain ){
                return "passwords.not.equal"
            }
            String invalid = PasswordUtil.isValid(val)
            if( invalid )
                return invalid;
        })
        passwordAgain(nullable: false, blank: false)

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
        cmd.name = user.contactInformation.responder;
        cmd.email = user.contactInformation.email;
        cmd.phone = user.contactInformation.phoneNumber;
        cmd.mailingAddress = user.contactInformation.mailingAddress;
        cmd.organizationId = user.organization.id;
        cmd.adminRole = user.isAdmin();
        cmd.userRole = user.isUser();
        cmd.reportOnlyRole = user.isReportOnly();
        cmd.enabled = user.enabled;
        return cmd;
    }

    Integer existingUserId;
    String password
    String passwordAgain
    String name
    String email
    String phone
    String mailingAddress
    Integer organizationId
    Boolean enabled = Boolean.TRUE
    Boolean reportOnlyRole = Boolean.FALSE
    Boolean userRole = Boolean.FALSE
    Boolean adminRole = Boolean.FALSE


    static constraints = {
        existingUserId(nullable: false)
        email(nullable: false, blank: false, email: true)
        password(nullable: true, blank: true)
        passwordAgain(nullable: true, blank: true, validator: { val, obj ->
            if( val != obj.passwordAgain ){
                return "passwords.not.equal"
            }
            if( val?.length() > 0 ) {
                String invalid = PasswordUtil.isValid(val)
                if (invalid)
                    return invalid;
            }
        })

        name(nullable: false, blank: false)
        phone(nullable: false, blank: false, matches: '([0-9]{3}\\-[0-9]{3}\\-[0-9]{4})|([0-9]{10})')
        mailingAddress(nullable: false, blank: false)
        organizationId(nullable: false, validator: { val, obj, errors ->
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

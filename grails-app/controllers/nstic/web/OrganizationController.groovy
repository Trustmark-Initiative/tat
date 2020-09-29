package nstic.web

import grails.converters.JSON
import grails.converters.XML
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.annotation.Secured
import org.springframework.util.StringUtils
import org.springframework.validation.ObjectError

import javax.servlet.ServletException

@Transactional
@Secured("ROLE_USER")
class OrganizationController {

    def springSecurityService;

    def index() {
        redirect(action:'list')
    }

    def list(){
        log.debug("Listing organizations...")
        if (!params.max)
            params.max = '20';
        params.max = Math.min(100, Integer.parseInt(params.max)).toString(); // Limit to at most 100 orgs at a time.
        [organizations: Organization.list(params), orgsCountTotal: Organization.count()]
    }

    @Secured(['ROLE_REPORTS_ONLY', 'ROLE_USER'])
    def listContacts() {
        User user = springSecurityService.currentUser;
        log.debug("User[@|blue ${user}|@] viewing contact list for org[@|cyan ${params.id}|@]...")
        Organization org = find(params.id);

        if( user.isReportOnly() && !user.isUser() ){
            if( org != user.organization &&
                    org.primaryContact != user.contactInformation &&
                    !org.contacts.contains(user.contactInformation) ){
                log.error("User[${user.username}] has tried to view organization[${org.name}] contact list, but was refused.")
                throw new ServletException("Forbidden to view organization ${params.id}")
            }
        }

        Boolean contactsOnly = params.contactsOnly ? Boolean.parseBoolean(params.contactsOnly) : Boolean.FALSE;

        log.debug("Getting direct contacts...");
        List<ContactInformation> contactsList = findContacts(org, contactsOnly);

        log.debug("For organization[@|cyan ${org.name}|@], resolved @|green ${contactsList.size()}|@ contacts. Rendering...");
        withFormat {
            html {
                throw new ServletException("HTML VIEW NOT IMPLEMENTED");
            }
            json {
                def contactsJson = []
                contactsList.each{ ci ->
                    contactsJson.add(ci.toJsonMap(false));
                }
                render contactsJson as JSON
            }
            xml {
                render contactsList as XML
            }
        }

    }//end listContacts()

    /**
     * Lists all contacts from the database who do NOT have any affiliation with the supplied organization.
     */
    def listUnaffiliatedContacts() {
        User user = springSecurityService.currentUser;
        log.debug("User[@|blue ${user}|@] viewing unaffiliated contact list for org[@|cyan ${params.id}|@]...")
        Organization org = find(params.id);

        Boolean contactsOnly = params.contactsOnly ? Boolean.parseBoolean(params.contactsOnly) : Boolean.FALSE;

        log.debug("Getting direct contacts...");
        List<ContactInformation> contactsList = findContacts(org, contactsOnly);
        List<ContactInformation> unaffiliatedContactsList = []
        ContactInformation.findAll().each { ContactInformation ci ->
            if( !contactsList.contains(ci) ){
                if( contactsOnly ){
                    if( User.findByContactInformation(ci) == null ){
                        unaffiliatedContactsList.add(ci);
                    }
                }else{
                    unaffiliatedContactsList.add(ci);
                }
            }
        }

        log.debug("For organization[@|cyan ${org.name}|@], resolved @|green ${unaffiliatedContactsList.size()}|@ unaffiliated contacts. Rendering...");
        withFormat {
            html {
                throw new ServletException("HTML VIEW NOT IMPLEMENTED");
            }
            json {
                def contactsJson = []
                unaffiliatedContactsList.each{ ci ->
                    contactsJson.add(ci.toJsonMap(false));
                }
                render contactsJson as JSON
            }
            xml {
                render unaffiliatedContactsList as XML
            }
        }

    }//end listUnaffiliatedContacts()


    def create(){
        [orgCommand : new CreateOrganizationCommand()]
    }

    def save(CreateOrganizationCommand orgCommand){
        log.info("Request to save organization: ${orgCommand?.uri} - ${orgCommand?.name}")
        if(!orgCommand.validate()){
            log.warn "Organization form does not validate: "
            orgCommand.errors.getAllErrors().each { ObjectError error ->
                log.warn "    ${error.defaultMessage}"
            }
            return render(view: 'create', model: [orgCommand: orgCommand])
        }

        log.info "Saving new organization: ${orgCommand.uri}"
        ContactInformation contactInformation = new ContactInformation()
        contactInformation.responder = orgCommand.responder
        contactInformation.email = orgCommand.email
        contactInformation.mailingAddress = orgCommand.mailingAddress
        contactInformation.phoneNumber = orgCommand.phone
        contactInformation.notes = orgCommand.notes
        contactInformation.save(failOnError: true, flush: true)

        Organization org = new Organization();
        org.uri = orgCommand.uri
        org.identifier = orgCommand.identifier
        org.name = orgCommand.name
        org.primaryContact = contactInformation;
        org.save(failOnError: true, flush: true);


        flash.message = "Successfully created organization '${org.name}'"
        return redirect(action:'list');
    }

    def edit(){
        log.info("Editing organization[${params.id}]...")
        Organization org = Organization.findById(params.id);
        if( !org )
            throw new ServletException("No such organization: ${params.id}")

        EditOrganizationCommand command = new EditOrganizationCommand()
        command.setData(org);
        [orgCommand: command]
    }//end edit()

    def update(EditOrganizationCommand orgCommand){
        log.info("Updating organization @|cyan ${orgCommand.uri}|@...")

        if(!orgCommand.validate()){
            log.warn "Organization edit form does not validate: "
            orgCommand.errors.getAllErrors().each { ObjectError error ->
                log.warn "    ${error.defaultMessage}"
            }
            return render(view: 'edit', model: [orgCommand: orgCommand])
        }

        Organization org = Organization.findById(orgCommand.id);
        if( !org )
            throw new ServletException("Unable to find organization ${orgCommand.id}")

        org.uri = orgCommand.uri
        org.name = orgCommand.name
        org.identifier = orgCommand.identifier

        if( org.primaryContact == null ){
            ContactInformation primaryContact = new ContactInformation();
            primaryContact.email = orgCommand.email
            primaryContact.responder = orgCommand.responder
            primaryContact.phoneNumber = orgCommand.phone
            primaryContact.mailingAddress = orgCommand.mailingAddress
            primaryContact.notes = orgCommand.notes
            primaryContact.save(failOnError: true);
            org.primaryContact = primaryContact;
        }else{
            org.primaryContact.email = orgCommand.email
            org.primaryContact.responder = orgCommand.responder
            org.primaryContact.phoneNumber = orgCommand.phone
            org.primaryContact.mailingAddress = orgCommand.mailingAddress
            org.primaryContact.notes = orgCommand.notes
            org.primaryContact.save(failOnError: true);
        }

        org.save(failOnError: true, flush: true);

        flash.message = "Successfully updated organization '${org.name}'"
        return redirect(action:'list');
    }

    def deleteArtifact() {
        User user = springSecurityService.currentUser;
        if( !params.id ){
            log.warn "Organization view requires org id!"
            throw new ServletException("Missing required id parameter.")
        }

        log.debug("User[@|blue ${user}|@] deleting artifact @|green ${params.artifactId}|@ for org [@|cyan ${params.id}|@]...")
        Organization org = find(params.id);
        OrganizationArtifact organizationArtifact = org.findArtifact(params.artifactId);
        if( organizationArtifact == null ){
            log.warn("Could not find artifact: ${params.artifactId} for org[@|green ${params.id}|@]")
            throw new ServletException("No such artifact: ${params.artifactId} on Organization: ${params.id}")
        }
        String artifactName = organizationArtifact.displayName ?: organizationArtifact.data.originalFilename;
        log.info("User[@|blue ${user}|@] deleting artifact @|green ${artifactName}|@ for org [@|cyan ${org.identifier}|@:@|yellow ${org.name}|@]...")
        org.removeFromArtifacts(organizationArtifact);
        organizationArtifact.delete(failOnError: true, flush: true);
        org.save(failOnError: true, flush: true);

        flash.message = "Successfully deleted artifact ${artifactName}."

        log.debug("Redirecting to view Org[${org.identifier}] page...");
        return redirect(action:'view', id: org.identifier);
    }


    def createArtifact() {
        if( !params.id ){
            log.warn "Organization view requires org id!"
            throw new ServletException("Missing required id parameter.")
        }

        log.info("User[@|blue ${springSecurityService.currentUser}|@] creating artifact for org [@|cyan ${params.id}|@]...")
        Organization org = find(params.id);

        CreateOrganizationArtifactCommand createForm = new CreateOrganizationArtifactCommand();
        createForm.organization = org;

        log.debug("Displaying form...")
        [command: createForm]
    }

    def saveArtifact(CreateOrganizationArtifactCommand createForm) {
        User user = springSecurityService.currentUser;
        if( !createForm.validate() ){
            log.warn "Create Organization Artifact form does not validate: "
            createForm.errors.getAllErrors().each { ObjectError error ->
                log.warn "    ${error.defaultMessage}, Args: ${error.arguments}"
            }
            return render(view: 'createArtifact', model: [command: createForm])
        }

        log.info("User[@|green ${user}|@] creating new artifact for organization[@|cyan ${createForm.organization}|@]...");
        OrganizationArtifact organizationArtifact = new OrganizationArtifact();
        organizationArtifact.organization = createForm.organization;
        organizationArtifact.data = createForm.binaryObject;
        organizationArtifact.active = createForm.active;
        organizationArtifact.description = createForm.description;
        organizationArtifact.displayName = createForm.displayName;
        organizationArtifact.uploadingUser = user;
        organizationArtifact.save(failOnError: true, flush: true);

        flash.message = "Successfully created artifact '${createForm.displayName}' for organization ${createForm.organization.identifier}"

        log.debug("Redirecting to view Org[${createForm.organization.identifier}] page...");
        return redirect(action:'view', id: createForm.organization.identifier);
    }//end saveArtifact()

    def editArtifact() {
        if( !params.id ){
            log.warn "Organization view requires org id!"
            throw new ServletException("Missing required id parameter.")
        }

        log.info("User[@|blue ${springSecurityService.currentUser}|@] editing artifact @|yellow ${params.artifactId}|@ for org [@|cyan ${params.id}|@]...")
        Organization org = find(params.id);
        OrganizationArtifact organizationArtifact = org.findArtifact(params.artifactId);
        if( organizationArtifact == null ){
            log.warn("Could not find artifact: ${params.artifactId} for org[@|green ${params.id}|@]")
            throw new ServletException("No such artifact: ${params.artifactId} on Organization: ${params.id}")
        }


        EditOrganizationArtifactCommand editForm = new EditOrganizationArtifactCommand();
        editForm.setData(organizationArtifact);

        log.debug("Displaying edit form...")
        [command: editForm]
    }

    def updateArtifact(EditOrganizationArtifactCommand editForm) {
        User user = springSecurityService.currentUser;
        if( !editForm.validate() ){
            log.warn "Edit Organization Artifact form does not validate: "
            editForm.errors.getAllErrors().each { ObjectError error ->
                log.warn "    ${error.defaultMessage}, Args: ${error.arguments}"
            }
            return render(view: 'editArtifact', model: [command: editForm])
        }

        log.info("User[@|green ${user}|@] editing artifact[${editForm.existingArtifactId}] for organization[@|cyan ${editForm.organization}|@]...");
        OrganizationArtifact artifact = editForm.organization.findArtifact(editForm.existingArtifactId.toString());
        if( !artifact ){
            log.warn("Bad artifact id[${editForm.existingArtifactId}] on org[${editForm.organization.identifier}]")
            throw new ServletException("Bad artifact id[${editForm.existingArtifactId}] on org[${editForm.organization.identifier}]")
        }

        artifact.displayName = editForm.displayName;
        artifact.description = editForm.description;
        artifact.active = editForm.active;
        if( artifact.data.id != editForm.binaryObject.id ){
            artifact.data = editForm.binaryObject;
        }
        log.debug("Performing save...");
        artifact.save(failOnError: true, flush: true);


        flash.message = "Successfully updated artifact '${editForm.displayName}' for organization ${editForm.organization.identifier}"

        log.debug("Redirecting to view Org[${editForm.organization.identifier}] page...");
        return redirect(action:'view', id: editForm.organization.identifier);
    }



    def view() {
        if( org.apache.commons.lang.StringUtils.isBlank(params.id) ){
            log.warn "Organization view requires org id!"
            throw new ServletException("Missing required id parameter.")
        }

        log.debug("User[@|blue ${springSecurityService.currentUser}|@] viewing org [@|cyan ${params.id}|@]...")
        Organization org = find(params.id);

        List<TrustmarkMetadata> metadataList = TrustmarkMetadata.findAllByProvider(org);

        withFormat {
            html {
                [organization: org, metadataList: metadataList]
            }
            xml {
                render org as XML
            }
            json {
                render org.toJsonMap() as JSON
            }
        }

    }

    def typeahead() {
        log.debug("User[@|blue ${springSecurityService.currentUser}|@] searching[@|cyan ${params.q}|@] via Organization typeahead...")

        def criteria = Organization.createCriteria();
        def results = criteria {
            or {
                like("name", '%'+params.q+'%')
                like("uri", '%'+params.q+'%')
            }
            maxResults(25)
            order("name", "asc")
        }

        withFormat {
            html {
                throw new ServletException("NOT SUPPORTED")
            }
            xml {
                render results as XML
            }
            json {
                def resultsJSON = []
                results.each{ result ->
                    resultsJSON.add(result.toJsonMap(false));
                }
                render resultsJSON as JSON
            }
        }

    }//end typeahead

    /**
     * Adds a contact to the given organization.
     */
    def addContact() {
        User user = springSecurityService.getCurrentUser();
        Organization org = find(params.id);
        log.info("User[$user] adding contact ${params.contactToAdd} to org[${org.identifier}]...")

        ContactInformation contactToAdd = ContactInformation.get(params.contactToAdd);
        if( !contactToAdd ){
            log.warn("No such contact: ${params.contactToAdd}")
            throw new ServletException("No Such Contact: ${params.contactToAdd}")
        }

        if( org.primaryContact == null ){
            org.primaryContact = contactToAdd;
        }else{
            org.addToContacts(contactToAdd);
        }
        org.save(failOnError: true);

        return redirect(controller:'organization', action:'view', id: org.identifier);
    }//end addContact()

    /**
     * Removes the contact from the given organization.
     */
    def removeContact() {
        User user = springSecurityService.getCurrentUser();
        Organization org = find(params.id);
        log.info("User[$user] removing contact[${params.contactToRemove}] from org[${org.identifier}]...")

        ContactInformation contactToRemove = ContactInformation.get(params.contactToRemove);
        if( !contactToRemove ){
            log.warn("No such contact: ${params.contactToRemove}")
            throw new ServletException("No Such Contact: ${params.contactToRemove}")
        }

        if( org.primaryContact.equals(contactToRemove) ){
            log.debug("Removing primary contact...")
            org.primaryContact = null;
        }else if( org.contacts.contains(contactToRemove) ){
            log.debug("Removing from contacts list...")
            if( org.contacts.size() == 1 ){
                org.contacts = null;
            }else {
                org.removeFromContacts(contactToRemove);
            }
        }
        org.save(failOnError: true, flush: true);

        log.debug("Redirecting to view org[${org.identifier}]...")
        return redirect(controller:'organization', action:'view', id: org.identifier);
    }//end removeContact()



    def createComment() {
        def user = springSecurityService.currentUser;
        log.debug("User[$user] is creating a new comment for organization[${params.id}]...")
        Organization org = find(params.id);

        CreateOrganizationCommentCommand command = new CreateOrganizationCommentCommand()
        command.organization = org;

        [command: command, user: user]
    }//end createComment()

    def saveComment(CreateOrganizationCommentCommand command) {
        def user = springSecurityService.currentUser;
        log.debug("User[$user] is saving a new comment for organization[${params.id}]...")

        if(command.hasErrors() ){
            log.warn("Create Comment form has errors.")
            return render(view: 'createComment', model: [command: command]);
        }

        OrganizationComment comment = new OrganizationComment();
        comment.organization = command.organization;
        comment.comment = command.comment;
        comment.title = command.name;
        comment.user = user;
        comment.save(failOnError: true, flush: true);

        command.organization.addToComments(comment);
        command.organization.save(failOnError: true);

        flash.message = "Successfully saved comment '${comment.title}'"

        log.info("Saved comment, redirecting to view organization...");
        return redirect(action:'view', id: command.organization.identifier)
    }//end saveComment()

    def editComment() {
        def user = springSecurityService.currentUser;
        log.debug("User[$user] is updating an existing comment for organization[${params.id}]...")
        Organization org = find(params.id);

        if(StringUtils.isEmpty(params.commentId))
            throw new ServletException("Missing required field: commentId")

        OrganizationComment commentToDelete = null;
        for( OrganizationComment curComment : org.comments ){
            if( curComment.id.toString().equals(params.commentId) ){
                commentToDelete = curComment;
                break;
            }
        }
        if( !commentToDelete )
            throw new ServletException("Could not find comment ${params.commentId} for org ${org.identifier}");

        EditOrganizationCommentCommand command = new EditOrganizationCommentCommand();
        command.organization = org;
        command.commentId = commentToDelete.id;
        command.name = commentToDelete.title;
        command.comment = commentToDelete.comment;

        [org: org, user: user, command: command]
    }//end createComment()

    def updateComment(EditOrganizationCommentCommand command) {
        def user = springSecurityService.currentUser;
        log.debug("User[$user] is updating an existing comment for organization[${params.id}]...")
        Organization org = find(params.id);

        if(StringUtils.isEmpty(params.commentId))
            throw new ServletException("Missing required field: commentId")

        OrganizationComment commentToDelete = null;
        for( OrganizationComment curComment : org.comments ){
            if( curComment.id.toString().equals(params.commentId) ){
                commentToDelete = curComment;
                break;
            }
        }
        if( !commentToDelete )
            throw new ServletException("Could not find comment ${params.commentId} for org ${org.identifier}");

        if( command.hasErrors() )
            return render(view:'editComment', model: [command: command, user: user, org: org]);


        commentToDelete.comment = command.comment;
        commentToDelete.title = command.name;
        commentToDelete.user = user;
        commentToDelete.save(failOnError: true, flush: true);

        flash.message = "Successfully updated comment '${commentToDelete.title}'"

        log.info("Updated comment ${commentToDelete.id}, redirecting to view organization...");
        return redirect(action:'view', id: org.identifier)
    }//end saveComment()

    def deleteComment() {
        def user = springSecurityService.currentUser;
        log.debug("User[$user] is updating an existing comment for organization[${params.id}]...")
        Organization org = find(params.id);

        if(StringUtils.isEmpty(params.commentId))
            throw new ServletException("Missing required field: commentId")

        OrganizationComment commentToDelete = null;
        for( OrganizationComment curComment : org.comments ){
            if( curComment.id.toString().equals(params.commentId) ){
                commentToDelete = curComment;
                break;
            }
        }
        if( !commentToDelete )
            throw new ServletException("Could not find comment ${params.commentId} for org ${org.identifier}");


        org.removeFromComments(commentToDelete);
        commentToDelete.delete(failOnError: true);
        org.save(failOnError: true);

        flash.message = "Successfully deleted comment '${commentToDelete.title}'"

        log.info("Deleted comment, redirecting to view organization...");
        return redirect(action:'view', id: org.identifier)
    }//end createComment()

    //==================================================================================================================
    //  Helper Methods
    //==================================================================================================================

    /**
     * Returns any contacts associated with the given org.
     */
    private List<ContactInformation> findContacts(Organization org, Boolean contactsOnly = Boolean.FALSE){
        log.debug("Getting direct contacts...");
        List<ContactInformation> contactsList = []
        if( org.primaryContact )
            contactsList.add(org.primaryContact);
        for( ContactInformation ci : org.contacts ){
            if( !contactsList.contains(ci) )
                contactsList.add(ci);
        }

        log.debug("Getting any user contacts...");
        List<User> usersInOrg = User.findAllByOrganization(org);
        usersInOrg?.each { currentUserInOrg ->
            if( !contactsList.contains(currentUserInOrg.contactInformation) )
                contactsList.add(currentUserInOrg.contactInformation);
        }

        List<ContactInformation> trueContactsList = [];
        if( params.contactsOnly && Boolean.parseBoolean(params.contactsOnly) ){
            log.debug("Trimming out those contacts which have user accounts already...");
            for( ContactInformation ci : contactsList ){
                if( User.findByContactInformation(ci) == null ){
                    trueContactsList.add(ci);
                }
            }
        }else{
            trueContactsList.addAll(contactsList);
        }
        Collections.sort(trueContactsList, {ContactInformation ci1, ContactInformation ci2 ->
            return ci1.responder.compareToIgnoreCase(ci2.responder);
        } as Comparator)

        return trueContactsList;
    }

    private Organization find(String id){
        Organization org = null;
        if( id != null && id.matches("^[0-9]+\$") ){
            org = Organization.get(id);
        }else if( id != null ){
            org = Organization.findByIdentifier(id);
        }
        if( !org ){
            log.warn("No such org @|red ${params.id}|@ found!")
            throw new ServletException("Invalid organization id: ${params.id}")
        }
        return org;
    }

}//end OrganizationController

class EditOrganizationArtifactCommand {

    public void setData(OrganizationArtifact organizationArtifact){
        this.existingArtifactId = organizationArtifact.id;
        this.organization = organizationArtifact.organization;
        this.binaryObject = organizationArtifact.data;
        this.displayName = organizationArtifact.displayName;
        this.description = organizationArtifact.description;
        this.active = organizationArtifact.active;
    }

    Integer existingArtifactId;
    Organization organization;
    BinaryObject binaryObject;
    String displayName;
    String description;
    Boolean active = Boolean.TRUE;

    static constraints = {
        existingArtifactId(nullable: false)
        organization(nullable: false)
        binaryObject(nullable: false)
        displayName(nullable: false, blank: false, maxSize: 256, validator: {val, obj, errors ->
            if( obj.organization.artifacts && !obj.organization.artifacts.isEmpty() ) {
                for (OrganizationArtifact oa : obj.organization.getArtifacts()) {
                    if( oa.id != obj.existingArtifactId && oa.getDisplayName().equalsIgnoreCase(val) ){
                        errors.rejectValue('displayName', 'duplicate.display.name', "Display name '${val}' is already in use.")
                        return false;
                    }
                }
            }
            return true;
        })
        description(nullable: true, maxSize: 65535)
        active(nullable: false)
    }
}

class CreateOrganizationArtifactCommand {
    Organization organization;
    BinaryObject binaryObject;
    String displayName;
    String description;
    Boolean active = Boolean.TRUE;

    static constraints = {
        organization(nullable: false)
        binaryObject(nullable: false)
        displayName(nullable: false, blank: false, maxSize: 256, validator: {val, obj, errors ->
            if( obj.organization.artifacts && !obj.organization.artifacts.isEmpty() ) {
                for (OrganizationArtifact oa : obj.organization.getArtifacts()) {
                    if( oa.getDisplayName().equalsIgnoreCase(val) ){
                        errors.rejectValue('displayName', 'duplicate.display.name', "Display name '${val}' is already in use.")
                        return false;
                    }
                }
            }
            return true;
        })
        description(nullable: true, maxSize: 65535)
        active(nullable: false)
    }
}

class CreateOrganizationCommand {
    String uri
    String identifier
    String name
    String responder
    String email
    String phone
    String mailingAddress
    String notes


    static constraints = {
        uri(nullable: false, blank: false, maxSize: 512, validator: {val, obj, errors ->
            Organization.withTransaction { tx ->
                Organization org = Organization.findByUri(val);
                if( org ){
                    errors.rejectValue("uri", "org.uri.exists", [val] as String[], "An organizaiton with URI ${val} already exists.")
                    return false;
                }
            }
        })
        identifier(nullable: false, blank: false, maxSize: 50, validator: {val, obj, errors ->
            Organization.withTransaction { tx ->
                Organization org = Organization.findByIdentifier(val);
                if( org ){
                    errors.rejectValue("identifier", "org.name.exists", [val] as String[], "An organizaiton with identifier ${val} already exists.")
                    return false;
                }
            }
        })
        name(nullable: false, blank: false, maxSize: 255, validator: {val, obj, errors ->
            Organization.withTransaction { tx ->
                Organization org = Organization.findByName(val);
                if( org ){
                    errors.rejectValue("name", "org.name.exists", [val] as String[], "An organizaiton with name ${val} already exists.")
                    return false;
                }
            }
        })
        responder(nullable: false, blank: false, maxSize: 255)
        email(nullable: false, blank: false, maxSize: 512)
        phone(nullable: false, blank: false, maxSize: 50)
        mailingAddress(nullable: true, blank: true, maxSize: 1024)
        notes(nullable: true, blank: true, maxSize: 65535)
    }

}


class EditOrganizationCommand {
    public void setData(Organization org){
        this.id = org.id;
        this.identifier = org.identifier;
        this.uri = org.uri;
        this.name = org.name;
        this.responder = org.primaryContact?.responder;
        this.email = org.primaryContact?.email;
        this.phone = org.primaryContact?.phoneNumber;
        this.mailingAddress = org.primaryContact?.mailingAddress;
        this.notes = org.primaryContact?.notes;
    }

    Long id
    String uri
    String identifier
    String name
    String responder
    String email
    String phone
    String mailingAddress
    String notes


    static constraints = {
        uri(nullable: false, blank: false, maxSize: 512, validator: {val, obj, errors ->
            Organization.withTransaction { tx ->
                Organization org = Organization.findByUri(val);
                if( org && org.id != obj.id ){
                    errors.rejectValue("uri", "org.uri.exists", [val] as String[], "An organizaiton with URI ${val} already exists.")
                    return false;
                }
            }
        })
        identifier(nullable: false, blank: false, maxSize: 50, validator: {val, obj, errors ->
            Organization.withTransaction { tx ->
                Organization org = Organization.findByIdentifier(val);
                if( org && org.id != obj.id ){
                    errors.rejectValue("identifier", "org.name.exists", [val] as String[], "An organizaiton with identifier ${val} already exists.")
                    return false;
                }
            }
        })
        name(nullable: false, blank: false, maxSize: 255, validator: {val, obj, errors ->
            Organization.withTransaction { tx ->
                Organization org = Organization.findByName(val);
                if( org && org.id != obj.id ){
                    errors.rejectValue("name", "org.name.exists", [val] as String[], "An organizaiton with name ${val} already exists.")
                    return false;
                }
            }
        })
        responder(nullable: false, blank: false, maxSize: 255)
        email(nullable: false, blank: false, maxSize: 512)
        phone(nullable: false, blank: false, maxSize: 50)
        mailingAddress(nullable: true, blank: true, maxSize: 1024)
        notes(nullable: true, blank: true, maxSize: 65535)
    }

}

class CreateOrganizationCommentCommand {
    Organization organization;
    String name;
    String comment;

    static constraints = {
        organization(nullable: false)
        name(nullable: false, blank: false, maxSize: 256)
        comment(nullable: false, blank: false, maxSize: 65535)
    }
}
class EditOrganizationCommentCommand {
    Integer commentId;
    Organization organization;
    String name;
    String comment;

    static constraints = {
        commentId(nullable: false)
        organization(nullable: false)
        name(nullable: false, blank: false, maxSize: 256)
        comment(nullable: false, blank: false, maxSize: 65535)
    }
}
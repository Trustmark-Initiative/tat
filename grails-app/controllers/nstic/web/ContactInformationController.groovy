package nstic.web

import grails.converters.JSON
import grails.converters.XML
import grails.gorm.transactions.Transactional
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.validation.ObjectError

import javax.servlet.ServletException

@Transactional
@PreAuthorize('hasAuthority("tat-admin")')
class ContactInformationController {

    def index() {
        redirect(action: 'list')
    }

    @PreAuthorize('hasAnyAuthority("tat-contributor", "tat-admin")')
    def list(){
        log.debug("Listing contacts...")
        if (!params.max)
            params.max = '20';
        params.max = Math.min(100, Integer.parseInt(params.max)).toString(); // Limit to at most 100 users at a time.
        withFormat {
            html {
                [contacts: ContactInformation.list(params), contactsCountTotal: ContactInformation.count()]
            }
            json {
                List<ContactInformation> contacts = ContactInformation.list(params);
                List<Map> contactsJson = []
                contacts.each{ contact ->
                    contactsJson.add(contact.toJsonMap(false));
                }
                render contactsJson as JSON
            }
            xml {
                render ContactInformation.list(params) as XML
            }
        }
    }

    def create() {
        log.debug("Displaying create contact page...")
        [contactCommand: new CreateContactCommand()]
    }//end createContact

    def save(CreateContactCommand contactCommand){
        log.info("Request to save contact: ${contactCommand?.email}")
        if(!contactCommand.validate()){
            log.warn "Contact form does not validate: "
            contactCommand.errors.getAllErrors().each { ObjectError error ->
                log.warn "    ${error.defaultMessage}"
            }
            return render(view: 'create', model: [contactCommand: contactCommand])
        }

        log.info "Saving contact: ${contactCommand.email}"
        ContactInformation contactInformation = new ContactInformation()
        contactInformation.responder = contactCommand.responder
        contactInformation.email = contactCommand.email
        contactInformation.mailingAddress = contactCommand.mailingAddress
        contactInformation.phoneNumber = contactCommand.phone
        contactInformation.notes = contactCommand.notes
        contactInformation.save(failOnError: true, flush: true)

        flash.message = "Successfully created contact '${contactInformation.responder ?: contactInformation.email}'"

        return redirect(action:'list');
    }//end save()

    def edit() {
        log.info("Editing contact ${params.contactId}")
        if( !params.contactId ){
            throw new ServletException("Missing required field: contactId")
        }

        ContactInformation contact = ContactInformation.findById(params.contactId);
        if( !contact )
            throw new ServletException("No such contact: ${params.contactId}")

        EditContactCommand command = new EditContactCommand()
        command.setData(contact);
        [contactCommand: command]
    }

    def update(EditContactCommand contactCommand){
        log.info("Updating contact @|cyan ${contactCommand.email}|@...")

        if(!contactCommand.validate()){
            log.warn "Contact edit form does not validate: "
            contactCommand.errors.getAllErrors().each { ObjectError error ->
                log.warn "    ${error.defaultMessage}"
            }
            return render(view: 'edit', model: [contactCommand: contactCommand])
        }

        ContactInformation contact = ContactInformation.findById(contactCommand.id);
        if (!contact)
            throw new ServletException("Unable to find contact ${contactCommand.id}")

        contact.email = contactCommand.email
        contact.responder = contactCommand.responder
        contact.phoneNumber = contactCommand.phone
        contact.mailingAddress = contactCommand.mailingAddress
        contact.notes = contactCommand.notes
        contact.save(failOnError: true, flush: true);

        flash.message = "Successfully updated contact '${contact.responder ?: contact.email}'"
        return redirect(action:'list');
    }

    def view() {
        log.info("Viewing contact ${params.id}...")

        ContactInformation contact = ContactInformation.findById(params.id);
        if( !contact )
            throw new ServletException("No such contact: ${params.id}")

        // TODO Multiple formats...
        [contact: contact]
    }//end view()



    def typeahead() {
        User user = User.findByUsername(((OAuth2AuthenticationToken) SecurityContextHolder.getContext().getAuthentication()).getName())
        log.debug("User[@|blue ${user}|@] searching[@|cyan ${params.q}|@] via ContactInformation typeahead...")

        if( params.org )
            log.debug("User has sent an organization also: @|blue ${params.org}|@")

        def criteria = ContactInformation.createCriteria();
        def results = criteria {
            or {
                like("email", '%'+params.q+'%')
            }
            maxResults(25)
            order("email", "asc")
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
                    def resultJSON = [
                            id: result.id,
                            response: result.responder ?: "",
                            email: result.email ?: "",
                            phoneNumber: result.phoneNumber ?: "",
                            mailingAddress: result.mailingAddress ?: ""
                    ]
                    resultsJSON.add(resultJSON);

                }
                render resultsJSON as JSON
            }
        }

    }//end typeahead
}

class CreateContactCommand {
    String responder
    String email
    String phone
    String mailingAddress
    String notes

    static constraints = {
        responder(nullable: true, blank: true, maxSize: 255)
        email(nullable: false, blank: false, maxSize: 512)
        phone(nullable: true, blank: true, maxSize: 50)
        mailingAddress(nullable: true, blank: true, maxSize: 1024)
        notes(nullable: true, blank: true, maxSize: 65535)
    }
}

class EditContactCommand {
    public void setData(ContactInformation contact){
        this.id = contact.id
        this.responder = contact.responder
        this.email = contact.email
        this.phone = contact.phoneNumber
        this.mailingAddress = contact.mailingAddress
        this.notes = contact.notes
    }

    Long id
    String responder
    String email
    String phone
    String mailingAddress
    String notes

    static constraints = {
        id(nullable: false)
        responder(nullable: true, blank: true, maxSize: 255)
        email(nullable: false, blank: false, maxSize: 512)
        phone(nullable: true, blank: true, maxSize: 50)
        mailingAddress(nullable: true, blank: true, maxSize: 1024)
        notes(nullable: true, blank: true, maxSize: 65535)
    }
}

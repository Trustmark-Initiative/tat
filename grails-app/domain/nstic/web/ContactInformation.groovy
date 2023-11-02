package nstic.web

/**
 * Represents a contact.  Does not need to be a person, could be something else like an email address or a phone number.
 */
class ContactInformation {

    int id
    String email
    String responder
    String phoneNumber
    String mailingAddress
    String notes

    static constraints = {
        email(email: true, nullable: false, blank: false, maxSize: 255)
        responder(nullable: true, blank: true, maxSize: 512)
        phoneNumber(nullable: true, blank: true, maxSize: 32)
        mailingAddress(nullable: true, blank: true, maxSize: 1024)
        notes(nullable: true, blank: true, maxSize: 65535)
    }

    static mapping = {
        table(name: 'contact_information')
        mailingAddress(type: 'text', column: 'mailing_address')
        notes(type: 'text', column: 'notes')
    }

    ContactInformation saveAndFlushHelper() {
        withTransaction {
            save(flush: true, failOnError: true)
        }
    }

    public String toString() {
        return "Contact[$email]"
    }


    public Map toJsonMap(boolean shallow = true) {
        def json = [
                id: this.id,
                email: this.email ?: "",
                responder: this.responder ?: "",
                phoneNumber: this.phoneNumber ?: "",
                mailingAddress: this.mailingAddress ?: "",
                notes: this.notes ?: ""
        ]
        return json;
    }

    public boolean equals(Object other){
        if( other instanceof ContactInformation )
            return this.id == ((ContactInformation) other).id;
        return false;
    }

    public int hashCode(){return this.id;}



}//end ContactInformation

package nstic.web

/**
 * Represents a temporary marker for when a contact information object is "granted" permission to see reports.  When that
 * happens, a row is added to this table.  When the user clicks the emailed unique id, then the row is removed and
 * they have created a "real" user account, allowing them to view reports, etc.
 */
class ContactGrant {

    /**
     * A unique identifier for this grant, sent to the user. (Ie, they click a link with this grant id and can promote
     * themselves into a user account).
     */
    String grantId;
    /**
     * The contact information to grant to regular user status (when the email link is clicked, verifying the email).
     */
    ContactInformation contactInformation;
    /**
     * The organization associated with the contact information.
     */
    Organization organization;
    /**
     * When this grant was created (Auto-populated)
     */
    Date dateCreated;
    /**
     * Who created this grant.
     */
    User createdBy;

    static constraints = {
        grantId(nullable: false, blank: false, maxSize: 64, unique: true)
        contactInformation(nullable: false)
        organization(nullable: false)
        dateCreated(nullable: true)
        createdBy(nullable: false)
    }
}

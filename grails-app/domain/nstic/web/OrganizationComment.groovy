package nstic.web

/**
 * NSTICTOOLS-237, Represents a comment made by a user on an organization - meant to track high level
 * information, like why assessments cannot be performed or do not make sense.
 */
class OrganizationComment {

    static belongsTo = [organization: Organization]

    String title
    String comment
    User user
    Date dateCreated

    static constraints = {
        organization(nullable: false)
        title(nullable: false, blank: false, maxSize: 256)
        comment(nullable: true, maxSize: 65535)
        user(nullable: false)
        dateCreated(nullable: true)
    }

    static mapping = {
        user(column: 'user_ref')
        title(column: 'title')
        comment(type:'text', column: 'comment')
    }


}

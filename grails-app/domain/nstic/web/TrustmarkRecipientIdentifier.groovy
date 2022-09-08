package nstic.web

class TrustmarkRecipientIdentifier {

    static belongsTo = [organization: Organization]

    String  uri
    Boolean defaultTrustmarkRecipientIdentifier = Boolean.FALSE

    static constraints = {
        uri                                 nullable: false
        defaultTrustmarkRecipientIdentifier nullable: false
    }

    static mapping = {
        table name: 'trustmark_recipient_identifier'
        uri column: 'uri'
        defaultTrustmarkRecipientIdentifier column: 'default_trustmark_recipient_identifier'
    }
}

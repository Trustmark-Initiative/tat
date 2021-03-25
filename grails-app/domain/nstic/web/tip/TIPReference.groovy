package nstic.web.tip

import nstic.web.td.TrustmarkDefinition

/**
 * Each TIP is a collection of references.  This class encapsulates the references found in a TIP.
 */
class TIPReference {

    static belongsTo = [
        owningTIP: TrustInteroperabilityProfile
    ]

    TrustmarkDefinition trustmarkDefinition;
    TrustInteroperabilityProfile trustInteroperabilityProfile;

    /**
     * The sequence number of this tip reference
     */
    Integer number
    /**
     * The Identifier assigned to this reference in the TIP.
     */
    String referenceName;

    /**
     * Holds notes about this TD
     */
    String notes;


    static constraints = {
        owningTIP(nullable: false)
        referenceName(nullable: false, blank: false, maxSize: 512)
        trustmarkDefinition(nullable: true)
        trustInteroperabilityProfile(nullable: true)
        notes(nullable: true, blank: true, maxSize: 65535)
    }

    static mapping = {
        table(name: 'tip_reference')
        owningTIP(column: 'owning_tip_ref')
        number(column: 'tip_sequence')
        trustmarkDefinition(column: 'td_ref')
        trustInteroperabilityProfile(column: 'tip_ref')
        notes(type: 'text')
    }
}
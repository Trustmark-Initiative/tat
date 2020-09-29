package nstic.web.assessment

import nstic.web.td.TrustmarkDefinition
import nstic.web.tip.TrustInteroperabilityProfile;

/**
 * Represents a link between an Assessment, and a Trustmark Definition which is being assessed.
 * <br/><br/>
 * Created by brad on 4/18/16.
 */
public class AssessmentTrustmarkDefinitionLink {

    static belongsTo = [
            trustmarkDefinition: TrustmarkDefinition,
            assessment: Assessment
    ]

    /**
     * TrustmarkDefinitions are assessed in a particular order, which is preserved here.
     */
    int index;

    /**
     * If this link is due to a TD being included in a TIP Tree somewhere, then the original TIP is captured in this
     * field.
     */
    TrustInteroperabilityProfile fromTip;


    static constraints = {
        trustmarkDefinition(nullable: false)
        assessment(nullable: false)
        index(nullable: false)
        fromTip(nullable: true)
    }

    static mapping = {
        table(name: 'assessment_td_link')
        trustmarkDefinition(column: 'td_ref')
        assessment(column: 'assessment_ref')
        fromTip(column: 'from_tip_ref')
        index(column: 'list_index')
    }


}/* end AssessmentTrustmarkDefinitionLink */
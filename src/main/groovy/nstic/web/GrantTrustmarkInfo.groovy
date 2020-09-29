package nstic.web

import nstic.web.assessment.Assessment
import nstic.web.assessment.AssessmentStepData
import nstic.web.td.AssessmentStep
import nstic.web.td.TrustmarkDefinition

/**
 * Holds information for the view about a TD from an assessment.  Used on the Grant Trustmarks page to show the info
 * about a single Trustmark and whether it should be granted or not.
 * <br/><br/>
 * Created by brad on 5/4/16.
 */
class GrantTrustmarkInfo {

    public GrantTrustmarkInfo(Assessment assessment, TrustmarkDefinition td){
        this.assessment = assessment;
        this.td = td;
    }

    Assessment assessment;
    List<AssessmentStepData> steps;
    TrustmarkDefinition td;
    List<AssessmentStep> stepsWithNoAnswer = []
    List requiredArtifactProblems = []
    List requiredParameterProblems = []
    Boolean issuanceCriteriaSatisfied = Boolean.FALSE;
    Boolean issuanceCriteriaError = Boolean.FALSE;
    String issuanceCriteriaErrorText;


    public Boolean shouldGrant() {
        return stepsWithNoAnswer.isEmpty() && requiredArtifactProblems.isEmpty() && requiredParameterProblems.isEmpty() && issuanceCriteriaSatisfied;
    }

}

package nstic.util

import edu.gatech.gtri.trustmark.v1_0.model.AssessmentStepResult
import edu.gatech.gtri.trustmark.v1_0.model.AssessmentStepResultType

/**
 * Created by brad on 5/4/16.
 */
class AssessmentStepResultImpl implements AssessmentStepResult {

    public AssessmentStepResultImpl(String id, Integer number, nstic.web.assessment.AssessmentStepResult result){
        this.assessmentStepId = id;
        this.assessmentStepNumber = number;
        if( result == nstic.web.assessment.AssessmentStepResult.Not_Known ){
            this.result = AssessmentStepResultType.UNKNOWN;
        }else if( result == nstic.web.assessment.AssessmentStepResult.Not_Applicable ) {
            this.result = AssessmentStepResultType.NA;
        }else if( result == nstic.web.assessment.AssessmentStepResult.Not_Satisfied ) {
            this.result = AssessmentStepResultType.NO;
        }else if( result == nstic.web.assessment.AssessmentStepResult.Satisfied ) {
            this.result = AssessmentStepResultType.YES;
        }else{
            throw new UnsupportedOperationException("Invalid value given for result: "+result);
        }
    }
    public AssessmentStepResultImpl(String id, Integer number, AssessmentStepResultType result){
        this.assessmentStepId = id;
        this.assessmentStepNumber = number;
        this.result = result;
    }

    String assessmentStepId;
    Integer assessmentStepNumber;
    AssessmentStepResultType result;

    String getAssessmentStepId() {
        return assessmentStepId
    }

    void setAssessmentStepId(String assessmentStepId) {
        this.assessmentStepId = assessmentStepId
    }

    Integer getAssessmentStepNumber() {
        return assessmentStepNumber
    }

    void setAssessmentStepNumber(Integer assessmentStepNumber) {
        this.assessmentStepNumber = assessmentStepNumber
    }

    AssessmentStepResultType getResult() {
        return result
    }

    void setResult(AssessmentStepResultType result) {
        this.result = result
    }

    public String toString(){
        return "AssStepResult[${assessmentStepNumber}:${assessmentStepId} = ${result}]";
    }

}

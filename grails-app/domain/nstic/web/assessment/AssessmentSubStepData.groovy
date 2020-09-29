package nstic.web.assessment

import nstic.web.User
import nstic.web.td.AssessmentStep
import nstic.web.td.AssessmentStepArtifact
import nstic.web.td.AssessmentSubStep

/**
 * In the event that an AssessmentStep has failed, the user has the ability to create AssessmentSubStep objects on the
 * original AssessmentStep, to specify what, if anything, passed.  This database object will contain the results of those
 * substeps.
 */
class AssessmentSubStepData {

    static belongsTo = [
        assessmentStepData: AssessmentStepData
    ]

    AssessmentSubStep substep;
    AssessmentStepResult result = AssessmentStepResult.Not_Known;
    /**
     * User who last changed the result for this step.
     */
    User lastResultUser = null;
    /**
     * When the result was last changed.
     */
    Date resultLastChangeDate = null;

    Date lastUpdated; // Date of last update.

    String assessorComment;
    /**
     * Person who made the last assessor comment (may be null).
     */
    User assessorCommentUser;
    /**
     * Last time the comment was modified.
     */
    Date lastCommentDate;


    static constraints = {
        assessmentStepData(nullable: false)
        substep(nullable: false)
        result(nullable: false)
        lastUpdated(nullable: true)
        assessorComment(nullable: true, blank: true, maxSize: 65535)
        lastResultUser(nullable: true)
        resultLastChangeDate(nullable: true)
        assessorCommentUser(nullable: true)
        lastCommentDate(nullable: true)
    }

    static mapping = {
        table(name:'assessment_sub_step_data')
        assessmentStepData(column: 'assessment_step_data_ref')
        substep(column: 'td_assessment_sub_step_ref')
        assessorComment(type:'text', column: 'assessor_comment')
    }

    //==================================================================================================================
    //  Helper Methods
    //==================================================================================================================



}//end AssessmentStepData

package nstic.web.assessment

import nstic.web.User
import nstic.web.td.AssessmentStep
import nstic.web.td.AssessmentStepArtifact
import nstic.web.td.AssessmentSubStep
import nstic.web.td.TdParameter

/**
 * Tied to a step, the step data is the container for the results, comments and artifacts of the step's information.
 */
class AssessmentStepData {

    static transients = [
        'completelySatisfied',
        'hasRequiredAttachments',
        'hasRequiredParameters',
        'areAllAttachmentsSatisfied',
        'areAllRequiredParametersFilled',
        'firstUnfilledRequiredParameter',
        'shortenedComment'
    ]

    static belongsTo = [
        assessment: Assessment
    ]

    AssessmentStep step;
    AssessmentStepResult result = AssessmentStepResult.Not_Known;
    /**
     * User who last changed the result for this step.
     */
    User lastResultUser = null;
    /**
     * When the result was last changed.
     */
    Date resultLastChangeDate = null;

    /*
     * If unsatisfied, these boolean checkboxes indicate why.
     */
    /**
     * Organization claims non-conformance and has not provided evidence
     */
    Boolean orgClaimsNonConformance = Boolean.FALSE;
    /**
     * Collected evidence indicates non-conformance to assessment criteria
     */
    Boolean evidenceIndicatesNonConformance = Boolean.FALSE;
    /**
     * Collected evidence indicates partial, but not full, conformance to assessment criteria
     */
    Boolean evidenceIndicatesPartialConformance = Boolean.FALSE;
    /**
     * Organization claims conformance but cannot provide sufficient evidence of conformance
     */
    Boolean orgCannotProvideSufficientEvidence = Boolean.FALSE;

    /**
     * Person who last modified a failure reason checkbox, ie, one of: orgClaimsNonConformance, evidenceIndicatesNonConformance,
     *  evidenceIndicatesPartialConformance or orgCannotProvideSufficientEvidence
     */
    User lastCheckboxUser = null;

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

    static hasMany = [
        artifacts: ArtifactData,
        substeps: AssessmentSubStepData,
        parameterValues: ParameterValue
    ]

    static constraints = {
        assessment(nullable: false)
        step(nullable: false)
        result(nullable: false)
        lastUpdated(nullable: true)
        assessorComment(nullable: true, blank: true, maxSize: 65535)
        artifacts(nullable: true)
        orgClaimsNonConformance(nullable: true)
        evidenceIndicatesNonConformance(nullable: true)
        evidenceIndicatesPartialConformance(nullable: true)
        orgCannotProvideSufficientEvidence(nullable: true)
        lastResultUser(nullable: true)
        resultLastChangeDate(nullable: true)
        lastCheckboxUser(nullable: true)
        assessorCommentUser(nullable: true)
        lastCommentDate(nullable: true)
        parameterValues(nullable: true)
    }

    static mapping = {
        table(name:'assessment_step_data')
        assessment(column: 'assessment_ref')
        step(column: 'td_assessment_step_ref')
        assessorComment(type:'text', column: 'assessor_comment')

        orgClaimsNonConformance(column: 'ORG_CLAIMS_NON_CONFORMANCE')
        evidenceIndicatesNonConformance(column: 'EVIDENCE_INDICATES_NON_CONFORMANCE')
        evidenceIndicatesPartialConformance(column: 'EVIDENCE_INDICATES_PARTIAL_CONFORMANCE')
        orgCannotProvideSufficientEvidence(column: 'ORG_CANNOT_PROVIDE_SUFFICIENT_EVIDENCE')

    }

    //==================================================================================================================
    //  Helper Methods
    //==================================================================================================================
    public Boolean getCompletelySatisfied() {
        return result && result == AssessmentStepResult.Satisfied && areAllAttachmentsSatisfied() && areAllRequiredParametersFilled()
    }//end isCompletelySatisfied()

    public Boolean getHasRequiredAttachments() {
        return this.step.artifacts?.size() > 0;
    }

    public Boolean getAreAllAttachmentsSatisfied() {
        if( this.result == AssessmentStepResult.Not_Applicable )
            return true;

        Map<ArtifactData, Boolean> satisifedMap = [:];
        this.step.artifacts.each{ AssessmentStepArtifact artifactDefinition ->
            satisifedMap.put(artifactDefinition.id, Boolean.FALSE);
            this.artifacts.each { ArtifactData artifact ->
                if( artifact.requiredArtifact?.equals(artifactDefinition) ){
                    satisifedMap.put(artifactDefinition.id, Boolean.TRUE);
                }
            }
        }
        boolean attachmentsAllSatisfied = true;
        for(Boolean nextValue : satisifedMap.values() ){
            if( nextValue == Boolean.FALSE ) {
                attachmentsAllSatisfied = false;
                break;
            }
        }
        return attachmentsAllSatisfied;
    }

    public Boolean getHasRequiredParameters() {
        return this.step.parameters.required.any()
    }

    public TdParameter getFirstUnfilledRequiredParameter() {
        if (this.result != AssessmentStepResult.Not_Applicable) {
            for (TdParameter parameter : this.step.parameters) {
                if (parameter.required && !this.isParameterFilled(parameter)) {
                    return parameter;
                }
            }
        }
        return null
    }

    public Boolean isParameterFilled(TdParameter parameter) {
        ParameterValue paramValue = ParameterValue.findByStepDataAndParameter(this, parameter)
        // TODO: is an empty STRING value ever okay?
        return paramValue?.userValue?.length();
    }

    public Boolean getAreAllRequiredParametersFilled() {
        return this.getFirstUnfilledRequiredParameter() == null
    }

    /**
     * Knows how to return a shortened comment for the purposes of display (As opposed to the whole damn thing)
     */
    public String getShortenedComment() {
        int maxSize = 250;
        if( this.assessorComment ){
            if( this.assessorComment.length() > maxSize ){
                return this.assessorComment.substring(0, maxSize) + "..."
            }else{
                return this.assessorComment;
            }
        }else{
            return null;
        }
    }//end getShortenedComment()


    public Map toJsonMap(boolean shallow = false) {
        def json = [
                id: this.id,
                result: this.result?.toString(),
                lastUpdated: this.lastUpdated?.getTime(),
                comment: this.assessorComment,
                assessment: [
                        id: this.assessment.id,
                        status: this.assessment.status?.toString()
                ],
                step: this.step.toJsonMap(shallow)
        ]
        if( !this.artifacts.isEmpty() ){
            def jsonList = []
            this.artifacts.each{ artifact ->
                def artifactJson = artifact.toJsonMap(shallow);
                jsonList.add(artifactJson);
            }
            json.put("artifacts", jsonList);
        }

        if( this.step.substeps && !this.step.substeps.isEmpty() ){
            def substepList = []
            for( AssessmentSubStep substep : this.step.substeps ) {
                def substepJson = substep.toJsonMap(false);
                AssessmentSubStepData substepData = null;
                this.substeps.each { AssessmentSubStepData currentSubstepData ->
                    if( currentSubstepData.substep.id.equals(substep.id) ){
                        substepData = currentSubstepData;
                    }
                }
                if( substepData ){
                    substepJson.put("substepDataId", substepData.id);
                    substepJson.put("result", substepData.result?.toString() ?: AssessmentStepResult.Not_Known.toString());
                    substepJson.put("resultUser", substepData.lastResultUser?.toJsonMap(true) ?: null);
                    substepJson.put("assessorComment", substepData.assessorComment ?: "");
                    substepJson.put("assessorCommentUser", substepData.assessorCommentUser?.toJsonMap(true) ?: null);
                }else{
                    substepJson.put("substepDataId", -1);
                }

                substepList.add( substepJson );
            }
            Collections.sort(substepList, {ss1, ss2 -> return ss1.id.compareTo(ss2.id); } as Comparator);
            json.put("substeps", substepList);
        }

        return json;
    }

    /**
     * Copies the data contained in the other, but only immediate data (data which would impact the table for this object)
     * does not copy references.
     */
    public void setImmediateData(AssessmentStepData that){
        this.result = that.result;
        this.lastResultUser = that.lastResultUser;
        this.resultLastChangeDate = that.resultLastChangeDate;
        this.orgClaimsNonConformance = that.orgClaimsNonConformance;
        this.evidenceIndicatesNonConformance = that.evidenceIndicatesNonConformance;
        this.evidenceIndicatesPartialConformance = that.evidenceIndicatesPartialConformance;
        this.orgCannotProvideSufficientEvidence = that.orgCannotProvideSufficientEvidence;
        this.lastCheckboxUser = that.lastCheckboxUser;
        this.lastUpdated = that.lastUpdated;
        this.assessorComment = that.assessorComment;
        this.assessorCommentUser = that.assessorCommentUser;
        this.lastCommentDate = that.lastCommentDate;
    }//end setData()


}//end AssessmentStepData

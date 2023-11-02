package nstic.web.assessment

import nstic.web.BinaryObject
import nstic.web.ContactInformation
import nstic.web.Organization
import nstic.web.User
import nstic.web.td.TrustmarkDefinition
import org.json.JSONArray
import org.json.JSONObject
import org.springframework.util.StringUtils

import java.util.stream.Collectors
import java.util.stream.IntStream

/**
 * Represents an actual assessment performed on a trustmark.
 */
class Assessment {

    static transients = ['sortedTds', 'sortedTdsByName', 'sortedTdsByTip','sortedSteps', 'uploadedBinaries', 'isComplete', 'percentStepsSatisfied', 'percentStepsUnsatisfied', 'lastGlobalCommentWithAuthor', 'lastAssessorFromLogEntry']

    static belongsTo = [
            createdBy: User                            // User who performed the trustmark assessment (or at least started it)
    ]

    String assessmentName              // User-provided name for this assessment
    Organization assessedOrganization  // Organization being assessed
    ContactInformation assessedContact // Person or entity being assessed.

    Date dateCreated // Date this assessment was started
    AssessmentStatus status = AssessmentStatus.CREATED
    User statusLastChangeUser
    Date statusLastChangeDate

    AssessmentLog logg

    User assignedTo // The user who will (or is) perform the assessment.
    User lastAssessor // The person who was most recently assigned to this assessment.

    String comment = null // Some information entered about this assessment, such as why it is pending, etc.
    User commentLastChangeUser = null
    Date commentLastChangeDate = null

    /**
     * When an assessment is created, it is created against TDs and potentially one or more TIPs.  This field will hold
     * that source data for future use.  Note that it is a nstic.web.CreateAssessmentTdsAndTips object encoded as JSON.
     */
    String tdsAndTipsJSON

    static hasMany = [
        steps: AssessmentStepData,
        tdLinks: AssessmentTrustmarkDefinitionLink
    ]

    static constraints = {
        createdBy(nullable: false)
        tdLinks(nullable: true)
        assessmentName(blank: false)
        assessedOrganization(nullable: false)
        assessedContact(nullable: true)
        dateCreated(nullable: true)
        status(nullable: false)
        logg(nullable: true)
        assignedTo(nullable: true)
        comment(nullable: true, blank: true, maxSize: 65535)
        steps(nullable: true)
        statusLastChangeUser(nullable: true)
        statusLastChangeDate(nullable: true)
        commentLastChangeUser(nullable: true)
        commentLastChangeDate(nullable: true)
        lastAssessor(nullable: true)
        tdsAndTipsJSON(nullable: true, blank: true, maxSize: Integer.MAX_VALUE)
    }

    static mapping = {
        table(name:'assessment')
        createdBy(column: 'created_by_ref')
        assessmentName(column: 'name')
        assessedOrganization(column: 'organization_ref')
        assessedContact(column: 'contact_information_ref')
        assignedTo(column: 'assigned_to_ref')
        status(column: 'status')
        logg(column: 'assessment_log_ref')
        comment(type: 'text', column: 'result_comment')
        statusLastChangeUser(column: 'status_last_change_user_ref')
        commentLastChangeUser(column: 'comment_last_change_user_ref')
        lastAssessor(column: 'last_assessor_ref')
        tdsAndTipsJSON(column: 'tds_and_tips_json', type: 'text')
    }

    List<AssessmentTrustmarkDefinitionLink> getSortedTds(){
        List<AssessmentTrustmarkDefinitionLink> tds = []
        tds.addAll(this.tdLinks)
        Collections.sort(tds, {link1, link2 -> return link1.index.compareTo(link2.index) } as Comparator)
        return tds
    }
    List<AssessmentTrustmarkDefinitionLink> getSortedTdsByName(){
        List<AssessmentTrustmarkDefinitionLink> tds = []
        tds.addAll(this.tdLinks);
        Collections.sort(tds, {link1, link2 -> return link1.trustmarkDefinition.name.compareToIgnoreCase(link2.trustmarkDefinition.name); } as Comparator);
        return tds;
    }
    List<AssessmentTrustmarkDefinitionLink> getSortedTdsByTip(){
        List<AssessmentTrustmarkDefinitionLink> tds = []
        tds.addAll(this.tdLinks);

        // get the TD uris in original import order
        JSONObject jsonObject = new JSONObject(this.tdsAndTipsJSON);
        JSONArray jsonArray = (JSONArray)jsonObject.get("tips");
        List<String> tdUris =IntStream.range(0, jsonArray.length())
                .mapToObj(jsonArray::getJSONObject)
                .flatMap(tip -> {
                    JSONArray stringArray = tip.getJSONArray("tdUris");
                    return IntStream.range(0, stringArray.length())
                            .mapToObj(stringArray::getString);
                })
                .collect(Collectors.toList());

        // Sort the TDs based on the original TD ordering which was also ordered by the TIPs original order
        Collections.sort(tds, Comparator.comparing(td -> tdUris.indexOf(((AssessmentTrustmarkDefinitionLink)td).trustmarkDefinition?.uri)))

        return tds;
    }

    /**
     * Returns all steps from this Assessment sorted in the proper order.
     */
    List<AssessmentStepData> getSortedSteps() {
        List<AssessmentStepData> sortedSteps = []
        List<AssessmentTrustmarkDefinitionLink> tdList = getSortedTdsByTip()
        List<AssessmentStepData> steps = getSortedStepData()
        for (AssessmentTrustmarkDefinitionLink tdLink : tdList)  {
            for(AssessmentStepData stepData : steps)  {
                if(stepData.step.trustmarkDefinition.id == tdLink.trustmarkDefinition.id)  {
                    sortedSteps.add(stepData)
                }
            }
        }

        return sortedSteps
    }

    /**
     * Returns all steps from this Assessment sorted in the proper order.
     *
     */
    List<AssessmentStepData> getSortedStepData() {
        List<AssessmentStepData> sortedSteps = []
        if( this.steps ) {
            sortedSteps.addAll(this.steps);
        }
        Collections.sort(sortedSteps, {AssessmentStepData step1, AssessmentStepData step2 ->
            return step1.step.id.compareTo(step2.step.id);
        } as Comparator);
        return sortedSteps;
    }

    /**
     * given the index of a step, returns that step.
     */
    AssessmentStepData getStepDataByNumber( Integer stepNum ){
        AssessmentStepData stepData = null;
        for( AssessmentStepData current : this.steps ){
            if( current.step.stepNumber == stepNum ){
                stepData = current;
                break;
            }
        }
        return stepData;
    }//end getStepDataByNumber()

    String getTipNameByStep(AssessmentStepData step)  {
        for (AssessmentTrustmarkDefinitionLink tdLink : this.tdLinks)  {
            if(step.step.trustmarkDefinition.id == tdLink.trustmarkDefinition.id)  {
                if(tdLink.fromTip != null)  {
                    return tdLink.fromTip.name
                }
            }
        }
        return ""
    }

    /**
     * Returns the list of uploaded binaries for this assessment.
     */
    List<BinaryObject> getUploadedBinaries() {
        def existingArtifacts = []
        this.steps?.each{ AssessmentStepData curStepData ->
            curStepData.artifacts.each { ArtifactData curStepArtifact ->
                if( curStepArtifact.data && !existingArtifacts.contains(curStepArtifact.data)){
                    existingArtifacts.add( curStepArtifact.data );
                }
            }
        }
        // TODO Sort them?
        return existingArtifacts;
    }
    public List<ArtifactData> getAllArtifacts() {
        def existingArtifacts = []
        this.steps?.each{ AssessmentStepData curStepData ->
            curStepData.artifacts.each { ArtifactData curStepArtifact ->
                if( !existingArtifacts.contains(curStepArtifact)){
                    existingArtifacts.add( curStepArtifact );
                }
            }
        }
        Collections.sort(existingArtifacts, { ArtifactData art1, ArtifactData art2 ->
            return art1.getDisplayName()?.compareToIgnoreCase(art2.getDisplayName()) ?: -1;
        } as Comparator);
        return existingArtifacts;
    }


    public List<AssessmentStepData> getStepListByTrustmarkDefinition(TrustmarkDefinition td){
        List<AssessmentStepData> stepDataList = []
        for( AssessmentStepData stepData : this.getSortedSteps() ){
            if( stepData.step.trustmarkDefinition.id == td.id ){
                stepDataList.add(stepData);
            }
        }
        Collections.sort(stepDataList, {s1, s2 -> return s1.step.stepNumber.compareTo(s2.step.stepNumber); } as Comparator);
        return stepDataList;
    }


    /**
     * Returns true if this assessment is in one of the completed states, ie, completely finished.
     */
    public Boolean getIsComplete() {
        return this.status == AssessmentStatus.ABORTED ||
                this.status == AssessmentStatus.FAILED ||
                this.status == AssessmentStatus.SUCCESS;
    }//end isComplete

    /**
     * Returns a number between 0-100 which is the percent of assessment steps that are satisfied.
     * Note that the number may be odd due to steps not applicable, and that this number may not make any sense
     * depending on the Trustmark Definitions issuance criteria.
     */
    public Integer getPercentStepsSatisfied(){
        Set<AssessmentStepData> stepDataSet = this.steps;
        Integer total = stepDataSet.size();
        Integer satisfied = 0;
        stepDataSet.each{ AssessmentStepData stepData ->
            if( stepData.result.result == AssessmentStepResult.Satisfied )
                satisfied++;
            else if( stepData.result.result == AssessmentStepResult.Not_Applicable )
                total--;
        }

        double percentD = ((double) satisfied) / ((double) total);
        percentD *= 100.0d;
        return (int) percentD;
    }//end getPercentStepsSatisfied()

    /**
     * Returns a number between 0-100 which is the percent of assessment steps that are not satisfied (ie, not satisifed or not known).
     * Note that the number may be odd due to steps not applicable, and that this number may not make any sense
     * depending on the Trustmark Definitions issuance criteria.
     */
    public Integer getPercentStepsUnsatisfied(){
        Set<AssessmentStepData> stepDataSet = this.steps;
        Integer total = stepDataSet.size();
        Integer unsatisfied = 0;
        stepDataSet.each{ AssessmentStepData stepData ->
            if( stepData.result == null || stepData.result.result == AssessmentStepResult.Not_Satisfied || stepData.result.result == AssessmentStepResult.Not_Known )
                unsatisfied++;
            else if( stepData.result.result == AssessmentStepResult.Not_Applicable )
                total--;
        }

        double percentD = ((double) unsatisfied) / ((double) total);
        percentD *= 100.0d;
        return (int) percentD;
    }//end getPercentStepsUnsatisfied()

    /**
     * Given one or more result status, will return the count of steps for that status.
     */
    public Integer getCountOfSteps( AssessmentStepResult ... results ){
        Integer count = 0;
        Set<AssessmentStepData> stepDataSet = this.steps;
        stepDataSet.each { AssessmentStepData stepData ->
            if( results.contains(stepData.result.result) )
                count++;
        }
        return count;
    }//end getCountOfSteps()

    /**
     *  Returns the global comment along with the user who authored the comment (based on log entries).
     */
    def getLastGlobalCommentWithAuthor( ){
        if(StringUtils.isEmpty(this.comment) )
            return null;
        if( commentLastChangeUser )
            return [
                    user: commentLastChangeUser,
                    comment: comment
            ]

        // This is old code for when 'commentLastChangeUser' didn't exist.  It is deprecated, and should be removed soon.
        AssessmentLogEntry lastGlobalCommentEntry = this.logg.findLastEntryByTypeMatching("Global Comment Change");
        if( lastGlobalCommentEntry ){
            def data = lastGlobalCommentEntry.getDataAsJson();
            if( data && data.user ){
                def result = [:]
                User.withTransaction {
                    result.user = User.findByUsername(data.user.username);
                }
                result.comment = data.newComment
                result.logEntry = lastGlobalCommentEntry;
                return result;
            }else{
                throw new RuntimeException("Global comment is not empty, but log entry 'Global Comment Change' has missing or bad JSON data.")
            }
        }else{
            throw new RuntimeException("Global comment is not empty, so I expected to find a log entry matching 'Global Comment Change', but couldn't.")
        }
    }//end getLastGlobalCommentWithAuthor()


    def getLastAssessorFromLogEntry(){
        AssessmentLogEntry lastAssessorChangeEntry = this.logg?.findLastEntryByTypeMatching("Assessor Change");
        if( lastAssessorChangeEntry ){
            def data = lastAssessorChangeEntry.getDataAsJson();
            if( data && data.user ) {
                def user = null;
                User.withTransaction {
                    user = User.findByUsername(data.user.username);
                }
                return user;
            }else{
                throw new RuntimeException("Last Log Entry 'Assessor Change' has missing or bad JSON data.")
            }
        }
    }



    public Map toJsonMap(boolean shallow = true) {
        def jsonData = [
                id: this.id,
                createdBy: this.createdBy?.toJsonMap(true) ?: null,
                assessmentName: this.assessmentName,
                assessedOrganization: this.assessedOrganization?.toJsonMap(shallow),
                assessedContact: this.assessedContact?.toJsonMap(shallow),
                dateCreated: dateCreated,
                lastAssessor: this.lastAssessor?.toJsonMap(true),
                status: this.status?.toString() ?: "unknown",
                logg: this.logg?.toJsonMap(shallow) ?: [id: -1],
                mostRecentEntry: this.logg?.mostRecentEntry?.dateCreated,
                title: this.logg?.mostRecentEntry?.title,
                assignedTo: this.createdBy?.toJsonMap(shallow) ?: null,
                comment: this.comment ?: "",
                commentLastChangeUser: this.commentLastChangeUser?.toJsonMap(true),
                statusLastChangeUser: this.statusLastChangeUser?.toJsonMap(true)
        ]

        def tdsJson = []
        if( this.tdLinks != null && !this.tdLinks.isEmpty() ){
            for( AssessmentTrustmarkDefinitionLink link : this.tdLinks ){
                tdsJson.add([
                        id: link.trustmarkDefinition.id,
                        identifier: link.trustmarkDefinition.uri,
                        name: link.trustmarkDefinition.name,
                        fromTip: link.fromTip?.uri
                ]);
            }
        }
        jsonData.put("trustmarkDefinitions", tdLinks);

        def sortedSteps = getSortedSteps();
        def stepsJson = []
        sortedSteps?.each{ step ->
            def stepJson = step.toJsonMap(shallow);
            stepsJson.add(stepJson);
        }
        jsonData.put("steps", stepsJson);

        if( !shallow ){
            def trustmarks = Trustmark.findAllByAssessment(this);
            def trustmarksJson = []
            trustmarks.each{ trustmark ->
                trustmarksJson.add(trustmark.toJsonMap(false));
            }
            jsonData.put("trustmarks", trustmarksJson);
        }


        return jsonData;
    }

}//end Assessment

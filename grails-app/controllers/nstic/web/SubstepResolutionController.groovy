package nstic.web

import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import grails.gorm.transactions.Transactional
import nstic.web.assessment.ArtifactData
import nstic.web.assessment.Assessment
import nstic.web.assessment.AssessmentLogEntry
import nstic.web.assessment.AssessmentStepData
import nstic.web.assessment.AssessmentStepResult
import nstic.web.assessment.AssessmentSubStepData
import nstic.web.td.AssessmentSubStep
import org.apache.commons.lang.StringUtils
import org.grails.help.ParamConversion
import org.grails.help.ParamConversions

import javax.servlet.ServletException

@Transactional
@Secured("ROLE_USER")
class SubstepResolutionController {

    def springSecurityService;

    /**
     * Loads the substep resolution index page.
     */
    def index() {
        User user = springSecurityService.currentUser;
        log.info("User[$user] is viewing the substep resolution page...");


    }//end index()

    /**
     * Uses the current logged in user's information to Determine a list of applicable un-fulfilled substeps.
     * It first checks any assessments created by this user, then finds any assessments this user has ever been assigned to.
     */
    def mylist() {
        User user = springSecurityService.currentUser;
        log.info("User[$user] is finding any substeps they may have...");

        List<Assessment> userAssessments = [];
        def createdByMe = Assessment.findAllByCreatedBy(user);
        log.debug("Found ${createdByMe.size()} assessments where $user has created them...")
        userAssessments.addAll(createdByMe);

        // Now find all where admin user has worked on them.
        def searchCriteria = AssessmentLogEntry.createCriteria();
        def entryResults = searchCriteria {
            eq("type", "Assessor Change")
            or {
                like("title", "Assigning user[${user.username}]%")
                like("title", "User ${user?.contactInformation?.responder} is now Assessing%")
            }
        }
        log.debug("User[$user] has been assigned in ${entryResults?.size()} log entries, finding those unique assessments...");
        def assignmentAssessments = []
        if( entryResults && !entryResults.isEmpty() ){
            entryResults.each{ entry ->
                def assId = entry.getDataAsJson().assessment.id;
                Assessment ass = Assessment.findById(assId);
                if( ass && !assignmentAssessments.contains(ass) ){
                    assignmentAssessments.add(ass);
                }
            }
        }
        log.debug("Assignment entries resulted in ${assignmentAssessments.size()} assessments, adding to overall list...");
        assignmentAssessments.each { Assessment ass ->
            if (!userAssessments.contains(ass) )
                userAssessments.add(ass);
        }
        Collections.sort(userAssessments, {Assessment a1, Assessment a2 ->
            return a1.id.compareTo(a2.id)
        } as Comparator);

        log.debug("Iterating the ${userAssessments?.size()} user assessments, to find any un-filled substep data...")
        List<AssessmentStepData> assessmentStepsWithUnfulfilledSubsteps = []
        List<AssessmentSubStep> unfulfilledSubsteps = []
        userAssessments.each{ Assessment ass ->
            ass.sortedSteps.each {AssessmentStepData stepData ->
                if( stepData.step.substeps && !stepData.step.substeps.isEmpty() ){
                    int diff = stepData.step.substeps.size() - stepData.substeps.size();
                    if( diff != 0 ){
                        log.debug("Assessment #${ass.id}, step ${stepData.step.stepNumber} has $diff unfulfilled substeps...")
                        for( AssessmentSubStep currentSubStep : stepData.step.substeps ){
                            if( !hasSubstepData(stepData, currentSubStep) ){
                                unfulfilledSubsteps.add(currentSubStep);
                            }
                        }
                        assessmentStepsWithUnfulfilledSubsteps.add( stepData );
                    }
                }
            }
        }

        log.debug("Formatting results...");
        withFormat {
            html {
                throw new ServletException("NOT YET SUPPORTED");
            }
            xml {
                throw new ServletException("NOT YET SUPPORTED");
            }
            json {

                Map<Long, Map> assessmentMap = [:]
                assessmentStepsWithUnfulfilledSubsteps.each{ AssessmentStepData stepData ->
                    def assessmentData = assessmentMap.get(stepData.assessment.id);
                    if( !assessmentData )
                        assessmentData = stepData.assessment.toJsonMap(true); // A shallow assessment dataset

                    List unfulfilledSubstepsJSON = (List) assessmentData.get("unfulfilledSubsteps");
                    if( !unfulfilledSubstepsJSON )
                        unfulfilledSubstepsJSON = []
                    unfulfilledSubstepsJSON.add(stepData.toJsonMap(false));
                    assessmentData.put("unfulfilledSubsteps", unfulfilledSubstepsJSON);
                    assessmentMap.put(stepData.assessment.id, assessmentData);
                }
                def assessmentJsonList = []
                assessmentMap.keySet().each{ key ->
                    assessmentJsonList.add(assessmentMap.get(key));
                }
                Collections.sort(assessmentJsonList, {a1, a2 -> return a1.id.compareTo(a2.id); } as Comparator);
                def resultsJson = [status: "Success", assessments: assessmentJsonList, assessmentsCount: assessmentJsonList?.size() ?: 0];
                render resultsJson as JSON
            }
        }

    }

    /**
     * Updates the assessor comment of a substep, creating the substep data if necessary.
     */
    @ParamConversions([
        @ParamConversion(paramName="assessmentId", toClass=Assessment.class, storeInto = "assessment"),
        @ParamConversion(paramName="stepNumber", toClass=AssessmentStepData.class, storeInto = "stepData")
    ])
    def updateAssessorComment() {
        User user = springSecurityService.currentUser;
        log.info("User[$user] updating assessor comment for Assessment[${params.assessmentId}], Step[${params.stepNumber}], Substep[${params.substepId}]")

        if( !(request.getMethod().toUpperCase() == "POST" || request.getMethod().toUpperCase() == "PUT") )
            throw new UnsupportedOperationException("Cannot retrieve the substep result this way. Only PUT or POST are supported.")

        Assessment assessment = params.assessment;
        AssessmentStepData stepData = params.stepData;
        if( !assessment || !stepData ){
            log.warn("Could not resolve assessemnt or step data!");
            throw new ServletException("Invalid assessment or step data given.");
        }

        AssessmentSubStep substep = null;
        stepData.step.substeps?.each{ cur ->
            if( cur.id.toString().equals(params.substepId) ) {
                log.debug("Found substep #${params.substepId}");
                substep = cur;
            }
        }
        if( !substep ){
            log.warn("Could not find substep!")
            throw new ServletException("No such substep");
        }

        if( StringUtils.isEmpty(params.assessorSubstepComment) )
            throw new ServletException("Missing required parameter 'assessorSubstepComment'")

        AssessmentSubStepData substepData = null;
        stepData.substeps?.each{ AssessmentSubStepData cur ->
            if( cur.substep.id.equals(substep.id) )
                substepData = cur;
        }
        if( !substepData ){
            substepData = new AssessmentSubStepData(substep: substep, assessmentStepData: stepData);
            substepData.result = AssessmentStepResult.Not_Known;
            substepData.resultLastChangeDate = Calendar.getInstance().getTime();
            substepData.lastResultUser = user;
            substepData.assessorComment = params.assessorSubstepComment;
            substepData.assessorCommentUser = user;
            substepData.lastCommentDate = Calendar.getInstance().getTime();
            substepData.save(failOnError: true);
            stepData.addToSubsteps(substepData);
            stepData.save(failOnError: true);
        }else{
            substepData.assessorComment = params.assessorSubstepComment;
            substepData.assessorCommentUser = user;
            substepData.lastCommentDate = Calendar.getInstance().getTime();
            substepData.save(failOnError: true);
        }

        assessment.logg.addEntry(
                "Edit Substep",
                "User ${user?.contactInformation?.responder} edited substep[${substep.name}] to step number ${stepData.step.stepNumber}",
                "User ${user?.contactInformation?.responder} edited substep[${substep.name}] to step number ${stepData.step.stepNumber} of assessment #${assessment.id}",
                [user      : [id: user.id, username: user.username],
                 assessment: [id: assessment.id],
                 stepData  : [id: stepData.id, step: [id: stepData.step.id, stepNumber: stepData.step.stepNumber, name: stepData.step.name]],
                 substep   : [id: substep.id, name: substep.name, description: substep.description],
                 substepData :  [id: substepData.id, result: substepData.result.toString(), assessorComment: substepData.assessorComment]
                ]
        );

        log.debug("Rendering success message...");
        withFormat {
            html { throw new UnsupportedOperationException("NOT YET IMPLEMENTED - use JSON instead"); }
            xml { throw new UnsupportedOperationException("NOT YET IMPLEMENTED - use JSON instead"); }
            json {
                def result = [
                        status: "SUCCESS",
                        message: "Successfully updated the assessor comment.",
                        comment: params.assessorSubstepComment
                ]
                render result as JSON
            }
        }
    }//end updateAssessorComment()

    /**
     * Updates the substep result of a substep, creating the substep data if necessary.
     */
    @ParamConversions([
            @ParamConversion(paramName="assessmentId", toClass=Assessment.class, storeInto = "assessment"),
            @ParamConversion(paramName="stepNumber", toClass=AssessmentStepData.class, storeInto = "stepData")
    ])
    def updateSubstepResult() {
        User user = springSecurityService.currentUser;
        log.info("User[$user] updating substep result for Assessment[${params.assessmentId}], Step[${params.stepNumber}], Substep[${params.substepId}] to ${params.result}")

        if( !(request.getMethod().toUpperCase() == "POST" || request.getMethod().toUpperCase() == "PUT") )
            throw new UnsupportedOperationException("Cannot retrieve the substep assessor comment this way. Only PUT or POST are supported.")

        Assessment assessment = params.assessment;
        AssessmentStepData stepData = params.stepData;
        if( !assessment || !stepData ){
            log.warn("Could not resolve assessment or step data!");
            throw new ServletException("Invalid assessment or step data given.");
        }

        AssessmentSubStep substep = null;
        stepData.step.substeps?.each{ cur ->
            if( cur.id.toString().equals(params.substepId) ) {
                log.debug("Found substep #${params.substepId}");
                substep = cur;
            }
        }
        if( !substep ){
            log.warn("Could not find substep!")
            throw new ServletException("No such substep");
        }

        if( StringUtils.isEmpty(params.result) )
            throw new ServletException("Missing required parameter 'result'")

        AssessmentStepResult givenResult = AssessmentStepResult.fromString(params.result);
        if( !givenResult )
            throw new ServletException("Invalid parameter 'result': ${params.result}")

        AssessmentSubStepData substepData = null;
        stepData.substeps?.each{ AssessmentSubStepData cur ->
            if( cur.substep.id.equals(substep.id) )
                substepData = cur;
        }
        if( !substepData ){
            substepData = new AssessmentSubStepData(substep: substep, assessmentStepData: stepData);
            substepData.result = givenResult;
            substepData.resultLastChangeDate = Calendar.getInstance().getTime();
            substepData.lastResultUser = user;
            substepData.assessorComment = null;
            substepData.assessorCommentUser = user;
            substepData.lastCommentDate = Calendar.getInstance().getTime();
            substepData.save(failOnError: true);
            stepData.addToSubsteps(substepData);
            stepData.save(failOnError: true);
        }else{
            substepData.result = givenResult;
            substepData.resultLastChangeDate = Calendar.getInstance().getTime();
            substepData.lastResultUser = user;
            substepData.save(failOnError: true);
        }

        assessment.logg.addEntry(
                "Edit Substep",
                "User ${user?.contactInformation?.responder} edited substep[${substep.name}] to step number ${stepData.step.stepNumber}",
                "User ${user?.contactInformation?.responder} edited substep[${substep.name}] to step number ${stepData.step.stepNumber} of assessment #${assessment.id}",
                [user      : [id: user.id, username: user.username],
                 assessment: [id: assessment.id],
                 stepData  : [id: stepData.id, step: [id: stepData.step.id, stepNumber: stepData.step.stepNumber, name: stepData.step.name]],
                 substep   : [id: substep.id, name: substep.name, description: substep.description],
                 substepData :  [id: substepData.id, result: substepData.result.toString(), assessorComment: substepData.assessorComment]
                ]
        );

        log.debug("Rendering success message...");
        withFormat {
            html { throw new UnsupportedOperationException("NOT YET IMPLEMENTED - use JSON instead"); }
            xml { throw new UnsupportedOperationException("NOT YET IMPLEMENTED - use JSON instead"); }
            json {
                def result = [
                        status: "SUCCESS",
                        message: "Successfully updated the substep result.",
                        result: givenResult.toString()
                ]
                render result as JSON
            }
        }
    }//end updateSubstepResult()





    private boolean hasSubstepData(AssessmentStepData stepData, AssessmentSubStep substep ){
        boolean hasSubstepData = false;
        for( AssessmentSubStepData subStepData : stepData.substeps ){
            if( subStepData.substep?.id?.equals(substep.id) ){
                hasSubstepData = true;
                break;
            }
        }
        return hasSubstepData;
    }//end hasSubstepdata()



}//end SubstepResolutionController
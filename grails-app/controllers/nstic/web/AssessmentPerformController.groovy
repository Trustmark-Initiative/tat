package nstic.web

import edu.gatech.gtri.trustmark.v1_0.impl.model.TrustmarkParameterBindingImpl
import edu.gatech.gtri.trustmark.v1_0.model.ParameterKind
import grails.converters.JSON
import grails.converters.XML
import grails.plugin.springsecurity.annotation.Secured
import grails.gorm.transactions.Transactional
import nstic.web.assessment.ArtifactData
import nstic.web.assessment.Assessment
import nstic.web.assessment.AssessmentStatus
import nstic.web.assessment.AssessmentStepData
import nstic.web.assessment.AssessmentStepResult
import nstic.web.assessment.AssessmentSubStepData
import nstic.web.assessment.ParameterValue
import nstic.web.td.AssessmentStepArtifact
import nstic.web.td.AssessmentSubStep
import nstic.web.td.TdParameter
import org.apache.commons.lang.StringUtils
import org.grails.help.ParamConversion
import org.grails.help.ParamConversions
import org.springframework.validation.ObjectError

import javax.servlet.ServletException

/**
 * This controller handles everything related to performing an assessment.
 */
@Transactional
@Secured("ROLE_USER")
class AssessmentPerformController {

    def springSecurityService

    /**
     * User has clicked on the "Being Assessment" button, to actually perform an assessment.
     */
    def startAssessment() {
        User user = springSecurityService.currentUser
        if(StringUtils.isEmpty(params.id)){
            log.warn("Cannot start assessment when missing id parameter")
            throw new ServletException("Missing assessment id parameter")
        }

        log.info("Start Assessment[${params.id}] for User[${user}]...")
        def assessment = Assessment.get(params.id)
        if( !assessment ){
            log.warn("Could not find assessmnet: ${params.id}")
            throw new ServletException("No such assessment ${params.id}")
        }


        if( !assessment.assignedTo ){
            log.debug("Assigning user[${user.username}] to assessment[${assessment.id}]")
            assessment.assignedTo = user
            assessment.lastAssessor = user
            assessment.save(failOnError: true, flush: true)

            assessment.logg.addEntry("Assessor Change", "Assigning user[${user.username}] to assessment[${assessment.id}]",
                    "User ${user?.contactInformation?.responder} [id: ${user?.username}] is now assigned assessment[${assessment.id}].",
                    [user: [id: user.id, username: user.username], assessment: [id: assessment.id], oldAssignee: null])

        }else if( assessment.assignedTo && assessment.assignedTo.username != user.username ){
            log.debug("Assigning user[${user.username}] to assessment[${assessment.id}], and removing assignedTo from User[${assessment.assignedTo.username}]")
            User oldAssignee = assessment.assignedTo
            assessment.assignedTo = user
            assessment.lastAssessor = user
            assessment.save(failOnError: true, flush: true)

            assessment.logg.addEntry(
                    "Assessor Change",
                    "User ${user?.contactInformation?.responder} is now Assessing Assessment ${assessment.id}",
                    "User ${user?.contactInformation?.responder} [id: ${user?.username}] is now assigned " +
                            "assessment[${assessment.id}].  Note that assessignment taken from user " +
                            "${oldAssignee?.contactInformation?.responder} [id: ${oldAssignee?.username}].",
                    [user       : [id: user.id, username: user.username],
                     assessment : [id: assessment.id],
                     oldAssignee: [id: oldAssignee.id, username: oldAssignee?.username]])

        }else{
            // It's the same user, no need to mark it as anything.
            log.debug("User[${user.username}] is already assigned to assessment[${assessment.id}]")
        }

        if( assessment.status != AssessmentStatus.IN_PROGRESS ){
            if( assessment.status == AssessmentStatus.WAITING || assessment.status == AssessmentStatus.CREATED ){
                assessment.logg.addEntry("Perform Start", "User ${user.username} Performing Assessment",
                        "User ${user.username} has started performing assessment[${assessment.id}]",
                        [user: [id: user.id, username: user.username], assessment: [id: assessment.id]])
            }

            AssessmentStatus oldStatus = assessment.status
            assessment.status = AssessmentStatus.IN_PROGRESS
            assessment.save(failOnError: true, flush: true)


            assessment.logg.addEntry("Status Change", "Assessment Status Change: ${oldStatus} -> ${AssessmentStatus.IN_PROGRESS}",
                    "User ${user.username} has changed assessment[${assessment.id}] status from ${oldStatus} to ${AssessmentStatus.IN_PROGRESS}.",
                    [user: [id: user.id, username: user.username], assessment: [id: assessment.id], oldStatus: oldStatus.toString(), newStatus: AssessmentStatus.IN_PROGRESS.toString()])
        }

        if (StringUtils.isEmpty(params.stepId)) {
            redirect(action: 'view', params: [id: assessment.id])
        }
        else {
            redirect(action: 'view', params: [id: assessment.id, stepId: params.stepId])
        }
    }//end startAssessment()

    /**
     * A debugging only method which will completely fill in an assessment with ideal answers.
     */
    def fillInAll() {
        User user = springSecurityService.currentUser
        if(StringUtils.isEmpty(params.id)){
            log.warn("Cannot view assessment performance when missing id parameter")
            throw new ServletException("Missing assessment id parameter")
        }
        def assessment = Assessment.get(params.id)
        if( !assessment ){
            log.warn("Could not find assessmnet: ${params.id}")
            throw new ServletException("No such assessment ${params.id}")
        }

        if( assessment.status != AssessmentStatus.IN_PROGRESS ){
            log.warn("Cannot view the perform assessment page if status is not IN_PROGRESS.")
            flash.error = "First click 'Perform Assessment' to begin work on this assessment.  The status is '${assessment.status}', so you can't assess it now."
            return redirect(controller: 'assessment', action: 'view', id: assessment.id)
        }

        // TODO We probably want to forward the user to a "re-assign" page instead of just throwing an error.
        if( assessment.assignedTo?.id != user?.id ){
            log.warn("User ${user?.username} cannot view the performance of assessment[${assessment.id}] without re-assigning it.")
            flash.error = "You are not the assessor for assessment ${assessment.id}.  Click 'Perform Assessment' to re-assign it to yourself."
            return redirect(controller: 'assessment', action: 'view', id: assessment.id)
        }

        int count = 0
        log.info("Filling in assessment #${assessment.id}...")
        for( AssessmentStepData stepData : assessment.getSortedSteps() ){
            log.debug("    Setting data for step: ${stepData.step.name}")
            stepData.result = AssessmentStepResult.Satisfied
            stepData.lastResultUser = user
            stepData.resultLastChangeDate = Calendar.getInstance().getTime()

            stepData.assessorComment = "Debugging result - automatically set to Satisfied"
            stepData.assessorCommentUser = user
            stepData.lastCommentDate = stepData.resultLastChangeDate

            for( AssessmentStepArtifact artifact : stepData.step.artifacts ){
                ArtifactData ad = new ArtifactData()
                ad.requiredArtifact = artifact
                ad.comment = "Test, Set by system for fillInAll()"
                ad.displayName = "TEST ONLY"
                ad.modifyingUser = user
                ad.uploadingUser = user
                ad.save(failOnError: true)

                stepData.addToArtifacts(ad)
            }

            stepData.save(failOnError: true)
            count++
        }

        log.info("Successfully set data for ${count} steps!")
        return render(text: '{"status": "SUCCESS", "count" : '+count+', "message": "Successfully filled in all data"}', contentType: 'application/json')
    }

    /**
     * Responsible for viewing an assessment's perform page.
     */
    def view() {
        User user = springSecurityService.currentUser
        if(StringUtils.isEmpty(params.id)){
            log.warn("Cannot view assessment performance when missing id parameter")
            throw new ServletException("Missing assessment id parameter")
        }
        def assessment = Assessment.get(params.id)
        if( !assessment ){
            log.warn("Could not find assessmnet: ${params.id}")
            throw new ServletException("No such assessment ${params.id}")
        }

        if( assessment.status != AssessmentStatus.IN_PROGRESS ){
            log.warn("Cannot view the perform assessment page if status is not IN_PROGRESS.")
            flash.error = "First click 'Perform Assessment' to begin work on this assessment.  The status is '${assessment.status}', so you can't assess it now."
            return redirect(controller: 'assessment', action: 'view', id: assessment.id)
        }

        // TODO We probably want to forward the user to a "re-assign" page instead of just throwing an error.
        if( assessment.assignedTo?.id != user?.id ){
            log.warn("User ${user?.username} cannot view the performance of assessment[${assessment.id}] without re-assigning it.")
            flash.error = "You are not the assessor for assessment ${assessment.id}.  Click 'Perform Assessment' to re-assign it to yourself."
            return redirect(controller: 'assessment', action: 'view', id: assessment.id)
        }


        log.info("View Performance of Assessment[${assessment.id}] for User[${user}]...")
        Map stepAttachmentStatus = [:]
        boolean allRequiredArtifactsSatisfied = true
        boolean allStepsHaveAnswer = true
        for( AssessmentStepData currentStep : assessment.sortedSteps ){
            if( !currentStep.result || currentStep.result == AssessmentStepResult.Not_Known ){
//                log.debug("Step #${currentStep.step.stepNumber} has result not_known, therefore, we cannot grant a trustmark.")
                allStepsHaveAnswer = false
            }
            boolean statusGood = true
            def foundMap = [:]
            currentStep.step.artifacts.each{ artifact ->
                foundMap.put(artifact.id, Boolean.FALSE)
            }
            currentStep.artifacts.each{ artifact ->
                if( artifact.requiredArtifact ){
                    foundMap.put(artifact.requiredArtifact.id, Boolean.TRUE)
                }
            }
            foundMap.keySet().each{ key ->
                if( foundMap.get(key) == Boolean.FALSE ){
                    statusGood = false
                    allRequiredArtifactsSatisfied = false
                }
            }
            stepAttachmentStatus.put(currentStep.id, statusGood)
        }


        // This section resolves an AssessmentStepData which is the focus of the view page.  It uses either stepId param or stepDataId param,
        //  if none are given, then the first AssessmentStepData is chosen.
        AssessmentStepData currentStep = null
        if( StringUtils.isBlank(params.stepId) && StringUtils.isBlank(params.stepDataId) ) {
            currentStep = assessment.sortedSteps.get(0); // Just grab the first one then.
        }else if (StringUtils.isNotBlank(params.stepId)) {
            log.debug("Resolving current step[${params.stepId}]...")
            currentStep = findByStepId(assessment, Long.parseLong(params.stepId))
        } else if (StringUtils.isNotBlank(params.stepDataId)) {
            currentStep = AssessmentStepData.get(Long.parseLong(params.stepDataId))
        }

        AssessmentStepData nextStep = null
        AssessmentStepData prevStep = null
        AssessmentStepData nextUnknownStep = null
        AssessmentStepData prevUnknownStep = null

        List<AssessmentStepData> sortedSteps = assessment.sortedSteps
        for( int i = 0; i < sortedSteps.size(); i++){
            AssessmentStepData aStep = sortedSteps.get(i)
            if( aStep.id == currentStep.id ){
                if( i > 0 ){
                    prevStep = sortedSteps.get(i-1)

                    for (int j = i - 1; j >= 0; j-- ) {
                        if (sortedSteps.get(j).result == AssessmentStepResult.Not_Known) {
                            prevUnknownStep = sortedSteps.get(j)
                            break
                        }
                    }
                }
                if( i < (sortedSteps.size() - 1) ){
                    nextStep = sortedSteps.get(i+1)

                    for (int j = i + 1; j < sortedSteps.size(); j++ ) {
                        if (sortedSteps.get(j).result == AssessmentStepResult.Not_Known) {
                            nextUnknownStep = sortedSteps.get(j)
                            break
                        }
                    }
                }
            }
        }

        def currentStepArtifacts = []
        for( ArtifactData artifactData : currentStep.artifacts ){
            if( artifactData.requiredArtifact == null ){
                currentStepArtifacts.add( artifactData )
            }
        }
        // TODO Sort these?  You really can't find a consistent way to sort any of this because data might be null.

        def currentStepRequiredArtifacts = []
        for( AssessmentStepArtifact requiredArtifact : currentStep.step.artifacts ){
            def matchingArtifacts = []
            for( ArtifactData artifactData : currentStep.artifacts ){
                if( artifactData.requiredArtifact?.id == requiredArtifact.id ) {
                    matchingArtifacts.add( artifactData )
                }
            }
            currentStepRequiredArtifacts.add([artifact: requiredArtifact, data: matchingArtifacts])
        }
        Collections.sort(currentStepRequiredArtifacts, {a1, a2 ->
            return a1.artifact.name.compareToIgnoreCase(a2.artifact.name)
        } as Comparator)


        [
                assessment: assessment, stepCount: assessment.steps.size(),
                currentStepData: currentStep, currentStepArtifacts: currentStepArtifacts,
                currentStepRequiredArtifacts: currentStepRequiredArtifacts,
                stepAttachmentStatus: stepAttachmentStatus,
                allRequiredArtifactsSatisfied: allRequiredArtifactsSatisfied,
                allStepsHaveAnswer: allStepsHaveAnswer,
                criteria: currentStep.step.criteria,
                nextStep: nextStep,
                prevStep: prevStep,
                nextUnknownStep: nextUnknownStep,
                prevUnknownStep: prevUnknownStep
        ]

    }//end view()

    /**
     * Sets the status of a step as one of "Success" "Failure" or "N/A".
     */
    def setStepDataStatus() {
        User user = springSecurityService.currentUser
        if(StringUtils.isEmpty(params.id)){
            log.warn("Cannot set assessment step status when missing id parameter")
            throw new ServletException("Missing assessment id parameter")
        }
        if(StringUtils.isEmpty(params.stepId)){
            log.warn("Cannot set assessment step status when missing stepId parameter")
            throw new ServletException("Missing assessment stepId parameter")
        }
        if(StringUtils.isEmpty(params.status)){
            log.warn("Cannot set assessment step status when missing status parameter")
            throw new ServletException("Missing assessment status parameter")
        }

        def assessment = Assessment.get(params.id)
        if( !assessment ){
            log.warn("Could not find assessment: ${params.id}")
            throw new ServletException("No such assessment ${params.id}")
        }

        // TODO We probably want to forward the user to a "re-assign" page instead of just throwing an error.
        if( assessment.assignedTo?.id != user?.id ){
            log.warn("User ${user?.username} cannot view the performance of assessment[${assessment.id}] without re-assigning it.")
            throw new ServletException("You are not the assessor for assessment ${assessment.id}.  Please re-assign it to yourself to continue.")
        }

        AssessmentStepData stepData = findByStepId(assessment, Long.parseLong(params.stepId))
        AssessmentStepResult newStatus = AssessmentStepResult.fromString(params.status)
        AssessmentStepResult oldStatus = stepData.result
        stepData.result = newStatus
        stepData.resultLastChangeDate = Calendar.getInstance().getTime()
        stepData.lastResultUser = user
        stepData.save(failOnError: true, flush: true)

        assessment.logg.addEntry(
                "Step Status Change",
                "User ${user?.contactInformation?.responder} set step number ${params.stepNumber} status to ${newStatus}",
                "User ${user?.contactInformation?.responder} [id: ${user?.username}] has set step number ${params.stepNumber} "+
                    "[name=${stepData.step.name}] status to ${newStatus} from ${oldStatus}.",
                [user       : [id: user.id, username: user.username],
                 assessment : [id: assessment.id],
                 stepData: [id: stepData.id, step: [id: stepData.step.id, name: stepData.step.name]],
                 oldStatus: oldStatus,
                 newStatus: newStatus]
            )


        redirect(action:'view', params: [id: assessment.id, stepId: params.stepId])
    }//end setAssessmentStepStatus()

    private AssessmentStepData findByStepId(Assessment ass, Long stepId){
        for( AssessmentStepData cur : ass.steps ){
            if( cur.step.id == stepId ){
                return cur
            }
        }
        log.warn("Could not find StepData with Step ID = ${stepId} in assessment ${ass.id} ")
        return null
    }

    /**
     * An ajax method meant to allow for posting a global comment change to an Assessment.
     */
    def changeGlobalComment() {
        User user = springSecurityService.currentUser
        Assessment assessment = Assessment.get(params.id)
        if( !assessment )
            throw new ServletException("Missing or invalid 'id' parameter")

        log.info("User[${user.username}] changing assessment[${assessment.id}] global comment to: ${params.comment}")

        String comment = params.comment?.trim() ?: ''
        String oldComment = assessment.comment
        assessment.comment = comment
        assessment.commentLastChangeDate = Calendar.getInstance().getTime()
        assessment.commentLastChangeUser = user
        assessment.save(failOnError: true, flush: true)

        assessment.logg.addEntry(
                "Global Comment Change",
                "User[${user.username}] set global comment",
                "User[${user.username}] has set global comment for assessment[${assessment.id}] to $comment",
                [user       : [id: user.id, username: user.username],
                 assessment : [id: assessment.id],
                 oldComment: oldComment,
                 newComment: comment]
        )

        log.debug("Global comment changed successfully....")
        def resultData = [status: 'SUCCESS', message: 'Successfully updated comment.']
        withFormat {
            html {
                throw new UnsupportedOperationException("NOT YET SUPPORTED - expected you would call AJAX method.")
            }
            json {
                render resultData as JSON
            }
            xml {
                render resultData as XML
            }
        }

    }//end changeGlobalComment()

    /**
     * An ajax method (similar to changeGlobalComment) which is meant to set the comment on a particular step.
     */
    def setStepDataComment() {
        User user = springSecurityService.currentUser
        if(StringUtils.isEmpty(params.id)){
            log.warn("Cannot set assessment step comment when missing id parameter")
            throw new ServletException("Missing id parameter")
        }
        if(StringUtils.isEmpty(params.stepId)){
            log.warn("Cannot set assessment step comment when missing stepId parameter")
            throw new ServletException("Missing stepId parameter")
        }
        if(StringUtils.isEmpty(params.comment)){
            params.comment = ""
        }

        def assessment = Assessment.get(params.id)
        if( !assessment ){
            log.warn("Could not find assessment: ${params.id}")
            throw new ServletException("No such assessment ${params.id}")
        }

        // TODO We probably want to forward the user to a "re-assign" page instead of just throwing an error.
        if( assessment.assignedTo?.id != user?.id ){
            log.warn("User ${user?.username} cannot view the performance of assessment[${assessment.id}] without re-assigning it.")
            throw new ServletException("You are not the assessor for assessment ${assessment.id}.  Please re-assign it to yourself to continue.")
        }

        AssessmentStepData stepData = findByStepId(assessment, Long.parseLong(params.stepId))

        log.info("User[${user.username}] changing assessment[${assessment.id}] -> Step[${params.stepId}] comment to: ${params.comment}")

        String oldComment = stepData.assessorComment
        stepData.assessorComment = params.comment
        stepData.assessorCommentUser = user
        stepData.lastCommentDate = Calendar.getInstance().getTime()
        stepData.save(failOnError: true, flush: true)

        assessment.logg.addEntry(
                "Step Comment Change",
                "User ${user?.contactInformation?.responder} set step id ${params.stepId} comment",
                "User ${user?.contactInformation?.responder} [id: ${user?.username}] has set step id ${params.stepId} "+
                        "[name=${stepData.step.name}] comment to '${params.comment}'.",
                [user       : [id: user.id, username: user.username],
                 assessment : [id: assessment.id],
                 stepData: [id: stepData.id, step: [id: stepData.step.id, name: stepData.step.name]],
                 oldComment: oldComment,
                 newComment: params.comment]
        )

        def resultData = [status: 'SUCCESS', message: 'Successfully updated comment.']
        withFormat {
            html {
                redirect(action:'view', params: [id: assessment.id, stepId: params.stepId])
            }
            json {
                render resultData as JSON
            }
            xml {
                render resultData as XML
            }
        }

    }//end setStepDataComment()

    /**
     * An ajax method (similar to changeGlobalComment) which is meant to set the value of a particular parameter.
     */
    def setParameterValue() {
        User user = springSecurityService.currentUser
        Assessment assessment = Assessment.get(params.id)
        if( !assessment )
            throw new ServletException("Missing or invalid 'id' parameter")

        AssessmentStepData stepData = AssessmentStepData.get(params.stepDataId)
        if( !stepData )
            throw new ServletException("Missing or invalid 'stepDataId' parameter")

        TdParameter parameter = TdParameter.get(params.parameterId)
        if( !parameter )
            throw new ServletException("Missing or invalid 'parameterId' parameter")

        ParameterValue paramValue = ParameterValue.findByStepDataAndParameter(stepData, parameter)
        if (!paramValue) {
            paramValue = new ParameterValue(stepData: stepData, parameter: parameter)
        }

        String userValueString = params.userValueString
        log.info("User[${user.username}] changing assessment[${assessment.id}] step[${stepData.id}] param[${parameter.id}] to value: ${userValueString}")

        // validate
        ParameterKind paramKind = Enum.valueOf(ParameterKind.class, parameter.kind)
        TrustmarkParameterBindingImpl parameterBinding = new TrustmarkParameterBindingImpl(parameterKind: paramKind, value: userValueString)
        boolean isValid = false
        if (parameterBinding.stringValue != null) {
            switch (parameterBinding.parameterKind) {
                // string is always valid if not null
                case ParameterKind.STRING: isValid = true; break
                // number, boolean, and datetime will throw exceptions in getters if invalid
                case ParameterKind.NUMBER: parameterBinding.numericValue; isValid = true; break
                case ParameterKind.BOOLEAN: parameterBinding.booleanValue; isValid = true; break
                case ParameterKind.DATETIME: parameterBinding.dateTimeValue; isValid = true; break
                // enum and enum-multi have to be validated against the list of allowed enum values
                case ParameterKind.ENUM:
                    String value = parameterBinding.stringValue
                    if (parameter.enumValues.contains(value)) {
                        isValid = true
                    }
                    break
                case ParameterKind.ENUM_MULTI:
                    List<String> values = parameterBinding.stringListValue
                    if (parameter.enumValues.containsAll(values)) {
                        isValid = true
                    }
                    break
            }
        }
        if (!isValid) {
            throw new IllegalArgumentException("Submitted value cannot be used for parameter kind " + parameterBinding.parameterKind)
        }

        userValueString = userValueString?.trim() ?: ''
        String oldUserValueString = paramValue.userValue
        paramValue.userValue = userValueString
        paramValue.save(failOnError: true, flush: true)

        assessment.logg.addEntry(
            "Set Parameter Value",
            "User[${user.username}] set parameter value",
            "User[${user.username}] has set assessment[${assessment.id}] step[${stepData.id}] param[${parameter.id}] to value: ${userValueString}",
            [
                user       : [id: user.id, username: user.username],
                assessment : [id: assessment.id],
                stepData   : [id: stepData.id],
                parameter  : [id: parameter.id],
                oldValue   : oldUserValueString,
                newValue   : userValueString
            ]
        )

        log.debug("Parameter set successfully....")
        def resultData = [status: 'SUCCESS', message: 'Successfully updated comment.']
        withFormat {
            html {
                throw new UnsupportedOperationException("NOT YET SUPPORTED - expected you would call AJAX method.")
            }
            json {
                render resultData as JSON
            }
            xml {
                render resultData as XML
            }
        }
    }//end setParameterValue()

    /**
     * This page will display the createArtifactForm.  Note that this method is called both for artifacts with only
     * step data affiliation, and also with required artifact affiliation.
     * <br/><br/>
     * Required Parameters:
     *   <b>id</b> - the assessment id
     *   <b>stepNumber</b> - the step which this artifact is affiliated with
     * Optional Parameters:
     *   <b>requiredArtifactId</b> - a required artifact identifier from the TD to associate with by default.
     */
    @ParamConversions([
            @ParamConversion(paramName="id", toClass=Assessment.class, storeInto = "assessment"),
            @ParamConversion(paramName="stepDataId", toClass=AssessmentStepData.class, storeInto = "stepData"),
            @ParamConversion(paramName="requiredArtifactId", toClass=ArtifactData.class, storeInto = "artifactData")
    ])
    def createArtifact() {
        User user = springSecurityService.currentUser
        Assessment assessment = params.assessment
        AssessmentStepData stepData = params.stepData
        ArtifactData requiredArtifact = params.artifactData

        log.info("Showing createArtifact form[user=$user, assessment=$assessment, step=$stepData]...")

        // TODO We probably want to forward the user to a "re-assign" page instead of just throwing an error.
        if( assessment.assignedTo?.id != user?.id ){
            log.warn("User ${user?.username} cannot view the performance of assessment[${assessment.id}] without re-assigning it.")
            throw new ServletException("You are not the assessor for assessment ${assessment.id}.  Please re-assign it to yourself to continue.")
        }

        def requiredArtifactList = []
        requiredArtifactList.addAll(stepData.step.artifacts )
        Collections.sort(requiredArtifactList, {AssessmentStepArtifact a1, AssessmentStepArtifact a2 ->
            return a1.getName().compareToIgnoreCase(a2.getName())
        } as Comparator)


        CreateArtifactCommand command = new CreateArtifactCommand()
        command.assessmentId = assessment.id
        command.stepDataId = stepData.id
        if( requiredArtifact )
            command.requiredArtifactId = requiredArtifact.id
        log.debug("Showing create artifact form...")
        [
                command: command,
                assessment: assessment,
                currentStepData: stepData,
                requiredArtifact: requiredArtifact,
                requiredArtifactList: requiredArtifactList
        ]

    }//end createArtifact()

    /**
     * This is the POST method for the create artifact form.
     */
    def saveArtifact(CreateArtifactCommand command) {
        log.debug("Called save artifact, validating...")
        User user = springSecurityService.currentUser

        def assessment = Assessment.get(command.assessmentId)
        if( !assessment ){
            log.warn("Could not find assessment: ${command.assessmentId}")
            throw new ServletException("No such assessment ${command.assessmentId}")
        }
        AssessmentStepData stepData = AssessmentStepData.findById(command.stepDataId)
        if( !stepData ){
            log.warn("Could not find stepData: ${command.stepDataId}")
            throw new ServletException("No such stepData ${command.stepDataId}")
        }
        AssessmentStepArtifact requiredArtifact = null
        if( command.requiredArtifactId && command.requiredArtifactId != -1 ){
            requiredArtifact = AssessmentStepArtifact.findById(command.requiredArtifactId)
            // TODO Should we check to see if this is actually under the given step data?
            if( !requiredArtifact ){
                log.error("No such required artifact '${command.requiredArtifactId}' for assessment '${assessment.id}'")
                throw new ServletException("No such required artifact '${command.requiredArtifactId}' for assessment '${assessment.id}'")
            }
        }else{
            log.debug("Found no artifact requirement, adding general artifact to step...")
        }

        if(!command.validate()){
            log.warn "Create Artifact form does not validate: "
            command.errors.getAllErrors().each { ObjectError error ->
                log.warn "    ${error.defaultMessage}"
            }
            return render(view: 'createArtifact', model: [command: command, assessment: assessment, currentStepData: stepData, requiredArtifact: requiredArtifact])
        }

        if( !command.binaryId1 )
            command.binaryId1 = -1
        BinaryObject binaryObject = null
        if( command.binaryId1 != -1 ){
            binaryObject = BinaryObject.findById(command.binaryId1)
            if( !binaryObject ){
                log.error("No such binary object '${command.binaryId1}' for assessment '${assessment.id}'")
                throw new ServletException("No such binary object '${command.binaryId1}' for assessment '${assessment.id}'")
            }
        }

        // TODO We probably want to forward the user to a "re-assign" page instead of just throwing an error.
        if( assessment.assignedTo?.id != user?.id ){
            log.warn("User ${user?.username} cannot view the performance of assessment[${assessment.id}] without re-assigning it.")
            throw new ServletException("You are not the assessor for assessment ${assessment.id}.  Please re-assign it to yourself to continue.")
        }

        log.info("Saving artifact...")
        ArtifactData artifactData = new ArtifactData()
        artifactData.displayName = command.displayName
        artifactData.comment = command.comment; // May be null/empty, but only if data has a binary object reference
        artifactData.uploadingUser = user
        artifactData.requiredArtifact = requiredArtifact; // May be null
        artifactData.data = binaryObject; // May be null, but only if comment has content
        artifactData.save(failOnError: true, flush: true)

        stepData.addToArtifacts(artifactData)
        stepData.save(failOnError: true, flush: true)

        String logArtifactId = artifactData.data ? artifactData.data.originalFilename : artifactData.id.toString()
        def artifactMap = [id: artifactData.id, displayName: artifactData.displayName, comment: artifactData.comment, uploadingUser: [id: user.id, username: user.username]]
        if( artifactData.requiredArtifact )
            artifactMap.put("requiredArtifact", [id: artifactData.requiredArtifact.id, name: artifactData.requiredArtifact.name])
        if( artifactData.data )
            artifactMap.put("data", [id: artifactData.data.id, originalFilename: artifactData.data.originalFilename, fileSize: artifactData.data.fileSize])
        if( artifactData.requiredArtifact ){
            assessment.logg.addEntry(
                    "Add Required Artifact",
                    "User ${user?.contactInformation?.responder} added artifact[${logArtifactId}] to step number ${stepData.step.id}, satisfying Required Artifact ${artifactData.requiredArtifact.name}",
                    "User ${user?.contactInformation?.responder} added artifact[${logArtifactId}] to step number ${stepData.step.id}, satisfying Required Artifact ${artifactData.requiredArtifact.name}",
                    [user      : [id: user.id, username: user.username],
                     assessment: [id: assessment.id],
                     stepData  : [id: stepData.id, step: [id: stepData.step.id, stepNumber: stepData.step.stepNumber, name: stepData.step.name]],
                     artifact  : artifactMap
                    ]
            )
        }else {
            assessment.logg.addEntry(
                    "Add Artifact",
                    "User ${user?.contactInformation?.responder} added artifact[${logArtifactId}] to step number ${stepData.step.id}",
                    "User ${user?.contactInformation?.responder} added artifact[${logArtifactId}] to step number ${stepData.step.id}",
                    [user      : [id: user.id, username: user.username],
                     assessment: [id: assessment.id],
                     stepData  : [id: stepData.id, step: [id: stepData.step.id, stepNumber: stepData.step.stepNumber, name: stepData.step.name]],
                     artifact  : artifactMap
                    ]
            )
        }

        withFormat {
            html {
                if( requiredArtifact ) {
                    redirect(action: 'view', id: assessment.id, params: [stepId: stepData.step.id, requiredArtifactId: requiredArtifact.id])
                }else{
                    redirect(action: 'view', id: assessment.id, params: [stepId: stepData.step.id])
                }
            }
            // TODO JSON, XML?
        }

    }//end saveArtifact()


    /**
     * This page will display the importAssessmentResults view.
     * Required Parameter:
     *   <b>id</b> - the assessment id
     */

    @ParamConversions([
            @ParamConversion(paramName="id", toClass=Assessment.class, storeInto = "assessment"),
    ])
    def importAssessmentResults() {
        User user = springSecurityService.currentUser
        Assessment assessment = params.assessment

        log.info("Showing importAssessmentResults form[user=$user, assessment=$assessment...")

        if( !assessment.assignedTo ){
            log.debug("Assigning user[${user.username}] to assessment[${assessment.id}]")
            assessment.assignedTo = user
            assessment.lastAssessor = user
            assessment.save(failOnError: true, flush: true)

            assessment.logg.addEntry("Assessor Change", "Assigning user[${user.username}] to assessment[${assessment.id}]",
                    "User ${user?.contactInformation?.responder} [id: ${user?.username}] is now assigned assessment[${assessment.id}].",
                    [user: [id: user.id, username: user.username], assessment: [id: assessment.id], oldAssignee: null])

        }else if( assessment.assignedTo && assessment.assignedTo.username != user.username ){
            log.debug("Assigning user[${user.username}] to assessment[${assessment.id}], and removing assignedTo from User[${assessment.assignedTo.username}]")
            User oldAssignee = assessment.assignedTo
            assessment.assignedTo = user
            assessment.lastAssessor = user
            assessment.save(failOnError: true, flush: true)

            assessment.logg.addEntry(
                    "Assessor Change",
                    "User ${user?.contactInformation?.responder} is now Assessing Assessment ${assessment.id}",
                    "User ${user?.contactInformation?.responder} [id: ${user?.username}] is now assigned " +
                            "assessment[${assessment.id}].  Note that assessignment taken from user " +
                            "${oldAssignee?.contactInformation?.responder} [id: ${oldAssignee?.username}].",
                    [user       : [id: user.id, username: user.username],
                     assessment : [id: assessment.id],
                     oldAssignee: [id: oldAssignee.id, username: oldAssignee?.username]])

        }else{
            // It's the same user, no need to mark it as anything.
            log.debug("User[${user.username}] is already assigned to assessment[${assessment.id}]")
        }


        CreateImportArtifactCommand command = new CreateImportArtifactCommand()
        command.assessmentId = assessment.id

        log.debug("Showing importAssessmentResults view...")

        [
                command: command,
                assessment: assessment
        ]

    }//end importAssessmentResults()

    /**
     * This is the POST method for the importAssessmentResults view.
     */
    def saveAssessmentResults(CreateImportArtifactCommand command) {
        log.debug("Called saveAssessmentResults, validating...")
        User user = springSecurityService.currentUser

        def assessment = Assessment.get(command.assessmentId)
        if( !assessment ){
            log.warn("Could not find assessment: ${command.assessmentId}")
            throw new ServletException("No such assessment ${command.assessmentId}")
        }

        if(!command.validate()){
            log.warn "Create Import Artifact form does not validate: "
            command.errors.getAllErrors().each { ObjectError error ->
                log.warn "    ${error.defaultMessage}"
            }
            return render(view: 'importAssessmentResults', model: [command: command, assessment: assessment])
        }

        if( !command.binaryId1 )
            command.binaryId1 = -1
        BinaryObject binaryObject = null
        if( command.binaryId1 != -1 ){
            binaryObject = BinaryObject.findById(command.binaryId1)
            if( !binaryObject ){
                log.error("No such binary object '${command.binaryId1}' for assessment '${assessment.id}'")
                throw new ServletException("No such binary object '${command.binaryId1}' for assessment '${assessment.id}'")
            }
        }

        // TODO We probably want to forward the user to a "re-assign" page instead of just throwing an error.
        if( assessment.assignedTo?.id != user?.id ){
            log.warn("User ${user?.username} cannot view the performance of assessment[${assessment.id}] without re-assigning it.")
            throw new ServletException("You are not the assessor for assessment ${assessment.id}.  Please re-assign it to yourself to continue.")
        }

        log.info("Importing assessment results for assessment #${assessment.id}...")

        int count = 0
        for( AssessmentStepData stepData : assessment.getSortedSteps() ){
            log.debug("    Setting artifact for step: ${stepData.step.name}")

            // set the step result appropriately
            if (stepData.hasRequiredParameters)
            {
                stepData.result = AssessmentStepResult.Not_Known
            } else {
                stepData.result = AssessmentStepResult.Satisfied
            }

            stepData.lastResultUser = user
            stepData.resultLastChangeDate = Calendar.getInstance().getTime()

            stepData.assessorComment = "Automatic assessment based on audit results."
            stepData.assessorCommentUser = user
            stepData.lastCommentDate = stepData.resultLastChangeDate

            for( AssessmentStepArtifact artifact : stepData.step.artifacts ){
                ArtifactData artifactData = new ArtifactData()
                artifactData.requiredArtifact = artifact
                artifactData.comment = command.comment
                artifactData.displayName = command.displayName
                artifactData.data = binaryObject
                artifactData.modifyingUser = user
                artifactData.uploadingUser = user
                artifactData.save(failOnError: true)

                stepData.addToArtifacts(artifactData)
            }

            stepData.save(failOnError: true)
            count++
        }

        log.info("Successfully imported assessments results for ${count} steps!")

        withFormat {
            html {
                redirect(controller: 'assessment', action: 'view', id: assessment.id)
            }
            // TODO JSON, XML?
        }

    }//end saveAssessmentResults()


    /**
     * Allows for status/state change for an assessment.
     */
    @ParamConversion(paramName="id", toClass=Assessment.class, storeInto = "assessment")
    def changeAssessmentStatus() {
        User user = springSecurityService.currentUser
        Assessment assessment = params.assessment

        AssessmentStatus newStatus = AssessmentStatus.fromString(params.newStatus)
        if( !newStatus ){
            log.warn("Missing or invalid parameter 'newStatus'")
            throw new ServletException("Missing or invalid parameter 'newStatus': ${params?.newStatus}")
        }

        // TODO We probably want to forward the user to a "re-assign" page instead of just throwing an error.
        if( assessment.assignedTo?.id != user?.id ){
            log.warn("User ${user?.username} cannot view the performance of assessment[${assessment.id}] without re-assigning it.")
            throw new ServletException("You are not the assessor for assessment ${assessment.id}.  Please re-assign it to yourself to continue.")
        }

        log.info("User[${user.username}] changing assessment[${assessment.id}] status from ${assessment.status} to ${newStatus}...")
        AssessmentStatus oldStatus = assessment.status
        assessment.status = newStatus
        assessment.statusLastChangeDate = Calendar.getInstance().getTime()
        assessment.statusLastChangeUser = user
        assessment.save(failOnError: true, flush: true)

        assessment.logg.addEntry("Status Change", "Assessment Status Change: ${oldStatus} -> ${newStatus}",
                "User ${user.username} has changed assessment[${assessment.id}] status from ${oldStatus} to ${newStatus}.",
                [user: [id: user.id, username: user.username], assessment: [id: assessment.id], oldStatus: oldStatus.toString(), newStatus: newStatus.toString()])


        if( assessment.assignedTo != null && ![AssessmentStatus.IN_PROGRESS, AssessmentStatus.PENDING_ASSESSOR].contains(assessment.status) ){
            log.debug("Removing assigned user[${assessment.assignedTo.username}] (since assessment[${assessment.id}] is no longer IN_PROGRESS or PENDING_ASSESSOR)...")
            User assignee = assessment.assignedTo
            assessment.assignedTo = null
            assessment.save(failOnError: true, flush: true)

            assessment.logg.addEntry("Assessor Change", "Removing assigned User[${assignee?.username}] from assessment[${assessment.id}]",
                    "User[${user?.username}] is marking assessment[${assessment.id}] as $newStatus, thus User[${assignee?.username}] is no longer assigned.",
                    [user: [id: user.id, username: user.username], assessment: [id: assessment.id, status: newStatus, oldStatus: oldStatus], oldAssignee: [id: assignee?.id, username: assignee?.username]])

        }

        flash.message = "Successfully marked assessment[${assessment.id}] status as ${newStatus}."
        return redirect(controller: 'assessment', action: 'view', id: assessment.id)
    }

    /**
     * This method will view an existing artifact.
     */
    @ParamConversions([
            @ParamConversion(paramName="id", toClass=Assessment.class, storeInto = "assessment"),
            @ParamConversion(paramName="stepDataId", toClass=AssessmentStepData.class, storeInto = "stepData"),
            @ParamConversion(paramName="artifactId", toClass=ArtifactData.class, storeInto = "artifactData")
    ])
    def viewArtifact() {
        log.debug("Request to remove artifact...")
        User user = springSecurityService.currentUser

        Assessment assessment = params.assessment
        AssessmentStepData stepData = params.stepData
        ArtifactData artifactData = params.artifactData

        // TODO We probably want to forward the user to a "re-assign" page instead of just throwing an error.
        if( assessment.assignedTo?.id != user?.id ){
            log.warn("User ${user?.username} cannot view the performance of assessment[${assessment.id}] without re-assigning it.")
            throw new ServletException("You are not the assessor for assessment ${assessment.id}.  Please re-assign it to yourself to continue.")
        }

        log.debug("Rendering view artifact[id=${artifactData.id}] page (for perform assessment #${assessment.id})...)")
        withFormat {
            html {
                [assessment: assessment, artifact: artifactData, stepData: stepData]
            }
        }

    }//end viewArtifact()

    /**
     * Sets the failure reason for a given assessment step.
     */
    @ParamConversions([
            @ParamConversion(paramName="id", toClass=Assessment.class, storeInto = "assessment"),
            @ParamConversion(paramName="stepDataId", toClass=AssessmentStepData.class, storeInto = "stepData")
    ])
    def setFailureReason() {
        User user = springSecurityService.currentUser
        Assessment assessment = params.assessment
        AssessmentStepData stepData = params.stepData
        if( !assessment || !stepData ){
            log.warn("Missing required assessment and/or step data to set failure reason.")
            throw new ServletException("Missing required assessment and/or step data!")
        }

        String failurePropertyName = null
        String statusString = null
        Enumeration names = request.getParameterNames()
        while( names.hasMoreElements() ){
            String nextParam = names.nextElement()
            if( nextParam.equalsIgnoreCase("failureId") )
                failurePropertyName = request.getParameter(nextParam)
            else if( nextParam.equalsIgnoreCase("status") )
                statusString = request.getParameter(nextParam)
        }
        Boolean status = Boolean.FALSE
        if( statusString.equalsIgnoreCase('true') ){
            status = Boolean.TRUE
        }

        log.info("Storing Assessment[@|blue ${assessment.id}|@]->StepData[@|blue ${stepData.step.stepNumber}|@].@|cyan ${failurePropertyName}|@ = @|green ${status}|@ ")

        if( failurePropertyName.equalsIgnoreCase('orgClaimsNonConformance') ){
            stepData.orgClaimsNonConformance = status
        }else if( failurePropertyName.equalsIgnoreCase('evidenceIndicatesNonConformance') ){
            stepData.evidenceIndicatesNonConformance = status
        }else if( failurePropertyName.equalsIgnoreCase('evidenceIndicatesPartialConformance') ){
            stepData.evidenceIndicatesPartialConformance = status
        }else if( failurePropertyName.equalsIgnoreCase('orgCannotProvideSufficientEvidence') ){
            stepData.orgCannotProvideSufficientEvidence = status
        }
        stepData.lastCheckboxUser = user
        stepData.save(failOnError: true)

        assessment.logg.addEntry(
                "Step Failure Reason Change",
                "StepData[${stepData.id}].${failurePropertyName} to ${status}",
                "User ${user?.contactInformation?.responder} set Assessment[${assessment.id}]->StepData[${stepData.id}].${failurePropertyName} to ${status}",
                [user       : [id: user.id, username: user.username],
                 assessment : [id: assessment.id],
                 stepData: [id: stepData.id, step: [id: stepData.step.id, number: stepData.step.stepNumber, name: stepData.step.name]],
                 propertyName: failurePropertyName,
                 status: status]
        )


        def resultData = [status: 'SUCCESS', message: 'Successfully set failure reason "'+failurePropertyName+'" to "'+status+'".']
        withFormat {
            html {
                throw new ServletException("NOT YET IMPLEMENTED")
            }
            json {
                render resultData as JSON
            }
            xml {
                render resultData as XML
            }
        }

    }//end setFailureReason

    /**
     * This method will display an existing artifact in the form so you can change it as necessary.
     */
    @ParamConversions([
        @ParamConversion(paramName="id", toClass=Assessment.class, storeInto = "assessment"),
        @ParamConversion(paramName="stepDataId", toClass=AssessmentStepData.class, storeInto = "stepData"),
        @ParamConversion(paramName="artifactId", toClass=ArtifactData.class, storeInto = "artifactData")
    ])
    def editArtifact() {
        log.debug("Request to edit artifact...")
        User user = springSecurityService.currentUser
        Assessment assessment = params.assessment
        AssessmentStepData stepData = params.stepData
        ArtifactData artifactData = params.artifactData

        // TODO We probably want to forward the user to a "re-assign" page instead of just throwing an error.
        if( assessment.assignedTo?.id != user?.id ){
            log.warn("User ${user?.username} cannot edit artifact[${params.artifactId}] for assessment[${assessment.id}] without re-assigning the assessment.")
            throw new ServletException("You are not the assessor for assessment ${assessment.id}.  Please re-assign it to yourself to continue.")
        }

        log.info("User[${user.username}] is editing the artifact[${artifactData?.id}] for step #${stepData?.step?.stepNumber} of assessment[${assessment?.id}]...")

        def requiredArtifactList = []
        requiredArtifactList.addAll(stepData.step.artifacts )
        Collections.sort(requiredArtifactList, {AssessmentStepArtifact a1, AssessmentStepArtifact a2 ->
            return a1.getName().compareToIgnoreCase(a2.getName())
        } as Comparator)


        EditArtifactCommand command = new EditArtifactCommand()
        command.artifactId = artifactData.id
        command.assessmentId = assessment.id
        command.binaryId1 = artifactData.data?.id ?: null
        command.comment = artifactData.comment
        command.displayName = artifactData.displayName
        command.stepDataId = Integer.parseInt(params.stepDataId)
        command.requiredArtifactId = artifactData.getRequiredArtifact()?.id ?: null
        if( artifactData.data != null ){
            command.artifactType = "noChange"
        }else{
            command.artifactType = "commentOnly"
        }

        [assessment: assessment, currentStepData: stepData, artifactData: artifactData, command: command, requiredArtifactList: requiredArtifactList]
    }//end editArtifact()

    /**
     * Called by the edit artifact form to save the updated artifact information.
     */
    @ParamConversions([
            @ParamConversion(paramName="assessmentId", toClass=Assessment.class, storeInto = "assessment"),
            @ParamConversion(paramName="stepDataId", toClass=AssessmentStepData.class, storeInto = "stepData"),
            @ParamConversion(paramName="artifactId", toClass=ArtifactData.class, storeInto = "artifactData")
    ])
    def updateArtifact(EditArtifactCommand command) {
        log.info("Updating an artifact...")
        User user = springSecurityService.currentUser

        Assessment assessment = params.assessment
        AssessmentStepData stepData = params.stepData
        ArtifactData artifactData = params.artifactData

        // TODO We probably want to forward the user to a "re-assign" page instead of just throwing an error.
        if( assessment.assignedTo?.id != user?.id ){
            log.warn("User ${user?.username} cannot edit artifact[${params.artifactId}] for assessment[${assessment.id}] without re-assigning the assessment.")
            throw new ServletException("You are not the assessor for assessment ${assessment.id}.  Please re-assign it to yourself to continue.")
        }

        log.info("User[${user.username}] has edited and now updating artifact[${params.artifactId}] for step #${params.stepDataId} of assessment[${assessment.id}]...")

        if(!command.validate()){
            log.warn "Edit Artifact form does not validate: "
            command.errors.getAllErrors().each { ObjectError error ->
                log.warn "    {${error.codes}} ${error.defaultMessage}"
            }
            return render(view: 'editArtifact', model: [assessment: assessment, currentStepData: stepData, artifactData: artifactData, command: command])
        }

        log.debug("Perform update in database...")

        String oldComment = null
        String oldDisplayName = null
        BinaryObject oldBinary = null
        AssessmentStepArtifact oldArtifact = null
        boolean changed = false
        if( !command.binaryId1 || (command.binaryId1 != artifactData.data?.id) ){
            oldBinary = artifactData.data
            if( !command.binaryId1 || command.binaryId1 == -1 ){
                log.debug("Removed binary altogether...")
                artifactData.data = null
            }else{
                log.debug("Changing binary to ${command.binaryId1}...")
                artifactData.data = BinaryObject.get(command.binaryId1)
            }
            artifactData.modifyingUser = user
            changed = true
        }
        if( command.displayName != artifactData.displayName ){
            oldDisplayName = artifactData.displayName
            log.debug("Updating the display name to: ${command.displayName}")
            artifactData.displayName = command.displayName
            changed = true
        }
        if( command.comment != artifactData.comment ){
            oldComment = artifactData.comment
            log.debug("Updating the comment to: ${command.comment}")
            artifactData.comment = command.comment
            changed = true
        }
        if( command.requiredArtifactId != artifactData.requiredArtifact?.id ){
            oldArtifact = artifactData.requiredArtifact
            artifactData.requiredArtifact = AssessmentStepArtifact.findById(command.requiredArtifactId)
            changed = true
        }
        if( artifactData.uploadingUser == null ){
            artifactData.uploadingUser = user
            changed = true
        }

        if( changed ){
            log.debug("Performing save...")
            artifactData.modifyingUser = user
            artifactData.save(failOnError: true, flush: true)

            assessment.logg.addEntry("Edit Artifact", "User[${user.username}] edit artifact[${artifactData.id}]",
                    "User[${user?.username}] has edited artifact[${artifactData.id}] pertaining to step #${params.stepNumber} in " +
                            "assessment[${assessment.id}].",
                    [   user: [id: user.id, username: user.username],
                        assessment: [id: assessment.id, status: assessment.status],
                        stepData: [id: stepData.id, step: [id: stepData.step.id, name: stepData.step.name]],
                        artifactData: [id: artifactData.id,
                            requiredArtifact: artifactData.requiredArtifact ?
                                    [id: artifactData.requiredArtifact.id,
                                        name: artifactData.requiredArtifact.name,
                                        description: artifactData.requiredArtifact.description]
                                : null,
                            displayName: artifactData.displayName,
                            comment: artifactData.comment,
                            uploadingUser: artifactData.uploadingUser.toJsonMap(true),
                            data: artifactData.data ?
                                    [id: artifactData.data.id,
                                     originalFilename: artifactData.data.originalFilename,
                                     fileSize: artifactData.data.fileSize]
                                : null
                        ],
                        oldBinary: oldBinary?.toJsonMap(true),
                        oldComment: oldComment,
                        oldDisplayName: oldDisplayName,
                        oldArtifact: oldArtifact ?
                                [ id : oldArtifact.id, name: oldArtifact.name, description: oldArtifact.description ]
                                : null
                    ])

            flash.message = "Successfully updated artifact."
        }else{
            flash.message = "No changes were made to artifact."
        }

        return redirect(action: 'viewArtifact', id: params.assessmentId, params: [stepDataId: params.stepDataId, artifactId: params.artifactId])
    }//end updateArtifact()


    /**
     * This method will delete an existing artifact.
     */
    @ParamConversions([
            @ParamConversion(paramName="id", toClass=Assessment.class, storeInto = "assessment"),
            @ParamConversion(paramName="stepDataId", toClass=AssessmentStepData.class, storeInto = "stepData"),
            @ParamConversion(paramName="artifactId", toClass=ArtifactData.class, storeInto = "artifactData")
    ])
    def deleteArtifact() {
        log.debug("Request to remove artifact...")
        User user = springSecurityService.currentUser

        Assessment assessment = params.assessment
        AssessmentStepData stepData = params.stepData
        ArtifactData artifactData = params.artifactData

        if( assessment == null )
            throw new ServletException("Invalid null Assessment encountered in deleteArtifact()!")
        if( stepData == null )
            throw new ServletException("Invalid null AssessmentStepData encountered in deleteArtifact()!")
        if( artifactData == null )
            throw new ServletException("Invalid null ArtifactData encountered in deleteArtifact()!")

        // TODO We probably want to forward the user to a "re-assign" page instead of just throwing an error.
        if( assessment.assignedTo?.id != user?.id ){
            log.warn("User ${user?.username} cannot view the performance of assessment[${assessment.id}] without re-assigning it.")
            throw new ServletException("You are not the assessor for assessment ${assessment.id}.  Please re-assign it to yourself to continue.")
        }

        log.debug("Performing database delete...")
        stepData.removeFromArtifacts(artifactData)
        stepData.save(failOnError: true, flush: true)
        artifactData.delete(failOnError: true, flush: true)

        // TODO Delete the binary from the database, also.  Could be done with a reaper thread later.

        log.debug("Storing log entry...")
        def artifactMap = [
                id: params.artifactId,
                assessment: [id: assessment.id],
                stepData  : [id: stepData.id, step: [id: stepData.step.id, stepNumber: stepData.step.stepNumber, name: stepData.step.name]],
                uploadingUser: [id: artifactData.uploadingUser?.id, username: artifactData.uploadingUser?.username],
                comment: artifactData.comment
        ]
        if( artifactData.requiredArtifact )
            artifactMap.put("requiredArtifact", [id: artifactData.requiredArtifact.id, name: artifactData.requiredArtifact.name])
        if( artifactData.data )
            artifactMap.put("data", [id: artifactData.data.id, originalFilename: artifactData.data.originalFilename, fileSize: artifactData.data.fileSize])
        assessment.logg.addEntry(
                "Delete Artifact",
                "User ${user?.contactInformation?.responder} deleted artifact[${params.artifactId}] from step number ${stepData.step.stepNumber}",
                "User ${user?.contactInformation?.responder} deleted artifact[${params.artifactId}] from step number ${stepData.step.stepNumber} on assessment ${assessment.id}",
                [user      : [id: user.id, username: user.username],
                 assessment: [id: assessment.id],
                 stepData  : [id: stepData.id, step: [id: stepData.step.id, stepNumber: stepData.step.stepNumber, name: stepData.step.name]],
                 artifact  : artifactMap
                ]
        )

        log.debug("Rendering response...")
        def responseData = [status: 'SUCCESS', message: "Successfully removed artifact #${params.artifactId} from assessment ${params.id}"]
        withFormat {
            html {
                throw new ServletException("NOT YET IMPLEMENTED")
            }
            json {
                render responseData as JSON
            }
            xml {
                render responseData as XML
            }
        }

    }//end deleteArtifact()

    /**
     * Sends users to the create substep form page.
     */
    @ParamConversions([
            @ParamConversion(paramName="id", toClass=Assessment.class, storeInto = "assessment"),
            @ParamConversion(paramName="stepNumber", toClass=AssessmentStepData.class, storeInto = "stepData")
    ])
    def createSubstep() {
        log.debug("Request to remove artifact...")
        User user = springSecurityService.currentUser

        Assessment assessment = params.assessment
        // TODO We probably want to forward the user to a "re-assign" page instead of just throwing an error.
        if( assessment.assignedTo?.id != user?.id ){
            log.warn("User ${user?.username} cannot edit artifact[${params.artifactId}] for assessment[${assessment.id}] without re-assigning the assessment.")
            throw new ServletException("You are not the assessor for assessment ${assessment.id}.  Please re-assign it to yourself to continue.")
        }
        AssessmentStepData stepData = params.stepData

        // TODO We probably want to forward the user to a "re-assign" page instead of just throwing an error.
        if( assessment.assignedTo?.id != user?.id ){
            log.warn("User ${user?.username} cannot view the performance of assessment[${assessment.id}] without re-assigning it.")
            throw new ServletException("You are not the assessor for assessment ${assessment.id}.  Please re-assign it to yourself to continue.")
        }

        log.info("User[$user] is creating a substep for Assessment[${assessment.id}], Step[#${stepData.step.stepNumber}]...")


        CreateSubstepCommand command = new CreateSubstepCommand()
        command.assessmentId = assessment.id
        command.stepDataId = stepData.id

        [command: command, assessment: assessment, stepData: stepData]
    }//end createSubstep()


    /**
     * Processes input from the substep form page.
     */
    @ParamConversions([
            @ParamConversion(paramName="id", toClass=Assessment.class, storeInto = "assessment"),
            @ParamConversion(paramName="stepNumber", toClass=AssessmentStepData.class, storeInto = "stepData")
    ])
    def saveSubstep(CreateSubstepCommand command) {
        log.debug("Saving new substep...")
        User user = springSecurityService.currentUser

        Assessment assessment = params.assessment
        // TODO We probably want to forward the user to a "re-assign" page instead of just throwing an error.
        if( assessment.assignedTo?.id != user?.id ){
            log.warn("User ${user?.username} cannot edit artifact[${params.artifactId}] for assessment[${assessment.id}] without re-assigning the assessment.")
            throw new ServletException("You are not the assessor for assessment ${assessment.id}.  Please re-assign it to yourself to continue.")
        }

        AssessmentStepData stepData = params.stepData
        if( command.hasErrors() ){
            log.warn("Create Substep form has errors: "+command.errors.allErrors)
            return render(view: 'createSubstep', model: [command: command, assessment: assessment, stepData: stepData])
        }

        log.info("Processing create substep...")
        AssessmentSubStep substep = new AssessmentSubStep(assessmentStep: stepData.step)
        substep.name = command.name
        substep.description = command.description
        substep.save(failOnError: true)
        stepData.step.addToSubsteps(substep)
        stepData.step.save(failOnError: true)

        AssessmentSubStepData subStepData = new AssessmentSubStepData(assessmentStepData: stepData, substep: substep)
        subStepData.result = command.result
        subStepData.lastResultUser = user
        subStepData.resultLastChangeDate = Calendar.getInstance().getTime()
        subStepData.assessorComment = command.assessorComment
        subStepData.assessorCommentUser = user
        subStepData.lastCommentDate = Calendar.getInstance().getTime()
        subStepData.save(failOnError: true)
        stepData.addToSubsteps(subStepData)
        stepData.save(failOnError: true, flush: true)

        assessment.logg.addEntry(
                "Add Substep",
                "User ${user?.contactInformation?.responder} added substep[${substep.name}] to step number ${stepData.step.stepNumber}",
                "User ${user?.contactInformation?.responder} added substep[${substep.name}] to step number ${stepData.step.stepNumber} of assessment #${assessment.id}",
                [user      : [id: user.id, username: user.username],
                 assessment: [id: assessment.id],
                 stepData  : [id: stepData.id, step: [id: stepData.step.id, stepNumber: stepData.step.stepNumber, name: stepData.step.name]],
                 substep   : [id: substep.id, name: substep.name, description: substep.description],
                 substepData :  [id: subStepData.id, result: subStepData.result.toString(), assessorComment: subStepData.assessorComment]
                ]
        )

        log.info("Successfully created new substep ${substep.name} on step #${stepData.step.stepNumber} of assessment ${assessment.id}")

        flash.message = "Successfully created substep '${substep.name}'"

        redirect(controller:'assessmentPerform', action: 'view', id: assessment.id, params: [stepNumber: stepData.step.stepNumber])
    }//end saveSubstep()



    /**
     * Processes input from the substep form page.
     */
    @ParamConversions([
            @ParamConversion(paramName="id", toClass=Assessment.class, storeInto = "assessment"),
            @ParamConversion(paramName="stepNumber", toClass=AssessmentStepData.class, storeInto = "stepData")
    ])
    def viewSubstep(){
        log.debug("Viewing existing substep...")
        User user = springSecurityService.currentUser

        Assessment assessment = params.assessment
        AssessmentStepData stepData = params.stepData
        // TODO We probably want to forward the user to a "re-assign" page instead of just throwing an error.
        if( assessment?.assignedTo?.id != user?.id ){
            log.warn("User ${user?.username} cannot edit artifact[${params.artifactId}] for assessment[${assessment.id}] without re-assigning the assessment.")
            throw new ServletException("You are not the assessor for assessment ${assessment.id}.  Please re-assign it to yourself to continue.")
        }
        log.debug("Finding substep ${params.substepId} on assessment #${assessment.id}, step #${stepData.step.stepNumber}")
        AssessmentSubStepData substep = null
        stepData.substeps?.each { current ->
            if( current.id.toString().equalsIgnoreCase(params.substepId) ){
                substep = current
            }
        }
        if( !substep ){
            log.warn("Invalid parameter substepId: ${params.substepId}")
            throw new ServletException("Invalid parameter substepId: ${params.substepId}")
        }

        log.debug("Displaying substep...")
        withFormat {
            html {
                [substep: substep, stepData: stepData, assessment: assessment]
            }
            json {
                throw new ServletException("JSON NOT YET SUPPORTED")
            }
            xml {
                throw new ServletException("XML NOT YET SUPPORTED")
            }
        }
    }


    /**
     * Sends users to the create substep form page.
     */
    @ParamConversions([
            @ParamConversion(paramName="id", toClass=Assessment.class, storeInto = "assessment"),
            @ParamConversion(paramName="stepNumber", toClass=AssessmentStepData.class, storeInto = "stepData")
    ])
    def editSubstep() {
        log.debug("Editing existing substep...")
        User user = springSecurityService.currentUser

        Assessment assessment = params.assessment
        AssessmentStepData stepData = params.stepData

        // TODO We probably want to forward the user to a "re-assign" page instead of just throwing an error.
        if( assessment.assignedTo?.id != user?.id ){
            log.warn("User ${user?.username} cannot edit artifact[${params.artifactId}] for assessment[${assessment.id}] without re-assigning the assessment.")
            throw new ServletException("You are not the assessor for assessment ${assessment.id}.  Please re-assign it to yourself to continue.")
        }
        if( StringUtils.isEmpty(params.substepId) ){
            log.warn("Missing parameter substepId")
            throw new ServletException("Missing expected parameter: 'substepId'")
        }
        log.info("User[$user] is editing substep ${params.substepId} on assessment #${assessment.id}, step #${stepData.step.stepNumber}")

        log.debug("Finding substep ${params.substepId} on assessment #${assessment.id}, step #${stepData.step.stepNumber}")
        AssessmentSubStep substep = null
        stepData.step.substeps?.each { current ->
            if( current.id.toString().equalsIgnoreCase(params.substepId) ){
                substep = current
            }
        }
        if( !substep ){
            log.warn("Invalid parameter substepId: ${params.substepId}")
            throw new ServletException("Invalid parameter substepId: ${params.substepId}")
        }

        AssessmentSubStepData subStepData = null
        stepData.substeps.each { current ->
            if( current.substep.id == substep.id ){
                subStepData = current
            }
        }
        if( !subStepData ){
            log.info("Assessment #${assessment.id}, step #${stepData.step.stepNumber} does not have any substep data!")
        }


        EditSubstepCommand command = new EditSubstepCommand()
        command.substepId = substep.id
        command.assessmentId = assessment.id
        command.stepDataId = stepData.id
        command.name = substep.name
        command.description = substep.description
        command.result = subStepData?.result ?: AssessmentStepResult.Not_Known
        command.assessorComment = subStepData?.assessorComment ?: ""

        log.debug("Displaying form...")
        [command: command, substep: substep, subStepData: subStepData, stepData: stepData, assessment: assessment]
    }//end editSubstep()




    /**
     * Processes input from the substep form page.
     */
    @ParamConversions([
            @ParamConversion(paramName="id", toClass=Assessment.class, storeInto = "assessment"),
            @ParamConversion(paramName="stepNumber", toClass=AssessmentStepData.class, storeInto = "stepData")
    ])
    def updateSubstep(EditSubstepCommand command) {
        log.debug("Editing existing substep...")
        User user = springSecurityService.currentUser

        Assessment assessment = params.assessment
        // TODO We probably want to forward the user to a "re-assign" page instead of just throwing an error.
        if( assessment.assignedTo?.id != user?.id ){
            log.warn("User ${user?.username} cannot edit artifact[${params.artifactId}] for assessment[${assessment.id}] without re-assigning the assessment.")
            throw new ServletException("You are not the assessor for assessment ${assessment.id}.  Please re-assign it to yourself to continue.")
        }
        AssessmentStepData stepData = params.stepData
        log.debug("Finding substep ${params.substepId} on assessment #${assessment.id}, step #${stepData.step.stepNumber}")

        AssessmentSubStep substep = null
        stepData.step.substeps?.each { current ->
            if( current.id.toString().equalsIgnoreCase(params.substepId) ){
                substep = current
            }
        }
        if( !substep ){
            log.warn("Invalid parameter substepId: ${params.substepId}")
            throw new ServletException("Invalid parameter substepId: ${params.substepId}")
        }

        AssessmentSubStepData subStepData = null
        stepData.substeps.each { current ->
            if( current.substep.id == substep.id ){
                subStepData = current
            }
        }
        if( !subStepData ){
            log.info("Assessment #${assessment.id}, step #${stepData.step.stepNumber} does not have any substep data!")
        }


        if( command.hasErrors() ){
            log.warn("Edit Substep form has errors: "+command.errors.allErrors)
            return render(view: 'editSubstep', model: [command: command, assessment: assessment, stepData: stepData, substep: substep, subStepData: subStepData])
        }

        log.info("Processing update substep #${substep.id} for step #${stepData.step.stepNumber} on assessment #${assessment.id}...")
        boolean substepChanged = false
        if( !substep.name.equalsIgnoreCase(command.name) ) {
            substep.name = command.name
            substepChanged = true
        }
        if( !substep.description?.equalsIgnoreCase(command.description) ) {
            substep.description = command.description
            substepChanged = true
        }
        if( substepChanged ) {

            substep.save(failOnError: true)
        }

        boolean subStepDataChanged = false
        if( subStepData ) {
            if (subStepData.result != command.result) {
                subStepData.result = command.result
                subStepData.lastResultUser = user
                subStepData.resultLastChangeDate = Calendar.getInstance().getTime()
                subStepDataChanged = true
            }
            if (!subStepData.assessorComment?.equalsIgnoreCase(command.assessorComment)) {
                subStepData.assessorComment = command.assessorComment
                subStepData.assessorCommentUser = user
                subStepData.lastCommentDate = Calendar.getInstance().getTime()
                subStepDataChanged = true
            }
            if( subStepDataChanged ) {

                subStepData.save(failOnError: true)
            }
        }else{
            subStepData = new AssessmentSubStepData(assessmentStepData: stepData, substep: substep)
            subStepData.result = command.result
            subStepData.lastResultUser = user
            subStepData.resultLastChangeDate = Calendar.getInstance().getTime()
            subStepData.assessorComment = command.assessorComment
            subStepData.assessorCommentUser = user
            subStepData.lastCommentDate = Calendar.getInstance().getTime()
            subStepData.save(failOnError: true)
            stepData.addToSubsteps(subStepData)
            stepData.save(failOnError: true)
            subStepDataChanged = true
        }
        if( !substepChanged && !subStepDataChanged ){
            log.warn("User[$user] did not edit substep ${subStepData.substep.name} on step #${stepData.step.stepNumber} of assessment ${assessment.id}")
        }else {
            assessment.logg.addEntry(
                    "Edit Substep",
                    "User ${user?.contactInformation?.responder} edited substep[${subStepData.substep.name}] to step number ${stepData.step.stepNumber}",
                    "User ${user?.contactInformation?.responder} edited substep[${subStepData.substep.name}] to step number ${stepData.step.stepNumber} of assessment #${assessment.id}",
                    [user      : [id: user.id, username: user.username],
                     assessment: [id: assessment.id],
                     stepData  : [id: stepData.id, step: [id: stepData.step.id, stepNumber: stepData.step.stepNumber, name: stepData.step.name]],
                     substep   : [id: subStepData.substep.id, name: subStepData.substep.name, description: subStepData.substep.description],
                     substepData :  [id: subStepData.id, result: subStepData.result.toString(), assessorComment: subStepData.assessorComment]
                    ]
            )

            log.info("User[$user] successfully edited substep ${subStepData.substep.name} on step #${stepData.step.stepNumber} of assessment ${assessment.id}")
        }

        flash.message = "Successfully edited substep '${subStepData.substep.name}'"
        redirect(controller:'assessmentPerform', action: 'view', id: assessment.id, params: [stepNumber: stepData.step.stepNumber])
    }//end saveSubstep()




}//end AssessmentPerformController

class CreateSubstepCommand {
    Integer assessmentId
    Integer stepDataId

    String name
    String description

    AssessmentStepResult result = AssessmentStepResult.Not_Known
    String assessorComment


    static constraints = {
        assessmentId(nullable: false)
        stepDataId(nullable: false)
        name(nullable: false, blank: false, maxSize: 255)
        description(nullable: false, blank: false, maxSize: 65535)
        result(nullable: false)
        assessorComment(nullable: true, blank: true, maxSize: 65535)
    }

}//end CreateSubstepCommand()

class EditSubstepCommand {
    Integer substepId
    Integer assessmentId
    Integer stepDataId

    String name
    String description

    AssessmentStepResult result = AssessmentStepResult.Not_Known
    String assessorComment

    static constraints = {
        substepId(nullable: false)
        assessmentId(nullable: false)
        stepDataId(nullable: false)
        name(nullable: false, blank: false, maxSize: 255)
        description(nullable: false, blank: false, maxSize: 65535)
        result(nullable: false)
        assessorComment(nullable: true, blank: true, maxSize: 65535)
    }

}//end EditSubstepCommand()

class CreateArtifactCommand {
    Integer assessmentId
    Integer stepDataId

    String displayName
    String artifactType = "newUpload"

    /**
     * Either an artifact id, or '-1' to indicate general step affiliation.
     */
    Integer requiredArtifactId

    /**
     * The plupload tool will upload files and via javascript receive a binary id back.  The page will then populate this
     * with that field information.  Note that a binary is not required, though, and this field may be null.
     */
    Integer binaryId1

    String comment

    static constraints = {
        assessmentId(nullable: false)
        stepDataId(nullable: false)
        artifactType(nullable: false)
        displayName(nullable: false, blank: false, maxSize: 255)
        requiredArtifactId(nullable: true)
        binaryId1(nullable: true)
        comment(nullable: true, blank: true, maxSize: 65535, validator: {val, obj, errors ->
            if( StringUtils.isEmpty(val) && (obj.binaryId1 == null || obj.binaryId1 == -1 )) {
                errors.rejectValue("comment", "create.artifact.something.required", [] as String[], "Either comment or file must be present.")
                return false
            }
            return true
        })
    }

}

class EditArtifactCommand {
    Integer assessmentId
    Integer stepDataId
    Integer artifactId

    /**
     * Either an artifact id, or '-1' to indicate general step affiliation.
     */
    Integer requiredArtifactId

    /**
     * The plupload tool will upload files and via javascript receive a binary id back.  The page will then populate this
     * with that field information.  Note that a binary is not required, though, and this field may be null.
     */
    Integer binaryId1

    String displayName
    String comment

    String artifactType = "newUpload"

    static constraints = {
        assessmentId(nullable: false)
        stepDataId(nullable: false)
        artifactId(nullable: false)
        binaryId1(nullable: true)
        displayName(nullable: false, blank: false, maxSize: 255)
        artifactType(nullable: false)
        requiredArtifactId(nullable: true)
        comment(nullable: true, blank: true, maxSize: 65535, validator: {val, obj, errors ->
            if( StringUtils.isEmpty(val) && (obj.binaryId1 == null || obj.binaryId1 == -1 )) {
                errors.rejectValue("comment", "edit.artifact.something.required", [] as String[], "Either comment or file must be present.")
                return false
            }
            return true
        })
    }

}

class CreateImportArtifactCommand {
    Integer assessmentId

    String displayName
    String artifactType = "newUpload"

    /**
     * The plupload tool will upload files and via javascript receive a binary id back.  The page will then populate this
     * with that field information.  Note that a binary is not required, though, and this field may be null.
     */
    Integer binaryId1

    String comment

    static constraints = {
        assessmentId(nullable: false)
        artifactType(nullable: false)
        displayName(nullable: false, blank: false, maxSize: 255)
        binaryId1(nullable: true)
        comment(nullable: true, blank: true, maxSize: 65535, validator: {val, obj, errors ->
            if( StringUtils.isEmpty(val) && (obj.binaryId1 == null || obj.binaryId1 == -1 )) {
                errors.rejectValue("comment", "create.artifact.something.required", [] as String[], "Either comment or file must be present.")
                return false
            }
            return true
        })
    }

}
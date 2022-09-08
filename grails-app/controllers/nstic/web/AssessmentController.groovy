package nstic.web

import com.googlecode.charts4j.GCharts
import com.googlecode.charts4j.PieChart
import com.googlecode.charts4j.Slice
import grails.converters.JSON
import grails.converters.XML
import grails.plugin.springsecurity.annotation.Secured
import grails.gorm.transactions.Transactional
import grails.util.GrailsStringUtils
import grails.web.mapping.LinkGenerator
import nstic.assessment.ColorPalette
import nstic.util.DifferentialAssessmentProcessor
import nstic.util.TipTreeNode
import nstic.web.assessment.*
import nstic.web.td.AssessmentStep
import nstic.web.td.AssessmentStepArtifact
import nstic.web.td.TrustmarkDefinition
import nstic.web.tip.TIPReference
import nstic.web.tip.TrustInteroperabilityProfile
import org.apache.commons.io.FileUtils
import org.apache.commons.lang.StringUtils
import org.grails.help.ParamConversion
import org.hibernate.SQLQuery
import org.hibernate.Session
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.validation.ObjectError

import javax.servlet.AsyncContext
import javax.servlet.ServletException
import javax.servlet.ServletResponse
import javax.servlet.ServletResponseWrapper
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField
import java.time.LocalDateTime

@Secured(["ROLE_USER", "ROLE_ADMIN"])
@Transactional
class AssessmentController {

    static String CREATE_ASSESSMENT_SESSION_VARIABLE = "createAssessmentCommand"
    static String TDS_AND_TIPS_SESSION_VARIABLE = AssessmentController.class.getName()+".TD_AND_TIP_PARAMS"
    static String LAST_TIP_TREE_SESSION_VARIABLE = AssessmentController.class.getName()+".LAST_TIP_TREE"

    def springSecurityService
    def fileService
    def sessionFactory
    def trustmarkService
    def messageSource
    LinkGenerator grailsLinkGenerator

    def index() {
        redirect(action:'list')
    }

    def list() {
        log.info("Taking the user to the list assessment page...")
        if (!params.max)
            params.max = '20'
        params.max = Math.min(100, Integer.parseInt(params.max)).toString(); // Limit to at most 100 users at a time.

        User user = springSecurityService.currentUser
        def assessments = []
        int assessmentsCount = 0

        if (user.isAdmin()) {
            assessments = Assessment.list(params)
            assessmentsCount = Assessment.count()
        } else if (user.isUser()) {
            assessments = Assessment.findAllByAssessedOrganization(
                    user.organization, [max: params.max])
            assessmentsCount = assessments.size()
        }

        [assessments: assessments, assessmentsCountTotal: assessmentsCount]
    }

    /**
     * Returns a list of the most relevant assessments to the current user.
     */
    def mostRelevantList() {
        User user = springSecurityService.currentUser
        log.info("User[${user?.username}] requesting most relevant list of assessments...")

        log.debug("Executing native SQL query...")
        final Session session = sessionFactory.currentSession
        String queryString = """
select distinct assId from (
  SELECT a.id as assId, entry.id as entryId
  FROM assessment a, assessment_log log, assessment_log_entry entry
  WHERE %s a.assessment_log_ref = log.id and entry.assessment_log_ref = log.id
  ORDER BY entry.date_created desc
) as tempTable
LIMIT 10
"""
        if (user.isAdmin()) {
            queryString = String.format(queryString, "")
        } else {
            // replace placeholder with a where clause for the user's organization
            int orgId = user.organization.id
            String orgWhere = String.format("a.organization_ref = %d and ", orgId)

            queryString = String.format(queryString, orgWhere)
        }

        final SQLQuery sqlQuery = session.createSQLQuery(queryString)
        def assessmentIds = []
        sqlQuery.list().each{ result ->
            if( result instanceof BigInteger ){
                assessmentIds.add( ((BigInteger) result).longValue() )
            }else {
                log.debug("Assessment Id[type=${result?.class.name}]: $result")
                assessmentIds.add(result)
            }
        }


        List<Assessment> assessments = []
        def assessmentLastLogEntryMap = [:]

        assessmentIds.each{ id ->
            log.debug("Looking up assessment[$id]...")
            Assessment ass = Assessment.get(id)
            if (!ass) {
                log.error("Cannot find assessemnt, bad id value: ${id}")
                throw new UnsupportedOperationException("Cannot find assessemnt, bad id value: ${id}")
            }
            assessments.add(ass)

            // TODO Lookup log entry date...
            AssessmentLogEntry latestEntry = AssessmentLogEntry.find(
                    "from AssessmentLogEntry e where e.logg.assessment.id = :assId order by e.dateCreated desc",
                    [assId: id])
            log.debug("Latest assessment: $latestEntry")
            assessmentLastLogEntryMap.put(ass, latestEntry)

        }//end each assessmentId

        // sort descending
        DateTimeFormatter fm = new DateTimeFormatterBuilder()
                .appendPattern("yyyy-MM-dd HH:mm:ss")
                .appendFraction(ChronoField.MICRO_OF_SECOND, 0, 6, true)
                .toFormatter();

        assessments.sort((a1, a2) -> LocalDateTime.parse(a2.dateCreated.toString(), fm)
                .compareTo(LocalDateTime.parse(a1.dateCreated.toString(), fm)));

        [assessments: assessments, assessmentLastLogEntryMap: assessmentLastLogEntryMap]
    }//end mostRelevantList()


    /**
     * Called when the user wishes to create a new assessment.  Takes the user to the create assessment form.
     */
    def create() {
        log.debug("Sending user @|cyan ${springSecurityService.currentUser}|@ to create form for assessment...")

        log.debug("Forwarding to form page...")

        User user = springSecurityService.currentUser

        CreateAssessmentCommand command = null

        if (user.isUser()) {
            command = CreateAssessmentCommand.fromUser(user)
        } else {
            command = new CreateAssessmentCommand()
        }

        [command: command]
    }//end create()

    /**
     * Called when the user clicks "create" on the create assessment form.
     */
    def save(CreateAssessmentCommand createAssessmentCommand) { // called when user submits a create assessment command
        User user = springSecurityService.currentUser
        log.debug("User[${user}] is calling AssessmentController.save()...")

        CreateAssessmentTdsAndTips tdsAndTips = processParams(params)
        synchronized (session) {
            session.setAttribute(CREATE_ASSESSMENT_SESSION_VARIABLE, createAssessmentCommand)
            session.setAttribute(TDS_AND_TIPS_SESSION_VARIABLE, tdsAndTips)
        }

        if( !createAssessmentCommand.validate() ){
            log.warn("Errors in create assessment form: ")
            createAssessmentCommand.errors.allErrors.each { ObjectError error ->
                log.warn("    ${error.code} [${error.arguments ?: "none"}]")
            }
            return render(view: 'create', model: [command: createAssessmentCommand])
        }

        log.debug("User is assessing ${tdsAndTips.trustmarkDefinitions.size()} TDs and ${tdsAndTips.trustInteroperabilityProfiles.size()} TIPs...")
        if( tdsAndTips.containsTipWhichNeedsTdResolution() ){
            log.debug("The user has to resolve a TIP, redirecting them to the resolveTdsFromTip controller...")

            boolean isDifferentiallAssessment = false
            String saveDifferential = params["SaveDifferential"]
            if (StringUtils.isNotEmpty(saveDifferential)) {
                isDifferentiallAssessment = true
            }

            return redirect(action: 'resolveTdsFromTip',
                    params: [TIP_TO_RESOLVE: tdsAndTips.getNextTipResolutionId(), isDifferentiallAssessment: isDifferentiallAssessment])
        }else{
            return forward(action: 'actuallyCreateAssessment')
        }
    }//end save()
    
    /**
     * This action is called when we need to present a form to the user to have them specify which TDs should be
     * selected from a TIP for inclusion into the assessment.
     */
    def resolveTdsFromTip() {
        log.debug("Request to resolveTdsFromTip...")
        if( !params.containsKey("TIP_TO_RESOLVE") )
            throw new ServletException("Could not find param containing the TIP to resolve for!")
        if( !params.containsKey("isDifferentiallAssessment") )
            throw new ServletException("Could not find param containing the differential assessment flag!")

        CreateAssessmentCommand createAssessmentCommand = null
        CreateAssessmentTdsAndTips tdsAndTips = null
        synchronized (session){
            createAssessmentCommand = (CreateAssessmentCommand) session.getAttribute(CREATE_ASSESSMENT_SESSION_VARIABLE)
            tdsAndTips = (CreateAssessmentTdsAndTips) session.getAttribute(TDS_AND_TIPS_SESSION_VARIABLE)
        }
        if( createAssessmentCommand == null )
            throw new ServletException("Called in invalid context, there is no CreateAssessment available in session.")
        if( tdsAndTips == null )
            throw new ServletException("Called in invalid context, there is no tdAndTips available in session.")

        CreateAssessmentTIPData tipData = tdsAndTips.getTipData(Long.parseLong(params["TIP_TO_RESOLVE"]))
        if( tipData == null )
            throw new ServletException("No such TIP[${params["TIP_TO_RESOLVE"]}] param found.")

        TrustInteroperabilityProfile databaseTip = TrustInteroperabilityProfile.get(tipData.databaseId)
        if( databaseTip == null )
            throw new ServletException("No such TIP[${tipData.databaseId}] param found.")

        log.info("Displaying TD resolution page for TIP[${databaseTip.uri}]...")
        TipTreeNode treeData = TipTreeNode.getTreeNonRecursively(databaseTip)

        // Pprocess differential assessments
        boolean isDifferentialAssessment = Boolean.parseBoolean(params["isDifferentiallAssessment"])

        if (isDifferentialAssessment) {
            long overallStartTime = System.currentTimeMillis()

            Organization recipientOrganization = Organization.findByUri(createAssessmentCommand.organizationUri)

            List<Trustmark> issuedTrustmarks = Trustmark.findAllByRecipientOrganization(recipientOrganization)

            DifferentialAssessmentProcessor.process(treeData, issuedTrustmarks)

            long overallStopTime = System.currentTimeMillis()
            log.info("Successfully Executed DIFFERENTIAL in ${(overallStopTime - overallStartTime)}ms.")
        }

        synchronized (session){
            session.setAttribute(LAST_TIP_TREE_SESSION_VARIABLE, treeData)
        }

        [tip: databaseTip, tdsAndTips: tdsAndTips, treeData: treeData] // params are "magically" available to the GSP, so no need to pass here.
    }

    /**
     * This method processes the resolveTdsFromTip() form post, and either 1) processes the next TIP on the list or
     * 2) forwards to the next step in the process.
     */
    def processTreeResolve() {
        log.debug("Processing TIP tree resolution [for TIP ${params['TIP_TO_RESOLVE']}]...")
        if( !params.containsKey("TIP_TO_RESOLVE") )
            throw new ServletException("Could not find param containing the TIP to resolve for!")

        CreateAssessmentCommand createAssessmentCommand = null
        CreateAssessmentTdsAndTips tdsAndTips = null
        synchronized (session){
            createAssessmentCommand = (CreateAssessmentCommand) session.getAttribute(CREATE_ASSESSMENT_SESSION_VARIABLE)
            tdsAndTips = (CreateAssessmentTdsAndTips) session.getAttribute(TDS_AND_TIPS_SESSION_VARIABLE)
        }
        if( createAssessmentCommand == null )
            throw new ServletException("Called in invalid context, there is no CreateAssessment available in session.")
        if( tdsAndTips == null )
            throw new ServletException("Called in invalid context, there is no tdAndTips available in session.")

        CreateAssessmentTIPData tipData = tdsAndTips.getTipData(Long.parseLong(params["TIP_TO_RESOLVE"]))
        if( tipData == null )
            throw new ServletException("No such TIP[${params["TIP_TO_RESOLVE"]}] param found.")

        TrustInteroperabilityProfile databaseTip = TrustInteroperabilityProfile.get(tipData.databaseId)
        if( databaseTip == null )
            throw new ServletException("No such TIP[${tipData.databaseId}] param found.")

        log.info("Processing TD resolution for TIP ${databaseTip.name}, version ${databaseTip.version}...")
        for( String key : params.keySet() )  {
            if( key.startsWith("tdCheckbox") && processTdCheckboxParam(params[key]) ){
                String[] tdtip = key.split("-")
                if(tdtip.length > 1)  {
                    Long tipId = Long.parseLong(tdtip[1])
                    Long tdId = Long.parseLong(tdtip[0].replace("tdCheckbox", ""))
                    TrustmarkDefinition td = TrustmarkDefinition.get(tdId)
                    if(tipData.getDatabaseId() == tipId)  {
                        tipData.getTdUris().add(td.uri)
                    } else {
                        CreateAssessmentTIPData assessmentTipData = tdsAndTips.getTipData(tipId)
                        if(assessmentTipData == null)  {
                            assessmentTipData = createAssessmentTIPData(tipId)
                            tdsAndTips.trustInteroperabilityProfiles.add(assessmentTipData)
                        }
                        assessmentTipData.getTdUris().add(td.uri)
                    }
                    log.debug("* User has selected to add TD ${td.uri} ${tdtip[0]}  ${tdtip[1]}")
                } else {
                    Long tdId = Long.parseLong(tdtip[0].replace("tdCheckbox", ""))
                    TrustmarkDefinition td = TrustmarkDefinition.get(tdId)
                    tipData.getTdUris().add(td.uri)
                }
            }
        }
        tipData.processed = true

        synchronized (session){
            session.setAttribute(TDS_AND_TIPS_SESSION_VARIABLE, tdsAndTips)
        }

        return forward(action: 'actuallyCreateAssessment')

    }//end processTreeResolve()

    private boolean processTdCheckboxParam(Object on) {
        String ON = "";

        if (on) {
            if (on instanceof String) {
                ON = (String) on;
            } else if (on instanceof String[]) {
                // multiple selected checkboxes with the same TD name comes back as an array of strings
                // all values are the same so we select the 1st
                String[] onStrings = (String[]) on;
                ON = onStrings[0]
            }

            return GrailsStringUtils.toBoolean(ON)
        }

        return false;
    }

    /**
     * This method will actually call the create assessment method.
     */
    def actuallyCreateAssessment() {
        User user = springSecurityService.currentUser

        CreateAssessmentCommand createAssessmentCommand = null
        CreateAssessmentTdsAndTips tdsAndTips = null
        synchronized (session){
            createAssessmentCommand = (CreateAssessmentCommand) session.getAttribute(CREATE_ASSESSMENT_SESSION_VARIABLE)
            tdsAndTips = (CreateAssessmentTdsAndTips) session.getAttribute(TDS_AND_TIPS_SESSION_VARIABLE)
        }
        if( createAssessmentCommand == null )
            throw new ServletException("Called in invalid context, there is no CreateAssessment available in session.")
        if( tdsAndTips == null )
            throw new ServletException("Called in invalid context, there is no tdAndTips available in session.")


        log.info("Actually creating new assessment...")

        // We sub out the heavy lifting to this function.
        Assessment assessment = doSaveInternal(createAssessmentCommand, user, tdsAndTips)

        flash.message = "Successfully created assessment."
        return redirect(action:'view', id: assessment.id)
    }

    /** This method allows the user to change the assessment's name. */
    def editAssessmentName() {
        Assessment assessment = Assessment.get(params.id)
        [command: new EditAssessmentNameCommand(assessmentId: assessment.id, assessmentName: assessment.assessmentName)]
    }

    def updateName(EditAssessmentNameCommand editAssessmentNameCommand) {
        User user = springSecurityService.currentUser
        Assessment assessment = Assessment.get(editAssessmentNameCommand.assessmentId)
        log.debug("User[$user] is editing the name of assessment ${assessment.id}...")
        assessment.assessmentName = editAssessmentNameCommand.assessmentName
        assessment.save(failOnError: true)
        assessment.logg.addEntry(
            "Update Name",
            "User[${user.username}] updated the assessment's name",
            "User[${user.username}] changed the assessment's name to: ${assessment.assessmentName}",
            [user       : [id: user.id, username: user.username],
             assessment : [id: assessment.id, name: assessment.assessmentName]
            ]
        )
        return redirect(action:'view', id: assessment.id)
    }

    /**
     * Takes the user to the assessment view page.  This is separate from the perform assessment page, this page is
     * about viewing the current status of an assessment rather than modifying it.
     */
    def view(){
        User user = springSecurityService.currentUser
        if(StringUtils.isEmpty(params.id)){
            log.warn("Cannot display assessment when missing id parameter")
            throw new ServletException("Missing id parameter")
        }

        log.debug("Displaying Assessment[${params.id}] to User[${user}]")

        def assessment = Assessment.get(params.id)
        if( !assessment ){
            log.warn("Could not find assessmnet: ${params.id}")
            throw new ServletException("No such assessment ${params.id}")
        }
//        assessment.logg.addEntry("VIEW", "Viewed by ${user.username}", "User ${user} has viewed this assessment.", [userid: user.id])


        def criteria = AssessmentLogEntry.createCriteria()
        def logEntries = criteria.list {
            eq("logg", assessment.logg)
//            not { 'in'('type', ["VIEW"]) } // TODO Add other log entry types we don't want to show.
            maxResults(10)
            order("dateCreated", "desc")
        }

        def logEntryCount = AssessmentLogEntry.countByLogg(assessment.logg)

        log.debug("Sorting assessment steps...")
        def assessmentSteps = new ArrayList<AssessmentStepData>()
        assessmentSteps.addAll(assessment.steps)
        Collections.sort(assessmentSteps, {AssessmentStepData step1, AssessmentStepData step2 ->
            int result = step1.step.trustmarkDefinition.name.compareToIgnoreCase(step2.step.trustmarkDefinition.name)
            if (result == 0) {
                result = step1.step.stepNumber.compareTo(step2.step.stepNumber)
            }
            return result
        } as Comparator)

        def statistics = [
                totalStepCount: assessmentSteps.size(),
                stepsNotMarked: 0,
                stepsMarkedYes: 0,
                stepsMarkedNo: 0,
                stepsMarkedNa: 0,
                stepsSatisfyingArtifactRequirement: 0,
                stepsNotSatisfyingArtifactRequirement: 0
        ]

        // Goal here is to easily map stepData to a list of artifact information.
        def stepDataArtifactsMap = [:]
        def stepDataArtifactStatus = [:]
        for( AssessmentStepData stepData : assessmentSteps ){
            def currentStepArtifactData = []
            int distinctRequiredArtifactsCount = 0
            Map<AssessmentStepArtifact, Boolean> artifactSatisfied = [:]
            stepData.step.artifacts?.each { AssessmentStepArtifact artifact ->
                artifactSatisfied.put(artifact, Boolean.FALSE)
                distinctRequiredArtifactsCount++
            }
            stepData.artifacts.each { actualUserArtifact ->
                if( actualUserArtifact.requiredArtifact == null ){
                    currentStepArtifactData.add([artifact: actualUserArtifact, artifactData: null])
                }else{
                    artifactSatisfied.put( actualUserArtifact.requiredArtifact, Boolean.TRUE )
                    currentStepArtifactData.add([artifact: actualUserArtifact, artifactData: actualUserArtifact.requiredArtifact])
                }
            }
            Boolean allArtifactsSatisfied = Boolean.TRUE
            artifactSatisfied.keySet().each{ key ->
                if( artifactSatisfied.get(key) != Boolean.TRUE )
                    allArtifactsSatisfied = Boolean.FALSE
            }
            stepDataArtifactsMap.put(stepData, currentStepArtifactData)
            stepDataArtifactStatus.put(stepData,
                    [allSatisfied: allArtifactsSatisfied,
                     distinctRequiredArtifactsCount: distinctRequiredArtifactsCount])

            if( allArtifactsSatisfied ){
                statistics['stepsSatisfyingArtifactRequirement'] = statistics['stepsSatisfyingArtifactRequirement'] + 1
            }else{
                statistics['stepsNotSatisfyingArtifactRequirement'] = statistics['stepsNotSatisfyingArtifactRequirement'] + 1
            }

            if( stepData.result == AssessmentStepResult.Not_Applicable ){
                statistics['stepsMarkedNa'] = statistics['stepsMarkedNa'] + 1
            }else if( stepData.result == AssessmentStepResult.Not_Satisfied ){
                statistics['stepsMarkedNo'] = statistics['stepsMarkedNo'] + 1
            }else if( stepData.result == AssessmentStepResult.Satisfied ){
                statistics['stepsMarkedYes'] = statistics['stepsMarkedYes'] + 1
            }else{
                statistics['stepsNotMarked'] = statistics['stepsNotMarked'] + 1
            }
        }

        log.debug("Loading trustmarks for assessment ${assessment?.id}...")
        def trustmarks = Trustmark.findAllByAssessment(assessment)
        if( !trustmarks )
            trustmarks = []
        Collections.sort(trustmarks, {Trustmark tm1, Trustmark tm2 ->
            return tm1.issueDateTime.compareTo(tm2.issueDateTime)
        } as Comparator)


        def charts = [:]
        Slice stepsMarkedYesSlice = Slice.newSlice( percent(statistics.stepsMarkedYes, statistics.totalStepCount), ColorPalette.SUCCESS_TEXT, "Satisfied ("+ statistics.stepsMarkedYes+")" )
        Slice stepsMarkedNoSlice = Slice.newSlice( percent(statistics.stepsMarkedNo, statistics.totalStepCount), ColorPalette.ERROR_TEXT, "Not Satisfied ("+ statistics.stepsMarkedNo+")" )
        Slice stepsMarkedNASlice = Slice.newSlice( percent(statistics.stepsMarkedNa, statistics.totalStepCount), ColorPalette.WARNING_TEXT, "N/A ("+ statistics.stepsMarkedNa+")" )
        Slice stepsNotMarkedSlice = Slice.newSlice( percent(statistics.stepsNotMarked, statistics.totalStepCount), ColorPalette.DEFAULT_BORDER, "Unknown ("+ statistics.stepsNotMarked+")" )
        PieChart stepChart = GCharts.newPieChart(stepsMarkedYesSlice, stepsMarkedNoSlice, stepsMarkedNASlice, stepsNotMarkedSlice)

        // charts4j uses the chart.apis.google.com default endpoint which does not support HTTPS, so using Chrome,
        // all HTTP requests will be redirected to HTTPS. Instead, use the endpoint chart.googleapis.com/chart which
        // does support HTTPS thus preventing redirection.
        stepChart.setURLEndpoint("https://chart.googleapis.com/chart")

        stepChart.setTitle("Assessment Step Status ("+statistics.totalStepCount+" Steps)")
        stepChart.setSize(400, 125)
        stepChart.setThreeD(true)
        charts.put('stepChart', stepChart)
        // TODO HEre is a comment.


        withFormat {
            html {
                [assessment: assessment, assessmentSteps: assessmentSteps, logEntries: logEntries,
                    trustmarks: trustmarks,
                    logEntryCount: logEntryCount, user: user, stepDataArtifactsMap: stepDataArtifactsMap,
                    stepDataArtifactStatus: stepDataArtifactStatus, statistics: statistics, charts: charts]
            }
            json {
                render assessment.toJsonMap(true) as JSON
            }
        }
    }//end view()

    /**
     * Allows a user to download the given assessment, suitable for import into another tool.
     */
    def download() {

    }


    private int percent(Number numerator, Number denominator){
        return (int) ((double) ( numerator.doubleValue() / denominator.doubleValue()) * 100.0d)
    }


    def delete() {
        User user = springSecurityService.currentUser
        log.debug("User[$user] is deleting assessment ${params.id}...")

        Assessment assessment = Assessment.get(params.id)
        if( !assessment )
            throw new ServletException("No such assessment: ${params.id}")

        def oldId = params.id

        log.debug("Assessment found, archiving...")
        def jsonMap = assessment.toJsonMap(false); // Generate a complete JSON representation for archival purposes.  Will it have all necessary data to retrieve later?
        def jsonFile = File.createTempFile("assessment${params.id}-json-save_", ".json")
        jsonFile << (jsonMap as JSON).toString()
        BinaryObject binaryObject = fileService.createBinaryObject(jsonFile, "SYSTEM")
        log.info("Successfully created Assessment[${assessment.id}] backup file as BinaryObject[${binaryObject.id}]")

        log.debug("Starting deletion...")
        def trustmarks = Trustmark.findAllByAssessment(assessment)
        if( trustmarks && !trustmarks.isEmpty() ){
            Collections.sort(trustmarks, {Trustmark tm1, Trustmark tm2 ->
                return tm2.id.compareTo(tm1.id); // Newest First
            } as Comparator)
            trustmarks.each { tm ->
                tm.delete(flush: true)
            }
        }
        assessment.delete(flush: true)

        log.debug("Successfully deleted assessment ${params.id}")
        [assessment: assessment, oldId: oldId, archive: binaryObject]
    }//end delete()

    @Secured(["ROLE_REPORTS_ONLY", "ROLE_USER", "ROLE_ADMIN"])
    def getAssessmentStatusSummary(){
        User user = springSecurityService.currentUser
        log.debug("User[$user] is viewing status summary for assessment ${params.id}...")

        Assessment assessment = Assessment.get(params.id)
        if( !assessment )
            throw new ServletException("No such assessment: ${params.id}")

        log.debug("Checking all answers and required artifact data...")
        boolean allRequiredArtifactsSatisfied = true
        boolean allRequiredParametersFilled = true
        boolean allStepsHaveAnswer = true
        for( AssessmentStepData currentStep : assessment.sortedSteps ){
            if( !currentStep.result || currentStep.result == AssessmentStepResult.Not_Known )
                allStepsHaveAnswer = false
            if( !currentStep.getAreAllAttachmentsSatisfied() )
                allRequiredArtifactsSatisfied = false
            if( !currentStep.getAreAllRequiredParametersFilled() )
                allRequiredParametersFilled = false
        }

        [
                assessment: assessment,
                allRequiredParametersFilled: allRequiredParametersFilled,
                allRequiredArtifactsSatisfied: allRequiredArtifactsSatisfied,
                allStepsHaveAnswer: allStepsHaveAnswer
        ]

    }

    /**
     * Iterates all assessment steps and compiles a list of available artifacts for reuse.  Also adds those from
     * the organization.
     */
    @ParamConversion(paramName="id", toClass=Assessment.class, storeInto = "assessment")
    def listAvailableArtifacts() {
        User user = springSecurityService.currentUser
        Assessment assessment = params.assessment
        if( !assessment )
            throw new ServletException("Missing required field 'id' to resolve an assessment.")

        List<ArtifactInformation> artifacts = []

        log.debug("User[$user] searching assessment #${assessment.id} to build an available artifact list...")
        assessment.getAllArtifacts().each{ ArtifactData currentArtifact ->
            if( currentArtifact.getData() != null ){
                ArtifactInformation ai = new ArtifactInformation(currentArtifact)
                if( !artifacts.contains(ai) ){
                    artifacts.add(ai)
                }
            }
        }
        log.debug("Searching organzation artifacts...")
        assessment.assessedOrganization.artifacts.each{ OrganizationArtifact currentArtifact ->
            if( currentArtifact.getData() != null ){
                ArtifactInformation ai = new ArtifactInformation(currentArtifact)
                if( !artifacts.contains(ai) ){
                    artifacts.add(ai)
                }
            }
        }

        log.debug("Sorting all ${artifacts.size()} artifacts...")
        Collections.sort(artifacts)

        log.debug("Rendering list...")
        withFormat {
            html {
                throw new ServletException("Not Yet Implemented")
            }
            xml {
                render artifacts as XML
            }
            json {
                render artifacts as JSON
            }
        }

    }//end listAvailableArtifacts()


    def viewStepData() {
        User user = springSecurityService.currentUser
        log.debug("User[$user] is requesting step data[${params.stepDataId}] data on assessment[${params.id}]...")

        if(StringUtils.isEmpty(params.id)){
            log.warn("Cannot display assessment log entry when missing id parameter")
            throw new ServletException("Missing id parameter")
        }
        if(StringUtils.isEmpty(params.stepDataId)){
            log.warn("Cannot display assessment step data when missing stepDataId parameter")
            throw new ServletException("Missing stepDataId parameter")
        }

        def assessment = Assessment.get(params.id)
        if( !assessment ){
            log.warn("Could not find assessment: ${params.id}")
            throw new ServletException("No such assessment ${params.id}")
        }

        AssessmentStepData step = AssessmentStepData.get(params.stepDataId)
        if( !step ){
            log.warn("Could not find assessment step data: ${params.stepDataId}")
            throw new ServletException("No such assessment step data: ${params.stepDataId}")
        }

        withFormat {
            html {
                throw new ServletException("Not Yet Implemented")
            }
            xml {
                render step as XML
            }
            json {
                render step.toJsonMap() as JSON
            }
        }

    }//end viewStepData()

    private static final long COPY_TIMEOUT = 10 * 60 * 1000 // 10 minutes
    /**
     * Creates a copy of an assessment.
     */
    def copy() {
        User user = springSecurityService.currentUser
        log.debug("User[$user] creating assessment from copy...")

        if( request.method.toUpperCase() == 'GET' ){
            log.debug("Showing form...")
            [:]
        }else if( request.method.toUpperCase() == 'POST' || request.method.toUpperCase() == 'PUT' ){
            log.info("Creating from copy...")

            log.info("Request parameters:")
            request.parameterNames.each{ param ->
                def val = request.getParameter(param)
                log.info("   [$param]=[$val]")
            }

            def ctx = startAsync()
            ctx.start {
                response.setContentType('application/json')
                PrintWriter writer = findResponse(ctx).getWriter()
                writer.write("[\n")
                writer.flush()

                writeMessage("Copying assessment #${params.assessmentId}...", writer)
                Assessment.withTransaction {
                    try {
                        log.debug("Finding assessment to copy...")
                        writeMessage("Finding assessment ${params.assessmentId}...", writer)
                        Assessment toCopy = Assessment.get(params.assessmentId)
                        if (!toCopy) {
                            throw new ServletException("No such assessment: ${params.assessmentId}")
                        }

                        writeMessage("Creating new assessment...", writer)
                        CreateAssessmentCommand createAssessmentCommand = new CreateAssessmentCommand()
                        bindData(createAssessmentCommand, params)
                        session.setAttribute(CREATE_ASSESSMENT_SESSION_VARIABLE, createAssessmentCommand)

                        if (!createAssessmentCommand.validate()) {
                            log.warn("Errors in create assessment form: ")
                            createAssessmentCommand.errors.allErrors.each { ObjectError error ->
                                throw new ServletException(message(error: error))
                            }
                        }

                        // We sub out the heavy lifting to this function.
                        CreateAssessmentTdsAndTips tdsAndTips = CreateAssessmentTdsAndTips.fromJSON(toCopy.tdsAndTipsJSON)
                        Assessment newAssessment = doSaveInternal(createAssessmentCommand, user, tdsAndTips)

                        List<AssessmentStepData> toCopySteps = toCopy.sortedSteps
                        List<AssessmentStepData> newSteps = newAssessment.sortedSteps
                        writeMessage("Copying assessment step immediate data for all steps(${toCopySteps.size()})...", writer)
                        for (int i = 0; i < toCopySteps.size(); i++) {
                            AssessmentStepData toCopyStep = toCopySteps.get(i)
                            AssessmentStepData newStep = newSteps.get(i)
                            newStep.setImmediateData(toCopyStep)
                            newStep.save(failOnError: true)
                        }//end for each step data.

                        // This cache holds the binary objects needed, to avoid re-copying.
                        Map<String, BinaryObject> cachedBinaries = [:]
                        newAssessment.assessedOrganization.artifacts.each { OrganizationArtifact oa ->
                            cachedBinaries.put(makeBinaryId(oa.data), oa.data)
                        }

                        writeMessage("Copying artifact references for all steps(${toCopySteps.size()})...", writer)
                        for (int i = 0; i < toCopySteps.size(); i++) {
                            AssessmentStepData toCopyStep = toCopySteps.get(i)
                            AssessmentStepData newStep = newSteps.get(i)

                            Set<ArtifactData> artifactDataToCopy = toCopyStep.artifacts
                            if (artifactDataToCopy.size() > 0) {
                                for (ArtifactData toCopyFrom : artifactDataToCopy) {
                                    ArtifactData theCopy = new ArtifactData()
                                    theCopy.dateCreated = toCopyFrom.dateCreated
                                    theCopy.comment = toCopyFrom.comment
                                    theCopy.displayName = toCopyFrom.displayName
                                    theCopy.modifyingUser = toCopyFrom.modifyingUser
                                    theCopy.requiredArtifact = toCopyFrom.requiredArtifact
                                    theCopy.uploadingUser = toCopyFrom.uploadingUser
                                    if (toCopyFrom.data) {
                                        String id = makeBinaryId(toCopyFrom.data)
                                        if (cachedBinaries.containsKey(id)) {
                                            theCopy.data = cachedBinaries.get(id)
                                        } else {
                                            writeMessage("Copying binary object(${toCopyFrom.data.originalFilename}, ${FileUtils.byteCountToDisplaySize(toCopyFrom.data.fileSize)})...", writer)
                                            File contents = toCopyFrom.data.content.toFile()
                                            File contentsCopy = File.createTempFile('assessment_copy-', '.dat')
                                            FileUtils.copyFile(contents, contentsCopy)

                                            BinaryObject binaryCopy = this.fileService.createBinaryObject(contentsCopy)
                                            binaryCopy.mimeType = toCopyFrom.data.mimeType
                                            binaryCopy.originalFilename = toCopyFrom.data.originalFilename
                                            binaryCopy.originalExtension = toCopyFrom.data.originalExtension
                                            binaryCopy.dateCreated = toCopyFrom.data.dateCreated
                                            binaryCopy.createdBy = toCopyFrom.data.createdBy
                                            binaryCopy.save(failOnError: true)

                                            theCopy.data = binaryCopy

                                            cachedBinaries.put(id, binaryCopy)
                                        }
                                    }
                                    theCopy.save(failOnError: true)

                                    newStep.addToArtifacts(theCopy)
                                    newStep.save(failOnError: true)
                                }
                            }

                        }

                        List<AssessmentLogEntry> entriesToCopy = toCopy.getLog().getSortedEntries()
                        writeMessage("Copying assessment log (${entriesToCopy.size()} entries)...", writer)
                        for (int i = 1; i < entriesToCopy.size(); i++) {
                            AssessmentLogEntry logEntry = entriesToCopy.get(i)
                            newAssessment.logg.copyEntry(logEntry)
                        }

                        writeMessageWithData("Copied assessment #${toCopy.id} to assessment #${newAssessment.id}", writer, true,
                                [newAssessmentId: newAssessment.id])
                    } catch (Throwable t) {
                        log.error("Error executing async copy assessment task", t)
                        writeErrorMessage(t.toString(), writer, true)
                        // TODO Rollback transaction...
                        throw t
                    }
                }

                writer.write("]")
                writer.flush()
                ctx.complete()
            }
        }else{
            throw new ServletException("Unsupported operation: ${request.method}.  Expecting GET or POST|PUT")
        }
    }//end copy()

    /**
     * Revoke all trustmarks that were issued for the specified assessment.
     */
    def revokeAllTrustmarksIssuedforAssessment() {
        User user = springSecurityService.currentUser
        if( StringUtils.isEmpty(params.id) ) {
            throw new ServletException("Missing required parameter 'id'.")
        }
        if( StringUtils.isEmpty(params.reason) ) {
            throw new ServletException("Missing required parameter 'reason'.")
        }

        Assessment assessment = Assessment.findById(params.id)
        if( assessment == null ) {
            throw new ServletException("Assessment not found.")
        }

        List<Trustmark> trustmarks = Trustmark.findAllByAssessment(assessment)

        trustmarkService.revokeAll(trustmarks, user, params.reason)

        String type = "All Trustmarks Revoked"
        String title = "All Trustmarks Revoked"
        String message = "User ${user.username} has revoked all trustmarks with reason [${params.reason}] from Organization[${assessment.assessedOrganization.name}] for assessment[${assessment.id}]"
        Map dataMap =  [
                user      : [id: user.id, username: user.username],
                assessment: [id: assessment.id]
        ]
        assessment.logg.addEntry(type, title, message, dataMap)


        Map results = [:]
        results.put("results", "SUCCESS")
        render results as JSON
    }

    def listTrustmarks()  {
        if( StringUtils.isEmpty(params.id) ) {
            throw new ServletException("Missing required parameter 'id'.")
        }

        Assessment assessment = Assessment.findById(params.id)
        if( assessment == null ) {
            throw new ServletException("Assessment not found.")
        }

        log.debug("Loading trustmarks for assessment ${assessment?.id}...")

        def trustmarks = Trustmark.findAllByAssessment(assessment)
        if( !trustmarks ) {
            trustmarks = []
        }
        Collections.sort(trustmarks, {Trustmark tm1, Trustmark tm2 ->
            return tm1.issueDateTime.compareTo(tm2.issueDateTime)
        } as Comparator)

        def trustmarksJson = []
        trustmarks.forEach({ tm -> trustmarksJson.add(tm.toJsonMap())})

        Map results = [:]

        def trustmarkViewBaseUrl = grailsLinkGenerator.link(controller: 'trustmark', action: 'view', absolute: true)

        log.info("trustmarkViewBaseUrl: ${trustmarkViewBaseUrl.toString()}")

        results.put("trustmarkViewBaseUrl", trustmarkViewBaseUrl)
        results.put("records", trustmarksJson)
        render results as JSON
    }

    def listAssessmentLogEntries() {
        if( StringUtils.isEmpty(params.id) ) {
            throw new ServletException("Missing required parameter 'id'.")
        }

        Assessment assessment = Assessment.findById(params.id)
        if( assessment == null ) {
            throw new ServletException("Assessment not found.")
        }

        log.debug("Loading assessment log entries for assessment ${assessment?.id}...")


        def criteria = AssessmentLogEntry.createCriteria()
        def logEntries = criteria.list {
            eq("logg", assessment.logg)
//            not { 'in'('type', ["VIEW"]) } // TODO Add other log entry types we don't want to show.
            maxResults(10)
            order("dateCreated", "desc")
        }

        def assessmentLogEntryViewBaseUrl = grailsLinkGenerator.link(controller: 'assessmentLog', action: 'viewLogEntry',  id: assessment.id, absolute: true)

        Map results = [:]

        def logEntryCount = AssessmentLogEntry.countByLogg(assessment.logg)

        results.put("logEntryCount", logEntryCount)
        results.put("assessmentLogEntryViewBaseUrl", assessmentLogEntryViewBaseUrl)
        results.put("records", logEntries)

        render results as JSON
    }

    //==================================================================================================================
    //  Private Helper Methods
    //==================================================================================================================
    protected String makeBinaryId(BinaryObject bo){
        return bo.originalFilename + "_" + bo.fileSize + "_" + bo.md5sum
    }

    /**
     * When a new CreateAssessment Form is submitted, we process the dynamic TIPs and TDs using this function so that
     * we can drive the remaining TIP resolution process.
     * <br/><br/>
     * @param params
     * @return
     */
    private CreateAssessmentTdsAndTips processParams(Map params){
        CreateAssessmentTdsAndTips tdsAndTips = new CreateAssessmentTdsAndTips()
        for( String paramName : params.keySet() ){
            if( paramName.startsWith("trustmarkDefinition") ){
                Long tdIdentifier = Long.parseLong(params[paramName])
                TrustmarkDefinition td = TrustmarkDefinition.get(tdIdentifier)
                log.info("Resolve TD[${td.name} : ${td.tdVersion}] for params[$paramName]")
                CreateAssessmentTdData tdData = new CreateAssessmentTdData()
                tdData.databaseId = td.id
                tdData.name = td.name
                tdData.version = td.tdVersion
                tdData.description = td.description
                tdData.uri = td.uri
                tdData.paramName = paramName
                tdsAndTips.trustmarkDefinitions.add(tdData)
            }else if( paramName.startsWith("trustInteroperabilityProfile") && !paramName.endsWith("INCLUDEALLTDS")){
                CreateAssessmentTIPData tipData = createAssessmentTIPData(Long.parseLong(params[paramName]))
                if( params.containsKey(paramName+"INCLUDEALLTDS") && params[paramName+"INCLUDEALLTDS"].toString().equalsIgnoreCase("on") ){
                    log.debug("Identified to use ALL TDs on TIP: ${tipData.uri}")
                    tipData.processed = true
                    tipData.useAllTds = true
                }else{
                    tipData.processed = false
                    tipData.useAllTds = false
                }
                tdsAndTips.trustInteroperabilityProfiles.add(tipData)
            }
        }
        return tdsAndTips
    }//end processParams()

    /**
     * create an AssessmentTIPData based on the tip identifier
     * @param tipId
     * @return
     */
    protected CreateAssessmentTIPData createAssessmentTIPData(Long tipId)  {
        TrustInteroperabilityProfile tip = TrustInteroperabilityProfile.get(tipId)
        log.info("Resolve TIP[${tip.name} : ${tip.tipVersion}] for param [${tipId}]")
        CreateAssessmentTIPData tipData = new CreateAssessmentTIPData()
        tipData.databaseId = tip.id
        tipData.name = tip.name
        tipData.description = tip.description
        tipData.version = tip.tipVersion
        tipData.uri = tip.uri
        tipData.processed = false
        tipData.useAllTds = false
        return tipData;
    }

    protected Assessment doSaveInternal( CreateAssessmentCommand createAssessmentCommand, User user, CreateAssessmentTdsAndTips tdsAndTips ) {
        Organization org = handleOrg(user, createAssessmentCommand)
        ContactInformation contact = handleContactInformation(user, org, createAssessmentCommand)

        log.debug("Creating new assessment...")
        Assessment assessment = new Assessment(createdBy: user)
        assessment.assessmentName = createAssessmentCommand.assessmentName
        assessment.assessedContact = contact
        assessment.assessedOrganization = org
        assessment.status = AssessmentStatus.CREATED
        assessment.tdsAndTipsJSON = tdsAndTips.toJSON().toString(); // Make sure we store the create information for later.
        assessment.save(failOnError: true, flush: true)

        log.debug("Creating assessment log...")
        AssessmentLog assessmentlog = new AssessmentLog(assessment: assessment)
        assessmentlog.save(failOnError: true, flush: true)
        assessment.logg = assessmentlog
        assessment.save(failOnError: true, flush: true)

        log.debug("Creating first assessment log entry..")
        assessmentlog.addEntry("Creation", "Assessment Created",
                "User ${user.username} has created assessment[${assessment.id}]  for organization ${org.uri}.",
                [user: user.toJsonMap(), organization: org.toJsonMap(true), contact: contact.toJsonMap()])


        // The following code will need to be repeated for ALL trustmark definitions attached to this assessment, not just one like before.

        log.debug("Associating TDs with new Assessment...")
        List<TrustmarkDefinition> tds = [] // Holds the set of all unique TDs so we only add it once.
        if( tdsAndTips.trustmarkDefinitions?.size() > 0 ) {
            for( CreateAssessmentTdData tdData : tdsAndTips.trustmarkDefinitions ){
                TrustmarkDefinition td = TrustmarkDefinition.get(tdData.databaseId)
                if( !tds.contains(td) ){
                    tds.add(td)
                    AssessmentTrustmarkDefinitionLink link = new AssessmentTrustmarkDefinitionLink(trustmarkDefinition: td, assessment: assessment)
                    link.index = tds.size()
                    link.save(failOnError: true)
                }
            }
        }
        if( tdsAndTips.trustInteroperabilityProfiles.size() > 0 ){
            for( CreateAssessmentTIPData tipData : tdsAndTips.trustInteroperabilityProfiles ){
                log.debug("Finding TDs from TIP[${tipData.uri}]...")
                TrustInteroperabilityProfile tip = TrustInteroperabilityProfile.get(tipData.databaseId)
                List<TrustmarkDefinition> tdsToUse = []
                if( tipData.useAllTds ){
                    log.debug("Filling all TDs from ${tip.uri}")
                    fillTds(tip, tdsToUse)
                    log.debug("Filled ${tdsToUse.size()} TDs from ${tip.uri}")
                }else{
                    for( String tdUri : tipData.tdUris ){
                        TrustmarkDefinition td = TrustmarkDefinition.findByUri(tdUri)
                        if( !tdsToUse.contains(td) )
                            tdsToUse.add(td)
                    }
                }
                for( TrustmarkDefinition curTd : tdsToUse ){
                    if( !tds.contains(curTd) ){
                        tds.add(curTd)
                        AssessmentTrustmarkDefinitionLink link = new AssessmentTrustmarkDefinitionLink(trustmarkDefinition: curTd, assessment: assessment)
                        link.index = tds.size()
                        link.fromTip = tip
                        link.save(failOnError: true)
                    }
                }
            }
        }

        log.debug("Creating assessment step data...")
        int stepCount = 0
        for( TrustmarkDefinition definition : tds ) {
            List<AssessmentStep> assessmentSteps = []
            assessmentSteps.addAll(definition.assessmentSteps)
            Collections.sort(assessmentSteps, { AssessmentStep step1, AssessmentStep step2 -> return step1.stepNumber.compareTo(step2.stepNumber) } as Comparator)
            for( AssessmentStep assessmentStep : assessmentSteps ){
                AssessmentStepData stepData = new AssessmentStepData()
                stepData.assessment = assessment
                stepData.step = assessmentStep
                stepData.result = AssessmentStepResult.Not_Known
                stepData.save(failOnError: true, flush: true)
                assessment.addToSteps(stepData)
                stepCount++
            }
        }

        log.debug("Created ${stepCount} assessment steps")
        assessment.save(failOnError: true); // Record all assessment steps added in last segment.

        assessment.status = AssessmentStatus.WAITING
        assessment.save(failOnError: true, flush: true)
        assessmentlog.addEntry("Status Change", "Assessment Status Change: ${AssessmentStatus.CREATED} -> ${AssessmentStatus.WAITING}",
                "User ${user.username} has changed assessment[${assessment.id}] status from ${AssessmentStatus.CREATED} to ${AssessmentStatus.WAITING}.",
                [user: [id: user.id, usenrame: user.username], assessment: [id: assessment.id], oldStatus: AssessmentStatus.CREATED.toString(), newStatus: AssessmentStatus.WAITING.toString()])


        log.info("User ${user.username} has Successfully created assessment[${assessment.id}] for organization ${org.uri}.")
        return assessment
    }

    /**
     * A recursive function which fills the given list of TDs with TDs found in the the give tip and all of its sub tips.
     */
    protected void fillTds(TrustInteroperabilityProfile tip, List<TrustmarkDefinition> tds){
        if( tip.getReferences() != null && tip.getReferences().size() > 0 ){
            for( TIPReference tipRef : tip.getReferences() ){
                if( tipRef.trustmarkDefinition != null ){
                    if( !tds.contains(tipRef.trustmarkDefinition) ){
                        tds.add(tipRef.trustmarkDefinition)
                    }
                }else if( tipRef.trustInteroperabilityProfile != null){
                    fillTds(tipRef.trustInteroperabilityProfile, tds)
                }
            }
        }
    }

    /**
     * Writes a JSON message to the user.
     */
    protected void writeMessage( String msg, PrintWriter writer, boolean last = false ){
        log.debug(msg)
        writer << """{ "id": "${UUID.randomUUID().toString()}", "status": "OK", "message": "${msg}" }"""
        if( !last )
            writer << ", \n"
        writer.flush()
        Thread.sleep(100)
    }//end writeMessage()

    protected void writeMessageWithData( String msg, PrintWriter writer, boolean last = false, Map data ){
        log.debug(msg)
        writer << """{ "id": "${UUID.randomUUID().toString()}", "status": "OK", "message": "${msg}" """
        data.keySet().each{ key ->
            writer << ", \"${key}\": "
            Object value = data.get(key)
            if( value instanceof Number ){
                writer << value.toString()
            }else{
                writer << "\"${value.toString()}\"" // In theory, we should escape this
            }
        }
        writer << "}"
        if( !last )
            writer << ", \n"
        writer.flush()
        Thread.sleep(100)
    }//end writeMessage()

    protected void writeErrorMessage( String msg, PrintWriter writer, boolean last = false ){
        log.error(msg)
        writer << """{ "id": "${UUID.randomUUID().toString()}", "status": "ERROR", "message": "${msg}" }"""
        if( !last )
            writer << ", \n"
        writer.flush()
        Thread.sleep(100)
    }//end writeMessage()


    protected ServletResponse findResponse(AsyncContext ac) {
        ServletResponse response = ac.getResponse()
        while (response instanceof ServletResponseWrapper) {
            response = ((ServletResponseWrapper)response).getResponse()
        }
        return response
    }


    private Organization handleOrg(User user, CreateAssessmentCommand createAssessmentCommand){
        Organization org = new Organization()
        if( createAssessmentCommand.existingOrgId ){
            log.debug("Finding organization with id[${createAssessmentCommand.existingOrgId}]...")
            org = Organization.get(createAssessmentCommand.existingOrgId)
            if( !org ){
                log.warn("User[$user] has requested new assessment for org[${createAssessmentCommand.existingOrgId}] which cannot be found!")
                throw new ServletException("Unable to find any Organization with id=${createAssessmentCommand.existingOrgId}")
            }
            if( !org.uri.equalsIgnoreCase(createAssessmentCommand.organizationUri) ){
                log.warn("For organization[${org.id}], User[${user}] is modifying URN from [${org.uri}] to [${createAssessmentCommand.organizationUri}].")
                org.addTrustmarkRecipientIdentifierUri(createAssessmentCommand.organizationUri)
                org.save(failOnError: true, flush: true)
            }
            if( !org.name.equalsIgnoreCase(createAssessmentCommand.organizationName) ){
                log.warn("For organization[${org.id}], User[${user}] is modifying name from [${org.name}] to [${createAssessmentCommand.organizationName}].")
                org.name = createAssessmentCommand.organizationName
                org.save(failOnError: true, flush: true)
            }
        }else{ // This organization doesn't already exist.
            log.debug("Creating new organization[${createAssessmentCommand.organizationUri}]...")
            Organization newOrg = Organization.newOrganization(createAssessmentCommand.organizationUri, "",
                    createAssessmentCommand.organizationName, false)
            newOrg.save(failOnError: true, flush: true)
        }
        return org
    }//end handleOrg()

    private ContactInformation handleContactInformation(User user, Organization org, CreateAssessmentCommand command){
        ContactInformation contact = null
        if( command.existingContactId ){
            contact = ContactInformation.get(command.existingContactId)
            if( !contact ){
                log.warn("User[$user] has requested new assessment for existing contact info[${command.existingContactId}] which cannot be found!")
                throw new ServletException("Unable to find any Contact Information with id=${command.existingContactId}")
            }
            updateContact(contact, command)
        }else{
            contact = new ContactInformation()
            contact.email = command.contactEmail
            contact.responder = command.contactResponder
            contact.notes = command.contactNotes
            contact.phoneNumber = command.contactPhoneNumber
            contact.mailingAddress = command.contactMailingAddress
            contact.save(failOnError: true, flush: true)
        }
        affiliate(contact, org)
        return contact
    }//end handleContactInformation()

    private void affiliate( ContactInformation contact, Organization org ){
        if( !isContactAffiliatedWithOrg(org, contact) ){
            if( org.primaryContact == null ){
                org.primaryContact = contact
            }else{
                org.addToContacts(contact)
            }
            log.debug("Updating organization[${org.id}: ${org.uri}] to add affiliation with contact[${contact.id}: ${contact.email}]...")
            org.save(failOnError: true, flush: true)
        }
    }

    private Boolean isContactAffiliatedWithOrg(Organization org, ContactInformation contact){
        boolean affiliated = false
        def contactsAtOrg = []
        if( org.primaryContact )
            contactsAtOrg.add( org.primaryContact )
        if( org.contacts )
            contactsAtOrg.addAll( org.contacts )
        for( ContactInformation current : contactsAtOrg ){
            if( current.id == contact.id ){
                affiliated = true
                break
            }
        }
        return affiliated
    }//end isContactAffiliatedWithOrg()

    private void updateContact( ContactInformation contact, CreateAssessmentCommand command ){
        if( command.existingContactBeingEdited ) {
            boolean fieldChanged = false
            if (contact.email != command.contactEmail) {
                log.debug("Updating contact[${contact.id}] email from [${contact.email}] to [${command.contactEmail}]")
                contact.email = command.contactEmail
                fieldChanged = true
            }
            if (contact.responder != command.contactResponder) {
                log.debug("Updating contact[${contact.id}] responder from [${contact.responder}] to [${command.contactResponder}]")
                contact.responder = command.contactResponder
                fieldChanged = true
            }
            if (contact.phoneNumber != command.contactPhoneNumber) {
                log.debug("Updating contact[${contact.id}] phone number from [${contact.phoneNumber}] to [${command.contactPhoneNumber}]")
                contact.phoneNumber = command.contactPhoneNumber
                fieldChanged = true
            }
            if (contact.mailingAddress != command.contactMailingAddress) {
                log.debug("Updating contact[${contact.id}] mailing address from [${contact.mailingAddress}] to [${command.contactMailingAddress}]")
                contact.mailingAddress = command.contactMailingAddress
                fieldChanged = true
            }
            if (contact.notes != command.contactNotes) {
                log.debug("Updating contact[${contact.id}] notes from [${contact.notes}] to [${command.contactNotes}]")
                contact.notes = command.contactNotes
                fieldChanged = true
            }
            if (fieldChanged) {
                log.debug("Updating contact[${contact.id} - ${contact.email}] in database...")
                contact.save(failOnError: true, flush: true)
            }
        }
    }//end updateContact()

}//end AssessmentController

/**
 * Representation of the create assessment form.
 */
class CreateAssessmentCommand {
    static Logger log = LoggerFactory.getLogger(CreateAssessmentCommand.class)

    public static CreateAssessmentCommand fromUser( User user ){
        CreateAssessmentCommand cmd = new CreateAssessmentCommand();
        cmd.existingOrgId = user.organization.id;
        cmd.organizationUri = user.organization.uri;
        cmd.organizationName = user.organization.name;

        // get contact information from the user organization
        cmd.existingContactId = user.organization.primaryContact.id;
        cmd.contactResponder = user.organization.primaryContact.responder;
        cmd.contactEmail = user.organization.primaryContact.email;
        cmd.contactPhoneNumber = user.organization.primaryContact.phoneNumber;
        cmd.contactMailingAddress = user.organization.primaryContact.mailingAddress;
        cmd.contactNotes = user.organization.primaryContact.notes;

        return cmd;
    }

    String assessmentName
    Integer existingOrgId; // Immutable database key (in case they selected an existing organziation)
    String organizationUri
    String organizationName

    Integer existingContactId; // Immutable database key (in case they selected an existing contact)
    Boolean existingContactBeingEdited = Boolean.FALSE // To preserve javascript state between server invocations.
    String contactResponder
    String contactEmail
    String contactPhoneNumber
    String contactMailingAddress
    String contactNotes

    static constraints = {
        existingOrgId(nullable: true, validator: {val, obj, errors ->
            log.debug("Create Assessment org ID validation...")
            if( val ){
                log.debug("  Org id has a value: ${val}")
                Organization.withTransaction {
                    Organization org = Organization.get(val)
                    if( !org ){
                        log.warn("No such org: $val")
                        return "org.does.not.exist"
                    }else{
                        log.debug("Org ${val} exists, validating URI...")
                        Organization uriConflictOrg = Organization.findByUri(obj.organizationUri)
                        if( uriConflictOrg ) {
                            log.debug("For URN ${obj.organizationUri}, found org: ${uriConflictOrg.name}")
                            if (uriConflictOrg && uriConflictOrg.id != val) {
                                errors.rejectValue("organizationUri", "org.uri.exists", [obj.organizationUri] as String[], "Organizaiton URI ${obj.organizationUri} already exists.")
                                return false
                            } else {
                                log.debug("A urn conflict was not detected.")
                            }
                        }else{
                            log.debug("No Org with URI[${obj.organizationUri}] exists in database to conflict.")
                        }

                        Organization nameConflictOrg = Organization.findByName(obj.organizationName)
                        if( nameConflictOrg ) {
                            log.debug("For name ${obj.organizationName}, found org: ${nameConflictOrg.uri}")
                            if (nameConflictOrg && nameConflictOrg.id != val) {
                                errors.rejectValue("organizationName", "org.name.exists", [obj.organizationName] as String[], "Organizaiton name ${obj.organizationName} already exists.")
                                return false
                            } else {
                                log.debug("A name conflict was not detected.")
                            }
                        }else{
                            log.debug("No Org Name[${obj.organizationName}] exists in the database to conflict.")
                        }
                    }
                }
            }else{
                log.debug("No value was given in the existing org id field.")
            }
            log.debug("Successfully validated org id.")
        })
        assessmentName(blank: false)
        organizationUri(nullable: false, blank: false, maxSize: 256, validator: { val, obj, errors ->
            if( !obj.existingOrgId ){
                log.debug("Validating Org URI...")
                Organization uriConflictOrg = Organization.findByUri(val)
                if( uriConflictOrg ){
                    log.debug("For URN ${val}, found org[${uriConflictOrg.id}]: ${uriConflictOrg.name}")
                    errors.rejectValue("organizationUri", "org.uri.exists", [val] as String[], "Organizaiton URI ${val} already exists.")
                    return false
                }else{
                    log.debug("An organization URI conflict was not detected.")
                }
            }
        })
        organizationName(nullable: false, blank: false, maxSize: 128, validator: { val, obj, errors ->
            if( !obj.existingOrgId ) {
                Organization nameConflictOrg = Organization.findByName(val)
                if( nameConflictOrg ){
                    log.debug("For name ${val}, found org[${nameConflictOrg.id}]: ${nameConflictOrg.urn}")
                    errors.rejectValue("organizationName", "org.name.exists", [val] as String[], "Organizaiton name ${val} already exists.")
                    return false
                } else {
                    log.debug("A name conflict was not detected.")
                }
            }
        })

        existingContactId(nullable: true, validator: {val, obj ->
            if( val ){
                ContactInformation.withTransaction {
                    ContactInformation contact = ContactInformation.get(val)
                    if( !contact ){
                        return "contact.does.not.exist"
                    }
                }
            }
        })
        existingContactBeingEdited(nullable: false)
        contactEmail(nullable: false, blank: false, maxSize: 128, email: true, validator: { val, obj, errors ->
            ContactInformation existingContact = ContactInformation.findByEmail(val)
            if( existingContact ) {
                if (!obj.existingContactId) {
                    log.warn("Creating new contact with email ${val} which conflicts with existing contact[${existingContact.id}]: ${existingContact.responder}")
                } else if (existingContact.id != obj.existingContactId) {
                    log.warn("Updating contact[${obj.existingContactId}] with email ${val} which conflicts with existing contact[${existingContact.id}]: ${existingContact.responder}")
                }
            }
        })
        contactResponder(nullable: true, blank: true, maxSize: 255)
        contactPhoneNumber(nullable: true, blank: true, maxSize: 32)
        contactMailingAddress(nullable: true, blank: true, maxSize: 512)
        contactNotes(nullable: true, blank: true, maxSize: 65535)

    }

}//end CreateAssessmentCommand

class EditAssessmentNameCommand {
    long assessmentId
    String assessmentName

    static constraints = {
        assessmentId(nullable: false)
        assessmentName(blank: false)
    }
}
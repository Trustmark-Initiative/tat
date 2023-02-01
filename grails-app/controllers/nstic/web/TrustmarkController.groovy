package nstic.web

import assessment.tool.TrustmarkService
import grails.converters.JSON
import grails.converters.XML
import grails.gorm.transactions.Transactional
import nstic.TrustmarkIdentifierGenerator
import nstic.util.AssessmentToolProperties
import nstic.web.assessment.Assessment
import nstic.web.assessment.AssessmentStatus
import nstic.web.assessment.AssessmentStepData
import nstic.web.assessment.AssessmentTrustmarkDefinitionLink
import nstic.web.assessment.ParameterValue
import nstic.web.assessment.Trustmark
import nstic.web.assessment.TrustmarkStatus
import nstic.web.td.TdParameter
import nstic.web.td.TrustmarkDefinition
import org.apache.commons.lang.StringUtils
import org.grails.help.ParamConversion
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.validation.ObjectError
import javax.servlet.ServletException
import javax.xml.bind.DatatypeConverter
import java.text.SimpleDateFormat

/**
 * Created by brad on 9/9/14.
 */
@Transactional
@PreAuthorize('hasAnyAuthority("tat-contributor", "tat-admin")')
class TrustmarkController {

    // Thread related variables
    public static final String INFO_LIST_THREAD_VAR = TrustmarkController.class.getName()+".INFO_LIST_THREAD"
    public static final String TRUSTMARK_GENERATION_THREAD_VAR = TrustmarkController.class.getName()+".TRUSTMARK_GENERATION_THREAD"

    def trustmarkService

    /**
     * Lists all trustmarks available in the system
     */
    def list(){
        log.debug("Listing Trustmarks...")
        if (!params.max)
            params.max = '20'
        params.max = Math.min(100, Integer.parseInt(params.max)).toString(); // Limit to at most 100 orgs at a time.

        User user = User.findByUsername(((OAuth2AuthenticationToken) SecurityContextHolder.getContext().getAuthentication()).getName())

        // case for filtering by recipient organizations
        if (params.containsKey("id") || user.isUser()) {
            Integer orgId = -1
            if (user.isUser() ) {
                orgId = user.organization.id
            } else {
                orgId = Integer.parseInt(params["id"])
            }
            Organization org = Organization.findById(orgId)
            def trustmarks = Trustmark.findAllByRecipientOrganization(org)
            [trustmarks: trustmarks, trustmarkCountTotal: trustmarks.size()]
        } else {
            [trustmarks: Trustmark.list(params), trustmarkCountTotal: Trustmark.count()]
        }
    }//end list()

    /**
     * An ajax method meant to return a list of Trustmark Definitions and their assessment status, based on the
     * assessment Id.  This method takes a while to churn, depending on the number of trustmarks (hence AJAX call pattern).
     */

    @ParamConversion(paramName="assessmentId", toClass=Assessment.class, storeInto = "assessment")
    def retrieveTrustmarkDefinitionsDisplayInfo() {
        log.debug("retrieveTrustmarkDefinitionsDisplayInfo...")

        // Do not start a new thread if a thread is already running
        if (!trustmarkService.isExecuting(TrustmarkService.INFO_LIST_EXECUTING_VAR)) {

            log.debug("retrieveTrustmarkDefinitionsDisplayInfo: Starting a new INFO_LIST_THREAD_VAR")

            // initialize status
            trustmarkService.setAttribute(TrustmarkService.INFO_LIST_MESSAGE_VAR, "Building display information for Trustmark Definitions")
            trustmarkService.setAttribute(TrustmarkService.INFO_LIST_STATUS_VAR, "RUNNING")
            trustmarkService.setAttribute(TrustmarkService.INFO_LIST_PERCENT_VAR, "0")

            trustmarkService.setExecuting(TrustmarkService.INFO_LIST_EXECUTING_VAR)

            log.debug("Create trustmark called...")
            User user = User.findByUsername(((OAuth2AuthenticationToken) SecurityContextHolder.getContext().getAuthentication()).getName())
            Assessment assessment = params.assessment

            final Integer assessmentId = assessment.id

            Thread infoListThread = new Thread(new Runnable() {
                @Override
                void run() {
                    trustmarkService.createGrantTrustmarkInfoList(assessmentId)
                }
            })
            trustmarkService.setAttribute(INFO_LIST_THREAD_VAR, infoListThread)
            infoListThread.start()

            log.debug(trustmarkService.getAttributesString("retrieveTrustmarkDefinitionsDisplayInfo"))
        }

        ["Building display information for TDs"]
    }

    @ParamConversion(paramName="assessmentId", toClass=Assessment.class, storeInto = "assessment")
    def getCreateInfoList() {
        log.debug("getCreateInfoList...")

        User user = User.findByUsername(((OAuth2AuthenticationToken) SecurityContextHolder.getContext().getAuthentication()).getName())
        Assessment assessment = params.assessment

        List<GrantTrustmarkInfo> infoList = trustmarkService.getAttribute(TrustmarkService.GENERATED_TD_INFO_LIST_VAR)

        log.debug("infoList size: " + infoList.size().toString())

        [infoList: infoList, assessment: assessment]
    }

    def trustmarkDefinitionsInfoListStatusUpdate() {
        log.info("** trustmarkDefinitionsInfoListStatusUpdate().")

        Map jsonResponse = [:]
        log.debug("Calculating trustmark definitions info list status update...")
        jsonResponse.put("status", trustmarkService.getAttribute(TrustmarkService.INFO_LIST_STATUS_VAR))

        String percentString = trustmarkService.getAttribute(TrustmarkService.INFO_LIST_PERCENT_VAR)

        int percentInt = 0
        if( StringUtils.isNotEmpty(percentString) ){
            percentInt = Integer.parseInt(percentString.trim())
        }
        jsonResponse.put("percent", percentInt)
        jsonResponse.put("message", trustmarkService.getAttribute(TrustmarkService.INFO_LIST_MESSAGE_VAR))

        log.info("trustmarkDefinitionsInfoListStatusUpdate::jsonResponse: " + jsonResponse.toString())

        render jsonResponse as JSON
    }

    def trustmarkGenerationStatusUpdate() {
        log.info("** trustmarkGenerationStatusUpdate().")

        Map jsonResponse = [:]
        log.debug("Calculating trustmark generation status update...")
        jsonResponse.put("status", trustmarkService.getAttribute(TrustmarkService.TRUSTMARK_GENERATION_STATUS_VAR))
        String percentString = trustmarkService.getAttribute(TrustmarkService.TRUSTMARK_GENERATION_PERCENT_VAR)

        int percentInt = 0
        if( StringUtils.isNotEmpty(percentString) ){
            percentInt = Integer.parseInt(percentString.trim())
        }
        jsonResponse.put("percent", percentInt)
        jsonResponse.put("message", trustmarkService.getAttribute(TrustmarkService.TRUSTMARK_GENERATION_MESSAGE_VAR))

        log.info("trustmarkGenerationStatusUpdate::jsonResponse: " + jsonResponse.toString())

        render jsonResponse as JSON
    }

    /**
     * Called to create a new set of Trustmarks, based on an existing assessment.  This action will calculate the command object
     * necessary for the create page form (setting all the default values), and then show the create page form.
     */
    @ParamConversion(paramName="assessmentId", toClass=Assessment.class, storeInto = "assessment")
    def create() {
        log.debug("Create trustmark called...")
        User user = User.findByUsername(((OAuth2AuthenticationToken) SecurityContextHolder.getContext().getAuthentication()).getName())
        Assessment assessment = params.assessment
        if( assessment == null )
            throw new InvalidRequestError("Could not locate any assessment from assessmentId=${params.assessmentId}")

        if( !assessment.getIsComplete() || assessment.status == AssessmentStatus.ABORTED ){
            log.warn("Cannot grant assessment ${assessment.id} a trustmark, because it is not marked success or fail.")
            throw new ServletException("Cannot grant assessment ${assessment.id} a trustmark, because it is not marked success or fail.")
        }

        // Check if operation is active, and if so, interrupt the operation and wait for the thread to finish
        if (trustmarkService.isExecuting(TrustmarkService.INFO_LIST_EXECUTING_VAR)) {
            trustmarkService.stopExecuting(TrustmarkService.INFO_LIST_EXECUTING_VAR)

            trustmarkService.setAttribute(TrustmarkService.INFO_LIST_STATUS_VAR, "CANCELLING")
            trustmarkService.setAttribute(TrustmarkService.INFO_LIST_MESSAGE_VAR, "Cancelling trustmark definition info list generation...")

            log.debug("Interrupting previous TD Info List thread...")
            Thread t = trustmarkService.getAttribute(INFO_LIST_THREAD_VAR)
            if (t &&  t.isAlive()) {
                t.join()
                log.debug("Interrupted previous TD Info List thread...")
            }
        }

        // initialize service attribute set
        trustmarkService.resetAttributes()

        // initialize status
        trustmarkService.setAttribute(TrustmarkService.INFO_LIST_STATUS_VAR, "RUNNING")
        trustmarkService.setAttribute(TrustmarkService.INFO_LIST_PERCENT_VAR, "0")
        trustmarkService.setAttribute(TrustmarkService.INFO_LIST_MESSAGE_VAR, "Preparing to build display information for Trustmark Definitions")

        // reset the info list
        trustmarkService.removeAttribute(TrustmarkService.GENERATED_TD_INFO_LIST_VAR)

        long startTime = System.currentTimeMillis()

        List<TrustmarkDefinition> trustmarkDefinitions = []
        for(AssessmentTrustmarkDefinitionLink link : assessment.getSortedTds() ){
            if( !trustmarkDefinitions.contains(link.trustmarkDefinition) ){
                trustmarkDefinitions.add(link.trustmarkDefinition)
            }
        }

        long stopTime = System.currentTimeMillis()
        log.info("*** Loading TDs time: ${(stopTime - startTime)}ms.")

        log.debug("Listing and sorting all organization's trustmark recipient identifier instances...")
        List<TrustmarkRecipientIdentifier> trustmarkRecipientIdentifierList =
                TrustmarkRecipientIdentifier.findAllByOrganization(assessment.assessedOrganization)

        log.debug("Listing and sorting all TrustmarkMetdata instances...")
        List<TrustmarkMetadata> metadataList = TrustmarkMetadata.findAll()
        Collections.sort(metadataList, {m1, m2 -> return m1.name.compareToIgnoreCase(m2.name); } as Comparator)

        log.debug(trustmarkService.getAttributesString("create"))

        [assessment: assessment,
         metadataList: metadataList,
         trustmarkDefinitions: trustmarkDefinitions,
         trustmarkRecipientIdentifierList: trustmarkRecipientIdentifierList]
    }//end create()

    @ParamConversion(paramName="assessmentId", toClass=Assessment.class, storeInto = "assessment")
    def cancelCreateInfoList(){
        User user = User.findByUsername(((OAuth2AuthenticationToken) SecurityContextHolder.getContext().getAuthentication()).getName())
        Assessment assessment = params.assessment
        log.debug("Request to cancel grant Trustmarks...")

        // Check if operation is active, interrupt operation and wait for the thread to finish
        if (trustmarkService.isExecuting(TrustmarkService.INFO_LIST_EXECUTING_VAR)) {
            trustmarkService.stopExecuting(TrustmarkService.INFO_LIST_EXECUTING_VAR)

            trustmarkService.setAttribute(TrustmarkService.INFO_LIST_STATUS_VAR, "CANCELLING")
            trustmarkService.setAttribute(TrustmarkService.INFO_LIST_MESSAGE_VAR, "Cancelling trustmark definition info list generation...")

            log.debug("Interrupting previous TD Info List thread...")
            Thread t = trustmarkService.getAttribute(INFO_LIST_THREAD_VAR)
            if (t &&  t.isAlive()) {
                t.join()
                log.debug("Interrupted TD Info List thread...")
            }
        }

        // Base url includes the TAT domain name, createLink includes the TAT domain name
        // Get the host and port from the base url and append the result of createLink to avoid duplicate TAT domain
        URL url = new URL(AssessmentToolProperties.getProperties().getProperty(AssessmentToolProperties.BASE_URL))
        def baseUrl = url.protocol + "://" + url.getAuthority()

        def jsonResponse = [href: baseUrl + createLink(controller: 'assessment', action: 'view', id: assessment.id)]

        log.debug(trustmarkService.getAttributesString("save"))

        render jsonResponse as JSON

    }//end cancelCreateInfoList()

    def getExpirationData() {
        Integer metadataId = Integer.parseInt(params.selectedMetadataId)

        TrustmarkMetadata trustmarkMetadata = TrustmarkMetadata.findById(metadataId)

        if (trustmarkMetadata == null) {
            log.error("trustmark metadata with id: ${metadataId} does not exist...")
            throw new ServletException("No such trustmark metaadta set: ${metadataId}")
        }

        SigningCertificate signingCertificate = SigningCertificate.findById(
                trustmarkMetadata.defaultSigningCertificateId)

        def certificateExpirationDateString = signingCertificate.expirationDate.toString()

        int timePeriodNoExcepionts  = trustmarkMetadata.timePeriodNoExceptions
        int timePeriodWithExceptions  = trustmarkMetadata.timePeriodWithExceptions

        int assessingOrganizationId = trustmarkMetadata.provider.id

        def model = [certificateExpirationDate: certificateExpirationDateString,
                     timePeriodNoExcepionts:    timePeriodNoExcepionts,
                     timePeriodWithExceptions:  timePeriodWithExceptions,
                     assessingOrganizationId:   assessingOrganizationId]

        render model as JSON
    }

    @ParamConversion(paramName="assessmentId", toClass=Assessment.class, storeInto = "assessment")
    def generateTrustmarks() {
        log.debug("Generate trustmark called...")
        User user = User.findByUsername(((OAuth2AuthenticationToken) SecurityContextHolder.getContext().getAuthentication()).getName())
        Assessment assessment = params.assessment
        if( assessment == null )
            throw new InvalidRequestError("Could not locate any assessment from assessmentId=${params.assessmentId}")

        if( !assessment.getIsComplete() || assessment.status == AssessmentStatus.ABORTED ){
            log.warn("Cannot grant assessment ${assessment.id} a trustmark, because it is not marked success or fail.")
            throw new ServletException("Cannot grant assessment ${assessment.id} a trustmark, because it is not marked success or fail.")
        }

        // Check if thread is running, interrupt thread and wait for it to finish
        if (trustmarkService.isExecuting(TrustmarkService.TRUSTMARK_GENERATION_EXECUTING_VAR)) {
            trustmarkService.stopExecuting(TrustmarkService.TRUSTMARK_GENERATION_EXECUTING_VAR)

            trustmarkService.setAttribute(TrustmarkService.TRUSTMARK_GENERATION_STATUS_VAR, "CANCELLING")
            trustmarkService.setAttribute(TrustmarkService.TRUSTMARK_GENERATION_MESSAGE_VAR, "Cancelling trustmark generation...")

            log.debug("Interrupting previous TM Generation thread...")
            Thread t = trustmarkService.getAttribute(TRUSTMARK_GENERATION_THREAD_VAR)
            if (t &&  t.isAlive()) {
                t.join()
                log.debug("Interrupted previous TM Generation thread...")
            }
        }

        // reset progress
        trustmarkService.setAttribute(TrustmarkService.TRUSTMARK_GENERATION_MESSAGE_VAR, "About to generate trustmarks")
        trustmarkService.setAttribute(TrustmarkService.TRUSTMARK_GENERATION_STATUS_VAR, "PREPARING")
        trustmarkService.setAttribute(TrustmarkService.TRUSTMARK_GENERATION_PERCENT_VAR, "0")

        // reset trustmark list
        trustmarkService.removeAttribute(TrustmarkService.GENERATED_TRUSTMARK_LIST_VAR)

        Map paramsMap = new HashMap()
        params.each { key, value ->
            paramsMap.put(key, value)
        }

        trustmarkService.setAttribute(TrustmarkService.TRUSTMARK_DEFINITIONS_PARAMS_MAP_VAR, paramsMap)

        log.debug(trustmarkService.getAttributesString("generateTrustmarks"))

        [assessment: assessment]
    }

    @ParamConversion(paramName="assessmentId", toClass=Assessment.class, storeInto = "assessment")
    def generateTrustmarkList() {

        log.debug("generateTrustmarkList...")

        User user = User.findByUsername(((OAuth2AuthenticationToken) SecurityContextHolder.getContext().getAuthentication()).getName())
        Assessment assessment = params.assessment
        if( assessment == null ) {
            log.warn("Bad or missing assessment id")
            throw new InvalidRequestError("Bad or missing assessment id.")
        }

        // Do not start a new thread if one is already running
        if (!trustmarkService.isExecuting(TrustmarkService.TRUSTMARK_GENERATION_EXECUTING_VAR)) {

            trustmarkService.setAttribute(TrustmarkService.TRUSTMARK_GENERATION_MESSAGE_VAR, "Generating the trutmarks list.")
            trustmarkService.setAttribute(TrustmarkService.TRUSTMARK_GENERATION_STATUS_VAR, "RUNNING")
            trustmarkService.setAttribute(TrustmarkService.TRUSTMARK_GENERATION_PERCENT_VAR, "0")

            log.debug("TrustmarkController::generateTrustmarkList, Collecting TrusmtarkDefinitions to grant on for Assessment #${assessment.id}...")

            long startTime = System.currentTimeMillis()

            def paramsMap = (Map) trustmarkService.getAttribute(TrustmarkService.TRUSTMARK_DEFINITIONS_PARAMS_MAP_VAR)

            List<TrustmarkDefinition> tdsToGrantOn = []

            int currentTdIndex = 0
            for (String paramName : paramsMap.keySet()) {

                if (paramName.startsWith("trustmarkDefinition") && paramName.endsWith("Checkbox") && trustmarkService.getBooleanValue(paramsMap[paramName])) {
                    Long id = Long.parseLong(paramName.replace("trustmarkDefinition", "").replace("Checkbox", ""))
                    TrustmarkDefinition tdFromDatabase = TrustmarkDefinition.get(id)
                    if (tdFromDatabase == null) {
                        log.warn("TD ${id} does not exist!")
                        throw new InvalidRequestError("Invalid trustmarkDefinition: ${id}")
                    }
                    log.info("Granting trustmark for TD[${tdFromDatabase.name}, v.${tdFromDatabase.tdVersion}]")
                    tdsToGrantOn.add(tdFromDatabase)
                }

                int percent = (int) Math.floor(((double) currentTdIndex++ / (double) paramsMap.size()) * 100.0d)

                trustmarkService.setAttribute(TrustmarkService.TRUSTMARK_GENERATION_PERCENT_VAR, "" + percent)

                String percentString = trustmarkService.getAttribute(TrustmarkService.TRUSTMARK_GENERATION_PERCENT_VAR);
                log.info("** TRUSTMARK_GENERATION_PERCENT_VAR: ${percent}%.")
                log.info("** percentString: ${percentString}%.")
            }

            long stopTime = System.currentTimeMillis()
            log.info("Collecting TDs time: ${(stopTime - startTime)}ms.")

            if (tdsToGrantOn.isEmpty()) {
                log.warn("The user has selected no TDs to grant Trustmarks for!")
                flash.error = "You must select at least 1 TD to grant a Trustmark on."
                return redirect(action: 'create', params: [assessmentId: assessment.id])
            }

            TrustmarkMetadata metadata = TrustmarkMetadata.get(paramsMap['trustmarkMetadataId'])
            if (metadata == null) {
                //throw new InvalidRequestError("Invalid trustmarkMetadataId!")
                log.warn("No trustmark metadata has been created!")
                flash.error = "You must generate and select at least 1 trustmark metadata set to grant a Trustmark on."
                return redirect(controller: 'trustmarkMetadata', action: 'create')
            }

            TrustmarkRecipientIdentifier trustmarkRecipientIdentifier =
                    TrustmarkRecipientIdentifier.get(paramsMap['trustmarkRecipientIdentifierId'])
            if (trustmarkRecipientIdentifier == null) {
                log.warn("No trustmark recipient identifier has been selected!")
                flash.error = "You must select  trustmark recipient identifier set to grant a Trustmark on."
                return redirect(action: 'create', params: [assessmentId: assessment.id])
            }

//            log.debug("///////////////////////////////////////////")
//            log.debug("tdsToGrantOn size: ${tdsToGrantOn.size()}...")
//            log.debug("tdsToGrantOn: ${tdsToGrantOn.toString()}...")

            trustmarkService.setAttribute(TrustmarkService.TRUSTMARK_DEFINITIONS_TO_GRANT_ON_VAR, tdsToGrantOn)

            trustmarkService.setExecuting(TrustmarkService.TRUSTMARK_GENERATION_EXECUTING_VAR)

            final Integer assessmentId = assessment.id
            final Integer userId = user.id
            final Integer metadataSetId = metadata.id
            final Integer trustmarkRecipientIdentifierId = trustmarkRecipientIdentifier.id


            Thread trustmarkListThread = new Thread(new Runnable() {
                @Override
                void run() {
                    trustmarkService.generateTrustmarkList(userId, assessmentId,
                            metadataSetId, trustmarkRecipientIdentifierId)
                }
            })
            trustmarkService.setAttribute(TRUSTMARK_GENERATION_THREAD_VAR, trustmarkListThread)
            trustmarkListThread.start()

            log.debug(trustmarkService.getAttributesString("generateTrustmarkList"))

        }

        ["Generating the list of trustmarks"]
    }

    @ParamConversion(paramName="assessmentId", toClass=Assessment.class, storeInto = "assessment")
    def cancelTrustmarkGeneration(){
        User user = User.findByUsername(((OAuth2AuthenticationToken) SecurityContextHolder.getContext().getAuthentication()).getName())
        Assessment assessment = params.assessment

        log.debug("Request to cancel trustmark generation...")


        // Check if thread is running, interrupt thread and wait for it to finish
        if (trustmarkService.isExecuting(TrustmarkService.TRUSTMARK_GENERATION_EXECUTING_VAR)) {
            trustmarkService.stopExecuting(TrustmarkService.TRUSTMARK_GENERATION_EXECUTING_VAR)

            trustmarkService.setAttribute(TrustmarkService.TRUSTMARK_GENERATION_STATUS_VAR, "CANCELLING")
            trustmarkService.setAttribute(TrustmarkService.TRUSTMARK_GENERATION_MESSAGE_VAR, "Cancelling trustmark generation...")

            log.debug("Interrupting previous trustmark generation thread...")
            Thread t = trustmarkService.getAttribute(TRUSTMARK_GENERATION_THREAD_VAR)
            if (t &&  t.isAlive()) {
                t.join()
                log.debug("Interrupted trustmark generation thread...")
            }
        }

        // Base url includes the TAT domain name, createLink includes the TAT domain name
        // Get the host and port from the base url and append the result of createLink to avoid duplicate TAT domain
        URL url = new URL(AssessmentToolProperties.getProperties().getProperty(AssessmentToolProperties.BASE_URL))
        def baseUrl = url.protocol + "://" + url.getAuthority()

        def jsonResponse = [href: baseUrl + createLink(controller: 'assessment', action: 'view', id: assessment.id)]

        log.debug(trustmarkService.getAttributesString("save"))

        render jsonResponse as JSON

    }//end cancelTrustmarkGeneration()

    /**
     * Called when the user clicks "Generate" on the create trustmark page.  Actually saves the database object.
     */
    @ParamConversion(paramName="assessmentId", toClass=Assessment.class, storeInto = "assessment")
    def save(){
        User user = User.findByUsername(((OAuth2AuthenticationToken) SecurityContextHolder.getContext().getAuthentication()).getName())
        log.debug("Request to grant Trustmarks...")
        Assessment assessment = params.assessment
        if( assessment == null ) {
            log.warn("Bad or missing assessment id")
            throw new InvalidRequestError("Bad or missing assessment id.")
        }

        log.info("All trustmarks granted, forwarding to the assessment page...")

        // initialize service attribute set to, in the case of assessments with a large number of TDs,
        // ease the memory footprint
        trustmarkService.resetAttributes()

        // Base url includes the TAT domain name, createLink includes the TAT domain name
        // Get the host and port from the base url and append the result of createLink to avoid duplicate TAT domain
        URL url = new URL(AssessmentToolProperties.getProperties().getProperty(AssessmentToolProperties.BASE_URL))
        def baseUrl = url.protocol + "://" + url.getAuthority()

        def jsonResponse = [href: baseUrl + createLink(controller: 'assessment', action: 'view', id: assessment.id)]

        log.debug(trustmarkService.getAttributesString("save"))

        render jsonResponse as JSON

    }//end save()

    /**
     * Called to view a trustmark in the system.
     */
    def view() {
        User user = User.findByUsername(((OAuth2AuthenticationToken) SecurityContextHolder.getContext().getAuthentication()).getName())
        if( StringUtils.isEmpty(params.id) )
            throw new ServletException("Missing required parameter 'id'.")

        Trustmark trustmark = null
        try{
            Long tmId = Long.parseLong(params.id)
            trustmark = Trustmark.findById(tmId)
        }catch(Throwable t){
            trustmark = Trustmark.findByIdentifier(params.id)
        }
        if( !trustmark )
            throw new ServletException("Unknown trustmark: ${params.id}")

        withFormat {
            html {
                [trustmark: trustmark]
            }
            json {
                render trustmark as JSON
            }
            xml {
                render trustmark as XML
            }
        }
    }//end view()

    /**
     * Displays a page to the user where they can edit the values of the trustmark, without having to regenerate it.
     */
    def edit() {
        log.debug("Editing trustmark[${params.id}]")
        if( StringUtils.isEmpty(params.id) ){
            throw new ServletException("Missing required parameter 'id'")
        }
        Trustmark trustmark = Trustmark.findById(params.id)
        if (!trustmark)
            throw new ServletException("No such trustmark '${params.id}'")

        TrustmarkCommand command = new TrustmarkCommand()
        command.trustmarkId = trustmark.id
        command.identifier = trustmark.identifier
        command.identifierURL = trustmark.identifierURL
        command.assessmentId = trustmark.assessment.id
        command.expirationDateTime = new SimpleDateFormat("yyyy-MM-dd").format(trustmark.expirationDateTime)
        command.policyPublicationURL = trustmark.policyPublicationURL
        command.relyingPartyAgreementURL = trustmark.relyingPartyAgreementURL
        command.providingContactId = trustmark.providerContactInformation.id
        command.providingOrganizationId = trustmark.providerOrganization.id
        command.recipientOrganizationId = trustmark.recipientOrganization.id
        command.recipientContactId = trustmark.recipientContactInformation.id
        command.hasExceptions = trustmark.hasExceptions
        command.assessorComments = trustmark.assessorComments
        command.statusURL = trustmark.statusURL

        command.definitionExtension = trustmark.definitionExtension

        Organization org = Organization.findById(trustmark.providerOrganization.id)
        List<SigningCertificate> signingCertificates = org.certificates.toList()

        if (signingCertificates && signingCertificates.size() > 0) {
            command.distinguishedName = signingCertificates[0].distinguishedName
        }

        [assessment: trustmark.assessment,
         signingCertificates: signingCertificates,
         command: command,
         trustmark: trustmark]
    }

    /**
     * Called when the user is submitting the edit form, performs the database update command.
     */
    def update(TrustmarkCommand command) {
        User user = User.findByUsername(((OAuth2AuthenticationToken) SecurityContextHolder.getContext().getAuthentication()).getName())
        log.debug("Request to update trustmark[${command.identifier}]...")
        Assessment assessment = Assessment.findById(command.assessmentId)
        if( assessment == null )
            throw new ServletException("Bad assessment id.")

        if(!command.validate()){
            log.warn "Update trustmark form does not validate: "
            command.errors.getAllErrors().each { ObjectError error ->
                log.warn "    ${error.defaultMessage}"
            }
            return render(view: 'edit', model: [assessment: assessment, command: command])
        }
        log.debug("Trustmark form validates.  Updating trustmark...")

        // TODO should we limit this operation to anyone in particular?

        if( assessment.status == AssessmentStatus.FAILED && command.hasExceptions.equals(Boolean.FALSE) ) {
            log.warn("User[$user] is trying to grant pristine trustmark against failed assessment[${assessment.id}].  This is forbidden.")
            command.errors.reject("assessment.failed.exceptions.required", [] as Object[], "The assessment has failed, so you are REQUIRED to mark this trustmark as having exceptions, and state what they are.")
            return render(view: 'create', model: [assessment: assessment, command: command])
        }


        Organization providerOrg = Organization.findById(command.providingOrganizationId)
        if( providerOrg == null )
            throw new ServletException("Bad provider org id")
        ContactInformation providerContactInformation = ContactInformation.findById(command.providingContactId)
        if( providerContactInformation == null )
            throw new ServletException("Bad provider contact id")

        Organization recipientOrg = Organization.findById(command.recipientOrganizationId)
        if( recipientOrg == null )
            throw new ServletException("Bad recipient org id")
        ContactInformation recipientContactInformation = ContactInformation.findById(command.recipientContactId)
        if( recipientContactInformation == null )
            throw new ServletException("Bad recipient contact id")

        Trustmark trustmark = Trustmark.findById(command.trustmarkId)
        trustmark.identifier = command.identifier
        trustmark.identifierURL = command.identifierURL
        trustmark.statusURL = command.statusURL
        trustmark.assessment = assessment
        trustmark.providerContactInformation = providerContactInformation
        trustmark.providerOrganization = providerOrg
        trustmark.recipientOrganization = recipientOrg
        trustmark.recipientContactInformation = recipientContactInformation
        trustmark.definitionExtension = command.definitionExtension
        trustmark.providerExtension = "<gtri:assessment-id xmlns:gtri=\"urn:edu:gatech:gtri:trustmark:assessment\">${assessment.id}</gtri:assessment-id>"
        trustmark.expirationDateTime = new SimpleDateFormat("yyyy-MM-dd").parse(command.expirationDateTime)
        trustmark.issueDateTime = trustmark.issueDateTime
        trustmark.grantingUser = trustmark.grantingUser
        trustmark.policyPublicationURL = command.policyPublicationURL
        trustmark.relyingPartyAgreementURL = command.relyingPartyAgreementURL
        trustmark.hasExceptions = command.hasExceptions
        trustmark.assessorComments = command.assessorComments

        // re-generate and save xml signature for this trustmark
        SigningCertificate signingCertificate = SigningCertificate.findByDistinguishedName(command.distinguishedName)
        if (signingCertificate == null) {
            throw new ServletException("Need at least one signing certificate to perform this operation.")
        }

        trustmarkService.signTrustmark(signingCertificate, trustmark)

        trustmark.save(failOnError: true, flush: true)

        redirect(action: 'view', id: trustmark.id)
    }

    /**
     * Generates the XML for a trustmark in the system.
     */
    def generateXml() {
        User user = User.findByUsername(((OAuth2AuthenticationToken) SecurityContextHolder.getContext().getAuthentication()).getName())
        if( StringUtils.isEmpty(params.id) )
            throw new ServletException("Missing required parameter 'id'.")

        Trustmark trustmark = Trustmark.findById(params.id)
        if( !trustmark )
            throw new ServletException("Unknown trustmark: ${params.id}")

        if( trustmark.status != TrustmarkStatus.OK ){
            log.warn("Refusing to generate for expired trustmark")
            throw new ServletException("Trustmark ${params.id} has bad status: ${trustmark.status}.  Refusing to generate XML.")
        }

        String trustmarkXml = trustmark.signedXml

        return render(contentType: 'text/xml', text: trustmarkXml)
    }

    /**
     * Generates the JSON representation for a trustmark in the system.
     */
    def generateJson() {
        User user = User.findByUsername(((OAuth2AuthenticationToken) SecurityContextHolder.getContext().getAuthentication()).getName())
        if( StringUtils.isEmpty(params.id) )
            throw new ServletException("Missing required parameter 'id'.")

        Trustmark trustmark = Trustmark.findById(params.id)
        if( !trustmark )
            throw new ServletException("Unknown trustmark: ${params.id}")

        if( trustmark.status != TrustmarkStatus.OK ){
            log.warn("Refusing to generate for expired trustmark")
            throw new ServletException("Trustmark ${params.id} has bad status: ${trustmark.status}.  Refusing to generate JSON.")
        }

        return render(contentType: 'text/jwt', text: trustmark.signedJson)
    }

    /**
     * This method displays the status XML for a given trustmark.
     */
    def generateStatusXML() {
        User user = User.findByUsername(((OAuth2AuthenticationToken) SecurityContextHolder.getContext().getAuthentication()).getName())
        if( StringUtils.isEmpty(params.id) )
            throw new ServletException("Missing required parameter 'id'.")

        Trustmark trustmark = null
        try{
            log.debug("Trying to parse Long id from ${params.id}...")
            Long tmId = Long.parseLong(params.id)
            log.debug("Finding trustmark with id = ${tmId}")
            trustmark = Trustmark.findById(tmId)
        }catch(Throwable t){
            log.debug("Checking for trustmark with identifier = ${params.id}")
            trustmark = Trustmark.findByIdentifier(params.id)
        }
        if( !trustmark )
            throw new ServletException("Unknown trustmark: ${params.id}")

        Calendar issueDateTimeCal = Calendar.getInstance()
        issueDateTimeCal.setTime(trustmark.issueDateTime)

        Calendar expirationDateTimeCal = Calendar.getInstance()
        expirationDateTimeCal.setTime(trustmark.expirationDateTime)

        Calendar now = Calendar.getInstance()
        if( expirationDateTimeCal.getTimeInMillis() < now.getTimeInMillis() ){
            log.warn("Uh-oh!  I've detected an expired trustmark.  Updating...")

            trustmark.status = TrustmarkStatus.EXPIRED
            trustmark.revokedTimestamp = now.getTime()
            trustmark.revokedReason = "The Assessment Tool System has automatically revoked this trustmark because it has expired."
            trustmark.save(failOnError: true, flush: true)
        }

        StringBuilder xmlBuilder = new StringBuilder()

        xmlBuilder.append("""<?xml version="1.0"?>

<tf:TrustmarkStatusReport xmlns:tf="https://trustmark.gtri.gatech.edu/specifications/trustmark-framework/1.0/schema/">
    <tf:ReportURL>${request.getRequestURL().toString()}</tf:ReportURL>
    <tf:TrustmarkIdentifier>${trustmark.identifierURL}</tf:TrustmarkIdentifier>
    <tf:Status>${trustmark.status.toString()}</tf:Status>
    <tf:StatusDateTime>${DatatypeConverter.printDateTime(now)}</tf:StatusDateTime>

""")

        if( trustmark.status != TrustmarkStatus.OK ){

            if( trustmark.supersededBy != null ){
                xmlBuilder.append("    <tf:SupercededByURL>${trustmark.supersededBy.identifier}</tf:SupercededByURL>\n")
            }

            if( StringUtils.isNotBlank(trustmark.revokedReason) ){
                xmlBuilder.append("   <tf:Notes>${trustmark.revokedReason}</tf:Notes>\n")
            }
        }


        xmlBuilder.append("</tf:TrustmarkStatusReport>\n\n")

        return render(contentType: 'text/xml', text: xmlBuilder.toString())
    }

    /**
     * Called when the user clicks on the "Revoke" button on the view trustmark page.  Should mark the trustmark as
     * revoked, indicating that it is no longer valid.
     */
    def revoke() {
        User user = User.findByUsername(((OAuth2AuthenticationToken) SecurityContextHolder.getContext().getAuthentication()).getName())
        if( StringUtils.isEmpty(params.id) )
            throw new ServletException("Missing required parameter 'id'.")
        if( StringUtils.isEmpty(params.reason) )
            throw new ServletException("Missing required parameter 'reason'.")

        Trustmark trustmark = Trustmark.findById(params.id)
        if( !trustmark )
            throw new ServletException("Unknown trustmark: ${params.id}")

        trustmark.status = TrustmarkStatus.REVOKED
        trustmark.revokedReason = params.reason
        trustmark.revokingUser = user
        trustmark.revokedTimestamp = Calendar.getInstance().getTime()
        trustmark.save(failOnError: true, flush: true)

        def responseData = [status: "SUCCESS", message: "Successfully revoked trustmark ${trustmark.id}", trustmark: [id: trustmark.id, identifier: trustmark.identifier, status: trustmark.status.toString()]]
        withFormat {
            html {
                flash.message = "Successfully revoked trustmark"
                return redirect(controller:'trustmark', action:'view', id: trustmark.id)
            }
            xml {
                render responseData as XML
            }
            json {
                render responseData as JSON
            }
        }


    }

    /**
     * Shows the current user the page where they can select multiple trustmarks to export.
     */
    def showBulkExportPage() {
        User user = User.findByUsername(((OAuth2AuthenticationToken) SecurityContextHolder.getContext().getAuthentication()).getName())
        log.debug("Showing User[${user.username}] the bulk export page...")

        if (!params.max)
            params.max = '20'
        params.max = Math.min(100, Integer.parseInt(params.max)).toString(); // Limit to at most 100 trustmarks at a time.

        def exportList = []
        if( session.getAttribute('export-list') ){
            exportList = session.getAttribute('export-list')
        }

        [trustmarkCountTotal: Trustmark.count(), trustmarks: Trustmark.list(params), exportList: exportList]
    }//end showBulkExportPage()

    /**
     * Called by AJAX in the trustmark bulk export process to add to the user's export list already in memory.
     * Returns some HTML that can be displayed to the user to show which trustmarks will be exported.
     */
    def addToExportList(){
        User user = User.findByUsername(((OAuth2AuthenticationToken) SecurityContextHolder.getContext().getAuthentication()).getName())
        log.debug("Adding to User[${user.username}] bulk export list...")

        def actualValue = params.get('trustmarkIds[]')
        def ids = []
        if( actualValue == null ) {
            // Do nothing, leave this an empty list.
        }else if( actualValue instanceof String ){
            ids.add(actualValue)
        }else if( actualValue instanceof String[] ){
            ids.addAll(actualValue)
        }


        def exportList = []
        synchronized (session) {
            if (session.getAttribute('export-list')) {
                exportList = session.getAttribute('export-list')
            }

            ids.each { id ->
                Trustmark tm = Trustmark.get(id)
                if (!tm) {
                    log.error("Cannot select missing trustmark from database, id=${id}")
                    throw new ServletException("Bad trustmark id: ${id}")
                }
                if( !exportList.contains(id) ) {
                    exportList.add(id)
                }else{
                    log.warn("Skipping trustmark $id, since the export list already contains that one.")
                }
            }

            session.setAttribute('export-list', exportList)
        }

        forward(action: 'exportList')
    }//end addToExportList()

    def exportList() {
        def exportList = []
        synchronized (session) {
            if (session.getAttribute('export-list')) {
                exportList = session.getAttribute('export-list')
            }
        }

        def trustmarkList = []
        exportList.each{ tmId ->
            trustmarkList.add(Trustmark.get(tmId))
        }

        log.debug("Rendering result export list...")
        [trustmarks: trustmarkList, trustmarkCount: trustmarkList.size()]
    }

    /**
     * Called by AJAX in the trustmark bulk export process to remove from the user's export list already in memory.
     * Returns some HTML that can be displayed to the user to show which trustmarks will be exported.
     */
    def removeFromExportList(){
        User user = User.findByUsername(((OAuth2AuthenticationToken) SecurityContextHolder.getContext().getAuthentication()).getName())
        log.debug("Removing From User[${user.username}] bulk export list...")

        def actualValue = params.get('trustmarkIds[]')
        def ids = []
        if( actualValue == null ) {
            // Do nothing, leave this an empty list.
        }else if( actualValue instanceof String ){
            ids.add(actualValue)
        }else if( actualValue instanceof String[] ){
            ids.addAll(actualValue)
        }


        def exportList = []
        synchronized (session) {
            if (session.getAttribute('export-list')) {
                exportList = session.getAttribute('export-list')
            }

            ids.each { id ->
                if( exportList.contains(id) ){
                    log.debug("Removed trustmark $id")
                    exportList.remove(id)
                }
            }
            session.setAttribute('export-list', exportList)
        }

        forward(action: 'exportList')
    }

    /**
     * Actually performs the bulk export.  takes the export-list session attribute, finds each trustmark, and then marshalls
     * to a directory containing the XML files.  Zips the directory up, and then sends that zip file to the user.
     */
    def doBulkExport() {
        User user = User.findByUsername(((OAuth2AuthenticationToken) SecurityContextHolder.getContext().getAuthentication()).getName())
        log.debug("User[${user.username}] performing bulk export...")

        List exportList = []
        synchronized (session) {
            if (session.getAttribute('export-list')) {
                exportList = session.getAttribute('export-list')
            }
        }

        if( exportList.isEmpty() ){
            log.error("Cannot export an empty list.")
            throw new ServletException("Cannot export an empty list.")
        }

        File tempOutputDirectory = File.createTempFile("trustmark-bulk-export.", ".dir")
        tempOutputDirectory.delete()
        tempOutputDirectory.mkdirs()
        File outputDir1 = new File(tempOutputDirectory, "trustmarks_${prettyDate()}")
        outputDir1.mkdirs()
        File outputDir = new File(outputDir1, "trustmarks_${prettyDate()}")
        outputDir.mkdirs()

        log.debug("Iterating and generating XML for trustmarks...")
        exportList.each{ tmId ->
            Trustmark tm = Trustmark.get(tmId)
            File tmFile = new File(outputDir, tm.identifier+".xml")
            String trustmarkXml = tm.signedXml
            tmFile << trustmarkXml
        }

        log.debug("Creating zip file...")
        File zipFile = new File(tempOutputDirectory, outputDir.getName()+".zip")
        def ant = new AntBuilder()
        ant.zip(destfile: zipFile.canonicalPath){
            zipfileset(dir: outputDir1.canonicalPath, includes: "**/*")
        }

        log.debug("Rendering zip to client...")
        response.setHeader 'Content-disposition', "attachment; filename=\"${zipFile.name}\""
        return render(file: new FileInputStream(zipFile), contentType: 'application/zip')
    }//end doBulkExport()

    /**
     * This process trustmarks that were signed with an expired or revoked certificate and reissues and sign each
     * trustmark with a new certificate.
     */
    def reissueTrustmarks() {

        int existingCertificateId = Integer.parseInt(params.originalCertId)
        int newCertificateId = Integer.parseInt(params.newCertId)

        log.debug("Reissuing trustmarks from old certificate[${existingCertificateId}] to new certificate[${newCertificateId}] ...")

        SigningCertificate signingCertificate = SigningCertificate.findById(newCertificateId)
        if (signingCertificate == null) {
            log.debug("signingCertificate [${newCertificateId}] is null...")
            throw new ServletException("Need at least one signing certificate to perform this operation.")
        }

        // we assume that the trustmark metadata set has been updated with
        // the new certificate
        def metadata = TrustmarkMetadata.findByDefaultSigningCertificateId(newCertificateId)

        if( metadata != null ) {

            Calendar now = Calendar.getInstance()

            TrustmarkIdentifierGenerator identifierGenerator = Class.forName(metadata.generatorClass).newInstance()

            // get a collection of trustmarks that were signed with the expired/revoked certificate
            def trustmarks = Trustmark.findAllBySigningCertificateId(existingCertificateId)

            log.debug("Number of trustmarks that were signed with the expired/revoked certificate [${existingCertificateId}]: ${trustmarks.size()}...")

            if (trustmarks && trustmarks.size() > 0) {
                trustmarks.each { Trustmark trustmark ->
                    // only reissue from active trustmarks
                    if (trustmark.status == TrustmarkStatus.ACTIVE || trustmark.status == TrustmarkStatus.OK) {

                        String id = identifierGenerator.generateNext()

                        Trustmark reissuedTrustmark = new Trustmark(trustmarkDefinition: trustmark.trustmarkDefinition,
                                assessment: trustmark.assessment)
                        reissuedTrustmark.identifier = id
                        reissuedTrustmark.identifierURL = trustmarkService.replaceIdentifier(metadata.identifierPattern, id)
                        reissuedTrustmark.statusURL = trustmarkService.replaceIdentifier(metadata.statusUrlPattern, id)
                        reissuedTrustmark.status = TrustmarkStatus.OK
                        reissuedTrustmark.recipientOrganization = trustmark.recipientOrganization
                        reissuedTrustmark.recipientContactInformation = trustmark.recipientContactInformation
                        reissuedTrustmark.issueDateTime = now.getTime()
                        reissuedTrustmark.policyPublicationURL = metadata.policyUrl
                        reissuedTrustmark.relyingPartyAgreementURL = metadata.relyingPartyAgreementUrl
                        reissuedTrustmark.providerOrganization = trustmark.providerOrganization
                        reissuedTrustmark.providerContactInformation = trustmark.providerContactInformation
                        reissuedTrustmark.providerExtension = trustmark.providerExtension
                        reissuedTrustmark.grantingUser = trustmark.grantingUser
                        reissuedTrustmark.definitionExtension = trustmark.definitionExtension
                        reissuedTrustmark.hasExceptions = trustmark.hasExceptions
                        reissuedTrustmark.assessorComments = trustmark.assessorComments
                        reissuedTrustmark.expirationDateTime = trustmark.expirationDateTime

                        // Parameters
                        AssessmentStepData[] stepList = trustmark.assessment.getStepListByTrustmarkDefinition(
                                trustmark.trustmarkDefinition)
                        TdParameter[] firstUnfilledRequiredParametersPerStep = stepList.collect { it.firstUnfilledRequiredParameter }
                        TdParameter firstUnfilledRequiredParameter = firstUnfilledRequiredParametersPerStep.find { it }
                        if (firstUnfilledRequiredParameter) {
                            String message = String.format(
                                    "Unfilled required parameter: TD[%s] >> Step[%s] >> Parameter[%s]",
                                    trustmark.trustmarkDefinition.name,
                                    firstUnfilledRequiredParameter.assessmentStep.name,
                                    firstUnfilledRequiredParameter.name
                            )
                            throw new InvalidRequestError(message)
                        }
                        ParameterValue[] assessmentParameterValues = stepList.collectMany { it.parameterValues }
                        for (assessmentParameterValue in assessmentParameterValues) {
                            ParameterValue parameterValue = new ParameterValue(
                                    parameter: assessmentParameterValue.parameter,
                                    userValue: assessmentParameterValue.userValue
                            )
                            reissuedTrustmark.addToParameterValues(parameterValue)
                        }

                        // generate and save xml signature for this trustmark
                        trustmarkService.signTrustmark(signingCertificate, reissuedTrustmark)

                        reissuedTrustmark.save(failOnError: true, flush: true)

                        // update original trustmark status
                        log.warn("Revoking trustmark due to an expired certificate.  Updating...")
                        trustmark.status = TrustmarkStatus.REVOKED
                        trustmark.revokedTimestamp = now.getTime()
                        trustmark.supersededBy = reissuedTrustmark
                        trustmark.revokedReason = "This trustmark has been revoked because the certificate" +
                                " used to generate its XML signature has been revoked or has expired."
                        trustmark.save(failOnError: true, flush: true)
                    }
                }
            }
        }

        render {
            view("/signingCertificate/view")
            div(id: "trustmarkUpdateStatusMessage", "Generated a new certificate and updated metadata sets, and reissued trustmarks...")
        }
    }

    /**
     * This process trustmarks that were signed with an expired or revoked certificate and reissues and sign each
     * trustmark using a valid certificate from an existing metadata set.
     */
    def reissueTrustmarksFromMetadataSet() {

        int existingCertificateId = Integer.parseInt(params.originalCertId)
        int metadataSetId = Integer.parseInt(params.metadataSetId)

        log.debug("Reissuing trustmarks from old certificate[${existingCertificateId}] to metadata[${metadataSetId}] ...")

        // we assume that the trustmark metadata set has been updated with
        // the new certificate
        def metadata = TrustmarkMetadata.findById(metadataSetId)

        if( metadata != null ) {

            int newCertificateId = metadata.defaultSigningCertificateId

            Calendar now = Calendar.getInstance()

            TrustmarkIdentifierGenerator identifierGenerator = Class.forName(metadata.generatorClass).newInstance()

            // get a collection of trustmarks that were signed with the expired/revoked certificate
            def trustmarks = Trustmark.findAllBySigningCertificateId(existingCertificateId)

            trustmarks.each { Trustmark trustmark ->
                // only reissue from active trustmarks
                if (trustmark.status == TrustmarkStatus.ACTIVE || trustmark.status == TrustmarkStatus.OK) {
                    String id = identifierGenerator.generateNext()

                    Trustmark reissuedTrustmark = new Trustmark(trustmarkDefinition: trustmark.trustmarkDefinition,
                            assessment: trustmark.assessment)
                    reissuedTrustmark.identifier = id
                    reissuedTrustmark.identifierURL = trustmarkService.replaceIdentifier(metadata.identifierPattern, id)
                    reissuedTrustmark.statusURL = trustmarkService.replaceIdentifier(metadata.statusUrlPattern, id)
                    reissuedTrustmark.status = TrustmarkStatus.OK
                    reissuedTrustmark.recipientOrganization = trustmark.recipientOrganization
                    reissuedTrustmark.recipientContactInformation = trustmark.recipientContactInformation
                    reissuedTrustmark.issueDateTime = now.getTime()
                    reissuedTrustmark.policyPublicationURL = metadata.policyUrl
                    reissuedTrustmark.relyingPartyAgreementURL = metadata.relyingPartyAgreementUrl
                    reissuedTrustmark.providerOrganization = trustmark.providerOrganization
                    reissuedTrustmark.providerContactInformation = trustmark.providerContactInformation
                    reissuedTrustmark.providerExtension = trustmark.providerExtension
                    reissuedTrustmark.grantingUser = trustmark.grantingUser
                    reissuedTrustmark.definitionExtension = trustmark.definitionExtension
                    reissuedTrustmark.hasExceptions = trustmark.hasExceptions
                    reissuedTrustmark.assessorComments = trustmark.assessorComments
                    reissuedTrustmark.expirationDateTime = trustmark.expirationDateTime

                    // Parameters
                    AssessmentStepData[] stepList = trustmark.assessment.getStepListByTrustmarkDefinition(
                            trustmark.trustmarkDefinition)
                    TdParameter[] firstUnfilledRequiredParametersPerStep = stepList.collect { it.firstUnfilledRequiredParameter }
                    TdParameter firstUnfilledRequiredParameter = firstUnfilledRequiredParametersPerStep.find { it }
                    if (firstUnfilledRequiredParameter) {
                        String message = String.format(
                                "Unfilled required parameter: TD[%s] >> Step[%s] >> Parameter[%s]",
                                trustmark.trustmarkDefinition.name,
                                firstUnfilledRequiredParameter.assessmentStep.name,
                                firstUnfilledRequiredParameter.name
                        )
                        throw new InvalidRequestError(message)
                    }
                    ParameterValue[] assessmentParameterValues = stepList.collectMany { it.parameterValues }
                    for (assessmentParameterValue in assessmentParameterValues) {
                        ParameterValue parameterValue = new ParameterValue(
                                parameter: assessmentParameterValue.parameter,
                                userValue: assessmentParameterValue.userValue
                        )
                        reissuedTrustmark.addToParameterValues(parameterValue)
                    }

                    // generate and save xml signature for this trustmark
                    SigningCertificate signingCertificate = SigningCertificate.findById(newCertificateId)
                    if (signingCertificate == null) {
                        throw new ServletException("Need at least one signing certificate to perform this operation.")
                    }

                    trustmarkService.signTrustmark(signingCertificate, reissuedTrustmark)

                    reissuedTrustmark.save(failOnError: true, flush: true)

                    // update original trustmark status
                    log.warn("Revoking trustmark due to an expired certificate.  Updating...")
                    trustmark.status = TrustmarkStatus.REVOKED
                    trustmark.revokedTimestamp = now.getTime()
                    trustmark.supersededBy = reissuedTrustmark
                    trustmark.revokedReason = "This trustmark has been revoked because the certificate" +
                            " used to generate its XML signature has been revoked or has expired."
                    trustmark.save(failOnError: true, flush: true)
                }

            } // end each
        }// end if
    } // end reissueTrustmarksFromMetadataSet

    /**
     * Revokes all trustmarks that were granted for a particular organization.
     */
    def revokeAll() {
        User user = User.findByUsername(((OAuth2AuthenticationToken) SecurityContextHolder.getContext().getAuthentication()).getName())
        if( StringUtils.isEmpty(params.id) )
            throw new ServletException("Missing required parameter 'id'.")
        if( StringUtils.isEmpty(params.reason) )
            throw new ServletException("Missing required parameter 'reason'.")

        Organization org = Organization.findById(params.id)
        if( org == null )
            throw new ServletException("Missing organization")

        List<Trustmark> trustmarks = Trustmark.findAllByRecipientOrganization(org)

        trustmarkService.revokeAll(trustmarks, user, params.reason)
    }

    /**
     * Revokes all trustmarks that were signed with a particular signing certificate.
     */
    def revokeAllTrustmarksSignedWithCertificate() {
        User user = User.findByUsername(((OAuth2AuthenticationToken) SecurityContextHolder.getContext().getAuthentication()).getName())
        if( StringUtils.isEmpty(params.id) ) {
            throw new ServletException("Missing required parameter 'id'.")
        }
        if( StringUtils.isEmpty(params.reason) ) {
            throw new ServletException("Missing required parameter 'reason'.")
        }

        SigningCertificate signingCertificate = SigningCertificate.findById(params.id)
        if( signingCertificate == null ) {
            throw new ServletException("Missing signing certificate")
        }

        List<Trustmark> trustmarks = Trustmark.findAllBySigningCertificateId(signingCertificate.id)

        if( trustmarks && !trustmarks.isEmpty() ){

            trustmarks.each { trustmark ->
                // only revoke active trustmarks
                if (trustmark.status == TrustmarkStatus.ACTIVE || trustmark.status == TrustmarkStatus.OK) {
                    trustmark.status = TrustmarkStatus.REVOKED
                    trustmark.revokedReason = params.reason
                    trustmark.revokingUser = user
                    trustmark.revokedTimestamp = Calendar.getInstance().getTime()
                    trustmark.save(failOnError: true, flush: true)
                }
            }
        }
    }

    def getTrustmarkStats() {

        Integer numberOfActiveTrustmarks = 0
        Integer numberOfExpiredTrustmarks = 0
        Integer numberOfRevokedTrustmarks = 0

        Organization organization = Organization.findById(params.id)
        if( organization == null ) {
            log.warn("Missing organization")
            throw new ServletException("Missing organization")
        }

        List<Trustmark> trustmarks  = Trustmark.findAllByRecipientOrganization(organization)

        if (trustmarks && trustmarks.size() > 0) {
            trustmarks.each { Trustmark trustmark ->
                if (trustmark.status == TrustmarkStatus.EXPIRED) {
                    numberOfExpiredTrustmarks++
                } else if (trustmark.status == TrustmarkStatus.REVOKED){
                    numberOfRevokedTrustmarks++
                } else {
                    numberOfActiveTrustmarks++;
                }
            }
        }

        def model = [numberOfActiveTrustmarks: numberOfActiveTrustmarks,
                     numberOfExpiredTrustmarks: numberOfExpiredTrustmarks,
                     numberOfRevokedTrustmarks: numberOfRevokedTrustmarks,
                     totalNumberOfTrustmarks: numberOfActiveTrustmarks + numberOfExpiredTrustmarks + numberOfRevokedTrustmarks]
        render model as JSON
    }

    //==================================================================================================================
    //  Helper Methods
    //==================================================================================================================
    private String prettyDate(){
        return new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime())
    }

    private Calendar getExpirationDateForTrustmarkWithExceptions() {
        int months = 6

        Calendar exp = Calendar.getInstance()
        exp.set(Calendar.HOUR_OF_DAY, 23)
        exp.set(Calendar.MINUTE, 59)
        exp.set(Calendar.SECOND, 59)
        exp.set(Calendar.MONTH, exp.get(Calendar.MONTH) + months)

        return exp
    }
    private Calendar getExpirationDateForTrustmarkWithoutExceptions() {
        int months = 36

        Calendar exp = Calendar.getInstance()
        exp.set(Calendar.HOUR_OF_DAY, 23)
        exp.set(Calendar.MINUTE, 59)
        exp.set(Calendar.SECOND, 59)
        exp.set(Calendar.MONTH, exp.get(Calendar.MONTH) + months)

        return exp
    }

}//end TrustmarkController


class TrustmarkCommand {
    Integer trustmarkId
    Integer assessmentId
    String identifier
    String identifierURL
    String statusURL
    String expirationDateTime
    String policyPublicationURL
    String relyingPartyAgreementURL
    String definitionExtension

    String distinguishedName

    Boolean hasExceptions
    String assessorComments

    Integer providingOrganizationId
    Integer providingContactId
    Integer recipientOrganizationId
    Integer recipientContactId


    static constraints = {
        assessmentId(nullable: false)
        providingOrganizationId(nullable: false)
        providingContactId(nullable: false)
        recipientOrganizationId(nullable: false)
        recipientContactId(nullable: false)
        assessmentId(nullable: false)
        identifierURL(nullable: false, blank: false, maxSize: 1024)
        expirationDateTime(nullable: false, blank: false, maxSize: 50)
        policyPublicationURL(nullable: false, blank: false, maxSize: 512)
        relyingPartyAgreementURL(nullable: false, blank: false, maxSize: 512)
        definitionExtension(nullable: true, blank: true, maxSize: 65535)
        assessorComments(nullable: true, blank: true, maxSize: 65535)
        distinguishedName(nullable: false, blank: false, maxSize: 512)
    }


}//end TrustmarkCommand

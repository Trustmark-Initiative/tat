package nstic.web

import assessment.tool.X509CertificateService
import edu.gatech.gtri.trustmark.v1_0.FactoryLoader
import edu.gatech.gtri.trustmark.v1_0.impl.io.json.TrustmarkJsonWebSignatureImpl
import edu.gatech.gtri.trustmark.v1_0.impl.io.xml.SerializerXml
import edu.gatech.gtri.trustmark.v1_0.impl.io.xml.XmlHelper
import edu.gatech.gtri.trustmark.v1_0.impl.model.TrustmarkStatusReportImpl
import edu.gatech.gtri.trustmark.v1_0.io.Serializer
import edu.gatech.gtri.trustmark.v1_0.io.SerializerFactory
import edu.gatech.gtri.trustmark.v1_0.io.TrustmarkDefinitionResolver
import edu.gatech.gtri.trustmark.v1_0.model.TrustmarkStatusCode
import edu.gatech.gtri.trustmark.v1_0.model.TrustmarkStatusReport
import edu.gatech.gtri.trustmark.v1_0.util.TrustmarkDefinitionUtils
import edu.gatech.gtri.trustmark.v1_0.impl.io.xml.TrustmarkXmlSignatureImpl

import grails.converters.JSON
import grails.converters.XML
import grails.plugin.springsecurity.annotation.Secured
import grails.gorm.transactions.Transactional
import nstic.SystemVariableDefinition
import nstic.TrustmarkIdentifierGenerator
import nstic.util.AssessmentStepResultImpl
import nstic.web.assessment.ArtifactData
import nstic.web.assessment.Assessment
import nstic.web.assessment.AssessmentStatus
import nstic.web.assessment.AssessmentStepData
import nstic.web.assessment.AssessmentStepResult
import nstic.web.assessment.AssessmentTrustmarkDefinitionLink
import nstic.web.assessment.ParameterValue
import nstic.web.assessment.Trustmark
import nstic.web.assessment.TrustmarkStatus
import nstic.web.td.AssessmentStep
import nstic.web.td.AssessmentStepArtifact
import nstic.web.td.TdParameter
import nstic.web.td.TrustmarkDefinition
import org.apache.commons.io.FileUtils
import org.apache.commons.lang.StringUtils
import org.grails.help.ParamConversion
import org.springframework.validation.ObjectError

import javax.servlet.ServletException
import javax.xml.bind.DatatypeConverter
import java.security.PrivateKey
import java.security.cert.X509Certificate
import java.text.SimpleDateFormat
import java.time.LocalDateTime


/**
 * Created by brad on 9/9/14.
 */
@Transactional
@Secured("ROLE_USER")
class TrustmarkController {

    def springSecurityService

    /**
     * Lists all trustmarks available in the system
     */
    def list(){
        log.debug("Listing Trustmarks...")
        if (!params.max)
            params.max = '20'
        params.max = Math.min(100, Integer.parseInt(params.max)).toString(); // Limit to at most 100 orgs at a time.

        // case for filtering by recipient organizations
        if (params.containsKey("id")) {
            Integer orgId = Integer.parseInt(params["id"])
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
    def getCreateInfoList() {
        log.debug("Create trustmark called...")
        User user = springSecurityService.currentUser
        Assessment assessment = params.assessment

        log.debug("Collecting TrusmtarkDefinitions for Assessment #${assessment.id}...")
        List<TrustmarkDefinition> trustmarkDefinitions = []
        for(AssessmentTrustmarkDefinitionLink link : assessment.getSortedTds() ){
            if( !trustmarkDefinitions.contains(link.trustmarkDefinition) ){
                trustmarkDefinitions.add(link.trustmarkDefinition)
            }
        }

        // TODO Move this to an AJAX call, due to the stress it puts on the system.
        log.debug("Building display information for TDs...")
        List<GrantTrustmarkInfo> infoList = []
        for( TrustmarkDefinition td : trustmarkDefinitions ){
            log.debug("Checking satisfaction of TD[${td.uri}]...")
            List<AssessmentStepData> steps = assessment.getStepListByTrustmarkDefinition(td)
            GrantTrustmarkInfo info = new GrantTrustmarkInfo(assessment, td)
            info.setSteps(steps)

            List<AssessmentStep> stepsWithNoAnswer = []
            List requiredArtifactProblems = []
            List requiredParameterProblems = []
            for( AssessmentStepData step : steps ){
                if( step.result == null || step.result == AssessmentStepResult.Not_Known )
                    stepsWithNoAnswer.add(step)
                if( step.step.artifacts ){
                    Map<AssessmentStepArtifact, Boolean> artifactSatisfiedMap = [:]
                    for( AssessmentStepArtifact artifact : step.step.artifacts )
                        artifactSatisfiedMap.put(artifact, Boolean.FALSE)
                    if( step.artifacts != null ) {
                        for (ArtifactData artifactData : step.artifacts)
                            if( artifactData.requiredArtifact != null )
                                artifactSatisfiedMap.put(artifactData.requiredArtifact, Boolean.TRUE)
                    }
                    // Now we see if there is any entry that has false as the value.
                    for( AssessmentStepArtifact artifact : step.step.artifacts ){
                        if( Boolean.FALSE.equals(artifactSatisfiedMap.get(artifact)) ){
                            requiredArtifactProblems.add([step: step, artifact: artifact])
                        }
                    }
                }
                if (step.step.parameters) {
                    List problemParameters = []
                    for (parameter in step.step.parameters) {
                        if (!parameter.required) { continue }
                        ParameterValue paramValue = ParameterValue.findByStepDataAndParameter(step, parameter)
                        if (!paramValue?.userValue?.length()) {
                            problemParameters.add(parameter)
                        }
                    }
                    if (!problemParameters.isEmpty()) {
                        requiredParameterProblems.add([step: step, parameters: problemParameters])
                    }
                }

            }
            info.setStepsWithNoAnswer(stepsWithNoAnswer)
            info.setRequiredArtifactProblems(requiredArtifactProblems)
            info.setRequiredParameterProblems(requiredParameterProblems)

            if( stepsWithNoAnswer.isEmpty() && requiredArtifactProblems.isEmpty()  && requiredParameterProblems.isEmpty() ){
                log.debug("Executing issuance criteria for TD[${td.uri}], binding step values...")
                // Now we can execute the issuance Criteria.
                List<edu.gatech.gtri.trustmark.v1_0.model.AssessmentStepResult> results = []
                for( AssessmentStepData step : steps ){
                    results.add(new AssessmentStepResultImpl(step.step.identifier, step.step.stepNumber, step.result))
                }
                try {
                    TrustmarkDefinitionUtils tdUtils = FactoryLoader.getInstance(TrustmarkDefinitionUtils.class)
                    edu.gatech.gtri.trustmark.v1_0.model.TrustmarkDefinition tdFromApi =
                            FactoryLoader.getInstance(TrustmarkDefinitionResolver.class).resolve(td.source.content.toFile(), false)
                    log.debug("  Issuance criteria is: "+tdFromApi.getIssuanceCriteria()+", Values: "+results)
                    boolean satisifiesIssuanceCriteria = tdUtils.checkIssuanceCriteria(tdFromApi, results)
                    info.setIssuanceCriteriaSatisfied(satisifiesIssuanceCriteria)
                    log.info("TD[${td.uri}] issuance criteria evaluated to "+satisifiesIssuanceCriteria)
                }catch(Throwable icError){
                    log.error("Encountered error in TD[${td.uri}] issuance criteria!", icError)
                    info.issuanceCriteriaError = true
                    info.issuanceCriteriaErrorText = icError.toString()
                }
            }

            infoList.add(info)
        }

        [infoList: infoList, assessment: assessment]
    }

    /**
     * Called to create a new set of Trustmarks, based on an existing assessment.  This action will calculate the command object
     * necessary for the create page form (setting all the default values), and then show the create page form.
     */
    @ParamConversion(paramName="assessmentId", toClass=Assessment.class, storeInto = "assessment")
    def create() {
        log.debug("Create trustmark called...")
        User user = springSecurityService.currentUser
        Assessment assessment = params.assessment
        if( assessment == null )
            throw new InvalidRequestError("Could not locate any assessment from assessmentId=${params.assessmentId}")

        if( !assessment.getIsComplete() || assessment.status == AssessmentStatus.ABORTED ){
            log.warn("Cannot grant assessment ${assessment.id} a trustmark, because it is not marked success or fail.")
            throw new ServletException("Cannot grant assessment ${assessment.id} a trustmark, because it is not marked success or fail.")
        }

        log.debug("Collecting TrusmtarkDefinitions for Assessment #${assessment.id}...")
        List<TrustmarkDefinition> trustmarkDefinitions = []
        for(AssessmentTrustmarkDefinitionLink link : assessment.getSortedTds() ){
            if( !trustmarkDefinitions.contains(link.trustmarkDefinition) ){
                trustmarkDefinitions.add(link.trustmarkDefinition)
            }
        }

        log.debug("Listing and sorting all TrustmarkMetdata instances...")
        List<TrustmarkMetadata> metadataList = TrustmarkMetadata.findAll()
        Collections.sort(metadataList, {m1, m2 -> return m1.name.compareToIgnoreCase(m2.name); } as Comparator)

        [assessment: assessment,
         metadataList: metadataList,
         trustmarkDefinitions: trustmarkDefinitions]
    }//end create()

    private String replaceIdentifier( String pattern, String uniqueId ){
        String safeId = URLEncoder.encode(uniqueId, "US-ASCII")
        String id = pattern.replaceAll("@IDENTIFIER@", uniqueId)
        id = id.replaceAll("@URLSAFE_IDENTIFIER@", uniqueId)
        // TODO We can do better, like timestmaps, etc.
        return id
    }

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

    /**
     * Called when the user clicks "Generate" on the create trustmark page.  Actually saves the database object.
     */
    @ParamConversion(paramName="assessmentId", toClass=Assessment.class, storeInto = "assessment")
    def save(){
        User user = springSecurityService.currentUser
        log.debug("Request to grant Trustmarks...")
        Assessment assessment = params.assessment
        if( assessment == null ) {
            log.warn("Bad or missing assessment id")
            throw new InvalidRequestError("Bad or missing assessment id.")
        }

        List<TrustmarkDefinition> tdsToGrantOn = []
        for( String paramName : params.keySet() ){
            if( paramName.startsWith("trustmarkDefinition") && paramName.endsWith("Checkbox") && params.boolean(paramName) ){
                Long id = Long.parseLong(paramName.replace("trustmarkDefinition", "").replace("Checkbox", ""))
                TrustmarkDefinition tdFromDatabase = TrustmarkDefinition.get(id)
                if( tdFromDatabase == null ){
                    log.warn("TD ${id} does not exist!")
                    throw new InvalidRequestError("Invalid trustmarkDefinition: ${id}")
                }
                log.info("Granting trustmark for TD[${tdFromDatabase.name}, v.${tdFromDatabase.tdVersion}]")
                tdsToGrantOn.add(tdFromDatabase)
            }
        }
        if( tdsToGrantOn.isEmpty() ){
            log.warn("The user has selected no TDs to grant Trustmarks for!")
            flash.error = "You must select at least 1 TD to grant a Trustmark on."
            return redirect(action: 'create', params: [assessmentId: assessment.id])
        }

        TrustmarkMetadata metadata = TrustmarkMetadata.get(params['trustmarkMetadataId'])
        if( metadata == null ){
            //throw new InvalidRequestError("Invalid trustmarkMetadataId!")
            log.warn("No trustmark metadata has been created!")
            flash.error = "You must generate and select at least 1 trustmark metadata set to grant a Trustmark on."
            return redirect(controller: 'trustmarkMetadata', action: 'create')
        }


        TrustmarkIdentifierGenerator identifierGenerator = Class.forName(metadata.generatorClass).newInstance()
        Calendar now = Calendar.getInstance()

        for( TrustmarkDefinition td : tdsToGrantOn ){
            // Metadata
            String id = identifierGenerator.generateNext()
            Trustmark trustmark = new Trustmark(trustmarkDefinition: td, assessment: assessment)
            trustmark.identifier = id
            trustmark.identifierURL = replaceIdentifier(metadata.identifierPattern, id)
            trustmark.statusURL = replaceIdentifier(metadata.statusUrlPattern, id)
            trustmark.status = TrustmarkStatus.OK
            trustmark.recipientOrganization = assessment.assessedOrganization
            trustmark.recipientContactInformation = assessment.assessedContact
            trustmark.issueDateTime = now.getTime()
            trustmark.policyPublicationURL = metadata.policyUrl
            trustmark.relyingPartyAgreementURL = metadata.relyingPartyAgreementUrl
            trustmark.providerOrganization = metadata.provider
            trustmark.providerContactInformation = metadata.provider.primaryContact
            trustmark.providerExtension = "<gtri:assessment-id xmlns:gtri=\"urn:edu:gatech:gtri:trustmark:assessment\">${assessment.id}</gtri:assessment-id>"
            trustmark.grantingUser = user

            if( StringUtils.isNotBlank(params['td'+td.id+'ExtensionData']) ){
                trustmark.definitionExtension = params['td'+td.id+'ExtensionData']
            }

            Integer expirationMonthsCount = -1
            if( params.boolean('td'+td.id+'HasExceptions')  ){
                trustmark.hasExceptions = true
                trustmark.assessorComments = params['td'+td.id+'ExceptionsDesc'] ?: 'No comments given.'
                expirationMonthsCount = metadata.timePeriodWithExceptions
            }else{
                trustmark.hasExceptions = false
                expirationMonthsCount = metadata.timePeriodNoExceptions
            }
            Calendar expirationDate = Calendar.getInstance()
            expirationDate.setTime(now.getTime())
            expirationDate.add(Calendar.MONTH, expirationMonthsCount)
            trustmark.expirationDateTime = expirationDate.getTime()

            // Parameters
            AssessmentStepData[] stepList = assessment.getStepListByTrustmarkDefinition(td)
            TdParameter[] firstUnfilledRequiredParametersPerStep = stepList.collect { it.firstUnfilledRequiredParameter }
            TdParameter firstUnfilledRequiredParameter = firstUnfilledRequiredParametersPerStep.find { it }
            if (firstUnfilledRequiredParameter) {
                String message = String.format(
                    "Unfilled required parameter: TD[%s] >> Step[%s] >> Parameter[%s]",
                    td.name,
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
                trustmark.addToParameterValues(parameterValue)
            }

            // generate and save xml signature for this trustmark
            SigningCertificate signingCertificate = SigningCertificate.findById(metadata.defaultSigningCertificateId)
            if (signingCertificate == null) {
                throw new ServletException("Need at least one signing certificate to perform this operation.")
            }

            signTrustmark(signingCertificate, trustmark)

            trustmark.save(failOnError: true)

            assessment.logg.addEntry("Trustmark Granted", "Trustmark Granted",
                    "User ${user.username} has granted Trustmark[${trustmark.identifier}] to Contact[${trustmark.recipientContactInformation.responder}] from Organization[${trustmark.providerOrganization.name}] for assessment[${assessment.id}]",
                    [
                            user: [id: user.id, usenrame: user.username],
                            assessment: [id: assessment.id],
                            trustmark: trustmark.toJsonMap(false)
                    ])

            log.info("Successfully granted trustmark for Assessment #${assessment.id}, TrustmarkDefinition: ${td.uri}")
        }

        log.info("All trustmarks granted, forwarding to the assessment page...")
        redirect(controller: 'assessment', action: 'view', id: assessment.id)
    }//end save()

    /**
     * Called to view a trustmark in the system.
     */
    def view() {
        def user = springSecurityService.currentUser
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
        User user = springSecurityService.currentUser
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

        signTrustmark(signingCertificate, trustmark)

        trustmark.save(failOnError: true, flush: true)

        redirect(action: 'view', id: trustmark.id)
    }

    /**
     * Generates the XML for a trustmark in the system.
     */
    def generateXml() {
        User user = springSecurityService.currentUser
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
     * This method displays the status XML for a given trustmark.
     */
    def generateStatusXML() {
        User user = springSecurityService.currentUser
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
        User user = springSecurityService.currentUser
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
        User user = springSecurityService.currentUser
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
        User user = springSecurityService.currentUser
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
        User user = springSecurityService.currentUser
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
        User user = springSecurityService.currentUser
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
                        reissuedTrustmark.identifierURL = replaceIdentifier(metadata.identifierPattern, id)
                        reissuedTrustmark.statusURL = replaceIdentifier(metadata.statusUrlPattern, id)
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
                        signTrustmark(signingCertificate, reissuedTrustmark)

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
                    reissuedTrustmark.identifierURL = replaceIdentifier(metadata.identifierPattern, id)
                    reissuedTrustmark.statusURL = replaceIdentifier(metadata.statusUrlPattern, id)
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

                    signTrustmark(signingCertificate, reissuedTrustmark)

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
        User user = springSecurityService.currentUser
        if( StringUtils.isEmpty(params.id) )
            throw new ServletException("Missing required parameter 'id'.")
        if( StringUtils.isEmpty(params.reason) )
            throw new ServletException("Missing required parameter 'reason'.")

        Organization org = Organization.findById(params.id)
        if( org == null )
            throw new ServletException("Missing organization")

        List<Trustmark> trustmarks = Trustmark.findAllByRecipientOrganization(org)

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

    private String toXml( Trustmark trustmark ){
        StringBuilder xmlBuilder = new StringBuilder()

        Calendar issueDateTimeCal = Calendar.getInstance()
        issueDateTimeCal.setTime(trustmark.issueDateTime)

        Calendar expirationDateTimeCal = Calendar.getInstance()
        expirationDateTimeCal.setTime(trustmark.expirationDateTime)

        xmlBuilder.append("""<?xml version="1.0"?>

<tf:Trustmark xmlns:tf="https://trustmarkinitiative.org/specifications/trustmark-framework/1.4/schema/"
        tf:id="_${trustmark.identifier}">

    <tf:Identifier>${trustmark.identifierURL}</tf:Identifier>

    <tf:TrustmarkDefinitionReference>
        <tf:Identifier>${trustmark.trustmarkDefinition.uri}</tf:Identifier>
        <tf:Name>${trustmark.trustmarkDefinition.name}</tf:Name>
        <tf:Version>${trustmark.trustmarkDefinition.tdVersion}</tf:Version>
    </tf:TrustmarkDefinitionReference>

    <tf:IssueDateTime>${DatatypeConverter.printDateTime(issueDateTimeCal)}</tf:IssueDateTime>
    <tf:ExpirationDateTime>${DatatypeConverter.printDateTime(expirationDateTimeCal)}</tf:ExpirationDateTime>

    <tf:PolicyURL>${trustmark.policyPublicationURL}</tf:PolicyURL>
    <tf:RelyingPartyAgreementURL>${trustmark.relyingPartyAgreementURL}</tf:RelyingPartyAgreementURL>
    <tf:StatusURL>${trustmark.statusURL}</tf:StatusURL>

    <tf:Provider>
        <tf:Identifier>${trustmark.providerOrganization.uri}</tf:Identifier>
        <tf:Name>${trustmark.providerOrganization.name}</tf:Name>
        <tf:Contact>
            <tf:Kind>PRIMARY</tf:Kind>
            <tf:Responder>${trustmark.providerContactInformation.responder ?: ""}</tf:Responder>
            <tf:Email>${trustmark.providerContactInformation.email ?: ""}</tf:Email>
            <tf:Telephone>${trustmark.providerContactInformation.phoneNumber ?: ""}</tf:Telephone>
            <tf:MailingAddress>${trustmark.providerContactInformation.mailingAddress ?: ""}</tf:MailingAddress>
            <tf:Notes>${trustmark.providerContactInformation.notes ?: ""}</tf:Notes>
        </tf:Contact>
    </tf:Provider>

    <tf:Recipient>
        <tf:Identifier>${trustmark.recipientOrganization.uri}</tf:Identifier>
        <tf:Name>${trustmark.recipientOrganization.name}</tf:Name>
        <tf:Contact>
            <tf:Kind>PRIMARY</tf:Kind>
            <tf:Responder>${trustmark.recipientContactInformation.responder ?: ""}</tf:Responder>
            <tf:Email>${trustmark.recipientContactInformation.email ?: ""}</tf:Email>
            <tf:Telephone>${trustmark.recipientContactInformation.phoneNumber ?: ""}</tf:Telephone>
            <tf:MailingAddress>${trustmark.recipientContactInformation.mailingAddress ?: ""}</tf:MailingAddress>
            <tf:Notes>${trustmark.recipientContactInformation.notes ?: ""}</tf:Notes>
        </tf:Contact>
    </tf:Recipient>
""")

        if( StringUtils.isNotEmpty(trustmark.definitionExtension) ) {
            xmlBuilder.append("""
    <tf:DefinitionExtension>
${trustmark.definitionExtension ?: ""}
    </tf:DefinitionExtension>
""")
        }


        xmlBuilder.append("""
    <tf:ProviderExtension>
${trustmark.providerExtension ?: ""}
        <nief:TrustmarkProviderExtension xmlns:nief="https://nief.gfipm.net/trustmarks">
            <nief:has-exceptions>${trustmark.hasExceptions}</nief:has-exceptions>
            """)

        if( trustmark.hasExceptions || StringUtils.isNotEmpty(trustmark.assessorComments) ){
            xmlBuilder.append("            <nief:exception-details><![CDATA[${trustmark.assessorComments}]]></nief:exception-details>\n")
        }

        xmlBuilder.append("""
        </nief:TrustmarkProviderExtension>
    </tf:ProviderExtension>
""")
        if (trustmark?.parameterValues?.size()) {
            xmlBuilder.append("""
    <tf:ParameterBindings>""")
            for (parameterValue in trustmark.parameterValues) {
                xmlBuilder.append("""
        <tf:ParameterBinding tf:identifier="${parameterValue.parameter.identifier}" tf:kind="${parameterValue.parameter.kind}">${parameterValue.userValue}</tf:ParameterBinding>""")
            }
            xmlBuilder.append("""
    </tf:ParameterBindings>
""")
        }

        xmlBuilder.append("""
</tf:Trustmark>

""")

        return xmlBuilder.toString()
    }

    private void signTrustmark(SigningCertificate signingCertificate, Trustmark trustmark) {

        // get the X509 certificate and private key
        X509CertificateService certService = new X509CertificateService()
        X509Certificate x509Certificate = certService.convertFromPem(signingCertificate.x509CertificatePem)
        PrivateKey privateKey = certService.getPrivateKeyFromPem(signingCertificate.privateKeyPem)

        // Generate XML Signature
        signTrustmarkXML(x509Certificate, privateKey, trustmark)

        // generate JSON Web Signature
        signTrustmarkJSON(x509Certificate, privateKey, trustmark)

        // save the signing certificate used
        trustmark.signingCertificateId = signingCertificate.id
    }

    private void signTrustmarkXML(X509Certificate x509Certificate, PrivateKey privateKey, Trustmark trustmark) {

        // get the trustmark's XML string
        String trustmarkXml = toXml(trustmark)

        // get the signed trustmark's XML string
        TrustmarkXmlSignatureImpl trustmarkXmlSignature = new TrustmarkXmlSignatureImpl()

        String referenceUri = "tf:id"
        String signedXml = trustmarkXmlSignature.generateXmlSignature(x509Certificate, privateKey,
                referenceUri, trustmarkXml)

        // Validate the trustmark's signed xml against the Trustmark Framework XML schema
        XmlHelper.validateXml(signedXml)
        log.debug("Successfully validated trustmark's signed XML")

        // validate the signature before saving
        boolean validXmlSignature = trustmarkXmlSignature.validateXmlSignature(referenceUri, signedXml)

        if (!validXmlSignature) {
            throw new ServletException("The Trustmark's XML signature failed validation.")
        }

        // save the signed trustmark's XML string to the db
        trustmark.signedXml = signedXml
    }

    private void signTrustmarkJSON(X509Certificate x509Certificate, PrivateKey privateKey, Trustmark trustmark) {

        TrustmarkJsonWebSignatureImpl trustmarkJsonWebSignature = new TrustmarkJsonWebSignatureImpl()

        String trustmarkJson = trustmark.toJsonMap()

        String signedJson = trustmarkJsonWebSignature.generateJsonWebSignature(privateKey, trustmarkJson)

        // validate the signature before saving
        boolean validJsonWebSignature = trustmarkJsonWebSignature.validateJsonWebSignature(x509Certificate, signedJson)

        if (!validJsonWebSignature) {
            throw new ServletException("The Trustmark's JSON WEB Signature failed validation.")
        }

        trustmark.signedJson = signedJson
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

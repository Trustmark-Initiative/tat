package assessment.tool

import edu.gatech.gtri.trustmark.v1_0.FactoryLoader
import edu.gatech.gtri.trustmark.v1_0.impl.io.json.TrustmarkJsonWebSignatureImpl
import edu.gatech.gtri.trustmark.v1_0.impl.io.xml.TrustmarkXmlSignatureImpl
import edu.gatech.gtri.trustmark.v1_0.impl.io.xml.XmlHelper
import edu.gatech.gtri.trustmark.v1_0.io.TrustmarkDefinitionResolver
import edu.gatech.gtri.trustmark.v1_0.util.TrustmarkDefinitionUtils
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import nstic.TrustmarkIdentifierGenerator
import nstic.util.AssessmentStepResultImpl
import nstic.web.GrantTrustmarkInfo
import nstic.web.InvalidRequestError
import nstic.web.SigningCertificate
import nstic.web.TrustmarkMetadata
import nstic.web.User
import nstic.web.assessment.ArtifactData
import nstic.web.assessment.Assessment
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
import org.apache.commons.lang.StringUtils
import org.springframework.transaction.TransactionStatus
import javax.servlet.ServletException
import javax.xml.bind.DatatypeConverter
import java.security.PrivateKey
import java.security.cert.X509Certificate
import java.util.concurrent.ConcurrentHashMap


/**
 * This service will
 * <br/>
 * It is expected that this service be asynchronously called from a controller, and thus will establish it's own
 * database transactions.
 * <br/><br/>
 * @author robert
 * @date 02/08/21
 */

class TrustmarkService {
    //==================================================================================================================
    //  VARIABLES
    //==================================================================================================================

    Map<String, Object> objectMap  = new ConcurrentHashMap<String, Object>()

    def sessionFactory

    public static final String INFO_LIST_STATUS_VAR = TrustmarkService.class.getName()+".INFO_LIST_STATUS"
    public static final String INFO_LIST_PERCENT_VAR = TrustmarkService.class.getName()+".INFO_LIST_PERCENT"
    public static final String INFO_LIST_MESSAGE_VAR = TrustmarkService.class.getName()+".INFO_LIST_MESSAGE"

    public static final String TRUSTMARK_GENERATION_STATUS_VAR = TrustmarkService.class.getName()+".TRUSTMARK_GENERATION_STATUS"
    public static final String TRUSTMARK_GENERATION_PERCENT_VAR = TrustmarkService.class.getName()+".TRUSTMARK_GENERATION_PERCENT"
    public static final String TRUSTMARK_GENERATION_MESSAGE_VAR = TrustmarkService.class.getName()+".TRUSTMARK_GENERATION_MESSAGE"

    public static final String SYNC_VAR = "SYNC";
    public static final String INFO_LIST_EXECUTING_VAR = TrustmarkService.class.simpleName+".INFO_LIST_EXECUTING"
    public static final String TRUSTMARK_GENERATION_EXECUTING_VAR = TrustmarkService.class.simpleName+".TRUSTMARK_GENERATION_EXECUTING"

    public static final String TRUSTMARK_DEFINITIONS_PARAMS_MAP_VAR = TrustmarkService.class.simpleName+".TRUSTMARK_DEFINITIONS_PARAMS_MAP"
    public static final String TRUSTMARK_DEFINITIONS_TO_GRANT_ON_VAR = TrustmarkService.class.simpleName+".TRUSTMARK_DEFINITIONS_TO_GRANT_ON"

    public static final String GENERATED_TD_INFO_LIST_VAR = TrustmarkService.class.simpleName+".GENERATED_TD_INFO_LIST"
    public static final String GENERATED_TRUSTMARK_LIST_VAR = TrustmarkService.class.simpleName+".GENERATED_TRUSTMARK_LIST"


    void setAttribute(String key, Object value) {
        objectMap.put(key, value)
    }

    Object getAttribute(String key) {
        return objectMap.get(key)
    }

    void removeAttribute(String key) {
        objectMap.remove(key)
    }

    void logAttributes(String context) {
        log.info("TrustmarkService current attribute set for ${context}:")
        synchronized (this) {
            objectMap.each { key, value ->
                log.debug("       [${key}, ${value.toString()}]")
            }
        }
    }

    String getAttributesString(String context) {
        StringBuilder buffer = new StringBuilder()
        buffer.append("TrustmarkService current attribute set for ${context}:\n")
        synchronized (this) {
            objectMap.each { key, value ->
                buffer.append("[${key}, ${value.toString()}]\n")
            }
        }
        return buffer.toString()
    }

    boolean isExecuting(String property) {
        String value = getAttribute(property)
        if (StringUtils.isBlank(value)) {
            value = "false"
        }

        return Boolean.parseBoolean(value);
    }

    void setExecuting(String property) {
        synchronized (SYNC_VAR) {
            setAttribute(property, "true")
        }
    }

    void stopExecuting(String property) {
        setAttribute(property, "false");
    }

    @Transactional
    void createGrantTrustmarkInfoList(Integer assessmentId) {

        Assessment assessment = Assessment.findById(assessmentId)

        log.debug("TrustmarkService::createGrantTrustmarkInfoList for assessment: ${assessment.assessmentName}")

        // TODO Move this to an AJAX call, due to the stress it puts on the system.
        log.debug("Building display information for TDs...")
        log.debug("Collecting TrusmtarkDefinitions for Assessment #${assessment.id}...")
        List<TrustmarkDefinition> trustmarkDefinitions = []

        for (AssessmentTrustmarkDefinitionLink link : assessment.getSortedTds()) {
            if (!trustmarkDefinitions.contains(link.trustmarkDefinition)) {
                trustmarkDefinitions.add(link.trustmarkDefinition)
            }
        }

        long startTime = System.currentTimeMillis()

        List<GrantTrustmarkInfo> infoList = []

        int currentTdIndex = 0
        for (TrustmarkDefinition td : trustmarkDefinitions) {
            log.debug("Checking satisfaction of TD[${td.uri}]...")

            // operation has been cancelled
            if (!isExecuting(INFO_LIST_EXECUTING_VAR)) {

                // exit operation
                return
            }

            List<AssessmentStepData> steps = assessment.getStepListByTrustmarkDefinition(td)
            GrantTrustmarkInfo info = new GrantTrustmarkInfo(assessment, td)
            info.setSteps(steps)

            List<AssessmentStep> stepsWithNoAnswer = []
            List requiredArtifactProblems = []
            List requiredParameterProblems = []
            for (AssessmentStepData step : steps) {
                if (step.result == null || step.result == AssessmentStepResult.Not_Known)
                    stepsWithNoAnswer.add(step)
                if (step.step.artifacts) {
                    Map<AssessmentStepArtifact, Boolean> artifactSatisfiedMap = [:]
                    for (AssessmentStepArtifact artifact : step.step.artifacts)
                        artifactSatisfiedMap.put(artifact, Boolean.FALSE)
                    if (step.artifacts != null) {
                        for (ArtifactData artifactData : step.artifacts)
                            if (artifactData.requiredArtifact != null)
                                artifactSatisfiedMap.put(artifactData.requiredArtifact, Boolean.TRUE)
                    }
                    // Now we see if there is any entry that has false as the value.
                    for (AssessmentStepArtifact artifact : step.step.artifacts) {
                        if (Boolean.FALSE.equals(artifactSatisfiedMap.get(artifact))) {
                            requiredArtifactProblems.add([step: step, artifact: artifact])
                        }
                    }
                }
                if (step.step.parameters) {
                    List problemParameters = []
                    for (parameter in step.step.parameters) {
                        if (!parameter.required) {
                            continue
                        }

                        ParameterValue paramValue = ParameterValue.findByStepDataAndParameter(step, parameter)
                        if (!paramValue?.userValue?.length()) {
                            problemParameters.add(parameter)

                            // Need to access the many in the one to many relationshio to avoid LazyInitializationException
                            def paramString = (parameter.toJsonMap(true) as JSON) as String
                            if (paramString && StringUtils.isNotEmpty(paramString)) {
                                log.debug("Required problem parameter: [${paramString}]...")
                            }
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

            if (stepsWithNoAnswer.isEmpty() && requiredArtifactProblems.isEmpty() && requiredParameterProblems.isEmpty()) {
                log.debug("Executing issuance criteria for TD[${td.uri}], binding step values...")
                // Now we can execute the issuance Criteria.
                List<edu.gatech.gtri.trustmark.v1_0.model.AssessmentStepResult> results = []
                for (AssessmentStepData step : steps) {
                    results.add(new AssessmentStepResultImpl(step.step.identifier, step.step.stepNumber, step.result))
                }
                try {
                    TrustmarkDefinitionUtils tdUtils = FactoryLoader.getInstance(TrustmarkDefinitionUtils.class)
                    edu.gatech.gtri.trustmark.v1_0.model.TrustmarkDefinition tdFromApi =
                            FactoryLoader.getInstance(TrustmarkDefinitionResolver.class).resolve(td.source.content.toFile(), false)
                    log.debug("  Issuance criteria is: " + tdFromApi.getIssuanceCriteria() + ", Values: " + results)
                    boolean satisifiesIssuanceCriteria = tdUtils.checkIssuanceCriteria(tdFromApi, results)
                    info.setIssuanceCriteriaSatisfied(satisifiesIssuanceCriteria)
                    log.info("TD[${td.uri}] issuance criteria evaluated to " + satisifiesIssuanceCriteria)
                } catch (Throwable icError) {
                    log.error("Encountered error in TD[${td.uri}] issuance criteria!", icError)
                    info.issuanceCriteriaError = true
                    info.issuanceCriteriaErrorText = icError.toString()
                }
            }

            infoList.add(info)

            int percent = (int) Math.floor(((double) currentTdIndex++ / (double) trustmarkDefinitions.size()) * 100.0d)
            setAttribute(TrustmarkService.INFO_LIST_PERCENT_VAR, "" + percent)
        }

        setAttribute(TrustmarkService.GENERATED_TD_INFO_LIST_VAR, infoList)

        long stopTime = System.currentTimeMillis()
        log.info("** Building display information for TDs time: ${(stopTime - startTime)}ms.")

        setAttribute(TrustmarkService.INFO_LIST_PERCENT_VAR, "100")
        setAttribute(TrustmarkService.INFO_LIST_MESSAGE_VAR, "Successfully built display information for TDs in ${(stopTime - startTime)}ms.")
        setAttribute(TrustmarkService.INFO_LIST_STATUS_VAR, "SUCCESS")

    } //end createGrantTrustmarkInfoList()

    @Transactional
    void generateTrustmarkList(Integer userId, Integer assessmentId, Integer metadataSetId) {

        User user = User.findById(userId)
        Assessment assessment = Assessment.findById(assessmentId)
        TrustmarkMetadata metadata = TrustmarkMetadata.findById(metadataSetId)

        def paramsMap = (Map) getAttribute(TrustmarkService.TRUSTMARK_DEFINITIONS_PARAMS_MAP_VAR)

        def tdsToGrantOn = (List<TrustmarkDefinition>) getAttribute(TrustmarkService.TRUSTMARK_DEFINITIONS_TO_GRANT_ON_VAR)

        TrustmarkIdentifierGenerator identifierGenerator = Class.forName(metadata.generatorClass).newInstance()
        Calendar now = Calendar.getInstance()

        log.debug("TrustmarkService::generateTrustmarkList, iterating TrusmtarkDefinitions to generate Trustmarks")

        setAttribute(TrustmarkService.TRUSTMARK_GENERATION_MESSAGE_VAR, "Generating trustmarks...")
        setAttribute(TrustmarkService.TRUSTMARK_GENERATION_PERCENT_VAR, "0")

        List<Trustmark> trustmarkList = []

        long startTime = System.currentTimeMillis()

        int currentTmIndex = 0
        for (TrustmarkDefinition td : tdsToGrantOn) {

            // operation has been cancelled
            if (!isExecuting(TRUSTMARK_GENERATION_EXECUTING_VAR)) {

                // exit operation
                return
            }

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

            if (StringUtils.isNotBlank(paramsMap['td' + td.id + 'ExtensionData'])) {
                trustmark.definitionExtension = paramsMap['td' + td.id + 'ExtensionData']
            }

            Integer expirationMonthsCount = -1
            if (getBooleanValue(paramsMap['td' + td.id + 'HasExceptions'])) {
                trustmark.hasExceptions = true
                trustmark.assessorComments = paramsMap['td' + td.id + 'ExceptionsDesc'] ?: 'No comments given.'
                expirationMonthsCount = metadata.timePeriodWithExceptions
            } else {
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

            // add to generated trustamrks list, to be saved later
            trustmarkList.add(trustmark)

            log.info("Successfully granted trustmark for Assessment #${assessment.id}, TrustmarkDefinition: ${td.uri}")

            // update progress percentage
            int percent = (int) Math.floor(((double) currentTmIndex++ / (double) tdsToGrantOn.size()) * 100.0d)

            setAttribute(TrustmarkService.TRUSTMARK_GENERATION_PERCENT_VAR, "" + percent)

//            String percentString = getAttribute(TrustmarkService.TRUSTMARK_GENERATION_PERCENT_VAR);
//            log.info("** TRUSTMARK_GENERATION_PERCENT_VAR: ${percent}%.")
//            log.info("** percentString: ${percentString}%.")
        }

        long stopTime = System.currentTimeMillis()
        log.info("Iterating TDs for generating trustmark list time: ${(stopTime - startTime)}ms.")

        setAttribute(TrustmarkService.TRUSTMARK_GENERATION_MESSAGE_VAR, "Saving trustmarks to database...")
        setAttribute(TrustmarkService.TRUSTMARK_GENERATION_PERCENT_VAR, "0")


        // save trustmarks
        // init item counter
        currentTmIndex = 0

        // wrap saving trustmarks with transaction for rollback support
        Trustmark.withTransaction { TransactionStatus status ->
            trustmarkList.each { trustmark ->

                if (isExecuting(TRUSTMARK_GENERATION_EXECUTING_VAR)) {
                    trustmark.save(failOnError: true)

                    assessment.logg.addEntry("Trustmark Granted", "Trustmark Granted",
                            "User ${user.username} has granted Trustmark[${trustmark.identifier}] to Contact[${trustmark.recipientContactInformation.responder}] from Organization[${trustmark.providerOrganization.name}] for assessment[${assessment.id}]",
                            [
                                    user      : [id: user.id, usenrame: user.username],
                                    assessment: [id: assessment.id],
                                    trustmark : trustmark.toJsonMap(false)
                            ])
                }
                else { // the trustmark generation has been cancelled

                    // rollback all previously saved trustmarks
                    status.setRollbackOnly()

                    // exit thread
                    return
                }

                // update progress percentage
                int percent = (int) Math.floor(((double) currentTmIndex++ / (double) trustmarkList.size()) * 100.0d)

                setAttribute(TrustmarkService.TRUSTMARK_GENERATION_PERCENT_VAR, "" + percent)

//                String percentString = getAttribute(TrustmarkService.TRUSTMARK_GENERATION_PERCENT_VAR);
//                log.info("** TRUSTMARK_GENERATION_PERCENT_VAR: ${percent}%.")
//                log.info("** percentString: ${percentString}%.")

            }
        }

        // done
        setAttribute(TrustmarkService.TRUSTMARK_GENERATION_PERCENT_VAR, "100")
        setAttribute(TrustmarkService.TRUSTMARK_GENERATION_MESSAGE_VAR, "Successfully generated trustmark list in ${(stopTime - startTime)}ms.")
        setAttribute(TrustmarkService.TRUSTMARK_GENERATION_STATUS_VAR, "SUCCESS")
    }

    void signTrustmark(SigningCertificate signingCertificate, Trustmark trustmark) {

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

    void signTrustmarkXML(X509Certificate x509Certificate, PrivateKey privateKey, Trustmark trustmark) {

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

    void signTrustmarkJSON(X509Certificate x509Certificate, PrivateKey privateKey, Trustmark trustmark) {

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

    boolean getBooleanValue(String value) {
        if (value == null) {
            return false;
        }

        return ("Y".equals(value.toUpperCase())
                ||"Yes".equals(value.toUpperCase())
                || "1".equals(value.toUpperCase())
                || "TRUE".equals(value.toUpperCase())
                || "ON".equals(value.toUpperCase())
        );
    }

    String replaceIdentifier( String pattern, String uniqueId ){
        String safeId = URLEncoder.encode(uniqueId, "US-ASCII")
        String id = pattern.replaceAll("@IDENTIFIER@", uniqueId)
        id = id.replaceAll("@URLSAFE_IDENTIFIER@", uniqueId)
        // TODO We can do better, like timestmaps, etc.
        return id
    }
}

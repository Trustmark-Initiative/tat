package nstic.web

import grails.converters.JSON
import grails.converters.XML
import grails.gorm.transactions.Transactional
import nstic.TATPropertiesHolder
import nstic.TrustmarkIdentifierGenerator
import nstic.util.AssessmentToolProperties
import nstic.web.assessment.ArtifactData
import nstic.web.assessment.Assessment
import org.grails.help.ParamConversion
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.validation.ObjectError

import javax.servlet.ServletContext
import javax.servlet.ServletException
import nstic.util.DefaultMetadataUtils

/**
 * Created by brad on 5/5/16.
 */
@Transactional
@PreAuthorize('hasAuthority("tat-admin")')
class TrustmarkMetadataController {

    //==================================================================================================================
    //  Web Methods
    //==================================================================================================================
    def index(){
        return redirect(action: 'list');
    }

    def list() {
        log.debug("Listing Trustmark Metadata objects...")
        if (!params.max)
            params.max = '20';
        params.max = Math.min(100, Integer.parseInt(params.max)).toString(); // Limit to at most 100 TrustmarkMetadata at a time.
        [tmList: TrustmarkMetadata.list(params), countTotal: TrustmarkMetadata.count()]
    }//end list()


    def view() {
        log.debug("Attempting to view TrustmarkMetadata: ${params.id}")
        TrustmarkMetadata metadata = fromIdParam(params.id);

        if( request.format?.equalsIgnoreCase("json") ) {
            throw new UnsupportedOperationException("NOT YET IMPLEMENTED")
        }else if( request.format?.equalsIgnoreCase("xml") ) {
            throw new UnsupportedOperationException("NOT YET IMPLEMENTED")
        }else if( request.format?.equalsIgnoreCase("html") || request.format?.equals("all")){
            log.debug("Requested format: "+request.format);
            // TODO Other formats?
            SigningCertificate signingCertificate = SigningCertificate.findById(metadata.defaultSigningCertificateId)
            [metadata: metadata,
             signingCertificate: signingCertificate]
        }else{
            log.error("Invalid format[${request.format}] requested.")
            throw new InvalidFormatError("This system only supports HTML, JSON or XML for TrustmarkMetadata.  Format[${request.format}] is not supported.")
        }
    }//end view()


    def create() {
        log.debug("Showing create TrustmarkMetadata form...");
        // TODO List all organizations?
        [command: new CreateTrustmarkMetadataCommand()]
    }//end create()

    def save(CreateTrustmarkMetadataCommand command){
        User user = User.findByUsername(((OAuth2AuthenticationToken) SecurityContextHolder.getContext().getAuthentication()).getName());
        log.debug("Processing CreateTrustmarkMetadataCommand...")
        if( !command.validate() ){
            log.warn "CreateTrustmarkMetadataCommand form does not validate: "
            command.errors.getAllErrors().each { ObjectError error ->
                log.warn "    ${error.defaultMessage}"
            }
            return render(view: 'create', model: [command: command])
        }

        log.info("User ${user.username} is saving a new TrustmarkMetadata ${command.name}...")
        TrustmarkMetadata metadata = new TrustmarkMetadata();
        metadata.setData(command);

        // set defaults
        Properties props = AssessmentToolProperties.getProperties()

        metadata.generatorClass = nstic.UUIDTrustmarkIdentifierGenerator.class.name
        metadata.identifierPattern = DefaultMetadataUtils.buildIdentifierPattern(props)
        metadata.statusUrlPattern = DefaultMetadataUtils.buildStatusUrlPattern(props)

        metadata.save(failOnError: true);
        flash.message = "Successfully created TrustmarkMetadata '${metadata.name}'"

        return redirect(action: 'list');
    }//end save()


    def edit() {
        log.debug("Request to edit TrustmarkMetadata ${params.id}")
        TrustmarkMetadata metadata = fromIdParam(params.id);
        EditTrustmarkMetadataCommand command = new EditTrustmarkMetadataCommand();
        command.setData(metadata);

        [command: command]
    }//end edit()

    def update(EditTrustmarkMetadataCommand command){
        log.info("========================================= UPDATING TM METADATA ===============================>");
        User user = User.findByUsername(((OAuth2AuthenticationToken) SecurityContextHolder.getContext().getAuthentication()).getName());
        log.debug("Processing EditTrustmarkMetadataCommand...")
        if (!command.validate()) {
            log.warn "EditTrustmarkMetadataCommand form does not validate: "
            command.errors.getAllErrors().each { ObjectError error ->
                log.warn "    ${error.defaultMessage}"
            }
            return render(view: 'edit', model: [command: command])
        }

        log.info("User ${user.username} is editing TrustmarkMetadata ${command.trustmarkMetadataId}...")
        TrustmarkMetadata metadata = TrustmarkMetadata.findById(command.trustmarkMetadataId);
        if (metadata == null) {
            log.warn("${command.trustmarkMetadataId} does not identify a valid TrustmarkMetadata instance.")
            throw new InvalidRequestError("${command.trustmarkMetadataId} does not identify a valid TrustmarkMetadata instance.");
        }
        metadata.name = command.name;
        metadata.description = command.description;
        metadata.policyUrl = command.policyUrl;
        metadata.relyingPartyAgreementUrl = command.relyingPartyAgreementUrl;
        metadata.timePeriodNoExceptions = command.timePeriodNoExceptions;
        metadata.timePeriodWithExceptions = command.timePeriodWithExceptions;
        metadata.provider = Organization.get(command.organizationId);

        metadata.defaultSigningCertificateId = command.defaultSigningCertificateId

        // set defaults
        Properties props = AssessmentToolProperties.getProperties()

        metadata.generatorClass = nstic.UUIDTrustmarkIdentifierGenerator.class.name
        metadata.identifierPattern = DefaultMetadataUtils.buildIdentifierPattern(props)
        metadata.statusUrlPattern = DefaultMetadataUtils.buildStatusUrlPattern(props)

        metadata.save(failOnError: true, flush: true);
        flash.message = "Successfully edited Trustmark Metadata '${metadata.name}'"

        log.info("<========================================= UPDATING TM METADATA DONE !===========================");
        return redirect(action: 'view', id: metadata.id);
    }//end update()

    def listAvailableCertificates() {
        Integer orgId = params.id as Integer
        log.debug("Attempting to load certificates for organization: ${orgId}")

        Organization org = Organization.findById(orgId)

        if (!org) {
            log.warn("${orgId} does not identify a valid Organization instance.")
            throw new InvalidRequestError("${orgId} does not identify a valid Organization instance.")
        }

        List<SigningCertificate> certs = SigningCertificate.findAllByOrganization(org)

        def activeCertificates = []
        certs.each { signingCertificate ->
            if (signingCertificate.status == nstic.web.SigningCertificateStatus.ACTIVE) {

                activeCertificates.add(signingCertificate);
            }
        }

        log.debug("Rendering list...")
        withFormat {
            html {
                throw new ServletException("Not Yet Implemented")
            }
            xml {
                render activeCertificates as XML
            }
            json {
                // render certs as JSON
                def resultsJSON = []
                activeCertificates.each{ result ->
                    def resultJSON = [
                            id: result.id,
                            distinguishedName: result.distinguishedName ?: "",
                    ]
                    resultsJSON.add(resultJSON);

                }
                render resultsJSON as JSON
            }
        }

    }

    def listAvailablePolicyDocuments() {
        Integer orgId = params.id as Integer
        log.debug("Attempting to load policy documents for organization: ${orgId}")

        Organization org = Organization.findById(orgId)

        if (!org) {
            log.warn("${orgId} does not identify a valid Organization instance.")
            throw new InvalidRequestError("${orgId} does not identify a valid Organization instance.")
        }

        List<Documents> policyDocs = Documents.findAllByOrganizationAndDocumentCategory(org, DocumentsController.TM_POLICY)

        log.debug("Rendering list...")
        withFormat {
            html {
                throw new ServletException("Not Yet Implemented")
            }
            xml {
                render policyDocs as XML
            }
            json {
                // render policy documents as JSON
                def resultsJSON = []
                policyDocs.each{ result ->
                    def resultJSON = [
                            id: result.id,
                            filename: result.filename ?: "",
                            url: result.url ?: "",
                            publicUrl: result.publicUrl ?: "",
                    ]
                    resultsJSON.add(resultJSON);

                }
                render resultsJSON as JSON
            }
        }

    }

    def listAvailableRelyingPartyAgreementDocuments() {
        Integer orgId = params.id as Integer
        log.debug("Attempting to load relying party agreements for organization: ${orgId}")

        Organization org = Organization.findById(orgId)

        if (!org) {
            log.warn("${orgId} does not identify a valid Organization instance.")
            throw new InvalidRequestError("${orgId} does not identify a valid Organization instance.")
        }

        List<Documents> relyingPartyAgreements = Documents.findAllByOrganizationAndDocumentCategory(org,
                DocumentsController.TM_RELYING_PARTY_AGREEMENT)

        log.debug("Rendering list...")
        withFormat {
            html {
                throw new ServletException("Not Yet Implemented")
            }
            xml {
                render relyingPartyAgreements as XML
            }
            json {
                // render relying party agreements as JSON
                def resultsJSON = []
                relyingPartyAgreements.each{ result ->
                    def resultJSON = [
                            id: result.id,
                            filename: result.filename ?: "",
                            url: result.url ?: "",
                            publicUrl: result.publicUrl ?: "",
                    ]
                    resultsJSON.add(resultJSON);

                }
                render resultsJSON as JSON
            }
        }

    }

    //==================================================================================================================
    //  Helper Methods
    //==================================================================================================================
    private TrustmarkMetadata fromIdParam(Object param){
        TrustmarkMetadata metadata = null;
        if( param && param.toString().matches("[0-9]+") ){
            metadata = TrustmarkMetadata.findById(Long.parseLong(param.toString()));
        }else if( param ){
            metadata = TrustmarkMetadata.findByName(param.toString());
        }else {
            throw new InvalidRequestError("Null TrustmarkMetadata identifier given.");
        }

        if( !metadata ){
            log.warn("No such TrustmarkMetadata ${param} found!");
            throw new InvalidRequestError("No such TrustmarkMetadata ${param} found!");
        }

        return metadata;
    }

}/* end TrustmarkMetadataController */

class EditTrustmarkMetadataCommand {
    private static final Logger log = LoggerFactory.getLogger(EditTrustmarkMetadataCommand.class);

    Long trustmarkMetadataId;
    String name;
    String description;
//    String generatorClass;
//    String identifierPattern;
    String policyUrl;
    String relyingPartyAgreementUrl;
//    String statusUrlPattern;
    Integer timePeriodNoExceptions;
    Integer timePeriodWithExceptions;
    Long organizationId;
    Integer defaultSigningCertificateId


    public void setData(TrustmarkMetadata metadata){
        this.trustmarkMetadataId = metadata.id;
        this.name = metadata.name;
        this.description = metadata.description;
//        this.generatorClass = metadata.generatorClass;
//        this.identifierPattern = metadata.identifierPattern;
//        this.identifierPattern = metadata.identifierPattern;
        this.policyUrl = metadata.policyUrl;
        this.relyingPartyAgreementUrl = metadata.relyingPartyAgreementUrl;
//        this.statusUrlPattern = metadata.statusUrlPattern;
        this.timePeriodNoExceptions = metadata.timePeriodNoExceptions;
        this.timePeriodWithExceptions = metadata.timePeriodWithExceptions;
        this.organizationId = metadata.provider.id;
        this.defaultSigningCertificateId = metadata.defaultSigningCertificateId
    }

    static constraints = {
        trustmarkMetadataId(nullable: false)
        name(nullable: false, blank: false, maxSize: 128, validator: {val, obj, errors ->
            // Make sure no existing org has this name
            log.debug("Checking to see if TrustmarkMetadata name[@|cyan ${val}|@] collides with another TrustmarkMetadata name...")
            TrustmarkMetadata original = TrustmarkMetadata.findById(obj.trustmarkMetadataId);
            TrustmarkMetadata fromDb = TrustmarkMetadata.findByName(val);
            if( fromDb != null && original.id != fromDb.id ) {
                log.warn("The user has given name ${val}, which already exists!");
                errors.rejectValue('name', 'nstic.web.EditTrustmarkMetadataCommand.name.exists', 'That name already exists in the system.')
            }

            // TODO Guarantee uniqueness in the system.
        })
        description(nullable: false, blank: false, maxSize: 65535)
//        generatorClass(nullable: false, blank: false, maxSize: 5096, validator: { val, obj, errors ->
//            log.debug("Validating the generator class[$val]...")
//            Class clz = null;
//            try{
//                clz = Class.forName(val);
//            }catch(Throwable t){
//                errors.rejectValue("generatorClass", "nstic.web.EditTrustmarkMetadataCommand.generatorClass.noClass", "The generator class could not be found in the code base.  Make sure the jar is loaded appropriately.")
//                return false;
//            }
//
//            Object instance = null;
//            try{
//                instance = clz.newInstance()
//            }catch(Throwable t){
//                errors.rejectValue("generatorClass", "nstic.web.EditTrustmarkMetadataCommand.generatorClass.cannotInstantiate", "The generator class could not be instantiated with a no-arg constructor.")
//                return false;
//            }
//
//            if( !(instance instanceof TrustmarkIdentifierGenerator) ){
//                errors.rejectValue("generatorClass", "nstic.web.EditTrustmarkMetadataCommand.generatorClass.notCorrect", "The generator class MUST extend nstic.TrustmarkIdentifierGenerator or it is invalid.")
//            }
//
//        })
//        identifierPattern(nullable: false, blank: false, maxSize: 5096)
        policyUrl(nullable: false, blank: false, maxSize: 5096, validator: { val, obj, errors ->
            URL url = null;
            try{
                url = new URL(val)
            }catch(Throwable t){
                errors.rejectValue("policyUrl", "nstic.web.EditTrustmarkMetadataCommand.policyUrl.invalidUrl", "Policy URL is not a valid URL.")
                return false;
            }

//            if( !CreateTrustmarkMetadataCommand.doesUrlExist(url) ){
//                errors.rejectValue("policyUrl", "nstic.web.EditTrustmarkMetadataCommand.policyUrl.notExists", "The Policy URL does not point to a valid internet location.")
//            }

        })
        relyingPartyAgreementUrl(nullable: false, blank: false, maxSize: 5096, validator: { val, obj, errors ->
            URL url = null;
            try{
                url = new URL(val)
            }catch(Throwable t){
                errors.rejectValue("relyingPartyAgreementUrl", "nstic.web.EditTrustmarkMetadataCommand.relyingPartyAgreementUrl.invalidUrl", "Relying Party Agreement URL is not a valid URL.")
                return false;
            }

        })
//        statusUrlPattern(nullable: false, blank: false, maxSize: 5096)
        timePeriodNoExceptions(nullable: false, validator: { val, obj, errors ->
            if( val < 1 ){
                errors.rejectValue("timePeriodNoExceptions", "nstic.web.EditTrustmarkMetadataCommand.timePeriodNoExceptions.lowerBound", "Duration no Exceptions must be greater than or equal to 1.")
                return false;
            }

            if( val > 120 ){
                errors.rejectValue("timePeriodNoExceptions", "nstic.web.EditTrustmarkMetadataCommand.timePeriodNoExceptions.upperBound", "Duration no Exceptions must be less than 120 months (ie 10 years).")
                return false;
            }

        })
        timePeriodWithExceptions(nullable: false, validator: { val, obj, errors ->
            if( val < 1 ){
                errors.rejectValue("timePeriodWithExceptions", "nstic.web.EditTrustmarkMetadataCommand.timePeriodWithExceptions.lowerBound", "Duration with Exceptions must be greater than or equal to 1.")
                return false;
            }

            if( val > 120 ){
                errors.rejectValue("timePeriodWithExceptions", "nstic.web.EditTrustmarkMetadataCommand.timePeriodWithExceptions.upperBound", "Duration with Exceptions must be less than 120 months (ie 10 years).")
                return false;
            }

            if( val > obj.timePeriodNoExceptions ){
                errors.rejectValue("timePeriodWithExceptions", "nstic.web.EditTrustmarkMetadataCommand.timePeriodWithExceptions.greaterThanTimePeriodNoExceptions", "Duration with Exceptions must be less than or equal to Duration no Exceptions.")
                return false;
            }

        })
        organizationId(nullable: false)
        defaultSigningCertificateId(nullable: false)
    }


}

class CreateTrustmarkMetadataCommand {
    private static final Logger log = LoggerFactory.getLogger(CreateTrustmarkMetadataCommand.class);

    String name;
    String description;
//    String generatorClass;
//    String identifierPattern;
    String policyUrl;
    String relyingPartyAgreementUrl;
//    String statusUrlPattern;
    Integer timePeriodNoExceptions;
    Integer timePeriodWithExceptions;
    Long organizationId;
    Long defaultSigningCertificateId

    private static boolean doesUrlExist(URL url) {
        try{
            HttpURLConnection.setFollowRedirects(false);
            // note : you may also need HttpURLConnection.setInstanceFollowRedirects(false)
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("HEAD");
            return (con.getResponseCode() == HttpURLConnection.HTTP_OK);
        }catch(Throwable t){
            log.error("Error contacting url: "+url, t);
            return false;
        }
    }

    static constraints = {
        name(nullable: false, blank: false, maxSize: 128, validator: {val, obj, errors ->
            // Make sure no existing org has this name
            log.debug("Checking to see if ${val} collides with another TrustmarkMetadata name...")
            TrustmarkMetadata fromDb = TrustmarkMetadata.findByName(val);
            if( fromDb != null ) {
                log.warn("The user has given name ${val}, which already exists!");
                errors.rejectValue('name', 'nstic.web.CreateTrustmarkMetadataCommand.name.exists', 'That name already exists in the system.')
            }

            // TODO Guarantee uniqueness in the system.
        })
        description(nullable: false, blank: false, maxSize: 65535)
//        generatorClass(nullable: false, blank: false, maxSize: 5096, validator: { val, obj, errors ->
//            Class clz = null;
//            try{
//                clz = Class.forName(val);
//            }catch(Throwable t){
//                errors.rejectValue("generatorClass", "nstic.web.CreateTrustmarkMetadataCommand.generatorClass.noClass", "The generator class could not be found in the code base.  Make sure the jar is loaded appropriately.")
//                return false;
//            }
//
//            Object instance = null;
//            try{
//                instance = clz.newInstance()
//            }catch(Throwable t){
//                errors.rejectValue("generatorClass", "nstic.web.CreateTrustmarkMetadataCommand.generatorClass.cannotInstantiate", "The generator class could not be instantiated with a no-arg constructor.")
//                return false;
//            }
//
//            if( !(instance instanceof TrustmarkIdentifierGenerator) ){
//                errors.rejectValue("generatorClass", "nstic.web.CreateTrustmarkMetadataCommand.generatorClass.notCorrect", "The generator class MUST extend nstic.TrustmarkIdentifierGenerator or it is invalid.")
//            }
//
//        })
//        identifierPattern(nullable: false, blank: false, maxSize: 5096)
        policyUrl(nullable: false, blank: false, maxSize: 5096, validator: { val, obj, errors ->
            URL url = null;
            try{
                url = new URL(val)
            }catch(Throwable t){
                errors.rejectValue("policyUrl", "nstic.web.CreateTrustmarkMetadataCommand.policyUrl.invalidUrl", "Policy URL is not a valid URL.")
                return false;
            }

//            if( !CreateTrustmarkMetadataCommand.doesUrlExist(url) ){
//                errors.rejectValue("policyUrl", "nstic.web.CreateTrustmarkMetadataCommand.policyUrl.notExists", "The Policy URL does not point to a valid internet location.")
//            }

        })
        relyingPartyAgreementUrl(nullable: false, blank: false, maxSize: 5096, validator: { val, obj, errors ->
            URL url = null;
            try{
                url = new URL(val)
            }catch(Throwable t){
                errors.rejectValue("relyingPartyAgreementUrl", "nstic.web.CreateTrustmarkMetadataCommand.relyingPartyAgreementUrl.invalidUrl", "Relying Party Agreement URL is not a valid URL.")
                return false;
            }

        })
//        statusUrlPattern(nullable: false, blank: false, maxSize: 5096)
        timePeriodNoExceptions(nullable: false, validator: { val, obj, errors ->
            if( val < 1 ){
                errors.rejectValue("timePeriodNoExceptions", "nstic.web.CreateTrustmarkMetadataCommand.timePeriodNoExceptions.lowerBound", "Duration no Exceptions must be greater than or equal to 1.")
                return false;
            }

            if( val > 120 ){
                errors.rejectValue("timePeriodNoExceptions", "nstic.web.CreateTrustmarkMetadataCommand.timePeriodNoExceptions.upperBound", "Duration no Exceptions must be less than 120 months (ie 10 years).")
                return false;
            }

        })
        timePeriodWithExceptions(nullable: false, validator: { val, obj, errors ->
            if( val < 1 ){
                errors.rejectValue("timePeriodWithExceptions", "nstic.web.CreateTrustmarkMetadataCommand.timePeriodWithExceptions.lowerBound", "Duration with Exceptions must be greater than or equal to 1.")
                return false;
            }

            if( val > 120 ){
                errors.rejectValue("timePeriodWithExceptions", "nstic.web.CreateTrustmarkMetadataCommand.timePeriodWithExceptions.upperBound", "Duration with Exceptions must be less than 120 months (ie 10 years).")
                return false;
            }

            if( val > obj.timePeriodNoExceptions ){
                errors.rejectValue("timePeriodWithExceptions", "nstic.web.CreateTrustmarkMetadataCommand.timePeriodWithExceptions.greaterThanTimePeriodNoExceptions", "Duration with Exceptions must be less than or equal to Duration no Exceptions.")
                return false;
            }

        })
        organizationId(nullable: false)
        defaultSigningCertificateId(nullable: false)
    }


}

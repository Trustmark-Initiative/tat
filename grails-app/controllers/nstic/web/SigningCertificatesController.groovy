package nstic.web

import edu.gatech.gtri.trustmark.v1_0.impl.util.TrustmarkMailClientImpl
import nstic.TATPropertiesHolder
import nstic.util.AssessmentToolProperties
import nstic.web.SigningCertificateStatus

import assessment.tool.X509CertificateService
import grails.converters.JSON
import grails.converters.XML
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.annotation.Secured
import nstic.web.assessment.Trustmark
import nstic.web.assessment.TrustmarkStatus
import nstic.web.td.TrustmarkDefinition
import org.apache.commons.lang.StringUtils
import org.dom4j.DocumentException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.ldap.support.LdapNameBuilder
import org.springframework.validation.ObjectError
import sun.security.x509.X500Name

import javax.naming.Name
import javax.servlet.ServletException
import org.grails.web.util.WebUtils

import javax.xml.bind.DatatypeConverter
import java.nio.charset.StandardCharsets
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.cert.Certificate
import java.security.cert.X509Certificate
import java.security.cert.CertificateFactory
import java.security.interfaces.RSAPublicKey
import java.time.Period
import java.time.ZoneId

@Transactional
@Secured(["ROLE_USER", "ROLE_ADMIN"])
class SigningCertificatesController {

    // certificate valid period in years
    private static List<Integer> CERTIFICATE_VALID_PERIOD_INTERVALS = [5, 10]

    // key length
    private static List<Integer> KEY_LENGTH = [2048, 4096]

    def springSecurityService;

    def index() {
        redirect(action:'list')
    }

    def list(){
        log.debug("Listing certificates...")
        if (!params.max)
            params.max = '20';
        params.max = Math.min(100, Integer.parseInt(params.max)).toString(); // Limit to at most 100 docs at a time.
        [signingCertificates: SigningCertificate.list(params), signingCertificatesCountTotal: SigningCertificate.count()]
    }

    def add() {

        // params organization id to get certificates, pass organization
        Organization org = new Organization()
        if( params.orgId ) {
            log.debug("Finding organization with id[${params.orgId}]...")
            org = Organization.get(params.orgId)
            if (!org) {
                log.warn("User[$user] has requested to generate a signing certificate for org[${params.orgId}] which cannot be found!")
                throw new ServletException("Unable to find any Organization with id=${params.orgId}")
            }
        }

        GenerateSigningCertificateCommand generateSigningCertificateCommand = new GenerateSigningCertificateCommand()

        // remove protocol from URL
        URI uri = new URI(org.uri)
        generateSigningCertificateCommand.commonName = uri.getHost()

        generateSigningCertificateCommand.countryName = "US"
        generateSigningCertificateCommand.emailAddress = org.primaryContact.email
        generateSigningCertificateCommand.organizationName = org.name
        generateSigningCertificateCommand.organizationalUnitName = ""
        generateSigningCertificateCommand.orgId = org.id

        if (params.containsKey("certId")) {
            SigningCertificate cert = SigningCertificate.findById(params.certId)

            if (cert) {
                generateSigningCertificateCommand.commonName = cert.commonName

                generateSigningCertificateCommand.countryName = cert.countryName
                generateSigningCertificateCommand.emailAddress = cert.emailAddress
                generateSigningCertificateCommand.organizationName = cert.organizationName
                generateSigningCertificateCommand.organizationalUnitName = cert.organizationalUnitName
                generateSigningCertificateCommand.orgId = cert.organization.id

                generateSigningCertificateCommand.organizationalUnitName = cert.organizationalUnitName
                generateSigningCertificateCommand.localityName = cert.localityName
                generateSigningCertificateCommand.stateOrProvinceName = cert.stateOrProvinceName

                generateSigningCertificateCommand.validPeriod = cert.validPeriod
                generateSigningCertificateCommand.keyLength = cert.keyLength

                generateSigningCertificateCommand.expirationDate = cert.expirationDate
            }
        }

        [
            generateSigningCertificateCommand: generateSigningCertificateCommand,
            certificateValidPeriodIntervalList: CERTIFICATE_VALID_PERIOD_INTERVALS,
            keyLengthList: KEY_LENGTH,
            orgId: org.id
        ]
    }

    def view() {
        log.info("Viewing certificate: [${params.id}]...")

        // SigningCertificate domain object
        SigningCertificate cert = SigningCertificate.findById(params.id)
        if( !cert ) {
            log.info("cert == null...")
            throw new ServletException("No such certificate: ${params.id}")
        }

        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509")

        // convert pem to certificate
        ByteArrayInputStream inStream = new ByteArrayInputStream(cert.x509CertificatePem.getBytes())
        X509Certificate x509Cert = (X509Certificate)certificateFactory.generateCertificate(inStream)

        if( !x509Cert ) {
            log.info("cert == null...")
            throw new ServletException("Could not create an X509 certificate from the database: ${params.id}")
        }

        String version = x509Cert.version.toString()
        String serialNumber = x509Cert.serialNumber.toString()
        String signatureAlgorithm = x509Cert.sigAlgName
        String issuer = x509Cert.issuerDN.name

        // Validity
        String notBefore = x509Cert.notBefore.toString()
        String notAfter = x509Cert.notAfter.toString()

        String subject = x509Cert.subjectDN.toString()

        // subject public Key info
        String publicKeyAlgorithm = x509Cert.publicKey.algorithm

        // certificate's key usage
        boolean[] keyUsage = x509Cert.getKeyUsage()

        ArrayList<String> certKeyUsageList = new ArrayList<String>()

        if (keyUsage) {
            for(int i = 0; i < keyUsage.size(); i++){
                if (keyUsage[i]) {
                    certKeyUsageList.add(X509CertificateService.KeyUsageStringList[i])
                }
            }
        }

        String keyUsageString = new String()

        for(int i = 0; i < certKeyUsageList.size(); i++){
            keyUsageString = keyUsageString.concat(certKeyUsageList[i])

            if (i < certKeyUsageList.size() - 1) {
                keyUsageString = keyUsageString.concat(", ")
            }
        }

        log.debug("Certificate thumbprint:");
        log.debug("$cert.thumbPrintWithColons");

        log.debug("Rendering signing certificate view [id=${cert.id}] " +
                "page for certificate #${cert.distinguishedName})...)");

        Integer numberOfTrustmarksAffected = numberOfTrustmarksAssociatedWithCertificate(cert.id)
        Integer numberOfTrustMarkMetadataSetsAffected = numberOfTrustmarkMetadataSetsAssociatedWithCertificate(cert.id)

        withFormat {
            html {
                [certId: cert.id, cert: cert, version: version, serialNumber: serialNumber,
                 signatureAlgorithm: signatureAlgorithm, issuer: issuer,
                 notBefore: notBefore, notAfter: notAfter, subject: subject,
                 publicKeyAlgorithm: publicKeyAlgorithm, keyUsageString: keyUsageString,
                 numberOfTrustmarksAffected: numberOfTrustmarksAffected,
                 numberOfTrustMarkMetadataSetsAffected: numberOfTrustMarkMetadataSetsAffected]
            }
        }
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

        SigningCertificate certificate = SigningCertificate.findById(params.id)
        if( !certificate )
            throw new ServletException("Unknown certificate: ${params.id}")

        certificate.status = SigningCertificateStatus.REVOKED
        certificate.revokedReason = params.reason
        certificate.revokingUser = user
        certificate.revokedTimestamp = Calendar.getInstance().getTime()
        certificate.save(failOnError: true, flush: true)

        def responseData = [status: "SUCCESS", message: "Successfully revoked certificate ${certificate.id}",
                            certificate: [id: certificate.id, distinguishedName: certificate.distinguishedName,
                                          status: certificate.status.toString()]]
        withFormat {
            html {
                flash.message = "Successfully revoked certificate"
                return redirect(controller:'signingCertificates', action:'view', id: certificate.id)
            }
            xml {
                render responseData as XML
            }
            json {
                render responseData as JSON
            }
        }
    }

    def generateCertificate(GenerateSigningCertificateCommand cmd) {

        User user = springSecurityService.currentUser;
        if( !cmd.validate() ){
            log.warn "Generate Certificate form does not validate: "
            cmd.errors.getAllErrors().each { ObjectError error ->
                log.warn "    ${error.defaultMessage}, Args: ${error.arguments}"
            }
            return render(view: 'add', model: [generateSigningCertificateCommand: cmd])
        }

        SigningCertificate signingCertificate = createNewSigningCertificate(cmd)

        signingCertificate.save(failOnError: true, flush: true)

        flash.message = "Successfully generated signing certificate " +
                "'${signingCertificate.distinguishedName}' for organization ${cmd.organizationName}"

        return redirect(controller: 'organization', action:'view', id: cmd.orgId)
    }

    def generateNewCertificateFromExpiredOrRevokedCertificate() {
        log.info("Generating a new certificate from expired/revoked certificate: [${params.id}]...")

        Integer id = Integer.parseInt(params.id)
        SigningCertificate newCert = generateNewCertificateFromExistingCertificate(id)

        def model = [newCert: newCert]
        render model as JSON
    }

    def generateNewCertificateAndUpdateTrustmarkMetadataSets() {

        Integer id = Integer.parseInt(params.id)

        log.info("Generating a new certificate and updating metadata from expired/revoked certificate: [${params.id}]...")

        // First, clone the expired/revoked certificate
        SigningCertificate newCert = generateNewCertificateFromExistingCertificate(id)

        updtateTrustmarkMetadataSets(id, newCert.id)

        def model = [newCert: newCert]
        render model as JSON
    }

    def generateNewCertificateAndUpdateTrustmarkMetadataSetsAndReissueTrustmarks() {

        Integer id = Integer.parseInt(params.id)

        log.info("Generating a new certificate, updating metadata, and reissuing trustmarks from expired/revoked certificate: [${params.id}]...")

        // First, clone the expired/revoked certificate
        SigningCertificate newCert = generateNewCertificateFromExistingCertificate(id)

        updtateTrustmarkMetadataSets(id, newCert.id)

        return redirect(controller: 'trustmark', action: 'reissueTrustmarks', params: [originalCertId: id, newCertId: newCert.id])
    }

    def getCertificateDependencies() {
        Integer id = Integer.parseInt(params.id)

        Integer numberOfTrustmarksAffected = numberOfTrustmarksAssociatedWithCertificate(id)
        Integer numberOfMetadataSets = numberOfTrustmarkMetadataSetsAssociatedWithCertificate(id)

        def model = [numberOfTrustmarks: numberOfTrustmarksAffected,
                     numberOfMetadataSets: numberOfMetadataSets]
        render model as JSON
    }

    def getCertificateTrustmarkDependencies() {
        Integer id = Integer.parseInt(params.id)

        Integer numberOfTrustmarksAffected = numberOfTrustmarksAssociatedWithCertificate(id)

        def model = [numberOfTrustmarks: numberOfTrustmarksAffected]
        render model as JSON
    }

    def getCertificateMetadataDependency() {
        Integer id = Integer.parseInt(params.id)

        Integer numberOfMetadataSets = numberOfTrustmarkMetadataSetsAssociatedWithCertificate(id)

        def model = [numberOfMetadataSets: numberOfMetadataSets]
        render model as JSON
    }

    def getActiveCertificates() {
        Integer orgId = Integer.parseInt(params.id)
        Organization org = Organization.findById(orgId)

        def activeCerts = []

        org.certificates.each { nstic.web.SigningCertificate cert ->
            if (cert.status == nstic.web.SigningCertificateStatus.ACTIVE) {
                Console.println("Adding certificate: " + cert.distinguishedName + " - " + cert.serialNumber);
                activeCerts.add(cert);
            }
        }

        def model = [activeCertificates: activeCerts]
        render model as JSON
    }

    def getActiveMetadataSets() {
        Integer orgId = Integer.parseInt(params.id)
        Organization org = Organization.findById(orgId)

        List<nstic.web.TrustmarkMetadata> validMetadata = [];
        List<nstic.web.TrustmarkMetadata> metadata = nstic.web.TrustmarkMetadata.findAllByProvider(org);

        metadata.each { nstic.web.TrustmarkMetadata md ->
            nstic.web.SigningCertificate signingCertificate = nstic.web.SigningCertificate.findById(md.defaultSigningCertificateId)
            if (signingCertificate.status == nstic.web.SigningCertificateStatus.ACTIVE) {
                validMetadata.add(md)
            }
        }

        def model = [activeMetadataSets: validMetadata]
        render model as JSON
    }

    def updateTrustmarkMetadataSet() {
        int existingCertificateId = Integer.parseInt(params.id)
        int newCertificateId = Integer.parseInt(params.newCertId)

        log.info("Updating metadata sets from expired/revoked certificate: [${params.id}] to new certificate: [${params.newCertId}]...")

        updtateTrustmarkMetadataSets(existingCertificateId, newCertificateId)

        Integer numberOfMetadataSets = numberOfTrustmarkMetadataSetsAssociatedWithCertificate(existingCertificateId)

        def model = [numberOfMetadataSets: numberOfMetadataSets]
        render model as JSON
    }

    def reissueTrustmarksFromMetadataSet() {
        int existingCertificateId = Integer.parseInt(params.id)
        int selectedMetadataId = Integer.parseInt(params.selectedMetadataId)

        log.info("Reissuing trustmarks from certificate: [${params.id}] and metadata sets: [${params.selectedMetadataId}]...")

        return redirect(controller: 'trustmark', action: 'reissueTrustmarksFromMetadataSet',
                params: [originalCertId: existingCertificateId, metadataSetId: selectedMetadataId])
    }

    def importPkcs12() {

        // params organization id to get certificates, pass organization
        Organization org = new Organization()
        if( params.orgId ) {
            log.debug("Finding organization with id[${params.orgId}]...")
            org = Organization.get(params.orgId)
            if (!org) {
                log.warn("User[$user] has requested to generate a signing certificate for org[${params.orgId}] which cannot be found!")
                throw new ServletException("Unable to find any Organization with id=${params.orgId}")
            }
        }

        [
            organization: org
        ]
    }

    def importPkcs12File() {
        User user = springSecurityService.currentUser;

        log.info("importPkcs12File")

        if( org.apache.commons.lang.StringUtils.isBlank(params.organizationId) ){
            log.warn "Missing required param organizationId!"
            throw new ServletException("Missing required organizationId parameter.")
        }

        Organization org = Organization.get(params.organizationId)

        if( params.filename == null) {
            log.warn "Missing required param filename!"
            throw new ServletException("Missing required filename parameter.")
        }

        Map results = [:]

        // status message
        Map messageMap = [:]

        SigningCertificate signingCertificate = null;

        try {
            if (params.filename != null) {

                log.info("Filename: ${params.filename.originalFilename}")

                String password = params.password[0]

                Boolean defaultCertificate = Boolean.parseBoolean(params.defaultCertificate[1])

                FileInputStream stream = params.filename.getInputStream()

                // create keystore
                KeyStore keystore = KeyStore.getInstance("PKCS12");
                keystore.load(null, null);
                keystore.load(stream, password.toCharArray())

                // process aliases
                Enumeration<String> aliases = keystore.aliases()
                Integer numberOfAliases = Collections.list(aliases).size()

                if (numberOfAliases > 1) {
                    log.info("The PKCS12 file contains more than one signing certificate when only one was expected.")
                    messageMap["WARNING"] = "The PKCS12 file ${params.filename.originalFilename}, contains more than one signing certificate when only one is allowed."
                } else {
                    // reset the enumaration back to the beginning
                    aliases = keystore.aliases()

                    while(aliases.hasMoreElements()) {
                        String alias = aliases.nextElement()

                        // get private key and public certificate
                        X509Certificate cert = (X509Certificate) keystore.getCertificate(alias);
                        PrivateKey privateKeyFromPkcs12 = (PrivateKey) keystore.getKey(alias, password.toCharArray());

                        X509CertificateService x509CertificateService = new X509CertificateService()

                        signingCertificate = createSigningCertificate(cert, privateKeyFromPkcs12, org, defaultCertificate)

                        SigningCertificate.withTransaction {
                            signingCertificate.save(failOnError: true, flush: true)
                        }
                    }
                }
            }

        } catch (Exception e) {
            log.info("Error parsing ${params.filename.originalFilename}, error: ${e.message}")
            messageMap["ERROR"] = "Error opening ${params.filename.originalFilename}, error: ${e.message}"
        }

        if (signingCertificate) {
            flash.message = "Successfully imported signing certificate " +
                    "'${signingCertificate.distinguishedName}' for organization ${org.name}"
            messageMap["SUCCESS"] = "SUCCESS"
        }

        results.put("messageMap", messageMap)

        withFormat  {
            json {
                render results as JSON
            }
        }
    }

    private SigningCertificate createSigningCertificate(X509Certificate certificate, PrivateKey privateKey,
                                      Organization org, boolean defaultCertificate) {
        X509CertificateService x509CertificateService = new X509CertificateService()

        SigningCertificate signingCertificate = new SigningCertificate()
        signingCertificate.distinguishedName = certificate.subjectDN.name

        X500Name x500Name = new X500Name(certificate.getSubjectX500Principal().getName());
        signingCertificate.commonName = x500Name.getCommonName();
        signingCertificate.localityName = x500Name.locality
        signingCertificate.stateOrProvinceName = x500Name.state
        signingCertificate.countryName = x500Name.country
        signingCertificate.emailAddress = org.primaryContact.email
        signingCertificate.organizationName = x500Name.organization
        signingCertificate.organizationalUnitName = x500Name.organizationalUnit
        signingCertificate.serialNumber = certificate.serialNumber.toString()
        signingCertificate.thumbPrint = x509CertificateService.getThumbPrint(certificate)
        signingCertificate.thumbPrintWithColons = x509CertificateService.getThumbPrintWithColons(certificate)
        signingCertificate.privateKeyPem = x509CertificateService.convertToPemPrivateKey(privateKey)
        signingCertificate.x509CertificatePem = x509CertificateService.convertToPem(certificate)
        signingCertificate.status = SigningCertificateStatus.ACTIVE

        signingCertificate.validPeriod = diffInYears(certificate.notBefore, certificate.notAfter);

        RSAPublicKey rsaPk = (RSAPublicKey) certificate.getPublicKey();
        signingCertificate.keyLength = rsaPk.getModulus().bitLength()

        X509CertificateService certService = new X509CertificateService()
        X509Certificate x509Certificate = certService.convertFromPem(signingCertificate.x509CertificatePem)
        signingCertificate.expirationDate = x509Certificate.notAfter

        signingCertificate.organization = org

        // URL: create a unique filename to create the downloadable file
        // filename: commonName-thumbprint.pem
        String filename = signingCertificate.commonName + "-" + signingCertificate.thumbPrint + ".pem"
        signingCertificate.filename = filename

        // get the base url from the http request and append the controller
        String baseUrl = getBaseAppUrl() + "/PublicCertificates/download/?id="

        signingCertificate.certificatePublicUrl = baseUrl + filename

        // override the default certificate flag and set it to true if
        // this is the first certificate for the organization
        if (SigningCertificate.findAllByOrganization(org).size() == 0) {
            signingCertificate.defaultCertificate = true
        } else {
            signingCertificate.defaultCertificate = defaultCertificate // add flag to UI for this
        }

        // TODO: Only change the certificates for the organization!!!

        // reset default certificate flag in db
        if (defaultCertificate) {
            org.certificates.toList().each { cert ->
                if (cert.defaultCertificate) {
                    cert.defaultCertificate = false
                    cert.save(failOnError: true, flush: true)
                }
            }
        }

        return signingCertificate;
    }

    private Integer diffInYears(Date from, Date to) {

        Period period = Period.between(from.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
                to.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());

        Integer monthsToYear = period.getMonths() > 5 ? 1 : 0;
        Integer years = period.getYears() + monthsToYear;

        return years
    }

    private int numberOfTrustmarksAssociatedWithCertificate(int certificateId) {
        Integer numberOfTrustmarksAffected = 0
        Trustmark.findAllBySigningCertificateId(certificateId).forEach { trustmark ->
            if (trustmark.status == TrustmarkStatus.ACTIVE || trustmark.status == TrustmarkStatus.OK) {
                numberOfTrustmarksAffected++
            }
        }

        return numberOfTrustmarksAffected
    }

    private int numberOfTrustmarkMetadataSetsAssociatedWithCertificate(int certificateId) {

        TrustmarkMetadata.findAllByDefaultSigningCertificateId(certificateId).size()
    }

    private SigningCertificate generateNewCertificateFromExistingCertificate(int existingCertificateId) {

        // SigningCertificate domain object
        SigningCertificate oldCert = SigningCertificate.findById(existingCertificateId)
        if( !oldCert ) {
            log.info("oldCert == null...")
            throw new ServletException("No such certificate: ${existingCertificateId}")
        }

        GenerateSigningCertificateCommand cmd = new GenerateSigningCertificateCommand()
        cmd.setData(oldCert)

        SigningCertificate newCert = createNewSigningCertificate(cmd)

        newCert.save(failOnError: true, flush: true)

        return newCert
    }

    private SigningCertificate createNewSigningCertificate(GenerateSigningCertificateCommand cmd) {
        Organization org = Organization.findById(cmd.orgId)

        if( !org ) {
            log.info("There is no organization with ordId: ${cmd.orgId}}")
            throw new ServletException("There is no organization with ordId: ${cmd.orgId}")
        }

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA")

        keyPairGenerator.initialize(cmd.keyLength)
        KeyPair keyPair = keyPairGenerator.generateKeyPair()

        // distinguished name
        X500Name x500Name = null
        if (StringUtils.isNotEmpty(cmd.distinguishedName)) {
            x500Name = new X500Name(cmd.distinguishedName)
        } else {
            // Create a distinguished name from the command
            String distinguishedName = signingCertificateCommandToDistinguishedName(cmd)

            x500Name = new X500Name(distinguishedName)
        }

        String distinguishedName = x500Name.getName()

        X509CertificateService x509CertificateService = new X509CertificateService()

        Certificate certificate = x509CertificateService.generateCertificate(distinguishedName,
                keyPair, cmd.validPeriod * 365, "SHA256withRSA")

        SigningCertificate signingCertificate = createSigningCertificate(certificate, keyPair.private, org, cmd.defaultCertificate)

        return signingCertificate
    }

    // Combine the non-empty relative distinguished names (RDNs) into a distinguished name, excluding email
    private String signingCertificateCommandToDistinguishedName(GenerateSigningCertificateCommand cmd) {
        LdapNameBuilder builder = LdapNameBuilder.newInstance()

        if (StringUtils.isNotEmpty(cmd.organizationalUnitName)) {
            builder.add("OU", cmd.organizationalUnitName)
        }
        if (StringUtils.isNotEmpty(cmd.organizationName)) {
            builder.add("O", cmd.organizationName)
        }
        if (StringUtils.isNotEmpty(cmd.localityName)) {
            builder.add("L", cmd.localityName)
        }
        if (StringUtils.isNotEmpty(cmd.stateOrProvinceName)) {
            builder.add("S", cmd.stateOrProvinceName)
        }
        if (StringUtils.isNotEmpty(cmd.countryName)) {
            builder.add("C", cmd.countryName)
        }
        if (StringUtils.isNotEmpty(cmd.commonName)) {
            builder.add("CN", cmd.commonName)
        }

        Name dn = builder.build()

        return dn.toString()
    }

    private void updtateTrustmarkMetadataSets(int existingCertificateId, int newCertificateId) {

        // get a collection of trustmark metadata sets that use the expired/revoked certificate
        def trustmarkMetadataSets = TrustmarkMetadata.findAllByDefaultSigningCertificateId(existingCertificateId)

        trustmarkMetadataSets.each{ TrustmarkMetadata trustmarkMetadata ->
            trustmarkMetadata.defaultSigningCertificateId = newCertificateId

            trustmarkMetadata.save(failOnError: true, flush: true)
        }
    }

    private String getBaseAppUrl() {
        return AssessmentToolProperties.getProperties().getProperty(AssessmentToolProperties.BASE_URL)
    }

    def download() {

        if( StringUtils.isBlank(params.id) ){
            log.warn "Missing required parameter id"
            throw new ServletException("Missing required parameter: 'id")
        }

        X509CertificateService x509CertificateService = new X509CertificateService()

        SigningCertificate certificate = SigningCertificate.findById(params.id)

        String organizationName = certificate.organizationName.replaceAll("[^A-Za-z0-9]", "")
        String organizationalUnitName = certificate.organizationalUnitName.replaceAll("[^A-Za-z0-9]", "")

        String filename = certificate.filename

        response.setHeader("Content-length", certificate.x509CertificatePem.length().toString())

        String mimeType = "text/html"

        response.setContentType( "application-xdownload")
        response.setHeader("Content-Disposition", "attachment;filename=${filename}")
        response.getOutputStream() << new ByteArrayInputStream(certificate.x509CertificatePem.getBytes())
    }
}

class GenerateSigningCertificateCommand {
    static Logger log = LoggerFactory.getLogger(GenerateSigningCertificateCommand.class);

    public void setData(SigningCertificate cert) {

        this.distinguishedName = cert.distinguishedName
        this.commonName = cert.commonName
        this.localityName = cert.localityName
        this.stateOrProvinceName = cert.stateOrProvinceName
        this.countryName = cert.countryName
        this.emailAddress = cert.emailAddress
        this.organizationName = cert.organizationName
        this.organizationalUnitName = cert.organizationalUnitName
        this.defaultCertificate = cert.defaultCertificate
        this.validPeriod = cert.validPeriod
        this.keyLength = cert.keyLength
        this.expirationDate = cert.expirationDate
        this.orgId = cert.organization.id
    }

    String distinguishedName
    String commonName
    String localityName
    String stateOrProvinceName
    String countryName
    String emailAddress
    String organizationName
    String organizationalUnitName
    Boolean defaultCertificate = Boolean.FALSE
    Integer validPeriod
    Integer keyLength
    Date expirationDate
    Integer orgId

    static constraints = {
        distinguishedName(nullable: true, blank: true, maxSize: 255)
        commonName(nullable: false, blank: false, maxSize: 255)
        localityName(nullable: true, blank: true, maxSize: 255)
        stateOrProvinceName(nullable: true, blank: false, maxSize: 255)
        countryName(nullable: true, blank: false, maxSize: 255)
        emailAddress(nullable: false, blank: false, maxSize: 255)
        organizationName(nullable: true, blank: false, maxSize: 255)
        organizationalUnitName(nullable: true, blank: true, maxSize: 255)
        defaultCertificate(null: false)
        orgId(nullable: true, validator: {val, obj, errors ->
            log.debug("Generate signing certificate origanization ID validation...")
            if( val ){
                log.debug("  Org id has a value: ${val}")
                Organization.withTransaction {
                    Organization org = Organization.get(val);
                    if( !org ){
                        log.warn("No such org: $val");
                        return "org.does.not.exist"
                    }else{
                        log.debug("Org ${val} exists, validating URI...");
                        Organization uriConflictOrg = Organization.findByName(obj.organizationName);
                        if( uriConflictOrg ) {
                            log.debug("For URN ${obj.organizationName}, found org: ${uriConflictOrg.name}")
                            if (uriConflictOrg && uriConflictOrg.id != val) {
                                errors.rejectValue("organizationName", "org.name.exists", [obj.organizationName] as String[], "Organizaiton Name ${obj.organizationName} already exists.")
                                return false;
                            } else {
                                log.debug("A urn conflict was not detected.")
                            }
                        }else{
                            log.debug("No Org with Name[${obj.organizationName}] exists in database to conflict.")
                        }

                        Organization nameConflictOrg = Organization.findByName(obj.organizationName);
                        if( nameConflictOrg ) {
                            log.debug("For name ${obj.organizationName}, found org: ${nameConflictOrg.uri}")
                            if (nameConflictOrg && nameConflictOrg.id != val) {
                                errors.rejectValue("organizationName", "org.name.exists", [obj.organizationName] as String[], "Organizaiton name ${obj.organizationName} already exists.")
                                return false;
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
        expirationDate(nullable: true)
    }
}
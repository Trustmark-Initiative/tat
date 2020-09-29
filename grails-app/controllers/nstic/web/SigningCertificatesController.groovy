package nstic.web

import assessment.tool.X509CertificateService
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.annotation.Secured
import org.apache.commons.lang.StringUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.validation.ObjectError
import sun.security.x509.X500Name

import javax.servlet.ServletException
import org.grails.web.util.WebUtils

import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.cert.Certificate
import java.security.cert.X509Certificate
import java.security.cert.CertificateFactory

@Transactional
@Secured("ROLE_USER")
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
        withFormat {
            html {
                [certId: cert.id, cert: cert, version: version, serialNumber: serialNumber,
                 signatureAlgorithm: signatureAlgorithm, issuer: issuer,
                 notBefore: notBefore, notAfter: notAfter, subject: subject,
                 publicKeyAlgorithm: publicKeyAlgorithm, keyUsageString: keyUsageString]
            }
        }
    }

    // deferred functionality
//    def revoke() {
//        log.info("Revoking certificate: [${params.id}]...")
//
//        // SigningCertificate domain object
//        SigningCertificate cert = SigningCertificate.findById(params.id)
//        if( !cert ) {
//            log.info("cert == null...")
//            throw new ServletException("No such certificate: ${params.id}")
//        }
//
//        cert.revoked = true
//
//        //cert.defaultCertificate = false
//
//        cert.save(failOnError: true, flush: true)
//
//        return redirect(controller: 'organization', action:'view', id: cert.organization.id)
//    }

    def generateCertificate(GenerateSigningCertificateCommand cmd) {

        User user = springSecurityService.currentUser;
        if( !cmd.validate() ){
            log.warn "Generate Certificate form does not validate: "
            cmd.errors.getAllErrors().each { ObjectError error ->
                log.warn "    ${error.defaultMessage}, Args: ${error.arguments}"
            }
            return render(view: 'add', model: [generateSigningCertificateCommand: cmd])
        }

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA")

        keyPairGenerator.initialize(cmd.keyLength)
        KeyPair keyPair = keyPairGenerator.generateKeyPair()

        // distinguished name
        X500Name x500Name = new X500Name(cmd.commonName, cmd.organizationalUnitName, cmd.organizationName,
                cmd.localityName, cmd.stateOrProvinceName, cmd.countryName)

        String distinguishedName = x500Name.getName()

        X509CertificateService x509CertificateService = new X509CertificateService()

        Certificate certificate = x509CertificateService.generateCertificate(distinguishedName,
                keyPair, cmd.validPeriod * 365, "SHA256withRSA")

        // public cert string
        String pemCert = x509CertificateService.convertToPem(certificate)

        // private key
        String pemKey = x509CertificateService.convertToPemPrivateKey(keyPair.getPrivate())

        // raw thumbprint
        String thumbprint = x509CertificateService.getThumbPrint(certificate)

        // viewable thumbprint
        String thumbprintWithColons = x509CertificateService.getThumbPrintWithColons(certificate)

        SigningCertificate signingCertificate = new SigningCertificate()
        signingCertificate.distinguishedName = certificate.subjectDN.name
        signingCertificate.commonName = cmd.commonName
        signingCertificate.localityName = cmd.localityName
        signingCertificate.stateOrProvinceName = cmd.stateOrProvinceName
        signingCertificate.countryName = cmd.countryName
        signingCertificate.emailAddress = cmd.emailAddress
        signingCertificate.organizationName = cmd.organizationName
        signingCertificate.organizationalUnitName = cmd.organizationalUnitName
        signingCertificate.serialNumber = certificate.serialNumber.toString()
        signingCertificate.thumbPrint = thumbprint
        signingCertificate.thumbPrintWithColons = thumbprintWithColons
        signingCertificate.privateKeyPem = pemKey
        signingCertificate.x509CertificatePem = pemCert

        Organization org = Organization.findById(cmd.orgId)
        signingCertificate.organization = org

        // URL: create a unique filename to create the downloadable file
        // filename: commonName-thumbprint.pem
        String filename = cmd.commonName + "-" + thumbprint + ".pem"
        signingCertificate.filename = filename

        // get the base url from the http request and append the controller
        String baseUrl = getBaseAppUrl() + "/PublicCertificates/download/?id="

        signingCertificate.certificatePublicUrl = baseUrl + filename

        // override the default certificate flag and set it to true if
        // this is the first certificate for the organization
        if (SigningCertificate.findAllByOrganization(org).size() == 0) {
            signingCertificate.defaultCertificate = true
        } else {
            signingCertificate.defaultCertificate = cmd.defaultCertificate
        }

        // reset default certificate flag in db
        if (cmd.defaultCertificate) {
            SigningCertificate.list().each { cert ->
                if (cert.defaultCertificate) {
                    cert.defaultCertificate = false
                    cert.save(failOnError: true, flush: true)
                }
            }
        }

        signingCertificate.save(failOnError: true, flush: true)

        flash.message = "Successfully generated signing certificate " +
                "'${signingCertificate.distinguishedName}' for organization ${cmd.organizationName}"

        return redirect(controller: 'organization', action:'view', id: cmd.orgId)
    }

    private String getBaseAppUrl() {
        def request = WebUtils.retrieveGrailsWebRequest().getCurrentRequest()

        def protocol = "http://"
        if (request.isSecure()) {
            protocol = "https://"
        }
        StringBuilder sb = new StringBuilder(protocol)
        sb.append(request.getServerName())
        sb.append(':')
        sb.append(request.getServerPort())
        // getContextPath already has the '/' prepended
        sb.append(request.getContextPath())

        return sb.toString()
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
    Integer orgId


    static constraints = {
        commonName(nullable: false, blank: false, maxSize: 255)
        localityName(nullable: true, blank: true, maxSize: 255)
        stateOrProvinceName(nullable: false, blank: false, maxSize: 255)
        countryName(nullable: false, blank: false, maxSize: 255)
        emailAddress(nullable: false, blank: false, maxSize: 255)
        organizationName(nullable: false, blank: false, maxSize: 255)
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
    }
}
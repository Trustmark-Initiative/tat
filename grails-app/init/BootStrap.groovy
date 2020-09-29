import assessment.tool.X509CertificateService
import edu.gatech.gtri.trustmark.v1_0.FactoryLoader
import edu.gatech.gtri.trustmark.v1_0.TrustmarkFramework
import grails.util.Environment
import nstic.TATPropertiesHolder
import nstic.SystemVariableDefinition
import nstic.util.AssessmentToolProperties
import nstic.util.QuartzConfig
import nstic.web.BinaryObject
import nstic.web.ContactInformation
import nstic.web.Organization
import nstic.web.Role
import nstic.web.ScanHostJob
import nstic.web.SigningCertificate
import nstic.web.SystemVariable
import nstic.web.TrustmarkMetadata
import nstic.web.User
import nstic.web.UserRole
import org.grails.web.util.WebUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.apache.commons.lang.StringUtils
import sun.security.x509.X500Name

import javax.servlet.ServletContext
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.cert.Certificate
import java.util.regex.Pattern

class BootStrap {

    public static final String TAT_CONFIG_FILE = "/WEB-INF/config/tat_config.properties"
    protected static final Logger log = LoggerFactory.getLogger(BootStrap.class)
    //==================================================================================================================
    //  Services/Injected Beans
    //==================================================================================================================
    def grailsApplication

    //==================================================================================================================
    //  Main Methods
    //==================================================================================================================
    def init = { ServletContext servletContext ->
        log.debug("Starting Trustmark Assessment Tool...")

        Properties props = readProps(servletContext)

        checkConfiguration(servletContext, props)
        checkFilesDir()

        boolean databaseEmpty = isDatabaseEmpty()
        checkDatabase(servletContext, props)

        if( Environment.current == Environment.DEVELOPMENT && databaseEmpty ){
            try {
                log.warn("Loading lots of @|yellow TEST|@ Data...")
                loadTestOrganizations()
                loadTestUsers()
                loadTestContacts()
                log.warn("Successfully loaded @|yellow TEST|@ Data!")
            }catch(Throwable t){
                log.error("An unexpected error occurred trying to load test data, ignoring it: "+t.toString())
            }
        }

        setupJobs(servletContext, props)

        printInitMessage()
    }
    def destroy = {
    }

    //==================================================================================================================
    //  Helper Methods
    //==================================================================================================================

    private void printInitMessage(){
        TrustmarkFramework tmf = FactoryLoader.getInstance(TrustmarkFramework.class)
        def config = grailsApplication.config

        String msg = """

--------------------------------------------------------------------------------------------------------------------
|  GTRI Trustmark Assessment Tool
|    Version: ${grailsApplication.metadata.getApplicationVersion()}
|    Environment: ${grailsApplication.metadata.getEnvironment()}
|
|    TMF API Information
|      API Version: ${tmf.getApiVersion()}, Build Date: ${tmf.getApiBuildDate()}
|      API Impl Version: ${tmf.getApiImplVersion()}, Build Date: ${tmf.getApiImplBuildDate()}
|      Supported TMF Version: @|cyan ${tmf.getTrustmarkFrameworkVersion()}|@
|
|    Configuration Information (@see /WEB-INF/config/tat_config.properties)
|      Files Directory: @|green ${SystemVariable.findBySystemVariableDefinition(SystemVariableDefinition.FILES_DIRECTORY)?.fieldValue}|@
|      Registry: @|green ${AssessmentToolProperties.getRegistryUrl()}|@
|      Mail Settings:
|        grails.mail.host            = ${config.grails?.mail?.host ?: 'null'}
|        grails.mail.port            = ${config.grails?.mail?.port ?: 'null'}
|        grails.mail.username        = ${config.grails?.mail?.username ?: 'null'}
|        grails.mail.default.from    = ${config.grails?.mail?.default?.from ?: 'null'}
|        grails.mail.disabled        = ${config.grails?.mail?.disabled ?: 'null'}
|        grails.mail.overrideAddress = ${config.grails?.mail?.overrideAddress ?: 'null'}
|        grails.mail.props           = [
${grailsMailProps(config)}
|        ]
|
--------------------------------------------------------------------------------------------------------------------

"""
        log.info(msg)
    }

    private void setupJobs(ServletContext servletContext, Properties props){
        log.debug("Starting Quartz Jobs...")

        new Thread(new Runnable(){
            public void run(){
                log.debug("Scheduling Scan Host Job...")
                QuartzConfig scanJobConfig = AssessmentToolProperties.getQuartzConfig("scanjob")
                if( scanJobConfig.getStartDelay() && scanJobConfig.getStartDelay() > 0l ) {
                    log.debug("Delaying ScanHostJob for @|green ${scanJobConfig.getStartDelay()}|@ms...")
                    Thread.sleep(scanJobConfig.getStartDelay())
                }
                if( scanJobConfig.getType() == QuartzConfig.TriggerType.cron ) {
                    ScanHostJob.schedule(scanJobConfig.getCronExpression())
                }else{
                    ScanHostJob.schedule(scanJobConfig.getRepeatInterval() ?: 10000l, scanJobConfig.getRepeatCount() ?: -1l)
                }
            }
        }).start()

    }//end setupJobs()


    private Properties readProps(ServletContext servletContext) {
        Properties props = AssessmentToolProperties.getProperties()
        servletContext.setAttribute("tatConfigProps", props)
        TATPropertiesHolder.setProperties(props)
        return props
    }

    private String grailsMailProps(config){
        StringBuilder sb = new StringBuilder()
        Map mailProps = config.grails?.mail?.props ?: [:]
        mailProps?.keySet().each{ key ->
            sb.append("|          [$key] = [${mailProps.get(key)}]\n")
        }
        return sb.toString()
    }

    private void checkConfiguration(ServletContext servletContext, Properties props) {
        SystemVariable.withTransaction {
            SystemVariableDefinition.values().each { SystemVariableDefinition sysVarDef ->
                log.debug("Checking value of SystemVariable[${sysVarDef}]...")
                SystemVariable sysVar = SystemVariable.findBySystemVariableDefinition(sysVarDef)
                if (!sysVar) {
                    log.debug("SystemVariable[${sysVarDef.getPropertyName()}] is not in database, checking configuration file...")
                    String valFromConfig = props.getProperty(sysVarDef.getPropertyName())
                    if (StringUtils.isNotEmpty(valFromConfig)) {
                        sysVar = new SystemVariable(name: sysVarDef.getPropertyName(), fieldValue: valFromConfig)
                        log.info("Created new SystemVariable[${sysVarDef.getPropertyName()}] = [${valFromConfig}]")
                        sysVar.save(failOnError: true, flush: true)
                    } else {
                        log.error("=========> Found no value for SystemVariable[${sysVarDef.getPropertyName()}]!")
                    }
                } else {
                    File configFile = new File(servletContext.getRealPath(TAT_CONFIG_FILE))
                    if (configFile.lastModified() > sysVar.lastUpdated.getTime()) {
                        //Config file was updated after database, so change database.
                        String valFromConfig = props.getProperty(sysVarDef.getPropertyName())
                        if (valFromConfig != sysVar.getFieldValue()) {
                            sysVar.fieldValue = valFromConfig
                            log.info("Set new value for SystemVariable[${sysVarDef.getPropertyName()}] = [${valFromConfig}]")
                            sysVar.save(failOnError: true, flush: true)
                        } else {
                            log.info("No change to SystemVariable[${sysVarDef.getPropertyName()}] = [${valFromConfig}]")
                        }
                    } else {
                        log.info("No change to SystemVariable[${sysVarDef.getPropertyName()}] = [${sysVar.value}]")
                    }
                }

                if (sysVar) {
                    servletContext.setAttribute(sysVarDef.getPropertyName(), sysVar.getFieldValue())
                }

            }
        }//end SystemVariable Transaction Scope

    }//end checkConfiguration()


    private void checkFilesDir() {

        SystemVariable filesDirVar = SystemVariable.findBySystemVariableDefinition(SystemVariableDefinition.FILES_DIRECTORY)
        if( !filesDirVar ){
            log.error("Expected to find SystemVariable[${SystemVariableDefinition.FILES_DIRECTORY.getPropertyName()}], but didn't!")
            throw new UnsupportedOperationException("Expected to find SystemVariable[${SystemVariableDefinition.FILES_DIRECTORY.getPropertyName()}], but didn't!")
        }
        String filesdirPath = filesDirVar.getFieldValue()
        log.info("Files directory set to: [@|cyan $filesdirPath|@]")
        if( filesdirPath.startsWith("/tmp") || filesdirPath.toLowerCase().contains("temp") ){
            log.error("**** ERROR ****  You should not use a 'temp' directory to store the assessment tool files.  The system is still starting, but these files will not survive! ")
        }
        def actualFilesDir = new File(filesdirPath)
        if( !actualFilesDir.exists() && !actualFilesDir.mkdirs() ){
            log.warn("Could not create files directory: "+actualFilesDir.canonicalPath)
            throw new RuntimeException("Unable to create files directory: "+actualFilesDir.canonicalPath)
        }

        try {
            File tempFile = new File(actualFilesDir, UUID.randomUUID().toString() + ".test")
            tempFile << "Test data"
            tempFile.delete()
            log.debug("Successfully created a test file, so we know we can write to the files directory.")
        }catch(Throwable T){
            log.error("**** ERROR ****  Cannot write to files directory[${filesdirPath}]!", T)
            throw T
        }

        BinaryObject.withTransaction {
            log.debug("Finding all BinaryObjects younger than 1 year...")
            List<BinaryObject> boList = BinaryObject.findAllByDateCreatedGreaterThan(getDateOneYearAgo())
            if (boList?.size() > 0) {
                log.debug("Checking ${boList.size()} binary objects on the file system...")
                for (BinaryObject bo : boList) {
                    try{
                        File contentFile = bo.content.toFile()
                        if( !contentFile.exists() ){
                            throw new FileNotFoundException("Cannot locate file[${contentFile.canonicalPath}], which was calculated for BinaryObject[id=${bo.id}, name=${bo.originalFilename}]")
                        }else{
                            log.debug("File ${bo.id}:${bo.originalFilename} confirmed to exist at ${contentFile.canonicalPath}")
                        }
                    }catch(Throwable t){
                        throw new RuntimeException("Error Validating BinaryObject[id=${bo.id}, name=${bo.originalFilename}]", t)
                    }
                }
                log.info("All ${boList.size()} BinaryObject files have been accounted for on the file system.")
            } else {
                log.info("No BinaryObject files in the last year found to check!")
            }
        }
    }

    private Date getDateOneYearAgo(){
        Calendar c = Calendar.getInstance()
        c.set(Calendar.YEAR, c.get(Calendar.YEAR) - 1)
        return c.getTime()
    }

    private boolean isDatabaseEmpty() {
        return Role.count() == 0
    }

    /**
     * Verifies the integrity of the database on application startup.
     */
    private void checkDatabase(ServletContext servletContext, Properties props) {
        Role.withTransaction {
            log.debug("Checking user roles...")
            for (String roleName : Role.ALL_ROLES) {
                Role role = Role.findByAuthority(roleName)
                if (!role) {
                    log.info("Creating Role @|cyan ${roleName}|@...")
                    role = new Role(authority: roleName)
                    role.save(failOnError: true)
                }
            }
            log.info("Database roles are correct.")
        }

        User.withTransaction {
            int userCount = User.count()
            if (userCount == 0) {
                Map defaultAccountData = AssessmentToolProperties.getDefaultAccountData()
                log.debug("No users found, creating from configured properties [${defaultAccountData.contacts?.size()} contacts, ${defaultAccountData.orgs?.size()} orgs, ${defaultAccountData.users?.size()} users]...")

                for( Map contact : defaultAccountData.contacts ?: []){
                    ContactInformation contactInformation = new ContactInformation()
                    contactInformation.responder = contact.responder
                    contactInformation.email = contact.email
                    contactInformation.phoneNumber = contact.telephone
                    contactInformation.mailingAddress = contact.mailingAddress
                    contactInformation.notes = contact.notes
                    log.debug("Saving ContactInformation[@|cyan ${contactInformation.email}|@]...")
                    contactInformation.save(failOnError: true)
                    contact.databaseObj = contactInformation
                }

                for( Map org : defaultAccountData.orgs ?: [] ){
                    Organization databaseOrg = new Organization()
                    databaseOrg.name = org.name
                    databaseOrg.identifier = org.identifier
                    databaseOrg.uri = org.uri
                    databaseOrg.primaryContact = findContact(defaultAccountData.contacts, org.contact)
                    log.debug("Saving Organization[@|cyan ${org.name}|@]...")
                    databaseOrg.save(failOnError: true)
                    org.databaseObj = databaseOrg
                }

                for( Map user : defaultAccountData.users ?: [] ){
                    User databaseUser = new User()
                    databaseUser.username = user.username
                    databaseUser.password = user.password
                    databaseUser.enabled = true
                    databaseUser.accountExpired = false
                    databaseUser.accountLocked = false
                    databaseUser.passwordExpired = false

                    databaseUser.contactInformation = findContact(defaultAccountData.contacts, user.contact)
                    databaseUser.organization = findOrganization(defaultAccountData.orgs, user.org)

                    log.debug("Saving User[@|cyan ${databaseUser.username}|@]...")
                    databaseUser.save(failOnError: true)
                    user.databaseObj = databaseUser


                    for( String roleName : user.roles.split(Pattern.quote(",")) ){
                        Role role = Role.findByAuthority(roleName)
                        if( !role )
                            throw new RuntimeException("Cannot find any role named ${roleName} to apply to user ${user.username}!  Check your configuration file.")

                        UserRole.create(databaseUser, role, true)
                    }

                }

                // TODO Assign extra contacts, or users in orgs, etc.

            } else {
                log.debug("@|cyan ${userCount}|@ users exist in the database, so we don't need to create any.")
            }
/*
            if (UserRole.countByRole(Role.findByAuthority(Role.ROLE_ADMIN)) == 0) {
                log.error("ERROR - Cannot find any administrator configured in the system.  Please check the system and try again!")
                throw new RuntimeException("Unable to start.  Database in strange state, there are users but none with ${Role.ROLE_ADMIN}.")
            }
 */
        }

        // Need to generate the signing certificate before the default trustmark metadata is generated
        SigningCertificate.withTransaction {
            if (SigningCertificate.count() == 0) {
                createDefaultSigningCertificate(AssessmentToolProperties.getProperties())
            }
        }

        TrustmarkMetadata.withTransaction {
            if (TrustmarkMetadata.count() == 0) {
                createDefaultTrustmarkMetadata(props)
            }
        }

    }//end checkDatabase()

    private Organization findOrganization(List orgs, String value){
        Organization databaseOrg = null
        for( Map org : orgs ?: []) {
            if( org.identifier == value || org.name == value || org.uri == value ){
                databaseOrg = org.databaseObj
                break
            }
        }
        if( databaseOrg == null ){
            throw new RuntimeException("Cannot find any organization with value: @|red ${value}|@")
        }
        return databaseOrg
    }

    private ContactInformation findContact(List contacts, String value){
        ContactInformation databaseContact = null
        for( Map c : contacts ?: []) {
            if( c.responder == value || c.email == value ){
                databaseContact = c.databaseObj
                break
            }
        }
        if( databaseContact == null ){
            throw new RuntimeException("Cannot find any contact with value: @|red ${value}|@")
        }
        return databaseContact
    }

    private void createDefaultTrustmarkMetadata (Properties props){
        log.info("Loading default Trustmark Metadata...")

        try {
            TrustmarkMetadata metadata = new TrustmarkMetadata()
            metadata.name = props.getProperty("trustmark.metadata.name") ?: "System Default"
            metadata.description = props.getProperty("trustmark.metadata.description") ?: "Generated automatically by scanning the assessment tool configuration properties."
            metadata.generatorClass = props.getProperty("trustmark.metadata.identifier.generator") ?: nstic.UUIDTrustmarkIdentifierGenerator.class.name
            metadata.identifierPattern = props.getProperty("trustmark.metadata.identifier.pattern") ?: "<not-set or not-found>"
            metadata.policyUrl = props.getProperty("trustmark.metadata.policy.url") ?: "<not-set or not-found>"
            metadata.relyingPartyAgreementUrl = props.getProperty("trustmark.metadata.relying.party.agreement.url") ?: "<not-set or not-found>"
            metadata.statusUrlPattern = props.getProperty("trustmark.metadata.status.url.pattern") ?: "<not-set or not-found>"
            metadata.timePeriodNoExceptions = Integer.parseInt(props.getProperty("trustmark.metadata.valid.timeperiod.no.exceptions") ?: '-1')
            if (metadata.timePeriodNoExceptions == -1)
                metadata.timePeriodNoExceptions = 36
            metadata.timePeriodWithExceptions = Integer.parseInt(props.getProperty("trustmark.metadata.valid.timeperiod.exceptions") ?: '-1')
            if (metadata.timePeriodWithExceptions == -1)
                metadata.timePeriodWithExceptions = 6

            String defaultCertificateDistinguishedName = props.getProperty("trustmark.certificate.default.distinguishedname")
                    ?: "CN=https://trustmarkinitiative.org/, OU=TI, O=Trustmark Initiative, L=Atlanta, ST=GA, C=US"

            SigningCertificate certificate = SigningCertificate.findByDistinguishedName(
                    defaultCertificateDistinguishedName)
            metadata.defaultSigningCertificateId = certificate.id

            String orgUri = props.getProperty("trustmark.metadata.orgUri") ?: null
            if (orgUri == null)
                throw new NullPointerException("Could not find property: trustmark.metadata.orgUri")
            Organization org = Organization.findByUri(orgUri)
            if (org == null)  {
                log.error("ERROR finding organization for default Trustmark Metadata...")
            }  else {
                metadata.provider = org
                metadata.save(failOnError: true)
            }
        }catch(Throwable t){
            log.error("Unable to create default Trustmark Metadata!", t)
            // This is a recoverable error, so we just continue on ignoring this failure.
        }
    }

    private void createDefaultSigningCertificate (Properties props){
        log.info("Loading default signing certificate...")

        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA")

            Integer keyLength = Integer.parseInt(
                    props.getProperty("trustmark.certificate.default.keylength") ?: "4096")
            keyPairGenerator.initialize(keyLength)
            KeyPair keyPair = keyPairGenerator.generateKeyPair()

            // distinguished name
            String distinguishedName = props.getProperty("trustmark.certificate.default.distinguishedname")
                    ?: "CN=https://trustmarkinitiative.org/, OU=TI, O=Trustmark Initiative, L=Atlanta, ST=GA, C=US"

            X500Name x500Name = new X500Name(distinguishedName)

            X509CertificateService x509CertificateService = new X509CertificateService()

            int validPeriod = Integer.parseInt(
                    props.getProperty("trustmark.certificate.default.validperiod") ?: "10")

            Certificate certificate = x509CertificateService.generateCertificate(distinguishedName,
                    keyPair, validPeriod * 365, "SHA256withRSA")

            // public cert string
            String pemCert = x509CertificateService.convertToPem(certificate)

            // private key
            String pemKey = x509CertificateService.convertToPemPrivateKey(keyPair.getPrivate())

            // raw thumbprint
            String thumbprint = x509CertificateService.getThumbPrint(certificate)

            // viewable thumbprint
            String thumbprintWithColons = x509CertificateService.getThumbPrintWithColons(certificate)

            String orgUri = props.getProperty("trustmark.metadata.orgUri") ?: null
            if (orgUri == null)
                throw new NullPointerException("Could not find property: trustmark.metadata.orgUri")
            Organization org = Organization.findByUri(orgUri)
            if (org == null)  {
                log.error("ERROR finding organization for default Trustmark Metadata...")
            }

            SigningCertificate signingCertificate = new SigningCertificate()
            signingCertificate.distinguishedName = distinguishedName
            signingCertificate.commonName = x500Name.commonName
            signingCertificate.localityName = x500Name.locality
            signingCertificate.stateOrProvinceName = x500Name.state
            signingCertificate.countryName = x500Name.country
            signingCertificate.emailAddress = org.primaryContact.email
            signingCertificate.organizationName = x500Name.organization
            signingCertificate.organizationalUnitName = x500Name.organizationalUnit
            signingCertificate.serialNumber = certificate.serialNumber.toString()
            signingCertificate.thumbPrint = thumbprint
            signingCertificate.thumbPrintWithColons = thumbprintWithColons
            signingCertificate.privateKeyPem = pemKey
            signingCertificate.x509CertificatePem = pemCert
            signingCertificate.organization = org


            // URL: create a unique filename to create the downloadable file
            // filename: commonName-thumbprint.pem
            String filename = x500Name.commonName + "-" + thumbprint + ".pem"
            signingCertificate.filename = filename

            String baseUrl = props.getProperty("tf.base.url") ?: "http://localhost:8080/trustmark-assessments"

            baseUrl += "/PublicCertificates/download/?id="

            signingCertificate.certificatePublicUrl = baseUrl + filename

            signingCertificate.defaultCertificate = true

            signingCertificate.save(failOnError: true)
        }catch(Throwable t){
            log.error("Unable to create default signing certificate!", t)
            // This is a recoverable error, so we just continue on ignoring this failure.
        }
    }

    public static List<String> MALE_FIRST_NAMES = [
            "JAMES", "JOHN", "ROBERT", "MICHAEL", "WILLIAM", "DAVID", "RICHARD", "CHARLES", "JOSEPH", "THOMAS", "CHRISTOPHER", "DANIEL", "PAUL", "MARK", "DONALD", "GEORGE", "KENNETH", "STEVEN", "EDWARD", "BRIAN", "RONALD", "ANTHONY", "KEVIN", "JASON", "MATTHEW", "GARY", "TIMOTHY", "JOSE", "LARRY", "JEFFREY", "FRANK", "SCOTT", "ERIC", "STEPHEN", "ANDREW", "RAYMOND", "GREGORY", "JOSHUA", "JERRY", "DENNIS", "WALTER", "PATRICK", "PETER", "HAROLD", "DOUGLAS", "HENRY", "CARL", "ARTHUR", "RYAN", "ROGER", "JOE", "JUAN", "JACK", "ALBERT", "JONATHAN", "JUSTIN", "TERRY", "GERALD", "KEITH", "SAMUEL", "WILLIE", "RALPH", "LAWRENCE", "NICHOLAS", "ROY", "BENJAMIN", "BRUCE", "BRANDON", "ADAM", "HARRY", "FRED", "WAYNE", "BILLY", "STEVE", "LOUIS", "JEREMY", "AARON", "RANDY", "HOWARD", "EUGENE", "CARLOS", "RUSSELL", "BOBBY", "VICTOR", "MARTIN", "ERNEST", "PHILLIP", "TODD", "JESSE", "CRAIG", "ALAN", "SHAWN", "CLARENCE", "SEAN", "PHILIP", "CHRIS", "JOHNNY", "EARL", "JIMMY", "ANTONIO", "DANNY", "BRYAN", "TONY", "LUIS", "MIKE", "STANLEY", "LEONARD", "NATHAN", "DALE", "MANUEL", "RODNEY", "CURTIS", "NORMAN", "ALLEN", "MARVIN", "VINCENT", "GLENN", "JEFFERY", "TRAVIS", "JEFF", "CHAD", "JACOB", "LEE", "MELVIN", "ALFRED", "KYLE", "FRANCIS", "BRADLEY", "JESUS", "HERBERT", "FREDERICK", "RAY", "JOEL", "EDWIN", "DON", "EDDIE", "RICKY", "TROY", "RANDALL", "BARRY", "ALEXANDER", "BERNARD", "MARIO", "LEROY", "FRANCISCO", "MARCUS", "MICHEAL", "THEODORE", "CLIFFORD", "MIGUEL", "OSCAR", "JAY", "JIM", "TOM", "CALVIN", "ALEX", "JON", "RONNIE", "BILL", "LLOYD", "TOMMY", "LEON", "DEREK", "WARREN", "DARRELL", "JEROME", "FLOYD", "LEO", "ALVIN", "TIM", "WESLEY", "GORDON", "DEAN", "GREG", "JORGE", "DUSTIN", "PEDRO", "DERRICK", "DAN", "LEWIS", "ZACHARY", "COREY", "HERMAN", "MAURICE", "VERNON", "ROBERTO", "CLYDE", "GLEN", "HECTOR", "SHANE", "RICARDO", "SAM", "RICK", "LESTER", "BRENT", "RAMON", "CHARLIE", "TYLER", "GILBERT", "GENE", "MARC", "REGINALD", "RUBEN", "BRETT", "ANGEL", "NATHANIEL", "RAFAEL", "LESLIE", "EDGAR", "MILTON", "RAUL", "BEN", "CHESTER", "CECIL", "DUANE", "FRANKLIN", "ANDRE", "ELMER", "BRAD", "GABRIEL",
    ]
    public static List<String> FEMALE_FIRST_NAMES = [
            "MARY", "PATRICIA", "LINDA", "BARBARA", "ELIZABETH", "JENNIFER", "MARIA", "SUSAN", "MARGARET", "DOROTHY", "LISA", "NANCY", "KAREN", "BETTY", "HELEN", "SANDRA", "DONNA", "CAROL", "RUTH", "SHARON", "MICHELLE", "LAURA", "SARAH", "KIMBERLY", "DEBORAH", "JESSICA", "SHIRLEY", "CYNTHIA", "ANGELA", "MELISSA", "BRENDA", "AMY", "ANNA", "REBECCA", "VIRGINIA", "KATHLEEN", "PAMELA", "MARTHA", "DEBRA", "AMANDA", "STEPHANIE", "CAROLYN", "CHRISTINE", "MARIE", "JANET", "CATHERINE", "FRANCES", "ANN", "JOYCE", "DIANE", "ALICE", "JULIE", "HEATHER", "TERESA", "DORIS", "GLORIA", "EVELYN", "JEAN", "CHERYL", "MILDRED", "KATHERINE", "JOAN", "ASHLEY", "JUDITH", "ROSE", "JANICE", "KELLY", "NICOLE", "JUDY", "CHRISTINA", "KATHY", "THERESA", "BEVERLY", "DENISE", "TAMMY", "IRENE", "JANE", "LORI", "RACHEL", "MARILYN", "ANDREA", "KATHRYN", "LOUISE", "SARA", "ANNE", "JACQUELINE", "WANDA", "BONNIE", "JULIA", "RUBY", "LOIS", "TINA", "PHYLLIS", "NORMA", "PAULA", "DIANA", "ANNIE", "LILLIAN", "EMILY", "ROBIN", "PEGGY", "CRYSTAL", "GLADYS", "RITA", "DAWN", "CONNIE", "FLORENCE", "TRACY", "EDNA", "TIFFANY", "CARMEN", "ROSA", "CINDY", "GRACE", "WENDY", "VICTORIA", "EDITH", "KIM", "SHERRY", "SYLVIA", "JOSEPHINE", "THELMA", "SHANNON", "SHEILA", "ETHEL", "ELLEN", "ELAINE", "MARJORIE", "CARRIE", "CHARLOTTE", "MONICA", "ESTHER", "PAULINE", "EMMA", "JUANITA", "ANITA", "RHONDA", "HAZEL", "AMBER", "EVA", "DEBBIE", "APRIL", "LESLIE", "CLARA", "LUCILLE", "JAMIE", "JOANNE", "ELEANOR", "VALERIE", "DANIELLE", "MEGAN", "ALICIA", "SUZANNE", "MICHELE", "GAIL", "BERTHA", "DARLENE", "VERONICA", "JILL", "ERIN", "GERALDINE", "LAUREN", "CATHY", "JOANN", "LORRAINE", "LYNN", "SALLY", "REGINA", "ERICA", "BEATRICE", "DOLORES", "BERNICE", "AUDREY", "YVONNE", "ANNETTE", "JUNE", "SAMANTHA", "MARION", "DANA", "STACY", "ANA", "RENEE", "IDA", "VIVIAN", "ROBERTA", "HOLLY", "BRITTANY", "MELANIE"
    ]
    public static List<String> LAST_NAMES = [
            "SMITH", "JOHNSON", "WILLIAMS", "BROWN", "JONES", "MILLER", "DAVIS", "GARCIA", "RODRIGUEZ", "WILSON", "MARTINEZ", "ANDERSON", "TAYLOR", "THOMAS", "HERNANDEZ", "MOORE", "MARTIN", "JACKSON", "THOMPSON", "WHITE", "LOPEZ", "LEE", "GONZALEZ", "HARRIS", "CLARK", "LEWIS", "ROBINSON", "WALKER", "PEREZ", "HALL", "YOUNG", "ALLEN", "SANCHEZ", "WRIGHT", "KING", "SCOTT", "GREEN", "BAKER", "ADAMS", "NELSON", "HILL", "RAMIREZ", "CAMPBELL", "MITCHELL", "ROBERTS", "CARTER", "PHILLIPS", "EVANS", "TURNER", "TORRES", "PARKER", "COLLINS", "EDWARDS", "STEWART", "FLORES", "MORRIS", "NGUYEN", "MURPHY", "RIVERA", "COOK", "ROGERS", "MORGAN", "PETERSON", "COOPER", "REED", "BAILEY", "BELL", "GOMEZ", "KELLY", "HOWARD", "WARD", "COX", "DIAZ", "RICHARDSON", "WOOD", "WATSON", "BROOKS", "BENNETT", "GRAY", "JAMES", "REYES", "CRUZ", "HUGHES", "PRICE", "MYERS", "LONG", "FOSTER", "SANDERS", "ROSS", "MORALES", "POWELL", "SULLIVAN", "RUSSELL", "ORTIZ", "JENKINS", "GUTIERREZ", "PERRY", "BUTLER", "BARNES", "FISHER"
    ]


    static Map<String, String> sampleDomains = [
            'mail.google.com' : 'Google',
            'gtri.org' : 'Georgia Tech Research Institute',
            'gatech.edu' : 'Georgia Institute of Technology',
            'microsoft.com': 'Microsoft',
            'amazon.com' : 'Amazon',
            'mitre.org' : 'Mitre',
            'nist.gov' : 'NIST',
            'fbi.gov' : 'Federal Bureau of Investigation',
            'leo.gov' : 'Law Enforcement Officers']
    static List<String> domainsList = new ArrayList(sampleDomains.keySet())

    private void loadTestOrganizations() {
        log.debug("Creating @|yellow TEST|@ organizations...")
        def orgs = []
        for( Map.Entry<String, String> entry : sampleDomains.entrySet() ){
            Organization.withTransaction {
                Organization org = new Organization()
                org.uri = "http://" + entry.key
                org.name = entry.value
                org.save(failOnError: true)
                orgs.add(org)
            }
        }
    }

    private void loadTestUsers() {
        User.withTransaction {
            Random random = new Random(System.currentTimeMillis())
            Integer userCount = 5

            Role userRole = Role.findByAuthority(Role.ROLE_USER)
            Role adminRole = Role.findByAuthority(Role.ROLE_ADMIN)

            int createdCount = 0
            log.debug("Creating @|cyan ${userCount}|@ @|yellow TEST|@ users...")
            for (int i = 0; i < userCount; i++) {
                log.debug("Constructing new user...")
                String firstName = MALE_FIRST_NAMES.get(random.nextInt(MALE_FIRST_NAMES.size()))
                if (random.nextBoolean())
                    firstName = FEMALE_FIRST_NAMES.get(random.nextInt(FEMALE_FIRST_NAMES.size()))
                firstName = firstName.toLowerCase().capitalize()
                String lastName = LAST_NAMES.get(random.nextInt(LAST_NAMES.size()))
                lastName = lastName.toLowerCase().capitalize()
                String domain = domainsList.get(random.nextInt(domainsList.size()))
                Organization org = Organization.findByUri("http://"+domain)
                String email = firstName.toLowerCase() + "." + lastName.toLowerCase() + "." + random.nextInt(98) + 1 + "@" + domain
                email = email.replace("/", "")

                log.debug("Built email=[${email}]")

                ContactInformation contactInformation = new ContactInformation()
                contactInformation.responder = firstName + " " + lastName
                contactInformation.email = email
                contactInformation.phoneNumber = random.nextInt(9).toString() + random.nextInt(9).toString() + random.nextInt(9).toString() + "-" + random.nextInt(9).toString() + random.nextInt(9).toString() + random.nextInt(9).toString() + "-" +
                        random.nextInt(9).toString() + random.nextInt(9).toString() + random.nextInt(9).toString() + random.nextInt(9).toString()
                contactInformation.mailingAddress = random.nextInt(1000) + " Peachtree, Atlanta, GA"
                contactInformation.save(failOnError: true, flush: true)

                if( random.nextBoolean() ) {
                    org.primaryContact = contactInformation
                    org.save(failOnError: true, flush: true)
                }

                User user = new User()
                user.username = email
                user.contactInformation = contactInformation
                user.organization = org
                user.password = "test11!"
                user.enabled = true
                user.accountLocked = false
                user.accountExpired = false
                user.passwordExpired = false
                user.save(failOnError: true, flush: false)
                createdCount++

                UserRole.create(user, userRole, false)

                if (random.nextInt(100) > 95) {
                    UserRole.create(user, adminRole, false)
                }

                if (i % 50 == 0 && i > 0) {
                    log.debug("Created ${i} users.")
                }
            }
            log.debug("Successfully created @|cyan ${createdCount}|@ users.")

        }//end User transaction.

    }//end loadTestUsers()

    /**
     * Loads up sample organization and contact information for use
     */
    def loadTestContacts() {
        Organization.withTransaction {

            Random random = new Random(System.currentTimeMillis())

            List<Organization> orgs = Organization.findAll()

            int contactCount = 30

            log.debug("Creating @|cyan ${contactCount}|@ @|yellow TEST|@ contact information instances...")
            def contacts = []
            for (int i = 0; i < contactCount; i++) {
                ContactInformation contactInfo = new ContactInformation()
                Organization organization = getRandom(orgs, random)
                String domain = organization.uri.replace("http://", "").replace("https://", "")

                String firstName = MALE_FIRST_NAMES.get(random.nextInt(MALE_FIRST_NAMES.size()))
                if (random.nextBoolean())
                    firstName = FEMALE_FIRST_NAMES.get(random.nextInt(FEMALE_FIRST_NAMES.size()))
                firstName = firstName.toLowerCase().capitalize()
                String lastName = LAST_NAMES.get(random.nextInt(LAST_NAMES.size()))
                lastName = lastName.toLowerCase().capitalize()
                String email = firstName.toLowerCase() + "." + lastName.toLowerCase() + "." + random.nextInt(98) + 1 + "@" + domain

                contactInfo.email = email
                contactInfo.responder = firstName + " " + lastName
                contactInfo.phoneNumber = random.nextInt(9).toString() + random.nextInt(9).toString() + random.nextInt(9).toString() + "-" + random.nextInt(9).toString() + random.nextInt(9).toString() + random.nextInt(9).toString() + "-" +
                        random.nextInt(9).toString() + random.nextInt(9).toString() + random.nextInt(9).toString() + random.nextInt(9).toString()
                contactInfo.save(failOnError: true)

                if (!organization.primaryContact) {
                    organization.primaryContact = contactInfo
                }
                organization.addToContacts(contactInfo)
                organization.save(failOnError: true)

            }

            log.info("Successfully loaded sample organizations & contacts!")
        }

    }//end loadOrganizationsAndContacts()



    static List<String> SAMPLE_URI_DATA = [
            "org:gtri:trustmark", "edu:gatech:gtri:trustmark", "gov:fbi:trustmark", "org:nief:trustmark",
            "gov:nist:nstic:trustmark", "gov:leo:trustmark", "org:incommon:trustmark", "com:microsoft:trustmark",
            "com:amazon:trustmark", "com:google:trustmark", "edu:mit:trustmark"
    ]


    private File antFileFindSingle( File directory, String nameExpression ){
        def ant = new AntBuilder()
        def scanner = ant.fileScanner {
            fileset(dir: directory.canonicalPath) {
                include(name: nameExpression)
            }
        }
        for( file in scanner){
            return file
        }
        return null
    }//end antFileFindSingle()

    /**
     * Explodes the given zip file to a location on the filesystem, and returns the extracted folder.
     */
    private File extract(File zipFile){
        File tempDir = File.createTempFile("td-extracted-zip-", ".dir")
        tempDir.delete()
        tempDir.mkdirs()
        // Nice bit of code from: http://stackoverflow.com/questions/645847/unzip-archive-with-groovy/2238489#2238489
        def ant = new AntBuilder()
        ant.unzip(  src: zipFile.canonicalPath,
                dest: tempDir.canonicalPath,
                overwrite:"true" )
        return tempDir
    }//end extract()



    private def getRandom(list, random){
        return list.get(random.nextInt(list.size()))
    }

}

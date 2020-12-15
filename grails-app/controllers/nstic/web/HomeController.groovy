package nstic.web

import nstic.util.AssessmentToolProperties
import nstic.util.QuartzConfig
import nstic.web.assessment.Assessment
import nstic.web.td.TrustmarkDefinition
import nstic.web.UserRole
import nstic.web.Role

import javax.servlet.ServletContext
import java.util.regex.Pattern

class HomeController {

    //def sessionFactory
    def springSecurityService


    def index() {
        log.info "Loading home page for user: @|cyan ${springSecurityService.currentUser ?: 'anonymous'}|@"

        //def connectionUrl = sessionFactory.getCurrentSession().connection().getMetaData().getURL()
        //log.info "Connection URL: $connectionUrl"

        boolean firstTimeLogin = (UserRole.countByRole(Role.findByAuthority(Role.ROLE_ADMIN)) == 0)

        boolean noTpatRegistryUrl = (AssessmentToolProperties.getRegistryUrl() == null)

        // We also include the missing of the registry url as a trigger to the first time login page
        firstTimeLogin = firstTimeLogin || noTpatRegistryUrl

        [
                firstTimeLogin: firstTimeLogin,
                trustmarkDefinitionCount: TrustmarkDefinition.count(),
                trustmarkDefinitions: TrustmarkDefinition.list([max:10]), // TODO Improve this to most relevant 10
                assessmentCount: Assessment.count(),
                user : springSecurityService.currentUser
        ]

    }//end index()

    def initialize()  {
        log.info "initalize admin password and registry url  ${[params]}"

        if(params.password == params.passwordAgain)  {
            User.withTransaction {

                ContactInformation contactInformation = ContactInformation.findByResponderAndEmailAndPhoneNumberAndMailingAddress(
                        params.contactResponder, params.contactEmail, params.contactPhone, params.contactAddr)

                if (!contactInformation) {
                    contactInformation = new ContactInformation()
                    contactInformation.responder = params.contactResponder
                    contactInformation.email = params.contactEmail
                    contactInformation.phoneNumber = params.contactPhone
                    contactInformation.mailingAddress = params.contactAddr
                    contactInformation.notes = 'Initial Administator contact'
                    contactInformation.save(failOnError: true)
                }

                Organization databaseOrg = Organization.findByNameAndIdentifierAndUri(
                        params.organizationName, params.organizationId, params.organizationUri)

                if (!databaseOrg) {
                    databaseOrg = new Organization()
                    databaseOrg.name = params.organizationName
                    databaseOrg.identifier = params.organizationId
                    databaseOrg.uri = params.organizationUri
                    databaseOrg.primaryContact = contactInformation
                    databaseOrg.save(failOnError: true)
                }

                User databaseUser = User.findByUsername(params.username)

                if (!databaseUser) {
                    databaseUser = new User()
                    databaseUser.username = params.username
                    databaseUser.password = params.password
                    databaseUser.enabled = true
                    databaseUser.accountExpired = false
                    databaseUser.accountLocked = false
                    databaseUser.passwordExpired = false
                    databaseUser.contactInformation = contactInformation
                    databaseUser.organization = databaseOrg

                    log.debug("Saving User[@|cyan ${databaseUser.username}|@]...")
                    databaseUser.save(failOnError: true)

                    Role role = Role.findByAuthority('ROLE_ADMIN')
                    UserRole.create(databaseUser, role, true)
                    role = Role.findByAuthority('ROLE_USER')
                    UserRole.create(databaseUser, role, true)
                }


                Registry registry = Registry.findByRegistryUrl(params.registryUrl)

                if (!registry) {
                    registry = new Registry()
                    registry.registryUrl = params.registryUrl
                    registry.name = 'TPAT Registry'
                    registry.lastUpdated = new Date()
                    registry.save(failOnError: true)
                }

                if (AssessmentToolProperties.getProperties().getProperty("registry.url") == null)  {
                    AssessmentToolProperties.setRegistryUrl(params.registryUrl)
                    setupJobs()
                }  else  {
                    AssessmentToolProperties.setRegistryUrl(params.registryUrl)
                }

                if (TrustmarkMetadata.count() == 0)  {
                    createDefaultTrustmarkMetadata(AssessmentToolProperties.getProperties(), databaseOrg)
                }
               }
            }
        redirect(controller: 'home', action: 'index')
    }

    private void createDefaultTrustmarkMetadata (Properties props, Organization dbOrg){
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

            String orgUri = props.getProperty("trustmark.metadata.orgUri") ?: null
            if( orgUri == null )
                throw new NullPointerException("Could not find property: trustmark.metadata.orgUri")
            Organization org = Organization.findByUri(orgUri)
            if( org == null )  {
                metadata.provider = dbOrg
            }  else  {
                metadata.provider = org
            }

            metadata.save(failOnError: true)
        }catch(Throwable t){
            log.error("Unable to create default Trustmark Metadata!", t)
            // This is a recoverable error, so we just continue on ignoring this failure.
        }
    }
    private void setupJobs(){
        log.debug("Starting Quartz Jobs...")

        new Thread(new Runnable(){
            void run()  {
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

}//end HomeController

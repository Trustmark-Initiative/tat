package nstic.web

import assessment.tool.FileService
import edu.gatech.gtri.trustmark.v1_0.FactoryLoader
import edu.gatech.gtri.trustmark.v1_0.io.Serializer
import edu.gatech.gtri.trustmark.v1_0.io.SerializerFactory
import edu.gatech.gtri.trustmark.v1_0.io.TrustInteroperabilityProfileResolver
import edu.gatech.gtri.trustmark.v1_0.io.TrustmarkDefinitionResolver
import edu.gatech.gtri.trustmark.v1_0.model.AbstractTIPReference
import edu.gatech.gtri.trustmark.v1_0.model.TrustmarkDefinitionParameter
import edu.gatech.gtri.trustmark.v1_0.service.Page
import edu.gatech.gtri.trustmark.v1_0.service.RemoteStatus
import edu.gatech.gtri.trustmark.v1_0.service.RemoteTrustInteroperabilityProfile
import edu.gatech.gtri.trustmark.v1_0.service.RemoteTrustmarkDefinition
import edu.gatech.gtri.trustmark.v1_0.service.TrustmarkFrameworkService
import edu.gatech.gtri.trustmark.v1_0.service.TrustmarkFrameworkServiceFactory
import grails.gorm.transactions.Transactional
import nstic.util.AssessmentToolProperties
import nstic.util.HtmlUtils
import nstic.web.td.AssessmentStep
import nstic.web.td.AssessmentStepArtifact
import nstic.web.td.Citation
import nstic.web.td.ConformanceCriterion
import nstic.web.td.TdParameter
import nstic.web.td.TrustmarkDefinition
import nstic.web.tip.TIPReference
import nstic.web.tip.TrustInteroperabilityProfile
import org.apache.commons.codec.binary.Base64
import org.apache.commons.lang.StringUtils
import org.springframework.beans.factory.annotation.Autowired

import java.security.MessageDigest

/**
 * Scans the pre-configured TMF Host instances to download TDs and TIPs for assessing.
 * Created by brad on 3/30/16.
 */
@Transactional
class ScanHostJob {

    public static final String TIP_CACHE_DATE_PROP_PREFIX = "TIP_CACHE_DATE"
    public static final String TD_CACHE_DATE_PROP_PREFIX = "TD_CACHE_DATE"

    public static final String STATUS_VAR = ScanHostJob.class.getName()+".STATUS"
    public static final String PERCENT_VAR = ScanHostJob.class.getName()+".PERCENT"
    public static final String MESSAGE_VAR = ScanHostJob.class.getName()+".MESSAGE"

    //==================================================================================================================
    // Job Specifics
    //==================================================================================================================
    def concurrent = false
    def description = "Scans the pre-configured TMF Host instances to detect any added or removed Trustmark Framework artifacts."

    def sessionFactory

    @Autowired
    FileService fileService
    //==================================================================================================================
    // Execute entry point
    //==================================================================================================================
    void execute() {
        log.info("Starting ${this.getClass().getSimpleName()}...")
        long overallStartTime = System.currentTimeMillis()
        SystemVariable.storeProperty(STATUS_VAR, "RUNNING")
        SystemVariable.storeProperty(PERCENT_VAR, "0")

        List<String> urls = getRemoteHostURLs()
        for( String url : urls ){
            try {
                if (url == null)  {
                    return
                }
                SystemVariable.storeProperty(STATUS_VAR, "SYNC $url")
                SystemVariable.storeProperty(MESSAGE_VAR, "Synchronizing with URL: "+url)
                log.debug("Starting import check from URL[$url]...")
                long currentUrlStartTime = System.currentTimeMillis()
                initialize(url)

                if( detectChanges() ) {
                    log.debug("Loading changes from remote TMF Service URL[$url]...")
                    long tdStartTime = System.currentTimeMillis()
                    scanForTDChanges()
                    long tdStopTime = System.currentTimeMillis()

                    long tipStartTime = tdStopTime
                    scanForTIPChanges()
                    long tipStopTime = System.currentTimeMillis()

                    long superSedesStartTime = tipStopTime
                    //        updateSupersedesInformation()
                    long superSedesStopTime = System.currentTimeMillis()
                }else{
                    log.debug("There have been no changes at TMF Service URL[$url]")
                }

                destroy()

                long currentUrlStopTime = System.currentTimeMillis()

                log.info("Successfully checked URL[$url] in ${currentUrlStopTime - currentUrlStartTime}ms.")
            }catch(Throwable t){
                log.error("Unable to import from URL[$url]", t)
                SystemVariable.storeProperty(MESSAGE_VAR, "Unable to import from URL[$url]: "+t.toString())
                ErrorLogMessage.quickAdd("ScanHostJob.execute()", "Unexpected error while scanning changes at URL[${url}]: "+t.toString(), t)
            }
        }

        try{
            cleanUpTipReferences()
        }catch(Throwable t){
            log.error("Unable to clean up TIP references!", t)
            SystemVariable.storeProperty(MESSAGE_VAR, "Unable to clean up TIP references: "+t.toString())
            ErrorLogMessage.quickAdd("ScanHostJob.execute()", "Unexpected error while cleaning up TIP references: "+t.toString(), t)
        }


        long overallStopTime = System.currentTimeMillis()
        log.info("Successfully Executed ${this.getClass().getSimpleName()} in ${(overallStopTime - overallStartTime)}ms.")
        SystemVariable.storeProperty(STATUS_VAR, "SUCCESS")
        SystemVariable.storeProperty(PERCENT_VAR, "100")
        SystemVariable.storeProperty(MESSAGE_VAR, "Successfully Executed ${this.getClass().getSimpleName()} in ${(overallStopTime - overallStartTime)}ms.")

    }//end execute()


    //==================================================================================================================
    // Instance Variables
    //==================================================================================================================
    private Map<String, Collection<String>> supersedesInformation = null
    private String url
    private TrustmarkFrameworkService service = null
    private RemoteStatus remoteStatus = null
    //==================================================================================================================
    // General Helper Methods
    //==================================================================================================================
    // Called when the job has some initialize work to do.
    private void initialize(String url) {
        this.url = url
        this.supersedesInformation = [:] // Create an empty hash to store supersedes information.
        TrustmarkFrameworkServiceFactory serviceFactory = FactoryLoader.getInstance(TrustmarkFrameworkServiceFactory.class)
        TrustmarkFrameworkService service = serviceFactory.createService(url)
        this.service = service
        RemoteStatus remoteStatus = service.getStatus()
        this.remoteStatus = remoteStatus
    }

    private void destroy() {
        this.url = null
        this.supersedesInformation = null
        this.service = null
        this.remoteStatus = null
    }

    private static List<String> getRemoteHostURLs(){
        List<String> registryURLs = []
        Registry.findAll().each { Registry registry ->
            registryURLs.add(registry.registryUrl)
        }

        return registryURLs
    }

    /**
     * Analyzes the remote status and compares against the known last cache date for changes.
     */
    private boolean detectChanges(){
        return hasTdChanges() || hasTipChanges()
    }//end detectChanges()


    private String getSha1Sum(File file){
        MessageDigest md = MessageDigest.getInstance("SHA-256")
        md.reset()
        md.update(file.bytes)
        byte[] digest = md.digest()
        return new String(Base64.encodeBase64(digest))
    }

    /**
     * Read this post:
     * http://naleid.com/blog/2009/10/01/batch-import-performance-with-grails-and-mysql
     */
    private void cleanUpGorm() {
        def session = sessionFactory.currentSession
        session.flush()
        session.clear()
//        propertyInstanceMap.get().clear()
    }

    //==================================================================================================================
    //  Trust Interoperability Profile Methods
    //==================================================================================================================
    private void scanForTIPChanges(){
        if( hasTipChanges() ){
            log.debug("Loading TIP Changes from URL[$url]...")

            List<RemoteTrustInteroperabilityProfile> remoteTips = readAllRemoteTips()
            log.debug("Successfully read ${remoteTips.size()} TIP objects from ${this.url}")

            for( int i = 0; i < remoteTips.size(); i++){
                RemoteTrustInteroperabilityProfile remoteTip = remoteTips.get(i)
                URL tipUrl = remoteTip.formats.get("json")
                if( tipUrl == null )
                    throw new UnsupportedOperationException("Missing required JSON tip URL!  Cannot download ${tipUrl}")

                TrustInteroperabilityProfile databaseTip = TrustInteroperabilityProfile.findByUri(remoteTip.getIdentifier().toString())
                if( databaseTip != null ){
                    TrustInteroperabilityProfile.withTransaction {
                        if (databaseTip.cachedUrl == null) {
                            databaseTip.cachedUrl = remoteTip.getFormats()?.get("json")?.toString()
                            databaseTip.save(failOnError: true)
                        }

                        if (sameTimestamp(databaseTip.getPublicationDateTime(), remoteTip.getPublicationDateTime())) {
                            log.debug("TIP[${databaseTip.getUri()}] has no changes detected.")
                        } else {
                            log.info("TIP[${databaseTip.getUri()}] has been updated remotely, marking the local copy as out of date...")
                            databaseTip.setEnabled(false)
                            databaseTip.setOutOfDate(true)
                            databaseTip.save(failOnError: true)
                        }
                    }
                }else{
                    importTipClean(remoteTip)
                    SystemVariable.storeProperty(MESSAGE_VAR, "Successfully downloaded & cached Trust Interoperability Profile: "+remoteTip.getName()+", v"+remoteTip.getVersion())
                }

                if( i % 50 == 0 ) {
                    cleanUpGorm()
                    log.info("Finished processing "+((int) Math.floor( ((double) i/(double) remoteTips.size()) * 100.0d))+"% of TIPs!")
                }

                int percent = (int) Math.floor (((double) i / (double) remoteTips.size()) * 100.0d)
                SystemVariable.storeProperty(PERCENT_VAR, ""+percent)
            }

            SystemVariable.storeProperty("${TIP_CACHE_DATE_PROP_PREFIX}{"+this.url+"}", System.currentTimeMillis()+""); // Update the cache date.
        }
    }//end scanForTIPChanges()

    private void importTipClean(RemoteTrustInteroperabilityProfile remoteTip){
        TrustInteroperabilityProfile.withTransaction {
            try{
                URL tipUrl = remoteTip.getFormats().get("json")
                if( tipUrl == null )
                    throw new UnsupportedOperationException("TIP[${remoteTip.getIdentifier().toString()}] does not have format 'json', which is required.")
                log.debug("    Loading ${tipUrl}")
                TrustInteroperabilityProfileResolver resolver = FactoryLoader.getInstance(TrustInteroperabilityProfileResolver.class)
                edu.gatech.gtri.trustmark.v1_0.model.TrustInteroperabilityProfile tip = resolver.resolve(tipUrl)

                TrustInteroperabilityProfile databaseTip = new TrustInteroperabilityProfile()
                databaseTip.baseUri = this.url
                databaseTip.uri = tip.getIdentifier().toString()
                databaseTip.cachedUrl = remoteTip.getFormats()?.get("json")?.toString()
                databaseTip.name = tip.getName()
                databaseTip.tipVersion = tip.getVersion()
                databaseTip.description = tip.getDescription()
                databaseTip.publicationDateTime = tip.getPublicationDateTime()
                databaseTip.tipExpression = tip.getTrustExpression()

                // Source & Signature.
                File jsonFile = serialize(tip)
                String originalFilename = "${tip.getName().replace("/", "_SLASH_")}.json"
                BinaryObject binaryObject = this.fileService.createBinaryObject(jsonFile, ScanHostJob.class.getName(), "application/json", originalFilename, "json")
                databaseTip.setSource(binaryObject)
                databaseTip.setSignature(getSha1Sum(jsonFile))

                databaseTip.save(failOnError: true)

                for(AbstractTIPReference tipReference : tip.getReferences() ){
                    TIPReference databaseTipRef = new TIPReference(owningTIP: databaseTip)
                    databaseTipRef.number = tipReference.number
                    if(databaseTipRef.getNumber() == null)  {
                        log.warn("Setting the Sequence Number to 1 for TIPReference ${databaseTipRef.id}")
                        databaseTipRef.setNumber(1)
                    }
                    databaseTipRef.referenceName = tipReference.getId()
                    if( tipReference.isTrustInteroperabilityProfileReference() ){
                        databaseTipRef.notes = "TIP: "+tipReference.getIdentifier().toString()
                    }else{ // It must be a TD
                        databaseTipRef.notes = "TD: "+tipReference.getIdentifier().toString()
                    }
                    databaseTipRef.save(failOnError: true)
                    databaseTip.addToReferences(databaseTipRef)
                    databaseTip.save(failOnError: true)
                }

            }catch(Throwable t){
                log.error("Unable to download TIP: "+remoteTip.getIdentifier(), t)
                ErrorLogMessage.quickAdd("ScanHostJob.importTipClean()", "Unexpected error while importing TIP[${remoteTip.getIdentifier().toString()}]: "+t.toString(), t)
            }
        }
    }//end importTipClean()

    /**
     * Searches for any TIPReference objects which have not linked to the database at all.  At this point, all external
     * TIPs and TDs should be loaded.
     */
    private void cleanUpTipReferences() {
        int totalCount = 0
        int errorCount = 0
        int successCount = 0
        List<TIPReference> nullTipRefs = TIPReference.executeQuery('from TIPReference where trustInteroperabilityProfile is null and trustmarkDefinition is null')
        totalCount = nullTipRefs.size()
        for( TIPReference tipRef : nullTipRefs ){
            TrustInteroperabilityProfile.withTransaction {
                try {
                    log.debug("Processing TIPReference[${tipRef.id}], with Notes = [${tipRef.notes}]...")
                    if (StringUtils.isNotBlank(tipRef.notes)) {
                        String link = tipRef.notes.trim()
                        if (link.startsWith("TIP: ")) {
                            link = link.substring(4).trim()
                            tipRef.trustInteroperabilityProfile = TrustInteroperabilityProfile.findByUri(link)
                            if (tipRef.trustInteroperabilityProfile == null)
                                throw new UnsupportedOperationException("For TIPRef[${tipRef.id}], could not locate any TIP with URI = ${link}")
                        } else if (link.startsWith("TD: ")) {
                            link = link.substring(3).trim()
                            tipRef.trustmarkDefinition = TrustmarkDefinition.findByUri(link)
                            if (tipRef.trustmarkDefinition == null)
                                throw new UnsupportedOperationException("For TIPRef[${tipRef.id}], could not locate any TD with URI = ${link}")
                        }
                        tipRef.notes = ""
                        log.debug("  Updating TIP Reference[${tipRef.referenceName}] on TIP[${tipRef.owningTIP.id}: ${tipRef.owningTIP.name}]")
                        tipRef.save(failOnError: true)
                    } else {
                        throw new UnsupportedOperationException("A Null TIPReference object[${tipRef.id}] cannot have notes empty as well.")
                    }
                    successCount++
                } catch (Throwable t) {
                    log.error("Unable to update TIP: " + tipRef.id + "!", t)
                    errorCount++
                }
            }
        }

        log.info("While cleaning ${totalCount} TIP references, found ${errorCount} errors and successfully processed ${successCount}!")

    }//end cleanUpTipReferences.

    /**
     * Uses the TrustmarkFrameworkService to retrieve all remote TD objects based on the index listing.
     */
    private List<RemoteTrustInteroperabilityProfile> readAllRemoteTips(){
        List<RemoteTrustInteroperabilityProfile> remoteTips = []
        Page<RemoteTrustInteroperabilityProfile> tipPage = this.service.listTrustInteroperabilityProfiles()
        boolean shouldContinue = true
        while( shouldContinue ){
            remoteTips.addAll(tipPage.objects)
            if( tipPage.hasNext() )
                tipPage = tipPage.next()
            else
                shouldContinue = false
        }
        return remoteTips
    }


    /**
     * Checks the last cache date for TIPs against the server's most recent TIP date to detect any changes.
     */
    private boolean hasTipChanges() {
        boolean hasChanges = true
        String value = SystemVariable.quickFindPropertyValue("${TIP_CACHE_DATE_PROP_PREFIX}{"+this.url+"}")
        if( StringUtils.isNotBlank(value) ){
            Long lastCacheDate = Long.parseLong(value)
            Long mostRecentTipDate = this.remoteStatus.getMostRecentTrustInteroperabilityProfileDate().getTime()
            hasChanges = mostRecentTipDate > lastCacheDate
        }
        return hasChanges
    }//end hasTipChanges()

    private File serialize(edu.gatech.gtri.trustmark.v1_0.model.TrustInteroperabilityProfile tip){
        SerializerFactory serializerFactory = FactoryLoader.getInstance(SerializerFactory.class)
        Serializer jsonSerializer = serializerFactory.getJsonSerializer()
        File tmpJsonFile = File.createTempFile("tip-", ".json")
        FileWriter tmpJsonFileWriter = new FileWriter(tmpJsonFile, false)
        jsonSerializer.serialize(tip, tmpJsonFileWriter)
        tmpJsonFileWriter.flush()
        tmpJsonFileWriter.close()
        return tmpJsonFile
    }

    //==================================================================================================================
    //  Trustmark Definition Methods
    //==================================================================================================================
    private boolean sameTimestamp(Date d1, Date d2){
        long diff = d2.getTime() - d1.getTime()
        if( Math.abs(diff) > 2000 ){ // 2016-11-10 - SBL - For some reason I was getting a -1000 (one second) difference in some of my comparisons (when I shouldn't have), so I made it more forgiving.
            return false
        }
        return true
    }


    private void scanForTDChanges(){
        if( hasTdChanges() ){
            log.debug("Loading TD Changes from URL[$url]...")
            List<RemoteTrustmarkDefinition> remoteTdList = readAllRemoteTds()
            log.debug("Successfully read ${remoteTdList.size()} trustmark definitions...")

            for( int i = 0; i <remoteTdList.size(); i++ ){
                RemoteTrustmarkDefinition remoteTd = remoteTdList.get(i)
                TrustmarkDefinition databaseTd = TrustmarkDefinition.findByUri(remoteTd.identifier.toString())
                if( databaseTd != null ){
                    TrustmarkDefinition.withTransaction {
                        if (databaseTd.cachedUrl == null) {
                            databaseTd.cachedUrl = remoteTd.getFormats()?.get("json")?.toString()
                            databaseTd.save(failOnError: true)
                        }

                        if (sameTimestamp(databaseTd.getPublicationDateTime(), remoteTd.getPublicationDateTime())) {
                            log.debug("No change detected for TD[${remoteTd.getIdentifier().toString()}]")
                        } else {
                            log.info("TD[${databaseTd.getUri()}] is out of date [diff: ${remoteTd.getPublicationDateTime().getTime() - databaseTd.getPublicationDateTime().getTime()}].  A newer one has been detected.")
                            // Now we need to perform a full Diff on these TDs to find out what changed, and whether we can tolerate the change.
                            databaseTd.setEnabled(false)
                            databaseTd.setOutOfDate(true)
                            databaseTd.save(failOnError: true)
                        }
                    }
                }else{
                    importTdClean(remoteTd)
                    SystemVariable.storeProperty(MESSAGE_VAR, "Successfully downloaded & cached Trustmark Definition: "+remoteTd.getName()+", v"+remoteTd.getVersion())
                }

                if( i % 50 == 0 ) {
                    cleanUpGorm()
                    log.info("Finished processing "+((int) Math.floor( ((double) i/(double) remoteTdList.size()) * 100.0d))+"% of TDs!")
                }

                int percent = (int) Math.floor (((double) i / (double) remoteTdList.size()) * 100.0d)
                SystemVariable.storeProperty(PERCENT_VAR, ""+percent)
            }
            SystemVariable.storeProperty("${TD_CACHE_DATE_PROP_PREFIX}{"+this.url+"}", System.currentTimeMillis()+""); // Update the cache date.
        }else{
            log.debug("The TDs have not changed at ${this.url}")
        }
    }//end scanForTDChanges()


    /**
     * Attempts to download the full data for the given TrustmarkDefinition, and then write it to the database -
     * assumes the TD does NOT already exist in the database.
     */
    private void importTdClean(RemoteTrustmarkDefinition remoteTd ){
        TrustmarkDefinition.withTransaction {
            try {
                log.debug("Cleanly importing RemoteTD[${remoteTd.identifier}]...")
                TrustmarkDefinitionResolver resolver = FactoryLoader.getInstance(TrustmarkDefinitionResolver.class)

                URL jsonURL = remoteTd.getFormats().get("json")
                if (jsonURL == null)
                    throw new UnsupportedOperationException("Cannot download TD[${remoteTd.identifier}], as JSON is not a supported format.")

                log.debug("Getting URL[${jsonURL}]...")
                edu.gatech.gtri.trustmark.v1_0.model.TrustmarkDefinition tdDefinition = resolver.resolve(jsonURL)

                log.debug("Saving TD[${tdDefinition.getMetadata().getName()}, ${tdDefinition.getMetadata().getVersion()}]")
                TrustmarkDefinition databaseTd = new TrustmarkDefinition()
                databaseTd.setBaseUri(this.url)
                databaseTd.setUri(tdDefinition.getMetadata().getIdentifier().toString())
                databaseTd.cachedUrl = remoteTd.getFormats()?.get("json")?.toString()
//              This field has been removed.
//                databaseTd.setReferenceAttributeName(tdDefinition?.getMetadata()?.getTrustmarkReferenceAttributeName()?.toString())
                databaseTd.setName(tdDefinition.getMetadata().getName())
                databaseTd.setTdVersion(tdDefinition.getMetadata().getVersion())
                databaseTd.setDescription(tdDefinition.getMetadata().getDescription())
                databaseTd.setPublicationDateTime(tdDefinition.getMetadata().getPublicationDateTime())
                if (StringUtils.isNotBlank(tdDefinition.getAssessmentStepPreface()))
                    databaseTd.setAssessmentPreface(tdDefinition.getAssessmentStepPreface())
                if (StringUtils.isNotBlank(tdDefinition.getConformanceCriteriaPreface()))
                    databaseTd.setCriteriaPreface(tdDefinition.getConformanceCriteriaPreface())
                if( tdDefinition.getMetadata().isDeprecated() != null )
                    databaseTd.deprecated = tdDefinition.getMetadata().isDeprecated()
                else
                    databaseTd.deprecated = false
                if( tdDefinition.getMetadata().getSupersedes() != null )
                    databaseTd.setSupersedesList(tdDefinition.getMetadata().getSupersedes() as List)
                if( tdDefinition.getMetadata().getSupersededBy() != null )
                    databaseTd.setSupersededByList(tdDefinition.getMetadata().getSupersededBy() as List)

                File jsonFile = serialize(tdDefinition)
                BinaryObject binaryObject = this.fileService.createBinaryObject(jsonFile, ScanHostJob.class.getName(), "application/json", "trustmark-definition.json", "json")
                databaseTd.setSource(binaryObject)
                databaseTd.setSignature(getSha1Sum(jsonFile))

                databaseTd.save(failOnError: true)

                for( edu.gatech.gtri.trustmark.v1_0.model.ConformanceCriterion criterionDef : tdDefinition.getConformanceCriteria() ){
                    ConformanceCriterion databaseCrit = new ConformanceCriterion(trustmarkDefinition: databaseTd)
                    databaseCrit.setCritNumber(criterionDef.getNumber())
                    databaseCrit.setName(criterionDef.getName())

                    String citerionDescription = HtmlUtils.inserTargetAttribToAnchorTag(criterionDef.getDescription())
                    databaseCrit.setDescription(citerionDescription)

                    databaseCrit.save(failOnError: true)
                    databaseTd.addToCriteria(databaseCrit)
                    databaseTd.save(failOnError: true)

                    for( edu.gatech.gtri.trustmark.v1_0.model.Citation citationDef : criterionDef.getCitations() ){
                        Citation citation = new Citation(source: citationDef.getSource().getIdentifier(), description: citationDef.getDescription())
                        citation.save(failOnError: true)
                        databaseCrit.addToCitations(citation)
                        databaseCrit.save(failOnError: true)
                    }

                }

                for( edu.gatech.gtri.trustmark.v1_0.model.AssessmentStep stepDef : tdDefinition.getAssessmentSteps() ){
                    AssessmentStep databaseStep = new AssessmentStep(trustmarkDefinition: databaseTd)
                    databaseStep.setIdentifier(stepDef.getId())
                    databaseStep.setStepNumber(stepDef.getNumber())
                    databaseStep.setName(stepDef.getName())

                    String assessmentStepDescription = HtmlUtils.inserTargetAttribToAnchorTag(stepDef.getDescription())
                    databaseStep.setDescription(assessmentStepDescription)

                    databaseStep.save(failOnError: true)
                    databaseTd.addToAssessmentSteps(databaseStep)
                    databaseTd.save(failOnError: true)

                    for(edu.gatech.gtri.trustmark.v1_0.model.ConformanceCriterion critRef : stepDef.getConformanceCriteria() ){
                        ConformanceCriterion critFromDb = criterionFromTD(databaseTd, critRef)
                        databaseStep.addToCriteria(critFromDb)
                        databaseStep.save(failOnError: true)
                    }

                    for( edu.gatech.gtri.trustmark.v1_0.model.Artifact artifactDef : stepDef.getArtifacts() ){
                        AssessmentStepArtifact artifact = new AssessmentStepArtifact(assessmentStep: databaseStep)
                        artifact.setName(artifactDef.getName())

                        String assessmentStepArtifactDescription = HtmlUtils.inserTargetAttribToAnchorTag(artifactDef.getDescription())
                        artifact.setDescription(assessmentStepArtifactDescription)

                        artifact.save(failOnError: true)
                        databaseStep.addToArtifacts(artifact)
                        databaseStep.save(failOnError: true)
                    }

                    if( stepDef.getParameters() != null && stepDef.getParameters().size() > 0 ) {
                        for (TrustmarkDefinitionParameter parameter : stepDef.getParameters()) {
                            TdParameter dbParam = new TdParameter(databaseStep, parameter)
                            dbParam.save(failOnError: true)
                            databaseStep.addToParameters(dbParam)
                            databaseStep.save(failOnError: true)
                        }
                    }

                }

                databaseTd.save(failOnError: true, flush: true)

                log.info("Successfully imported TD[${remoteTd.identifier.toString()}] cleanly.")
            } catch (Throwable t) {
                log.error("Unable to import RemoteTD[${remoteTd.identifier.toString()}]!", t)
                ErrorLogMessage.quickAdd("ScanHostJob.importTdClean()", "Unexpected error while importing TD[${remoteTd.getIdentifier().toString()}]: "+t.toString(), t)
            }
        }
    }//end importTdClean()

    private ConformanceCriterion criterionFromTD(TrustmarkDefinition td, edu.gatech.gtri.trustmark.v1_0.model.ConformanceCriterion critDef){
        ConformanceCriterion crit = null
        for( ConformanceCriterion current : td.getCriteria() ){
            if( current.getCritNumber().equals(critDef.getNumber()) ) {
                crit = current
                break
            }
        }
        return crit
    }

    private File serialize(edu.gatech.gtri.trustmark.v1_0.model.TrustmarkDefinition td){
        SerializerFactory serializerFactory = FactoryLoader.getInstance(SerializerFactory.class)
        Serializer jsonSerializer = serializerFactory.getJsonSerializer()
        File tmpJsonFile = File.createTempFile("td-", ".json")
        FileWriter tmpJsonFileWriter = new FileWriter(tmpJsonFile, false)
        jsonSerializer.serialize(td, tmpJsonFileWriter)
        tmpJsonFileWriter.flush()
        tmpJsonFileWriter.close()
        return tmpJsonFile
    }

    /**
     * Uses the TrustmarkFrameworkService to retrieve all remote TD objects based on the index listing.
     */
    private List<RemoteTrustmarkDefinition> readAllRemoteTds(){
        List<RemoteTrustmarkDefinition> remoteTds = []
        Page<RemoteTrustmarkDefinition> tdPage = this.service.listTrustmarkDefinitions()
        boolean shouldContinue = true
        while( shouldContinue ){
            remoteTds.addAll(tdPage.objects)
            if( tdPage.hasNext() )
                tdPage = tdPage.next()
            else
                shouldContinue = false
        }
        return remoteTds
    }

    /**
     * Checks the last cache date for TDs against the server's most recent TD date to detect any changes.
     */
    private boolean hasTdChanges() {
        boolean hasChanges = true
        String value = SystemVariable.quickFindPropertyValue("${TD_CACHE_DATE_PROP_PREFIX}{"+this.url+"}")
        if( StringUtils.isNotBlank(value) ){
            Long lastCacheDate = Long.parseLong(value)
            Long mostRecentTDDate = this.remoteStatus.getMostRecentTrustmarkDefinitionDate().getTime()
            hasChanges = mostRecentTDDate > lastCacheDate
        }
        return hasChanges
    }//end hasTdChanges()


}/* End tmf.host.ScanConfigJob */

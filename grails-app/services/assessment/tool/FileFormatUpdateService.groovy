package assessment.tool

import edu.gatech.gtri.trustmark.v1_0.FactoryLoader
import edu.gatech.gtri.trustmark.v1_0.io.HttpResponse
import edu.gatech.gtri.trustmark.v1_0.io.NetworkDownloader
import edu.gatech.gtri.trustmark.v1_0.io.TrustInteroperabilityProfileResolver
import edu.gatech.gtri.trustmark.v1_0.io.TrustmarkDefinitionResolver
import edu.gatech.gtri.trustmark.v1_0.service.RemoteTrustmarkDefinition
import nstic.web.BinaryObject
import nstic.web.ScanHostJob
import nstic.web.SystemVariable
import nstic.web.td.TrustmarkDefinition
import nstic.web.tip.TrustInteroperabilityProfile
import org.apache.commons.codec.binary.Base64
import org.json.JSONObject

import java.security.MessageDigest

/**
 * This service will scan all TDs and TIPs, checking to make sure their format can be parsed.  If they fail to parse
 * from the locally cached copy, then the service attempts to download them.
 * <br/>
 * It is expected that this service be asynchronously called from a controller, and thus will establish it's own
 * database transactions.
 * <br/><br/>
 * @author brad
 * @date 11/14/16
 */
class FileFormatUpdateService {
    //==================================================================================================================
    //  VARIABLES
    //==================================================================================================================
    public static final String EXECUTING_VAR = FileFormatUpdateService.class.simpleName + ".EXECUTING";
    public static final String STATUS_VAR = FileFormatUpdateService.class.simpleName + ".STATUS";
    public static final String MESSAGE_VAR = FileFormatUpdateService.class.simpleName + ".MESSAGE";
    public static final String TIMESTAMP_VAR = FileFormatUpdateService.class.simpleName + ".TIMESTAMP";
    public static final String DATA_VAR = FileFormatUpdateService.class.simpleName + ".DATA";


    private Map data = [:]; // This is converted to JSON and stored in the DATA variable when desired.
    private List tds = [];
    private List tips = [];

    FileService fileService;
    //==================================================================================================================
    //  SERVICE METHODS
    //==================================================================================================================
    public void doFormatCheck() {
        init();

        try {
            updateStatus("LOCAL_COLLECT", "Finding all locally cached TDs and TIPs...");
            collectTdAndTipInfo();
            storeData();

            int count = 0;
            updateStatus("PROCESS_TDS", "Processing Trustmark Definitions(${data.tdsCount})...")
            for (Map td : this.tds ?: []) {
                log.debug("Processing TD: " + td.name + ", v." + td.version);
                updateSourceIfNecessary(td);
                data.lastProcessed = td;
                data.count = count++;
                storeData();
                checkStop();
            }

            count = 0;
            updateStatus("PROCESS_TIPS", "Processing Trust Interoperability Profiles(${data.tipsCount})...")
            for (Map tip : this.tips ?: []) {
                log.debug("Processing TIP: " + tip.name + ", v." + tip.version);
                updateSourceIfNecessary(tip);
                data.lastProcessed = tip;
                data.count = count++;
                storeData();
                checkStop();
            }

            updateStatus("COMPLETE", "Successfully updated all TD and TIP formats.");
        }catch(Throwable t){
            log.error("Error updating TD and TIP foramts!", t);
            data.errorText = t.getMessage();
            data.errorClass = t.getClass().getName();
            storeData();
            updateStatus("ERROR", "Error while updating formats: "+t.toString());
        }
        destroy();
    }//end doFormatCheck()

    //==================================================================================================================
    //  HELPER METHODS
    //==================================================================================================================
    /**
     * Checks the executing variable.  IF it is false, then some process requested that we immediately stop.
     */
    private void checkStop() {
        if( !getBooleanVariable(EXECUTING_VAR) ){
            log.error("Detected EXECUTING_VAR is set back to false, stopping...")
            throw new UnsupportedOperationException("The EXECUTING_VAR indicates the process should stop - forcibly terminating.");
        }
    }

    /**
     * If the remote object has not been updated, we just make sure we can still parse the local cache.  It's
     * possible that the local cache is not of the proper format, and we update it if we need to.
     * @param databaseTd
     */
    private void updateSourceIfNecessary(Map data){
        BinaryObject.withTransaction {
            BinaryObject source = BinaryObject.get(data.sourceId);
            if( !canReadInMemory(source, data.type) ){
                log.info("${data.type}[${data.name}, v${data.version}] needs to be updated [url: ${data.cachedUrl}]...");

                NetworkDownloader networkDownloader = FactoryLoader.getInstance(NetworkDownloader.class);
                HttpResponse response = networkDownloader.download(new URL(data.cachedUrl));

                if( response.getResponseCode() == 200 ){
                    String json = response.getContent();
                    File jsonFile = File.createTempFile("td-", ".json");
                    jsonFile << json;
                    BinaryObject binaryObject = this.fileService.createBinaryObject(jsonFile, ScanHostJob.class.getName(), "application/json", "trustmark-definition.json", "json");

                    if( data.type == "TD" ){
                        TrustmarkDefinition databaseTd = TrustmarkDefinition.get(data.id);
                        databaseTd.setSource(binaryObject);
                        databaseTd.setSignature(getSha1Sum(jsonFile));
                        databaseTd.save(failOnError: true);
                    }else{ // data.type == "TIP"
                        TrustInteroperabilityProfile databaseTip = TrustInteroperabilityProfile.get(data.id);
                        databaseTip.setSource(binaryObject);
                        databaseTip.setSignature(getSha1Sum(jsonFile));
                        databaseTip.save(failOnError: true);
                    }

                }else{
                    throw new UnsupportedOperationException("Cannot download ${data.cachedUrl}!  Received Response[${response.responseCode}]: "+response.getResponseMessage())
                }

            }
        }
//
//
//        if( !canReadTdInMemory(databaseTd.source) ){
//            URL jsonURL = remoteTd.getFormats().get("json");
//            if (jsonURL == null)
//                throw new UnsupportedOperationException("Cannot download TD[${remoteTd.identifier}], as JSON is not a supported format.");
//
//            log.warn("Couldn't read TD[${databaseTd.getUri()}] into memory, downloading and updating from ${jsonURL}...")
//            NetworkDownloader networkDownloader = FactoryLoader.getInstance(NetworkDownloader.class);
//            HttpResponse response = networkDownloader.download(jsonURL);
//            if( response.getResponseCode() == 200 ){
//                String json = response.getContent();
//                File jsonFile = File.createTempFile("td-", ".json");
//                jsonFile << json;
//                BinaryObject binaryObject = this.fileService.createBinaryObject(jsonFile, ScanHostJob.class.getName(), "application/json", "trustmark-definition.json", "json");
//                databaseTd.setSource(binaryObject);
//                databaseTd.setSignature(getSha1Sum(jsonFile));
//                databaseTd.save(failOnError: true);
//            }else{
//                throw new UnsupportedOperationException("Cannot download ${jsonURL}!  Received Response[${response.responseCode}]: "+response.getResponseMessage())
//            }
//        }
    }

    private String getSha1Sum(File file){
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.reset();
        md.update(file.bytes);
        byte[] digest = md.digest()
        return new String(Base64.encodeBase64(digest));
    }

    private boolean canReadInMemory(BinaryObject binaryObject, String type) {
        File file = binaryObject.getContent().toFile();
        if( type == "TD" ) {
            TrustmarkDefinitionResolver resolver = FactoryLoader.getInstance(TrustmarkDefinitionResolver.class);
            try {
                resolver.resolve(file, true);
                return true;
            } catch (Throwable t) {
                log.warn("Unable to read TD: " + t.toString());
                return false;
            }
        }else { // TYPE IS TIP
            TrustInteroperabilityProfileResolver resolver = FactoryLoader.getInstance(TrustInteroperabilityProfileResolver.class);
            try{
                resolver.resolve(file, true);
                return true;
            }catch(Throwable t){
                log.warn("Unable to read TIP: "+t.toString());
                return false;
            }
        }
    }


    private collectTdAndTipInfo(){
        log.debug("Collecting all locally cached TDs...");
        TrustmarkDefinition.withTransaction {
            List<TrustmarkDefinition> tds = TrustmarkDefinition.findAll();
            if( tds && tds.size() > 0 ){
                for( TrustmarkDefinition td : tds ){
                    this.tds.add([
                            type: "TD",
                            id: td.id,
                            uri: td.uri,
                            cachedUrl: td.cachedUrl,
                            name: td.name,
                            version: td.tdVersion,
                            sourceId: td.source.id
                    ])
                }
                log.debug("Successfully found @|green ${tds.size()}|@ Trustmark definitions!");
                data.tdsCount = tds.size();
            }else{
                data.tdCount = 0;
            }
        }

        log.debug("Collecting all locally cached TIPs...");
        TrustInteroperabilityProfile.withTransaction {
            List<TrustInteroperabilityProfile> tips = TrustInteroperabilityProfile.findAll();
            if( tips && tips.size() > 0 ){
                for( TrustInteroperabilityProfile tip : tips ){
                    this.tips.add([
                            type: "TIP",
                            id: tip.id,
                            uri: tip.uri,
                            cachedUrl: tip.cachedUrl,
                            name: tip.name,
                            version: tip.tipVersion,
                            sourceId: tip.source.id
                    ])
                }
                log.debug("Successfully found @|green ${tips.size()}|@ Trust Interoperability Profiles!")
                data.tipsCount = tips.size();
            }else {
                data.tipsCount = 0;
            }
        }

    }


    private void init() {
        if( getBooleanVariable(EXECUTING_VAR) ){
            log.error("Cannot execute another ${FileFormatUpdateService.class.simpleName} because one is already running.");
            throw new UnsupportedOperationException("Cannot execute another ${FileFormatUpdateService.class.simpleName} because one is already running.  Please terminate that one before continuing.");
        }
        data = [:];
        tds = [];
        tips = [];
        putVariable(EXECUTING_VAR, "true");
        updateStatus("INIT", "File Format Update Service is starting up...");
        storeData();

        // TODO Other init work?

    }

    private void destroy() {
        putVariable(TIMESTAMP_VAR, Calendar.getInstance().getTime().toString());
        putVariable(EXECUTING_VAR, "false");
    }

    private void updateStatus(String status, String message){
        putVariable(STATUS_VAR, status);
        putVariable(MESSAGE_VAR, message);
    }

    private void storeData() {
        JSONObject jsonObject = new JSONObject(this.data);
        putVariable(DATA_VAR, jsonObject.toString());
    }

    private void putVariable(String name, Object value){
        SystemVariable.withTransaction {
            SystemVariable var = SystemVariable.findByName(name);
            if( var == null )
                var = new SystemVariable(name: name);
            var.setFieldValue(value.toString());
            var.save(failOnError: true, flush: true);
        }
    }

    private boolean getBooleanVariable(String name){
        Boolean value = null;
        SystemVariable.withTransaction {
            SystemVariable var = SystemVariable.findByName(name);
            if( var == null )
                value = false;
            else
                value = var.getBooleanValue();
        }
        return value;
    }

}

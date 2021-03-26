package nstic.web

import assessment.tool.FileFormatUpdateService
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import groovy.json.JsonSlurper
import org.apache.commons.lang.StringUtils

import javax.servlet.ServletException

@Secured("ROLE_ADMIN")
class TdAndTipUpdateController {
    //==================================================================================================================
    //  SERVICES
    //==================================================================================================================
    FileFormatUpdateService fileFormatUpdateService;
    //==================================================================================================================
    //  CONTROLLER METHODS
    //==================================================================================================================
    def index() { }


    def scanHostJobDetails() {
        Map jsonResponse = [:]
        log.debug("Calculating Scan Job Details...")
        jsonResponse.put("status", SystemVariable.quickFindPropertyValue(ScanHostJob.STATUS_VAR));
        String percentString = SystemVariable.quickFindPropertyValue(ScanHostJob.PERCENT_VAR);
        int percentInt = 0;
        if( StringUtils.isNotEmpty(percentString) ){
            percentInt = Integer.parseInt(percentString.trim());
        }
        jsonResponse.put("percent", percentInt);
        jsonResponse.put("message", SystemVariable.quickFindPropertyValue(ScanHostJob.MESSAGE_VAR));
        appendScanHostVars(jsonResponse);
        render jsonResponse as JSON;
    }

    def startScanHostJob() {
        log.debug("Starting the scan host job...");
        ScanHostJob.triggerNow([:]);
        Map jsonResponse = [status: 'SUCCESS', message: 'Successfully started the scan host job.'];
        render jsonResponse as JSON;
    }

    def clearScanHostVariables() {
        log.debug("Clearing the variables that control the scan host job...");
        removeScanHostVars();
        Map jsonResponse = [status: 'SUCCESS', message: 'Removed all scan host variables.'];
        render jsonResponse as JSON;
    }

    def startTdAndTipFormatCheck() {
        log.info("Starting TD and TIP Format Check...")

        final FileFormatUpdateService fService = this.fileFormatUpdateService;
        Thread t = new Thread({
            fService.doFormatCheck();
        } as Runnable);
        t.setName("FORMAT_CHECK_UPDATE_THREAD");
        t.start();

        Map jsonResponse = [status: 'SUCCESS', message: 'Successfully started TD and TIP format check.'];
        render jsonResponse as JSON;
    }

    def clearFormatCheckExecutingVar() {
        log.warn("Forcibly making the variable[${FileFormatUpdateService.EXECUTING_VAR}] = false ...")
        SystemVariable executingVar = SystemVariable.findByName(FileFormatUpdateService.EXECUTING_VAR);
        SystemVariable.withTransaction {
            if (executingVar) {
                executingVar.fieldValue = "false";
                executingVar.save(failOnError: true);
            }
        }

        Map jsonResponse = [status: 'SUCCESS', message: 'Successfully reset executing var.'];
        render jsonResponse as JSON;
    }

    def formatCheck() {
        log.debug("Checking Format Check Status (and returning JSON)...");

        SystemVariable executingVar = SystemVariable.findByName(FileFormatUpdateService.EXECUTING_VAR);
        SystemVariable statusVar = SystemVariable.findByName(FileFormatUpdateService.STATUS_VAR);
        SystemVariable messageVar = SystemVariable.findByName(FileFormatUpdateService.MESSAGE_VAR);
        SystemVariable tsVar = SystemVariable.findByName(FileFormatUpdateService.TIMESTAMP_VAR);
        SystemVariable dataVar = SystemVariable.findByName(FileFormatUpdateService.DATA_VAR);

        Map json = [:]
        json.put("executing", executingVar?.booleanValue ?: false);
        json.put("status", statusVar?.stringValue ?: "UNKNOWN");
        json.put("timestamp", tsVar?.stringValue ?: "---");
        json.put("message", messageVar?.stringValue ?: "");
        String dataJson = dataVar?.stringValue ?: "{}";
        json.put("data", new JsonSlurper().parseText(dataJson));

        render json as JSON;
    }

    def saveRegistry() {

        if (!validateRegistryUrl(params.registryUrl)) {
            throw new ServletException("Not a valid registry URL: ${params.registryUrl}")
        }

        Registry registry = new Registry()
        registry.registryUrl = params.registryUrl
        registry.name = params.registryName
        registry.lastUpdated = new Date()
        registry.save(failOnError: true)

        def resultJSON = [
            registryName: registry.name,
            registryUrl: registry.registryUrl,
        ]

        render resultJSON as JSON
    }

    //==================================================================================================================
    //  HELPER METHODS
    //==================================================================================================================
    private void removeScanHostVars() {
        List<SystemVariable> vars = SystemVariable.findAll();
        List<String> varsToDelete = []
        for( SystemVariable var : vars ){
            if( var.getName().startsWith("TD_CACHE_DATE{") || var.getName().startsWith("TIP_CACHE_DATE{") ){
                varsToDelete.add(var.getName());
            }
        }

        SystemVariable.withTransaction {
            for (String name : varsToDelete) {
                log.warn("Removing variable: " + name);
                SystemVariable var = SystemVariable.findByName(name);
                var.delete(flush: true);
            }
        }
    }

    private void appendScanHostVars(Map map){
        List tdCacheDates = []
        List tipCacheDates = []
        List<SystemVariable> vars = SystemVariable.findAll();
        for( SystemVariable var : vars ){
            if( var.getName().startsWith("TD_CACHE_DATE{") ){
                String sourceName = var.getName();
                sourceName = sourceName.replace("TD_CACHE_DATE{", "");
                sourceName = sourceName.replace("}", "");
                tdCacheDates.add([ source: sourceName, prettyTime: new Date(var.getNumericValue().longValue()).toString(), timestamp: var.getNumericValue().longValue()]);
            }else if( var.getName().startsWith("TIP_CACHE_DATE{") ){
                String sourceName = var.getName();
                sourceName = sourceName.replace("TIP_CACHE_DATE{", "");
                sourceName = sourceName.replace("}", "");
                tipCacheDates.add(
                        [
                                source: sourceName,
                                prettyTime : new Date(var.getNumericValue().longValue()).toString(),
                                timestamp: var.getNumericValue().longValue()
                        ]
                );
            }
        }
        map.put("tdCacheDates", tdCacheDates);
        map.put("tipCacheDates", tipCacheDates);
    }

    private boolean validateRegistryUrl(String registryUrl) {

        log.debug("Validating registry url: ${registryUrl}...")

        URL url = null;
        try{
            url = new URL(registryUrl)

            log.debug("Valid registry url: ${registryUrl}...")

            return true
        }catch(Throwable t){
            log.debug("Invalid registry url: ${registryUrl}...")
            return false;
        }
    }
}

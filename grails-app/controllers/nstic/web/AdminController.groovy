package nstic.web

import assessment.tool.ImportExportService
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import nstic.TATPropertiesHolder
import org.apache.commons.lang.StringUtils

import javax.servlet.ServletException
import java.text.SimpleDateFormat

import edu.gatech.gtri.trustmark.v1_0.impl.util.TrustmarkMailClientImpl

@Secured("ROLE_ADMIN")
class AdminController {

    def springSecurityService
    ImportExportService importExportService

    /**
     * Called to clean the SystemVariable table of all export thread data.  Note that if a thread is actively executing,
     * then it will leave it's data alone.  It only deletes those with STATUS = "FINISHED SUCCESSFULLY".  Do note, however,
     * that it will reset the database value and the currently executing thread will just be "lost".
     */
    def reapExportThreadData() {
        log.info("Removing export thread data...")
        List<String> pastThreads = []
        List<SystemVariable> exportData = SystemVariable.findAllByNameLike("EXPORT_%");
        if( exportData && !exportData.isEmpty() ){
            for( SystemVariable sysVar : exportData ){
                String threadName = sysVar.name.split("\\.")[0];
                if( !pastThreads.contains(threadName) ){
                    log.debug('Adding export var: '+threadName);
                    pastThreads.add(threadName);
                }
            }
        }

        List<String> threadsToDelete = []
        for( String threadName : pastThreads ){
            String status = SystemVariable.quickFindPropertyValue(threadName+".STATUS");
            if(!status || status.equalsIgnoreCase("FINISHED SUCCESSFULLY") ){
                threadsToDelete.add(threadName);
            }else{
                log.debug("WARNING: '${threadName}' is not finished yet!")
            }
        }

        int deleted = 0;
        for( String threadToDelete : threadsToDelete ){
            List<SystemVariable> vars = SystemVariable.findAllByNameLike(threadToDelete+"%");
            for( SystemVariable var : vars ) {
                log.debug("Removing system variable: "+var.name+" = "+var.fieldValue);
                SystemVariable.withTransaction {
                    var.delete();
                }
                deleted++;
            }
        }

        SystemVariable.storeProperty(ImportExportService.EXPORT_THREAD_RUNNING, "false");
        SystemVariable.storeProperty(ImportExportService.EXPORT_THREAD_NAME, "");

        def responseMap = [status: 'SUCCESS', message: "Successfully removed ${deleted} system variables related to old exports.", deleted: deleted]
        if( response.format == 'json' ) {
            return render(contentType: 'application/json', text: responseMap as JSON)
        }else{
            throw new UnsupportedOperationException("Only JSON responses are acceptable.")
        }

    }//end reapExportThreadData()

    /**
     * Displays the import export page to the user.
     */
    def importExportView() {
        log.debug("Showing the user the import/export page...")
    }//end importExportView()

    /**
     * Initializes and kicks off the export thread.
     */
    def startExport(){
        log.info("Starting export[response.format = ${response.format}]...");
        def responseMap = [:]

        try{
            String exportThreadVal = SystemVariable.quickFindPropertyValue(ImportExportService.EXPORT_THREAD_RUNNING);
            if( exportThreadVal != null && exportThreadVal.equalsIgnoreCase("true") ){
                // We do not need to start a new export thread, because one is already running.
                String threadName = SystemVariable.quickFindPropertyValue(ImportExportService.EXPORT_THREAD_NAME);
                log.warn("An export thread["+threadName+"] is already running, returning that information...");
                responseMap = [
                        status: 'SUCCESS',
                        message: 'An already executing thread was found, so no new thread was started.',
                        threadName: threadName,
                        started: false,
                        alreadyStarted: true
                ]
            }else{
                log.debug("No running thread was found, creating one...");
                SystemVariable.storeProperty(ImportExportService.EXPORT_THREAD_RUNNING, "true");
                String threadName = "EXPORT_" + System.currentTimeMillis();
                final ImportExportService fImportExportService = this.importExportService
                Runnable runnable = new Runnable() {
                    @Override
                    void run() {
                        fImportExportService.doFullExport();
                    }
                }
                Thread fullExportThread = new Thread(runnable);
                fullExportThread.setName(threadName);
                fullExportThread.start();
                SystemVariable.storeProperty(ImportExportService.EXPORT_THREAD_NAME, threadName);
                responseMap = [
                        status: 'SUCCESS',
                        message: 'Successfully started a new export process.',
                        threadName: threadName,
                        started: true,
                        alreadyStarted: false
                ]
                log.debug("Successfully started a new export thread: "+threadName);
            }
        }catch(Throwable t){
            log.error("Error starting an export thread!", t);
            responseMap = [
                    status: 'ERROR',
                    message: t.getMessage(),
                    fullMessage: t.toString()
            ]
        }

        if( response.format == 'json' ) {
            return render(contentType: 'application/json', text: responseMap as JSON)
        }else{
            throw new UnsupportedOperationException("Only JSON responses are acceptable.")
        }
    }//end startExport()

    /**
     * Polled method from the client to check the long running export thread's status.
     */
    def checkExportStatus() {
        String threadName = params.threadName;
        log.info("Checking export thread status[${threadName}]...");
        def responseMap = [:]

        if( StringUtils.isBlank(threadName) ){
            log.error("Missing required parameter: "+threadName);
            responseMap = [status: 'ERROR', message: 'threadName is a required parameter, but was not given', executionStatus: 'DONE']
        }else{
            String statusVal = SystemVariable.quickFindPropertyValue(threadName+".STATUS")
            if( StringUtils.isNotBlank(statusVal) ){
                String progressValStr = SystemVariable.quickFindPropertyValue(threadName+".PROGRESS") // a number between 0 and 100
                responseMap = [
                        status: 'SUCCESS',
                        message: SystemVariable.quickFindPropertyValue(threadName+".MESSAGE") ?: "",
                        executionStatus: statusVal,
                        errorDetails: SystemVariable.quickFindPropertyValue(threadName+".ERROR_DETAILS") ?: "",
                        progress: Integer.parseInt(progressValStr)
                ]
            }else{
                log.warn("Export status not found!  This means there is no such thread[${threadName}] executing.")
                responseMap = [
                        status: 'ERROR',  message: 'There is no such export thread \''+threadName+'\' executing.', executionStatus: 'DONE'
                ]
            }
        }

        log.info("Returning status query: \n   STATUS: ${responseMap.status}\n   EXECUTION STATUS: ${responseMap.executionStatus}\n   PROGRESS:   ${responseMap.progress}\n   MESSAGE: ${responseMap.message}")
        if( response.format == 'json' ) {
            return render(contentType: 'application/json', text: responseMap as JSON)
        }else{
            throw new UnsupportedOperationException("Only JSON responses are acceptable.")
        }
    }//end checkExportStatus()

    def downloadExportZip(){
        String threadName = params.threadName;
        log.info("Downloading export thread for [${threadName}]...");
        if( StringUtils.isBlank(threadName) ){
            log.error("Missing required parameter: "+threadName);
            throw new ServletException("Missing a required parameter: "+threadName);
        }

        String status = SystemVariable.quickFindPropertyValue(threadName+".STATUS");
        if( StringUtils.isBlank(status) )
            throw new ServletException("No status found for thread '"+threadName+"'!");

        if( status != "FINISHED SUCCESSFULLY" )
            throw new ServletException("Found thread, but the status[${status}] is not FINISHED SUCCESSFULLY")

        String filePath = SystemVariable.quickFindPropertyValue(threadName+".FILE");
        if( StringUtils.isBlank(filePath) )
            throw new ServletException("Could not find any file data for thread "+threadName);

        return render(file: new FileInputStream(filePath), contentType: 'application/zip', fileName: "export-${getDateTimeStamp()}.zip")
    }

    /**
     * Polled method from the client to check the long running export thread's status.
     */
    def checkScanConfigStatus() {
        String statusVal = SystemVariable.quickFindPropertyValue(ScanHostJob.STATUS_VAR);
        Integer percent = Integer.parseInt(SystemVariable.quickFindPropertyValue(ScanHostJob.PERCENT_VAR));
        String messageVal = SystemVariable.quickFindPropertyValue(ScanHostJob.MESSAGE_VAR);

        def responseMap = [
                status: statusVal,
                progress: percent,
                message: messageVal
        ]


        log.info("Returning ScanHostJob status query: \n   STATUS: ${responseMap.status}\n   PROGRESS:   ${responseMap.progress}\n   MESSAGE: ${responseMap.message}")
        if( response.format == 'json' ) {
            return render(contentType: 'application/json', text: responseMap as JSON)
        }else{
            throw new UnsupportedOperationException("Only JSON responses are acceptable.")
        }
    }//end checkScanConfigStatus()

    private String getDateTimeStamp() {
        return new SimpleDateFormat("yyyyMMdd").format(Calendar.getInstance().getTime());
    }

}//end AdminController
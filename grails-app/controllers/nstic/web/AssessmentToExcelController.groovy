package nstic.web

import grails.converters.JSON
import grails.gorm.transactions.Transactional
import groovy.json.JsonSlurper
import nstic.web.assessment.Assessment
import org.apache.commons.lang.StringUtils
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import javax.servlet.ServletException
import java.text.SimpleDateFormat

@PreAuthorize('hasAnyAuthority("tat-contributor", "tat-admin")')
@Transactional
class AssessmentToExcelController {

    def fileService;

    def assessmentToExcelService

    /**
     * Loads the substep resolution index page.
     */
    def index() {
        User user = User.findByUsername(((OAuth2AuthenticationToken) SecurityContextHolder.getContext().getAuthentication()).getName());
        log.info("User[$user] is viewing the substep to excel generation page...");

        Organization org = Organization.findById(params.id);

        if( !org ) {
            log.error("No such organization: ${params.id}")
            throw new ServletException("No such organization: ${params.id}")
        }

        [organization: org]
    }

    def assessmentListing() {
        User user = User.findByUsername(((OAuth2AuthenticationToken) SecurityContextHolder.getContext().getAuthentication()).getName());
        log.info("User[$user] is viewing the list of assessments...");

        def organizationId = Long.parseLong(params.organizationId)
        Organization organization = Organization.get(organizationId)

        def assessmentJsonList = []

        Assessment.findAllByAssessedOrganization(organization).each { Assessment a ->
            assessmentJsonList.add( a.toJsonMap() )
        }
        int assessmentsCount = assessmentJsonList.size()

        def responseJson = ['status': 'SUCCESS', 'assessmentCount': assessmentsCount]

        responseJson.put("assessmentList", assessmentJsonList);

        withFormat {
            html { throw new UnsupportedOperationException("JSON is the only supported output format.") }
            xml { throw new UnsupportedOperationException("JSON is the only supported output format.") }
            json {
                render responseJson as JSON
            }
        }
    }

    /**
     * Generates the Assessment to Excel file as a binary, and then returns the binary id to what was generated so it can
     * be downloaded.
     */
    def excelGenerate() {
        User user = User.findByUsername(((OAuth2AuthenticationToken) SecurityContextHolder.getContext().getAuthentication()).getName())
        log.info("Generating an Excel Spreadsheet...");

        Long assessmentId = -1

        if( params.containsKey("id") ){
            assessmentId = Long.parseLong(params.id)
        }else{
            log.warn("Missing id parameter!");
            throw new UnsupportedOperationException("Missing required parameter 'id', which is a  single assessment id to generate.")
        }

        log.info("Generating from Id: $assessmentId");

        Assessment assessment = Assessment.get(assessmentId)
        if( assessment != null ) {
            log.debug("Found Assessment[$assessmentId]: ${assessment.assessmentName}")
        }else{
            log.warn("No such assessment: ${assessmentId}")
        }

        assessmentToExcelService.resetAttributes()

        String sessionId = session.getId()

        // Check if assessment to excel thread is running, excel report thread and wait for it to finish
        if (assessmentToExcelService.isExecuting(sessionId, AssessmentToExcelService.ASSESSMENT_TO_EXCEL_REPORT_EXECUTING_VAR)) {
            assessmentToExcelService.stopExecuting(sessionId, AssessmentToExcelService.ASSESSMENT_TO_EXCEL_REPORT_EXECUTING_VAR)

            log.debug("Interrupting previous excel report thread...")
            Object threadId = assessmentToExcelService.getAttribute(sessionId, AssessmentToExcelService.ASSESSMENT_TO_EXCEL_REPORT_THREAD_VAR)

            Thread t = threadId != null ? threadById(threadId as long) : null
            if (t &&  t.isAlive()) {
                assessmentToExcelService.setAttribute(sessionId, AssessmentToExcelService.ASSESSMENT_TO_EXCEL_REPORT_STATUS_VAR, "CANCELLING")
                assessmentToExcelService.setAttribute(sessionId, AssessmentToExcelService.ASSESSMENT_TO_EXCEL_REPORT_MESSAGE_VAR,
                        "Cancelling previous assessment to excel generation process...")
                assessmentToExcelService.removeAttribute(sessionId, AssessmentToExcelService.ASSESSMENT_TO_EXCEL_REPORT_THREAD_VAR)

                t.join()
                assessmentToExcelService.setAttribute(sessionId, AssessmentToExcelService.ASSESSMENT_TO_EXCEL_REPORT_STATUS_VAR, "CANCELLED")
                log.debug("Interrupted previous excel report thread...")
            }
        }


        // create params map for service
        final Map paramsMap = [:]
        paramsMap.put("assessmentId", assessment.id)
        paramsMap.put("sessionId", sessionId)
        paramsMap.put("username", user.username)

        assessmentToExcelService.setAttribute(sessionId, AssessmentToExcelService.ASSESSMENT_TO_EXCEL_REPORT_STATUS_VAR,
                "RUNNING")
        assessmentToExcelService.setAttribute(sessionId, AssessmentToExcelService.ASSESSMENT_TO_EXCEL_REPORT_MESSAGE_VAR,
                "Preprocessing assessment to excelreport for assessment ${assessment.assessmentName}...")

        assessmentToExcelService.setExecuting(sessionId, AssessmentToExcelService.ASSESSMENT_TO_EXCEL_REPORT_EXECUTING_VAR)

        Thread assessmentToExcelReportThread = new Thread(new Runnable() {
            @Override
            void run() {
                Assessment.withTransaction {
                    assessmentToExcelService.assessmentToExcelReport(paramsMap)
                }
            }
        })

        long assessmentToExcelReportThreadId = assessmentToExcelReportThread.getId()
        assessmentToExcelService.setAttribute(sessionId, AssessmentToExcelService.ASSESSMENT_TO_EXCEL_REPORT_THREAD_VAR,
                assessmentToExcelReportThreadId)
        assessmentToExcelReportThread.start()

        Map jsonResponse = [status: 'STARTING', message: 'Starting the assessment to excel report process.']

        render jsonResponse as JSON
    }

    private Thread threadById(Long id) {
        for (Thread t : Thread.getAllStackTraces().keySet()) {
            if (t.getId() == id) {
                return t;
            }
        }

        return null;
    }

    def renderAssessmentToExcelReport() {
        log.info("renderAssessmentToExcelReport...")
        User user = User.findByUsername(((OAuth2AuthenticationToken) SecurityContextHolder.getContext().getAuthentication()).getName())

        String sessionId = session.getId()

        def jsonSlurper = new JsonSlurper()
        def result = jsonSlurper.parseText(assessmentToExcelService.getAttribute(sessionId,
                AssessmentToExcelService.ASSESSMENT_TO_EXCEL_REPORT_RENDER_MODEL_VAR) as String)

        long assessmentId = Long.parseLong(result["assessmentId"] as String)

        Assessment assessment = Assessment.get(assessmentId)

        File excelFile = new File(result["canonicalPath"] as String);

        log.debug("Creating binary object...");

        String filename = assessment.assessedOrganization.name.replace(' ', '-').toLowerCase() + "_" +
                new SimpleDateFormat("yyyy-MM-dd").format(assessment.statusLastChangeDate).toLowerCase()

        BinaryObject excelBinary = fileService?.createBinaryObject(excelFile, user.username,
                'application/vnd.ms-excel', filename.toLowerCase() + '.xlsx', 'xlsx');

        Map jsonResponse = [status: 'STARTING', message: 'Generated Excel file.', binaryId: excelBinary.id]

        render jsonResponse as JSON
    }

    def initAssessmentToExcelReportState() {
        log.info("initAssessmentToExcelReportState...")
        User user = User.findByUsername(((OAuth2AuthenticationToken) SecurityContextHolder.getContext().getAuthentication()).getName())

        String sessionId = session.getId()

        assessmentToExcelService.setAttribute(sessionId, AssessmentToExcelService.ASSESSMENT_TO_EXCEL_REPORT_MESSAGE_VAR, "About to start the assessment to excel report process...")
        assessmentToExcelService.setAttribute(sessionId, AssessmentToExcelService.ASSESSMENT_TO_EXCEL_REPORT_STATUS_VAR, "RUNNING")
        assessmentToExcelService.setAttribute(sessionId, AssessmentToExcelService.ASSESSMENT_TO_EXCEL_REPORT_PERCENT_VAR, "0")

        assessmentToExcelService.setExecuting(sessionId, AssessmentToExcelService.ASSESSMENT_TO_EXCEL_REPORT_EXECUTING_VAR)

        Map jsonResponse = [status: 'SUCCESS', message: 'Successfully initialized the assessment to excel report process.']

        render jsonResponse as JSON
    }

    def assessmentToExcelReportStatusUpdate() {

        String sessionId = session.getId()

        Map jsonResponse = [:]
        jsonResponse.put("status", assessmentToExcelService.getAttribute(sessionId, AssessmentToExcelService.ASSESSMENT_TO_EXCEL_REPORT_STATUS_VAR))
        String percentString = assessmentToExcelService.getAttribute(sessionId, AssessmentToExcelService.ASSESSMENT_TO_EXCEL_REPORT_PERCENT_VAR)

        int percentInt = 0
        if( StringUtils.isNotEmpty(percentString) ){
            percentInt = Integer.parseInt(percentString.trim())
        }
        jsonResponse.put("percent", percentInt)
        jsonResponse.put("message", assessmentToExcelService.getAttribute(sessionId, AssessmentToExcelService.ASSESSMENT_TO_EXCEL_REPORT_MESSAGE_VAR))

        render jsonResponse as JSON
    }
}
package nstic.web

import edu.gatech.gtri.trustmark.v1_0.impl.model.TrustmarkParameterBindingImpl
import edu.gatech.gtri.trustmark.v1_0.model.ParameterKind
import grails.gorm.transactions.Transactional
import groovy.json.JsonBuilder
import nstic.web.assessment.Assessment
import nstic.web.assessment.AssessmentStepData
import nstic.web.td.AssessmentSubStep
import nstic.web.td.TrustmarkDefinition
import org.apache.commons.lang.StringUtils
import org.apache.poi.common.usermodel.Hyperlink
import org.apache.poi.common.usermodel.HyperlinkType
import org.apache.poi.hssf.util.HSSFColor
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.CreationHelper
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.ss.usermodel.RichTextString
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.VerticalAlignment
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.xssf.usermodel.XSSFFont
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.text.SimpleDateFormat

@Transactional(readOnly = true)
class AssessmentToExcelService {

    nstic.util.DualKeyConcurrentHashMap<nstic.util.DualKey, Object> objectMap = new nstic.util.DualKeyConcurrentHashMap<nstic.util.DualKey, Object>()

    // execution
    public static final String ASSESSMENT_TO_EXCEL_REPORT_EXECUTING_VAR = AssessmentToExcelService.class.simpleName + ".ASSESSMENT_TO_EXCEL_REPORT_EXECUTING"

    // messaging
    public static final String ASSESSMENT_TO_EXCEL_REPORT_STATUS_VAR = AssessmentToExcelService.class.getName() + ".ASSESSMENT_TO_EXCEL_REPORT_STATUS"
    public static final String ASSESSMENT_TO_EXCEL_REPORT_PERCENT_VAR = AssessmentToExcelService.class.getName() + ".ASSESSMENT_TO_EXCEL_REPORT_PERCENT"
    public static final String ASSESSMENT_TO_EXCEL_REPORT_MESSAGE_VAR = AssessmentToExcelService.class.getName() + ".ASSESSMENT_TO_EXCEL_REPORT_MESSAGE"

    public static final String ASSESSMENT_TO_EXCEL_REPORT_THREAD_VAR = AssessmentToExcelService.class.getName() + ".ASSESSMENT_TO_EXCEL_REPORT_THREAD"

    public static final String ASSESSMENT_TO_EXCEL_REPORT_RENDER_MODEL_VAR = AssessmentToExcelService.class.getName() + ".ASSESSMENT_TO_EXCEL_REPORT_RENDER_MODEL"

    public static final String ASSESSMENT_METADATA_SHEET_NAME   = "Assessment Metadata"
    public static final String ASSESSMENT_STEPS_SHEET_NAME      = "Assessment Steps"

    public static final String TD_NAME_HEADER                   = "TD Name"
    public static final String TD_URI_HEADER                    = "TD URI"
    public static final String STEP_NAME_HEADER                 = "Step Name"
    public static final String STEP_DESC_HEADER                 = "Step Desc"
    public static final String ARTIFACT_COUNT_HEADER            = "Artifact Count"
    public static final String STEP_STATUS_HEADER               = "Step Status"
    public static final String ASSESSOR_COMMENTS_HEADER         = "Assessor Comment"
    public static final String PARAMETERS_HEADER                = "Parameters"

    void setAttribute(String key1, String key2, Object value) {
        objectMap.put(new nstic.util.DualKey(key1, key2), value)
    }

    Object getAttribute(String key1, String key2) {
        return objectMap.get(new nstic.util.DualKey(key1, key2))
    }

    void removeAttribute(String key1, String key2) {
        objectMap.remove(new nstic.util.DualKey(key1, key2))
    }

    void resetAttributes() {
        objectMap.clear()
    }

    boolean isExecuting(String key, String property) {
        String value = getAttribute(key, property)
        if (StringUtils.isBlank(value)) {
            value = "false"
        }

        return Boolean.parseBoolean(value);
    }

    void setExecuting(String key, String property) {
        setAttribute(key, property, "true")
    }

    void stopExecuting(String key, String property) {
        setAttribute(key, property, "false");
    }

    @Transactional
    void assessmentToExcelReport(Map paramsMap) {

        log.debug("Processing assessment to excel report...");

        String sessionId = paramsMap.get("sessionId")
        String username = paramsMap.get("username")

        try {

            long assessmentId = paramsMap.get("assessmentId")

            Assessment assessment = Assessment.get(assessmentId)

            setAttribute(sessionId, ASSESSMENT_TO_EXCEL_REPORT_STATUS_VAR, "RUNNING")
            setAttribute(sessionId, ASSESSMENT_TO_EXCEL_REPORT_MESSAGE_VAR, "Preprocessing assessment to excel report for assessment ${assessment.assessmentName}...")

            // TODO: STATUS VAR?
            List<TrustmarkDefinition> tdList = []
            Assessment.withTransaction {
                assessment.tdLinks.forEach(tdLink -> {
                    tdList.add(tdLink.trustmarkDefinition)
                })
            }

            Workbook workbook = new XSSFWorkbook();

            setAttribute(sessionId, ASSESSMENT_TO_EXCEL_REPORT_MESSAGE_VAR, "Generating assessment metadata Excel sheet...")
            generateMetadata(workbook, assessment)

            setAttribute(sessionId, ASSESSMENT_TO_EXCEL_REPORT_MESSAGE_VAR, "Generating assessment steps Excel sheet...")
            generateSteps(workbook, assessment, sessionId)

            File tempOutputFile = File.createTempFile("td-dump_", ".xlsx");  // Writes out file
            FileOutputStream fout = new FileOutputStream(tempOutputFile);
            workbook.write(fout);
            fout.flush();
            fout.close();
            log.info("Successfully wrote file: ${tempOutputFile.canonicalPath}")

            log.debug("Writing response...");
            def responseJson = [
                     assessmentId: assessment.id,
                           status: 'SUCCESS',
                          message: 'Generated Excel File',
                    canonicalPath: tempOutputFile.canonicalPath
            ]

            // store in service attributes
            JsonBuilder jsonBuilder = new JsonBuilder(responseJson)
            setAttribute(sessionId, ASSESSMENT_TO_EXCEL_REPORT_RENDER_MODEL_VAR, jsonBuilder.toString())

            stopExecuting(sessionId, ASSESSMENT_TO_EXCEL_REPORT_EXECUTING_VAR)
            setAttribute(sessionId, ASSESSMENT_TO_EXCEL_REPORT_STATUS_VAR, "SUCCESS")
            setAttribute(sessionId, ASSESSMENT_TO_EXCEL_REPORT_MESSAGE_VAR, "Finished generating the assessment to excel report for assessment ${assessment.assessmentName}!")
        } catch (Throwable t) {
            log.error("An unexpected error occurred trying to generate the assessment to excel report: " + t.toString(), t)

            stopExecuting(sessionId, ASSESSMENT_TO_EXCEL_REPORT_EXECUTING_VAR)
            setAttribute(sessionId, ASSESSMENT_TO_EXCEL_REPORT_STATUS_VAR, "ERROR")
            setAttribute(sessionId, ASSESSMENT_TO_EXCEL_REPORT_MESSAGE_VAR, "An error has occurred. Please check the logs...")
        }
    }


    private boolean isCancelling(String sessionId) {
        return getAttribute(sessionId, ASSESSMENT_TO_EXCEL_REPORT_STATUS_VAR) == "CANCELLING"
    }

    private void generateSteps(Workbook workbook, Assessment assessment, String sessionId) {
        Sheet assessmentStepsSheet = workbook.createSheet(ASSESSMENT_STEPS_SHEET_NAME);

        CreationHelper createHelper = workbook.getCreationHelper();
        def fonts = createFonts(workbook);
        def cellStyles = createCellStyles(workbook, fonts);

        for( int i = 0; i < 20; i++ ) {
            assessmentStepsSheet.setColumnWidth(i, 15000);
        }
        int rowNum = 0;

        Row headerRow = assessmentStepsSheet.createRow(rowNum++);
        headerRow.setRowStyle(cellStyles.tdTitleRowCellStyle as CellStyle);
        def headerList =  [TD_NAME_HEADER, TD_URI_HEADER, STEP_NAME_HEADER, STEP_DESC_HEADER, ARTIFACT_COUNT_HEADER,
                           STEP_STATUS_HEADER, ASSESSOR_COMMENTS_HEADER, PARAMETERS_HEADER
        ];
        insertListToRow(workbook, assessmentStepsSheet, headerRow, cellStyles.tdTitleRowCellStyle as CellStyle, 0, headerList);

        Long currentTdId = -1
        long currentAssessmentStep = 0

        assessment.getSortedSteps().each { AssessmentStepData assStepData ->

            if (isCancelling(sessionId)) {
                return
            }

            TrustmarkDefinition td = assStepData.step.trustmarkDefinition

            Row nextStepRow = assessmentStepsSheet.createRow(rowNum++);
            nextStepRow.setRowStyle(cellStyles.assessmentStepRowCellStyle as CellStyle);

            def stepsToWrite = []
            if( assStepData.step.substeps && !assStepData.step.substeps.isEmpty() ){
                assStepData.step.substeps.each{ AssessmentSubStep substep ->
                    stepsToWrite.add( [assStepId: assStepData.step.id, name: substep.name, desc: substep.description, substepId: substep.id] );
                }
            }else{
                stepsToWrite.add( [assStepId: assStepData.step.id, name: assStepData.step.name, desc: assStepData.step.description, substepId: -1] );
            }

            for( int stepIndex = 0; stepIndex < stepsToWrite.size(); stepIndex++ ){

                if (currentTdId != td.id) {
                    currentTdId = td.id
                    Cell tdNameCell = nextStepRow.createCell(0);
                    tdNameCell.setCellStyle(cellStyles.stepDescCellStyle as CellStyle);
                    tdNameCell.setCellValue(td.name);
                    assessmentStepsSheet.autoSizeColumn(0);

                    Cell tdUriCell = nextStepRow.createCell(1);
                    Hyperlink link = createHelper.createHyperlink(HyperlinkType.URL) as Hyperlink;
                    link.setAddress(td.uri);
                    tdUriCell.setHyperlink(link as org.apache.poi.ss.usermodel.Hyperlink);
                    tdUriCell.setCellStyle(cellStyles.tdUriLinkCellStyle as CellStyle);
                    tdUriCell.setCellValue(td.uri);
                    assessmentStepsSheet.autoSizeColumn(1);
                }

                def step = stepsToWrite.get(stepIndex);

                Cell assStepNameCell = nextStepRow.createCell(2);
                assStepNameCell.setCellStyle(cellStyles.stepNameCellStyle as CellStyle);
                assStepNameCell.setCellValue(step.name);
                assessmentStepsSheet.autoSizeColumn(2);

                Cell assStepDescCell = nextStepRow.createCell(3);
                assStepDescCell.setCellStyle(cellStyles.stepDescCellStyle as CellStyle);
                assStepDescCell.setCellValue(step.desc);

                // Artifact Count
                Cell artifactCountCell = nextStepRow.createCell(4);
                artifactCountCell.setCellStyle(cellStyles.stepNameCellStyle as CellStyle);
                artifactCountCell.setCellValue(assStepData.step.artifacts.size());
                assessmentStepsSheet.autoSizeColumn(4);

                // Step response
                Cell stepResponse = nextStepRow.createCell(5);
                stepResponse.setCellStyle(cellStyles.stepNameCellStyle as CellStyle);
                stepResponse.setCellValue(assStepData.result.name);
                assessmentStepsSheet.autoSizeColumn(5);


                Cell assessorComment = nextStepRow.createCell(6);
                assessorComment.setCellStyle(cellStyles.stepNameCellStyle as CellStyle);
                assessorComment.setCellValue(assStepData.assessorComment);

                // Parameters
                Cell paramsNamesCell = nextStepRow.createCell(7);
                Cell paramsValuesCell = nextStepRow.createCell(8);

                StringBuilder paramNamesBuilder = new StringBuilder();
                StringBuilder paramValuesBuilder = new StringBuilder();

                assStepData.parameterValues.sort().each { paramValue ->
                    paramNamesBuilder.append("${paramValue.parameter.name}:\n")

                    ParameterKind paramKind = Enum.valueOf(ParameterKind.class, paramValue.parameter.kind)
                    TrustmarkParameterBindingImpl paramBinding = new TrustmarkParameterBindingImpl(
                            parameterKind: paramKind,
                            value: paramValue.userValue
                    )
                    switch (paramBinding.parameterKind) {
                        case ParameterKind.ENUM_MULTI:
                            if (paramBinding.stringListValue.size() > 1) {
                                for (String value : paramBinding.stringListValue) {
                                    log.info("ParameterKind.ENUM_MULTI: ${value}")
                                    paramNamesBuilder.append("\n")
                                    paramValuesBuilder.append("\u2022" + " " + value + "\n")
                                }
                                paramValuesBuilder.append("\n")
                                break
                            }
                        case ParameterKind.ENUM:
                        case ParameterKind.STRING:
                            log.info("<ParameterKind.ENUM, ParameterKind.STRING>:${paramBinding.stringValue}")
                            paramNamesBuilder.append("\n")
                            paramValuesBuilder.append("${paramBinding.stringValue}" + "\n\n")
                            break
                        case ParameterKind.NUMBER:
                            log.info("ParameterKind.NUMBER: ${paramBinding.numericValue}")
                            paramNamesBuilder.append("\n")
                            paramValuesBuilder.append("${paramBinding.numericValue}" + "\n\n")
                            break
                        case ParameterKind.BOOLEAN:
                            log.info("ParameterKind.BOOLEAN: ${paramBinding.booleanValue}")
                            paramNamesBuilder.append("\n")
                            paramValuesBuilder.append("${paramBinding.booleanValue}" + "\n\n")
                            break
                        case ParameterKind.DATETIME:
                            log.info("ParameterKind.DATETIME: ${paramBinding.dateTimeValue}")
                            paramNamesBuilder.append("\n")
                            paramValuesBuilder.append("${paramBinding.booleanValue}" + "\n\n")
                            break
                    }
                }

                CellStyle paramCellStyle = workbook.createCellStyle();
                paramCellStyle.setWrapText(true);
                paramCellStyle.setVerticalAlignment(VerticalAlignment.TOP);

                RichTextString paramNameString = workbook.getCreationHelper().createRichTextString(paramNamesBuilder.toString());
                paramsNamesCell.setCellValue(paramNameString);
                paramsNamesCell.setCellStyle(paramCellStyle);

                RichTextString paramValueString = workbook.getCreationHelper().createRichTextString(paramValuesBuilder.toString());
                paramsValuesCell.setCellValue(paramValueString);
                paramsValuesCell.setCellStyle(paramCellStyle);

                if (paramNamesBuilder.length() > 0) {
                    assessmentStepsSheet.autoSizeColumn(7);
                }
                if (paramValuesBuilder.length() > 0) {
                    assessmentStepsSheet.autoSizeColumn(8);
                }

                if (stepIndex < (stepsToWrite.size() - 1)) {
                    nextStepRow = assessmentStepsSheet.createRow(rowNum++);
                    nextStepRow.setRowStyle(cellStyles.assessmentStepRowCellStyle);
                }
            }//end each step data to write.

            // update progress percentage
            int percent = (int) Math.floor(((double) currentAssessmentStep++ / (double) assessment.steps.size()) * 100.0d)
            setAttribute(sessionId, ASSESSMENT_TO_EXCEL_REPORT_PERCENT_VAR, "" + percent)
        }//end each assessment step from this td.
    }

    private void generateMetadata(Workbook workbook, Assessment assessment) {
        // Assessment metadata sheet
        Sheet assessmentMetadataSheet = workbook.createSheet(ASSESSMENT_METADATA_SHEET_NAME);
        int metadataRowNum = 0;

        // Assessment Name
        Row assessmentNameRow = assessmentMetadataSheet.createRow(metadataRowNum++);
        Cell assessmentNameLabelCell = assessmentNameRow.createCell(0);
        assessmentNameLabelCell.setCellValue("Assessment Name:");
        assessmentMetadataSheet.autoSizeColumn(0);

        Cell assessmentNameValueCell = assessmentNameRow.createCell(1);
        assessmentNameValueCell.setCellValue(assessment.assessmentName);
        assessmentMetadataSheet.autoSizeColumn(1);


        // Assessed Organization
        Row assessedOrgRow = assessmentMetadataSheet.createRow(metadataRowNum++);
        Cell assessedOrgLabelCell = assessedOrgRow.createCell(0);
        assessedOrgLabelCell.setCellValue("Assessed Organization:");
        assessmentMetadataSheet.autoSizeColumn(0);

        Cell assessedOrgValueCell = assessedOrgRow.createCell(1);
        assessedOrgValueCell.setCellValue(assessment.assessedOrganization.getName());
        assessmentMetadataSheet.autoSizeColumn(1);

        // Assessor (last assessor)
        Row assessorRow = assessmentMetadataSheet.createRow(metadataRowNum++);
        Cell assessorLabelCell = assessorRow.createCell(0);
        assessorLabelCell.setCellValue("Assessor (last):");
        assessmentMetadataSheet.autoSizeColumn(0);

        Cell assessorValueCell = assessorRow.createCell(1);
        assessorValueCell.setCellValue("${assessment.lastAssessor.nameGiven} ${assessment.lastAssessor.nameFamily}");
        assessmentMetadataSheet.autoSizeColumn(1);


        // Assessment Comment
        Row assessmentCommentRow = assessmentMetadataSheet.createRow(metadataRowNum++);
        Cell assessmentCommentLabelCell = assessmentCommentRow.createCell(0);
        assessmentCommentLabelCell.setCellValue("Assessment Comment:");
        assessmentMetadataSheet.autoSizeColumn(0);

        Cell assessmentCommentValueCell = assessmentCommentRow.createCell(1);
        assessmentCommentValueCell.setCellValue(assessment.comment);
        assessmentMetadataSheet.autoSizeColumn(1);


        // Assessment Status
        Row assessmentStatusRow = assessmentMetadataSheet.createRow(metadataRowNum++);
        Cell assessmentStatusLabelCell = assessmentStatusRow.createCell(0);
        assessmentStatusLabelCell.setCellValue("Assessment Status:");
        assessmentMetadataSheet.autoSizeColumn(0);

        Cell assessmentStatusValueCell = assessmentStatusRow.createCell(1);
        assessmentStatusValueCell.setCellValue(assessment.status.toString());
        assessmentMetadataSheet.autoSizeColumn(1);

        // Assessment Date Created
        Row assessmentDateCreatedRow = assessmentMetadataSheet.createRow(metadataRowNum++);
        Cell assessmentDateCreatedLabelCell = assessmentDateCreatedRow.createCell(0);
        assessmentDateCreatedLabelCell.setCellValue("Date Created:");
        assessmentMetadataSheet.autoSizeColumn(0);

        Cell assessmentDateCreatedValueCell = assessmentDateCreatedRow.createCell(1);
        assessmentDateCreatedValueCell.setCellValue(new SimpleDateFormat("yyyy-MM-dd").format(assessment.dateCreated).toString());
        assessmentMetadataSheet.autoSizeColumn(1);


        // Assessment Number of TDs
        Row assessmentNumberOfTDsRow = assessmentMetadataSheet.createRow(metadataRowNum++);
        Cell assessmentNumberOfTDsLabelCell = assessmentNumberOfTDsRow.createCell(0);
        assessmentNumberOfTDsLabelCell.setCellValue("Number of TDs:");
        assessmentMetadataSheet.autoSizeColumn(0);

        Cell assessmentNumberOfTDsValueCell = assessmentNumberOfTDsRow.createCell(1);
        assessmentNumberOfTDsValueCell.setCellValue(assessment.tdLinks ? assessment.tdLinks.size().toString() : "0");
        assessmentMetadataSheet.autoSizeColumn(1);


        // Assessment Number of Steps
        Row assessmentNumberOfStepsRow = assessmentMetadataSheet.createRow(metadataRowNum++);
        Cell assessmentNumberOfStepsLabelCell = assessmentNumberOfStepsRow.createCell(0);
        assessmentNumberOfStepsLabelCell.setCellValue("Number of Steps:");
        assessmentMetadataSheet.autoSizeColumn(0);

        Cell assessmentNumberOfStepsValueCell = assessmentNumberOfStepsRow.createCell(1);
        assessmentNumberOfStepsValueCell.setCellValue(assessment.steps ? assessment.steps.size().toString() : "0");
        assessmentMetadataSheet.autoSizeColumn(1);
    }

    private void insertListToRow(Workbook workbook, Sheet assessmentStepsSheet, Row row, CellStyle style, int startIndex, List values){
        int curIndex = startIndex;
        for( Object value : values ){
            if (PARAMETERS_HEADER.equals(value)) {
                // parameters header is a special case
                Cell parameterTitleCell = row.createCell(curIndex++);
                parameterTitleCell.setCellValue(PARAMETERS_HEADER);
                CellStyle parameterTitleStyle = workbook.createCellStyle();
                parameterTitleStyle.setVerticalAlignment(VerticalAlignment.TOP);
                parameterTitleStyle.setAlignment(HorizontalAlignment.CENTER);
                parameterTitleCell.setCellStyle(parameterTitleStyle);
                // TODO: Maybe create the next cell placeholder?
                assessmentStepsSheet.addMergedRegion(new CellRangeAddress(0, 0, 7, 8));
            } else {
                Cell cell = row.createCell(curIndex++);
                cell.setCellStyle(style);
                cell.setCellValue(value.toString());
            }
        }
    }

    private Map createFonts(Workbook workbook){
        def fonts = [:]

        XSSFFont tdTitleFont = workbook.createFont() as XSSFFont;
        tdTitleFont.setBold(true);

        tdTitleFont.setFontHeightInPoints((short) 20);
        tdTitleFont.setFontName("Arial");
        fonts.put("tdTitleFont", tdTitleFont);

        return fonts;
    }

    private Map createCellStyles(Workbook workbook, Map fonts){
        def cellStyles = [:]

        CellStyle tdTitleRowCellStyle = workbook.createCellStyle();
        tdTitleRowCellStyle.setFillForegroundColor(new HSSFColor.AQUA().getIndex());
        tdTitleRowCellStyle.setFillBackgroundColor(new HSSFColor.AQUA().getIndex());
        tdTitleRowCellStyle.setWrapText(true);
        tdTitleRowCellStyle.setVerticalAlignment(VerticalAlignment.TOP);
        cellStyles.put("tdTitleRowCellStyle", tdTitleRowCellStyle);

        CellStyle tdTitleCellStyle = workbook.createCellStyle();
        tdTitleCellStyle.setWrapText(true);
        tdTitleCellStyle.setFont(fonts.tdTitleFont);
        tdTitleCellStyle.setVerticalAlignment(VerticalAlignment.TOP);
        cellStyles.put("tdTitleCellStyle", tdTitleRowCellStyle);

        CellStyle assessmentStepRowCellStyle = workbook.createCellStyle();
        assessmentStepRowCellStyle.setWrapText(true);
        assessmentStepRowCellStyle.setFillForegroundColor(new HSSFColor.WHITE().getIndex());
        assessmentStepRowCellStyle.setFillBackgroundColor(new HSSFColor.WHITE().getIndex());
        assessmentStepRowCellStyle.setVerticalAlignment(VerticalAlignment.TOP);
        cellStyles.put("assessmentStepRowCellStyle", assessmentStepRowCellStyle);

        CellStyle stepNameCellStyle = workbook.createCellStyle();
        stepNameCellStyle.setWrapText(true);
        stepNameCellStyle.setVerticalAlignment(VerticalAlignment.TOP);
        cellStyles.put("stepNameCellStyle", stepNameCellStyle);

        CellStyle stepDescCellStyle = workbook.createCellStyle();
        stepDescCellStyle.setWrapText(true);
        stepDescCellStyle.setVerticalAlignment(VerticalAlignment.TOP);
        cellStyles.put("stepDescCellStyle", stepDescCellStyle);

        CellStyle parametersCellStyle = workbook.createCellStyle();
        parametersCellStyle.setWrapText(true);
        parametersCellStyle.setVerticalAlignment(VerticalAlignment.TOP);
        parametersCellStyle.setAlignment(HorizontalAlignment.CENTER);
        cellStyles.put("parametersTitleCellStyle", parametersCellStyle);

        // Web Links
        CellStyle hlinkstyle = workbook.createCellStyle();
        XSSFFont hlinkfont = workbook.createFont() as XSSFFont;
        hlinkfont.setUnderline(XSSFFont.U_SINGLE);
        hlinkfont.setColor(IndexedColors.BLUE.index);
        hlinkstyle.setFont(hlinkfont);
        hlinkstyle.setVerticalAlignment(VerticalAlignment.TOP);
        cellStyles.put("tdUriLinkCellStyle", hlinkstyle);

        return cellStyles;
    }
}

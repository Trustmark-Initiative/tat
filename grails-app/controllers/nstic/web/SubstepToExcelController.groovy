package nstic.web

import grails.converters.JSON
import nstic.web.td.AssessmentStep
import nstic.web.td.AssessmentStepArtifact
import nstic.web.td.AssessmentSubStep
import nstic.web.td.TrustmarkDefinition
import org.apache.poi.hssf.usermodel.HSSFFont
import org.apache.poi.hssf.util.HSSFColor
import org.dom4j.Document
import org.dom4j.Element
import org.dom4j.io.SAXReader;

import org.apache.poi.hssf.usermodel.HSSFCellStyle
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.Font
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

@PreAuthorize('hasAnyAuthority("tat-contributor", "tat-admin")')
class SubstepToExcelController {

    def fileService;

    /**
     * Loads the substep resolution index page.
     */
    def index() {
        User user = User.findByUsername(((OAuth2AuthenticationToken) SecurityContextHolder.getContext().getAuthentication()).getName());
        log.info("User[$user] is viewing the substep to excel generation page...");


    }//end index()

    def substepListing() {
        User user = User.findByUsername(((OAuth2AuthenticationToken) SecurityContextHolder.getContext().getAuthentication()).getName());
        log.info("User[$user] is viewing the list of substeps...");

        log.debug("Finding all Assessments with Substeps...");
        log.debug("Writing substep JSON...");
        def responseJson = ['status': 'SUCCESS', 'tdCount': TrustmarkDefinition.count()]
        def tdJsonList = []
        TrustmarkDefinition.findAll().each { TrustmarkDefinition td ->
            def tdJson = [:]

            File tdXmlFile = td.originalXml.getContent().toFile()
            if( !tdXmlFile.exists() )
                throw new UnsupportedOperationException("Cannot locate original XML file[${tdXmlFile.canonicalPath}] for TD: ${td.name}, Version: ${td.tdVersion}")

            Element tdXml = this.readTdAsXml(tdXmlFile);

            tdJson.databaseId = td.id;
            tdJson.id = td.uri
            tdJson.version = td.tdVersion
            tdJson.name = td.name
            tdJson.description = td.description
            tdJson.publicationDateTime = td.publicationDateTime
            tdJson.containingFilename = tdXmlFile.canonicalPath

            def criteria = []
            tdXml.selectNodes("//tf:ConformanceCriterion/tf:ConformanceCriterion").each { Element crit ->
                def critJson = [:]
                critJson.id = crit.selectObject("string(./@tf:id)")?.toString()
                critJson.number = Integer.parseInt(crit.selectObject("string(./tf:Number)")?.toString())
                critJson.name = crit.selectObject("string(./tf:Name)")?.toString()
                critJson.description = crit.selectObject("string(./tf:Description)")?.toString()
                criteria.add(critJson)
            }
            tdJson.criteria = criteria

            def assStepList = []
            td.assessmentSteps.each { AssessmentStep assessmentStep ->
                def assStepJson = [:]

                assStepJson.stepNumber = assessmentStep.stepNumber;
                assStepJson.name = assessmentStep.name;
                assStepJson.description = assessmentStep.description;

                def substepList = []
                assessmentStep.substeps?.each { AssessmentSubStep substep ->
                    def substepJson = [:]
                    substepJson.name = substep.name
                    substepJson.description = substep.description
                    substepList.add( substepJson );
                }
                assStepJson.substeps = substepList;

                assStepList.add( assStepJson );
            }
            tdJson.assessmentSteps = assStepList

            tdJsonList.add( tdJson );
        }

        Collections.sort(tdJsonList, {td1, td2 -> return td1.name.compareToIgnoreCase(td2.name); } as Comparator);

        responseJson.put("tdList", tdJsonList);

        withFormat {
            html { throw new UnsupportedOperationException("JSON is the only supported output format.") }
            xml { throw new UnsupportedOperationException("JSON is the only supported output format.") }
            json {
                render responseJson as JSON
            }
        }
    }//end substepListing()

    /**
     * Generates the substep to Excel file as a binary, and then returns the binary id to what was generated so it can
     * be downloaded.
     */
    def excelGenerate() {
        User user = User.findByUsername(((OAuth2AuthenticationToken) SecurityContextHolder.getContext().getAuthentication()).getName())
        log.info("Generating an Excel Spreadsheet...");
        List<Long> ids = []

        if( params.containsKey("ids[]") ){
            def paramList = params.list("ids[]");
            paramList.each{ id ->
                ids.add(Long.parseLong(id));
            }
        }else if( params.containsKey("ids") ){
            ids.add( Long.parseLong(params.get("ids")) );
        }else{
            log.warn("Missing ids parameter!");
            throw new UnsupportedOperationException("Missing required parameter 'ids', which is a list (or single) trustmark definition id to generate.")
        }

        log.info("Generating from Ids: $ids");
        List<TrustmarkDefinition> tdList = []
        ids.each { id ->
            TrustmarkDefinition td = TrustmarkDefinition.get(id)
            if( td != null ) {
                log.debug("Found TD[$id]: ${td.getUniqueDisplayName()}")
                tdList.add(td);
            }else{
                log.warn("No such TD: ${id}")
            }
        }
        Collections.sort(tdList, {TrustmarkDefinition td1, TrustmarkDefinition td2 ->
            return td1.name.compareToIgnoreCase(td2.name);
        } as Comparator);


        HSSFWorkbook workbook = new HSSFWorkbook();

        def fonts = createFonts(workbook);
        def cellStyles = createCellStyles(workbook, fonts);

        Sheet substepList = workbook.createSheet("Listing");
        for( int i = 0; i < 20; i++ ) {
            substepList.setColumnWidth(i, 15000);
        }
        int rowNum = 0;

        Row headerRow = substepList.createRow(rowNum++);
        headerRow.setRowStyle(cellStyles.tdTitleRowCellStyle);
        def headerList =  ["Step Name", "Step Desc", "Criterion Name", "Criterion Desc", "Citations", "Artifacts", "TD Moniker", "TD Name", "TD Version", "TD Description",
                                    "TD Publication DateTime", "Stakeholder Desc", "Recipient Desc", "Relying Party Desc", "Provider Desc",
                                    "Provider Eligibility Criteria", "Assessor Qualifications Desc", "Trustmark Revocation Criteria", "Extension Description"];
        insertListToRow(headerRow, cellStyles.tdTitleRowCellStyle, 0, headerList);

        Cell assStepIdHeaderCell = headerRow.createCell(headerList.size() + 1);
        assStepIdHeaderCell.setCellStyle(cellStyles.tdTitleRowCellStyle);
        assStepIdHeaderCell.setCellValue("Step Id");

        Cell substepIdHeaderCell = headerRow.createCell(headerList.size() + 2);
        substepIdHeaderCell.setCellStyle(cellStyles.tdTitleRowCellStyle);
        substepIdHeaderCell.setCellValue("Substep Id");

        tdList.each { TrustmarkDefinition td ->

            File tdXmlFile = td.originalXml.getContent().toFile()
            if( !tdXmlFile.exists() )
                throw new UnsupportedOperationException("Cannot locate original XML file[${tdXmlFile.canonicalPath}] for TD: ${td.name}, Version: ${td.tdVersion}")

            Element tdXml = this.readTdAsXml(tdXmlFile);

            td.getSortedSteps().each { AssessmentStep assStep ->
                Row nextStepRow = substepList.createRow(rowNum++);
                nextStepRow.setRowStyle(cellStyles.assStepRowCellStyle);

                def stepsToWrite = []
                if( assStep.substeps && !assStep.substeps.isEmpty() ){
                    assStep.substeps.each{ AssessmentSubStep substep ->
                        stepsToWrite.add( [assStepId: assStep.id, name: substep.name, desc: substep.description, substepId: substep.id] );
                    }
                }else{
                    stepsToWrite.add( [assStepId: assStep.id, name: assStep.name, desc: assStep.description, substepId: -1] );
                }

                for( int stepIndex = 0; stepIndex < stepsToWrite.size(); stepIndex++ ){
                    def step = stepsToWrite.get(stepIndex);

                    Cell assStepNameCell = nextStepRow.createCell(0);
                    assStepNameCell.setCellStyle(cellStyles.stepNameCellStyle);
                    assStepNameCell.setCellValue(step.name);

                    Cell assStepDescCell = nextStepRow.createCell(1);
                    assStepNameCell.setCellStyle(cellStyles.stepDescCellStyle);
                    assStepDescCell.setCellValue(step.desc);

                    StringBuilder artifactsData = new StringBuilder();
                    List<AssessmentStepArtifact> artifacts = []
                    artifacts.addAll(assStep.artifacts);
                    for( int i = 0; i < artifacts.size(); i++ ){
                        AssessmentStepArtifact artifact = artifacts.get(i);
                        artifactsData.append(artifact.getName()).append(":")
                            .append(artifact.getDescription())
                        if( i < (artifacts.size() - 1) ){
                            artifactsData.append("|");
                        }
                    }
                    Cell artifactsCell = nextStepRow.createCell(5);
                    artifactsCell.setCellStyle(cellStyles.stepNameCellStyle);
                    artifactsCell.setCellValue(artifactsData.toString());

                    insertListToRow(nextStepRow, cellStyles.stepDescCellStyle, 6, [
                            getMoniker(td.uri), td.name, td.version, td.description, new java.text.SimpleDateFormat("yyyy-MM-dd").format(td.publicationDateTime),
                            td.targetStakeholderDescription, td.targetTrustmarkRecipientDescription, td.targetTrustmarkRelyingPartyDescription,
                            td.targetTrustmarkProviderDescription, td.providerEligibilityCriteria, td.assessorQualificationsDescription,
                            td.revocationAndReissuanceCriteria, td.extensionDescription
                    ]);

                    Cell assStepIdCell = nextStepRow.createCell(headerList.size() + 1);
                    assStepIdCell.setCellStyle(cellStyles.stepNameCellStyle);
                    assStepIdCell.setCellValue(step.assStepId);

                    Cell assSubstepIdCell = nextStepRow.createCell(headerList.size() + 2);
                    assSubstepIdCell.setCellStyle(cellStyles.stepNameCellStyle);
                    assSubstepIdCell.setCellValue(step.substepId);


                    Element assStepXml = (Element) tdXml.selectSingleNode("//tf:AssessmentSteps/tf:AssessmentStep[string(./tf:Number) = '${assStep.stepNumber}']")
                    int critCount = 0;
                    assStepXml.selectNodes("./tf:ConformanceCriterion").each { Element conformanceCritRefXml ->
                        String critId = (String) conformanceCritRefXml.selectObject("string(./@tf:ref)");
                        Element conformanceCritXml = tdXml.selectSingleNode("//tf:ConformanceCriterion/tf:ConformanceCriterion[./@tf:id = '${critId}']")

                        Cell critNameCell = nextStepRow.createCell(2);
                        critNameCell.setCellStyle(cellStyles.stepNameCellStyle);
                        critNameCell.setCellValue(conformanceCritXml.selectObject("string(./tf:Name)"));

                        Cell critDescCell = nextStepRow.createCell(3);
                        critDescCell.setCellStyle(cellStyles.stepDescCellStyle);
                        critDescCell.setCellValue(conformanceCritXml.selectObject("string(./tf:Description)"));

                        if (critCount > 0) {
                            nextStepRow = substepList.createRow(rowNum++);
                            nextStepRow.setRowStyle(cellStyles.assStepRowCellStyle);
                        }

                        StringBuilder citationBuilder = new StringBuilder();
                        List citationElements = conformanceCritXml.selectNodes("./tf:Citation") ?: []
                        for( int i = 0; i < citationElements.size(); i++ ){
                            Element citationXml = citationElements.get(i);
                            String citationRefVal = citationXml.selectObject("string(./tf:Source/@tf:ref)") ?: '';
                            String citationDescVal = citationXml.selectObject("string(./tf:Description)") ?: '';
                            citationBuilder.append(citationRefVal).append(":").append(citationDescVal);
                            if( i < (citationElements.size() - 1) ){
                                citationBuilder.append("|");
                            }
                        }
                        Cell critCitationCell = nextStepRow.createCell(4);
                        critCitationCell.setCellStyle(cellStyles.stepDescCellStyle);
                        critCitationCell.setCellValue(citationBuilder.toString());

                        critCount++;
                    }//end each relevant criteria reference...

                    if (stepIndex < (stepsToWrite.size() - 1)) {
                        nextStepRow = substepList.createRow(rowNum++);
                        nextStepRow.setRowStyle(cellStyles.assStepRowCellStyle);
                    }
                }//end each step data to write.
            }//end each assessment step from this td.

        }

        // Finally we write it out to a file...
        File tempOutputFile = File.createTempFile("td-dump_", ".xls");  // Writes out file
        FileOutputStream fout = new FileOutputStream(tempOutputFile);
        workbook.write(fout);
        fout.flush();
        fout.close();
        log.info("Successfully wrote file: ${tempOutputFile.canonicalPath}")

        log.debug("Creating binary object...");
        BinaryObject excelBinary = fileService?.createBinaryObject(tempOutputFile, user.username, 'application/vnd.ms-excel', 'step-dump.'+Calendar.getInstance().getTimeInMillis()+'.xls', 'xls');

        log.debug("Writing response...");
        def responseJson = [status: 'SUCCESS', message: 'Generated Excel File', binaryId: excelBinary.id]
        withFormat {
            html { throw new UnsupportedOperationException("JSON is the only supported output format.") }
            xml { throw new UnsupportedOperationException("JSON is the only supported output format.") }
            json {
                render responseJson as JSON
            }
        }

    }//end excelGenerate()
    //==================================================================================================================
    //  Private Helper Methods
    //==================================================================================================================
    private String getMoniker(String uri){
        String moniker = uri;
        moniker = moniker.replace("https://trustmark.gtri.gatech.edu/operational-pilot/trustmark-definitions/", "");
        return moniker.split("/")[0];
    }

    private void insertListToRow(Row row, HSSFCellStyle style, int startIndex, List values){
        int curIndex = startIndex;
        for( Object value : values ){
            Cell cell = row.createCell(curIndex++);
            cell.setCellStyle(style);
            cell.setCellValue(value.toString());
        }
    }//end insertListToRow()


    private Map createFonts(HSSFWorkbook workbook){
        def fonts = [:]

        HSSFFont tdTitleFont = workbook.createFont();
        tdTitleFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        tdTitleFont.setFontHeightInPoints((short) 20);
        tdTitleFont.setFontName(HSSFFont.FONT_ARIAL);
        fonts.put("tdTitleFont", tdTitleFont);

        return fonts;
    }

    private Map createCellStyles(HSSFWorkbook workbook, Map fonts){
        def cellStyles = [:]

        HSSFCellStyle tdTitleRowCellStyle = workbook.createCellStyle();
        tdTitleRowCellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        tdTitleRowCellStyle.setFillForegroundColor(new HSSFColor.AQUA().getIndex());
        tdTitleRowCellStyle.setFillBackgroundColor(new HSSFColor.AQUA().getIndex());
        tdTitleRowCellStyle.setWrapText(true);
        cellStyles.put("tdTitleRowCellStyle", tdTitleRowCellStyle);

        HSSFCellStyle tdTitleCellStyle = workbook.createCellStyle();
        tdTitleCellStyle.setWrapText(true);
        tdTitleCellStyle.setFont(fonts.tdTitleFont);
        cellStyles.put("tdTitleCellStyle", tdTitleRowCellStyle);

        HSSFCellStyle assStepRowCellStyle = workbook.createCellStyle();
        assStepRowCellStyle.setWrapText(true);
        assStepRowCellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        assStepRowCellStyle.setFillForegroundColor(new HSSFColor.WHITE().getIndex());
        assStepRowCellStyle.setFillBackgroundColor(new HSSFColor.WHITE().getIndex());
        cellStyles.put("assStepRowCellStyle", assStepRowCellStyle);

        HSSFCellStyle stepNameCellStyle = workbook.createCellStyle();
        stepNameCellStyle.setWrapText(true);
        cellStyles.put("stepNameCellStyle", stepNameCellStyle);

        HSSFCellStyle stepDescCellStyle = workbook.createCellStyle();
        stepDescCellStyle.setWrapText(true);
        cellStyles.put("stepDescCellStyle", stepDescCellStyle);


        return cellStyles;
    }//end


    private Element readTdAsXml(File xmlFile){
        SAXReader reader = new SAXReader();
        Document document = reader.read(xmlFile);
        Element rootElement = document.getRootElement();
        rootElement.addNamespace("tf", "https://trustmark.gtri.gatech.edu/specifications/trustmark-framework/1.0/schema/");
        return rootElement;
    }



}//end SubstepToExcelController()
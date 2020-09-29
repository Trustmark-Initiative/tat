package nstic.td.io

import assessment.tool.FileService
import edu.gatech.gtri.trustmark.v1_0.FactoryLoader
import edu.gatech.gtri.trustmark.v1_0.io.SerializerFactory
import nstic.web.BinaryObject
import nstic.web.assessment.ArtifactData
import nstic.web.assessment.Assessment
import nstic.web.assessment.AssessmentStepData
import nstic.web.td.AssessmentStep
import nstic.web.td.AssessmentStepArtifact
import nstic.web.td.TrustmarkDefinition
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.dom4j.Document
import org.dom4j.Element
import org.dom4j.io.OutputFormat
import org.dom4j.io.SAXReader
import org.dom4j.io.XMLWriter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.util.StringUtils

import javax.xml.bind.DatatypeConverter

/**
 * Created by brad on 10/7/14.
 */
class TrustmarkDefinitionV1_0Importer implements Importer {
    protected static final Logger log = LoggerFactory.getLogger(TrustmarkDefinitionV1_0Importer.class)

    static final String TF_NS_URI = "https://trustmark.gtri.gatech.edu/specifications/trustmark-framework/1.0/schema/";

    @Autowired
    FileService fileService;

    @Override
    String getName() {
        return "Trustmark Definition v1.0 XML Importer"
    }

    @Override
    Object supports(File file, String contentType) {
        return supports(file.text, contentType);
    }
    @Override
    Object supports(Reader reader, String contentType) {
        return supports(reader.text, contentType);
    }
    @Override
    Object supports(InputStream inputStream, String contentType) {
        return supports(inputStream.text, contentType);
    }
    @Override
    Object supports(String data, String contentType) {
        if( contentType.toLowerCase() == "text/xml" || contentType.toLowerCase() == "application/xml" ){
            SAXReader reader = new SAXReader();
            Document document = reader.read(new StringReader(data));
            Element element = document.getRootElement();
            element.addNamespace("tf", TF_NS_URI);
            return supports(document.getRootElement());
        }
        return null;
    }
    @Override
    Object supports(Element xmlElement) {
        if( xmlElement.getName() == "TrustmarkDefinition" && xmlElement.getNamespace().getURI() == TF_NS_URI){
            return xmlElement;
        }
        return null
    }//end supports()

    @Override
    Object doImport(Object data) throws ImportException {
        TrustmarkDefinition.withTransaction {
            Element tdXml = (Element) data;
            if( true )
                throw new ImportException("Needs to be Rewritten with TMF API in mind.");

            TrustmarkDefinition td = new TrustmarkDefinition();
            td.uri = tdXml.selectObject("string(/tf:TrustmarkDefinition/tf:Metadata/tf:Identifier)")?.trim();

            TrustmarkDefinition tdFromDb = TrustmarkDefinition.findByUri(td.uri);
            if( tdFromDb ){
                Integer existingAssessmentsCount = Assessment.countByTrustmarkDefinition(tdFromDb);
                if( existingAssessmentsCount == 0 ){
                    // Delete Existing Trustmark Definition...
                    log.warn("A TD[${td.uri}] exists in database already, but has no assessments.  Deleting it...");
                    // First, we delete anything that could reference this TD, just in case.
                    def assSteps = []
                    assSteps.addAll(tdFromDb.assessmentSteps ?: []);
                    for( AssessmentStep step : assSteps ){
                        if( step.artifacts && !step.artifacts.isEmpty() ) {
                            def theArtifacts = []
                            theArtifacts.addAll(step.artifacts);
                            for (AssessmentStepArtifact artifact : theArtifacts) {
                                def artifactDatas = ArtifactData.findAllByRequiredArtifact(artifact) ?: [];
                                for( ArtifactData ad : artifactDatas ){
                                    ad.delete(flush: true);
                                }
                                step.removeFromArtifacts(artifact);
                                artifact.delete(flush: true);
                            }
                        }
                        def stepDatas = AssessmentStepData.findAllByStep(step) ?: [];
                        for( AssessmentStepData stepData : stepDatas ){
                            stepData.delete(flush: true);
                        }

                        // TODO Substeps?

                        tdFromDb.removeFromAssessmentSteps(step);
                        step.delete(flush: true);
                    }
                    tdFromDb.delete(flush: true);
                }else{
                    String errorMsg = "Cannot import TrustmarkDefinition[${td.uri}], another Trustmark Definition with "+
                            "that ID already exists: ${tdFromDb.name}[version: ${tdFromDb.tdVersion}] and has valid "+
                            "Assessments.  Either delete those assessments($existingAssessmentsCount) or rename your TD.";
                    log.error(errorMsg)
                    throw new ImportException(errorMsg);
                }
            }

            td.referenceAttributeName = tdXml.selectObject("string(/tf:TrustmarkDefinition/tf:Metadata/tf:TrustmarkReferenceAttributeName)")?.trim();
            td.name = tdXml.selectObject("string(/tf:TrustmarkDefinition/tf:Metadata/tf:Name)")?.trim();
            td.tdVersion = tdXml.selectObject("string(/tf:TrustmarkDefinition/tf:Metadata/tf:Version)")?.trim();
            td.description = tdXml.selectObject("string(/tf:TrustmarkDefinition/tf:Metadata/tf:Description)")?.trim();
            Calendar pubDateTime =
                    DatatypeConverter.parseDateTime(tdXml.selectObject("string(/tf:TrustmarkDefinition/tf:Metadata/tf:PublicationDateTime)")?.trim())
            td.publicationDateTime = pubDateTime.getTime();

            td.targetStakeholderDescription = tdXml.selectObject("string(/tf:TrustmarkDefinition/tf:Metadata/tf:TargetStakeholderDescription)")?.trim();
            td.targetTrustmarkRecipientDescription = tdXml.selectObject("string(/tf:TrustmarkDefinition/tf:Metadata/tf:TargetRecipientDescription)")?.trim();
            td.targetTrustmarkProviderDescription = tdXml.selectObject("string(/tf:TrustmarkDefinition/tf:Metadata/tf:TargetProviderDescription)")?.trim();
            td.targetTrustmarkRelyingPartyDescription = tdXml.selectObject("string(/tf:TrustmarkDefinition/tf:Metadata/tf:TargetRelyingPartyDescription)")?.trim();

            td.providerEligibilityCriteria = tdXml.selectObject("string(/tf:TrustmarkDefinition/tf:Metadata/tf:ProviderEligibilityCriteria)")?.trim();
            td.assessorQualificationsDescription = tdXml.selectObject("string(/tf:TrustmarkDefinition/tf:Metadata/tf:AssessorQualificationsDescription)")?.trim();
            td.revocationAndReissuanceCriteria = tdXml.selectObject("string(/tf:TrustmarkDefinition/tf:Metadata/tf:CriteriaNecessitatingTrustmarkRevocationAndReissuance)")?.trim();
            td.extensionDescription = tdXml.selectObject("string(/tf:TrustmarkDefinition/tf:Metadata/tf:ExtensionDescription)")?.trim();
            td.legalNotice = tdXml.selectObject("string(/tf:TrustmarkDefinition/tf:Metadata/tf:LegalNotice)")?.trim();
            td.notes = tdXml.selectObject("string(/tf:TrustmarkDefinition/tf:Metadata/tf:Notes)")?.trim();


            log.debug("Saving TD: [${td.uri}] name=${td.name} version=${td.tdVersion}...")
            td.save(failOnError: true);
            log.debug("Successfully saved td: ${td.name}:${td.tdVersion}")

            String xmlContent = toXmlString(tdXml);
            File tdXmlFile = File.createTempFile("td-", ".xml");
            tdXmlFile << xmlContent;

//            log.debug("Rendering TD to HTML...");
//            HtmlFormatter formatter = HtmlFormatterProvider.resolve(xmlContent);
//            String tdHtml = formatter.render(xmlContent);
//            File tdHtmlFile = File.createTempFile("td-", ".xhtml");
//            tdHtmlFile << tdHtml;
//            BinaryObject htmlRendering = fileService.createBinaryObject( tdHtmlFile, "TdImporterService", "text/xhtml", "TrustmarkDefinition.xhtml", "xhtml");
//            td.cachedHtml = htmlRendering;
//            td.save(failOnError: true);

            log.debug("Storing original TD xml file...")
            BinaryObject originalXml = fileService.createBinaryObject( tdXmlFile, "TdImporterService" );
            td.originalXml = originalXml
            td.save(failOnError: true);

            log.debug("Storing assessment steps...");
            int stepCount = 0;
            tdXml.selectNodes("/tf:TrustmarkDefinition/tf:AssessmentSteps/tf:AssessmentStep")?.each { Element assStepXml ->
                AssessmentStep step = new AssessmentStep(trustmarkDefinition: td);
                step.stepNumber = Integer.parseInt(assStepXml.selectObject("string(./tf:Number)")?.trim())
                step.name = assStepXml.selectObject("string(./tf:Name)")?.trim()
                if(StringUtils.isEmpty(step.name))
                    throw new ImportException("TD Step #${step.stepNumber} has no name.")
                step.description = assStepXml.selectObject("string(./tf:Description)")?.trim()

                step.save(failOnError: true);
                td.addToAssessmentSteps(step);
                td.save(failOnError: true);

                assStepXml.selectNodes("./tf:Artifact")?.each{ Element stepArtifactXml ->
                    AssessmentStepArtifact artifact = new AssessmentStepArtifact(assessmentStep: step);
                    artifact.name = stepArtifactXml.selectObject("string(./tf:Name)")?.trim()
                    artifact.description = stepArtifactXml.selectObject("string(./tf:Description)")?.trim() ?: "<em>No Description Given</em>"
                    artifact.save(failOnError: true);
                    step.addToArtifacts(artifact);
                    step.save(failOnError: true);
                }

                stepCount++;
            }
            log.info("Successfully stored @|cyan ${stepCount}|@ assessment steps on td @|green "+td.name+"|@")
            return td;
        }//end transaction boundary
    }//end doImport()



    private String toXmlString(Element dom4jElement){
        OutputFormat outFormat = OutputFormat.createPrettyPrint();
        StringWriter outStringWriter = new StringWriter();
        XMLWriter writer = new XMLWriter(outStringWriter, outFormat);
        writer.write(dom4jElement);
        return outStringWriter.toString();
    }

}//end TrustmarkDefinitionV1_0Importer
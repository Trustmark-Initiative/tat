package nstic.web

import edu.gatech.gtri.trustmark.v1_0.FactoryLoader
import edu.gatech.gtri.trustmark.v1_0.io.SerializerFactory
import edu.gatech.gtri.trustmark.v1_0.io.TrustmarkDefinitionResolver
import grails.converters.JSON
import grails.converters.XML
import grails.plugin.springsecurity.annotation.Secured
import grails.gorm.transactions.Transactional
import nstic.web.assessment.Assessment
import nstic.web.assessment.AssessmentTrustmarkDefinitionLink
import nstic.web.assessment.Trustmark
import nstic.web.td.AssessmentStep
import nstic.web.td.AssessmentSubStep
import nstic.web.td.TrustmarkDefinition
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.apache.commons.lang.StringUtils
import org.springframework.web.multipart.MultipartFile

import javax.servlet.ServletException

@Secured("ROLE_USER")
@Transactional
class TrustmarkDefinitionController {

    def springSecurityService;
//    def tdImporterService;
    def fileService;

    def list() {
        log.debug("User[@|blue ${springSecurityService.currentUser}|@] listing trustmark definitions...")
        if( !params.max ){
            params.max = "10"
        }
        params.max = Math.min(5000, Integer.parseInt(params.max)).toString()

        def trustmarkDefinitions = TrustmarkDefinition.list(params);


        withFormat {
            html {
                [trustmarkDefinitions: trustmarkDefinitions, trustmarkDefinitionsCount: TrustmarkDefinition.count()]
            }
            json {
                log.debug("Styling JSON content...")
                def trustmarkDefJsonList = []
                trustmarkDefinitions.each{ td ->
                    def tdJson = [id:td.id, uri: td.uri, name:td.name, description: td.description, value: td.name, version: td.tdVersion]
                    trustmarkDefJsonList.add(tdJson)
                }
                render trustmarkDefJsonList as JSON
            }
            // TODO XML maybe?
        }
    }//end list()

    /**
     * Given a trustmark definition identifier from the database, this method will return a list of assessments
     * which are based on that TD.
     */
    def listAssessments() {
        User user = springSecurityService.currentUser;

        if( !params.id ){
            log.warn("Missing required parameter 'id'")
            throw new ServletException("Missing required parameter 'id'")
        }
        TrustmarkDefinition td = TrustmarkDefinition.get(params.id);
        if (!td ){
            log.warn("Invalid Trustmark Definition identifier: ${params.id}")
            throw new ServletException("Invalid Trustmark Definition identifier: ${params.id}")
        }

        log.info("Finding all assessments for TD[${td.id}: ${td.uniqueDisplayName}]")
        List<Assessment> assessments = AssessmentTrustmarkDefinitionLink.findAllByTrustmarkDefinition(td).collect{ it.assessment }
        if( !assessments ) assessments = []

        log.debug("Found ${assessments.size()} assessments for TD[${td.id}: ${td.uniqueDisplayName}]")
        def assessmentsJSON = []
        assessments.each{ Assessment ass ->
            def assessmentJSON = [
                    id: ass.id,
                    name: ass.assessmentName,
                    status: ass.status?.toString(),
                    createdBy: ass.createdBy.toJsonMap(true),
                    assessedOrganization: ass.assessedOrganization?.toJsonMap(true),
                    assessedContact: ass.assessedContact?.toJsonMap(true),
                    dateCreated: ass.dateCreated,
                    assignedTo: ass.assignedTo?.toJsonMap(true),
                    lastAssessor: ass.lastAssessor?.toJsonMap(true),
                    comment: ass.comment?.toString()
            ]

            log.debug("Adding assessment #${ass.id} json...")
            assessmentsJSON.add(assessmentJSON);
        }

        withFormat {
            html {
                throw new ServletException("No HTML format for trustmark definition assessment list yet.  Try JSON.")
            }
            json {
                render assessmentsJSON as JSON
            }
            xml {
                render assessmentsJSON as XML
            }
        }
    }//end listAssessments()

    def enable(){
        log.info("Request to enable TrustmarkDefinition[${params.id}]...")
        TrustmarkDefinition td = TrustmarkDefinition.findById(params.id);
        if( !td )
            throw new ServletException("No such TrustmarkDefinition: ${params.id}")

        td.enabled = true;
        td.save(failOnError: true, flush: true);

        flash.message = "Successfully enabled trustmark definition: "+td.name

        def statusJson = [status: 'SUCCESS', message: "Successfully enabled trustmark definition: "+td.name];
        render statusJson as JSON
    }

    def disable(){
        log.info("Request to disable TrustmarkDefinition[${params.id}]...")
        TrustmarkDefinition td = TrustmarkDefinition.findById(params.id);
        if( !td )
            throw new ServletException("No such TrustmarkDefinition: ${params.id}")

        td.enabled = false;
        td.save(failOnError: true, flush: true);

        flash.message = "Successfully disabled trustmark definition: "+td.name

        def statusJson = [status: 'SUCCESS', message: "Successfully disabled trustmark definition: "+td.name];
        render statusJson as JSON
    }

    /**
     *
     */
    def uploadNewPage() {
        log.info("Showing user upload page...")
    }

    def downloadCachedSource(){
        log.info("Request to download cached source of TrustmarkDefinition[${params.id}]...")
        TrustmarkDefinition td = null;

        td = TrustmarkDefinition.findById(params.id);
        if( !td ){
            td = TrustmarkDefinition.findByUri(params.id);
        }

        if( !td )
            throw new ServletException("No such TrustmarkDefinition: ${params.id}")

        return render(contentType: td.source.mimeType, text: td.source.content.toFile().text);
    }

    def view(){
        log.info("Request to view TrustmarkDefinition[${params.id}]...")
        TrustmarkDefinition td = null;

        td = TrustmarkDefinition.findById(params.id);
        if( !td ){
            td = TrustmarkDefinition.findByUri(params.id);
        }

        if( !td )
            throw new ServletException("No such TrustmarkDefinition: ${params.id}")

        log.debug("Resolving TD from file...");
        edu.gatech.gtri.trustmark.v1_0.model.TrustmarkDefinition tmfTd = null;
        File sourceFile = td.source.content.toFile();
        tmfTd = FactoryLoader.getInstance(TrustmarkDefinitionResolver.class).resolve(sourceFile);

        withFormat{
            html {
                [databaseTd: td, trustmarkDefinition: tmfTd]
            }
            xml {
                StringWriter writer = new StringWriter();
                FactoryLoader.getInstance(SerializerFactory.class).getXmlSerializer().serialize(tmfTd, writer)
                return render(contentType: 'text/xml', text: writer.toString());
            }
            json {
                StringWriter writer = new StringWriter();
                FactoryLoader.getInstance(SerializerFactory.class).getJsonSerializer().serialize(tmfTd, writer)
                return render(contentType: 'application/json', text: writer.toString());
            }
        }

    }//end view()

    def viewByStep(){
        log.info("Request to view TrustmarkDefinition for AssessmentStep[${params.stepId}] or AssessmentSubStep[${params.substepId}]...")
        TrustmarkDefinition td = null;

        if( StringUtils.isNotBlank(params.stepId) ) {
            AssessmentStep step = AssessmentStep.findById(params.stepId);
            if (!step)
                throw new ServletException("No such AssessmentStep: ${params.stepid}")

            td = step.trustmarkDefinition;
            log.info("Resolved assessment step ${params.stepid} to td ${td.uri}")

        }else if( StringUtils.isNotBlank(params.substepId) ){
            AssessmentSubStep substep = AssessmentSubStep.findById(params.substepId);
            if (!substep)
                throw new ServletException("No such AssessmentSubStep: ${params.substepId}")

            td = substep.assessmentStep.trustmarkDefinition;
            log.info("Resolved assessment substep ${params.substepId} to td ${td.uri}")
        }else{
            throw new ServletException("Either 'stepId' or 'substepId' parameter is required!");
        }

        withFormat{
            html {
                BinaryObject cachedHtml = td.cachedHtml;
                log.debug("Rendering cachedHtml[size: ${cachedHtml.fileSize}]...")
                response.setHeader("Content-length", cachedHtml.fileSize.toString())
                File cachedHtmlAsFile = cachedHtml.content.toFile();
                FileInputStream fileInputStream = new FileInputStream(cachedHtmlAsFile);
                log.info("Returning...")
                return render(file: fileInputStream, contentType: "text/html"); // Note xhtml does not work in firefox.
            }
            xml {
                BinaryObject originalXml = td.originalXml;
                log.debug("Rendering cachedHtml[size: ${originalXml.fileSize}]...")
                response.setHeader("Content-length", originalXml.fileSize.toString())
                response.setHeader("Content-Disposition", "attachment; filename= ${td.name}-${td.version}.xml")
                File cachedXmlAsFile = originalXml.content.toFile();
                FileInputStream fileInputStream = new FileInputStream(cachedXmlAsFile);
                log.info("Returning...")
                return render(file: fileInputStream, contentType: "application/xml");
            }
            json {
                throw new UnsupportedOperationException("NOT YET IMPLEMENTED - No JSON Output for TD yet.")
            }
        }

    }//end view()


    def typeahead() {
        log.debug("User[@|blue ${springSecurityService.currentUser}|@] searching[@|cyan ${params.q}|@] via TrustmarkDefinition typeahead...")

        // TODO Instead of this, integrate the searchable plugin
        def criteria = TrustmarkDefinition.createCriteria();
        def results = criteria {
            or {
                like("name", '%'+params.q+'%')
                like("description", '%'+params.q+'%')
                like("tdVersion", '%'+params.q+'%')
            }
            maxResults(25)
            order("name", "asc")
        }

        withFormat {
            html {
                throw new ServletException("NOT SUPPORTED")
            }
            xml {
                render results as XML
            }
            json {
                def resultsJSON = []
                results.each{ result ->
                    resultsJSON.add([
                        id: result.id,
                        name: result.name,
                        description: result.description,
                        tdVersion: result.tdVersion
                    ])
                }
                render resultsJSON as JSON
            }
        }

    }//end typeahead


    //==================================================================================================================
    //  Private Helper Methods
    //==================================================================================================================

    private List<File> findTdXmlFiles(File directory) {
        return FileUtils.listFiles(directory, ["xml"] as String[], true);
    }


    /**
     * Explodes the given zip file to a location on the filesystem, and returns the extracted folder.
     */
    private File extract(File zipFile){
        File tempDir = File.createTempFile("td-extracted-zip-", ".dir");
        tempDir.delete();
        tempDir.mkdirs();
        // Nice bit of code from: http://stackoverflow.com/questions/645847/unzip-archive-with-groovy/2238489#2238489
        def ant = new AntBuilder()
        ant.unzip(  src: zipFile.canonicalPath,
                dest: tempDir.canonicalPath,
                overwrite:"true" )
        return tempDir;
    }//end extract()

}//end TrustmarkDefinitionController

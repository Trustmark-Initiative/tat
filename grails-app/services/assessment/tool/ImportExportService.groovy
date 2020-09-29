package assessment.tool

import nstic.util.ZipUtils
import nstic.web.BinaryObject
import nstic.web.ContactInformation
import nstic.web.Organization
import nstic.web.OrganizationArtifact
import nstic.web.OrganizationComment
import nstic.web.Role
import nstic.web.SystemVariable
import nstic.web.User
import nstic.web.assessment.ArtifactData
import nstic.web.assessment.Assessment
import nstic.web.assessment.AssessmentLog
import nstic.web.assessment.AssessmentLogEntry
import nstic.web.assessment.AssessmentStatus
import nstic.web.assessment.AssessmentStepData
import nstic.web.assessment.AssessmentStepResult
import nstic.web.td.AssessmentStep
import nstic.web.td.AssessmentStepArtifact
import nstic.web.td.TrustmarkDefinition
import org.apache.commons.codec.binary.Base64
import org.json.JSONArray
import org.json.JSONObject

import javax.xml.bind.DatatypeConverter
import java.text.SimpleDateFormat

class ImportExportService {

    public static final String EXPORT_THREAD_RUNNING = ImportExportService.class.getName()+".EXPORT_THREAD_RUNNING";
    public static final String EXPORT_THREAD_NAME = ImportExportService.class.getName()+".EXPORT_THREAD_NAME";
    public static final String EXPORT_THREAD_STATUS = ImportExportService.class.getName()+".EXPORT_THREAD_STATUS";

    public static final String IO_VERSION_ID = "20160303";

    /**
     * Performs the full system export, writing the results to a large-ish zip file.  Assumes that it is ran in a
     * separate non-request thread, so writes all messages to a database for communicating with a web browser.
     */
    public void doFullExport() {
        String threadName = Thread.currentThread().getName();
        log.info("Starting full assessment export from Thread["+threadName+"]...");
        SystemVariable.storeProperty(threadName+".STATUS", "RUNNING");
        SystemVariable.storeProperty(threadName+".PROGRESS", "0");
        SystemVariable.storeProperty(threadName+".MESSAGE", "Full System Export started at: "+new Date().toString());

        try {
            File tempDir = File.createTempDir("assessment-export", ".dir");
            File exportDir = new File(tempDir, "AssessmentExport_"+getDateString()); exportDir.mkdirs();

            exportUsers(threadName, exportDir);
            exportContacts(threadName, exportDir);
            exportOrgs(threadName, exportDir);
            exportBinaries(threadName, exportDir);
            exportAssessments(threadName, exportDir);

            SystemVariable.storeProperty(threadName+".PROGRESS", ""+100);
            SystemVariable.storeProperty(threadName+".MESSAGE", "Creating export ZIP file...");
            File zipFile = File.createTempFile("assessment_export-", ".zip");
            zipFile.delete();
            AntBuilder builder = new AntBuilder();
            builder.zip(destFile: zipFile.canonicalPath, level: '9') {
                fileset( dir: tempDir.canonicalPath ) {
                    include( name: '**/*' )
                }
            }

            log.info("Successfully created zip file: " + zipFile.canonicalPath);

            SystemVariable.storeProperty(EXPORT_THREAD_RUNNING, "false");
            SystemVariable.storeProperty(EXPORT_THREAD_NAME, "<no thread>");

            SystemVariable.storeProperty(threadName + ".STATUS", "FINISHED SUCCESSFULLY");
            SystemVariable.storeProperty(threadName + ".PROGRESS", "100");
            SystemVariable.storeProperty(threadName + ".MESSAGE", "Full System Export completed at: " + new Date().toString());
            SystemVariable.storeProperty(threadName + ".FILE", zipFile.canonicalPath)

        }catch(Throwable t){
            log.error("Error encountered during export process!", t);
            SystemVariable.storeProperty(threadName + ".STATUS", "ERROR");
            SystemVariable.storeProperty(threadName + ".PROGRESS", "100");
            SystemVariable.storeProperty(threadName + ".MESSAGE", "The following error was encountered: "+t.getMessage());
            SystemVariable.storeProperty(threadName + ".ERROR_DETAILS", t.toString());
        }

    }//end doFullExport()


    //==================================================================================================================
    //  Private Helper Methods
    //==================================================================================================================
    private void exportUsers(String threadName, File exportDir){
        log.debug("Selecting all User ids...")
        List<Long> userIds = []
        User.withTransaction {
            userIds = User.executeQuery('select id from User');
        }
        log.debug("There are "+userIds.size()+" Users to export.");

        JSONArray usersJSON = new JSONArray();
        for( int index = 0; index < userIds.size(); index++ ){
            Long userId = userIds.get(index);
            User.withTransaction {
                User user = User.get(userId);
                JSONObject userJSON = new JSONObject();
                userJSON.put("id", user.id);
                userJSON.put("username", user.username);
                userJSON.put("password", user.password);
                userJSON.put("enabled", user.enabled);
                userJSON.put("accountExpired", user.accountExpired);
                userJSON.put("accountLocked", user.accountLocked);
                userJSON.put("passwordExpired", user.passwordExpired);
                JSONArray authorities = new JSONArray();
                for( Role role : user.authorities ){
                    authorities.put(role.authority);
                }
                userJSON.put("authorities", authorities);
                userJSON.put("contactReference", user.contactInformation?.id ?: -1);
                userJSON.put("organizationReference", user.organization?.id ?: -1);
                usersJSON.put(userJSON);
            }
        }

        File usersFile = new File(exportDir, "index.users");
        usersFile.write(usersJSON.toString(2));
    }

    private void exportContacts(String threadName, File exportDir){
        log.debug("Selecting all ContactInformation ids...")
        List<Long> contactIds = []
        ContactInformation.withTransaction {
            contactIds = ContactInformation.executeQuery('select id from ContactInformation');
        }
        log.debug("There are "+contactIds.size()+" ContactInformation objects to export.");

        JSONArray contactsJSON = new JSONArray();
        for( int index = 0; index < contactIds.size(); index++ ){
            Long userId = contactIds.get(index);
            ContactInformation.withTransaction {
                ContactInformation contact = ContactInformation.get(userId);
                JSONObject contactJSON = new JSONObject();
                contactJSON.put("id", contact.id);
                contactJSON.put("email", contact.email);
                contactJSON.put("responder", contact.responder);
                contactJSON.put("phoneNumber", contact.phoneNumber);
                contactJSON.put("mailingAddress", contact.mailingAddress);
                contactJSON.put("notes", contact.notes);
                contactsJSON.put(contactJSON);
            }
        }

        File contactsFile = new File(exportDir, "index.contacts");
        contactsFile.write(contactsJSON.toString(2));
    }

    private void exportOrgs(String threadName, File exportDir){
        log.debug("Selecting all ContactInformation ids...")
        List<Long> orgIds = []
        Organization.withTransaction {
            orgIds = Organization.executeQuery('select id from Organization');
        }
        log.debug("There are "+orgIds.size()+" Organization objects to export.");

        JSONArray orgsJSON = new JSONArray();
        for( int index = 0; index < orgIds.size(); index++ ){
            Long orgId = orgIds.get(index);
            Organization.withTransaction {
                Organization org = Organization.get(orgId);
                JSONObject orgJSON = new JSONObject();
                orgJSON.put("id", org.id);
                orgJSON.put("uri", org.uri);
                orgJSON.put("identifier", org.identifier);
                orgJSON.put("name", org.name);
                orgJSON.put("primaryContactReference", org.primaryContact?.id ?: -1);

                JSONArray adminReferences = new JSONArray();
                for( User admin : org.administrators ?: []){
                    adminReferences.put(admin.id);
                }
                orgJSON.put("adminReferences", adminReferences);

                JSONArray assessorRefs = new JSONArray();
                for( User assessor : org.assessors ?: []){
                    assessorRefs.put(assessor.id);
                }
                orgJSON.put("assessorReferences", assessorRefs);

                JSONArray contactRefs = new JSONArray();
                for( ContactInformation ci : org.contacts ?: []){
                    contactRefs.put(ci.id);
                }
                orgJSON.put("contactReferences", contactRefs);

                JSONArray commentsJSON = new JSONArray();
                for(OrganizationComment comment : org.comments ?: []){
                    JSONObject commentJSON = new JSONObject();
                    commentJSON.put("id", comment.id);
                    commentJSON.put("title", comment.title);
                    commentJSON.put("comment", comment.comment);
                    commentJSON.put("userReference", comment.user?.id ?: -1);
                    commentJSON.put("dateCreated", JSONExporterForAssessments.toDateTimeString(comment.dateCreated));
                    commentsJSON.put(commentJSON);
                }
                orgJSON.put("comments", commentsJSON);

                JSONArray orgArtifactsJSON = new JSONArray();
                for(OrganizationArtifact artifact : org.artifacts ?: []){
                    JSONObject artifactJSON = new JSONObject();
                    artifactJSON.put("id", artifact.id);
                    artifactJSON.put("dateCreated", JSONExporterForAssessments.toDateTimeString(artifact.dateCreated));
                    artifactJSON.put("displayName", artifact.displayName);
                    artifactJSON.put("description", artifact.description);
                    artifactJSON.put("active", artifact.active);
                    artifactJSON.put("uploadingUserReference", artifact.uploadingUser?.id ?: -1);
                    artifactJSON.put("binaryObjectReference", artifact.data?.id ?: -1);
                    orgArtifactsJSON.put(artifactJSON);
                }
                orgJSON.put("artifacts", orgArtifactsJSON);

                orgsJSON.put(orgJSON);
            }
        }

        File orgsFile = new File(exportDir, "index.organizations");
        orgsFile.write(orgsJSON.toString(2));
    }



    /**
     * This method will export ALL BinaryObjects from the database.  They are referenced from exported Assessments and
     * therefore required.
     */
    private void exportBinaries(String threadName, File exportDir){
        File binaryObjectsSubDir = new File(exportDir, "binaryObjects"); binaryObjectsSubDir.mkdirs();

        log.debug("Selecting all BinaryObject ids...")
        List<Long> binaryObjectIds = []
        BinaryObject.withTransaction {
            binaryObjectIds = BinaryObject.executeQuery('select id from BinaryObject');
        }
        log.debug("There are "+binaryObjectIds.size()+" BinaryObjects to export.");

        Map<File, Map> boFileMap = [:]
        for( int index = 0; index < binaryObjectIds.size(); index++ ) {

            Long boId = binaryObjectIds.get(index);
            File binaryOutFile = new File(binaryObjectsSubDir, "binaryObject-"+boId+"_"+getDateString()+".json");
            BinaryObject.withTransaction {
                BinaryObject binaryObject = BinaryObject.get(boId);

                JSONObject binaryObjectJSON = new JSONObject();
                binaryObjectJSON.put("id", binaryObject.id);
                binaryObjectJSON.put("md5sum", binaryObject.md5sum);
                binaryObjectJSON.put("mimeType", binaryObject.mimeType);
                binaryObjectJSON.put("originalFilename", binaryObject.originalFilename);
                binaryObjectJSON.put("originalExtension", binaryObject.originalExtension);
                binaryObjectJSON.put("fileSize", binaryObject.fileSize);
                binaryObjectJSON.put("dateCreated", JSONExporterForAssessments.toDateTimeString(binaryObject.dateCreated));
                binaryObjectJSON.put("createdBy", binaryObject.createdBy);
                binaryObjectJSON.put("content", base64ThisFile(binaryObject.content.toFile()));
                binaryOutFile.write(binaryObjectJSON.toString(2));

                boFileMap.put(binaryOutFile, [id: boId, fileName: binaryObject.originalFilename, fileSize: binaryObject.fileSize, mimeType: binaryObject.mimeType]);

                int percentage = Math.round( (((double) index / (double) binaryObjectIds.size())* 100.0d  ));
                SystemVariable.storeProperty(threadName+".PROGRESS", ""+percentage);
                SystemVariable.storeProperty(threadName+".MESSAGE", "Successfully exported BinaryObject "+binaryObject.originalFilename);

                if( (percentage % 10) == 0 )
                    log.info("We are ${percentage}% complete with binary objects export.")
            }
        }

        log.debug("Writing binaryObject index...");
        File boIndex = new File(exportDir, "index.binaryObjects");
        JSONArray boIndexJson = new JSONArray();
        for( File file : boFileMap.keySet() ){
            Map data = boFileMap.get(file);
            JSONObject currentIndexObj = new JSONObject();
            currentIndexObj.put("id", data.get("id"));
            currentIndexObj.put("fileName", data.get("fileName"));
            currentIndexObj.put("fileSize", data.get("fileSize"));
            currentIndexObj.put("mimeType", data.get("mimeType"));
            currentIndexObj.put("file", "binaryObjects/" + file.name);
            boIndexJson.put(currentIndexObj);
        }
        boIndex.write(boIndexJson.toString(2));

    }//end exportBinaries()

    /**
     * Reads the file into memory, and returns the base64 representation of the file.
     */
    private String base64ThisFile(File file){
        byte[] fileBytes = file.bytes;
        return new String(Base64.encodeBase64(fileBytes));
    }

    private void exportAssessments(String threadName, File exportDir){
        File assessmentsSubDir = new File(exportDir, "assessments"); assessmentsSubDir.mkdirs();
        log.debug("Selecting all assessment ids...")
        List<Long> assessmentIds = []
        Assessment.withTransaction {
            assessmentIds = Assessment.executeQuery('select id from Assessment');
        }
        log.debug("There are "+assessmentIds.size()+" assessments to export.");

        Map<Long, File> assessmentFileMap = [:]
        for( int index = 0; index < assessmentIds.size(); index++ ){
            Long assId = assessmentIds.get(index);
            Assessment.withTransaction {
                Assessment assessment = Assessment.get(assId);
                File currentAssessmentFile = exportAssessment(assessment, assessmentsSubDir);
                assessmentFileMap.put(assId, currentAssessmentFile);
            }

            int percentage = Math.round( (((double) index / (double) assessmentIds.size())* 100.0d  ));
            SystemVariable.storeProperty(threadName+".PROGRESS", ""+percentage);
            SystemVariable.storeProperty(threadName+".MESSAGE", "Successfully exported assessment "+assId);

            if( (percentage % 10) == 0 )
                log.info("We are ${percentage}% complete with assessments export.")
        }

        log.debug("Writing assessment index...");
        File assessmentIndex = new File(exportDir, "index.assessments");
        JSONArray assIndexJson = new JSONArray();
        for( Long id : assessmentFileMap.keySet() ){
            File file = assessmentFileMap.get(id);
            JSONObject currentIndexObj = new JSONObject();
            currentIndexObj.put("id", id);
            currentIndexObj.put("file", "assessments/" + file.name);
            assIndexJson.put(currentIndexObj);
        }
        assessmentIndex.write(assIndexJson.toString(2));

    }//end exportAssessments()

    private static String getDateString(){
        return new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
    }

    /**
     * Handles the task of writing the given assessment as a JSON file in the given folder.  Makes assumptions that
     * the binary objects are written elsewhere, and they can simply be referenced (NOTE: Not written herein).
     */
    private File exportAssessment(Assessment assessment, File containingFolder) {
        String assessmentFileId = "assessment-"+assessment.id+"_"+getDateString();
        JSONObject jsonObject = JSONExporterForAssessments.Assessment(assessment);
        File assessmentJsonFile = new File(containingFolder, assessmentFileId+".json");
        assessmentJsonFile.write(jsonObject.toString(2));
        return assessmentJsonFile
    }//end exportAssessment()




    //==================================================================================================================
    //  toJSON Methods
    //==================================================================================================================
    public static class JSONExporterForAssessments {
        public static String toDateTimeString(Date date){
            Calendar c = Calendar.getInstance();
            c.setTime(date);
            return toDateTimeString(c);
        }
        public static String toDateTimeString(Calendar calendar){
            return DatatypeConverter.printDateTime(calendar);
        }

        private static JSONObject createEmpty() {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("ExportTimestamp", toDateTimeString(Calendar.getInstance()));
            jsonObject.put("ExportVersion", IO_VERSION_ID);
            return jsonObject;
        }


        public static JSONObject Assessment(Assessment assessment) {
            JSONObject jsonObject = createEmpty();
            jsonObject.put("id", assessment.id);

            jsonObject.put("createdBy", assessment.createdBy);

            JSONArray tdsAssessed = new JSONArray();
            tdsAssessed.put(TrustmarkDefinition(assessment.trustmarkDefinition))
            jsonObject.put("AssessedTrustmarkDefinitions", tdsAssessed);

            JSONArray tipsAssessed = new JSONArray();
            jsonObject.put("AssessedTrustInteroperabilityProfiles", tipsAssessed);

            jsonObject.put("AssessedOrganization", Organization(assessment.assessedOrganization));
            jsonObject.put("AssessedContact", ContactInformation(assessment.assessedContact));

            jsonObject.put("createdDate", toDateTimeString(assessment.dateCreated));
            jsonObject.put("status", assessment.status.toString() ?: AssessmentStatus.UNKNOWN.toString());
            if( assessment.statusLastChangeUser )
                jsonObject.put("statusLastChangeUser", User(assessment.statusLastChangeUser));
            if( assessment.statusLastChangeDate )
                jsonObject.put("statusLastChangeDate", toDateTimeString(assessment.statusLastChangeDate));

            jsonObject.put("log", AssessmentLog(assessment.logg));

            if( assessment.assignedTo )
                jsonObject.put("assignedTo", User(assessment.assignedTo));
            if( assessment.lastAssessor )
                jsonObject.put("lastAssessor", User(assessment.lastAssessor));

            if( assessment.comment )
                jsonObject.put("comment", assessment.comment);
            if( assessment.commentLastChangeUser )
                jsonObject.put("commentLastChangeUser", User(assessment.commentLastChangeUser));
            if( assessment.commentLastChangeDate )
                jsonObject.put("commentLastChangeDate", toDateTimeString(assessment.commentLastChangeDate));

            JSONArray sortedStepJson = new JSONArray();
            for(AssessmentStepData stepData : assessment.getSortedSteps()) {
                sortedStepJson.put( AssessmentStepData(stepData) );
            }
            jsonObject.put("StepData", sortedStepJson);

            return jsonObject;
        }//end Assessment


        public static JSONObject User(User user){
            JSONObject jsonObject = createEmpty();

            jsonObject.put("id", user.id);
            jsonObject.put("username", user.username);

            // Rest of the data is not needed here.

            return jsonObject;
        }

        public static JSONObject Organization(Organization org){
            JSONObject jsonObject = createEmpty();

            jsonObject.put("id", org.id);
            jsonObject.put("uri", org.uri);
            jsonObject.put("identifier", org.identifier);
            jsonObject.put("name", org.name);

            // Rest of the data is not needed here.

            return jsonObject;
        }

        public static JSONObject ContactInformation(ContactInformation contact){
            JSONObject jsonObject = createEmpty();
            jsonObject.put("id", contact.id);
            jsonObject.put("email", contact.email);
            if( contact.responder )
                jsonObject.put("responder", contact.responder);
            if( contact.phoneNumber )
                jsonObject.put("phoneNumber", contact.phoneNumber);
            if( contact.mailingAddress )
                jsonObject.put("mailingAddress", contact.mailingAddress);
            if( contact.notes )
                jsonObject.put("notes", contact.notes);
            return jsonObject;
        }

        public static JSONObject TrustmarkDefinition(TrustmarkDefinition td){
            JSONObject jsonObject = createEmpty();
            jsonObject.put("uri", td.getUri());
            jsonObject.put("name", td.getName());
            jsonObject.put("version", td.getVersion());
            jsonObject.put("description", td.getDescription());

            // No other data is needed here.

            return jsonObject;
        }

        public static JSONArray AssessmentLog(AssessmentLog log){
            JSONArray logEntries = new JSONArray();
            if( log && log.entries && !log.entries.isEmpty() ){
                for(AssessmentLogEntry entry : log.getSortedEntries() ){
                    logEntries.put(AssessmentLogEntry(entry));
                }
            }
            return logEntries;
        }

        public static JSONObject AssessmentLogEntry(AssessmentLogEntry entry){
            JSONObject jsonObject = createEmpty();

            jsonObject.put("id", entry.id);
            jsonObject.put("dateCreated", toDateTimeString(entry.dateCreated))
            jsonObject.put("title", entry.title)
            jsonObject.put("type", entry.type)
            jsonObject.put("message", entry.message)
            jsonObject.put("data", entry.data)

            return jsonObject;
        }

        public static JSONObject AssessmentStepData(AssessmentStepData stepData){
            JSONObject jsonObject = createEmpty();

            jsonObject.put("id", stepData.id);
            jsonObject.put("AssessmentStep", AssessmentStep(stepData.step));
            jsonObject.put("result", stepData.result?.toString() ?: AssessmentStepResult.Not_Known.toString());
            if( stepData.lastResultUser )
                jsonObject.put("lastResultUser", User(stepData.lastResultUser));
            if( stepData.resultLastChangeDate )
                jsonObject.put("resultLastChangeDate", toDateTimeString(stepData.resultLastChangeDate));

            if( stepData.orgClaimsNonConformance )
                jsonObject.put("orgClaimsNonConformance", stepData.orgClaimsNonConformance);
            if( stepData.evidenceIndicatesNonConformance )
                jsonObject.put("evidenceIndicatesNonConformance", stepData.evidenceIndicatesNonConformance);
            if( stepData.evidenceIndicatesPartialConformance )
                jsonObject.put("evidenceIndicatesPartialConformance", stepData.evidenceIndicatesPartialConformance);
            if( stepData.orgCannotProvideSufficientEvidence )
                jsonObject.put("orgCannotProvideSufficientEvidence", stepData.orgCannotProvideSufficientEvidence);

            if( stepData.lastCheckboxUser )
                jsonObject.put("lastCheckboxUser", User(stepData.lastCheckboxUser));
            if( stepData.lastUpdated )
                jsonObject.put("lastUpdated", toDateTimeString(stepData.lastUpdated));

            jsonObject.put("assessorComment", stepData.assessorComment);
            if( stepData.assessorCommentUser )
                jsonObject.put("assessorCommentUser", User(stepData.assessorCommentUser));
            if( stepData.lastCommentDate )
                jsonObject.put("lastCommentDate", toDateTimeString(stepData.lastCommentDate));

            if( stepData.artifacts && !stepData.artifacts.isEmpty() ){
                JSONArray artifacts = new JSONArray();
                for(ArtifactData artifactData : stepData.artifacts ){
                    artifacts.put(ArtifactData(artifactData));
                }
                jsonObject.put("artifacts", artifacts);
            }

            return jsonObject;
        }

        public static JSONObject ArtifactData(ArtifactData ad){
            JSONObject jsonObject = createEmpty();

            jsonObject.put("id", ad.id);
            jsonObject.put("displayName", ad.displayName);
            jsonObject.put("comment", ad.comment);
            jsonObject.put("dateCreated", toDateTimeString(ad.dateCreated));
            if( ad.data )
                jsonObject.put("data", BinaryObject(ad.data));

            if( ad.uploadingUser )
                jsonObject.put("uploadingUser", User(ad.uploadingUser));
            if( ad.modifyingUser )
                jsonObject.put("modifyingUser", User(ad.modifyingUser));

            if( ad.requiredArtifact )
                jsonObject.put("requiredArtifact", AssessmentStepArtifact(ad.requiredArtifact));

            return jsonObject;
        }

        public static JSONObject AssessmentStepArtifact(AssessmentStepArtifact asa){
            JSONObject jsonObject = createEmpty();
            jsonObject.put("name", asa.name);
            jsonObject.put("description", asa.description);
            return jsonObject;
        }


        public static JSONObject BinaryObject(BinaryObject obj){
            JSONObject jsonObject = createEmpty();
            jsonObject.put("id", obj.id);
            jsonObject.put("md5sum", obj.md5sum);
            jsonObject.put("mimeType", obj.mimeType);
            jsonObject.put("originalFilename", obj.originalFilename);
            jsonObject.put("originalExtension", obj.originalExtension);
            jsonObject.put("fileSize", obj.fileSize);
            jsonObject.put("dateCreated", toDateTimeString(obj.dateCreated));
            jsonObject.put("createdBy", obj.createdBy);
            return jsonObject;
        }


        public static JSONObject AssessmentStep(AssessmentStep step){
            JSONObject jsonObject = createEmpty();

            jsonObject.put("id", step.id);
            jsonObject.put("trustmarkDefinition", step.trustmarkDefinition.uri);
            jsonObject.put("number", step.getStepNumber());
            jsonObject.put("name", step.getName());
            jsonObject.put("description", step.getDescription());

            return jsonObject;
        }


    }



}/* end ImportExportService */
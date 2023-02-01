package nstic.web

import grails.converters.JSON
import grails.converters.XML
import grails.gorm.transactions.Transactional
import nstic.util.AssessmentToolProperties
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.validation.ObjectError

import javax.servlet.ServletException

import org.grails.web.util.WebUtils

import grails.util.Environment

@Transactional
class DocumentsController {

    // document categories
    public static final String TM_RECIPIENT_AGREEMENT = "Trustmark Recipient Agreement"
    public static final String TM_RELYING_PARTY_AGREEMENT = "Trustmark Relying Party Agreement"
    public static final String TM_POLICY = "Trustmark Policy"
    public static final String TM_SIGNING_CERTIFICATE_POLICY = "Trustmark Signing Certificate Policy"

    // map["Category": isPublic]
    private static Map DOCUMENT_CATEGORIES = [(TM_RECIPIENT_AGREEMENT) : false,
                                              (TM_RELYING_PARTY_AGREEMENT) : true,
                                              (TM_POLICY) : true,
                                              (TM_SIGNING_CERTIFICATE_POLICY) : true]

    @PreAuthorize('hasAnyAuthority("tat-contributor", "tat-admin")')
    def index() {
        redirect(action:'list')
    }

    @PreAuthorize('hasAnyAuthority("tat-contributor", "tat-admin")')
    def list(){
        log.debug("Listing documents...")
        if (!params.max)
            params.max = '20';
        params.max = Math.min(100, Integer.parseInt(params.max)).toString(); // Limit to at most 100 docs at a time.

        User user = User.findByUsername(((OAuth2AuthenticationToken) SecurityContextHolder.getContext().getAuthentication()).getName())

        def documents = []
        int documentsCount = 0

        if (user.isUser()) {
            documents = Documents.findAllByPublicDocument(true)
            documents.addAll(Documents.findAllByDocumentCategoryAndOrganization(DocumentsController.TM_RECIPIENT_AGREEMENT, user.organization))
            documentsCount = documents.size()
        } else {
            documents = Documents.list(params)
            documentsCount = Documents.count()
        }

        [documents: documents, documentsCountTotal: documentsCount]
    }

    @PreAuthorize('hasAnyAuthority("tat-contributor", "tat-admin")')
    def add() {
        def docCategoryList = DOCUMENT_CATEGORIES.keySet() as ArrayList

        AddDocumentCommand addDocumentCommand = new AddDocumentCommand()

        [
                addDocumentCommand: addDocumentCommand,
                docCategoryList: docCategoryList
        ]
    }

    @PreAuthorize('hasAnyAuthority("tat-contributor", "tat-admin")')
    def edit() {
        def docCategoryList = DOCUMENT_CATEGORIES.keySet() as ArrayList

        log.info("Editing document record: [${params.id}]...")
        Documents doc = Documents.findById(params.id);
        if( !doc ) {
            log.info("doc == null...")
            throw new ServletException("No such document: ${params.id}")
        }

        if (!doc.organization) {
            log.info("doc.organization == null...")
        }

        EditDocumentCommand editDocumentCommand = new EditDocumentCommand()
        editDocumentCommand.setData(doc);
        [
                editDocumentCommand: editDocumentCommand,
                docCategoryList: docCategoryList
        ]
    }

    def view() {
        log.info("Viewing document record: [${params.id}]...")
        Documents doc = Documents.findById(params.id)
        if( !doc ) {
            log.info("doc == null...")
            throw new ServletException("No such document: ${params.id}")
        }

        log.debug("Rendering document view [id=${doc.id}] page for document #${doc.filename})...)")
        withFormat {
            html {
                [docId: doc.id, doc: doc]
            }
            json {
                render doc as JSON
            }
            xml {
                render doc as XML
            }
        }
    }

    @PreAuthorize('hasAnyAuthority("tat-contributor", "tat-admin")')
    def saveDocument(AddDocumentCommand uploadForm) {
        User user = User.findByUsername(((OAuth2AuthenticationToken) SecurityContextHolder.getContext().getAuthentication()).getName());
        if( !uploadForm.validate() ){
            log.warn "Upload Document form does not validate: "
            uploadForm.errors.getAllErrors().each { ObjectError error ->
                log.warn "    ${error.defaultMessage}, Args: ${error.arguments}"
            }

            def docCategoryList = DOCUMENT_CATEGORIES.keySet() as ArrayList

            return render(view: 'add', model: [command: uploadForm, docCategoryList: docCategoryList])
        }

        log.info("User[@|green ${user}|@] uploading document for organization[@|cyan ${uploadForm.organization}|@]...");
        Documents document = new Documents()
        document.organization = uploadForm.organization
        document.binaryObject = uploadForm.binaryObject
        document.description = uploadForm.description
        document.filename = uploadForm.filename
        document.documentCategory = uploadForm.documentCategory

        document.publicDocument = DOCUMENT_CATEGORIES[uploadForm.documentCategory]

        document.url = getDocumentUrl(uploadForm.binaryObject.id)

        document.publicUrl = getPublicDocumentUrl(uploadForm.filename)

        document.save(failOnError: true, flush: true)

        flash.message = "Successfully uploaded document '${uploadForm.filename}' for organization ${uploadForm.organization.name}"

        return redirect(action:'list')
    }

    @PreAuthorize('hasAnyAuthority("tat-contributor", "tat-admin")')
    def updateDocument(EditDocumentCommand editForm) {
        User user = User.findByUsername(((OAuth2AuthenticationToken) SecurityContextHolder.getContext().getAuthentication()).getName())
        if( !editForm.validate() ){
            log.warn "Edit Document form does not validate: "
            editForm.errors.getAllErrors().each { ObjectError error ->
                log.warn "    ${error.defaultMessage}, Args: ${error.arguments}"
            }

            def docCategoryList = DOCUMENT_CATEGORIES.keySet() as ArrayList

            return render(view: 'edit', model: [command: editForm, docCategoryList: docCategoryList])
        }

        log.info("User[@|green ${user}|@] editing document[${editForm.existingDocumentId}] " +
                "for organization[@|cyan ${editForm.organization}|@]...")
        Documents doc = Documents.findById(editForm.existingDocumentId)
        if( !doc ){
            log.warn("Bad document id[${editForm.existingDocumentId}] on org[${editForm.organization.identifier}]")
            throw new ServletException("Bad document id[${editForm.existingDocumentId}] on org[${editForm.organization.identifier}]")
        }

        doc.organization = editForm.organization
        doc.binaryObject = editForm.binaryObject
        doc.filename = editForm.filename
        doc.description = editForm.description

        doc.url = getDocumentUrl(editForm.binaryObject.id)

        doc.publicUrl = getPublicDocumentUrl(editForm.filename)

        doc.documentCategory = editForm.documentCategory

        doc.publicDocument = DOCUMENT_CATEGORIES[editForm.documentCategory]

        if( doc.binaryObject.id != editForm.binaryObject.id ){
            doc.binaryObject = editForm.binaryObject
        }
        log.debug("Performing update...")
        doc.save(failOnError: true, flush: true)

        flash.message = "Successfully updated document '${editForm.filename}' for organization ${editForm.organization.identifier}"

        log.debug("Redirecting to view document[${editForm.existingDocumentId}] page...");

        return redirect(action:'list')
    }

    @PreAuthorize('hasAnyAuthority("tat-contributor", "tat-admin")')
    def deleteDocument() {
        if( !params.orgId ){
            log.warn "Invalid org id!"
            throw new ServletException("Missing required id parameter.")
        }
        log.info("Organization ID: "+params.orgId)

        if( !params.documentId ){
            log.warn "Invalid document id!"
            throw new ServletException("Missing required id parameter.")
        }
        log.info("Document ID: "+params.documentId)

        Documents doc = Documents.findById(params.documentId)

        doc.delete(failOnError: true, flush: true)

        return redirect(action:'list')
    }

    def pdf() {
        log.debug("Viewing PDF document: ${params.documentId}...")

        // get the document associated to the binary id
        BinaryObject binaryObject = BinaryObject.findById(params.documentId);
        if( !binaryObject ) {
            throw new ServletException("No such binary object: ${params.documentId}")
        }

        Documents doc = Documents.findByBinaryObject(binaryObject);
        if( !doc ) {
            throw new ServletException("No such document asscociated to binary object id: ${params.documentId}")
        }

        boolean  isUser = ((OAuth2AuthenticationToken) SecurityContextHolder.getContext().getAuthentication()).authorities.contains((String)"tat-contributor")

        boolean isPublic = doc.publicDocument

        if (isUser || isPublic) {
            response.setHeader("Content-length", binaryObject.fileSize.toString())
            response.setHeader("Content-Disposition",
                    "inline; filename= ${URLEncoder.encode(binaryObject.originalFilename ?: "", "UTF-8")}")
            String mimeType = binaryObject.mimeType;
            if (mimeType == "text/xhtml") {
                mimeType = "text/html"; // A hack around XHTML display in browsers.
            }
            response.setContentType(mimeType);
            File outputFile = binaryObject.content.toFile();
            FileInputStream fileInputStream = new FileInputStream(outputFile);

            log.info("Rendering binary data...")

            return render(file: fileInputStream, contentType: mimeType);
        } else {
            return redirect(controller:'error', action:'notAuthorized401')
        }
    }

    private String getBaseAppUrl() {
        def request = WebUtils.retrieveGrailsWebRequest().getCurrentRequest()

        def protocol = "http://"
        if (request.isSecure()) {
            protocol = "https://"
        }
        StringBuilder sb = new StringBuilder(protocol)
        sb.append(request.getServerName())
        sb.append(':')
        sb.append(request.getServerPort())
        // getContextPath already has the '/' prepended
        sb.append(request.getContextPath())

        return sb.toString()
    }

    private String getDocumentUrl(Long documentId) {

        String baseAppUrl = getBaseAppUrl();

        StringBuilder sb = new StringBuilder(baseAppUrl)
        sb.append('/')
        sb.append('documents')
        sb.append('/')
        sb.append("pdf")
        sb.append('/')
        sb.append(documentId)
        return sb.toString()
    }

    private String getPublicDocumentUrl(String filename)  {
        return AssessmentToolProperties.getPublicDocumentApi()+'/'+filename
    }
}

class AddDocumentCommand {
    static Logger log = LoggerFactory.getLogger(AddDocumentCommand.class);

    String filename
    String url
    String documentCategory
    String description
    Organization organization
    Integer existingOrgId // Immutable database key (in case they selected an existing organziation)

    String organizationName
    BinaryObject binaryObject // do I need this?
    Boolean publicDocument = Boolean.TRUE

    static constraints = {
        //documentId(nullable: false)
        url(nullable: true, maxSize: 65535)
        documentCategory(nullable: true, maxSize: 65535)
        existingOrgId(nullable: true, validator: {val, obj, errors ->
            log.debug("Upload document organization ID validation...")
            if( val ){
                log.debug("  Org id has a value: ${val}")
                Organization.withTransaction {
                    Organization org = Organization.get(val);
                    if( !org ){
                        log.warn("No such org: $val");
                        return "org.does.not.exist"
                    }else{
                        log.debug("Org ${val} exists, validating trustmark provider...");

                        log.debug("Org ${val} trusmark provider: ${org.isTrustmarkProvider}");
                        log.debug("Document category: ${obj.documentCategory}");

                        // test trustmark provider
                        if (!org.isTrustmarkProvider && !obj.documentCategory.equals(DocumentsController.TM_RECIPIENT_AGREEMENT)) {
                            errors.rejectValue("organizationName", "org.trustmark.provider.public-documents", [obj.organizationName] as String[],
                                    "Only trustmark provider organizations can upload public documents.")
                            return false;
                        }

                        log.debug("Org ${val} exists, validating URI...");
                        Organization uriConflictOrg = Organization.findByName(obj.organizationName);
                        if( uriConflictOrg ) {
                            log.debug("For URN ${obj.organizationName}, found org: ${uriConflictOrg.name}")
                            if (uriConflictOrg && uriConflictOrg.id != val) {
                                errors.rejectValue("organizationName", "org.name.exists", [obj.organizationName] as String[], "Organizaiton Name ${obj.organizationName} already exists.")
                                return false;
                            } else {
                                log.debug("A urn conflict was not detected.")
                            }
                        }else{
                            log.debug("No Org with Name[${obj.organizationName}] exists in database to conflict.")
                        }

                        Organization nameConflictOrg = Organization.findByName(obj.organizationName);
                        if( nameConflictOrg ) {
                            log.debug("For name ${obj.organizationName}, found org: ${nameConflictOrg.uri}")
                            if (nameConflictOrg && nameConflictOrg.id != val) {
                                errors.rejectValue("organizationName", "org.name.exists", [obj.organizationName] as String[], "Organizaiton name ${obj.organizationName} already exists.")
                                return false;
                            } else {
                                log.debug("A name conflict was not detected.")
                            }
                        }else{
                            log.debug("No Org Name[${obj.organizationName}] exists in the database to conflict.")
                        }
                    }
                }
            }else{
                log.debug("No value was given in the existing org id field.")
            }
            log.debug("Successfully validated org id.")
        })
        organizationName(nullable: false, blank: false, maxSize: 128, validator: { val, obj, errors ->
            if( !obj.existingOrgId ) {
                Organization nameConflictOrg = Organization.findByName(val);
                if( nameConflictOrg ){
                    log.debug("For name ${val}, found org[${nameConflictOrg.id}]: ${nameConflictOrg.urn}")
                    errors.rejectValue("organizationName", "org.name.exists", [val] as String[], "Organizaiton name ${val} already exists.")
                    return false;
                } else {
                    log.debug("A name conflict was not detected.")
                }
            }
        })
        binaryObject(nullable: false)
        filename(nullable: false, blank: false, maxSize: 256, validator: {val, obj, errors ->

            log.debug("Check that file [${val}] is a pdf file...")

            def ext = val.substring(val.lastIndexOf(".") + 1).toLowerCase()
            if (ext != "pdf") {
                errors.rejectValue('filename', 'not.pdf.filename', "File name '${val}' must be a PDF file.")
                return false
            }

            log.debug("Check organization documents for duplicate for file [${val}]...")

            log.debug("Organization id: [${obj.existingOrgId}]...")
            Organization org = Organization.get(obj.existingOrgId)
            if (org) {
                obj.organization = org
            }

            if( obj.organization && obj.organization.documents && !obj.organization.documents.isEmpty() ) {
                for (Documents doc : obj.organization.getDocuments()) {
                    if (doc == null) {
                        log.debug("null document...")
                    }
                    if( doc && doc.getFilename().equalsIgnoreCase(val) ){
                        errors.rejectValue('filename', 'duplicate.filename', "File name '${val}' is already in use.")
                        return false;
                    }
                    log.debug("valid document [${val}]...")
                }
            }

            return true;
        })
        description(nullable: true, maxSize: 65535)
        publicDocument(nullable: false)
    }
}

class EditDocumentCommand {
    static Logger log = LoggerFactory.getLogger(EditDocumentCommand.class)

    public void setData(Documents doc){
        log.info("EditDocumentCommand for org: [${doc.organization.name}]...")

        this.existingDocumentId = doc.id
        this.existingOrgId = doc.organization.id
        this.organization = doc.organization
        this.organizationName = doc.organization.name
        this.binaryObject = doc.binaryObject
        this.filename = doc.filename
        this.description = doc.description
        this.url = doc.url
        this.documentCategory = doc.documentCategory
        this.publicDocument = doc.publicDocument
    }

    Integer existingDocumentId
    Integer existingOrgId // Immutable database key (in case they selected an existing organziation)
    String filename
    String url
    String documentCategory
    String description
    Organization organization
    String organizationName
    BinaryObject binaryObject // do I need this?
    Boolean publicDocument = Boolean.TRUE

    static constraints = {
        url(nullable: true, maxSize: 65535)
        documentCategory(nullable: true, maxSize: 65535)
        existingOrgId(nullable: true, validator: {val, obj, errors ->
            log.debug("Edit Document org ID validation...")
            if( val ){
                log.debug("  Org id has a value: ${val}")
                Organization.withTransaction {
                    Organization org = Organization.get(val);
                    if( !org ){
                        log.warn("No such org: $val");
                        return "org.does.not.exist"
                    }else{
                        log.debug("Org ${val} exists, validating URI...");
                        Organization uriConflictOrg = Organization.findByName(obj.organizationName);
                        if( uriConflictOrg ) {
                            log.debug("For URN ${obj.organizationName}, found org: ${uriConflictOrg.name}")
                            if (uriConflictOrg && uriConflictOrg.id != val) {
                                errors.rejectValue("organizationName", "org.name.exists", [obj.organizationName] as String[], "Organizaiton Name ${obj.organizationName} already exists.")
                                return false;
                            } else {
                                log.debug("A urn conflict was not detected.")
                            }
                        }else{
                            log.debug("No Org with Name[${obj.organizationName}] exists in database to conflict.")
                        }

                        Organization nameConflictOrg = Organization.findByName(obj.organizationName);
                        if( nameConflictOrg ) {
                            log.debug("For name ${obj.organizationName}, found org: ${nameConflictOrg.uri}")
                            if (nameConflictOrg && nameConflictOrg.id != val) {
                                errors.rejectValue("organizationName", "org.name.exists", [obj.organizationName] as String[], "Organizaiton name ${obj.organizationName} already exists.")
                                return false;
                            } else {
                                log.debug("A name conflict was not detected.")
                            }
                        }else{
                            log.debug("No Org Name[${obj.organizationName}] exists in the database to conflict.")
                        }
                    }
                }
            }else{
                log.debug("No value was given in the existing org id field.")
            }
            log.debug("Successfully validated org id.")
        })
        organizationName(nullable: false, blank: false, maxSize: 128, validator: { val, obj, errors ->
            log.info("constraints for organizationName: [${obj.organizationName}]...")
            Organization nameConflictOrg = Organization.findByName(val)
            if( !nameConflictOrg ){
                log.debug("Organization name ${val} does not exists")
                errors.rejectValue("organizationName", "org.does.not.exist", [val] as String[], "Organization does not exists.")
                return false
            } else {
                log.debug("Valid organization.")
            }
        })
        binaryObject(nullable: false)
        filename(nullable: false, blank: false, maxSize: 256, validator: {val, obj, errors ->
            log.debug("Check organization documents for duplicate for file [${val}]...")
            if( obj.organization && obj.organization.documents && !obj.organization.documents.isEmpty() ) {
                for (Documents doc : obj.organization.getDocuments()) {
                    if (doc == null) {
                        log.debug("null document...")
                    }

                    log.debug("obj.existingDocumentId [${obj.existingDocumentId}]...")
                    log.debug("doc.id [${doc.id}]...")

                    if( doc && obj.existingDocumentId != doc.id && doc.getFilename().equalsIgnoreCase(val) ){
                        errors.rejectValue('filename', 'duplicate.filename', "File name '${val}' is already in use.")
                        return false;
                    }
                    log.debug("valid document [${val}]...")
                }
            }
            return true;
        })
        description(nullable: true, maxSize: 65535)
        publicDocument(nullable: false)
    }
}
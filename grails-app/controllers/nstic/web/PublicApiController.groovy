package nstic.web

import edu.gatech.gtri.trustmark.v1_0.FactoryLoader
import edu.gatech.gtri.trustmark.v1_0.impl.model.TrustmarkStatusReportImpl
import edu.gatech.gtri.trustmark.v1_0.io.Serializer
import edu.gatech.gtri.trustmark.v1_0.io.SerializerFactory
import edu.gatech.gtri.trustmark.v1_0.model.TrustmarkStatusCode
import edu.gatech.gtri.trustmark.v1_0.model.TrustmarkStatusReport
import grails.converters.JSON
import grails.converters.XML
import grails.plugin.springsecurity.SpringSecurityUtils
import nstic.util.AssessmentToolProperties
import nstic.web.assessment.Trustmark
import nstic.web.assessment.TrustmarkStatus
import nstic.web.td.TrustmarkDefinition
import org.apache.commons.lang.StringUtils

import javax.servlet.ServletException
import java.text.SimpleDateFormat

class PublicApiController {

    final String STATUS = "/status/"

    def index() {
    }

    def documents() {
    }

    def trustmarks() {
    }

    /**
     * Called to view a trustmark in the system.
     */
    def view() {
        if( StringUtils.isEmpty(params.id) )  {
            render (status:404, text: "Missing argument identifier")
            return
        }

        Trustmark trustmark = Trustmark.findByIdentifier(params.id)
        if( !trustmark )  {
            render (status:404, text: "Trustmark not found: ${params.id}")
            return
        }

        // set to public URL for documents
        trustmark.policyPublicationURL = generateDocumentUrl(trustmark.policyPublicationURL.substring(trustmark.policyPublicationURL.lastIndexOf('/')+1))
        trustmark.relyingPartyAgreementURL = generateDocumentUrl(trustmark.relyingPartyAgreementURL.substring(trustmark.relyingPartyAgreementURL.lastIndexOf('/')+1))

        // set to public status report
        trustmark.statusURL = generateTrustmarkStatusUrl(trustmark.identifier)

        withFormat {
            html {
                [trustmark: trustmark]
            }
            json {
                render (contentType: 'text/html', text: 'JSON not supported, yet')
            }
            jwt {
                render(contentType: 'text/jwt', text: trustmark.signedJson)
            }
            xml {
                render(contentType: 'text/xml', text: trustmark.signedXml)
            }
        }
    }

    /**
     * return pdf document when requested by name
     * @return
     */
    def pdfByName() {
        log.debug("Viewing PDF document: ${params.name}...")

        Documents doc = Documents.findByFilename(params.name)
        if( doc == null) {
            render (status:404, text: "No such document named: ${params.name}")
            return
        }

        // get the document associated to the binary id
        BinaryObject binaryObject = BinaryObject.findById(doc.binaryObjectId);
        if( !binaryObject ) {
            throw new ServletException("No such binary object: ${doc.binaryObjectId}")
        }

        boolean  isUser = SpringSecurityUtils.ifAllGranted("ROLE_USER")
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

    /**
     * find public documents
     * @return
     */
    def findDocs() {
        log.info("Finding documents ...")
        List<PublicDocument> documents = new ArrayList<>()
        if(params.id != null)  {
            try  {
                Integer id = Integer.parseInt(params.id)
                log.info("findDocs by Id...")
                Documents.findAllByIdAndPublicDocument(id, true).forEach(
                        {d -> documents.add(new PublicDocument(d.filename, generateDocumentUrl(d.filename), d.description, d.documentCategory, d.dateCreated))
                        })
            } catch (NumberFormatException nfe)  {
                log.info("findDocs by Filename...")
                if(params.id == "ALL")  {
                    Documents.findAllByPublicDocument(true).forEach(
                            {d -> documents.add(new PublicDocument(d.filename, generateDocumentUrl(d.filename), d.description, d.documentCategory, d.dateCreated))
                            })
                } else {
                    Documents.findAllByFilenameLikeAndPublicDocument("%"+params.id+"%", true).forEach(
                            {d -> documents.add(new PublicDocument(d.filename, generateDocumentUrl(d.filename), d.description, d.documentCategory, d.dateCreated))
                            })
                }
            }
        }

        withFormat {
            json {
                render documents as JSON
            }
            xml {
                render documents as XML
            }
        }
    }

    /**
     * Called to view a trustmark status report in the system.
     */
    def status() {
        if( StringUtils.isEmpty(params.id) )  {
            render (status:404, text: "Missing required parameter: id")
            return
        }
        Trustmark trustmark = Trustmark.findByIdentifier(params.id)

        if( !trustmark )  {
            render (status:404, text: "Trustmark not found: ${params.id}")
            return
        }

        TrustmarkStatusReport trustmarkStatusReport = new TrustmarkStatusReportImpl()
        trustmarkStatusReport.setTrustmarkReference(new URI(trustmark.identifierURL))

        switch(trustmark.status)  {
            case TrustmarkStatus.OK:
            case TrustmarkStatus.ACTIVE:
                trustmarkStatusReport.setStatus(TrustmarkStatusCode.ACTIVE)
                break
            case TrustmarkStatus.REVOKED:
                trustmarkStatusReport.setStatus(TrustmarkStatusCode.REVOKED)
                break
            case TrustmarkStatus.EXPIRED:
                trustmarkStatusReport.setStatus(TrustmarkStatusCode.EXPIRED)
                break
        }
        trustmarkStatusReport.setStatusDateTime(new Date())

        log.info("trustmark.status.report ${trustmarkStatusReport.trustmarkReference},  ${trustmarkStatusReport.status},  ${trustmarkStatusReport.statusDateTime}")
        SerializerFactory factory = FactoryLoader.getInstance(SerializerFactory.class)

        withFormat {
            html {
                [trustmarkStatusReport: trustmarkStatusReport]
            }
            json {
                Serializer serializer = factory.getJsonSerializer()
                StringWriter output = new StringWriter()
                serializer.serialize(trustmarkStatusReport, output)
                render output.toString()
            }
            xml {
                Serializer serializer = factory.getXmlSerializer()
                StringWriter output = new StringWriter()
                serializer.serialize(trustmarkStatusReport, output)
                render(contentType: 'text/xml', text: output.toString())
            }
        }
    }

    /**
     * Called to find a trustmark in the system.
     */
    def findMany() {
        log.info("trustmark.findMany ${params.td} ${params.recipient} ${params.fromDate} ${params.toDate}")

        Map results = [:]

        List<PublicTrustmark> trustmarks = new ArrayList<>()

            if (StringUtils.isNotEmpty(params.td))  {
                try  {
                    TrustmarkDefinition td = TrustmarkDefinition.findById(Long.parseLong(params.td))
                    log.info("findMany by TD Id ${params.td}")  // trustmark definition id search
                    Trustmark.findAllByTrustmarkDefinition(td).forEach {
                        t ->
                            Organization org = Organization.findById(t.recipientOrganization.id)
                            trustmarks.add(new PublicTrustmark(td.name, generateTrustmarkUrl(t.identifier), generateTrustmarkStatusUrl(t.identifier), org.name, org.uri, td.uri, t.status.toString()))
                    }
                } catch (NumberFormatException nfe)  {
                    log.info("findMany by TD Name ${params.td}")  // trustmark definition name search
                    TrustmarkDefinition.findAllByNameLike("%"+params.td+"%").forEach({td ->
                        Trustmark.findAllByTrustmarkDefinition(td).forEach {
                            t ->
                                Organization org = Organization.findById(t.recipientOrganization.id)
                                trustmarks.add(new PublicTrustmark(td.name, generateTrustmarkUrl(t.identifier), generateTrustmarkStatusUrl(t.identifier), org.name, org.uri, td.uri, t.status.toString()))
                        }
                    })
                }
            } else if (StringUtils.isNotEmpty(params.recipient)) {
                try  {
                    ContactInformation ci = ContactInformation.findById(Integer.parseInt(params.recipient))
                    log.info("findMany by Contact Id ${params.recipient}")  // recipient id search
                    Trustmark.findAllByRecipientContactInformation(ci).forEach {
                        t ->
                            TrustmarkDefinition td = TrustmarkDefinition.findById(t.trustmarkDefinition.id)
                            Organization org = Organization.findById(t.recipientOrganization.id)
                            trustmarks.add(new PublicTrustmark(td.name, generateTrustmarkUrl(t.identifier), generateTrustmarkStatusUrl(t.identifier), org.name, org.uri, td.uri, t.status.toString()))
                    }
                }  catch (NumberFormatException nfe)  {
                    log.info("findMany by Contact Responder ${params.recipient}")
                    ContactInformation.findAllByEmailLike("%"+params.recipient+"%").forEach({ci ->
                        Trustmark.findAllByRecipientContactInformation(ci).forEach {
                            t ->
                                TrustmarkDefinition td = TrustmarkDefinition.findById(t.trustmarkDefinition.id)
                                Organization org = Organization.findById(t.recipientOrganization.id)
                                trustmarks.add(new PublicTrustmark(td.name, generateTrustmarkUrl(t.identifier), generateTrustmarkStatusUrl(t.identifier), org.name, org.uri, td.uri, t.status.toString()))
                        }
                    })
                }
            } else if (StringUtils.isNotEmpty(params.fromDate) && StringUtils.isNotEmpty(params.toDate))  {
                Date fromDate = new SimpleDateFormat("MM/dd/yyyy").parse(params.fromDate)
                Date toDate = new SimpleDateFormat("MM/dd/yyyy").parse(params.toDate)

                // To allow searches with the from and to dates being the same, add a close to
                // 24 hour offset to the to date.
                Calendar cal = Calendar.getInstance()
                cal.setTime(toDate)

                cal.add(Calendar.HOUR_OF_DAY, 23)
                cal.add(Calendar.MINUTE, 59)
                toDate = cal.getTime()

                Trustmark.findAllByIssueDateTimeBetween(fromDate, toDate).forEach {
                    t ->
                        TrustmarkDefinition td = TrustmarkDefinition.findById(t.trustmarkDefinition.id)
                        Organization org = Organization.findById(t.recipientOrganization.id)
                        trustmarks.add(new PublicTrustmark(td.name, generateTrustmarkUrl(t.identifier), generateTrustmarkStatusUrl(t.identifier), org.name, org.uri, td.uri, t.status.toString()))
                }
            }

        if(trustmarks.isEmpty())  {
            results.put("message", "Search did not match any trustmarks!")
        }

        results.put("trustmarks", trustmarks)

        withFormat {
            json {
                render results as JSON
            }
            xml {
                render results as XML
            }
        }
    }

    /**
     * Called to find all trustmarks issued to a trustmark recipient id.
     */
    def findByRecipient() {
        log.info("trustmark.findByRecipients ${params.recipientId}")

        String decoded = decodeURIComponent(params.recipientId)

        String decodedRecipientId = new String(decoded.decodeBase64())

        List<PublicTrustmark> trustmarks = new ArrayList<>()

        if (StringUtils.isNotEmpty(decodedRecipientId))  {
            Organization recipientOrganization = Organization.findByUri(decodedRecipientId)

            Trustmark.findAllByRecipientOrganization(recipientOrganization).forEach {
                t ->
                    TrustmarkDefinition td = TrustmarkDefinition.findById(t.trustmarkDefinition.id)
                    Organization org = Organization.findById(t.recipientOrganization.id)
                    trustmarks.add(new PublicTrustmark(td.name, generateTrustmarkUrl(t.identifier), generateTrustmarkStatusUrl(t.identifier), org.name, org.uri, td.uri, t.status.toString(),
                            t.signedXml, t.signedJson, t.hasExceptions, t.assessorComments))
            }
        }

        def result = ["trustmarks" : trustmarks]

        withFormat {
            json {
                render result as JSON
            }
            xml {
                render result as XML
            }
        }
    }

    /**
     * TAT Status.
     */
    def serverStatus() {
        log.info("TAT status...")

        def tatStatus = [
                status: "OK"
        ]

        withFormat {
            json {
                render tatStatus as JSON
            }
        }
    }

    private String decodeURIComponent(String s)
    {
        if (s == null)
        {
            return null;
        }

        String result = null;

        try
        {
            result = URLDecoder.decode(s, "UTF-8");
        }

        // This exception should never occur.
        catch (UnsupportedEncodingException e)
        {
            result = s;
        }

        return result;
    }

    /**
     * generates the Trustmark URL from the trustmark id
     * @param trustmarkId
     * @return
     */
    private String generateTrustmarkUrl(String trustmarkId)  {
        return AssessmentToolProperties.getPublicTrustmarkApi()+'/'+trustmarkId
    }

    /**
     * generates the TSR url from the trustmark id
     * @param trustmarkId
     * @return
     */
    private String generateTrustmarkStatusUrl(String trustmarkId)  {
        return AssessmentToolProperties.getPublicTrustmarkApi()+'/status/'+trustmarkId
    }

    /**
     * generates the TSR url from the trustmark id
     * @param trustmarkId
     * @return
     */
    private String generateDocumentUrl(String docName)  {
        return AssessmentToolProperties.getPublicDocumentApi()+'/'+docName
    }
}

package nstic.web

/**
 * POJO class to container just the info we want to put on the wire
 */
class PublicTrustmark {
    String name
    String identifierURL
    String statusURL
    String recipient
    String organizationUri
    String trustmarkDefinitionURL
    String trustmarkStatus

    String signedXml
    String signedJson
    boolean hasExceptions
    String assessorComments

    PublicTrustmark(String identifier, String idUrl, String statusUrl, String recipient, String orgUri, String tdUrl, String tmStatus)  {
        this.name = identifier
        this.identifierURL = idUrl
        this.statusURL = statusUrl
        this.recipient = recipient
        this.organizationUri = orgUri
        this.trustmarkDefinitionURL = tdUrl
        if(tmStatus == "OK")  {
            this.trustmarkStatus = "ACTIVE"
        } else {
            this.trustmarkStatus = tmStatus
        }
    }

    PublicTrustmark(String identifier, String idUrl, String statusUrl, String recipient, String orgUri, String tdUrl, String tmStatus,
                    String signedXml, String signedJson, boolean hasExceptions, String assessorComments)  {
        this.name = identifier
        this.identifierURL = idUrl
        this.statusURL = statusUrl
        this.recipient = recipient
        this.organizationUri = orgUri
        this.trustmarkDefinitionURL = tdUrl
        if(tmStatus == "OK")  {
            this.trustmarkStatus = "ACTIVE"
        } else {
            this.trustmarkStatus = tmStatus
        }

        this.signedXml = signedXml
        this.signedJson = signedJson

        this.hasExceptions = hasExceptions
        this.assessorComments = assessorComments
    }
}


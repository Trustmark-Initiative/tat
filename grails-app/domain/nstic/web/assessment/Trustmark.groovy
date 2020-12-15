package nstic.web.assessment

import nstic.web.BinaryObject
import nstic.web.ContactInformation
import nstic.web.Organization
import nstic.web.SigningCertificate
import nstic.web.User
import nstic.web.td.TrustmarkDefinition

/**
 * Represents a Trustmark that was granted.
 *
 * Created by brad on 9/5/14.
 */
class Trustmark {

    static belongsTo = [
        assessment: Assessment,
        trustmarkDefinition: TrustmarkDefinition
    ]

    User grantingUser;
    String identifier; // By default, a UUID.
    String identifierURL;
    Date issueDateTime;
    Date expirationDateTime;

    String policyPublicationURL;
    String relyingPartyAgreementURL;
    String statusURL;

    String signedXml;
    String signedJson;

    int signingCertificateId;

    Organization providerOrganization;
    ContactInformation providerContactInformation;

    Organization recipientOrganization;
    ContactInformation recipientContactInformation;

    String definitionExtension;
    String providerExtension;

    BinaryObject generatedXml;

    Boolean hasExceptions = Boolean.FALSE;
    String assessorComments;

    static hasMany = [parameterValues: ParameterValue]

    // Status Information
    TrustmarkStatus status;
    User revokingUser;
    Date revokedTimestamp;
    String revokedReason;
    Trustmark supersededBy;

    static constraints = {
        grantingUser(nullable: false)
        assessment(nullable: false)
        trustmarkDefinition(nullable: false)
        identifier(nullable: false, blank: false, maxSize: 128)
        identifierURL(nullable: false, blank: false, maxSize: 1024)
        issueDateTime(nullable: false)
        expirationDateTime(nullable: false)
        policyPublicationURL(nullable: false, blank: false, maxSize: 1024)
        relyingPartyAgreementURL(nullable: false, blank: false, maxSize: 1024)
        statusURL(nullable: false, blank: false, maxSize: 1024)
        signedXml(nullable:false, blank:false, maxSize: 65535)
        signedJson(nullable:false, blank:false, maxSize: 65535)
        signingCertificateId(nullable: false)
        definitionExtension(nullable: true, blank: true, maxSize: 65535)
        providerExtension(nullable: true, blank: true, maxSize: 65535)
        providerOrganization(nullable: false)
        providerContactInformation(nullable: false)
        recipientOrganization(nullable: false)
        recipientContactInformation(nullable: false)
        generatedXml(nullable: true)
        hasExceptions(nullable: false)
        assessorComments(nullable: true, blank: true, maxSize: 65535)
        status(nullable: false)
        revokingUser(nullable: true)
        revokedTimestamp(nullable: true)
        revokedReason(nullable: true, blank: true, maxSize: 65535)
        supersededBy(nullable: true)
        parameterValues(nullable: true)
    }

    static mapping = {
        table(name: 'trustmark')
        assessment(column: 'assessment_ref')
        trustmarkDefinition(column: 'trustmark_definition_ref')
        grantingUser(column: 'granting_user_ref')
        identifierURL(type: 'text', column: 'identifier_url')
        policyPublicationURL(type: 'text', column: 'policy_publication_url')
        relyingPartyAgreementURL(type: 'text', column: 'relying_party_agreement_url')
        statusURL(type: 'text', column: 'status_url')
        definitionExtension(type: 'text', column: 'definition_extension')
        providerExtension(type: 'text', column: 'provider_extension')

        providerOrganization(column: 'provider_organization_ref')
        providerContactInformation(column: 'provider_contact_information_ref')
        recipientOrganization(column: 'recipient_organization_ref')
        recipientContactInformation(column: 'recipient_contact_information_ref')

        generatedXml(column: 'generated_xml_ref')
        signedXml(type: 'text', column: 'signed_xml')
        signedJson(type: 'text', column: 'signed_json')
        revokedReason(type: 'text')
        supersededBy(column: 'superseded_by_ref')

        hasExceptions(column: 'has_exceptions')
        assessorComments(type: 'text', column: 'assessor_comments')
    }

    public Map toJsonMap(boolean shallow = false){
        log.info("Trustmark to JSON Map...")
        def json = [
                id: this.id,
                identifier: this.identifier,
                issueDateTime: this.issueDateTime.getTime(),
                expirationDateTime: this.expirationDateTime?.getTime() ?: -1,
                policyPublicationURL:  this.policyPublicationURL ?: "",
                relyingPartyAgreementURL: this.relyingPartyAgreementURL ?: "",
                statusURL: this.statusURL ?: "",
                hasExceptions: this.hasExceptions,
                assessorComments: this.assessorComments ?: "",
                status: this.status?.toString(),
                revokingUser: this.revokingUser? [id: this.revokingUser.id, usenrame: this.revokingUser.username] : null,
                revokedTimestamp: this.revokedTimestamp?.getTime() ?: -1,
                revokedReason: this.revokedReason ?: "",
                supersededBy: this.supersededBy ? [id: this.supersededBy.id] : null,
                grantingUser: [
                        id: this.grantingUser.id,
                        username: this.grantingUser.username
                ],
                trustmarkDefinition: [
                        id: this.trustmarkDefinition.id,
                        uri: this.trustmarkDefinition.uri,
                        name: this.trustmarkDefinition.name,
                        version: this.trustmarkDefinition.tdVersion
                ],
                assessment: [
                        id: assessment.id
                ],
                provider: [
                        organization: [
                                id: this.providerOrganization.id,
                                uri: this.providerOrganization.uri,
                                name: this.providerOrganization.name
                        ],
                        contactInformation: [
                                id: this.providerContactInformation.id,
                                responder: this.providerContactInformation.responder,
                                email: this.providerContactInformation.email
                        ]
                ],
                recipient: [
                        organization: [
                                id: this.assessment.assessedOrganization.id,
                                uri: this.assessment.assessedOrganization.uri,
                                name: this.assessment.assessedOrganization.name
                        ],
                        contactInformation: [
                                id: this.assessment.assessedContact.id,
                                responder: this.assessment.assessedContact.responder,
                                email: this.assessment.assessedContact.email
                        ]
                ],
                definitionExtension: this.definitionExtension ?: "",
                providerExtension: this.providerExtension ?: ""
        ]

        if( this.parameterValues != null && !this.parameterValues.isEmpty() ) {
            def paramValues = []
            for( ParameterValue pv : this.parameterValues ){
                paramValues.add([
                        identifier: pv.parameter.identifier,
                        kind: pv.parameter.kind,
                        value: pv.userValue
                ]);
            }
            json.put("parameters", paramValues);
        }

        return json;
    }//end toJsonMap()

}//end Trustmark
package tmf.assessment

class UrlMappings {

    static mappings = {
        "/$controller/$action?/$id?(.$format)?"{
            constraints {
                // apply constraints here
            }
        }

        "/" ( controller: 'home', action: 'index')

        "/user/$id/profile" (controller: 'profile', action: 'index')
        "/user/$id/profile/update" (controller: 'profile', action: 'update')

        "/trust-interoperability-profiles"(controller:'tip', action: 'list')
        "/trust-interoperability-profiles/enable"(controller:'tip', action: 'enable')
        "/trust-interoperability-profiles/disable"(controller:'tip', action: 'disable')
        "/trust-interoperability-profiles/$id"(controller:'tip', action: 'view')
        "/trust-interoperability-profiles/$id/cached"(controller:'tip', action: 'downloadCachedSource')

        "/trustmark-definitions"(controller:'trustmarkDefinition', action: 'list')
        "/trustmark-definitions/enable"(controller:'trustmarkDefinition', action: 'enable')
        "/trustmark-definitions/disable"(controller:'trustmarkDefinition', action: 'disable')
        "/trustmark-definitions/new"(controller:'trustmarkDefinition', action: 'create')
        "/trustmark-definitions/$id"(controller:'trustmarkDefinition', action: 'view')
        "/trustmark-definitions/$id/assessments"(controller:'trustmarkDefinition', action: 'listAssessments')
        "/trustmark-definitions/$id/cached"(controller:'trustmarkDefinition', action: 'downloadCachedSource')
        "/trustmark-definitions/by-step"(controller:'trustmarkDefinition', action: 'viewByStep')

        "/assessments"(controller:'assessment', action:'list')
        "/assessments/search"(controller:'assessmentSearch', action:'search')
        "/assessments/create"(controller:'assessment', action:'create')
        "/assessments/save"(controller:'assessment', action:'save')
        "/assessments/copy"(controller:'assessment', action:'copy')
        "/assessments/$id"(controller:'assessment', action:'view')
        "/assessments/$id/steps/$stepDataId"(controller:'assessment', action:'viewStepData')
        "/assessments/$id/available-artifacts"(controller:'assessment', action:'listAvailableArtifacts')
        "/assessments/$id/status-summary"(controller:'assessment', action:'getAssessmentStatusSummary')

        "/assessments/$id/log"(controller:'assessmentLog', action: 'viewLog')
        "/assessments/$id/log/entries/$entryId"(controller:'assessmentLog', action: 'viewLogEntry')

//        Note: Deprecated until further review
//        "/assessments/$assessmentId/steps/$stepNumber/substeps/$substepId/assessor-comment"(controller:'substepResolution', action: 'updateAssessorComment')
//        "/assessments/$assessmentId/steps/$stepNumber/substeps/$substepId/result"(controller:'substepResolution', action: 'updateSubstepResult')


        // Each of these methods are distinguished with /perform since they are valid during the assessment process only.
        //  They also imply that you can only see them if you own the assessment at the time.
        "/perform/assessments/$id/start"(controller:'assessmentPerform', action:'startAssessment')
        "/perform/assessments/$id"(controller:'assessmentPerform', action:'view')
        "/perform/assessments/$id/steps/$stepDataId/set-status"(controller:'assessmentPerform', action:'setStepDataStatus')
        "/perform/assessments/$id/steps/$stepDataId/set-comment"(controller:'assessmentPerform', action:'setStepDataComment')
        "/perform/assessments/$id/steps/$stepDataId/set-failure-reason"(controller:'assessmentPerform', action:'setFailureReason')
        "/perform/assessments/$id/steps/$stepDataId/create-artifact"(controller:'assessmentPerform', action:'createArtifact')
        "/perform/assessments/$id/steps/$stepDataId/save-artifact"(controller:'assessmentPerform', action:'saveArtifact')

//        Note: Deprecated until further review
//        "/perform/assessments/$id/steps/$stepDataId/substeps/create"(controller:'assessmentPerform', action:'createSubstep')
//        "/perform/assessments/$id/steps/$stepDataId/substeps/save"(controller:'assessmentPerform', action:'saveSubstep')
//        "/perform/assessments/$id/steps/$stepDataId/substeps/$substepId"(controller:'assessmentPerform', action:'viewSubstep')
//        "/perform/assessments/$id/steps/$stepDataId/substeps/$substepId/edit"(controller:'assessmentPerform', action:'editSubstep')
//        "/perform/assessments/$id/steps/$stepDataId/substeps/$substepId/update"(controller:'assessmentPerform', action:'updateSubstep')

        "/perform/assessments/$id/steps/$stepDataId/artifacts/$artifactId"(controller:'assessmentPerform', action:'viewArtifact')
        "/perform/assessments/$id/steps/$stepDataId/artifacts/$artifactId/edit"(controller:'assessmentPerform', action:'editArtifact')
        "/perform/assessments/$id/steps/$stepDataId/artifacts/$artifactId/update"(controller:'assessmentPerform', action:'updateArtifact')
        "/perform/assessments/$id/steps/$stepDataId/artifacts/$artifactId/delete"(controller:'assessmentPerform', action:'deleteArtifact')

        "/trustmark-metadata"(controller: "trustmarkMetadata", action: "list")
        "/trustmark-metadata/create"(controller: "trustmarkMetadata", action: "create")
        "/trustmark-metadata/save"(controller: "trustmarkMetadata", action: "save")
        "/trustmark-metadata/$id"(controller: "trustmarkMetadata", action: "view")
        "/trustmark-metadata/$id/edit"(controller: "trustmarkMetadata", action: "edit")
        "/trustmark-metadata/$id/update"(controller: "trustmarkMetadata", action: "update")

        "/trustmarks"(controller: "trustmark", action: "list")
        "/trustmarks/$id/recipient-organization"(controller: "trustmark", action: "list")
        "/trustmarks/create"(controller: "trustmark", action: "create") // Note parameter assessmentId holds the assessment id to create from.
        "/trustmarks/save"(controller: "trustmark", action: "save")
        "/trustmarks/$id"(controller: "trustmark", action: "view")
        "/trustmarks/$id/edit"(controller: "trustmark", action: "edit")
        "/trustmarks/$id/update"(controller: "trustmark", action: "update")
        "/trustmarks/$id/xml"(controller: "trustmark", action: "generateXml")
        "/trustmarks/$id/jwt"(controller: "trustmark", action: "generateJson")
        "/trustmarks/$id/status"(controller: "trustmark", action: "generateStatusXML")
        "/trustmarks/$id/revoke"(controller: "trustmark", action: "revoke")

        "/trustmarks/$originalCertId/reissue-trustmarks/$newCertId"(controller: "trustmark", action: "reissueTrustmarks")

        "/binaries"(controller:'binary', action:'list')
        "/binaries/upload"(controller:'binary', action:'upload')
        "/binaries/$id"(controller:'binary', action:'view')

//        "/forgot-password"(controller:'forgotPassword', action: 'index')

        "/users"(controller:'user', action: 'list')
        "/users/create"(controller: 'user', action: 'create')
        "/users/save"(controller: 'user', action: 'save')
        "/users/$id"(controller:'user', action: 'view')
        "/users/$id/edit"(controller: 'user', action: 'edit')
        "/users/$id/update"(controller: 'user', action: 'update')
        "/users/grant/$id"(controller: 'user', action: 'createFromGrant')

        "/contacts"(controller:'contactInformation', action: 'list')
        "/contacts/new"(controller:'contactInformation', action: 'create')
        "/contacts/save"(controller:'contactInformation', action: 'save')
        "/contacts/edit"(controller:'contactInformation', action: 'edit')
        "/contacts/update"(controller:'contactInformation', action: 'update')
        "/contacts/$id"(controller:'contactInformation', action: 'view')

        "/organizations"(controller:'organization', action: 'list')
        "/organizations/new"(controller:'organization', action: 'create')
        "/organizations/save"(controller:'organization', action: 'save')
        "/organizations/$id"(controller:'organization', action: 'view')
        "/organizations/$id/edit"(controller:'organization', action: 'edit')
        "/organizations/$id/update"(controller:'organization', action: 'update')
        "/organizations/$id/artifacts/new"(controller: 'organization', action: 'createArtifact')
        "/organizations/$id/artifacts/save"(controller: 'organization', action: 'saveArtifact')
        "/organizations/$id/artifacts/$artifactId/delete"(controller: 'organization', action: 'deleteArtifact')
        "/organizations/$id/artifacts/$artifactId/edit"(controller: 'organization', action: 'editArtifact')
        "/organizations/$id/artifacts/$artifactId/update"(controller: 'organization', action: 'updateArtifact')
        "/organizations/$id/contacts"(controller:'organization', action: 'listContacts')
        "/organizations/$id/unaffiliated-contacts"(controller:'organization', action: 'listUnaffiliatedContacts')
        "/organizations/$id/contacts/add"(controller:'organization', action: 'addContact')
        "/organizations/$id/contacts/$contactToRemove/remove"(controller:'organization', action: 'removeContact')
        "/organizations/$id/comments/new"(controller: 'organization', action: 'createComment')
        "/organizations/$id/comments/save"(controller: 'organization', action: 'saveComment')
        "/organizations/$id/comments/$commentId/edit"(controller: 'organization', action: 'editComment')
        "/organizations/$id/comments/$commentId/update"(controller: 'organization', action: 'updateComment')
        "/organizations/$id/comments/$commentId/delete"(controller: 'organization', action: 'deleteComment')

        "/documents"(controller:'documents', action: 'list')
        "/documents/add"(controller:'documents', action: 'add')
        "/documents/saveDocument"(controller: 'documents', action: 'saveDocument')
        "/documents/$id/edit"(controller:'documents', action: 'edit')
        "/documents/$id/updateDocument"(controller:'documents', action: 'updateDocument')
        "/documents/$id/view"(controller:'documents', action: 'view')
        "/documents/pdf/$documentId"(controller:'documents', action: 'pdf')

        "/signingcertificates/$id"(controller: "signingCertificates", action: "view")
        "/signingcertificates/$id/$certId?"(controller: "signingCertificates", action: "add")
        "/signingcertificates/$id/generate-certificate"(controller: "signingCertificates",
                action: "generateNewCertificateFromExpiredOrRevokedCertificate")
        "/signingcertificates/$id/generate-certificate-update-trustmark-metadata"(controller: "signingCertificates",
                action: "generateNewCertificateAndUpdateTrustmarkMetadataSets")
        "/signingcertificates/$id/generate-certificate-update-trustmark-metadata-reissue-trustmarks"(controller: "signingCertificates",
                action: "generateNewCertificateAndUpdateTrustmarkMetadataSetsAndReissueTrustmarks")
        "/signingcertificates/$id/update-trustmark-metadata/$newCertId"(controller: "signingCertificates",
                action: "updateTrustmarkMetadataSet")
        "/signingcertificates/$id/reissue-trustmarks-from-metadata/$selectedMetadataId"(controller: "signingCertificates",
                action: "reissueTrustmarksFromMetadataSet")

        "/public/status"(controller:'publicApi', action: 'serverStatus')

        "/publiccertificates/$filename"(controller:'publicCertificates', action: 'download', id: '$filename')

        "/public/trustmarks/$id"(controller: 'publicApi', action: 'view')
        "/public/trustmarks/find"(controller: 'publicApi', action: 'findMany')
        "/public/trustmarks/status/$id"(controller: 'publicApi', action: 'status')

        "/public/documents"(controller: 'publicApi', action: 'documents')

        "/public/documents/$id"(controller: 'publicApi', action: 'findDocs')


        "/public/documents/pdf/$name"(controller:'publicApi', action: 'pdfByName')

        "/public/trustmarks/find-by-recipient/$recipientId"(controller: 'publicApi', action: 'findByRecipient')

        "/reports" (controller:'reports', action: 'index')
        "/reports/overall" (controller:'reports', action: 'overallReport')
        "/reports/organization" (controller:'reports', action: 'organizationReport')
        "/reports/trustmark-definition" (controller:'reports', action: 'tdReport')

        "500"(controller:'error', action: 'error500')
        "404"(controller:'error', action: 'notFound404')
        "401"(controller:'error', action: 'notAuthorized401')
    }

}

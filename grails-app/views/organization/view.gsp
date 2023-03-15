<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main"/>
		<title>View Organization</title>

        <style type="text/css">

            .infoTable {

            }
            .infoTable td {
                padding: 0.5em;
            }

            .artifactActionCol {
                width: 80px;
                font-size: 120%;
            }

            .artifactActiveCol {
                width: 60px;
                text-align: center;
            }
            .artifactSizeCol {
                width: 80px;
                text-align: center;
            }
            .artifactNameCol {
                width: 400px;
            }
            .artifactDescCol {
                width: auto;
            }

        </style>
	</head>
	<body>

        <h1>View Organization</h1>
        <div class="pageSubsection">

        </div>

    <div id="messageContainer">
        <g:if test="${flash.message}">
            <div style="margin-top: 2em;" class="alert alert-info">
                ${flash.message}
            </div>
        </g:if>
        <g:if test="${flash.error}">
            <div style="margin-top: 2em;" class="alert alert-danger">
                ${flash.error}
            </div>
        </g:if>
    </div>

        <div class="pageContent">

            <!-- Org Info -->
            <div class="row" style="margin-top: 2em;">
                <div class="col-sm-6">
                    <h4>${organization.isTrustmarkProvider ? "Trustmark Provider " : "Trustmark Recipient "}Organization Information</h4>
                    <table class="infoTable table table-striped table-bordered table-condensed">
                        <tr>
                            <th>Abbreviation</th><td>${organization.identifier}</td>
                        </tr><tr>
                            <th>URI</th><td>${organization.uri}</td>
                        </tr><tr>
                            <th>Name</th><td>${organization.name}</td>
                        </tr>
                    </table>
                    <div style="margin-top: 2em;">
                        <g:link controller="organization" action="edit" params="[id: organization.id]" class="btn btn-default">
                            <span class="glyphicon glyphicon-edit"></span>
                            Edit
                        </g:link>
                    </div>
                </div>
                <div class="col-sm-6">
                    <h4>Primary Contact Information</h4>
                    <table class="infoTable table table-striped table-bordered table-condensed">
                        <tr>
                            <th>Responder Name</th><td>${organization.primaryContact?.responder}</td>
                        </tr><tr>
                            <th>Email</th><td>${organization.primaryContact?.email}</td>
                        </tr><tr>
                            <th>Phone Number</th><td>${organization.primaryContact?.phoneNumber}</td>
                        </tr><tr>
                            <th>Mailing Address</th><td>${organization.primaryContact?.mailingAddress}</td>
                        </tr><tr>
                            <th>Notes</th><td>${organization.primaryContact?.notes}</td>
                        </tr>
                    </table>
                </div>
            </div>

            <sec:authorize access="isAuthenticated()">
                <sec:authorize access="hasAuthority('tat-admin')">
                    <div style="margin-top: 2em;">
                        <h4>Trustmark Recipient Identifiers</h4>
                        <div class="sectionDescription  text-muted">
                            These Trustmark Recipient Identifiers are uniform resource identifiers (URIs) that are used for granting of trustmarks to this organization.
                        </div>
                        <table class="table table-bordered table-striped table-condensed">
                            <thead>
                            <tr>
                                <th>URI</th>
                                <th>Default</th>
                            </tr>
                            </thead>
                            <tbody>
                            <g:if test="${organization.trustmarkRecipientIdentifiers && !organization.trustmarkRecipientIdentifiers.isEmpty()}">
                                <g:each in="${organization.sortedTrustmarkRecipientIdentifiers}" var="trid">
                                    <td>
                                        <span>${trid.uri}</span>
                                    </td>
                                    <td class="certificateDefaultCol">
                                        <g:if test="${trid.defaultTrustmarkRecipientIdentifier}">
                                            <span>Yes</span>
                                        </g:if>
                                        <g:else>
                                            <span>No</span>
                                        </g:else>
                                    </td>
                                    </tr>
                                </g:each>

                            </g:if>
                            <g:else>
                                <tr>
                                    <td colspan="2">
                                        <em>There are no trustmark recipient identifierss tied to this organization.</em>
                                    </td>
                                </tr>
                            </g:else>
                            </tbody>
                        </table>
                    </div>
                </sec:authorize>
            </sec:authorize>

            <!-- Signing Certificates -->
            <g:if test="${organization.isTrustmarkProvider}">
                <sec:authorize access="isAuthenticated()">
                    <sec:authorize access="hasAuthority('tat-admin')">
                        <div style="margin-top: 2em;">
                            <h4>Signing Certificates</h4>
                            <div class="sectionDescription  text-muted">
                                These are the cryptographic keys and X.509 certificates that are used for digital signature and verification of Trustmarks that this Trustmark Provider issues.
                            </div>
                            <table class="table table-bordered table-striped table-condensed">
                                <thead>
                                    <tr>
                                        <th class="certificateActionCol certificateActionColHeader">&nbsp;</th>
                                        <th class="certificateDistinguishedNameCol certificateDistinguishedNameColHeader">Distinguished Name</th>
                                        <th class="certificateEmailAddressCol certificateEmailAddressColHeader">Email Address</th>
                                        <th class="certificateUrlCol certificateUrlColHeader">URL</th>
                                        <th class="certificateStatusCol certificateStatusColHeader">Status</th>
                                        <th class="certificateDefaultCol certificateDefaultColHeader">Default</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <g:if test="${organization.certificates && !organization.certificates.isEmpty()}">
                                        <g:each in="${organization.certificates}" var="cert">
                                            <tr>
                                                <td class="certificateActionCol">
                                                    <a href="${createLink(controller:'signingCertificates', action:'view', id:cert.id)}"
                                                        title="View ${cert.distinguishedName}">
                                                        <span class="glyphicon glyphicon-eye-open"></span>
                                                    </a>
                                                    <a href="${createLink(controller:'signingCertificates',action:'download', id:cert.id)}"
                                                        target="_blank" title="Download ${cert.distinguishedName}">
                                                        <span class="glyphicon glyphicon-download"></span>
                                                    </a>

                                                </td>
                                                <td class="certificateDistinguishedNameCol">
                                                    <a href="${createLink(controller:'signingCertificates', action:'view', id:cert.id)}"
                                                       title="View ${cert.distinguishedName}">
                                                        <span>${cert.distinguishedName}</span>
                                                    </a>
                                                </td>
                                                <td class="certificateEmailAddressCol">
                                                    ${cert.emailAddress}
                                                </td>
                                                <td class="certificateUrlCol">
                                                    <g:link url="${cert.certificatePublicUrl}">
                                                        ${cert.certificatePublicUrl}
                                                    </g:link>
                                                </td>
                                                <td class="certificateStatusCol">
                                                    <g:if test="${cert.status == nstic.web.SigningCertificateStatus.ACTIVE}">
                                                        <span style="color: darkgreen;" class="glyphicon glyphicon-ok-sign" title="Certificate still valid"></span>
                                                    </g:if>
                                                    <g:if test="${cert.status == nstic.web.SigningCertificateStatus.REVOKED}">
                                                        <span style="color: darkred;" class="glyphicon glyphicon-remove-sign" title="Certificate has been revoked."></span>
                                                    </g:if>
                                                    <g:if test="${cert.status == nstic.web.SigningCertificateStatus.EXPIRED}">
                                                        <span style="color: rgb(150, 150, 0);" class="glyphicon glyphicon-minus-sign" title="Certificate has expired."></span>
                                                    </g:if>
                                                </td>
                                                <td class="certificateDefaultCol">
                                                    <g:if test="${cert.defaultCertificate}">
                                                        <span>Yes</span>
                                                    </g:if>
                                                    <g:else>
                                                        <span>No</span>
                                                    </g:else>
                                                </td>
                                            </tr>
                                        </g:each>

                                    </g:if>
                                    <g:else>
                                        <tr>
                                            <td colspan="6">
                                                <em>There are no signing certificates tied to this organization.</em>
                                            </td>
                                        </tr>
                                    </g:else>
                                </tbody>
                            </table>
                            <div>
                                <g:link controller="signingCertificates" action="add" params="[orgId: organization.id]" class="btn btn-default">
                                    Generate New Certificate
                                </g:link>

                                <g:link controller="signingCertificates" action="importPkcs12" params="[orgId: organization.id]" class="btn btn-default">
                                    Import Signing Certificate
                                </g:link>

                            </div>
                        </div>
                    </sec:authorize>
                </sec:authorize>
            </g:if>

            <!-- Trustmarks -->
%{--            <div class="row" style="margin-top: 2em;">--}%
            <div style="margin-top: 2em;">
                <h4>Trustmarks</h4>
                <div class="sectionDescription text-muted">
                    The number of trustmarks granted to this organization.
                </div>

                <%

                    Console.println("=========================================================")
                    Console.println("Viewing Recipient Organization: " + organization.name)

                    Integer numberOfActiveTrustmarks = 0
                    Integer numberOfExpiredTrustmarks = 0
                    Integer numberOfRevokedTrustmarks = 0
                    List<nstic.web.assessment.Trustmark> trustmarks  = nstic.web.assessment.Trustmark.findAllByRecipientOrganization(organization)

                    if (trustmarks && trustmarks.size() > 0) {
                        trustmarks.each { nstic.web.assessment.Trustmark trustmark ->
                            if (trustmark.status == nstic.web.assessment.TrustmarkStatus.EXPIRED) {
                                numberOfExpiredTrustmarks++
                            } else if (trustmark.status == nstic.web.assessment.TrustmarkStatus.REVOKED){
                                numberOfRevokedTrustmarks++
                            } else {
                                numberOfActiveTrustmarks++;
                            }
                        }
                    }
                    Console.println("numberOfActiveTrustmarks: " + numberOfActiveTrustmarks)
                    Console.println("numberOfExpiredTrustmarks: " + numberOfExpiredTrustmarks)
                    Console.println("numberOfRevokedTrustmarks: " + numberOfRevokedTrustmarks)
                    Console.println("----------------------------------------------------------------------------------")
                %>

                <table class="infotable table table-bordered table-striped table-condensed"
                       style="white-space:nowrap;width:100%;">
                    <tr>
                        <th class="block" style="width:30%">Active Trustmarks</th>
                        <td class="block" style="width:40%">
                            <div>
                                <span id="activeTrustmarks">${numberOfActiveTrustmarks}</span>
                            </div>
                        </td>
                    </tr>
                        <th class="block" style="width:30%">Expired Trustmarks</th>
                        <td class="block" style="width:40%">
                            <div>
                                <span id="expiredTrustmarks">${numberOfExpiredTrustmarks}</span>
                            </div>
                        </td>
                    </tr>
                    <tr>
                        <th class="block" style="width:30%">Revoked Trustmarks</th>
                        <td class="block" style="width:40%">
                            <div>
                                <span id="revokedTrustmarks">${numberOfRevokedTrustmarks}</span>
                            </div>
                        </td>
                    </tr>
                    <tr>
                        <th class="block" style="width:30%">Total</th>
                        <td class="block" style="width:40%">
                            <a href="${createLink(controller:'trustmark', action:'list', id: organization.id)}" title="View Trustmarks">
                                <span id="totalTrustmarks">${numberOfActiveTrustmarks + numberOfExpiredTrustmarks + numberOfRevokedTrustmarks}</span>
                            </a>
                        </td>
                    </tr>
                </table>

                <sec:authorize access="hasAuthority('tat-admin')">
                    <g:if test="${numberOfActiveTrustmarks > 0}">
                        <div style="margin-top: 2em; margin-bottom: 3em;">
                            <a href="javascript:revokeAllTrustmarks()" class="btn btn-danger">Revoke All Active Trustmarks</a>
                            <div id="revokeAllTrustmarksStatusMessage">

                            </div>
                        </div>
                    </g:if>
                </sec:authorize>
                <script type="text/javascript">
                    function revokeAllTrustmarks() {

                        if( !confirm("Are you sure you want to revoke all trustmarks? This operation cannot be reversed.") ){
                            return;
                        }

                        var url = '${createLink(controller:'trustmark', action: 'revokeAll', id: organization.id)}';

                        var reason = prompt("What is the reason you are revoking all trustmarks?");
                        if( !reason){
                            alert("A reason is required.");
                        }else {

                            $.ajax({
                                url: url,
                                type: 'POST',
                                data: {
                                    format: 'json',
                                    reason: reason
                                },
                                beforeSend: function () {
                                    $('#revokeAllTrustmarksStatusMessage').html('<asset:image src="spinner.gif" /> Revoking all trustmarks...');
                                },
                                success: function (data, statusText, jqXHR) {

                                    // get trustmark stats
                                    $.ajax({
                                        url: '${createLink(controller: 'trustmark', action: 'getTrustmarkStats', id: organization.id)}',
                                        beforeSend: function() {
                                        },
                                        success: function(data, statusText, jqXHR){

                                            $('#activeTrustmarks').html(data["numberOfActiveTrustmarks"]);
                                            $('#expiredTrustmarks').html(data["numberOfExpiredTrustmarks"]);
                                            $('#revokedTrustmarks').html(data["numberOfRevokedTrustmarks"]);
                                            $('#totalTrustmarks').html(data["totalNumberOfTrustmarks"]);

                                            numberOfActiveTrustmarks = parseInt(data["numberOfActiveTrustmarks"]);
                                        },
                                        error: function(jqXHR, statusText, errorThrown){
                                        }
                                    });


                                    $('#revokeAllTrustmarksStatusMessage').html("All trustmarks have been revoked!");

                                },
                                error: function (jqXHR, statusText, errorThrown) {
                                    $('#revokeAllTrustmarksStatusMessage').html("An error has occurred...");
                                }
                            });
                        }

                    }
                </script>
            </div>

            <!-- Artifacts -->
            <div style="margin-top: 2em;">
                <h4>Artifacts</h4>
                <div class="sectionDescription  text-muted">
                    These artifacts are associated with this organization.  Any artifacts listed here (and marked as active)
                    will be displayed as a valid choice for any artifact requirement in an on-going assessment.
                </div>
                <table class="table table-bordered table-striped table-condensed">
                    <thead>
                        <tr>
                            <g:if test="${organization.artifacts && !organization.artifacts.isEmpty()}">
                                <th class="artifactActionCol artifactActionColHeader">&nbsp;</th>
                            </g:if>
                            <th class="artifactActiveCol artifactActiveColHeader">Active</th>
                            <th class="artifactSizeCol artifactSizeColHeader">Size</th>
                            <th class="artifactNameCol artifactNameColHeader">Artifact Name</th>
                            <th class="artifactDescCol artifactDescColHeader">Description</th>
                        </tr>
                    </thead>
                    <tbody>
                        <g:if test="${organization.artifacts && !organization.artifacts.isEmpty()}">
                            <g:each in="${organization.sortedArtifacts}" var="artifact">
                                <tr>
                                    <td class="artifactActionCol">
                                        <a href="${createLink(controller:'binary',action:'view', id:artifact.data.id)}"
                                                target="_blank" title="Download ${artifact.displayName}">
                                            <span class="glyphicon glyphicon-download"></span>
                                        </a>
                                        <a href="${createLink(controller:'organization', action: 'editArtifact', id: organization.identifier, params:[artifactId: artifact.id])}"
                                                title="Edit ${artifact.displayName}">
                                            <span class="glyphicon glyphicon-pencil"></span>
                                        </a>
                                        <a href="${createLink(controller:'organization', action: 'deleteArtifact', id: organization.identifier, params:[artifactId: artifact.id])}"
                                                onclick="return confirm('Really delete?');"
                                                title="Delete ${artifact.displayName}">
                                            <span class="glyphicon glyphicon-remove-sign"></span>
                                        </a>
                                    </td>
                                    <td class="artifactActiveCol">
                                        <g:if test="${artifact.active}">
                                            <span class="glyphicon glyphicon-ok"></span>
                                        </g:if>
                                        <g:else>
                                            <span class="glyphicon glyphicon-remove"></span>
                                        </g:else>
                                    </td>
                                    <td class="artifactSizeCol">
                                        ${org.apache.commons.io.FileUtils.byteCountToDisplaySize(artifact.data.fileSize)}
                                    </td>
                                    <td class="artifactNameCol">
                                        <g:if test="${org.apache.commons.lang.StringUtils.isNotBlank(artifact.displayName)}">
                                            ${artifact.displayName} <small style="color: #999">(${artifact.data.originalFilename})</small>
                                        </g:if>
                                        <g:else>
                                            ${artifact.data.originalFilename}
                                        </g:else>
                                    </td>
                                    <td class="artifactDescCol">
                                        ${artifact.description}
                                    </td>
                                </tr>
                            </g:each>

                        </g:if>
                        <g:else>
                            <tr>
                                <td colspan="4">
                                    <em>There are no artifacts tied to this organization.</em>
                                </td>
                            </tr>
                        </g:else>
                    </tbody>
                </table>
                <div>
                    <a href="${createLink(controller: 'organization', action: 'createArtifact', id: organization.identifier)}" class="btn btn-default">
                        <span class="glyphicon glyphicon-upload"></span>
                        Add
                    </a>
                </div>
            </div>


            <!-- Comments -->
            <div style="margin-top: 2em;">
                <h4>Assessor Comments</h4>
                <div class="sectionDescription text-muted">
                    Place comments here which contain useful information about this organization.  These comments will
                    be presented in the reports associated with this organization.
                </div>
                <table class="table table-striped table-bordered table-condensed">
                    <thead>
                    <tr>
                        <g:if test="${organization.comments && !organization.comments.isEmpty()}">
                            <td style="width: 50px;">&nbsp;</td>
                        </g:if>

                        <td style="width: 100px;">Date</td>
                        <td style="width: 150px;">User</td>
                        <td style="width: auto;">Comment</td>
                    </tr>
                    </thead>
                    <tbody>
                    <g:if test="${organization.comments && organization.comments.size() > 0}">
                        <g:each in="${organization.sortedComments}" var="comment">
                            <tr>
                                <td style="font-size: 120%;">
                                    <a href="${createLink(controller:'organization', action:'editComment', id: organization.identifier, params: [commentId: comment.id])}" title="Edit Comment">
                                        <span class="glyphicon glyphicon-pencil"></span>
                                    </a>
                                    <a onclick="return assertDeleteComment();" href="${createLink(controller:'organization', action:'deleteComment', id: organization.identifier, params: [commentId: comment.id])}" title="Delete Comment">
                                        <span class="glyphicon glyphicon-remove"></span>
                                    </a>

                                </td>
                                <td>
                                    <g:formatDate date="${comment.dateCreated}" format="yyyy-MM-dd" />
                                </td>
                                <td>
                                    ${comment.user.contactInformation.responder}
                                </td>
                                <td>
                                    <h5 style="font-weight: bold;">${comment.title}</h5>
                                    <div>
                                        ${comment.comment}
                                    </div>
                                </td>
                            </tr>
                        </g:each>
                    </g:if><g:else>
                        <tr>
                            <td colspan="3">
                                <em>There are no comments.</em>
                            </td>
                        </tr>
                    </g:else>
                    </tbody>
                </table>
                <div style="margin-top: 1em">
                    <a href="${createLink(controller:'organization', action:'createComment', id: organization.identifier)}" class="btn btn-default">
                        <span class="glyphicon glyphicon-plus-sign"></span>
                        New Comment
                    </a>
                </div>

            </div>


            <!-- Contacts List -->
            <div style="margin-top: 2em;">
                <h4>Organization Contacts</h4>
                <div class="sectionDescription text-muted">
                    Contacts who are associated with this organization.  Anyone in this list has the ability to view
                    reports for this organization.
                </div>
                <table class="table table-striped table-bordered table-condensed">
                    <thead>
                        <tr>
                            <g:if test="${organization.contacts && !organization.contacts.isEmpty()}">
                                <th style="width: 30px;">&nbsp;</th>
                            </g:if>

                            <th style="width: 150px;">Name</th>
                            <th style="width: 300px;">E-Mail</th>
                            <th style="width: 120px;">Phone Number</th>
                            <th style="width: auto;">Notes</th>
                        </tr>
                    </thead>
                    <tbody>
                        <g:if test="${organization.contacts && organization.contacts.size() > 0}">
                            <g:each in="${organization.contacts}" var="ci">
                                <tr>
                                    <td style="font-size: 120%;">
                                        <a onclick="return assertDeleteContact();" href="${createLink(controller:'organization', action:'removeContact', id: organization.id, params: [contactToRemove: ci.id])}"
                                                title="Remove ${ci.responder} from the contacts associated to ${organization.identifier}">
                                            <span class="glyphicon glyphicon-remove"></span>
                                        </a>
                                    </td>
                                    <td>${ci.responder}</td>
                                    <td>${ci.email}</td>
                                    <td>${ci.phoneNumber}</td>
                                    <td>${ci.notes}</td>
                                </tr>
                            </g:each>

                        </g:if>
                        <g:else>
                            <tr>
                                <td colspan="4">
                                    <em>There are no additional contacts for this organization.</em>
                                </td>
                            </tr>
                        </g:else>
                    </tbody>
                </table>
                <div style="margin-top: 1em;">
                    <form class="form-inline" action="${createLink(controller:'organization', action: 'addContact', id: organization.identifier)}" method="POST">
                        <div class="form-group" id="unaffiliatedDropdownContainer">

                        </div>
                        <button type="submit" class="btn btn-default">
                            <span class="glyphicon glyphicon-plus-sign"></span>
                            Add Contact
                        </button>
                        <a href="${createLink(controller:'contactInformation', action:'create')}" class="btn btn-default">
                            <span class="glyphicon glyphicon-plus-sign"></span>
                            New Contact
                        </a>
                    </form>
                </div>
            </div>

            <!-- Trustmark Metadata Sets -->
            <g:if test="${organization.isTrustmarkProvider}">
                <div style="margin-top: 2em;">
                <h4>Trustmark Metadata Sets</h4>
                <div class="sectionDescription text-muted">
                    A list of Trustmark Metadata sets for which this organization is the provider.
                </div>
                <table class="table table-striped table-bordered table-condensed">
                    <thead>
                    <tr>
                        <th style="width: 35%;">Name</th>
                        <th style="width: auto;">Description</th>
                    </tr>
                    </thead>
                    <tbody>
                    <g:if test="${metadataList && metadataList.size() > 0}">
                        <g:each in="${metadataList}" var="metadata">
                            <tr>
                                <td style="font-size: 120%;">
                                    <a href="${createLink(controller:'trustmarkMetadata', action:'view', id: metadata.id)}">
                                        ${metadata.name}
                                    </a>
                                </td>
                                <td>${metadata.description}</td>
                            </tr>
                        </g:each>

                    </g:if>
                    <g:else>
                        <tr>
                            <td colspan="2">
                                <em>This organization is not the provider for any TrustmarkMetadata.</em>
                            </td>
                        </tr>
                    </g:else>
                    </tbody>
                </table>
            </div>
            </g:if>
        </div>

        <script type="text/javascript">
            var organizationId = null;

            var UNAFFILIATED_CONTACTS = null;

            $(document).ready(function(){
                loadUnaffiliatedContacts();
            })

            function loadUnaffiliatedContacts(){
                $('#unaffiliatedDropdownContainer').html('<asset:image src="spinner.gif" /> Loading...');
                var url = '${createLink(controller:'organization',action: 'listUnaffiliatedContacts', id: organization.id)}';
                $.ajax({
                    url: url,
                    dataType: 'json',
                    data: {
                        now : new Date().getMilliseconds(),
                        format : 'json'
                    },
                    failure: function(jqXHR, statusText, errorThrown){
                        console.log("Error: "+statusText+", "+errorThrown);
                        $('#unaffiliatedDropdownContainer').html('<span class="text-danger">Error loading contacts</div>');
                    },
                    success: function(data, statusText, jqXHR){
                        console.log("Successfully got data: "+JSON.stringify(data, null, 4));
                        UNAFFILIATED_CONTACTS = data;
                        updateUnaffiliatedContactsView();
                    }
                })
            }

            function updateUnaffiliatedContactsView() {
                var html = '';
                if( UNAFFILIATED_CONTACTS && UNAFFILIATED_CONTACTS.length > 0 ){
                    html += '<select id="contactToAdd" name="contactToAdd" class="form-control">\n';
                    for( var i = 0; i < UNAFFILIATED_CONTACTS.length; i++ ){
                        var contact = UNAFFILIATED_CONTACTS[i];
                        html += '    <option value="'+contact.id+'">'+contact.responder+' &lt;'+contact.email+'&gt;</option>\n';
                    }
                    html += '</select>\n';
                }else{
                    html += '<span class="text-warning">There are no contacts.</span>'
                }

                $('#unaffiliatedDropdownContainer').html(html);
            }

            function assertDeleteComment(){
                return confirm('Really delete this comment?');
            }
            function assertDeleteContact(){
                return confirm('Really delete this contact?');
            }

            $("#importSigningCertificate").click(function() {
                var input = $(document.createElement("input"));
                input.attr("type", "file");
                input.on('change', function() {
                    var filename = input[0].files[0];
                    if (filename != undefined) {
                        importSigningCertificate(filename.name);
                    }
                });

                input.trigger("click"); // opening dialog
                return false; // avoiding navigation
            });

            // Import a PKCS12 based private key and X509 certificate
            function importSigningCertificate(filename){

                $.ajax({
                    url: '${createLink(controller: 'signingCertificates', action: 'importPkcs12File', id: 1)}',
                    type: 'POST',
                    data: {
                        format: 'json',
                        filename: filename
                    },
                    beforeSend: function () {
                        $('#importSigningCertificateStatus').html('<asset:image src="spinner.gif" /> Status: Importing PKCS12 file...');
                    },
                    success: function (data, statusText, jqXHR) {

                        $('#importSigningCertificateStatus').html("Status: Imported PKCS12 file!");
                    },
                    error: function (jqXHR, statusText, errorThrown) {
                        console.log("Error: " + errorThrown);

                        $('#importSigningCertificateStatus').html(errorThrown);
                    }
                });

            }
        </script>
	</body>
</html>

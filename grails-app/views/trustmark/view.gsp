<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main"/>
		<title>View Trustmark</title>

        <style type="text/css">

            .infoTable {

            }
            .infoTable td {
                padding: 0.5em;
            }
        </style>
	</head>
	<body>

        <h1>View Trustmark</h1>
        <div class="pageSubsection">

        </div>


        <div class="pageContent">
            <h4>Trustmark Information</h4>
            <table class="infoTable">
                <tr>
                    <td>Unique Database Id</td><td>${trustmark.id}</td>
                </tr>
                <tr>
                    <td>Identifier</td><td><a href="${trustmark.identifier}">${trustmark.identifier}</a></td>
                </tr>
                <tr>
                    <td>Identifier URL</td><td><a href="${trustmark.identifierURL}">${trustmark.identifierURL}</a></td>
                </tr>
                <tr>
                    <td>Assessment</td>
                    <td>
                        <a href="${createLink(controller:'assessment', action:'view', id:trustmark.assessment.id)}">
                            <tmpl:/templates/displayAssessmentStatusIcon assessment="${trustmark.assessment}" />
                            ${trustmark.assessment.assessmentName}
                        </a>
                    </td>
                </tr>
                <tr>
                    <td>Trustmark Definition</td>
                    <td>
                        <a href="${trustmark.trustmarkDefinition.uri}" target="_blank">
                            <span class="glyphicon glyphicon-tag"></span>
                        </a>
                        <a href="${createLink(controller:'trustmarkDefinition', action: 'view', id: trustmark.trustmarkDefinition.id)}">
                            ${trustmark.trustmarkDefinition.name}, version ${trustmark.trustmarkDefinition.tdVersion}
                        </a>
                    </td>
                </tr>

                <tr>
                    <td>Signing Certificate</td>
                    <td>
                        <%
                            nstic.web.SigningCertificate cert =  nstic.web.SigningCertificate.findById(trustmark.signingCertificateId)
                        %>
                        <g:link controller="signingCertificates" action="view" id="${cert.id}">
                            ${cert.distinguishedName}
                        </g:link>
                    </td>
                </tr>

                <tr>
                    <td>Issue Date Time</td>
                    <td>
                        <g:formatDate format="yyyy-MM-dd" date="${trustmark.issueDateTime}" />
                    </td>
                </tr>
                <tr>
                    <td>Expiration Date Time</td>
                    <td>
                        <g:formatDate format="yyyy-MM-dd" date="${trustmark.expirationDateTime}" />
                    </td>
                </tr>
                <tr>
                    <td>Recipient Organization</td><td>${trustmark.recipientOrganization.name}</td>
                </tr>
                <tr>
                    <td>Recipient Contact</td><td>${trustmark.recipientContactInformation.responder} - ${trustmark.recipientContactInformation.email}</td>
                </tr>
                <tr>
                    <td>Provider Organization</td><td>${trustmark.providerOrganization.name}</td>
                </tr>
                <tr>
                    <td>Provider Contact</td><td>${trustmark.providerContactInformation.responder} - ${trustmark.providerContactInformation.email}</td>
                </tr>
                <tr>
                    <td>Policy Publication URL</td><td><a href="${trustmark.policyPublicationURL}">${trustmark.policyPublicationURL}</a></td>
                </tr>
                <tr>
                    <td>Relying Party Agreement URL</td><td><a href="${trustmark.relyingPartyAgreementURL}">${trustmark.relyingPartyAgreementURL}</a></td>
                </tr>
                <tr>
                    <td>Status URL</td><td><a href="${trustmark.statusURL}">${trustmark.statusURL}</a></td>
                </tr>
                <tr>
                    <td>Definition Extension</td>
                    <td>
                        <g:if test="${org.apache.commons.lang.StringUtils.isNotEmpty(trustmark.definitionExtension)}">
                            ${trustmark.definitionExtension}
                        </g:if><g:else>
                            <em>No Data</em>
                        </g:else>
                    </td>
                </tr>
                <tr>
                    <td>Provider Extension</td>
                    <td>
                        <g:if test="${org.apache.commons.lang.StringUtils.isNotEmpty(trustmark.providerExtension)}">
                            ${trustmark.providerExtension}
                        </g:if><g:else>
                            <em>No Data</em>
                        </g:else>
                    </td>
                </tr>
                <tr>
                    <td>Has Exceptions</td><td>${trustmark.hasExceptions}</td>
                </tr>
                <tr>
                    <td>Exception Comments</td><td>${trustmark.assessorComments}</td>
                </tr>
                </tr>
                <tr>
                    <td>Parameter Values</td>
                    <td>
                        <table class="table table-hover">
                            <thead>
                                <tr>
                                    <th>Name</th>
                                    <th>Kind</th>
                                    <th>Value</th>
                                </tr>
                            </thead>
                            <tbody>
                            <g:each in="${trustmark.parameterValues.sort{it.parameter.identifier}}" var="parameterValue">
                                <tr>
                                    <td title="${parameterValue.parameter.identifier}">${parameterValue.parameter.name}</td>
                                    <g:set var="enumValues" value="${parameterValue.parameter.enumValues}"/>
                                    <g:set var="enumValues" value="${enumValues ? enumValues.join("\n - "): ''}"/>
                                    <td title="${enumValues ? ('Enum values were: \n - ' + enumValues) : ''}">
                                        ${parameterValue.parameter.kind}
                                    </td>
                                    <td>${parameterValue.userValue}</td>
                                </tr>
                            </g:each>
                            </tbody>
                        </table>
                    </td>
                </tr>
            </table>

            <h4 style="margin-top: 3em;">Trustmark Status</h4>
            <table class="infoTable">
                <tr>
                    <td>Status</td>
                    <td>
                        ${trustmark.status}
                        <g:if test="${trustmark.status == nstic.web.assessment.TrustmarkStatus.OK}">
                            <span style="color: darkgreen;" class="glyphicon glyphicon-ok-sign" title="Trustmark still valid"></span>
                        </g:if>
                        <g:if test="${trustmark.status == nstic.web.assessment.TrustmarkStatus.REVOKED}">
                            <span style="color: darkred;" class="glyphicon glyphicon-remove-sign" title="Trustmark has been revoked."></span>
                        </g:if>
                        <g:if test="${trustmark.status == nstic.web.assessment.TrustmarkStatus.EXPIRED}">
                            <span style="color: rgb(150, 150, 0);" class="glyphicon glyphicon-minus-sign" title="Trustmark has expired."></span>
                        </g:if>
                    </td>
                </tr>
                <g:if test="${trustmark.status != nstic.web.assessment.TrustmarkStatus.OK}">
                    <tr>
                        <td>Revoked Timestamp</td><td><g:formatDate date="${trustmark.revokedTimestamp}" format="yyyy-MM-dd" /></td>
                    </tr>
                    <tr>
                        <td>Revoked By</td><td>${trustmark.revokingUser?.contactInformation?.responder}</td>
                    </tr>
                    <tr>
                        <td>Revoked Reason</td><td>${trustmark.revokedReason}</td>
                    </tr>
                    <tr>
                        <td>Superseded By</td>
                        <td>
                            ${trustmark.supersededBy?.identifier}
                        </td>
                    </tr>
                </g:if>
            </table>
        </div>

        <div style="margin-top: 2em; margin-bottom: 3em;">
            <a href="${createLink(controller:'trustmark', action: 'list')}" class="btn btn-default">List Trustmarks</a>
            <a href="${createLink(controller:'trustmark', action: 'generateXml', id: trustmark.id)}" class="btn btn-primary">Generate XML</a>
            <a href="${createLink(controller:'trustmark', action: 'edit', id: trustmark.id)}" class="btn btn-default">Edit</a>
            <a href="javascript:revoke()" class="btn btn-danger">Revoke</a>
        </div>

        <script type="text/javascript">

            function revoke() {
                var url = '${createLink(controller:'trustmark', action: 'revoke', id: trustmark.id)}';

                var reason = prompt("What is the reason you are revoking this trustmark?");
                if( reason ){
                    window.location.href = url + "?reason="+encodeURI(reason);
                }else{
                    alert("A reason is required.");
                }

            }

        </script>

	</body>
</html>

<%--
  Created by IntelliJ IDEA.
  User: jeh
  Date: 9/22/20
  Time: 12:31 PM
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>Trustmark</title>
    <style type="text/css">
    .infoTable {

    }
    .infoTable td {
        padding: 0.5em;
    }
    </style>
</head>

<body>
<div>
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
<table class="infoTable">

    <tr>
    <td>Identifier</td><td><a href="${trustmark.identifierURL}">${trustmark.identifierURL}</a></td>
    </tr><tr>
    <td>Assessment</td>
    <td>
        <a href="${createLink(controller:'assessment', action:'view', id:trustmark.assessment.id)}">
            <tmpl:/templates/displayAssessmentStatusIcon assessment="${trustmark.assessment}" />
            ${trustmark.assessment.assessmentName}
        </a>
    </td>
    </tr><tr>
    <td>Trustmark Definition</td>
    <td>
        <a href="${trustmark.trustmarkDefinition.uri}" target="_blank">
            <span class="glyphicon glyphicon-tag"></span>
        </a>
        <a href="${createLink(controller:'trustmarkDefinition', action: 'view', id: trustmark.trustmarkDefinition.id)}">
            ${trustmark.trustmarkDefinition.name}, version ${trustmark.trustmarkDefinition.tdVersion}
        </a>
    </td>
    </tr><tr>
    <td>Issue Date Time</td>
    <td>
        <g:formatDate format="yyyy-MM-dd" date="${trustmark.issueDateTime}" />
    </td>
    </tr><tr>
    <td>Expiration Date Time</td>
    <td>
        <g:formatDate format="yyyy-MM-dd" date="${trustmark.expirationDateTime}" />
    </td>
    </tr><tr>
    <td>Recipient Organization</td><td>${trustmark.recipientOrganization.name}</td>
    </tr><tr>
    <td>Recipient Contact</td><td>${trustmark.recipientContactInformation.responder} - ${trustmark.recipientContactInformation.email}</td>
    </tr><tr>
    <td>Provider Organization</td><td>${trustmark.providerOrganization.name}</td>
    </tr><tr>
    <td>Provider Contact</td><td>${trustmark.providerContactInformation.responder} - ${trustmark.providerContactInformation.email}</td>
    </tr><tr>
    <td>Policy Publication URL</td><td><a href="${trustmark.policyPublicationURL}">${trustmark.policyPublicationURL}</a></td>
    </tr><tr>
    <td>Relying Party Agreement URL</td><td><a href="${trustmark.relyingPartyAgreementURL}">${trustmark.relyingPartyAgreementURL}</a></td>
    </tr><tr>
    <td>Status URL</td><td><a href="${trustmark.statusURL}">${trustmark.statusURL}</a></td>
    </tr><tr>
    <td>Definition Extension</td>
    <td>
        <g:if test="${org.apache.commons.lang.StringUtils.isNotEmpty(trustmark.definitionExtension)}">
            ${trustmark.definitionExtension}
        </g:if><g:else>
        <em>No Data</em>
    </g:else>
    </td>
    </tr><tr>
    <td>Provider Extension</td>
    <td>
        <g:if test="${org.apache.commons.lang.StringUtils.isNotEmpty(trustmark.providerExtension)}">
            ${trustmark.providerExtension}
        </g:if><g:else>
        <em>No Data</em>
    </g:else>
    </td>
    </tr><tr>
    <td>Has Exceptions</td><td>${trustmark.hasExceptions}</td>
    </tr><tr>
    <td>Exception Comments</td><td>${trustmark.assessorComments}</td>
    </tr>
    </tr><tr>
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
</div>
<br/>
<div>
    Also available as
    <a href="${request.getRequestURL()}?format=xml">
        XML
    </a>
    or
    <a href="${request.getRequestURL()}?format=jwt">
        JWT
    </a>
</div>
</body>
</html>
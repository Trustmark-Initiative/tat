<%@ page import="nstic.web.ContactInformation; nstic.web.Organization" %>
<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main"/>
		<title>Edit Trustmark</title>

        <style type="text/css">

        </style>
	</head>
	<body>

        <h1>Edit Trustmark for Assessment ${assessment.id} <small>(<assess:assessmentStatusIcon status="${assessment.status}" />  <assess:assessmentStatusName status="${assessment.status}" />)</small></h1>
        <div class="pageSubsection">

        </div>

        <g:hasErrors bean="${command}">
            <div style="margin-top: 2em;" class="alert alert-danger">
                <g:renderErrors bean="${command}" />
            </div>
        </g:hasErrors>


        <div class="pageContent">
            <g:form class="form-horizontal" name="createTrustmarkForm" method="POST" action="update">
                <g:hiddenField name="trustmarkId" id="trustmarkId" value="${command?.trustmarkId}" />
                <g:hiddenField name="assessmentId" id="assessmentId" value="${command?.assessmentId}" />
                <g:hiddenField id="distinguishedName" name="distinguishedName" value="${command?.distinguishedName ?: ''}" />

                <div class="form-group">
                    <label for="trustmarkDefName" class="col-sm-2 control-label">Trustmark Definition</label>
                    <div class="col-sm-10">
                        <p class="form-control-static" id="trustmarkDefName">
                            <a href="${createLink(controller:'trustmarkDefinition', action:'view', id: trustmark.trustmarkDefinition.id)}">
                                ${trustmark.trustmarkDefinition.name}, version: ${trustmark.trustmarkDefinition.tdVersion}
                            </a>
                        </p>
                    </div>
                </div>

                <div class="form-group">
                    <label for="identifier" class="col-sm-2 control-label">Identifier</label>
                    <div class="col-sm-10">
                        <g:textField name="identifier" id="identifier" class="form-control" value="${command?.identifier}" />
                    </div>
                </div>

                <div class="form-group">
                    <label for="identifierURL" class="col-sm-2 control-label">Identifier URL</label>
                    <div class="col-sm-10">
                        <g:textField name="identifierURL" id="identifierURL" class="form-control" value="${command?.identifierURL}" />
                    </div>
                </div>

                <div class="form-group">
                    <label for="statusURL" class="col-sm-2 control-label">Status URL</label>
                    <div class="col-sm-10">
                        <g:textField name="statusURL" id="statusURL" class="form-control" value="${command?.statusURL}" />
                    </div>
                </div>

                <div class="form-group">
                    <label for="expirationDateTime" class="col-sm-2 control-label">Expiration Date</label>
                    <div class="col-sm-10">
                        <g:textField name="expirationDateTime" id="expirationDateTime" class="form-control" placeholder="YYYY-MM-DD" value="${command?.expirationDateTime}" />
                    </div>
                </div>
                <div class="form-group">
                    <label for="policyPublicationURL" class="col-sm-2 control-label">Policy Publication URL</label>
                    <div class="col-sm-10">
                        <g:textField name="policyPublicationURL" id="policyPublicationURL" class="form-control" value="${command?.policyPublicationURL}" />
                    </div>
                </div>

                <div class="form-group">
                    <label for="relyingPartyAgreementURL" class="col-sm-2 control-label">Relying Party Agreement URL</label>
                    <div class="col-sm-10">
                        <g:textField name="relyingPartyAgreementURL" id="relyingPartyAgreementURL" class="form-control" value="${command?.relyingPartyAgreementURL}" />
                    </div>
                </div>

                <div class="form-group">
                    <label for="recipientOrganizationId" class="col-sm-2 control-label">Recipient Organization</label>
                    <div class="col-sm-10">
                        <g:select name="recipientOrganizationId"
                                  class="form-control"
                                  from="${Organization.list()}"
                                  value="${command?.recipientOrganizationId}"
                                  optionKey="id" optionValue="name" />
                    </div>
                </div>
                <div class="form-group">
                    <label for="recipientContactId" class="col-sm-2 control-label">Recipient Contact</label>
                    <div class="col-sm-10">
                        <g:select name="recipientContactId"
                                  class="form-control"
                                  from="${ContactInformation.list()}"
                                  value="${command?.recipientContactId}"
                                  optionKey="id" optionValue="${{it.responder + ' - ' + it.email}}" />
                    </div>
                </div>

                <div class="form-group">
                    <label for="providingOrganizationId" class="col-sm-2 control-label">Provider Organization</label>
                    <div class="col-sm-10">
                        <g:select name="providingOrganizationId"
                                  class="form-control"
                                  from="${Organization.list()}"
                                  value="${command?.providingOrganizationId}"
                                  optionKey="id" optionValue="name" />
                    </div>
                </div>
                <div class="form-group">
                    <label for="providingContactId" class="col-sm-2 control-label">Provider Contact</label>
                    <div class="col-sm-10">
                        <g:select name="providingContactId"
                                  class="form-control"
                                  from="${ContactInformation.list()}"
                                  value="${command?.providingContactId}"
                                  optionKey="id" optionValue="${{it.responder + ' - ' + it.email}}" />
                    </div>
                </div>

                <div class="form-group">
                    <label for="definitionExtension" class="col-sm-2 control-label">Definition Extension</label>
                    <div class="col-sm-10">
                        <g:textArea name="definitionExtension"
                                    id="definitionExtension"
                                    class="form-control"
                                    style="width: 100%;"
                                    value="${command?.definitionExtension}" />
                    </div>
                </div>

                <hr />

                <div class="form-group">
                    <label for="hasExceptions" class="col-sm-2 control-label">Has Exceptions</label>
                    <div class="col-sm-10">
                        <g:checkBox name="hasExceptions" id="hasExceptions"
                                    onchange="updateExpirationDate()"
                                    value="${command?.hasExceptions}"  />
                    </div>
                </div>

                <div class="form-group">
                    <label for="assessorComments" class="col-sm-2 control-label">Exception Comments</label>
                    <div class="col-sm-10">
                        <g:textArea name="assessorComments"
                                    id="assessorComments"
                                    class="form-control"
                                    style="width: 100%;"
                                    value="${command?.assessorComments}" />
                    </div>
                </div>

                <hr />

                <sec:ifLoggedIn>
                    <sec:ifAllGranted roles="ROLE_ADMIN">
                        <div class="form-group">
                            <label for="distinguishedName" class="col-sm-1 control-label">Signing Certificate</label>
                            <div class="col-sm-10">
                                <g:if test="${signingCertificates?.size() > 0}">
                                    <div class="btn-group" style="margin: 0;">
                                        <button type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown" aria-expanded="false">
                                            <span id="selectedDistinguishedNameTitle"><em>Select Signing Certificate...</em></span> <span class="caret"></span>
                                        </button>
                                        <ul class="dropdown-menu" role="menu">
                                            <g:each in="${signingCertificates}" var="cert">
                                                <li>
                                                    <a href="javascript:setSigningCertificate('${cert.distinguishedName}')">
                                                        <div style="font-weight: bold;">${cert.distinguishedName}</div>
                                                    </a>
                                                </li>
                                            </g:each>
                                        </ul>
                                    </div>
                                </g:if>
                                <g:else>
                                    <g:link controller="signingCertificates" action="add" params="[orgId: command?.providingOrganizationId]">
                                        <div style="font-weight: bold; color: #ff0000;">
                                            There are no signing certificates. At least one signing certificate must have been generated for the assessing organization before Trustmarks can be granted.
                                        </div>
                                    </g:link>
                                </g:else>

                            </div>

                            <script type="text/javascript">

                                function setSigningCertificate(dn) {
                                    console.log("Setting signing certificate to " + dn);
                                    $('#selectedDistinguishedNameTitle').html(dn);
                                    $('#distinguishedName').val(dn);
                                }
                            </script>
                        </div>
                    </sec:ifAllGranted>
                </sec:ifLoggedIn>

                <hr />

                <div class="form-group">
                    <div class="col-sm-offset-2 col-sm-10">
                        <button type="submit" class="btn btn-primary">Update</button>
                        <a href="${createLink(controller:'trustmark', action: 'view', id: command.trustmarkId)}" class="btn btn-default">Cancel</a>
                    </div>
                </div>
            </g:form>

        </div>


        <script type="text/javascript">
            function updateExpirationDate(){
                console.log("Called updateExpirationDate()")

                var expirationValue = "2014-01-01";
                if( $('#hasExceptions').is(':checked') ) {
                    expirationValue = $('#_expWithExceptions').val()
                }else{
                    expirationValue = $('#_expWithoutExceptions').val()
                }

                $('#expirationDateTime').val(expirationValue);

            }//end updateExpirationDate()
        </script>

	</body>
</html>

<%@ page import="nstic.web.User; nstic.web.Role" %>
<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main"/>
		<title>List Trustmarks</title>

        <style type="text/css">

        </style>
	</head>
	<body>
        %{--<ol class="breadcrumb">--}%
            %{--<li class="active">Home</li>--}%
        %{--</ol>--}%

        <h1>Trustmarks <small>(${trustmarkCountTotal} total)</small></h1>
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
            <table class="table table-striped table-bordered table-condensed">
                <thead>
                    <tr>
                        <th>Identifier</th>
                        <th>Status</th>
                        <th>Expiration Date</th>
                        <th>Granted By</th>
                        <th>Trustmark Definition</th>
                        <th>Recipient Organization</th>
                    </tr>
                </thead>
                <tbody>
                    <g:if test="${trustmarks && trustmarks.size() > 0}">
                        <g:each in="${trustmarks}" var="trustmark">
                            <tr>
                                <td>
                                    <g:link controller="trustmark" action="view" id="${trustmark.identifier}">
                                        ${trustmark.identifier}
                                    </g:link>
                                </td>
                                <td style="text-align: center;">
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
                                <td>
                                    <g:formatDate format="yyyy-MM-dd" date="${trustmark.expirationDateTime}" />
                                </td>
                                <td>
                                    ${trustmark.grantingUser.contactInformation.responder}
                                </td>
                                <td>
                                    ${trustmark.trustmarkDefinition.name}
                                </td>
                                <td>${trustmark.recipientOrganization.name}</td>
                            </tr>
                        </g:each>
                    </g:if>
                    <g:else>
                        <tr>
                            <td colspan="6"><em>There are no trustmarks.</em></td>
                        </tr>
                    </g:else>
                </tbody>
            </table>
            <div class="row" style="margin-bottom: 0.5em;">
                <div class="col-md-6">

                </div><div class="col-md-6" style="text-align: right">
                    <g:paginate total="${trustmarkCountTotal}" />
                </div>
            </div>
            <div class="row" style="margin-bottom: 2em;">
                <div class="col-md-6">
                    <a href="${createLink(controller: 'trustmark', action:'showBulkExportPage')}" class="btn btn-default">Bulk Export</a>
                </div>
            </div>

        </div>

	</body>
</html>

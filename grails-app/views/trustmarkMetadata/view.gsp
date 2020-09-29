<%@ page import="nstic.web.Role" %>
<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main"/>
		<title>Trustmark Metadata ${metadata.name}</title>

        <style type="text/css">

        </style>
	</head>
	<body>

        <h1>Trustmark Metadata: ${metadata.name}</h1>
        <div class="pageSubsection text-muted">
            ${metadata.description}
        </div>


        <g:if test="${flash.message}">
            <div class="alert alert-info" style="margin-bottom: 2em; margin-top: 2em;">${flash.message}</div>
        </g:if>

        <div class="pageContent">

            <div class="row" style="margin-bottom: 1em;">
                <div class="col-md-12">
                    <table class="table table-striped table-condensed">
                        <tr>
                            <td>
                                Provider
                            </td>
                            <td>
                                <g:link controller="organization" action="view" id="${metadata.provider.id}">
                                    ${metadata.provider.name}
                                </g:link>
                            </td>
                        </tr>

                        <tr>
                            <td>
                                Signing Certificate
                            </td>
                            <td>
                                <g:link controller="signingCertificates" action="view" id="${signingCertificate.id}">
                                    ${signingCertificate.distinguishedName}
                                </g:link>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                Policy URL
                            </td>
                            <td>
                                ${metadata.policyUrl}
                            </td>
                        </tr>
                        <tr>
                            <td>
                                Relying Party Agreement URL
                            </td>
                            <td>
                                ${metadata.relyingPartyAgreementUrl}
                            </td>
                        </tr>
                        <tr>
                            <td>
                                Trustmark Lifetime No Exception (months)
                            </td>
                            <td>
                                ${metadata.timePeriodNoExceptions}
                            </td>
                        </tr>
                        <tr>
                            <td>
                                Trustmark Lifetime with Exception (months)
                            </td>
                            <td>
                                ${metadata.timePeriodWithExceptions}
                            </td>
                        </tr>

                        <!-- TODO Other FIelds Here -->
                    </table>
                </div>
            </div>
            <div style="margin-top: 1em;">
                <a href="${createLink(controller: 'trustmarkMetadata', action: 'list')}" class="btn btn-default">&laquo; Back to List</a>
                <a href="${createLink(controller: 'trustmarkMetadata', action: 'edit', id: metadata.name)}" class="btn btn-primary">Edit</a>
                <a href="${createLink(controller: 'trustmarkMetadata', action: 'delete', id: metadata.name)}" class="btn btn-danger disabled">
                    <span class="glyphicon glyphicon-warning-sign"></span>
                    Delete
                </a>
            </div>

        </div>

        <script type="text/javascript">


        </script>

	</body>
</html>

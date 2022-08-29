<%@ page import="nstic.web.User; nstic.web.Role" %>
<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main"/>
		<title>List Documents</title>

        <style type="text/css">

        </style>
	</head>
	<body>

        <h1>Documents <small>(${documentsCountTotal} total)</small></h1>
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
                        <th>&nbsp;</th>
                        <g:sortableColumn property="filename" title="Document Name" />
                        <g:sortableColumn property="url" title="URL" />
                        <g:sortableColumn property="description" title="Description" />
                        <g:sortableColumn property="documentCategory" title="Category" />
                        <g:sortableColumn property="organization" title="Organization" />
                        <g:sortableColumn property="publicDocument" title="Public" />
                    </tr>
                </thead>
                <tbody>
                    <g:if test="${documents && documents.size() > 0}">
                        <g:each in="${documents}" var="doc">
                            <tr>
                                <td style="font-size: 120%;">
                                    <g:link controller="documents" action="view" id="${doc.id}" title="View ${doc.filename}">
                                        <span class="glyphicon glyphicon-eye-open"></span>
                                    </g:link>
                                    <g:link controller="binary" action="view" id="${doc.binaryObjectId}" title="Download ${doc.filename}">
                                        <span class="glyphicon glyphicon-download"></span>
                                    </g:link>
                                    <sec:ifAllGranted roles="ROLE_ADMIN">
                                        <g:link controller="documents" action="edit" id="${doc.id}" title="Edit document ${doc.filename}">
                                            <span class="glyphicon glyphicon-edit"></span>
                                        </g:link>
                                        <g:link controller="documents" action="deleteDocument" params="[documentId: doc.id, orgId: doc.organization.id]"
                                                id="${doc.id}" title="Delete document ${doc.filename}" onclick="return confirm('Really delete?');">
                                            <span class="glyphicon glyphicon-remove"></span>
                                        </g:link>
                                    </sec:ifAllGranted>
                                </td>
                                <td>
                                    <g:link controller="binary" action="view" id="${doc.binaryObjectId}">
                                        ${doc.filename}
                                    </g:link>
                                </td>
                                <td>
                                    <g:link controller="documents" action="pdf" id="${doc.binaryObjectId}">
                                        ${doc.url}
                                    </g:link>
                                </td>
                                <td>
                                    ${doc.description}
                                </td>
                                <td>
                                    ${doc.documentCategory}
                                </td>
                                <td>
                                    <g:link controller="organization" action="view" id="${doc.organization.id}">
                                        ${doc.organization.name}
                                    </g:link>
                                </td>
                                <td>
                                    <g:if test="${doc.publicDocument}">
                                        <span class="glyphicon glyphicon-ok"></span>
                                    </g:if>
                                    <g:else>
                                        <span class="glyphicon glyphicon-remove"></span>
                                    </g:else>
                                </td>
                            </tr>
                        </g:each>
                    </g:if>
                    <g:else>
                        <tr>
                            <td colspan="7"><em>There are no documents.</em></td>
                        </tr>
                    </g:else>
                </tbody>
            </table>
            <div class="row" style="margin-bottom: 2em;">
                <sec:ifAllGranted roles="ROLE_ADMIN">
                    <div class="col-md-6">
                        <a href="${createLink(controller:'documents', action:'add')}" class="btn btn-primary">Add Document</a>
                    </div>
                </sec:ifAllGranted>
                <div class="col-md-6" style="text-align: right">
                    <g:paginate total="${documentsCountTotal}" />
                </div>
            </div>

        </div>

    <script type="text/javascript">

    </script>
	</body>
</html>

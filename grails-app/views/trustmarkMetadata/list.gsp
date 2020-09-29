<%@ page import="nstic.web.User; nstic.web.Role" %>
<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main"/>
		<title>List Trustmark Metadata objects</title>

        <style type="text/css">

        </style>
	</head>
	<body>
        %{--<ol class="breadcrumb">--}%
            %{--<li class="active">Home</li>--}%
        %{--</ol>--}%

        <h1>Trustmark Metadata objects <small>(${countTotal} total)</small></h1>
        <div class="pageSubsection text-muted">
            On this page, you can see which Trustmark Metadata instances are configured in this system.  This metadata
            can be selected when granting Trustmarks to an Assessment, so that all of the resulting Trustmarks have the
            same metadata information.
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
                        <th>Name</th>
                        <th>Description</th>
                    </tr>
                </thead>
                <tbody>
                    <g:if test="${tmList && tmList.size() > 0}">
                        <g:each in="${tmList}" var="metadata">
                            <tr>
                                <td>
                                    <g:link controller="trustmarkMetadata" action="view" id="${metadata.id}">
                                        ${metadata.name}
                                    </g:link>
                                </td>
                                <td>
                                    ${metadata.description}
                                </td>
                            </tr>
                        </g:each>
                    </g:if>
                    <g:else>
                        <tr>
                            <td colspan="2"><em>There are no Trustmark Metadata objects.</em></td>
                        </tr>
                    </g:else>
                </tbody>
            </table>
            <div class="row" style="margin-bottom: 0.5em;">
                <div class="col-md-6">

                </div><div class="col-md-6" style="text-align: right">
                    <g:paginate total="${countTotal}" />
                </div>
            </div>
            <div class="row" style="margin-bottom: 2em;">
                <div class="col-md-6">
                    <a href="${createLink(controller: 'trustmarkMetadata', action:'create')}" class="btn btn-default">Create New</a>
                </div>
            </div>

        </div>

	</body>
</html>

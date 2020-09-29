<%@ page import="nstic.web.User; nstic.web.Role" %>
<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main"/>
		<title>List Organizations</title>

        <style type="text/css">

        </style>
	</head>
	<body>
        %{--<ol class="breadcrumb">--}%
            %{--<li class="active">Home</li>--}%
        %{--</ol>--}%

        <h1>Organizations <small>(${orgsCountTotal} total)</small></h1>
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
                        <g:sortableColumn property="identifier" title="Abbreviation" />
                        <g:sortableColumn property="uri" title="URI" />
                        <g:sortableColumn property="name" title="Name" />
                        <th>Primary Contact</th>
                    </tr>
                </thead>
                <tbody>
                    <g:if test="${organizations && organizations.size() > 0}">
                        <g:each in="${organizations}" var="org">
                            <tr>
                                <td style="font-size: 120%;">
                                    <g:link controller="organization" action="edit" id="${org.id}" title="Edit organization ${org.name}">
                                        <span class="glyphicon glyphicon-edit"></span>
                                    </g:link>
                                </td>
                                <td>
                                    <g:link controller="organization" action="view" id="${org.id}">
                                        ${org.identifier}
                                    </g:link>
                                </td>
                                <td>
                                    <g:link controller="organization" action="view" id="${org.id}">
                                        ${org.uri}
                                    </g:link>
                                </td>
                                <td>
                                    <g:link controller="organization" action="view" id="${org.id}">
                                        ${org.name}
                                    </g:link>
                                </td>
                                <td>
                                    <g:link controller="contactInformation" action="view" id="${org.primaryContact?.id}">
                                        ${org.primaryContact?.responder}
                                    </g:link>
                                </td>
                            </tr>
                        </g:each>
                    </g:if>
                    <g:else>
                        <tr>
                            <td colspan="4"><em>There are no organizations.</em></td>
                        </tr>
                    </g:else>
                </tbody>
            </table>
            <div class="row" style="margin-bottom: 2em;">
                <div class="col-md-6">
                    <a href="${createLink(controller:'organization', action:'create')}" class="btn btn-primary">New Organization</a>
                </div><div class="col-md-6" style="text-align: right">
                    <g:paginate total="${orgsCountTotal}" />
                </div>
            </div>

        </div>

	</body>
</html>

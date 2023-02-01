<%@ page import="nstic.web.Role" %>
<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main"/>
		<title>List Contributors</title>

        <style type="text/css">

        </style>
	</head>
	<body>
        %{--<ol class="breadcrumb">--}%
            %{--<li class="active">Home</li>--}%
        %{--</ol>--}%

        <h1>System Users <small>(${userCountTotal} total users)</small></h1>
        <div class="pageSubsection">

        </div>

        <div id="messageContainer">
            <g:if test="${flash.message}">
                <div style="margin-top: 2em;" class="alert alert-info">
                    ${flash.message}
                </div>
            </g:if>
        </div>

        <div class="pageContent">
            <table class="table table-striped table-bordered table-condensed">
                <thead>
                    <tr>
                        <th>&nbsp;</th>
                        <g:sortableColumn property="contactInformation.email" title="Email" />
                        <th>Role</th>
                        <g:sortableColumn property="contactInformation.responder" title="Name" />
                        <g:sortableColumn property="contactInformation.phoneNumber" title="Phone" />
                        <g:sortableColumn property="organization.name" title="Organization" />
                    </tr>
                </thead>
                <tbody>
                    <g:if test="${users && users.size() > 0}">
                        <g:each in="${users}" var="user">
                            <tr>
                                <td style="font-size: 120%;">
                                    <g:link controller="user" action="edit" id="${user.id}">
                                        <span class="glyphicon glyphicon-edit" title="Edit user ${user.contactInformation?.responder}"></span>
                                    </g:link>

                                    %{--<g:link controller="user" action="edit" id="${user.id}">--}%
                                        %{--<span class="glyphicon glyphicon-remove-sign" title="Edit user ${user.contactInformation?.responder}"></span>--}%
                                    %{--</g:link>--}%

                                </td>
%{--                                <td>${user.contactInformation?.email}</td>--}%
                                <td>${user.contactEmail}</td>
                                <td>
                                    <g:if test="${user.isAdmin()}">
                                        <span class="label label-danger">Admin</span>
                                    </g:if>
                                    <g:elseif test="${user.isUser()}">
                                        <span class="label label-info">Contributor</span>
                                    </g:elseif>
                                    <g:elseif test="${user.isReportOnly()}">
                                        <span class="label label-default">Report Viewer</span>
                                    </g:elseif>
                                    <g:else>
                                        <span class="label label-warning">No role</span>
                                    </g:else>
                                </td>
                                <td>${user.contactInformation?.responder}</td>
                                <td>${user.contactInformation?.phoneNumber}</td>
                                <td>${user.organization?.name}</td>
                            </tr>
                        </g:each>
                    </g:if>
                    <g:else>
                        <tr>
                            <td colspan="7"><em>There are no contributors</em></td>
                        </tr>
                    </g:else>
                </tbody>
            </table>
            <div class="row" style="margin-bottom: 2em;">
                <div class="col-md-6">
                    <a href="${createLink(controller:'user', action:'create')}" class="btn btn-primary">
                        <span class="glyphicon glyphicon-plus-sign" title="Create a new user"></span>
                        New User
                    </a>
                </div>
                <div class="col-md-6" style="text-align: right">
                    <g:paginate total="${userCountTotal}" />
                </div>
            </div>

        </div>

        <script type="text/javascript">

        </script>

	</body>
</html>

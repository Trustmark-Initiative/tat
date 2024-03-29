<%@ page import="nstic.web.User; nstic.web.Role" %>
<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main"/>
		<title>User Profile Page</title>

        <style type="text/css">

        </style>
	</head>
	<body>
        %{--<ol class="breadcrumb">--}%
            %{--<li class="active">Home</li>--}%
        %{--</ol>--}%

        <h1> <span class="glyphicon glyphicon-user"></span> User Profile </h1>
        <div class="pageSubsection">
            On this page, you can edit the information about yourself that shows up in generated trustmarks and assessments,
            as well as reset your email and password.
        </div>

        <div id="messageContainer">
            <g:if test="${flash.message}">
                <div style="margin-top: 2em;" class="alert alert-info">
                    ${flash.message}
                </div>
            </g:if>
            <g:hasErrors bean="${command}">
                <div class="alert alert-danger">
                    <g:renderErrors bean="${command}" as="list" />
                </div>
            </g:hasErrors>
        </div>

        <div class="pageContent" style="margin-top: 3em;">
            <form action="${createLink(controller:'profile', action:'update', id: command?.username)}" method="POST" class="form-horizontal">
                <fieldgroup>
                    <legend>Login Information</legend>
                    <div class="form-group">
                        <label for="username" class="col-sm-2 control-label">Username</label>
                        <div class="col-sm-10">
                            <g:field type="text" readonly="readonly" class="form-control" name="username" id="username" value="${command?.username ?: ''}" />
                        </div>
                    </div>
                </fieldgroup>

                <fieldgroup>
                    <legend>Organization</legend>
                    <div class="form-group">
                        <label for="organization" class="col-sm-2 control-label">Organization</label>
                        <div class="col-sm-10">
                            <g:if test="${user.organization == null}">
                                <g:select name="organization" id="organization" class="form-control" from="${nstic.web.Organization.findAll()}"
                                          noSelection="${['null':'Select an organization...']}" optionKey="id" optionValue="name" value="${command?.organization?.id}" />
                            </g:if>
                            <g:elseif test="${user.isUser()}">
                                <g:select name="organization" id="organization" class="form-control" from="${nstic.web.Organization.findAll()}" optionKey="id" optionValue="name" value="${command?.organization?.id}" />
                            </g:elseif>
                            <g:else>
                                <g:hiddenField name="organization" id="organization" value="${user.organization.id}" />
                                <p class="form-control-static">
                                    ${user.organization.name}
                                </p>
                            </g:else>
                        </div>
                    </div>
                </fieldgroup>

                <fieldgroup>
                    <legend>Contact Information</legend>

                    <div class="form-group">
                        <label for="email" class="col-sm-2 control-label">E-Mail</label>
                        <div class="col-sm-10">
                            <g:field type="text" readonly="readonly" class="form-control" name="email" id="email" value="${command?.email ?: ''}" />
                        </div>
                    </div>

                    <div class="form-group">
                        <label for="responder" class="col-sm-2 control-label">Name</label>
                        <div class="col-sm-10">
                            <g:field type="text" readonly="readonly" class="form-control" name="responder" id="responder" value="${command?.responder ?: ''}" />
                        </div>
                    </div>

                    <div class="form-group">
                        <label for="phoneNumber" class="col-sm-2 control-label">Phone Number</label>
                        <div class="col-sm-10">
                            <g:field type="text" readonly="readonly" class="form-control" name="phoneNumber" id="phoneNumber" value="${command?.phoneNumber ?: ''}" />
                        </div>
                    </div>

                    <div class="form-group">
                        <label for="mailingAddress" class="col-sm-2 control-label">Mailing Address</label>
                        <div class="col-sm-10">
                            <g:field type="text" readonly="readonly" class="form-control" name="mailingAddress" id="mailingAddress" value="${command?.mailingAddress ?: ''}" />
                        </div>
                    </div>

                    <div class="form-group">
                        <label for="notes" class="col-sm-2 control-label">Notes</label>
                        <div class="col-sm-10">
                            <g:textArea class="form-control" name="notes" id="notes">${command?.notes ?: ''}</g:textArea>
                        </div>
                    </div>

                </fieldgroup>

                <div class="form-group">
                    <div class="col-sm-offset-2 col-sm-10">
                        <g:submitButton name="do it" value="Update Profile" class="btn btn-primary" />
                    </div>
                </div>
            </form>
        </div>
	</body>
</html>

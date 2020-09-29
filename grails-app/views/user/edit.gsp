<%@ page import="nstic.web.Organization" %>
<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main"/>
		<title>Edit User</title>

        <style type="text/css">

        </style>
	</head>
	<body>
        %{--<ol class="breadcrumb">--}%
            %{--<li class="active">Home</li>--}%
        %{--</ol>--}%

        <h1>Edit User: ${userCommand.name}</h1>
        <div class="pageSubsection">

        </div>

        <g:hasErrors bean="${userCommand}">
            <div style="margin-top: 2em;" class="alert alert-danger">
                <g:renderErrors bean="${userCommand}" />
            </div>
        </g:hasErrors>


        <div class="pageContent">
            <g:form class="form-horizontal" name="createUserForm" method="POST" action="update">
                <g:hiddenField name="existingUserId" id="existingUserId" value="${userCommand?.existingUserId}" />
                <div class="form-group">
                    <label for="email" class="col-sm-2 control-label">Email address <br/><small>(&amp; Username)</small></label>
                    <div class="col-sm-10">
                        <g:textField name="email" id="email" class="form-control" placeholder="user@example.com" value="${userCommand?.email}" />
                    </div>
                </div>
                <div class="form-group">
                    <label for="password" class="col-sm-2 control-label">Password</label>
                    <div class="col-sm-5">
                        <g:passwordField name="password" id="password" class="form-control col-md-6" value="${userCommand?.password}" placeholder="Password" />
                    </div>
                    <div class="col-sm-5">
                        <g:passwordField name="passwordAgain" id="passwordAgain" class="form-control col-md-6" value="${userCommand?.passwordAgain}" placeholder="Password (Again)" />
                    </div>
                </div>
                <div class="form-group">
                    <label for="name" class="col-sm-2 control-label">Name</label>
                    <div class="col-sm-10">
                        <g:textField name="name" id="name" class="form-control" placeholder="George Washington" value="${userCommand?.name}" />
                    </div>
                </div>
                <div class="form-group">
                    <label for="phone" class="col-sm-2 control-label">Phone Number</label>
                    <div class="col-sm-10">
                        <g:textField name="phone" id="phone" class="form-control" placeholder="555-555-5555" value="${userCommand?.phone}" />
                    </div>
                </div>
                <div class="form-group" >
                    <label for="mailingAddress" class="col-sm-2 control-label">Mailing address</label>
                    <div class="col-sm-10">
                        <g:textField name="mailingAddress" id="mailingAddress" class="form-control" placeholder="250 14th Stree NW, Atlanta, GA" value="${userCommand?.mailingAddress}" />
                    </div>
                </div>

                <div class="form-group">
                    <label for="organizationId" class="col-sm-2 control-label">Organization</label>
                    <div class="col-sm-10">
                        <g:select name="organizationId"
                                  class="form-control"
                                  from="${Organization.list()}"
                                  value="${userCommand?.organizationId}"
                                  optionKey="id" optionValue="name" />
                    </div>
                </div>

                <div class="form-group">
                    <label class="col-sm-2 control-label">Roles</label>
                    <div class="col-sm-10">
                        <g:hiddenField name="userRole" id="userRole" value="${userCommand?.userRole ?: 'false'}" />
                        <g:hiddenField name="adminRole" id="adminRole" value="${userCommand?.adminRole ?: 'false'}" />
                        <g:hiddenField name="reportOnlyRole" id="reportOnlyRole" value="${userCommand?.reportOnlyRole ?: 'false'}" />

                        <a id="reportOnlyRoleButton" href="javascript:changeRoleStatus('reportOnly')" class="btn btn-default">
                            <span id="reportOnlyRoleStatusContainer"></span>
                            Report Only
                        </a>

                        <a id="userRoleButton" href="javascript:changeRoleStatus('user')" class="btn btn-default">
                            <span id="userRoleStatusContainer"></span>
                            User
                        </a>

                        <a id="adminRoleButton" href="javascript:changeRoleStatus('admin')" class="btn btn-default">
                            <span id="adminRoleStatusContainer"></span>
                            Admin
                        </a>
                        <span id="helpBlock" class="help-block">
                            <b>All Roles</b>: Are permitted to log on.  Remove all roles to prevent logins (ie, disable user).
                            <br/>
                            <b>Report Only Users</b>: Can only view reports based on their organization.
                            <br/>
                            <b>Users</b>: Can do most major tool functions, such as create &amp; perform assessments,
                        manage binary artifacts, trustmark definitions and trustmarks.
                            <br/>
                            <b>Admins</b>: Can do all functions of users, and manage any part of the system.  Including
                        creating user accounts, organizations, etc. <em>Different than organization administrators.</em>

                        </span>

                    </div>
                </div>
                <div class="form-group">
                    <label for="enabled" class="col-sm-2 control-label">Enabled</label>
                    <div class="col-sm-10">
                        <g:checkBox name="enabled" id="enabled" value="${userCommand?.enabled}" />
                    </div>
                </div>
                <div class="form-group">
                    <div class="col-sm-offset-2 col-sm-10">
                        <button type="submit" class="btn btn-primary">Update</button>
                        <a href="${createLink(controller:'user', action: 'list')}" class="btn btn-default">Cancel</a>
                    </div>
                </div>
            </g:form>

        </div>

    <script type="text/javascript">

        $(document).ready(function(){
            updateRoleButtonsView();
        })

        function changeRoleStatus(role){
            var hasRole = $('#'+role+'Role').val();
            if( hasRole === 'false' ){
                $('#'+role+'Role').val('true');
            }else if( hasRole === 'true' ){
                $('#'+role+'Role').val('false');
            }else{
                alert("Invalid value for role '"+role+"': "+hasRole);
                $('#'+role+'Role').val('false');
            }






            updateRoleButtonsView();
        }//end changeRoleStatus()

        function updateRoleButtonsView() {
            updateRoleButtonView('user');
            updateRoleButtonView('admin');
            updateRoleButtonView('reportOnly');
        }//end updateRoleButtonView()

        function updateRoleButtonView(role) {
            var hasRole = $('#'+role+'Role').val();
            $('#'+role+'RoleButton').removeClass('btn-default');
            $('#'+role+'RoleButton').removeClass('btn-success');
            $('#'+role+'RoleButton').removeClass('btn-danger');
            if( hasRole === 'false' ){
                $('#'+role+'RoleButton').addClass('btn-danger');
                $('#'+role+'RoleStatusContainer').html(falseStatusHtml())
            }else if( hasRole === 'true' ){
                $('#'+role+'RoleButton').addClass('btn-success');
                $('#'+role+'RoleStatusContainer').html(trueStatusHtml())
            }else{
                alert("Invalid value for role '"+role+"': "+hasRole);
            }
        }//end updateRoleButtonView()

        function trueStatusHtml(){
            return '<span class="glyphicon glyphicon-ok"></span>'
        }
        function falseStatusHtml(){
            return '<span class="glyphicon glyphicon-remove"></span>'
        }

    </script>

	</body>
</html>

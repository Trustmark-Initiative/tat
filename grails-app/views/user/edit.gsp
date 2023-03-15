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
                    <label for="username" class="col-sm-2 control-label">Username</label>
                    <div class="col-sm-10">
                        <g:field type="text" readonly="readonly" class="form-control" name="username" id="username" value="${userCommand?.username ?: ''}" />
                    </div>
                </div>
                <div class="form-group">
                    <label for="name" class="col-sm-2 control-label">Name</label>
                    <div class="col-sm-10">
                        <g:field type="text" readonly="readonly" class="form-control" name="name" id="name" value="${userCommand?.name ?: ''}" />
                    </div>
                </div>
                <div class="form-group">
                    <label for="email" class="col-sm-2 control-label">Email address</label>
                    <div class="col-sm-10">
                        <g:field type="text" readonly="readonly" class="form-control" name="email" id="email" value="${userCommand?.email ?: ''}" />
                    </div>
                </div>
                <div class="form-group">
                    <label for="organizationId" class="col-sm-2 control-label">Organization</label>
                    <div class="col-sm-10">
                        <g:if test="${user.organization == null}">
                            <g:select name="organizationId" class="form-control" from="${Organization.list()}" noSelection="${['null':'Select an organization...']}"
                                      value="${userCommand?.organizationId}" optionKey="id" optionValue="name" />
                        </g:if>
                        <g:else>
                            <g:select name="organizationId" class="form-control" from="${Organization.list()}"
                                      value="${userCommand?.organizationId}" optionKey="id" optionValue="name" />
                        </g:else>
                    </div>
                </div>

                <div class="form-group">
                    <label class="col-sm-2 control-label">Roles</label>
                    <div class="col-sm-10">
                        <g:hiddenField class="roleField" name="userRole" id="userRole" value="${userCommand?.userRole ?: 'false'}" />
                        <g:hiddenField class="roleField" name="adminRole" id="adminRole" value="${userCommand?.adminRole ?: 'false'}" />
                        <g:hiddenField class="roleField" name="reportOnlyRole" id="reportOnlyRole" value="${userCommand?.reportOnlyRole ?: 'false'}" />

                        <a id="reportOnlyRoleButton" class="btn btn-default">
                            <span id="reportOnlyRoleStatusContainer"></span>
                            Report Viewer
                        </a>

                        <a id="userRoleButton" class="btn btn-default">
                            <span id="userRoleStatusContainer"></span>
                            Contributor
                        </a>

                        <a id="adminRoleButton" class="btn btn-default">
                            <span id="adminRoleStatusContainer"></span>
                            Admin
                        </a>
                        <span id="helpBlock" class="help-block">
                            <b>Report Viewers</b>: Can only view reports for their organization.
                            <br/>
                            <b>Contributors</b>: Can perform assessments for their organization, view reports for their organization,
                        and upload/view recipient agreements for their organization.
                            <br/>
                            <b>Admins</b>: Can do all functions allowed by the tool, including assessments of any organization, view all reports,
                        issue trustmarks, configure TPATs, add/edit users, and upload all document types.</em>

                        </span>

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
                // reset other roles
                $(".roleField").each(function() {
                    $(this).val('false');
                });

                $('#'+role+'Role').val('true');
            }else if( hasRole === 'true' ){
                // ignore
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
            $('#'+role+'RoleButton').removeClass('alert alert-secondary');
            $('#'+role+'RoleButton').removeClass('alert alert-success');
            $('#'+role+'RoleButton').removeClass('alert alert-danger');
            if( hasRole === 'false' ){
                $('#'+role+'RoleButton').addClass('alert alert-danger');
                $('#'+role+'RoleStatusContainer').html(falseStatusHtml())
            }else if( hasRole === 'true' ){
                $('#'+role+'RoleButton').addClass('alert alert-success');
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

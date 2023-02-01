<%@ page import="nstic.web.Organization" %>
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

        <h1>Create New User</h1>
        <div class="pageSubsection">
            This page allows you to create a new contributor.  Contributors are capable of performing actions in the system, from
            viewing reports to assessing.  Contacts are used to identify people at organizations who do not use the tool.
        </div>

        <g:hasErrors bean="${userCommand}">
            <div style="margin-top: 2em;" class="alert alert-danger">
                <g:renderErrors bean="${userCommand}" />
            </div>
        </g:hasErrors>


        <div class="pageContent">
            <g:form class="form-horizontal" name="createUserForm" method="POST" action="save">
                <div class="form-group">
                    <label for="email" class="col-sm-2 control-label">Email address <br/><small>(&amp; Username)</small></label>
                    <div class="col-sm-10">
                        <g:textField name="email" id="email" class="form-control" placeholder="user@example.com" value="${userCommand?.email}" />
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
                        <g:hiddenField class="roleField" name="userRole" id="userRole" value="${userCommand?.userRole ?: 'false'}" />
                        <g:hiddenField class="roleField" name="adminRole" id="adminRole" value="${userCommand?.adminRole ?: 'false'}" />
                        <g:hiddenField class="roleField" name="reportOnlyRole" id="reportOnlyRole" value="${userCommand?.reportOnlyRole ?: 'false'}" />

                        <a id="reportOnlyRoleButton" href="javascript:changeRoleStatus('reportOnly')" class="btn btn-default">
                            <span id="reportOnlyRoleStatusContainer"></span>
                            Report Viewer
                        </a>

                        <a id="userRoleButton" href="javascript:changeRoleStatus('user')" class="btn btn-default">
                            <span id="userRoleStatusContainer"></span>
                            Contributor
                        </a>

                        <a id="adminRoleButton" href="javascript:changeRoleStatus('admin')" class="btn btn-default">
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
                        <button type="submit" class="btn btn-primary">Save</button>
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

<%@ page import="nstic.web.Organization; nstic.web.User; nstic.web.Role" %>
<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main"/>
		<title>Organization Report Form</title>

        <style type="text/css">

        </style>
	</head>
	<body>
        %{--<ol class="breadcrumb">--}%
            %{--<li class="active">Home</li>--}%
        %{--</ol>--}%

        <h1><span class="glyphicon glyphicon-home"></span> Organization Report</h1>
        <div class="pageSubsection">
            The organizational report will include a summary and detailed information about all ongoing assessments.
        </div>

        <div id="messageContainer">
            <g:if test="${flash.message}">
                <div style="margin-top: 2em;" class="alert alert-info">
                    ${flash.message}
                </div>
            </g:if>
            <g:hasErrors bean="${command}">
                <div style="margin-top: 2em;" class="alert alert-danger">
                    <g:renderErrors bean="${command}" as="list" />
                </div>
            </g:hasErrors>
        </div>

        <div class="pageContent" style="margin-top: 3em;">

            <g:form action="organizationReport" method="POST" class="form-horizontal">
                <div class="form-group">
                    <label for="organization" class="col-sm-2 control-label">Organization</label>
                    <div class="col-sm-10">
                        <%
                            List<Organization> validOrgs = []
                            if( user.isUser() ){
                                validOrgs = Organization.findAll(); // Sort?
                            }else{
                                Organization.findAll().each { Organization org ->
                                    if( org.primaryContact.equals(user.contactInformation) ||
                                            org.contacts.contains(user.contactInformation) ||
                                            user.organization.equals(org) ){
                                        validOrgs.add( org );
                                    }
                                }
                            }
                        %>
                        <g:select name="organization" id="organization" class="form-control" from="${validOrgs}" optionKey="id" optionValue="name" value="${command?.organization?.id}" />
                        <p class="help-block"This is the organization to report on.</p>
                    </div>
                </div>

                <div class="collapse" id="optionsCollapseContainer">
                  <div class="well">

                    <div class="form-group">
                        <label for="startDate" class="col-sm-2 control-label">Start Date</label>
                            <div class="col-sm-10">
                                <g:datePicker class="form-control" id="startDate" precision="day" name="startDate" placeholder="mm/dd/yyyy" value="${command?.startDate ?: ""}" />
                                <p class="help-block">The report will include all assessments with log entries after this date.</p>
                            </div>
                        </div>
                        <div class="form-group">
                            <label for="endDate" class="col-sm-2 control-label">End Date</label>
                            <div class="col-sm-10">
                                <g:datePicker class="form-control" id="endDate" precision="day" name="endDate" placeholder="mm/dd/yyyy" value="${command?.endDate ?: ""}"  />
                                <p class="help-block">The report will include all assessments with log entries prior to this date.</p>
                            </div>
                        </div>

                        <div class="form-group">
                            <div class="col-sm-10 col-sm-offset-2">
                                <label>
                                    <g:checkBox name="hideCompletedAssessments" value="${command?.hideCompletedAssessments}" />
                                    Hide Completed Assessments
                                </label>
                                <p class="help-block">If checked, then completed assessments will not be in the report to shorten it.</p>
                            </div>
                        </div>

                        <div class="form-group">
                            <div class="col-sm-10 col-sm-offset-2">
                                <label>
                                    <g:checkBox name="hideCompletedSteps" value="${command?.hideCompletedSteps}" />
                                    Hide Completed Steps
                                </label>
                                <p class="help-block">If checked, then satisfied (or N/A) assessment steps will not be in the report to shorten it.</p>
                            </div>
                        </div>

                    </div>
                </div>

                <div class="form-group">
                    <div class="col-sm-offset-2 col-sm-10">
                        <g:submitButton name="do it" value="Run Report" class="btn btn-default" />


                        <a class="btn btn-default" data-toggle="collapse" href="#optionsCollapseContainer" aria-expanded="false" aria-controls="optionsCollapseContainer">
                            Options...
                        </a>
                    </div>
                </div>

            </g:form>

        </div>


	</body>
</html>

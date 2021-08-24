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

            <g:form class="form-horizontal" onSubmit="runReport(); return false;">
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
                        <p class="help-block">This is the organization to report on.</p>
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
                <br>
                <div id="organizationReportStatusMessage"></div>

                <script type="text/javascript">
                    var STOP_LOOP = false;
                    var CANCEL_LOOP = false;

                    let runReport = function() {
                        console.log("runReport");

                        var organizationId = $("select#organization option").filter(":selected").val();

                        initOrganizationReportState();

                        STOP_LOOP = false;
                        organizationReportStatusLoop(organizationId);

                        startOrganizationReport();
                    }

                    let initOrganizationReportState = function () {

                        $('#organizationReportStatusMessage').html('');

                        var organizationId = $("select#organization option").filter(":selected").val();

                        var url = '${createLink(controller: 'reports',  action: 'initOrganizationReportState')}';
                        $.ajax({
                            url: url,
                            dataType: 'json',
                            async: false,
                            data: {
                                id: organizationId,
                                format: 'json'
                            },
                            beforeSend: function () {
                            },
                            success: function (data, statusText, jqXHR) {
                                console.log("initOrganizationReportState: " + JSON.stringify(data));
                            },
                            error: function (jqXHR, statusText, errorThrown) {
                                console.log("Error: " + errorThrown);

                                $('#organizationReportStatusMessage').html(errorThrown);
                            }
                        });
                    }

                    let startOrganizationReport = function() {
                        console.log("startOrganizationReport...");

                        var organizationId = $("select#organization option").filter(":selected").val();

                        var startyear = $("#startDate_year").val();
                        var startmonth = $("#startDate_month").val();
                        var startday = $("#startDate_day").val();
                        var startDate = new Date(startyear, startmonth, startday);

                        var endyear = $("#endDate_year").val();
                        var endmonth = $("#endDate_month").val();
                        var endday = $("#endDate_day").val();
                        var endDate = new Date(endyear, endmonth, endday);

                        var hideCompletedAssessments= $("#hideCompletedAssessments").is(":checked");
                        var hideCompletedSteps = $("#hideCompletedSteps").is(":checked");

                        var url = '${createLink(controller: 'reports',  action: 'organizationReport')}';
                        $.ajax({
                            url: url,
                            dataType: 'html',
                            method: 'POST',
                            data: {
                                id: organizationId,
                                startDate: startDate.getTime(),
                                endDate: endDate.getTime(),
                                hideCompletedAssessments: hideCompletedAssessments,
                                hideCompletedSteps: hideCompletedSteps,
                                // format: 'json'
                                format: 'html'
                            },
                            beforeSend: function () {
                            },
                            success: function (data, statusText, jqXHR) {
                                renderOrganizationReportStatus(data);
                            },
                            error: function (jqXHR, statusText, errorThrown) {
                                console.log("Error: " + errorThrown);
                                console.log("Status: " + statusText);
                            },
                        });
                    }

                    let organizationReportStatusLoop = function() {
                        if (!CANCEL_LOOP) {
                            if (STOP_LOOP) {
                                console.log("STOP_LOOP");

                                // calls a reports controller action to render the reports results
                                renderOrganizationReport();

                                return;
                            }

                            updateOrganizationReportStatus();

                            setTimeout(organizationReportStatusLoop, 250);
                        }
                    }

                    let renderOrganizationReport = function() {

                        var targetUrl = "${createLink(controller:'reports', action: 'renderOrganizationReport')}";
                        window.location.href = targetUrl;
                    }

                    let updateOrganizationReportStatus = function () {

                        $.ajax({
                            url: '${createLink(controller:'reports', action: 'organizationReportStatusUpdate')}',
                            method: 'GET',
                            dataType: 'json',
                            cache: false,
                            data: {
                                format: 'json',
                                timestamp: new Date().getTime()
                            },
                            success: function(data, textStatus, jqXHR){
                                if( data && (data.status == "SUCCESS" || data.status == "ERROR")) {
                                    STOP_LOOP = true;
                                }

                                renderOrganizationReportStatus(data);
                            },
                            error: function(jqXHR, textStatus, errorThrown){
                                console.log("updateOrganizationReportStatus error: " + textStatus);
                                if (errorThrown) {
                                    console.log("       errorThrown: " + errorThrown.toString());
                                }
                            }
                        })
                    }

                    function renderOrganizationReportStatus(data){

                        var html = '';
                        if( data && data.status ){

                            html += '<div class="well"><h5>';
                            if( data.status == "SUCCESS" ) {
                                html += '<span class="glyphicon glyphicon-ok-circle"></span> SUCCESS';
                            }else{
                                html += '<span class="glyphicon glyphicon-time"></span> '+data.status;
                            }
                            html += '</h5>';
                            html += buildProgressBarHtml(data.percent);
                            html += '<div>' + data.message + '</div>';

                            html += '</div>';

                        }else{
                            // html = '<div class="alert alert-danger"><b>Error</b> <br/> The server returned an invalid response.  Please refresh the page and try again.</div>';
                        }
                        $('#organizationReportStatusMessage').html(html);
                    }

                    /**
                     * Given a percent value (ie between 0-100) this method will create a progress bar HTML snippet.
                     */
                    function buildProgressBarHtml(percent){

                        percent = Math.floor(percent);
                        return '<div class="progress">' +
                            '<div class="progress-bar" role="progressbar" aria-valuenow="'+percent+'" aria-valuemin="0" aria-valuemax="100" style="width: '+percent+'%;">'+
                            '<span class="sr-only">'+percent+'% Complete</span>'+
                            '</div></div>';
                    }
                </script>

            </g:form>

        </div>


	</body>
</html>

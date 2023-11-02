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
                        <g:if test="${user.isAdmin() || user.isReportOnly()}">
                            <%
                                List<Organization> validOrgs = []
                                if( user.isAdmin() ){
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
                        </g:if>
                        <g:else>
                            <g:hiddenField name="organization" id="organization" value="${user.organization.id}" />
                            <input class="form-control" type="text" id="organizationInput" name="organizationInput" value=${user.organization.name} readonly>
                        </g:else>
                        <p class="help-block">This is the organization to report on.</p>
                    </div>
                </div>

                <div class="form-group">
                    <div class="col-sm-offset-2 col-sm-10">
                        <div class="radio-inline">
                            <label for="organizational-report-option">
                                <input id="organizational-report-option" class="tdCheckbox" checked = "checked" type="radio" name="report-types" onclick="toggleReportTypeOptions(this)"/>
                                Organizational Report Overview
                            </label>
                        </div>
                        <div class="radio-inline">
                            <label for="assessment-report-option">
                                <input id="assessment-report-option" class="tdCheckbox" type="radio" name="report-types" onclick="toggleReportTypeOptions(this)"/>
                                Assessment Report
                            </label>
                        </div>
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

                        <a id="organizational-report-options" class="btn btn-default" data-toggle="collapse" href="#optionsCollapseContainer"
                           aria-expanded="false" aria-controls="optionsCollapseContainer">
                            Options...
                        </a>
                    </div>

                </div>
                <br>
                <div id="organizationReportStatusMessage"></div>

                <script type="text/javascript">
                    var STOP_LOOP = false;
                    var CANCEL_LOOP = false;

                    $(document).ready(function(){
                        updateReportTypeOptions()
                    })

                    function toggleReportTypeOptions(elem) {

                        // enable/disable options button
                        if (elem.id === "organizational-report-option") {
                            $('#organizational-report-options').removeAttr('disabled');
                            localStorage.setItem('report-types-selected-option', 'organizational-report-option')
                        } else {
                            $('#organizational-report-options').attr('disabled', 'disabled');
                            localStorage.setItem('report-types-selected-option', 'assessment-report-option')
                        }
                    }

                    function updateReportTypeOptions() {

                        const savedReportTypeOption = localStorage.getItem('report-types-selected-option');
                        if (savedReportTypeOption === 'organizational-report-option') {
                            $('#organizational-report-option').prop('checked', true);
                            $('#organizational-report-options').removeAttr('disabled');
                        } else {
                            $('#assessment-report-option').prop('checked', true);
                            $('#organizational-report-options').attr('disabled', 'disabled');
                        }
                    }

                    let selectedOrganizationId = function() {
                        var organizationId = $("select#organization option").filter(":selected").val();
                        if(organizationId === undefined) {
                            organizationId = $("#organization").val();
                        }

                        return organizationId;
                    }

                    let runReport = function() {
                        console.log("runReport");

                        var organizationId = selectedOrganizationId();

                        // based on radio button selection run report or excel export
                        var organizationalReport = true;

                        $('.tdCheckbox').each( function(index){
                            var tdCheckbox = $(this);
                            var currentId = tdCheckbox.attr('id');
                            if( tdCheckbox.is(":checked") ){
                                if (currentId === "organizational-report-option") {
                                    organizationalReport = true;
                                } else {
                                    organizationalReport = false;
                                }
                            }
                        });

                        if (organizationalReport) {
                            initOrganizationReportState();

                            STOP_LOOP = false;
                            organizationReportStatusLoop(organizationId);

                            startOrganizationReport();
                        } else {
                            startAssessmentReport();
                        }
                    }

                    let startAssessmentReport = function() {

                        var organizationId = selectedOrganizationId();

                        var redirectUrl = '${createLink(controller: 'assessmentToExcel')}';

                        window.location.href = redirectUrl + "?id=" + organizationId;
                    }

                    let initOrganizationReportState = function () {

                        $('#organizationReportStatusMessage').html('');

                        var organizationId = selectedOrganizationId();

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

                        var organizationId = selectedOrganizationId();

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

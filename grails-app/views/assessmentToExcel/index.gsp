<%@ page import="nstic.web.assessment.Assessment; nstic.web.assessment.AssessmentStepResult; nstic.web.User; nstic.web.Role" %>
<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main"/>
		<title>Assessment To Excel Generation</title>

        <style type="text/css">

        </style>
	</head>
	<body>

        <h1>Assessment to Excel Generation </h1>
        <div class="pageSubsection">
            This page allows you to generate a "Spreadsheet" for editing an
            assessment as one spreadsheet that will output all TDs.
            The spreadsheet will represent each TD as a "line" along with assessment
            steps with no substeps.  The idea is that each line will output into it's own
            spreadsheet.
        </div>

        <div id="mainPageContent" class="pageContent" style="margin-top: 3em;">

            <asset:image src="spinner.gif" /> Loading Assessments...

        </div>

        <div id="assessmentToExcelStatusMessage" class="container"></div>

    <%
        def organizationId = organization.id
    %>
    <script type="text/javascript">
        var STOP_LOOP = false;
        var CANCEL_LOOP = false;

        $(document).ready(function(){
            console.log("Initializing Assessment To Excel page..."+'${organization.id}');

            var orgId = parseInt(${organization.id});

            $.ajax({
                method: 'GET',
                url: '${createLink(controller:'assessmentToExcel', action: 'assessmentListing')}',
                data: {
                    format: 'json',
                    organizationId: orgId,
                    now: new Date().getMilliseconds() + ""
                },
                dataType: 'json',
                success: function(data){
                    var tdListingHtml = buildTdListing(data);
                    $('#mainPageContent').html(tdListingHtml);
                },
                error: function(jqXHR, textStatus, errorThrown){
                    console.log("Error receiving data["+textStatus+"]: "+errorThrown);
                    $('#mainPageContent').html('<div class="alert alert-danger" style="width: 600px;">An unexpected error has occurred, please refresh the page and try again. <br/><br /><b>Text Status:</b>'+textStatus+'<br/><b>Error Thrown:</b>'+errorThrown+'</div>')
                }//end error()
            });

        });

        function buildTdListing(data) {
            var html = '';

            html += "<div>\n";

            if( data.status && data.status == "SUCCESS" ) {
                html += "   <h3>Assessment to Export (" + data.assessmentCount + ")</h3>\n";
                var numericColWidth = "125";
                if( data.assessmentList && data.assessmentList.length && data.assessmentList.length > 0 ) {
                    html += '    <table class="table table-striped table-bordered table-condensed">\n';
                    html += '        <thead><tr>\n';
                    html += '            <th style="text-align: center; width: 20px;"></th>\n';
                    html += '            <th style="text-align: left; width: auto;">Name</th>\n';
                    html += '            <th style="text-align: left; width: auto;">Organization</th>\n';
                    html += '        </tr></thead>\n';
                    html += '        <tbody>\n';
                    for (var tdIndex = 0; tdIndex < data.assessmentList.length; tdIndex++) {
                        var td = data.assessmentList[tdIndex];
                        html += '<tr><!-- Start Assessment '+td.id+' -->\n';

                        var data_checked = (tdIndex === 0) ? "checked = 'checked'" : "";

                        html += '    <td style="text-align: center; width: 20px;"><input id="tdCheckbox'+td.id+'" class="tdCheckbox" ' + data_checked +
                            ' type="radio" onclick="updateCheckboxFeedback(this)" /></td>\n';
                        html += '    <td style="text-align: left; width: auto;">'+td.assessmentName+'</td>\n';
                        html += '    <td style="text-align: left; width: auto;">'+td.assessedOrganization.name+'</td>\n';

                        html += '</tr><!-- End Trustmark Definition '+td.id+' -->\n\n\n';
                    }
                    html += '        </tbody>\n';
                    html += '    </table>\n';

                    html += '<div style="margin-top: 2em;"><a id="submitButton" href="javascript:doGenerate()" class="btn btn-primary">Generate Excel File</a></div>\n'

                }else{
                    // TODO Display some empty message?
                }

            }else{
                html += '<div class="alert alert-danger" style="width: 600px;">The server has returned an invalid or error response.  Please refresh the page and try again.</div>'
            }
            html += "</div>\n";

            return html;
        }

        function selectAllChecked(){
            if( $('#selectAllCheckbox').is(":checked") ){
                console.log("Checking all td checkboxes...");
                $('.tdCheckbox').prop('checked', true);
            }else {
                console.log("Unchecking all td checkboxes...");
                $('.tdCheckbox').prop('checked', false);
            }
            updateCheckboxFeedback();
        }//end selectAllChecked()

        function updateCheckboxFeedback(elem) {

            console.log("Checking all td checkboxes...");

            var allChecked = true;
            var anyChecked = false;

            console.log("elem: " + elem.toString());

            // clear first
            $('.tdCheckbox').each( function(index){
                var tdCheckbox = $(this);

                tdCheckbox.prop('checked', false);
            });

            $(elem).prop('checked', true);

        }//end updateCheckboxFeedback()

        /**
         *  Causes the Excel Spreadsheet to generate on the server, based on the selected TDs on this side.
         */
        function doGenerate() {

            initAssessmentToExcelReportState();

            STOP_LOOP = false;
            assessmentToExcelReportStatusLoop(${organization.id});

            startAssessmentToExcelReport();

        }//end doGenerate()

        let selectedAssessmentId = function() {
            var assessmentId = '-1';
            $('.tdCheckbox').each( function(index){
                var tdCheckbox = $(this);
                var currentId = tdCheckbox.attr('id').replace("tdCheckbox", "");
                if( tdCheckbox.is(":checked") ){
                    console.log(currentId+" is checked");
                    assessmentId = currentId;
                }
            });

            return assessmentId;
        }

        let startAssessmentToExcelReport = function() {
            console.log("startAssessmentToExcelReport...");

            var idToGen = selectedAssessmentId();

            console.log("Generating Excel file for assessment id: " + idToGen);

            if( idToGen.length > 0 ){
                $.ajax({
                    url: '${createLink(controller:'assessmentToExcel', action: 'excelGenerate')}',
                    method: 'POST',
                    data: {
                        format: 'json',
                        id: idToGen,
                        now: new Date().getMilliseconds() + ""
                    },
                    dataType: 'json',
                    beforeSend: function() {
                    },
                    success: function(data){

                        renderAssessmentToExcelReportStatus(data);
                    },
                    error: function(jqXHR, textStatus, errorThrown){
                        console.log("Error receiving data["+textStatus+"]: "+errorThrown);
                    }
                })
            }
         }

        function renderAssessmentToExcelReportStatus(data){

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
            $('#assessmentToExcelStatusMessage').html(html);
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

        let initAssessmentToExcelReportState = function () {

            $('#assessmentToExcelStatusMessage').html('');
            var assessmentId = selectedAssessmentId();

            var url = '${createLink(controller: 'assessmentToExcel',  action: 'initAssessmentToExcelReportState')}';
            $.ajax({
                url: url,
                dataType: 'json',
                async: false,
                data: {
                    id: assessmentId,
                    format: 'json'
                },
                beforeSend: function () {
                },
                success: function (data, statusText, jqXHR) {
                    // console.log("initAssessmentToExcelReportState: " + JSON.stringify(data));
                },
                error: function (jqXHR, statusText, errorThrown) {
                    console.log("Error: " + errorThrown);

                    $('#assessmentToExcelStatusMessage').html(errorThrown);
                }
            });
        }

        let assessmentToExcelReportStatusLoop = function() {

            if (!CANCEL_LOOP) {
                if (STOP_LOOP) {
                    console.log("STOP_LOOP");

                    renderAssessmentToExcelReport();

                    return;
                }

                updateAssessmentToExcelReportStatus();

                setTimeout(assessmentToExcelReportStatusLoop, 250);
            } else {
                $('#assessmentToExcelStatusMessage').html('');
                CANCEL_LOOP = false;
            }
        }

        let updateAssessmentToExcelReportStatus = function () {

            $.ajax({
                url: '${createLink(controller:'assessmentToExcel', action: 'assessmentToExcelReportStatusUpdate')}',
                method: 'GET',
                dataType: 'json',
                cache: false,
                data: {
                    format: 'json',
                    timestamp: new Date().getTime()
                },
                success: function(data, textStatus, jqXHR){
                    if( data && (data.status === "SUCCESS" || data.status === "ERROR")) {
                        STOP_LOOP = true;
                    }

                    if (CANCEL_LOOP === false && data && data.status === "CANCELLING" ) {
                        CANCEL_LOOP = true;
                    }

                    if (CANCEL_LOOP === true && data && data.status === "CANCELLED" ) {
                        CANCEL_LOOP = false;
                    }

                    renderAssessmentToExcelReportStatus(data);
                },
                error: function(jqXHR, textStatus, errorThrown){
                    console.log("updateAssessmentToExcelReportStatus error: " + textStatus);
                    if (errorThrown) {
                        console.log("       errorThrown: " + errorThrown.toString());
                    }
                }
            })
        }

        let renderAssessmentToExcelReport = function() {
            $.ajax({
                url: '${createLink(controller:'assessmentToExcel', action: 'renderAssessmentToExcelReport')}',
                method: 'GET',
                data: {
                    format: 'json',
                    now: new Date().getMilliseconds() + ""
                },
                dataType: 'json',
                beforeSend: function() {
                },
                success: function(data){

                    window.location = '${createLink(controller:'binary', action: 'view', id: '__REPLACEME__')}'.replace('__REPLACEME__', data.binaryId);
                },
                error: function(jqXHR, textStatus, errorThrown){
                    console.log("Error receiving data["+textStatus+"]: "+errorThrown);
                }

            })
        }

    </script>

	</body>
</html>

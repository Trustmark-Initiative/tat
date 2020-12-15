<%@ page import="nstic.web.ContactInformation; nstic.web.Organization" %>
<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main"/>
		<title>Create Trustmark(s)</title>

        <style type="text/css">

        </style>
	</head>
	<body>

        <h1>Create Trustmark(s) for ${assessment.assessmentName}</h1>
        <div class="pageSubsection">
            <div>
                Assessment Status: Marked as <assess:assessmentStatusIcon status="${assessment.status}" />  <assess:assessmentStatusName status="${assessment.status}" /> by ${assessment.statusLastChangeUser.username} on <g:formatDate date="${assessment.statusLastChangeDate}" format="yyyy-MM-dd" />
            </div>
            <div>
                Assessed Organization: ${assessment.assessedOrganization.name} (${assessment.assessedOrganization.uri})
            </div>

        </div>

        <g:if test="${flash.error}">
            <div style="margin-top: 2em;" class="alert alert-danger">
                ${flash.error}
            </div>
        </g:if>


        <div class="pageContent">
            <g:form class="form-horizontal" name="createTrustmarkForm" method="POST" controller="trustmark" action="save">
                <g:hiddenField name="assessmentId" id="assessmentId" value="${assessment.id}" />

                <div id="trustmarkDefinitionStatusTableContainer" style="margin-bottom: 2em;">
                    <asset:image src="spinner.gif" /> Loading Trustmark Definition satisfaction information...
                </div>


                <div class="form-group">
                    <label for="trustmarkMetadataId" class="col-sm-1 control-label">Metadata</label>
                    <div class="col-sm-11">
                        <g:if test="${metadataList.size() > 0}">
                            <g:select name="trustmarkMetadataId" id="trustmarkMetadataId" class="form-control" optionKey="id" optionValue="name" from="${metadataList}" onchange="checkExpiration();"/>
                            <span class="help-block">This metadata template will be used to generate metadata for the resulting trustmarks.</span>
                        </g:if>
                        <g:else>
                            <div class="alert alert-danger">No trustmark metadata sets have been defined. Follow this
                                <a href="${createLink(controller:'trustmarkMetadata', action:'create')}">link</a>
                            to create a metadata set.</div>
                        </g:else>
                    </div>
                </div>

                <div class="alert alert-info" id="expirationDateWarning">

                </div>

                <div class="form-group">
                    <div class="col-sm-offset-0 col-sm-10">
                        <button type="submit" class="btn btn-primary">Grant</button>
                        <a href="${createLink(controller:'assessment', action: 'view', id: assessment.id)}" class="btn btn-default">Cancel</a>
                    </div>
                </div>
            </g:form>

        </div>

        <script type="text/javascript">
            $(document).ready( function() {
                console.log("(document).ready( function() )");

                loadTrustmarkDefinitionStatus();
            });

            function loadTrustmarkDefinitionStatus(){
                var assId = '${assessment.id}';
                var url = '${createLink(controller: 'trustmark', action: 'getCreateInfoList')}';
                $.ajax({
                    url: url,
                    data: {
                        format: 'html',
                        timestamp: new Date().getTime(),
                        assessmentId: assId
                    },
                    dataType: 'html',
                    error: function(jqXHR, textStatus, errorThrown){
                        console.log("Received error: "+textStatus);
                        $('#trustmarkDefinitionStatusTableContainer').html('An unexpected error occurred contacting the server.  Please contact support.');
                        $('#trustmarkDefinitionStatusTableContainer').addClass('alert');
                        $('#trustmarkDefinitionStatusTableContainer').addClass('alert-danger');
                    },
                    success: function(data, textStatus, jqXHR){
                        $('#trustmarkDefinitionStatusTableContainer').html(data);

                        checkExpiration();
                    }

                })
            }

            function checkExpiration() {

                var rselect = document.getElementById('trustmarkMetadataId');
                var selectedValue = rselect.options[rselect.selectedIndex].text;

                var selectedMetadataId = $('#trustmarkMetadataId').val();

                console.log("selectedMetadataId: " + selectedMetadataId);

                // ajax to get selected certificate expiration and exception related time periods
                var url = '${createLink(controller: 'trustmark', action: 'getExpirationData')}';
                $.ajax({
                    url: url,
                    data: {
                        format: 'json',
                        timestamp: new Date().getTime(),
                        selectedMetadataId: selectedMetadataId
                    },
                    beforeSend: function() {
                    },
                    success: function(data, statusText, jqXHR){
                        console.log("getCertificateExpirationDate SUCCESS");

                        var date = data["certificateExpirationDate"];

                        var certificateExpirationDate = new Date(Date.parse(date));
                        console.log("parsed date: " + certificateExpirationDate.toString());

                        var now = new Date();
                        var timePeriodNoExcepionts = data["timePeriodNoExcepionts"];
                        var noExceptionExpirationDate = addMonthsToDate(now, timePeriodNoExcepionts);

                        var timePeriodWithExceptions = data["timePeriodWithExceptions"];
                        var withExceptionExpirationDate = addMonthsToDate(now, timePeriodWithExceptions);

                        var assessingOrganizationId = data["assessingOrganizationId"];

                        var html = "";
                        // compare the expiration dates
                        if (noExceptionExpirationDate > certificateExpirationDate ||
                            withExceptionExpirationDate > certificateExpirationDate) {

                            $('#expirationDateWarning').show();

                            // render expiration warning
                            html += '<div class="alert alert-danger style="margin-top: 1em;">';
                            html += '<div>The signing certificate specified for the selected metadata set is going to expire before the expiration date for the generated trustmark(s).</div>';

                            html += '<div>Please select a different metadata set or <a href="${createLink(controller: 'organization', action: 'view')}/' + assessingOrganizationId + '">generate</a> a new certificate for the origanization and select that certificate for the medatata set.</div>';

                            html += '</div>\n';
                        } else {
                            $('#expirationDateWarning').hide();
                        }

                        $('#expirationDateWarning').html(html);

                    },
                    error: function(jqXHR, statusText, errorThrown){
                        console.log("getExpirationData ERROR");
                    }
                });
            }

            function addMonthsToDate(date, numberOfMonths) {
                var d = new Date(date);
                var years = Math.floor(numberOfMonths / 12);
                var months = numberOfMonths - (years * 12);

                if (years) {
                    d.setFullYear(d.getFullYear() + years);
                }

                if (months) {
                    d.setMonth(d.getMonth() + months);
                }

                return d;
            }

            function updateExpirationDate(){
                console.log("Called updateExpirationDate()")

                var expirationValue = "2014-01-01";
                if( $('#hasExceptions').is(':checked') ) {
                    expirationValue = $('#_expWithExceptions').val()
                }else{
                    expirationValue = $('#_expWithoutExceptions').val()
                }

                $('#expirationDateTime').val(expirationValue);

            }//end updateExpirationDate()
        </script>

	</body>
</html>

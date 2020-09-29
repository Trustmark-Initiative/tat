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
                        <g:select name="trustmarkMetadataId" id="trustmarkMetadataId" class="form-control" optionKey="id" optionValue="name" from="${metadataList}" />
                        <span class="help-block">This metadata template will be used to generate metadata for the resulting trustmarks.</span>
                    </div>
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
                        console.log("A successfully response was sent back.");
                        $('#trustmarkDefinitionStatusTableContainer').html(data);
                    }

                })
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

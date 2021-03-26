
<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main"/>
		<title>Generating Trustmark(s)</title>

        <style type="text/css">

        </style>
	</head>
	<body>

        <h1>Generating Trustmark(s) for ${assessment.assessmentName}</h1>

        <g:if test="${flash.error}">
            <div style="margin-top: 2em;" class="alert alert-danger">
                ${flash.error}
            </div>
        </g:if>

    <div style="margin-top: 4em;">
        <h4>Status</h4>
        <div id="trustmarkGenerationDisplayInfoStatus"></div>
    </div>

    <div class="pageContent">

        <div class="col-sm-offset-0 col-sm-10">
            <a href="javascript:cancelTrustmarkGeneration()" class="btn btn-default">Cancel</a>
        </div>

    </div>

    <script type="text/javascript">
        var TM_GEN_STATUS_UPDATE = null;
        var STOP_LOOP = false;
        var CANCEL_LOOP = false;

        $(document).ready( function() {

            // clear status element
            $("trustmarkGenerationDisplayInfoStatus").empty();

            generateTrustmarks();

            STOP_LOOP = false;
            trustmarkGenerationDisplayInfoStatusLoop();
        });

        // rename to save trustmarks
        function generateTrustmarks(){

            var assId = '${assessment.id}';
            var url = '${createLink(controller: 'trustmark', action: 'generateTrustmarkList')}';
            $.ajax({
                url: url,
                type: 'POST',
                data: {
                    format: 'html',
                    timestamp: new Date().getTime(),
                    assessmentId: assId
                },
                dataType: 'html',
                error: function(jqXHR, textStatus, errorThrown){
                    // console.log("Received error: "+textStatus);
                    console.log("generateTrustmarks error: " + textStatus);
                    if (errorThrown) {
                        console.log("       errorThrown: " + errorThrown.toString());
                    }
                    $('#trustmarkGenerationDisplayInfoStatus').html('An unexpected error occurred contacting the server.  Please contact support.');
                    $('#trustmarkGenerationDisplayInfoStatus').addClass('alert');
                    $('#trustmarkGenerationDisplayInfoStatus').addClass('alert-danger');
                },
                success: function(data, textStatus, jqXHR){
                    // redirect to the assessment view page
                    console.log("generateTrustmarks success: " + textStatus);

                    $('#trustmarkGenerationDisplayInfoStatus').html(data);
                }
            })
        }

        function saveTrustmarks(){

            var assId = '${assessment.id}';
            var url = '${createLink(controller: 'trustmark', action: 'save')}';
            $.ajax({
                url: url,
                type: 'POST',
                data: {
                    format: 'json',
                    timestamp: new Date().getTime(),
                    assessmentId: assId
                },
                dataType: 'json',
                error: function(jqXHR, textStatus, errorThrown){
                    // console.log("Received error: "+textStatus);
                    console.log("saveTrustmarks error: " + textStatus);
                    if (errorThrown) {
                        console.log("       errorThrown: " + errorThrown.toString());
                    }
                    $('#trustmarkGenerationDisplayInfoStatus').html('An unexpected error occurred contacting the server.  Please contact support.');
                    $('#trustmarkGenerationDisplayInfoStatus').addClass('alert');
                    $('#trustmarkGenerationDisplayInfoStatus').addClass('alert-danger');
                },
                success: function(data, textStatus, jqXHR){
                    // redirect to the assessment view page
                    window.location.href = data["href"];
                }
            })
        }

        function cancelTrustmarkGeneration() {
            CANCEL_LOOP = true;
            var assessmentId = '${assessment.id}';
            $.ajax({
                url: '${createLink(controller:'trustmark', action: 'cancelTrustmarkGeneration')}',
                method: 'GET',
                dataType: 'json',
                cache: false,
                data: {
                    format: 'json',
                    timestamp: new Date().getTime(),
                    assessmentId: assessmentId
                },
                success: function(data, textStatus, jqXHR){
                    // redirect to the assessment view page
                    window.location.href = data["href"];
                },
                error: function(jqXHR, textStatus, errorThrown){
                    console.log("cancelTrustmarkGeneration error: " + textStatus);
                    if (errorThrown) {
                        console.log("       errorThrown: " + errorThrown.toString());
                    }
                }
            })
        }

        function trustmarkGenerationDisplayInfoStatusLoop() {

            if (!CANCEL_LOOP) {
                if (STOP_LOOP) {

                    saveTrustmarks();

                    return;
                }

                updateTrustmarkGenerationDisplayInfoStatus();

                setTimeout(trustmarkGenerationDisplayInfoStatusLoop, 250);
            }
        }

        function updateTrustmarkGenerationDisplayInfoStatus() {

            $.ajax({
                url: '${createLink(controller:'trustmark', action: 'trustmarkGenerationStatusUpdate')}',
                method: 'GET',
                dataType: 'json',
                cache: false,
                data: {
                    format: 'json',
                    timestamp: new Date().getTime()
                },
                success: function(data, textStatus, jqXHR){
                    TM_GEN_STATUS_UPDATE = data;
                    if( data && data.status == "SUCCESS" )
                        STOP_LOOP = true;

                    renderTrustmarkGenerationDisplayInfoStatus(data);
                },
                error: function(jqXHR, textStatus, errorThrown){
                    // console.log("Error updating trustmark definitions info list status!");
                    console.log("updateTrustmarkGenerationDisplayInfoStatus error: " + textStatus);
                    if (errorThrown) {
                        console.log("       errorThrown: " + errorThrown.toString());
                    }
                }
            })
        }

        function renderTrustmarkGenerationDisplayInfoStatus(data){

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
                html = '<div class="alert alert-danger"><b>Error</b> <br/> The server returned an invalid response.  Please refresh the page and try again.</div>';
            }
            $('#trustmarkGenerationDisplayInfoStatus').html(html);
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

	</body>
</html>

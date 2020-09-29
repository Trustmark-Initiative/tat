<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main"/>
		<title>Home</title>

        <style type="text/css">
            .feedbackContainer {
                margin-bottom: 1em;
            }
        </style>
	</head>
	<body>
        %{--<ol class="breadcrumb">--}%
            %{--<li class="active">Home</li>--}%
        %{--</ol>--}%

        <h1>System Import/Export</h1>
        <div class="pageSubsection">
            On this page, you can import and export the system data.
        </div>

        <div id="buttonWindow" style="margin-top: 2em;">
            <div class="well well-lg">
                <a href="#" class="btn btn-default">Import</a>
                <span style="margin-left: 1em;">
                    Prompts you for a file that was exported from an assessment tool for re-import.
                </span>
            </div>
            <div class="well well-lg" style="margin-top: 1em;">
                <a href="javascript:exportButtonClick()" class="btn btn-default">Export</a>
                <span style="margin-left: 1em;">
                    Generates a complete system backup from this assessment tool's database.  Suitable for offline
                    backup.
                </span>
            </div>
            <div class="well well-lg" style="margin-top: 1em;">
                <a href="javascript:exportReap()" class="btn btn-default">Export Reap</a>
                <span style="margin-left: 1em;">
                    Clears the database of any previous export thread data, except those currently executing - but
                    ignores them.
                </span>
            </div>
        </div>

        <div id="exportFeedbackWindow" style="margin-top: 2em;">
            <div id="exportInitializeContainerMsg" class="feedbackContainer">
                <span id="exportIcon1" class="glyphicon glyphicon-time" title="Waiting to start..."></span>
                Telling the server to initiate the export...<span id="exportInitializeContainerFeedback"></span>
            </div>
            <div id="exportProgressContainer" class="feedbackContainer">
                <span id="exportIcon2" class="glyphicon glyphicon-time" title="Waiting to start..."></span>
                Exporting...<span id="exportProgressCompleteFeedback"></span>

            </div>

        </div>


    <script type="text/javascript">
        $(document).ready(function(){
           $('#exportFeedbackWindow').hide();
        });

        var THREAD_NAME = null;
        var WAITING_ICON_CLASS = 'glyphicon-time';
        var WORKING_ICON_CLASS = 'glyphicon-refresh';
        var ERROR_ICON_CLASS = 'glyphicon-alert';
        var SUCCESS_ICON_CLASS = 'glyphicon-ok';

        var rotate = false;
        var rotateId = '';
        function doRotate(){
            if( rotate ) {
                console.log("Doing rotation...");
                $({deg: 0}).animate({deg: 360}, {
                    duration: 1500,
                    step: function(now){
                        $('#'+rotateId).css({
                            '-moz-transform':'rotate('+now+'deg)',
                            '-webkit-transform':'rotate('+now+'deg)',
                            '-o-transform':'rotate('+now+'deg)',
                            '-ms-transform':'rotate('+now+'deg)',
                            'transform':'rotate('+now+'deg)'
                        })
                    }
                });
                setTimeout('doRotate()', 1600);
            }
        }
        function initRotate(id){
            rotate = true;
            rotateId = id;
            $('#'+rotateId).removeClass(WAITING_ICON_CLASS);
            $('#'+rotateId).addClass(WORKING_ICON_CLASS);
            $('#'+rotateId).attr('title', 'Process has started.');
            doRotate();
        }
        function stopRotate(newIconClass) {
            rotate = false;
            var now = 0;
            $('#'+rotateId).css({
                '-moz-transform':'rotate('+now+'deg)',
                '-webkit-transform':'rotate('+now+'deg)',
                '-o-transform':'rotate('+now+'deg)',
                '-ms-transform':'rotate('+now+'deg)',
                'transform':'rotate('+now+'deg)'
            }) // Resets the rotation
            $('#'+rotateId).removeClass(WORKING_ICON_CLASS);
            $('#'+rotateId).addClass(newIconClass);
            $('#'+rotateId).attr('title', 'Process has completed.');
        }

        function exportButtonClick() {
            if( confirm('Really start export?  It could take a while...') ){
                $('#buttonWindow').hide();
                $('#exportFeedbackWindow').show();
                initRotate('exportIcon1');
                exportStart();
            }
        }

        function exportStart() {
            var url = '${createLink(controller: 'admin', action: 'startExport')}';
            console.log("Sending put request to: "+url);
            $.ajax({
                url: url,
                type: 'POST',
                data: {
                    timestamp: new Date().getTime(),
                    format: 'json'
                },
                dataType: 'json',
                success: function (data, textStatus, jqXHR) {
                    console.log("Received start export response: " + JSON.stringify(data, null, '   '));
                    if( data && data.status && data.status == "SUCCESS" ){
                        THREAD_NAME = data.threadName;
                        stopRotate(SUCCESS_ICON_CLASS);
                        addSuccessMessage('exportInitializeContainerFeedback', data.message);
                        initRotate('exportIcon2');
                        initMonitor();
                    }else{
                        stopRotate(ERROR_ICON_CLASS);
                        addErrorMessage('exportInitializeContainerFeedback', 'An unexpected error occurred starting the export!  Please contact support.');
                    }

                },
                error: function (jqXHR, textStatus, errorThrown) {
                    console.log("ERROR posting startExport command to server!  textStatus=["+textStatus+"], errorThrown: "+JSON.stringify(errorThrown, null, '  '));
                    alert("ERROR posting startExport command to server!");
                }

            })
        }//end exportStart()

        function addSuccessMessage(id, msg){
            $('#'+id).html('<span style="font-weight: bold; color: rgb(0, 150, 0);">'+msg+'</span>');
        }
        function addErrorMessage(id, msg){
            $('#'+id).html('<span style="font-weight: bold; color: rgb(150, 0, 0);">'+msg+'</span>');
        }


        function initMonitor() {
            var html =
                '<div style="margin-left: 1em;" id="exportFeedbackPane">'+
                '<div class="progress" id="exportProgressBarContainer">'+
                '    <div id="exportProgressBar" class="progress-bar" role="progressbar" aria-valuenow="0" aria-valuemin="0" aria-valuemax="100" style="width: 0%;">'+
                '        <span class="sr-only" id="exportProgressBarStatusText">0% Complete</span>'+
                '    </div>'+
                '</div>'+
                '<div class="well well-lg" id="outputContainer" style="overflow-y: visible; overflow-x: hidden; max-height: 150px;">'+
                '</div>'+
                '</div>'
            $('#exportProgressContainer').append('<div style="margin-top: 1em;">'+html+'</div>');
            doMonitor();
        }

        function setProgress(progressVal){
            $('#exportProgressBar').css('width', progressVal+"%").attr('aria-valuenow', progressVal);
            $('#exportProgressBarStatusText').html(progressVal+'% Complete')
        }

        var LAST_MESSAGE = null;

        function doMonitor(){
            var url = '${createLink(controller: 'admin', action: 'checkExportStatus')}';
            console.log("Sending get request to: "+url);
            $.ajax({
                url: url,
                type: 'GET',
                data: {
                    timestamp: new Date().getTime(),
                    format: 'json',
                    threadName: THREAD_NAME
                },
                dataType: 'json',
                success: function (data, textStatus, jqXHR) {
                    console.log("Received export status response: " + JSON.stringify(data, null, '   '));
                    if( data && data.status && data.status == "SUCCESS" ){
                        setProgress(data.progress);
                        if( data.message && data.message != LAST_MESSAGE ){
                            $('#outputContainer').prepend('<div>'+data.message+'</div>');
                            LAST_MESSAGE = data.message;
                        }


                        // TODO Handle this
                        if( data.executionStatus != "FINISHED SUCCESSFULLY" ) {
                            setTimeout('doMonitor();', 500);
                        }else{
                            setProgress(100);
                            stopRotate(SUCCESS_ICON_CLASS);
                            addSuccessMessage('exportProgressCompleteFeedback', 'Export completed successfully!');
                            // TODO Other cleanup code

                            var url = '${createLink(controller:'admin', action:'downloadExportZip', params: ['threadName': '__REPLACEME__'])}';
                            url = url.replace("__REPLACEME__", THREAD_NAME);
                            $('#exportFeedbackPane').append('<div style="margin-top: 2em;"><a href="'+url+'" target="_blank">Download Zip File</a></div>')
                        }

                    }else{
                        // TODO Display the error message!
                    }

                },
                error: function (jqXHR, textStatus, errorThrown) {
                    console.log("ERROR getting export status command from server!  textStatus=["+textStatus+"], errorThrown: "+JSON.stringify(errorThrown, null, '  '));
                    alert("ERROR getting export status command from server!");
                }

            })
        }

        function exportReap() {
            var url = '${createLink(controller: 'admin', action: 'reapExportThreadData')}';
            console.log("Sending get request to: "+url);
            $.ajax({
                url: url,
                type: 'GET',
                data: {
                    timestamp: new Date().getTime(),
                    format: 'json'
                },
                dataType: 'json',
                success: function (data, textStatus, jqXHR) {
                    console.log("Received export status response: " + JSON.stringify(data, null, '   '));
                    if( data && data.status && data.status == "SUCCESS" ){
                       alert("Successfully cleaned the database.");
                    }else{
                        alert("ERROR Cleaning the database.");
                    }

                },
                error: function (jqXHR, textStatus, errorThrown) {
                    console.log("ERROR getting export status command from server!  textStatus=["+textStatus+"], errorThrown: "+JSON.stringify(errorThrown, null, '  '));
                    alert("UNEXPECTED ERROR Cleaning the database.");
                }

            })
        }

    </script>
	</body>
</html>

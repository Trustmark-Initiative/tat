<%@ page import="nstic.web.ScanHostJob; nstic.web.SystemVariable" %>
<html>
    <head>
        <meta name="layout" content="main"/>
        <title>TPAT Management</title>


        <script type="text/javascript">
            var SCAN_JOB_DETAILS = null;
            var STOP_LOOP = false;

            $(document).ready(function(){
                updateScanJobDetails();

                // disabled for the moment
                //updateFormatCheckData();
            })


            function updateScanJobDetails(){
                $.ajax({
                    url: '${createLink(controller:'tdAndTipUpdate', action: 'scanHostJobDetails')}',
                    method: 'GET',
                    dataType: 'json',
                    cache: false,
                    data: {
                        timestamp: new Date().getTime()
                    },
                    success: function(data, textStatus, jqXHR){
                        SCAN_JOB_DETAILS = data;
                        console.log("Successfully received scanJobDetails response: "+JSON.stringify(data));
                        renderScanJobDetails(data);
                        if( data && data.status == "SUCCESS" )
                            STOP_LOOP = true;
                    },
                    error: function(jqXHR, textStatus, errorThrown){
                        console.log("Error updating scan job details!");
                    }
                })
            }

            function renderScanJobDetails(data){
                var html = '';
                if( data && data.status ){
                    html += '<div class="well"><h3>';
                    if( data.status == "SUCCESS" ) {
                        html += '<span class="glyphicon glyphicon-ok-circle"></span> SUCCESS';
                    }else if( data.status.startsWith("SYNC") ){
                        html += '<span class="glyphicon glyphicon-refresh"></span> '+data.status;
                    }else{
                        html += '<span class="glyphicon glyphicon-question-sign"></span> UNKNOWN? : '+data.status;
                    }
                    html += '</h3>';
                    if( data.status.startsWith("SYNC") ){
                        html += buildProgressBarHtml(data.percent);
                    }
                    html += '<div>' + data.message + '</div>';

                    if( (data.tdCacheDates && data.tdCacheDates.length > 0) || (data.tipCacheDates && data.tipCacheDates.length > 0) ){
                        html += '<div style="margin-top: 2em;">';
                        if( data.tdCacheDates && data.tdCacheDates.length > 0 ){
                            for( var i = 0; i < data.tdCacheDates.length; i++ ){
                                html += '<div>'+data.tdCacheDates[i].source+' TDs cached at '+data.tdCacheDates[i].prettyTime+'</div>';
                            }
                        }
                        if( data.tipCacheDates && data.tipCacheDates.length > 0 ){
                            for( var i = 0; i < data.tipCacheDates.length; i++ ){
                                html += '<div>'+data.tipCacheDates[i].source+' TIPs cached at '+data.tipCacheDates[i].prettyTime+'</div>';
                            }
                        }
                        html += '</div>';
                    }


                    if( data.status == "SUCCESS" ){
                        html += '<div style="margin-top: 2em;">';
                        html += '    <a href="javascript:startScanHostJob();" class="btn btn-primary">Start</a>';
                        // html += '    <a href="javascript:clearScanHostVariables();" class="btn btn-default">Clear Variables</a>';
                        // html += '<div class="text-muted">To force a re-check of each TD &amp; TIP: hit clear variables, then start.  Otherwise if the cache is up to date, nothing will occur.</div>\n';
                        html += '<div class="text-muted">To force a re-check of each TD &amp; TIP: hit start.</div>\n';
                        html += '</div>'
                    }else{

                    }
                    html += '</div>';

                }else{
                    html = '<div class="alert alert-danger"><b>Error</b> <br/> The server returned an invalid response.  Please refresh the page and try again.</div>';
                }
                $('#scanJobDetails').html(html);
            }

            function scanHostUpdateLoop() {
                if( STOP_LOOP ){
                    return;
                }
                updateScanJobDetails();
                setTimeout("scanHostUpdateLoop()", 500);
            }

            /**
             * Confirms with the user, then subsequently beings the job of scanning remote hosts...
             */
            function startScanHostJob(){
                if( !confirm("Are you sure you want to start this job?") ){
                    return;
                }
                $.ajax({
                    url: '${createLink(controller:'tdAndTipUpdate', action: 'startScanHostJob')}',
                    method: 'GET',
                    dataType: 'json',
                    cache: false,
                    data: {
                        timestamp: new Date().getTime()
                    },
                    success: function(data, textStatus, jqXHR){
                        console.log("Successfully received startScanHostJob response: "+JSON.stringify(data));
                        STOP_LOOP = false;
                        scanHostUpdateLoop();
                    },
                    error: function(jqXHR, textStatus, errorThrown){
                        console.log("Error executing startScanHostJob!");
                    }
                })
            }

            /**
             * Confirms with the user, then subsequently beings the job of scanning remote hosts...
             */
            function clearScanHostVariables(){
                if( !confirm("Are you sure you want to clear the scheduling variables?  Doing so will cause a full update inspection on the next scheduled cache update.") ){
                    return;
                }
                $.ajax({
                    url: '${createLink(controller:'tdAndTipUpdate', action: 'clearScanHostVariables')}',
                    method: 'GET',
                    dataType: 'json',
                    cache: false,
                    data: {
                        timestamp: new Date().getTime()
                    },
                    success: function(data, textStatus, jqXHR){
                        console.log("Successfully received clearScanHostVariables response: "+JSON.stringify(data));
                        setTimeout("updateScanJobDetails();", 500);
                    },
                    error: function(jqXHR, textStatus, errorThrown){
                        console.log("Error executing clearScanHostVariables!");
                    }
                })
            }


            /**
             * Contains the FORMAT_CHECK status as given by the server.
             */
            var FORMAT_CHECK = null;

            /**
             * First makes a call to the server to get the latest FORMAT_CHECK data.  Next, initiates the user interface
             * update once the value is successfully retrieved.  Note that if the response from the server is COMPLETE,
             * then the loop is terminated.
             */
            function updateFormatCheckData() {
                $.ajax({
                    url: '${createLink(controller:'tdAndTipUpdate', action: 'formatCheck')}',
                    method: 'GET',
                    dataType: 'json',
                    cache: false,
                    data: {
                        timestamp: new Date().getTime()
                    },
                    success: function(data, textStatus, jqXHR){
                        console.log("Successfully received formatCheck response: "+JSON.stringify(data));
                        FORMAT_CHECK = data;
                        renderFormatCheck();
                        if( data && data.status && data.status === "COMPLETE" ){
                            console.log("Format check is complete.");
                            STOP_FC_LOOP = true;
                        }else{
                            setTimeout("updateFormatCheckData()", 500);
                        }
                    },
                    error: function(jqXHR, textStatus, errorThrown){
                        console.log("Error executing clearScanHostVariables!");
                    }
                })
            }

            /**
             * Assumes that FORMAT_CHECK contains javascript from the server and creates the user interface based on the
             * values therein.
             */
            function renderFormatCheck() {
                var html = '';
                html += '<div class="well">\n';

                // TODO Perform better rendering...
                html += "<div>";
                if( FORMAT_CHECK.status === "PROCESS_TDS" || FORMAT_CHECK.status === "PROCESS_TIPS" ) {
                    html += '<span class="glyphicon glyphicon-refresh"></span> ';
                }else if( FORMAT_CHECK.status === "COMPLETE" ){
                    html += '<span class="glyphicon glyphicon-ok-circle"></span> ';
                }else if( FORMAT_CHECK.status === "ERROR" ){
                    html += '<span class="glyphicon glyphicon-remove-circle" title="An unexpected error has happened on the server."></span> ';
                }else{
                    html += FORMAT_CHECK.status + ": ";
                }
                html += FORMAT_CHECK.message;
                html += "</div>";

                html += '<div>Last Stored Timestamp: '+FORMAT_CHECK.timestamp+'</div>\n';

                if( FORMAT_CHECK.status === "PROCESS_TDS" ) {

                    html += "<div>Processing TD #"+FORMAT_CHECK.data.count+" of "+FORMAT_CHECK.data.tdsCount+"</div>";

                    var percent = (FORMAT_CHECK.data.count / FORMAT_CHECK.data.tdsCount) * 100;
                    html += buildProgressBarHtml(percent);
                }else if( FORMAT_CHECK.status === "PROCESS_TIPS" ) {

                    html += "<div>Processing TIP #"+FORMAT_CHECK.data.count+" of "+FORMAT_CHECK.data.tipsCount+"</div>";

                    var percent = (FORMAT_CHECK.data.count / FORMAT_CHECK.data.tipsCount) * 100;
                    html += buildProgressBarHtml(percent);
                }

                if( !FORMAT_CHECK.executing ){
                    html += '<div style="margin-top: 2em;">\n';
                    html += '    <a href="javascript:beginFormatCheck()" class="btn btn-primary">Begin Format Check</a>\n';
                    html += '</div>\n';
                }else{
                    html += '<div style="margin-top: 2em;">\n';
                    html += '    <a href="javascript:resetExecutingVar()" class="btn btn-danger">Stop Executing</a>\n';
                    html += '</div>\n';
                }

                html += '</div>\n\n';
                $('#formatCheckFeedback').html(html);
            }

            function resetExecutingVar(){
                if( !confirm('Really reset the executing variable?  If the system is executing, it will cause immediate termination (and can lead to an incomplete finish).') )
                    return;
                $.ajax({
                    url: '${createLink(controller:'tdAndTipUpdate', action: 'clearFormatCheckExecutingVar')}',
                    method: 'GET',
                    dataType: 'json',
                    cache: false,
                    data: {
                        timestamp: new Date().getTime()
                    },
                    success: function(data, textStatus, jqXHR){
                        console.log("Successfully received clearFormatCheckExecutingVar response: "+JSON.stringify(data));
                    },
                    error: function(jqXHR, textStatus, errorThrown){
                        console.log("Error executing clearFormatCheckExecutingVar!");
                    }
                })
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

            /**
             * Called to kick off the format check on the server side.  Note that this method has javascript which will
             * first confirm that's really what the user wants to do.  Initiates the format check loop.
             */
            function beginFormatCheck() {
                if( !confirm('Really check the format of each TD and TIP?  This can take a while...') )
                    return;

                $.ajax({
                    url: '${createLink(controller:'tdAndTipUpdate', action: 'startTdAndTipFormatCheck')}',
                    method: 'GET',
                    dataType: 'json',
                    cache: false,
                    data: {
                        timestamp: new Date().getTime()
                    },
                    success: function(data, textStatus, jqXHR){
                        console.log("Successfully started TD and TIP format check!");
                        STOP_FC_LOOP = false;
                        formatCheckUpdateLoop();
                    },
                    error: function(jqXHR, textStatus, errorThrown){
                        console.log("Error listing all Tds and TIPs!");
                    }
                })

            }

            /**
             * If set to true, and the update loop is running, then this will cause the update loop to stop.
             */
            var STOP_FC_LOOP = false;

            /**
             * Actually performs the update loop.  Calls itself every 500ms unless STOP_FC_LOOP is set to true.
             */
            function formatCheckUpdateLoop() {
                if( STOP_FC_LOOP ){
                    return;
                }
                updateFormatCheckData();
            }
        </script>

    </head>
    <body>
        <div class="row">
            <div class="col-md-9">
                <h1>TPAT Management</h1>
                <div class="text-muted">
                    This page allows you to manage some high-level administrative cache actions for the TDs & TIPs that have been
                    cached.
                </div>
            </div>
            <div class="col-md-3" style="font-size: 150%;">
                <div>Cache Details:</div>
                <div>
                    <span style="font-weight: bold;">${nstic.web.td.TrustmarkDefinition.count()}</span> Trustmark Definitions
                </div>
                <div>
                    <span style="font-weight: bold;">${nstic.web.tip.TrustInteroperabilityProfile.count()}</span> TIPs
                </div>
            </div>
        </div>

        <div style="margin-top: 4em;">
            <h3>TPAT or Registry URLs</h3>

            <table class='table table-condensed table-striped table-bordered'>
                <thead>
                <tr>
                    <g:sortableColumn property="registryName" title="Name" />
                    <g:sortableColumn property="registryUrl" title="URL" />
                </tr>
                </thead>
                <tbody id="registrUrlTableRows">
                <%
                    List<nstic.web.Registry> registryUrls = nstic.web.Registry.findAll()
                %>
                <g:if test="${registryUrls && registryUrls.size() > 0}">
                    <g:each in="${registryUrls}" var="registry">
                        <tr>
                            <td style="font-size: 120%;">
                                ${registry.name}
                            </td>
                            <td>
                                ${registry.registryUrl}
                            </td>
                        </tr>
                    </g:each>
                </g:if>
                <g:else>
                    <tr id="noTpatRegistryUrlsMessage">
                        <td colspan="2"><em>There are no TPAT registry URLs stored.</em></td>
                    </tr>
                </g:else>
                </tbody>

            </table>

            <div id="addRegistryUrlButton">
                <a href="#" class="btn btn-primary" onclick="enableUrlUI();">Add URL</a>
            </div>

            <div id="inputRegistryUrl" style="display: none">

                <div class="form">
                    <div class="title">Registry Name</div>
                    <input id="registryNameId" type="text" class="input-text">
                    <div class="content">Registry URL</div>
                    <input id="registryUrlId" class="input-text"></input>
                    <div class="a">
                        <button type="button" class="btn btn-primary" href="javascript:void(0)" id="save">Save</button>
                    </div>

                    <div id="saveRegistryStatus">
                    </div>
                </div>
            </div>

            <script type="text/javascript">

                $('#save').click(function() {

                    if ( $('#registryNameId').val() == '' ){
                        alert('Please enter a name.');
                    }
                    else if ($('#registryUrlId').val() == '') {
                        alert('Please enter a URL.');
                    }
                    else {
                        var url = '${createLink(controller: 'tdAndTipUpdate', action: 'saveRegistry')}';
                        $.ajax({
                            url: url,
                            dataType: 'json',
                            data: {
                                registryName: $('#registryNameId').val(),
                                registryUrl: $('#registryUrlId').val(),
                                format: 'json'
                            },
                            error: function( jqXHR, textStatus, errorThrown ){
                                console.log("An error occurred while saving the registry url!  textStatus=["+textStatus+"], errorThrown: "+JSON.stringify(errorThrown, null, '  '));
                                $('#saveRegistryStatus').html('<div class="alert alert-danger">An error occurred while saving the registry url.  Please try again.</div>')
                            },
                            success: function(result, textStatus, jqXHR){
                                console.log("Received registry result: "+JSON.stringify(result, null, 4));

                                var name = result.registryName;
                                var url = result.registryUrl;

                                if( name && url  ) {

                                    $('#registrUrlTableRows').append('<tr> <td style="font-size: 120%;">' + result.registryName + '</td> <td>' + result.registryUrl + '</td> </tr>');

                                    showElement("inputRegistryUrl",  false);
                                    $('#registryNameId').val('');
                                    $('#registryUrlId').val('');

                                    $('#noTpatRegistryUrlsMessage').style.display = "none";
                                }else{
                                    $('#saveRegistryStatus').html('<div class="alert alert-danger">An error occurred while loading the remote data.  Please refresh and try again.</div>')
                                }

                                $('#selectExistingCertificatesControl').html(html);
                            }
                        })

                    }
                });

                function enableUrlUI() {
                    showElement("inputRegistryUrl",  true);
                }

                function showElement(elemId, show) {

                    var elem = document.getElementById(elemId);
                    if (show) {
                        elem.style.display = "block";
                    } else {
                        elem.style.display = "none";
                    }
                }
            </script>
        </div>

        <div style="margin-top: 4em;">
            <h3>TD & TIP Update Scheduling</h3>
            <div class="text-muted">Here you can force the ScanHostJob to execute, and update TDs and TIPs.</div>
            <div id="scanJobDetails"></div>
        </div>


%{-- Disabled for the moment--}%
%{--        <div style="margin-top: 4em;">--}%
%{--            <h3>TD & TIP Format Check</h3>--}%
%{--            <div class="text-muted">Check each TD &amp; TIP to make sure their format matches the source format.  Useful if the JSON or XML version changes, but the TD or TIP did not.</div>--}%
%{--            <div id="formatCheckFeedback"></div>--}%
%{--        </div>--}%

    </body>

</html>
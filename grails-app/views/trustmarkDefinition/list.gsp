<%@ page import="nstic.web.Role" %>
<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main"/>
		<title>Trustmark Definitions</title>

        <style type="text/css">

        </style>
	</head>
	<body>

        <h1>Trustmark Definitions <small>(${trustmarkDefinitionsCount} total)</small></h1>
        <div class="pageSubsection">
            This page allows you to view all Trustmark Definitions in the system.  Note that Trustmark Definitions
            are loaded from <i>other</i> places.  To add them, you need to add a fully compliant Trustmark Framework
            hosting server URL in the configuration.  You can force this page to recheck for TDs (a very time consuming
            process) by clicking the button below.  Additionally, the configuration will allow you to specify cron
            triggers for the update process.
        </div>

        <div id="messageContainer">
            <g:if test="${flash.message}">
                <div style="margin-top: 2em;" class="alert alert-info">
                    ${flash.message}
                </div>
            </g:if>
            <g:if test="${flash.error}">
                <div style="margin-top: 2em;" class="alert alert-danger">
                    ${flash.error}
                </div>
            </g:if>
        </div>

        <div class="pageContent">
            <table class="table table-striped table-bordered table-condensed">
                <thead>
                    <tr>
                        <th>Enabled?</th>
                        <g:sortableColumn property="name" title="Name" />
                        <g:sortableColumn property="tdVersion" title="Version" />
                        <th>Description</th>
                    </tr>
                </thead>
                <tbody>
                    <g:if test="${trustmarkDefinitions && trustmarkDefinitions.size() > 0}">
                        <g:each in="${trustmarkDefinitions}" var="td">
                            <tr>
                                <td style="text-align: center;">
                                    <g:if test="${td.enabled}">
                                        <a href="javascript:disableTd(${td.id})">
                                            <span class="glyphicon glyphicon-ok" title="This trustmark definition IS enabled." style="color: rgb(0, 150, 0);"></span>
                                        </a>
                                    </g:if>
                                    <g:else>
                                        <a href="javascript:enableTd(${td.id})">
                                            <span class="glyphicon glyphicon-remove" title="This trustmark definition is NOT enabled." style="color: rgb(150, 0, 0);"></span>
                                        </a>
                                    </g:else>
                                </td>
                                <td>
                                    <g:link controller="trustmarkDefinition" action="view" id="${td.id}">
                                        ${td.name}
                                    </g:link>
                                </td>
                                <td>${td.tdVersion}</td>
                                <td>${td.description}</td>
                            </tr>
                        </g:each>
                    </g:if>
                    <g:else>
                        <tr>
                            <td colspan="4"><em>There are no Trustmark Definitions</em></td>
                        </tr>
                    </g:else>
                </tbody>
            </table>
            <div class="row" style="margin-bottom: 2em;">
                <div class="col-md-6">
                    <a href="#" id="uploadTdButton" class="btn btn-primary">Update TD(s)</a>
                </div><div class="col-md-6" style="text-align: right">
                    <g:paginate total="${trustmarkDefinitionsCount}" />
                </div>
            </div>

        </div>

        <script type="text/javascript">
            function enableTd(id){
                console.log("Click to enable td: "+id);
                if( confirm("Do you really want to enable this Trustmark Definition?") ){
                    console.log("Sending async request to enable TD #"+id);

                    $.ajax({
                        url: '${createLink(controller:'trustmarkDefinition', action:'enable')}?id='+id,
                        success: function(data, textStatus, jqXHR){
                            location.reload();
                        },
                        error: function(jqXHR, textStatus, errorThrown){
                            alert("Error enabling TD: "+textStatus);
                        }
                    })

                }
            }
            function disableTd(id){
                console.log("Click to disable td: "+id);
                if( confirm("Do you really want to disable this Trustmark Definition?") ){
                    console.log("Sending async request to disable TD #"+id);

                    $.ajax({
                        url: '${createLink(controller:'trustmarkDefinition', action:'disable')}?id='+id,
                        success: function(data, textStatus, jqXHR){
                            location.reload();
                        },
                        error: function(jqXHR, textStatus, errorThrown){
                            alert("Error disabling TD: "+textStatus);
                        }
                    })
                }
            }

        </script>

	</body>
</html>

<%@ page import="nstic.web.Role" %>
<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main"/>
		<title>Trust Interoperability Profiles</title>

        <style type="text/css">

        </style>
	</head>
	<body>

        <h1>Trust Interoperability Profiles <small>(${tipsCount} total)</small></h1>
        <div class="pageSubsection">
            This page allows you to view all Trust Interoperability Profiles in the system.  Note that Trust Interoperability Profiles
            are loaded from <i>other</i> places.  To add them, you need to add a fully compliant Trust Interoperability Profiles
            hosting server URL in the configuration.  You can force this page to recheck for TIPs (a very time consuming
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
                    <g:if test="${tips && tips.size() > 0}">
                        <g:each in="${tips}" var="tip">
                            <tr>
                                <td style="text-align: center;">
                                    <g:if test="${tip.enabled}">
                                        <a href="javascript:disableTip(${tip.id})">
                                            <span class="glyphicon glyphicon-ok" title="This TIP IS enabled." style="color: rgb(0, 150, 0);"></span>
                                        </a>
                                    </g:if>
                                    <g:else>
                                        <a href="javascript:enableTip(${tip.id})">
                                            <span class="glyphicon glyphicon-remove" title="This TIP is NOT enabled." style="color: rgb(150, 0, 0);"></span>
                                        </a>
                                    </g:else>
                                </td>
                                <td>
                                    <g:link controller="tip" action="view" id="${tip.id}">
                                        ${tip.name}
                                    </g:link>
                                </td>
                                <td>${tip.tipVersion}</td>
                                <td>${tip.description}</td>
                            </tr>
                        </g:each>
                    </g:if>
                    <g:else>
                        <tr>
                            <td colspan="4"><em>There are no Trust Interoperability Profiles</em></td>
                        </tr>
                    </g:else>
                </tbody>
            </table>
            <div class="row" style="margin-bottom: 2em;">
                <div class="col-md-6">
                    <a href="#" id="uploadTdButton" class="btn btn-primary">Update TIP(s)</a>
                </div><div class="col-md-6" style="text-align: right">
                    <g:paginate total="${tipsCount}" />
                </div>
            </div>

        </div>

        <script type="text/javascript">
            function enableTip(id){
                console.log("Click to enable tip: "+id);
                if( confirm("Do you really want to enable this Trust Interoperability Profile?") ){
                    console.log("Sending async request to enable TIP #"+id);

                    $.ajax({
                        url: '${createLink(controller:'tip', action:'enable')}?id='+id,
                        success: function(data, textStatus, jqXHR){
                            location.reload();
                        },
                        error: function(jqXHR, textStatus, errorThrown){
                            alert("Error enabling TIP: "+textStatus);
                        }
                    })

                }
            }
            function disableTip(id){
                console.log("Click to disable tip: "+id);
                if( confirm("Do you really want to disable this Trust Interoperability Profile?") ){
                    console.log("Sending async request to disable TIP #"+id);

                    $.ajax({
                        url: '${createLink(controller:'tip', action:'disable')}?id='+id,
                        success: function(data, textStatus, jqXHR){
                            location.reload();
                        },
                        error: function(jqXHR, textStatus, errorThrown){
                            alert("Error disabling TIP: "+textStatus);
                        }
                    })
                }
            }

        </script>

	</body>
</html>

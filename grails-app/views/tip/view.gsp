<%@ page import="nstic.web.Role" %>
<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main"/>
		<title>TIP: ${databaseTip.name}</title>

        <style type="text/css">

        </style>
	</head>
	<body>

        <h1>${databaseTip.name}, v${databaseTip.tipVersion} <small>(Cached TIP)</small></h1>
        <div class="pageSubsection">
            ${databaseTip.description}
        </div>


        <div class="pageContent">
            <div class="row" style="margin-bottom: 2em;">
                <div class="col-md-6" style="text-align: center;">
                    <a href="${databaseTip.uri}"
                       class="btn btn-primary" target="_blank" title="Clicking here will open this TIP in a new window at the originally hosted location.">
                        Open Remotely <span class="glyphicon glyphicon-new-window"></span>
                    </a>
                    &nbsp;
                    <a href="${createLink(controller:'tip', action:'downloadCachedSource', id: databaseTip.id)}"
                       class="btn btn-default" title="Clicking here will allow you to view the cached copy locally.">
                        Download Cached Source <span class="glyphicon glyphicon-download-alt"></span>
                    </a>
                    &nbsp;
                    <a href="#"
                       class="btn btn-default disabled"
                       title="Clicking here will allow you to download the difference between the local copy and the remote copy.">
                        View Diff <span class="glyphicon glyphicon-transfer"></span>
                    </a>
                </div>
            </div>

            <div class="row" style="margin-bottom: 1em;">
                <div class="col-md-12">
                    <h4 style="border-bottom: 1px solid #CCC;">Metadata</h4>
                    <table class="table table-striped table-condensed">
                        <tr>
                            <td>Enabled:</td>
                            <td>
                                <g:if test="${databaseTip.enabled}">
                                    <span class="glyphicon glyphicon-ok"></span> You can create assessments against this TIP.
                                </g:if><g:else>
                                    <span class="glyphicon glyphicon-remove"></span> You <b>CANNOT</b> create assessments against this TIP.
                                </g:else>
                            </td>
                        </tr>
                        <tr>
                            <td>Identifier:</td>
                            <td>
                                ${databaseTip.uri}
                            </td>
                        </tr>
                        <tr>
                            <td>Publication Date:</td>
                            <td>
                                <g:formatDate date="${databaseTip.publicationDateTime}" format="yyyy-MM-dd" />
                            </td>
                        </tr>
                        <tr>
                            <td>Cache Date:</td>
                            <td>
                                <g:formatDate date="${databaseTip.lastUpdated}" format="yyyy-MM-dd" />
                            </td>
                        </tr>
                        <tr>
                            <td>TIP Expression:</td>
                            <td>
                                ${databaseTip.tipExpression}
                            </td>
                        </tr>
                    </table>
                </div>
            </div>

            <div class="row" style="margin-bottom: 1em;">
                <div class="col-md-12">
                    <h4 style="border-bottom: 1px solid #CCC;">TIP References (${databaseTip.references.size()})</h4>
                    <table class="table table-striped table-condensed">
                        <g:each in="${databaseTip.sortedTipReferences}" var="ref">
                            <tr>
                                <td>${ref.referenceName}</td>
                                <g:if test="${ref.trustmarkDefinition != null}">
                                    <td>
                                        <a href="${createLink(controller:'trustmarkDefinition', action:'view', id: ref.trustmarkDefinition.id)}">
                                            <span class="glyphicon glyphicon-tag"></span>
                                            ${ref.trustmarkDefinition.name}, v${ref.trustmarkDefinition.tdVersion}
                                        </a>
                                        <a href="${ref.trustmarkDefinition.uri}" target="_blank">
                                            <span class="glyphicon glyphicon-share"></span>
                                        </a>
                                    </td>
                                </g:if><g:elseif test="${ref.trustInteroperabilityProfile != null}">
                                <td>
                                    <a href="${createLink(controller:'tip', action:'view', id: ref.trustInteroperabilityProfile.id)}">
                                        <span class="glyphicon glyphicon-list"></span>
                                        ${ref.trustInteroperabilityProfile.name}, v${ref.trustInteroperabilityProfile.tipVersion}
                                    </a>
                                    <a href="${ref.trustInteroperabilityProfile.uri}" target="_blank">
                                        <span class="glyphicon glyphicon-share"></span>
                                    </a>
                                </td>
                            </g:elseif><g:else>
                                <td>
                                    <em>Both TD and TIP are null!</em>
                                </td>
                            </g:else>
                            </tr>
                        </g:each>
                    </table>
                </div>
            </div>



        </div>



        <script type="text/javascript">


        </script>

	</body>
</html>

<%@ page import="nstic.web.Role" %>
<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main"/>
		<title>Trustmark Definition ${databaseTd.name}</title>

        <style type="text/css">

        </style>
	</head>
	<body>

        <h1>${databaseTd.name}, v${databaseTd.tdVersion} <small>(Cached TD)</small></h1>
        <div class="pageSubsection">
            ${databaseTd.description}
        </div>


        <div class="pageContent">
            <div class="row" style="margin-bottom: 2em;">
                <div class="col-md-6" style="text-align: center;">
                    <a href="${databaseTd.uri}"
                       class="btn btn-primary" target="_blank" title="Clicking here will open this TD in a new window at the originally hosted location.">
                        Open Remotely <span class="glyphicon glyphicon-new-window"></span>
                    </a>
                    &nbsp;
                    <a href="${createLink(controller:'trustmarkDefinition', action:'downloadCachedSource', id: databaseTd.id)}"
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
                            <td>Enabled:</td><td>
                                <g:if test="${databaseTd.enabled}">
                                    <span class="glyphicon glyphicon-ok"></span> You can create assessments against this TD.
                                </g:if><g:else>
                                    <span class="glyphicon glyphicon-remove"></span> You <b>CANNOT</b> create assessments against this TD.
                                </g:else>
                            </td>
                        </tr>
                        <tr>
                            <td>Identifier:</td><td>
                                ${databaseTd.uri}
                            </td>
                        </tr>
                        <tr>
                            <td>Publication Date:</td><td>
                                <g:formatDate date="${databaseTd.publicationDateTime}" format="yyyy-MM-dd" />
                            </td>
                        </tr>
                        <tr>
                            <td>Cache Date:</td><td>
                                <g:formatDate date="${databaseTd.lastUpdated}" format="yyyy-MM-dd" />
                            </td>
                        </tr>
                    </table>
                </div>
            </div>

            <div class="row">
                <div class="col-md-6">
                    <h4 style="border-bottom: 1px solid #CCC;">Conformance Criteria (${databaseTd.criteria.size()})</h4>
                    <g:each in="${databaseTd.criteria}" var="crit">
                        <div style="margin-bottom: 1em;">
                            <div style="font-size: 105%; font-weight: bold">
                                #${crit.critNumber}: ${crit.name}
                            </div>
                            <div style="margin-left: 0.5em; font-size: 90%; color: #555;">
                                ${crit.description}
                            </div>
                        </div>
                    </g:each>
                </div>

                <div class="col-md-6">
                    <h4 style="border-bottom: 1px solid #CCC;">Assessment Steps (${databaseTd.assessmentSteps.size()})</h4>
                    <g:each in="${databaseTd.sortedSteps}" var="step">
                        <div style="margin-bottom: 1em;">
                            <div style="font-size: 105%; font-weight: bold">
                                #${step.stepNumber}: ${step.name}
                            </div>
                            <div style="margin-left: 0.5em; font-size: 90%; color: #555;">
                                ${step.description}
                            </div>
                            <g:if test="${step.artifacts?.size() > 0}">
                                <div style="margin-left: 0.5em;">
                                    <div>Artifacts (${step.artifacts.size()})</div>
                                    <g:each in="${step.artifacts}" var="artifact">
                                        <div style="margin-left: 0.5em; font-size: 90%; color: #555;">
                                            <span style="color: black; font-weight: bold;">${artifact.name}</span>: ${artifact.description}
                                        </div>
                                    </g:each>
                                </div>
                            </g:if>
                        </div>
                    </g:each>
                </div>
            </div>

        </div>

        <script type="text/javascript">


        </script>

	</body>
</html>

<%@ page import="groovy.json.JsonBuilder; grails.converters.JSON; nstic.web.assessment.Assessment; nstic.web.assessment.AssessmentStepData; nstic.web.assessment.AssessmentStatus; org.apache.commons.io.FileUtils; nstic.web.assessment.Trustmark" defaultCodec="none" %>
<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main"/>
		<title>View Assessment</title>

        <style type="text/css">
            h5 {
                font-weight: bold;
                font-size: 105%;
            }

            .assessorCommentContainer {
                margin-bottom: 1em;
            }

            #assessmentInformationContainer {

            }
            #assessmentInformationContainer th {
                vertical-align: top;
                width: 150px;
            }
            #assessmentInformationContainer td {
                vertical-align: top;
                width: auto;
            }


            ul.assStepRequiredArtifactsList {
                list-style: none;
                margin: 0;
                padding: 0;
            }

            .requiredArtifactName {
                font-style: italic;
            }
            .requiredArtifactDesc {
                margin-left: 1em;
            }

            #tdSearch {
                background-position: 10px 12px; /* Position the search icon */
                background-repeat: no-repeat;   /* Do not repeat the icon image */
                width: 100%;                    /* Full-width */
                font-size: 16px;                /* Increase font-size */
                padding: 12px 20px 12px 40px;   /* Add some padding */
                border: 1px solid #ddd;         /* Add a grey border */
                margin-bottom: 12px;            /* Add some space below the input */
            }

            .form-control {
                padding-right: 30px;
            }

            .form-control + .glyphicon {
                position: absolute;
                left: 0;
                padding: 8px 27px;
            }
        </style>

        <script type="text/javascript" src="https://www.gstatic.com/charts/loader.js"></script>
        <script type="text/javascript">
            google.charts.load('current', {'packages':['corechart']});
            google.charts.setOnLoadCallback(drawChart);

            function drawChart() {
                var chartData = <%= (new JsonBuilder(chartData)).toString() %>;
                console.log("chartData: ", chartData);

                var data = new google.visualization.DataTable();
                data.addColumn('string', 'Status');
                data.addColumn('number', 'Percentage');
                data.addColumn({type: 'string', role: 'style'}); // Adding color as a role

                var rows = [];
                <% chartData.each { item ->
                    out << "rows.push(['${item[0]}', ${item[1]}, '${item[2]}']);\n"
                } %>

                data.addRows(rows);

                var options = {
                    width: 600,
                    height: 200,
                    chartArea: {
                        top     : "5%"
                    },
                    is3D: true
                };

                var chart = new google.visualization.PieChart(document.getElementById('stepchart'));
                chart.draw(data, options);
            }

        </script>
	</head>
	<body>
        <div class="row">
            <div class="col-md-12">
                <a href="#metadata">Metadata</a> |
                <a href="#trustmarks">Trustmarks</a> |
                <a href="#assessmentLog">Log</a> |
                <a href="#assessmentSteps">Steps</a>
            </div>
        </div>
        <%
            def assessment = (Assessment)assessment
        %>
        <div class="row">
            <div class="col-md-7">
                <h1>
                    <tmpl:/templates/displayAssessmentStatusIcon assessment="${assessment}" />
                    ${assessment.assessmentName}
                    <sup>
                        <a href="${createLink(action: 'editAssessmentName', id: assessment.id)}" class="btn btn-info btn-xs">
                            <span class="glyphicon glyphicon-pencil"></span>
                            Edit Name
                        </a>
                    </sup>
                </h1>
                <div style="margin: 0;">
                    <table class="table">
                        <tr>
                            <td style="width: 175px;"><b>Organization</b>:</td><td>${assessment.assessedOrganization.name}</td>
                        </tr>
                    </table>
                </div>
            </div>
            <div class="col-md-5" style="text-align: right;">
                <div class="buttonContainer">
                    <g:link controller="assessmentPerform" action="startAssessment" id="${assessment.id}" class="btn btn-primary">
                        Perform Assessment
                    </g:link>
                    <g:link controller="assessmentPerform" action="importAssessmentResults" id="${assessment.id}" class="btn btn-primary confirmLink">
                        Import Assessment Results
                    </g:link>

%{--                This is the popup ci=onfirmation dialog for the automated assessment task--}%
                    <div id="dialog" title="Confirmation Required">
                        Please take caution when using this feature.  The uploaded artifact should be an audit
                        letter or report that shows the organization has undergone a thorough audit that covers
                        the same criteria for which this assessment covers.  The assessor should have very high
                        confidence that this existing audit event has thoroughly verified conformance to this TIP.
                        <br>
                        The assessor is still required to provide any parameters required by this assessment.
                    </div>

                    <g:link controller="assessment" action="delete" id="${assessment.id}" class="btn btn-danger" onclick="return confirm('Really delete?  There is no undoing this operation.');">
                        Delete
                    </g:link>
                </div>

                <div class="statusContainer" style="text-align: right;" id="assessmentStatusSummaryContainer">
                    <asset:image src="spinner.gif"/> Loading status summary...
                </div>
            </div>
        </div>

        <g:if test="${flash.error}">
            <div class="alert alert-danger">${flash.error}</div>
        </g:if>
        <g:if test="${flash.message}">
            <div class="alert alert-success">${flash.message}</div>
        </g:if>

        <div style="margin-top: 25px; margin-bottom: 2px; padding-bottom: 0; text-align: center; font-weight: bold">Assessment Step Status (${statistics.totalStepCount} Steps)</div>
        <div id="stepchart" style="width: 100%; padding-top: 0; display: flex; justify-content: center; align-items: center"></div>

        <div class="pageContent">

            <!-- Metadata -->
            <a name="metadata"></a>
            <h3>Assessment Information <small>Status, What are we assessing, and Who is it for?</small></h3>
            <div class="row" style="margin-top: 0.5em;" id="assessmentInformationContainer">
                <div class="col-md-6">
                    <h5>Status</h5>
                    <table class="table-condensed table-striped table-bordered" style="width: 100%;">
                        <tr>
                            <th>Status</th>
                            <td>
                                ${assessment.status}
                            </td>
                        </tr>
                        <tr>
                            <th>Date Created</th>
                            <td>
                                <g:formatDate date="${assessment.dateCreated}" format="yyyy-MM-dd" />
                            </td>
                        </tr>
                        <tr>
                            <th>Created By</th>
                            <td>
                                ${assessment.createdBy.contactInformation.responder}
                            </td>
                        </tr>
                        <tr>
                            <th>Assigned To</th>
                            <td>
                                <g:if test="${assessment.assignedTo}">
                                    ${assessment.assignedTo.contactInformation.responder}
                                </g:if><g:else>
                                    <em>Nobody</em>
                                </g:else>
                            </td>
                        </tr>
                        <tr>
                            <th>Most Recent Entry</th>
                            <td>

                                <g:if test="${logEntries?.isEmpty()}">
                                    <i>None</i>
                                </g:if>
                                <g:else>
                                    <small>
                                        <g:formatDate date="${logEntries.get(0).dateCreated}" format="yyyy-MM-dd" />
                                    </small>

                                    ${logEntries.get(0).title}
                                </g:else>

                            </td>
                        </tr>
                        <tr>
                            <th>Comment</th>
                            <td>
                                <g:if test="${assessment && assessment.comment && assessment.comment.trim().length() > 0}">
                                    ${assessment.comment}
                                </g:if><g:else>
                                    <em>No Comment</em>
                                </g:else>
                            </td>
                        </tr>
                    </table>
                </div>
                <div class="col-md-6">

                    <h5>Organization Contact</h5>
                    <table class="table-condensed table-striped table-bordered" style="width: 100%;">
                        <tr>
                            <th>Email</th><td>${assessment.assessedContact.email}</td>
                        </tr><tr>
                            <th>Name</th><td>${assessment.assessedContact.responder}</td>
                        </tr><tr>
                            <th>Phone</th><td>${assessment.assessedContact.phoneNumber}</td>
                        </tr><tr>
                            <th>Mailing Address</th><td>${assessment.assessedContact.mailingAddress}</td>
                        </tr><tr>
                            <th>Notes</th><td>${assessment.assessedContact.notes}</td>
                        </tr>
                    </table>
                    <div style="margin-top: 1em;">
                        <g:link controller="contactInformation" action="edit" params="[contactId: assessment.assessedContact.id]" class="btn btn-default">
                            Edit
                        </g:link>
                    </div>
                </div>
            </div>
            <div class="row" style="margin-top: 1em;">
                <div class="col-md-12">
                    <h5>Trustmark Definitions (${assessment.tdLinks.size()})</h5>
                    <div class="text-muted" style="font-size: 90%;">
                        This contains the set of Trustmark Definitions which are being assessed.
                    </div>
                    <div style="max-height: 20em; overflow-y: scroll;">
                        <table class="table-condensed table-striped table-bordered" style="width: 100%;">
                            <g:each in="${assessment.sortedTdsByName}" var="tdLink">
                                <tr>
                                    <td>
                                        <div>
                                            <a href="${tdLink.trustmarkDefinition.uri}" target="_blank">${tdLink.trustmarkDefinition.name}</a>,
                                            version ${tdLink.trustmarkDefinition.tdVersion}
                                        </div>
                                        <div class="text-muted" style="margin-left: 1em; font-size: 80%;">
                                            ${tdLink.trustmarkDefinition.description}
                                        </div>
                                    </td>
                                </tr>
                            </g:each>
                        </table>
                    </div>
                </div>
            </div>


            <a name="trustmarks"></a>
            <h3 style="margin-top: 2em;">Trustmarks (${trustmarks ? trustmarks.size() : 0})</h3>
            <div class="row">
                <div class="col-md-12">
                    <g:if test="${trustmarks && !trustmarks.isEmpty()}">
                        <div>
                            <input class="form-control" autocomplete="off" type="search" id="tdSearch" onkeyup="searchForTD()" placeholder="Search for Trustmark Definition...">
                            <span class="glyphicon glyphicon-search"></span>
                        </div>
                    </g:if>
                    <div id="assessment-trustmarks"style="max-height: 20em; overflow-y: scroll;">
                    </div>
                    <div style="margin-top: 0.5em;">
                        <g:if test="${!assessment.getIsComplete() || assessment.status == AssessmentStatus.ABORTED}">
                            <span id="grant-tooltip" class="d-inline-block" data-toggle="tooltip" title="To grant trustmarks the assessment must be completed and marked as Success.">
                                <a href="#" class="btn btn-primary disabled" style="pointer-events: none;">Grant</a>
                            </span>
                        </g:if>
                        <g:else>
                            <a href="${createLink(controller: 'trustmark', action: 'create', params:[assessmentId: assessment.id])}" class="btn btn-primary">Grant</a>
                        </g:else>
                            <g:if test="${trustmarks && !trustmarks.isEmpty()}">
                                <a href="javascript:revokeAllTrustmarks();" class="btn btn-default" title="Revoke all trustmarks issued in this assessment.">Revoke All</a>
                                <span id="revokeAllTrustmarksStatusMessage" />
                            </g:if>
                    </div>
                    <span id="trustmarksStatusMessage" />
                </div>
            </div>

            <!-- Log -->
            <a name="assessmentLog"></a>
            <h3 style="margin-top: 2em;">Assessment Log <small>Who's doing what?</small></h3>
            <div class="row">
                <div class="col-md-12">

                    <div id="assessment-log-entries" style="max-height: 20em; overflow-y: scroll;"></div>

                    <div style="margin-top: 0.5em;">
                        <a href="${createLink(controller:'assessmentLog', action: 'viewLog', id: assessment.id)}" class="btn btn-default">View All</a>
                    </div>

                    <span id="assessment-log-entries-status-message" />

                </div>
            </div>


            <!-- Assessment Steps -->
            <a name="assessmentSteps"></a>
            <h3 style="margin-top: 2em;">Assessment Steps <small>(${assessmentSteps.size()})</small></h3>
            <hr />
            <div class="row">
                <div class="col-md-12">

                    <g:each in="${((ArrayList<AssessmentStepData>)assessmentSteps)}" var="assStepData" status="assStepIndex">
                        <div class="stepContainer">

                            <div class="stepHeader">
                                <h4 class="step-title">
%{--                                    <assess:assessmentStepResult result="${assStepData.result}" />--}%
                                    <assess:assessmentStepResponseResult result="${assStepData.result.result}" description="${assStepData.result.description}"/>

                                    <g:if test="${stepDataArtifactStatus.get(assStepData)?.distinctRequiredArtifactsCount > 0}">
                                        <span>
                                            <span class="glyphicon glyphicon-paperclip" title="${stepDataArtifactStatus.get(assStepData)?.allSatisfied ? 'Required Attachments Satisfied' : 'Required Attachments Not Satisfied'}">
                                                <g:if test="${stepDataArtifactStatus.get(assStepData)?.allSatisfied}">
                                                    <span class="glyphicon glyphicon-ok-sign" style="color: darkgreen; left: -21px; font-size: 10px;"></span>
                                                </g:if><g:else>
                                                <span class="glyphicon glyphicon-remove-sign" style="color: darkred; left: -21px; font-size: 10px;"></span>
                                            </g:else>
                                            </span>
                                        </span>
                                    </g:if>

                                    ${assStepData.step.name}
                                </h4>
                            </div>
                            <div id="collapseAssStep${assStepIndex}" class="assStepBody">
                                <div class="assStepDescriptionContainer">
                                    ${assStepData.step.description}
                                </div>
                                <g:if test="${assStepData.step.artifacts && assStepData.step.artifacts.size() > 0}">
                                    <div>
                                        <h5>Required Artifacts (${assStepData.step.artifacts.size()})</h5>
                                        <ul class="assStepRequiredArtifactsList">
                                            <g:each in="${assStepData.step.artifacts.sort{it.name}}" var="artifactDef">
                                                <li>
                                                    <div class="requiredArtifactName">
                                                        <assess:assessmentStepSingleArtifactStatus step="${assStepData}" artifact="${artifactDef}" />
                                                        ${artifactDef.name}
                                                    </div>
                                                    <div class="requiredArtifactDesc">${artifactDef.description}</div>
                                                </li>
                                            </g:each>
                                        </ul>
                                    </div>
                                </g:if>
                                <g:if test="${assStepData.step.parameters && assStepData.step.parameters.count{it.required} > 0}">
                                    <div>
                                        <h5>Required Parameters (${assStepData.step.parameters.count{it.required}})</h5>
                                        <ul class="assStepRequiredArtifactsList">
                                            <g:each in="${assStepData.step.parameters.findAll{it.required}.sort{it.name}}" var="tdParam">
                                                <li>
                                                    <div class="requiredArtifactName">
                                                        <assess:assessmentStepSingleParameterStatus step="${assStepData}" parameter="${tdParam}" />
                                                        ${tdParam.name}
                                                    </div>
                                                    <div class="requiredArtifactDesc">${tdParam.description}</div>
                                                </li>
                                            </g:each>
                                        </ul>
                                    </div>
                                </g:if>

                                <h5>Assessor Findings</h5>
                                <div>
                                    <assess:assessmentStepResponseResult result="${assStepData.result.result}" description="${assStepData.result.description}"/>
                                    <assess:assessmentStepResponseTextOnly result="${assStepData.result.result}" name="${assStepData.result.name}" description="${assStepData.result.description}" />
                                </div>
                                <div class="assessorCommentContainer">
                                    <div>
                                        Comment:
                                        <g:if test="${assStepData.assessorComment && assStepData.assessorComment?.trim().length() > 0}">
                                            ${assStepData.assessorComment}
                                        </g:if><g:else>
                                            <em>No Comment.  Only marked result as '${assStepData.result.result ?: nstic.web.assessment.AssessmentStepResult.Not_Known}'</em>
                                        </g:else>
                                    </div>
                                </div>
                                <g:if test="${assStepData.artifacts && assStepData.artifacts.size() > 0}">
                                    <div class="artifactsContainer">
                                        <div>Artifacts (${assStepData.artifacts?.size() ?: 0})</div>
                                        <g:each in="${assStepData.artifacts.sort{it.requiredArtifact?.name}}" var="artifact">
                                            <div class="artifactContainer">
                                                <assess:renderArtifactSummary artifact="${artifact}" />
                                            </div>
                                        </g:each>
                                    </div>
                                </g:if>
                                <g:if test="${assStepData.parameterValues && !assStepData.parameterValues.isEmpty()}">
                                    <div class="artifactsContainer">
                                        <div>Parameter Values (${assStepData.parameterValues?.size() ?: 0})</div>
                                        <g:each in="${assStepData.parameterValues.sort{it.parameter.name}}" var="paramValue">
                                            <div class="artifactContainer">
                                                <assess:renderParameterSummary paramValue="${paramValue}" />
                                            </div>
                                        </g:each>
                                    </div>
                                </g:if>
                            </div>
                        </div>
                        <hr />
                    </g:each>

                </div>
            </div>

        </div>

    <script>

        $(document).ready(function()  {
            var url = '${createLink(controller:'assessment', action: 'getAssessmentStatusSummary', id: assessment.id)}';
            console.log('Getting contents of: '+url);
            $.get(url, function(data){
                $('#assessmentStatusSummaryContainer').html(data);
            });

            $("#dialog").dialog({
                autoOpen: false,
                modal: true
            });

            getTrustmarks(${assessment.id});

            getAssessmentLogEntries(${assessment.id});

            $('#grant-tooltip').tooltip();
        });

        $(".confirmLink").click(function(e) {
            e.preventDefault();
            var targetUrl = $(this).attr("href");

            $("#dialog").dialog({
                buttons : {
                    "Confirm" : function() {
                        window.location.href = targetUrl;
                    },
                    "Cancel" : function() {
                        $(this).dialog("close");
                    }
                }
            });

            $("#dialog").dialog("open");
        });

        let getTrustmarks = function(assessmentid) {
            $('#trustmarksStatusMessage').html('<asset:image src="spinner.gif" /> Status: Loading trustmarks...');
            list("${createLink(controller:'assessment', action: 'listTrustmarks')}"
                , trustmarkResults
                , { id: assessmentid }
            );
        }

        let trustmarkResults = function(results)  {
            renderTrustmarks('assessment-trustmarks', results);
            $('#trustmarksStatusMessage').html("");
        }

        let renderTrustmarks = function(target, data)  {

            let html = "<table id='trustmarks-table' class='table table-striped table-bordered table-condensed'>";

            // table header
            html += "<thead><tr style='white-space: nowrap;'><th>Id</th><th>Status</th><th>Issue Date</th><th>Expiration Date</th><th>Trustmark Definition</th></tr></thead>";

            if (data.records.length === 0)  {
                html += '<tr><td colspan="5"><em>There are no trustmarks.</em></td></tr>';
            }  else {

                html += "<tbody>";

                data.records.forEach(tm => {
                    html += drawTrustmark(tm, data.trustmarkViewBaseUrl);
                });

                html += "</tbody>";
            }
            html += "</table>";
            document.getElementById(target).innerHTML = html;
        }

        let drawTrustmark = function(entry, trustmarkViewBaseUrl)  {

            let html = "<tr>";

            html += "<td><a href='" + trustmarkViewBaseUrl + "/" + entry.id + "'>" + entry.id + "</a></td>";
            html += "<td>" + entry.status + "</td>";
            html += "<td style='white-space: nowrap;'>" + formatDate(new Date(entry.issueDateTime)) + "</td>";
            html += "<td style='white-space: nowrap;'>" + formatDate(new Date(entry.expirationDateTime)) + "</td>";
            html += "<td>" + entry.trustmarkDefinition.name + "</td>";

            html += "</tr>";

            return html;
        }

        // assessment lof entries
        let getAssessmentLogEntries = function(assessmentid) {
            $('#assessment-log-entries-status-message').html('<asset:image src="spinner.gif" /> Status: Loading assessment log entries...');
            list("${createLink(controller:'assessment', action: 'listAssessmentLogEntries')}"
                , assessmentLogEntriesResults
                , { id: assessmentid }
            );
        }

        let assessmentLogEntriesResults = function(results)  {
            renderAssessmentLogEntries('assessment-log-entries', results);
            $('#assessment-log-entries-status-message').html("");
        }

        let renderAssessmentLogEntries = function(target, data)  {

            let html = "<div style='margin-left: 1em; color: #555;'>Displaying " + data.records.length + " of " + data.logEntryCount + " entries.</div>";

            html += "<table  class='table table-striped table-bordered table-condensed'>";

            // table header
            html += "<thead><tr style='white-space: nowrap;'><th>ID</th><th>Type</th><th>Created</th><th>Title</th></tr></thead>";

            if (data.records.length === 0)  {
                html += '<tr><td colspan="4"><em>There are no assessment log entries.</em></td></tr>';
            }  else {

                html += "<tbody>";

                data.records.forEach(logEntry => {
                    html += drawAssessmentLogEntry(logEntry, data.assessmentLogEntryViewBaseUrl);
                });

                html += "</tbody>";
            }
            html += "</table>";
            document.getElementById(target).innerHTML = html;
        }

        let drawAssessmentLogEntry = function(entry, assessmentLogEntryViewBaseUrl)  {

            let html = "<tr>";

            html += "<td><a target='_blank' href='" + assessmentLogEntryViewBaseUrl + "/?entryId=" + entry.id + "'>" + entry.id + "</a></td>";
            html += "<td>" + entry.type + "</td>";
            html += "<td style='white-space: nowrap;'>" + formatDate(new Date(entry.dateCreated)) + "</td>";
            html += "<td style='white-space: nowrap;'>" + entry.title + "</td>";

            html += "</tr>";

            return html;
        }

        // Revoke all trustmarks that have been signed with this certificate
        function revokeAllTrustmarks(){

            if( !confirm("Are you sure you want to revoke all trustmarks? This operation cannot be reversed.") ){
                return;
            }

            var reason = prompt("What is the reason you are revoking all trustmarks?");
            if( !reason) {
                alert("A reason is required.");
            } else {
                $.ajax({
                    url: '${createLink(controller: 'assessment', action: 'revokeAllTrustmarksIssuedforAssessment', id: assessment.id)}',
                    type: 'POST',
                    data: {
                        format: 'json',
                        reason: reason
                    },
                    beforeSend: function () {
                        $('#revokeAllTrustmarksStatusMessage').html('<asset:image src="spinner.gif" /> Status: Revoking all trustmarks...');
                    },
                    success: function (data, statusText, jqXHR) {

                        // window.location.reload();
                        getTrustmarks(${assessment.id});

                        getAssessmentLogEntries(${assessment.id});

                        $('#revokeAllTrustmarksStatusMessage').html("Status: Revoked all trustmarks!");
                    },
                    error: function (jqXHR, statusText, errorThrown) {
                        console.log("Error: " + errorThrown);

                        $('#revokeAllTrustmarksStatusMessage').html(errorThrown);
                    }
                });
            }
        }

        function searchForTD() {
            // Declare variables
            var input, filter, table, tr, td, i, txtValue;
            input = document.getElementById("tdSearch");
            filter = input.value.toUpperCase();
            table = document.getElementById("trustmarks-table");
            tr = table.getElementsByTagName("tr");

            // Loop through all table rows, and hide those who don't match the search query
            for (i = 0; i < tr.length; i++) {
                // get the td at the trustmark definition's index
                td = tr[i].getElementsByTagName("td")[4];
                if (td) {
                    txtValue = td.textContent || td.innerText;

                    if (txtValue.toUpperCase().indexOf(filter) > -1) {
                        tr[i].style.display = "";
                    } else {
                        tr[i].style.display = "none";
                    }
                }
            }
        }

        let formatDate = function(aDate) {
            var aDate = new Date(aDate);
            var year = aDate.getFullYear();
            var month = aDate.getMonth() + 1;
            if (month < 10) {
                month = "0" + month;
            }
            var day = aDate.getDate();

            return  year + "-" + month + "-" + day;
        }
    </script>
	</body>
</html>

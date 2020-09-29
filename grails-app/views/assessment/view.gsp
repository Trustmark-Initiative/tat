<%@ page import="nstic.web.assessment.Assessment; nstic.web.assessment.AssessmentStepData; nstic.web.assessment.AssessmentStatus; org.apache.commons.io.FileUtils; nstic.web.assessment.Trustmark" defaultCodec="none" %>
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
        </style>
	</head>
	<body>
        %{--<ol class="breadcrumb">--}%
            %{--<li class="active">Home</li>--}%
        %{--</ol>--}%

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
                    <g:link controller="assessment" action="delete" id="${assessment.id}" class="btn btn-danger" onclick="return confirm('Really delete?  There is no undoing this operation.');">
                        Delete
                    </g:link>
                </div>

                <div class="statusContainer" style="text-align: right;" id="assessmentStatusSummaryContainer">
                    <asset:image src="spinner.gif"/> Loading status summary...
                </div>
                <script type="text/javascript">
                    $(document).ready(function(){
                        var url = '${createLink(controller:'assessment', action: 'getAssessmentStatusSummary', id: assessment.id)}';
                        console.log('Getting contents of: '+url);
                        $.get(url, function(data){
                            $('#assessmentStatusSummaryContainer').html(data);
                        });
                    });
                </script>
            </div>
        </div>

        <g:if test="${flash.error}">
            <div class="alert alert-danger">${flash.error}</div>
        </g:if>
        <g:if test="${flash.message}">
            <div class="alert alert-success">${flash.message}</div>
        </g:if>

        <img src="${charts['stepChart'].toURLForHTML()}" />

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
                    <div style="max-height: 20em; overflow-y: scroll;">
                        <table class="table-condensed table-striped table-bordered" style="width: 100%;">
                            <tr>
                                <th>Id</th>
                                <th>Status</th>
                                <th>Issue Date</th>
                                <th>Expiration Date</th>
                                <th>Identifier</th>
                            </tr>
                            <g:if test="${trustmarks && !trustmarks.isEmpty()}">
                                <g:each in="${trustmarks}" var="tm">
                                    <tr>
                                        <td>
                                            <g:link controller="trustmark" action="view" id="${tm.id}">
                                                ${tm.id}
                                            </g:link>
                                        </td>
                                        <td>
                                            ${tm.status}
                                        </td>
                                        <td>
                                            <g:formatDate format="yyyy-MM-dd" date="${tm.issueDateTime}" />
                                        </td>
                                        <td>
                                            <g:formatDate format="yyyy-MM-dd" date="${tm.expirationDateTime}" />
                                        </td>
                                        <td>
                                            ${tm.identifier}
                                        </td>
                                    </tr>
                                </g:each>
                            </g:if><g:else>
                                <tr>
                                    <td colspan="5">
                                        <em>No trustmarks have been created yet.</em>
                                    </td>
                                </tr>
                            </g:else>
                        </table>
                    </div>
                    <div style="margin-top: 0.5em;">
                        <a href="${createLink(controller: 'trustmark', action: 'create', params:[assessmentId: assessment.id])}" class="btn btn-primary">Grant</a>
                        <g:if test="${assessment.isComplete}">
                            <g:if test="${trustmarks && !trustmarks.isEmpty()}">
                                <a href="#" class="btn btn-default" title="Bulk Revoke capability is not yet implemented.">Revoke</a>
                            </g:if>
                        </g:if>
                    </div>
                </div>
            </div>


            <!-- Log -->
            <a name="assessmentLog"></a>
            <h3 style="margin-top: 2em;">Assessment Log <small>Who's doing what?</small></h3>
            <div class="row">
                <div class="col-md-12">
                    <div style="margin-left: 1em; color: #555;">
                        Displaying ${logEntries.size()} of ${logEntryCount} entries.
                    </div>

                    <table class="table table-striped table-bordered table-condensed">
                        <thead>
                            <th>ID</th>
                            <th>Type</th>
                            <th>Created</th>
                            <th>Title</th>
                        </thead>
                        <tbody>
                            <g:each in="${logEntries}" var="entry">
                                <tr>
                                    <td>
                                        <a target="_blank" href="${createLink(controller:'assessmentLog', action: 'viewLogEntry', id: assessment.id, params: [entryId: entry.id])}">
                                            #${entry.id}
                                        </a>
                                    </td>
                                    <td>
                                        ${entry.type}
                                    </td>
                                    <td>
                                        <g:formatDate date="${entry.dateCreated}" format="yyyy-MM-dd HH:mm" />
                                    </td>
                                    <td>
                                        ${entry.title}
                                    </td>
                                </tr>
                            </g:each>
                        </tbody>
                    </table>

                    <div style="margin-top: 0.5em;">
                        <a href="${createLink(controller:'assessmentLog', action: 'viewLog', id: assessment.id)}" class="btn btn-default">View All</a>
                    </div>

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
                                    <assess:assessmentStepResult result="${assStepData.result}" />
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
                                    <assess:assessmentStepResult result="${assStepData.result}" />
                                    <assess:assessmentStepResultTextOnly result="${assStepData.result}" />
                                </div>
                                <div class="assessorCommentContainer">
                                    <div>
                                        Comment:
                                        <g:if test="${assStepData.assessorComment && assStepData.assessorComment?.trim().length() > 0}">
                                            ${assStepData.assessorComment}
                                        </g:if><g:else>
                                            <em>No Comment.  Only marked result as '${assStepData.result ?: nstic.web.assessment.AssessmentStepResult.Not_Known}'</em>
                                        </g:else>
                                    </div>
                                </div>
                                <g:if test="${assStepData.artifacts && assStepData.artifacts.size() > 0}">
                                    <div class="artifactsContainer">
                                        <div>Artifacts (${assStepData.artifacts?.size() ?: 0})</div>
                                        <g:each in="${assStepData.artifacts.sort{it.requiredArtifact.name}}" var="artifact">
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


	</body>
</html>

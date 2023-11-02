<%@ page import="nstic.web.assessment.AssessmentSubStepData; nstic.web.td.AssessmentStepArtifact; nstic.web.assessment.AssessmentStatus; org.apache.commons.io.FileUtils; nstic.web.assessment.AssessmentStepResult" defaultCodec="none" %>
<%@ page import="nstic.web.assessment.ParameterValue" %>
<%@ page import="edu.gatech.gtri.trustmark.v1_0.model.ParameterKind" %>
<%@ page import="edu.gatech.gtri.trustmark.v1_0.impl.model.TrustmarkParameterBindingImpl" %>

<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>Perform Assessment for ${assessment.assessmentName}</title>

    <style type="text/css">
    .assStepNavList {
        /*padding: 0;*/
        /*margin: 0;*/
        /*list-style: none;*/
    }

    #assStepNavList > li.active > a, #assStepNavList > li.active > a:hover, #assStepNavList > li.active > a:focus {
        background-color: #dfd1ba;
        color: #555;
    }

    .assStepNavList a {
        color: #555;
    }

    .assStepNavList a:HOVER {
        color: #555;
    }

    .artifactDisplayTable {
        width: 100%;
        table-layout: fixed;
        border-collapse: collapse;
        border: 1px solid #ddd;
    }

    .artifactDisplayTable tr.odd {
        background-color: #ddd;
    }

    .artifactDisplayTable td {
        padding: 5px;
    }

    .artifactDisplayTable td.actionsCell {
        width: 70px;
        text-align: center;
    }

    .artifactDisplayTable td.uploadingUserCell {
        width: 100px;
    }

    .artifactDisplayTable td.filesizeCell {
        width: 75px;
    }

    .artifactDisplayTable td.filenameCell {
        width: auto;
    }

        .stepArtifactName{
            font-weight: bold;
        }

        .stepArtifactDesc {
            margin-left: 1.5em;
            font-size: 90%;
        }

    .parameter-values {
        padding-top: 5px;
    }

    [data-param-kind] .radio {
        margin-top: 0;
    }
    </style>
</head>

<body>

<ol class="breadcrumb">
    <li>
        <g:link controller="home" action="index">Home</g:link>
    </li>
    <li>
        <g:link controller="assessment" action="list">Assessments</g:link>
    </li>
    <li class="active">
        Perform Assessment for ${assessment.assessmentName} Step ${currentStepData.step.stepNumber}
    </li>
</ol>

<div class="row">
        <dialog id="dlgCriteria">
            <div id="divCriteria"></div>
        </dialog>
    <div class="col-md-9">
        <h1>Perform Assessment for ${assessment.assessmentName}
            <g:if test="${grails.util.Environment.current == grails.util.Environment.DEVELOPMENT}">
                <small>
                    <a href="javascript:autoFill();">(Auto Fill)</a>
                </small>
            </g:if>
        </h1>

        <div class="pageSubsection">
            This page allows you to perform the assessment of <a
            href="${createLink(controller: 'assessment', action: 'view', id: assessment.id)}">assessment ${assessment.id}</a>,
            for organization ${assessment.assessedOrganization?.identifier ?: assessment.assessedOrganization?.name}
        </div>
    </div>

    <div class="col-md-3" style="text-align: right;" id="assessmentStatusSummaryContainer">
        <asset:image src="spinner.gif"/> Loading status summary...
    </div>
    <script type="text/javascript">
        $(document).ready(function () {
            let url = '${createLink(controller:'assessment', action: 'getAssessmentStatusSummary', id: assessment.id)}';
            console.log('Getting contents of: ' + url);
            $.get(url, function (data) {
                $('#assessmentStatusSummaryContainer').html(data);
            });
            setToggles();
            setOpener();
        });

        // set the tree nodes for opening and closing on a click
        function setToggles()  {
            document.querySelectorAll(".tipcaret").forEach( t =>  {
                t.addEventListener("click", function() {
                    this.parentElement.querySelector(".nestit").classList.toggle("unnest");
                    this.classList.toggle("tipcaret-down");
                });
            });
        }

        // open all tree nodes
        function setOpener()  {
            document.querySelectorAll(".nestit").forEach( c => {
                    c.classList.toggle("unnest");
                    c.parentElement.querySelector(".tipcaret").classList.toggle("tipcaret-down");
            });
        }

        //  close all tree nodes
        function setCloser()  {
            document.querySelectorAll(".unnest").forEach( c => {
                    c.classList.toggle("nestit");
                    c.parentElement.querySelector(".tipcaret-down").classList.toggle("tipcaret");
            });
        }

        function popUpCriteria()  {
            let dlgCriteria = document.getElementById("dlgCriteria");
            if(dlgCriteria.open)  {
                dlgCriteria.close();
            }  else  {
                let critTable = "<h4>Conformance Criteria</h4><table class=\"table table-condensed table-striped\">";
                <g:each in="${criteria}" var="criterion">
                    critTable += '<tr><td><div><div style="font-weight: bold;">';
                    critTable += '${criterion.name}';
                    critTable += '</div><div>';
                    critTable += '${criterion.description.replace("\n", "<br>").replace("\'", "\\'")}';
                    critTable += '</div></div></td></tr>';
                </g:each>
                critTable += "</table>";
                document.getElementById("divCriteria").innerHTML = critTable;
                dlgCriteria.show();
            }
        }

        function autoFill(){
            $.ajax({
                url: '${createLink(controller: 'assessmentPerform', action: 'fillInAll', id: assessment.id)}',
                data: {
                    timestamp: new Date().getTime(),
                    format: 'json'
                },
                dataType: 'json',
                success: function(data, statusText, jqXHR){
                    alert("Successfully set data for all, refreshing page...");
                    window.location.reload();
                },
                error: function(jqXHR, statusText, errorThrown){
                    alert("An unexpected error occurred setting the data for all!");
                }
            });
        }
    </script>
</div>

<g:if test="${flash.message}">
    <div style="margin-bottom: 2em; margin-top: 2em;" class="alert alert-info">${flash.message}</div>
</g:if>


<!-- Assessment Controls -->
<div class="row" style="margin-top: 1em;">
    <div class="col-md-12">
        <h4>Assessment Controls</h4>

        <div class="row">
            <div class="col-md-6">
                <!-- Single button -->
                <div class="btn-group">
                        <button type="button" class="btn btn-primary dropdown-toggle" data-toggle="dropdown" aria-expanded="false">
                            <assess:assessmentStatusIcon status="${assessment.status}" />
                            <assess:assessmentStatusName status="${assessment.status}" />
                            <span class="caret"></span>
                        </button>
                        <ul class="dropdown-menu" role="menu">
                            <li>
                                <g:link controller="assessmentPerform" action="changeAssessmentStatus"
                                        title="Use this to indicate the assessment has succeeded, and a trustmark should be generated."
                                        id="${assessment.id}" params="[newStatus: AssessmentStatus.SUCCESS]">
                                    <assess:assessmentStatusIcon status="${AssessmentStatus.SUCCESS}" /> &nbsp;
                                    Success
                                </g:link>
                            </li>
                            <li>
                                <g:link controller="assessmentPerform" action="changeAssessmentStatus"
                                        onclick="return verifyHasComment()"
                                        title="Use this to indicate for some reason the assessment has failed and the trustmark should not be generated."
                                        id="${assessment.id}" params="[newStatus: AssessmentStatus.FAILED]">
                                    <assess:assessmentStatusIcon status="${AssessmentStatus.FAILED}" /> &nbsp;
                                    Fail
                                </g:link>
                            </li>
                            <li>
                                <g:link controller="assessmentPerform" action="changeAssessmentStatus"
                                        onclick="return verifyHasComment()"
                                        id="${assessment.id}" params="[newStatus: AssessmentStatus.PENDING_ASSESSOR]"
                                        title="Use this to indicate you are pausing this assessment yourself.">
                                    <assess:assessmentStatusIcon status="${AssessmentStatus.PENDING_ASSESSOR}" /> &nbsp;
                                    Pending Assessor
                                </g:link>
                            </li>
                            <li>
                                <g:link controller="assessmentPerform" action="changeAssessmentStatus"
                                        onclick="return verifyHasComment()"
                                        id="${assessment.id}" params="[newStatus: AssessmentStatus.PENDING_ASSESSED]"
                                        title="Use this to indicate you are waiting on the assessed organization for something.">
                                    <assess:assessmentStatusIcon status="${AssessmentStatus.PENDING_ASSESSED}" /> &nbsp;
                                    Pending Assessed
                                </g:link>
                            </li>
                            <li>
                                <g:link controller="assessmentPerform" action="changeAssessmentStatus"
                                        onclick="return verifyHasComment()"
                                        id="${assessment.id}" params="[newStatus: AssessmentStatus.ABORTED]"
                                        title="Use this to indicate you are abandoning this assessment and do not intend to return.">
                                    <assess:assessmentStatusIcon status="${AssessmentStatus.ABORTED}" /> &nbsp;
                                    Abort
                                </g:link>
                            </li>
                        </ul>
                    </div>

            </div>

            <div class="col-md-6">
                <h5>Global Comment</h5>
                <textarea id="globalComment" class="form-control">${assessment?.comment ?: ''}</textarea>
            </div>

        </div>

        <script type="text/javascript">
            function verifyHasComment() {
                var globalComment = $('#globalComment').val();
                if( globalComment && globalComment.trim().length() > 0 ){
                    return true;
                }else{
                    alert("You are required to enter a comment.")
                    return false;
                }
            }//end verifyHasComment()
        </script>

    </div>
</div>

<div class="pageContent">
    <a name="assessmentStepView"></a>
    <h4>Assessment Steps</h4>

    <div class="row">
        <!-- Renders the assessment step enumeration -->
        <div id="assStepNavListContainer" class="col-md-4" style="min-height: 1000px;">
            <ul id="assStepNavList" class="assStepNavList nav nav-pills nav-stacked">
                <% String tipName = ""; String tdName = "";%>
                <g:each in="${assessment.sortedSteps}" var="stepData" status="stepDataStatus">
                    <g:if test="${tdName != "" && tdName != stepData.step.trustmarkDefinition.name}">
                        </ul></li>
                    </g:if>
                    <g:if test="${tipName != null && tipName != "" && tipName != assessment.getTipNameByStep(stepData)}">
                        </ul></li>
                    </g:if>
                    <g:if test="${tipName != null && tipName != assessment.getTipNameByStep(stepData)}">
                         <li class="${tipName == "" ? '' : 'tipindent'}"><span class="tipcaret"><b>&nbsp;${assessment.getTipNameByStep(stepData)}</b></span><span class="glyphicon"></span>
                         <% tipName = assessment.getTipNameByStep(stepData) %>
                         <ul class="nestit">
                    </g:if>
                    <g:if test="${tdName != stepData.step.trustmarkDefinition.name}">
                        <% tdName = stepData.step.trustmarkDefinition.name %>
                        <li class="tipindent"><span class="tipcaret"><i>&nbsp;${tdName}</i></span><span class="glyphicon"></span>
                        <ul class="nestit">
                    </g:if>
                    <li style="margin-left: 5px;" class="${stepData.step.id == currentStepData.step.id ? 'currentStep': ''}">
                        <a href="${createLink(controller: 'assessmentPerform', action: 'view', id: assessment.id, params: [stepId: stepData.step.id])}">

                            <assess:assessmentStepResult result="${stepData.result.result ?: AssessmentStepResult.Not_Known}" description="${stepData.result.description}"/>

                            <span id="step${stepData.id}Container">
                                <% int MAX_SIZE = 40; %>
                                <g:if test="${stepData.step.name.length() > MAX_SIZE}">
                                    ${stepData.step.name.substring(0, MAX_SIZE)}...
                                </g:if>
                                <g:else>
                                    ${stepData.step.name}
                                </g:else>
                            </span>

                            <g:if test="${!stepData.step.artifacts.isEmpty()}">
                                <span style="float: right;">
                                    <assess:assessmentStepAttachmentStatus step="${stepData}" />
                                </span>
                            </g:if>
                        </a>
                    </li>
                </g:each>
            </ul>
        </div>

        <!-- Renders the right side of the page, the actual step contents. -->
        <div class="col-md-8">
            <!-- Render Top Next/Prev Buttons -->
            <tmpl:renderNextPrevButtons />

            <!-- Container for current assessment step view -->
            <div class="row" style="margin-bottom: 1em;">
                <div class="col-md-12">
                    <div id="currentAssStepContainer">
                        <h3 class=stepName>
                            ${currentStepData.step.name}
                            <span style="float: right;">
                                <a href="#" onclick="popUpCriteria();"
                                   title="Show Conformance Criteria">
                                    <span class="glyphicon glyphicon-list-alt"></span>
                                </a>
                                <a href="${currentStepData.step.trustmarkDefinition.uri}" target="_blank"
                                   title="Show Trustmark Definition Source">
                                    <span class="glyphicon glyphicon-tag"></span>
                                </a>
                            </span>
                        </h3>
                        <div class="stepDescription">
                            ${currentStepData.step.description}
                        </div>

                        <g:if test="${currentStepData.step.artifacts && currentStepData.step.artifacts.size() > 0}">
                            <div style="margin-top: 1em;">
                                <h4>Required Artifacts</h4>
                                <div style="margin-top: 0.5em;">
                                    <g:each in="${currentStepData.step.artifacts.sort{ it.name }}" var="artifact">
                                        <div class="stepArtifactContainer">
                                            <div class="stepArtifactName">
                                                <assess:assessmentStepSingleArtifactStatus step="${currentStepData}" artifact="${artifact}" />
                                                ${artifact.name}
                                            </div>
                                            <div class="stepArtifactDesc">
                                                ${artifact.description}
                                            </div>
                                        </div>
                                    </g:each>
                                </div>
                            </div>
                        </g:if>

                        <hr/>

                        <div class="assStepFormData">
                            <ul class="nav nav-pills">
                                <g:each in="${allAssessmentStepResponses}" var="assessmentStepResponse">
                                    <li id="${assessmentStepResponse.name.replaceAll("\\s", "")}"
                                        class="stepDataStatus ${currentStepData.result == assessmentStepResponse ? 'active' : ''}">
                                        <a href="javascript:setStepDataStatus('${assessment.id}', '${currentStepData.step.id}', '${assessmentStepResponse.id}', '${assessmentStepResponse.name}')">
                                            <assess:assessmentStepResponseResult result="${assessmentStepResponse.result}" description="${assessmentStepResponse.description}"/>
                                            ${assessmentStepResponse.name}
                                        </a>
                                    </li>
                                </g:each>
                            </ul>

                            <div class="textCommentContainer" style="margin-top: 1em;">
                                <h4>Step Comment</h4>
                                <textarea id="assStepAssessorComment" class="form-control"
                                          style="width: 100%; height: 150px;">${currentStepData.assessorComment ?: ""}</textarea>
                            </div>

                            <div class="userArtifactsContainer" style="margin-top: 1em">
                                <h4>Artifacts</h4>
                                <div style="margin-top: 0.5em;">
                                    <g:if test="${currentStepData.artifacts && currentStepData.artifacts.size() > 0}">
                                        <g:each in="${currentStepData.artifacts}" var="artifact">
                                            <div class="row" style="margin-bottom: 1em;">
                                                <div class="col-md-1" style="padding-top: 20px;">
                                                    <g:link controller="assessmentPerform" action="viewArtifact" id="${assessment.id}"
                                                            params="[stepDataId: currentStepData.id, artifactId: artifact.id]"
                                                            class="btn btn-default" title="View this artifact's details.">

                                                        <span class="glyphicon glyphicon-search"></span>
                                                    </g:link>
                                                </div>
                                                <div class="col-md-11">
                                                    <assess:renderArtifactSummary artifact="${artifact}" shortenComment="false" />
                                                </div>
                                            </div>
                                        </g:each>
                                    </g:if>
                                    <g:else>
                                        <em>There are no artifacts.</em>
                                    </g:else>
                                </div>
                                <div style="margin-top: 0.5em;">
                                    <g:link class="btn btn-default"
                                            controller="assessmentPerform" action="createArtifact" id="${assessment.id}" params="[stepDataId: currentStepData.id]">
                                        <span class="glyphicon glyphicon-plus"></span>
                                        Add Artifact
                                    </g:link>
                                </div>
                            </div>

                            <div class="userParametersContainer" style="margin-top: 1em">
                                <h4>Parameters</h4>
                                <div style="margin-top: 0.5em">
                                    <g:if test="${currentStepData.step.parameters && currentStepData.step.parameters.size() > 0}">
                                        <g:each in="${currentStepData.step.parameters.sort()}" var="parameter">
                                            <g:set var="paramKind" value="${Enum.valueOf(ParameterKind, parameter.kind)}"/>
                                            <g:set var="paramValue" value="${ParameterValue.findByStepDataAndParameter(currentStepData, parameter) ?: null}"/>
                                            <g:set var="paramBinding" value="${new TrustmarkParameterBindingImpl(parameterKind: paramKind, value: paramValue?.userValue)}"/>
                                            <g:set var="userValueFor" value="${{fn -> paramValue ? fn() : null }}"/>
                                            <div class="row" style="margin-bottom: 1em;">
                                                <div class="col-xs-7">
                                                    <g:if test="${parameter.required}">
                                                        <span class="glyphicon glyphicon-star"></span>
                                                    </g:if>
                                                    <span>${parameter.name}</span>
                                                    (<small>${parameter.identifier}</small>)
                                                    <div>
                                                        <em>${raw(parameter.description)}</em>
                                                    </div>
                                                </div>
                                                <div class="col-xs-5 row parameter-values text-center"
                                                     data-step-data-id="${currentStepData.id}"
                                                     data-td-param-id="${parameter.id}"
                                                     data-param-kind="${paramKind}"
                                                >
                                                    <g:if test="${paramKind == ParameterKind.STRING}">
                                                        <!-- String Parameter Control -->
                                                        <g:set var="userValue" value="${userValueFor(paramBinding.&getStringValue)}"/>
                                                        <div class="col-xs-12">
                                                            <div class="form-group">
                                                                <input type="text"
                                                                       id="param_${parameter.identifier}"
                                                                       class="form-control"
                                                                       data-initial-value="${userValue}"
                                                                       placeholder="text value"
                                                                />
                                                            </div>
                                                        </div>
                                                    </g:if>

                                                    <g:if test="${paramKind == ParameterKind.BOOLEAN}">
                                                        <!-- Boolean Parameter Control -->
                                                        <g:set var="userValue" value="${userValueFor(paramBinding.&getBooleanValue)}"/>
                                                        <div class="col-xs-6">
                                                            <div class="radio disabled inline">
                                                                <label>
                                                                    <input type="radio"
                                                                           name="param_${parameter.identifier}"
                                                                           value="null"
                                                                           disabled="disabled"
                                                                           data-checked="${userValue == null}"
                                                                    />
                                                                    Unknown
                                                                </label>
                                                            </div>
                                                        </div>
                                                        <div class="col-xs-6">
                                                            <div class="radio">
                                                                <label>
                                                                    <input type="radio"
                                                                           name="param_${parameter.identifier}"
                                                                           value="true"
                                                                           data-checked="${userValue != null && userValue}"
                                                                    />
                                                                    True
                                                                </label>
                                                            </div>
                                                            <div class="radio">
                                                                <label>
                                                                    <input type="radio" name="param_${parameter.identifier}"
                                                                           value="false"
                                                                           data-checked="${userValue != null && !userValue}"
                                                                    />
                                                                    False
                                                                </label>
                                                            </div>
                                                        </div>
                                                    </g:if>

                                                    <g:if test="${paramKind == ParameterKind.NUMBER}">
                                                        <!-- Number Parameter Control -->
                                                        <g:set var="userValue" value="${userValueFor(paramBinding.&getNumericValue)}"/>
                                                        <div class="col-xs-12">
                                                            <div class="form-group">
                                                                <input type="number"
                                                                       id="param_${parameter.identifier}"
                                                                       class="form-control"
                                                                       data-initial-value="${userValue}"
                                                                       placeholder="number value"
                                                                />
                                                            </div>
                                                        </div>
                                                    </g:if>

                                                    <g:if test="${paramKind == ParameterKind.DATETIME}">
                                                        <g:set var="userValue" value="${(Calendar)userValueFor(paramBinding.&getDateTimeValue)}"/>
                                                        <!-- DateTime Parameter Control -->
                                                        <div class="col-xs-12">
                                                            <div class="form-group">
                                                                <input type="text"
                                                                       id="param_${parameter.identifier}"
                                                                       class="form-control"
                                                                       data-initial-value="${userValue?.format('yyyy-MM-dd')}"
                                                                       placeholder="yyyy-mm-dd"
                                                                />
                                                                <span class="hide help-block">
                                                                    Invalid Date (use yyyy-mm-dd)
                                                                </span>
                                                            </div>
                                                        </div>
                                                    </g:if>

                                                    <g:if test="${paramKind == ParameterKind.ENUM}">
                                                        <!-- Enum-Single Parameter Control -->
                                                        <g:set var="userValue" value="${userValueFor(paramBinding.&getStringValue)}"/>
                                                        <div class="col-xs-12">
                                                            <div class="form-group">
                                                                <select id="param_${parameter.identifier}"
                                                                        class="form-control"
                                                                        data-initial-value="${userValue}"
                                                                >
                                                                    <option value="" disabled="disabled">Choose one...</option>
                                                                    <g:each in="${parameter.sortedEnumValues}" var="enumValue">
                                                                        <option value="${enumValue.encodeAsHTML()}">${enumValue.encodeAsHTML()}</option>
                                                                    </g:each>
                                                                </select>
                                                            </div>
                                                        </div>
                                                    </g:if>

                                                    <g:if test="${paramKind == ParameterKind.ENUM_MULTI}">
                                                        <!-- Enum-Multi Parameter Control -->
                                                        <g:set var="userValue" value="${userValueFor(paramBinding.&getStringListValue)}"/>
                                                        <div class="col-xs-12 text-left">
                                                            <g:each in="${parameter.sortedEnumValues}" var="enumValue">
                                                                <div class="checkbox">
                                                                    <label>
                                                                        <input type="checkbox"
                                                                               data-value="${enumValue}"
                                                                               data-checked="${userValue != null && enumValue in userValue}"
                                                                        >
                                                                        ${enumValue}
                                                                    </label>
                                                                </div>
                                                            </g:each>
                                                        </div>
                                                    </g:if>

                                                </div>
                                            </div>
                                        </g:each>
                                    </g:if>
                                    <g:else>
                                        <em>There are no parameters.</em>
                                    </g:else>
                                </div>
                            </div>
                        </div>

                    </div>
                </div>
            </div>


            <div style="margin-top: 20px;">
                <!-- Display bottom next-previous buttons -->
                <tmpl:renderNextPrevButtons />
            </div>
        </div>


</div>


<script type="text/javascript">
    $(document).ready(function () {
        $('#assStepAssessorComment').keyup(function () {
            typewatch(function () {
                changeAssessmentStepComment();
            }, 500);
        })

        $('#globalComment').keyup(function () {
            typewatch(function () {
                changeGlobalComment();
            }, 500);
        })

    });

    function changeAssessmentStepComment() {
        console.log("Change assessment step comment called!");
        var commentText = $('#assStepAssessorComment').val();
        // TODO check for length
        var url = '${createLink(controller:'assessmentPerform', action: 'setStepDataComment', id: assessment.id, params: [stepId: currentStepData.step.id])}';
        $.ajax({
            url: url,
            type: 'POST',
            data: {
                comment: commentText,
                format: 'json'
            },
            success: function (data) {
                console.log("Finished new comment post, response: " + JSON.stringify(data, null, '   '));
                if (data && data.status && data.status == "SUCCESS") {
                    console.log("Successfully posted comment.")
                }
            },
            error: function (xhr) {
                console.log("An error occurred posting the global comment.")
                alert("An error occurred posting the global comment.")
            }
        });
    }//end changeAssessmentStepComment

    function changeGlobalComment() {
        console.log("Change global comment called!");
        var commentText = $('#globalComment').val();
        // TODO check for length
        var url = '${createLink(controller:'assessmentPerform', action: 'changeGlobalComment', id: assessment.id)}';
        $.ajax({
            url: url,
            type: 'POST',
            data: {
                comment: commentText,
                format: 'json'
            },
            success: function (data) {
                console.log("Finished new comment post, response: " + JSON.stringify(data, null, '   '));
                if (data && data.status && data.status == "SUCCESS") {
                    console.log("Successfully posted comment.")
                }
            },
            error: function (xhr) {
                console.log("An error occurred posting the global comment.")
                alert("An error occurred posting the global comment.")
            }
        });
    }//end changeGlobalComment

    function parseDate(str) {
        var isValid = /\d\d\d\d-\d\d?-\d\d?/.test(str);
        if (!isValid) { return {isValid: false, value: null}; }
        var split = str.split('-');
        var year = +split[0];
        var month = +split[1] - 1;
        var day = +split[2];
        var d = new Date(year, month, day);
        var parts = [d.getFullYear(), d.getMonth() + 1, d.getDate()];
        if (year !== parts[0] || month !== parts[1] || day !== parts[2]) {
            return {isValid: false, value: null};
        }
        return {isValid: true, value: d.getTime()}
    }
    $(document).ready(function () {
        var jqParameterContainers = jQuery('[data-step-data-id]');
        jqParameterContainers.each(function () {
            var jqBaseElement = jQuery(this);
            var stepDataId = jqBaseElement.data('stepDataId');
            var parameterId = jqBaseElement.data('tdParamId');
            var paramKind = jqBaseElement.data('paramKind');
            switch (paramKind) {
                case 'STRING':
                case 'NUMBER':
                case 'DATETIME':
                    jqBaseElement.find('input[data-initial-value]').first().each(function () {
                        // initial value
                        var jqSelf = jQuery(this);
                        var initialValue = jqSelf.data('initialValue');
                        jqSelf.prop('value', initialValue);
                        // updates
                        jqSelf.on('blur change keyup', function () {
                            typewatch(function () {
                                var userValueString = jqSelf.prop('value');
                                if (paramKind === 'NUMBER' && !userValueString) { return; }
                                if (paramKind === 'DATETIME') {
                                    var jqForm = jqSelf.closest('.form-group');
                                    var jqHelp = jqForm.find('.help-block');;
                                    var parsed = parseDate(userValueString);
                                    jqForm.toggleClass('has-error', !parsed.isValid);
                                    jqHelp.toggleClass('hide', parsed.isValid);
                                    if (!parsed.isValid) { return; }
                                    userValueString = parsed.value;
                                }
                                setParameterValue(stepDataId, parameterId, userValueString);
                            }, 500);
                        });
                    });
                    break;
                case 'BOOLEAN':
                    jqBaseElement.find('[data-checked]').each(function () {
                        // initial value
                        var jqSelf = jQuery(this);
                        var isChecked = jqSelf.data('checked'); // jQuery#data will convert the value to native type
                        jqSelf.prop('checked', isChecked);
                        // updates
                        var userValueString = jqSelf.prop('value');
                        if (userValueString !== 'true' && userValueString !== 'false') { return; }
                        jqSelf.on('click', function () {
                            typewatch(function () {
                                setParameterValue(stepDataId, parameterId, userValueString);
                            }, 500);
                        });
                    });
                    break;
                case 'ENUM':
                    jqBaseElement.find('select[data-initial-value]').first().each(function () {
                        // initial value
                        var jqSelf = jQuery(this);
                        var initialValue = jqSelf.data('initialValue');
                        jqSelf.prop('value', initialValue);
                        // updates
                        jqSelf.on('blur change', function () {
                            typewatch(function () {
                                var userValueString = jqSelf.prop('value');
                                if (!userValueString) { return; }
                                setParameterValue(stepDataId, parameterId, userValueString);
                            }, 500);
                        });
                    });
                    break;
                case 'ENUM_MULTI':
                    var ENUM_MULTI_SEPARATOR = '${ParameterKind.IOConstants.ENUM_MULTI_SEPARATOR}';
                    jqBaseElement.find('[data-checked]').each(function () {
                        // initial value
                        var jqSelf = jQuery(this);
                        var isChecked = jqSelf.data('checked'); // jQuery#data will convert the value to native type
                        jqSelf.prop('checked', isChecked);
                        // updates
                        jqSelf.on('click', function () {
                            typewatch(function () {
                                var checkedBoxes = jqBaseElement.find('[data-checked]:checked');
                                var checkedValues = checkedBoxes.map(function () { return $(this).data('value'); }).get();
                                var userValueString = checkedValues.join(ENUM_MULTI_SEPARATOR);
                                if (!userValueString) { jqSelf.prop('checked', true); return; }
                                setParameterValue(stepDataId, parameterId, userValueString);
                            }, 500);
                        });
                    });
                    break;
                default:
                    console.log('Param Kind not yet implemented: ' + paramKind);
                    break;
            }
        });
    });
    function setParameterValue(stepDataId, parameterId, userValueString) {
        console.log("Set parameter value called! [step, param, value]", stepDataId, parameterId, userValueString);
        var url = '${createLink(controller: 'assessmentPerform', action: 'setParameterValue', id: assessment.id)}';
        $.ajax({
            url: url,
            type: 'POST',
            data: {
                format: 'json',
                stepDataId: stepDataId,
                parameterId: parameterId,
                userValueString: userValueString
            },
            success: function (data) {
                console.log("Finished parameter value update, response: " + JSON.stringify(data, null, '   '));
                if (data && data.status && data.status === "SUCCESS") {
                    console.log("Successfully updated parameter value. [step, param, value]", stepDataId, parameterId, userValueString);
                }
            },
            error: function () {
                console.log("An error occurred updating parameter value. [step, param, value]", stepDataId, parameterId, userValueString);
                alert("An error occurred updating a parameter value.")
            }
        });
    }

    function deleteArtifact(id) {
        if (confirm("Really delete this artifact?")) {
            var url = "${createLink(controller:'assessmentPerform', action: 'deleteArtifact', id: assessment.id, params:[stepNumber: currentStepData.step.stepNumber, artifactId: '_99_'])}?format=json";
            url = url.replace("_99_", id);
            console.log("Deleting artifact: " + id + ", URL: " + url);
            $.post(url, function (data) {
                console.log("Response: " + data);
                if (data && data.status && data.status == "SUCCESS") {
                    location.reload();
                } else {
                    alert("An error occurred while deleting the artifact.  Please refresh the page, and try again.")
                }

            })
        }
    }

    function setStepDataStatus(assessmentId, currentStepDataStepId, assessmentStepResponseId, assessmentStepResponseName) {
        console.log("setStepDataStatus! [assessmentId, currentStepDataStepId, assessmentStepResponseId, assessmentStepResponseName]", assessmentId,
            currentStepDataStepId, assessmentStepResponseId, assessmentStepResponseName);

        var url = '${createLink(controller: 'assessmentPerform', action: 'setStepDataStatus')}';
        $.ajax({
            url: url,
            type: 'POST',
            data: {
                format: 'json',
                id: assessmentId,
                stepId: currentStepDataStepId,
                statusId: assessmentStepResponseId
            },
            success: function (data) {
                console.log("Finished parameter value update, response: " + JSON.stringify(data, null, '   '));
                if (data && data.status && data.status === "SUCCESS") {
                    console.log("Successfully updated the set step data status value. [assessmentId, currentStepDataStepId, assessmentStepResponseId]",
                        assessmentId, currentStepDataStepId, assessmentStepResponseId);

                    // deactivate all status links
                    $(".stepDataStatus").removeClass( "active" );

                    // activate the selected status link
                    $("#" + assessmentStepResponseName.replace(/\s+/g, '')).addClass( "active" );
                }
            },
            error: function () {
                console.log("An error occurred updating the set step data status value. [assessmentId, currentStepDataStepId, assessmentStepResponseName]",
                    assessmentId, currentStepDataStepId, assessmentStepResponseName);
                alert("An error occurred updating the set step data status value.")
            }
        });
    }

</script>

</div>
</body>
</html>

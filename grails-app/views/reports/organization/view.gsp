<%@ page import="nstic.util.TipTreeNode; nstic.web.tip.TrustInteroperabilityProfile; nstic.web.td.TrustmarkDefinition; nstic.web.assessment.*; org.apache.commons.io.FileUtils; nstic.web.*;" defaultCodec="none"  %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="report-base"/>
    <title>Organization Report</title>
    <style type="text/css">
        .assessmentIdColHeader {width: 50px;}
        .assessmentNameColHeader {width: auto;}
        .statusColHeader {width: 50px; text-align: center;}
        .lastActivityDateColHeader {width: 100px; text-align: center;}
        .assessmentCommentColHeader {width: auto;}

        .assessmentIdCol {width: 50px; font-size: 80%;}
        .assessmentNameColCol {width: auto; font-size: 80%;}
        .statusCol {width: 65px; text-align: center;}
        .lastActivityDateCol {width: 115px; text-align: center;}
        .assessmentCommentCol {width: auto;}


        .overallGroupByTDBox {
            border-top: 2px solid #333;
            border-bottom: 2px solid #333;
        }
        .tdOrgReportContainer {
            margin-top: 50px;
            padding-bottom: 25px;
        }
        .tdOrgReportContainer:not(:last-child) {
            border-bottom: 2px solid #888;
        }

        .tipOrgReportContainer > .row:last-child .tdTipSeparator {
            border-bottom: 1px solid #999;
            padding-bottom: 15px;
        }

        .stepContainer {
            padding-bottom: 20px;
        }

        .containsStepStatusAndNameContainer {
            margin-left: 15px;
        }

        .artifactName {
            text-decoration: underline;
            font-style: italic;
        }

        .artifactDataContainer {
            margin-left: 30px;
        }
        .artifactCommentText {
            font-style: italic;
        }

        .assessmentStepDescription {
            padding-left: 1em;
        }

        .assessmentStepArtifactDefinitions {
            padding-left: 1em;
            margin-bottom: 1em;
        }

        .requiredArtifactsList, .stepArtifactsList {
            list-style: none;
            margin: 0;
            padding: 0;
        }
        .requiredArtifactsList li {
            padding-left: 1em;
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


    <div class="reportContent" style="margin-top: 25px;">
        <h3>Organization Report: ${organization.name}</h3>
        <div>
            Generated <g:formatDate date="${Calendar.getInstance().getTime()}" format="MM/dd/yyyy" />,
            By ${user.username} <br/>
            Including Assessment Data from <g:formatDate date="${startDate}" format="MM/dd/yyyy" />
            to <g:formatDate date="${endDate}" format="MM/dd/yyyy" /> for ${organization.identifier},
            resulting in ${assessments.size()} assessments.
        </div>

        <div style="margin-top: 25px;">
            <img src="${charts['statusChart'].toURLForHTML()}" />
        </div>

        <div style="margin-top: 25px;">
            <h3>Organization Comments <small>(${organization.comments?.size() ?: 0} total)</small></h3>

            <g:if test="${organization.comments && !organization.comments.isEmpty()}">
                <g:each in="${organization.comments}" var="orgComment">
                    <div class="row">
                        <div class="col-md-2"><g:formatDate date="${orgComment.dateCreated}" format="yyyy-MM-dd" /></div>
                        <div class="col-md-3">${orgComment.user?.contactInformation?.responder}</div>
                    </div>
                    <div class="row" style="margin-bottom: 2em;">
                        <div class="col-md-12">
                            <b>${orgComment.title}</b>
                            <div style="font-size: 90%;" class="text-muted">
                                <pre style="width: 725px;">${orgComment.comment}</pre>
                            </div>
                        </div>
                    </div>
                </g:each>
            </g:if>
            <g:else>
                <em>There are no organization comments.</em>
            </g:else>
        </div>

        <!-- TD Summary Table -->
        <div style="margin-top: 25px;">
            <h3>Trustmark Assessment Summary</h3>
            <tmpl:/templates/displayAssessmentStatusIconLegend />
            <table class="table table-bordered table-striped table-condensed" id="summaryTable">
                <thead>
                    <tr>
                        <th class="assessmentIdColHeader">
                            <a href="javascript:sortStatusTable('assessmentIdCol')">ID <span id="assessmentIdColSort">&darr;</span></a>
                        </th>
                        <th class="assessmentNameColHeader">
                            <a href="javascript:sortStatusTable('assessmentNameCol')">Assessment Name <span id="assessmentNameColSort">&darr;</span></a>
                        </th>
                        <th class="statusColHeader">
                            <a href="javascript:sortStatusTable('statusCol')">Status <span id="statusColSort"></span></a>
                        </th>
                        <th>
                            TIPs
                        </th>
                        <th>
                            TDs
                        </th>
                        <th class="lastActivityDateColHeader">
                            <a href="javascript:sortStatusTable('lastActivityDateCol')">Last Activity <span id="lastActivityDateColSort"></span></a>
                        </th>
                        <th class="assessmentCommentColHeader">Assessment Comment</th>
                    </tr>
                </thead>
                <tbody>
                    <g:each in="${assessments}" var="assessment">
                        <g:if test="${command.hideCompletedAssessments && assessment.status == nstic.web.assessment.AssessmentStatus.SUCCESS}">
                            <!-- Assessment ${assessment.id} omitted because it is completed. -->
                        </g:if>
                        <g:else>
                            <tr id="assessment_${assessment.id}_SUMMARY_ROW">
                                <td class="assessmentIdCol">
                                    <span class="assessmentId">${assessment.id}</span>
                                </td>
                                <td class="assessmentNameCol">
                                    <a href="#assessment_${assessment.id}">
                                        <span class="assessmentName">${assessment.assessmentName}</span>
                                    </a>
                                </td>
                                <td class="statusCol" style="white-space: nowrap;">
                                    <tmpl:/templates/displayAssessmentStatusIcon assessment="${assessment}" />
                                    <g:if test="${!assessment.getIsComplete()}">
                                        <span class="percentSatisifedContainer">
                                            <span class="statusColValue">${assessment.getPercentStepsSatisfied()}</span>%
                                        </span>
                                    </g:if>
                                    <g:else>
                                        <span style="display: none;">
                                            <span class="statusColValue">100</span>%
                                        </span>
                                    </g:else>
                                </td>
                                <td class="text-center" style="white-space: nowrap">
                                    ${fullySatisfiedTipCountByAssessmentId[assessment.id]}
                                    /
                                    ${tdsAndTipsByAssessmentId[assessment.id].trustInteroperabilityProfiles.size()}
                                </td>
                                <td class="text-center" style="white-space: nowrap">
                                    ${fullySatisfiedTdCountByAssessmentId[assessment.id]}
                                    /
                                    ${assessment.tdLinks.size()}
                                </td>
                                <td class="lastActivityDateCol">
                                %{--<g:formatDate format="yyyy-MM-dd" date="${assessment.logg.getMostRecentEntry().dateCreated}" /> <br/>--}%
                                    <% int daysAgo = (int) ((System.currentTimeMillis() - assessment.logg.getMostRecentEntry().dateCreated.getTime()) / (24 * 60 * 60 * 1000d)); %>
                                    <g:if test="${daysAgo < 30}">
                                        <span style="color: #${nstic.assessment.ColorPalette.SUCCESS_TEXT.toString()}; font-weight: bold;" class="daysAgo">${daysAgo}</span>
                                    </g:if>
                                    <g:elseif test="${daysAgo < 60}">
                                        <span style="color: #${nstic.assessment.ColorPalette.WARNING_TEXT.toString()}; font-weight: bold;" class="daysAgo">${daysAgo}</span>
                                    </g:elseif>
                                    <g:else>
                                        <span style="color: #${nstic.assessment.ColorPalette.ERROR_TEXT.toString()}; font-weight: bold;" class="daysAgo">${daysAgo}</span>
                                    </g:else>
                                    Days Ago
                                </td>
                                <td class="assessmentCommentCol">
                                    <g:set var="lastCommentWithAssessor" value="${assessment.getLastGlobalCommentWithAuthor()}" />
                                    <g:if test="${lastCommentWithAssessor && lastCommentWithAssessor.comment && lastCommentWithAssessor.comment.trim().length() > 0}">
                                        ${lastCommentWithAssessor.user?.contactInformation?.responder} wrote:
                                        ${lastCommentWithAssessor.comment}
                                    </g:if>
                                    <g:else>
                                        <assess:assessmentStatusName status="${assessment.status}" />
                                    </g:else>
                                </td>
                            </tr>
                        </g:else>
                    </g:each>
                </tbody>
            </table>
            <script type="text/javascript">
                var lastSortCol = "assessmentIdCol";
                var lastSortDir = "asc";

                $(document).ready(function(){
                    updateSortIndication();
                })

                function clearSortIndication(){
                    $('#assessmentIdColSort').html('');
                    $('#assessmentNameColSort').html('');
                    $('#statusColSort').html('');
                    $('#lastActivityDateColSort').html('');
                }

                function updateSortIndication(){
                    clearSortIndication();
                    var html = "&uarr;";
                    if( lastSortDir === "desc" ){
                        html = "&darr;";
                    }
                    $('#'+lastSortCol+"Sort").html(html);
                }

                function getSortValue(colToSort, row){
                    var id = row.id;
                    var spanFieldClassName = "";
                    if( colToSort === "assessmentIdCol" ){
                        spanFieldClassName = "assessmentId";
                    }else if( colToSort === "assessmentNameCol" ){
                        spanFieldClassName = "assessmentName";
                    }else if( colToSort === "statusCol" ){
                        spanFieldClassName = "statusColValue";
                    }else{
                        spanFieldClassName = "daysAgo";
                    }
                    var idOfValue = '#'+id+" ."+spanFieldClassName;
                    var sortValue = $(idOfValue).text().toLowerCase();
                    // console.log("For colToSort='"+colToSort+"', row '"+id+"', returning sort value['"+idOfValue+"']: '"+sortValue+"'");
                    return sortValue;
                }

                function sortStatusTable(col){
                    console.log("Changing sort on column: "+col);
                    if( lastSortCol === col && lastSortDir === "desc" ){
                        lastSortDir = "asc";
                    }else if( lastSortCol === col && lastSortDir === "asc" ){
                        lastSortDir = "desc";
                    }else {
                        lastSortCol = col;
                        lastSortDir = "asc";
                    }

                    var rows = []
                    $('#summaryTable tbody tr').each(function(index, object){
                        var sortValue = getSortValue(col, object);
                        rows.push({index: object.id, html: object.outerHTML, sortValue: sortValue});
                    });

                    rows.sort(function(row1, row2){
                        var compareVal = 0;
                        if( isNaN(row1.sortValue) ){
                            compareVal = row1.sortValue.localeCompare(row2.sortValue);
                        }else{
                            var num1 = +row1.sortValue;
                            var num2 = +row2.sortValue;
                            compareVal = num1 - num2;
                        }

                        if( lastSortDir == "asc" ){
                            return compareVal;
                        }else{
                            return -compareVal;
                        }
                    });

                    $('#summaryTable tbody').html('');
                    for( var i = 0; i < rows.length; i++ ){
                        var rowData = rows[i];
                        $('#summaryTable tbody').append(rowData.html);
                    }

                    updateSortIndication();
                }//end sortStatusTable();

            </script>
        </div>

        <!-- Each TD by Assessment Report -->
        <div style="magin-top: 50px;" class="overallGroupByTDBox container-fluid">
            <g:each in="${assessments}" var="assessment">
                <g:set var="lastAssessor" value="${assessment.getLastAssessor()}" />

                <g:if test="${command.hideCompletedAssessments && assessment.status == AssessmentStatus.SUCCESS}">
                    <!-- Assessment ${assessment.id} omitted because it is completed. -->
                </g:if>
                <g:else>
                    <div class="tdOrgReportContainer">
                        <a name="assessment_${assessment.id}"></a>
                        <div class="row">
                            <div class="col-md-8">
                                <h3>
                                    <span style="font-size: 120%">
                                        <tmpl:/templates/displayAssessmentStatusIcon assessment="${assessment}" />
                                    </span>
                                    ${assessment.assessmentName}
                                    <small>
                                        <a href="${createLink(controller: 'assessment', action: 'view', id: assessment.id)}" target="_blank">
                                            <span class="glyphicon glyphicon-new-window"></span>
                                        </a>
                                    </small>
                                </h3>
                                <img src="${charts[assessment.id+"_STEP_CHART"].toURLForHTML()}" />
                                <div>
                                    <span class="assessmentStepResultStatusSummary">
                                        ${assessment.getCountOfSteps(AssessmentStepResult.Satisfied)} satisfied,
                                        ${assessment.getCountOfSteps(AssessmentStepResult.Not_Satisfied)} not satisfied,
                                        ${assessment.getCountOfSteps(AssessmentStepResult.Not_Known)} unanswered
                                        and
                                        ${assessment.getCountOfSteps(AssessmentStepResult.Not_Applicable)} N/A.
                                    </span>
                                </div>
                            </div>
                            <div class="col-md-4" style="text-align: right; font-size: 90%;">
                                <div class="statusContainer" style="text-align: right;" data-summary-id="${assessment.id}">
                                    <asset:image src="spinner.gif" /> Loading status summary...
                                </div>
                                <script type="text/javascript">
                                    $(document).ready(function(){
                                        loadAssessmentStatus${assessment.id}();
                                    });
                                    function loadAssessmentStatus${assessment.id}(){
                                        var url = '${createLink(controller:'assessment', action: 'getAssessmentStatusSummary', id: assessment.id)}';
                                        console.log('Getting contents of: '+url);
                                        $.ajax({
                                            url: url,
                                            dataType: 'html',
                                            error: function(jqXHR, textStatus, errorThrown){
                                                $('[data-summary-id=${assessment.id}]').html(
                                                    "<div class=\"text-danger\">An error occurred: "+errorThrown+"</div>"+
                                                    "<a href=\"javascript:loadAssessmentStatus${assessment.id}();\" class=\"btn btn-default btn-xs\">Reload</a>");
                                            },
                                            success: function (data, status, jqXHR) {
                                                $('[data-summary-id=${assessment.id}]').html(data);
                                            }
                                        });
                                    }
                                </script>
                            </div>
                        </div>
                        <div class="row" style="margin-top: 0.5em; margin-bottom: 1em;">
                            %{--<div class="col-md-6">--}%
                            %{--<tmpl:/templates/displayAssessmentStatusText assessment="${assessment}" />--}%
                            %{--</div>--}%
                            <div class="col-md-12" style="text-align: left;">
                                Last Assessed By: ${lastAssessor?.contactInformation?.responder} <br/>
                                Comment:
                                <g:if test="${assessment.comment && assessment.comment?.trim().length() > 0}">
                                    <pre style="white-space: pre-wrap;">${assessment.comment}</pre>
                                </g:if>
                                <g:else>
                                    <em>No Comment.</em>
                                </g:else>
                            </div>
                        </div>
                        <div class="tipOrgReportContainer">
                        <%
                            def tdsAndTips = tdsAndTipsByAssessmentId[assessment.id]
                            def tipInfoByTipId = tipInfoByTipIdByAssessmentId[assessment.id]
                        %>
                        <g:each in="${tdsAndTips.trustInteroperabilityProfiles}" var="tipData">
                            <% def tipInfo = tipInfoByTipId[tipData.databaseId] %>
                            <div class="row" style="margin: 0.5em auto 1em;">
                                <h4>
                                    <g:if test="${tipInfo.getChosenTdStepResultCount(AssessmentStepResult.Not_Known) > 0}">
                                        <span class="glyphicon glyphicon-cog" style="color: #ddd;"></span>
                                    </g:if>
                                    <g:else>
                                        <g:if test="${tipInfo.getChosenTdStepResultCount(AssessmentStepResult.Not_Satisfied) > 0}">
                                            <span class="glyphicon glyphicon-remove-sign" style="color: #a94442;"></span>
                                        </g:if>
                                        <g:else>
                                            <span class="glyphicon glyphicon-ok-sign" style="color: darkgreen"></span>
                                        </g:else>
                                    </g:else>
                                    <span class="glyphicon glyphicon-list"></span>
                                    ${tipData.name}, ${tipData.version}
                                </h4>
                                <div class="stepContainer">
                                    <div class="row containsStepDescAndArtifactsContainer">
                                        <div class="col-md-offset-1 col-md-11">
                                            <b>Assessing: </b>
                                            <g:if test="${tipData.useAllTds}">
                                                all
                                            </g:if>
                                            <g:else>
                                                a selection of
                                            </g:else>
                                            referenced TDs in TIP
                                            <g:if test="${tipData.useAllTds}">
                                                (${tipInfo.chosenTds.size()} total)
                                            </g:if>
                                            <g:else>
                                                (${tipData.tdUris.size()} of ${tipInfo.allPotentialTds.size()})
                                            </g:else>
                                        </div>
                                        <div class="col-md-offset-1 col-md-11">
                                            <b>TIP Step Results (of ${tipInfo.chosenTdSteps.size()} Steps): </b>
                                            ${tipInfo.getChosenTdStepResultCount(AssessmentStepResult.Satisfied)} satisfied,
                                            ${tipInfo.getChosenTdStepResultCount(AssessmentStepResult.Not_Satisfied)} not satisfied,
                                            ${tipInfo.getChosenTdStepResultCount(AssessmentStepResult.Not_Known)} unanswered, and
                                            ${tipInfo.getChosenTdStepResultCount(AssessmentStepResult.Not_Applicable)} N/A
                                        </div>
                                        <div class="col-md-offset-1 col-md-10 tdTipSeparator"></div>
                                    </div>
                                </div>

                                <div class="col-md-11">
                                    <%
                                        def stepListByTd = assessment.sortedSteps.groupBy{it.step.trustmarkDefinition}
                                        def completeResults = [AssessmentStepResult.Satisfied, AssessmentStepResult.Not_Applicable].toSet()
                                        def tdIsComplete = { TrustmarkDefinition td -> stepListByTd[td].every{ completeResults.contains(it.result) } }

                                        def usedTds = assessment.tdLinks.findAll{tipData.tdUris.contains(it.trustmarkDefinition.uri)}

                                        def sortedTds = usedTds.collect{it.trustmarkDefinition}.sort { x, y ->
                                            def xCompletion = tdIsComplete(x)
                                            def yCompletion = tdIsComplete(y)
                                            if (xCompletion == yCompletion) { x.name.compareToIgnoreCase(y.name) }
                                            else { xCompletion ? -1 : 1 }
                                        }
                                    %>
                                    <g:each in="${sortedTds}" var="td">
                                        <%
                                            def tdStepList = stepListByTd[td]
                                            def stepCount = tdStepList.size()
                                            def satisfiedStepCount = tdStepList.count {it.result == AssessmentStepResult.Satisfied}
                                            def notSatisfiedStepCount = tdStepList.count {it.result == AssessmentStepResult.Not_Satisfied}
                                            def notKnownStepCount = tdStepList.count {it.result == AssessmentStepResult.Not_Known}
                                            def notApplicableStepCount = tdStepList.count {it.result == AssessmentStepResult.Not_Applicable}
                                            def omitAllStepBecauseSatisfiedOrNA = command.hideCompletedSteps && (stepCount == (satisfiedStepCount + notApplicableStepCount))
                                        %>


                                        <h4>
                                            <span>
                                                <g:if test="${omitAllStepBecauseSatisfiedOrNA}">
                                                    <span class="glyphicon glyphicon-ok-sign text-success"></span>
                                                </g:if>
                                                <span class="glyphicon glyphicon-tag"></span>
                                                ${td.name}, ${td.tdVersion}
                                            </span>
                                        </h4>
                                        <g:if test="${omitAllStepBecauseSatisfiedOrNA}">
                                            <!-- Omitting all steps because they are all Satisfied or N/A. -->
                                            <!-- Even if satisfied or N/A, show the parameter values. -->
                                            <g:each in="${tdStepList}" var="stepData">
                                                <div class="stepContainer">
                                                    <g:if test="${stepData.parameterValues && !stepData.parameterValues.isEmpty()}">
                                                        <h5 style="margin-top: 10px;">
                                                            <div class="artifactsHeader">Parameter Values (${stepData.parameterValues?.size() ?: 0})</div>
                                                        </h5>
                                                        <ul class="stepArtifactsList">
                                                            <g:each in="${stepData.parameterValues}" var="paramValue">
                                                                <li class="stepArtifactItem">
                                                                    <assess:renderParameterSummary paramValue="${paramValue}" />
                                                                </li>
                                                            </g:each>
                                                        </ul>
                                                    </g:if>
                                                </div>
                                            </g:each>
                                        </g:if>
                                        <g:else>
                                            <h5 style="margin-top: 10px;">
                                                <b>TD Steps</b>
                                                (${notSatisfiedStepCount} not satisfied, ${notKnownStepCount} unanswered, and ${notApplicableStepCount} N/A)
                                            </h5>
                                        </g:else>
                                        <g:each in="${tdStepList}" var="stepData">
                                            <g:if test="${(stepData.result == AssessmentStepResult.Satisfied || stepData.result == AssessmentStepResult.Not_Applicable) && command.hideCompletedSteps}">
                                                <!-- Omitting step ${stepData.id} because it's Satisfied or N/A and we were told to hide those. -->
                                            </g:if>
                                            <g:else>
                                                <div class="stepContainer">
                                                    <div class="row containsStepStatusAndNameContainer">
                                                        <div class="col-md-1 text-right" style="font-size: 120%; padding-right: 0;">
                                                            <assess:assessmentStepResult result="${stepData.result}" />
                                                            <assess:assessmentStepAttachmentStatus step="${stepData}" />
                                                        </div>
                                                        <div class="col-md-11">
                                                            <h4>
                                                                ${stepData.step.name}
                                                            </h4>
                                                        </div>
                                                    </div>
                                                    <div class="row containsStepDescAndArtifactsContainer">
                                                        <div class="col-md-offset-1 col-md-11">
                                                            <b>Step Definition</b>
                                                            <div class="assessmentStepDescription">
                                                                ${stepData.step.description}
                                                            </div>
                                                            <g:if test="${stepData.step.artifacts && stepData.step.artifacts.size() > 0}">
                                                                <div class="assessmentStepArtifactDefinitions">
                                                                    <div class="requiredArtifactsHeader">Required Artifacts (${stepData.step.artifacts.size()})</div>
                                                                    <ul class="requiredArtifactsList">
                                                                        <g:each in="${stepData.step.artifacts.sort{it.name}}" var="artifactDef">
                                                                            <%
                                                                                boolean satisfied = false;
                                                                                stepData.artifacts.each{ artifact ->
                                                                                    if( artifact.requiredArtifact?.id == artifactDef.id )
                                                                                        satisfied = true;
                                                                                }

                                                                            %>
                                                                            <li class="requiredArtifactDefinition">
                                                                                <div class="requiredArtifactName">
                                                                                    <g:if test="${satisfied}">
                                                                                        <span class="glyphicon glyphicon-star" title="This artifact is satisfied."></span>
                                                                                    </g:if>
                                                                                    <g:else>
                                                                                        <span class="glyphicon glyphicon-star-empty" title="This artifact has not yet been satisfied."></span>
                                                                                    </g:else>
                                                                                    ${artifactDef.name}
                                                                                </div>
                                                                                <div class="requiredArtifactDesc">${artifactDef.description}</div>
                                                                            </li>
                                                                        </g:each>
                                                                    </ul>
                                                                </div>
                                                            </g:if>
                                                            <g:if test="${stepData.step.parameters && stepData.step.parameters.count{it.required} > 0}">
                                                                <div class="assessmentStepArtifactDefinitions">
                                                                    <div class="requiredArtifactsHeader">Required Parameters (${stepData.step.parameters.count{it.required}})</div>
                                                                    <ul class="requiredArtifactsList">
                                                                        <g:each in="${stepData.step.parameters.findAll{it.required}.sort()}" var="tdParam">
                                                                            <% boolean filled = stepData.isParameterFilled(tdParam) %>
                                                                            <li class="requiredArtifactDefinition">
                                                                                <div class="requiredArtifactName">
                                                                                    <g:if test="${filled}">
                                                                                        <span class="glyphicon glyphicon-star" title="This parameter is filled."></span>
                                                                                    </g:if>
                                                                                    <g:else>
                                                                                        <span class="glyphicon glyphicon-star-empty" title="This parameter has not yet been filled."></span>
                                                                                    </g:else>
                                                                                    ${tdParam.name}
                                                                                </div>
                                                                                <div class="requiredArtifactDesc">${tdParam.description}</div>
                                                                            </li>
                                                                        </g:each>
                                                                    </ul>
                                                                </div>
                                                            </g:if>
                                                        </div>
                                                    </div>
                                                    <div class="row containsAssessorFindingsContainer">
                                                        <div class="col-md-offset-1 col-md-11">
                                                            <b>Assessor Findings</b>
                                                            <div style="padding-left: 1em;">
                                                                <div class="statusSummary">
                                                                    <assess:assessmentStepResult result="${stepData.result}" />
                                                                    <assess:assessmentStepResultTextOnly result="${stepData.result}" />
                                                                    <g:if test="${stepData.lastResultUser}">
                                                                        (marked by ${stepData.lastResultUser?.contactInformation?.responder}, <g:formatDate date="${stepData.resultLastChangeDate}" format="yyyy-MM-dd" />)
                                                                    </g:if>
                                                                </div>
                                                                <div>
                                                                    <g:if test="${org.apache.commons.lang.StringUtils.isNotEmpty(stepData.assessorComment)}">
                                                                        Comment by ${stepData.assessorCommentUser?.contactInformation?.responder ?: 'Unknown'}:
                                                                        <pre style="white-space: pre-wrap; word-break: keep-all">${stepData.assessorComment}</pre>
                                                                    </g:if>
                                                                    <g:else>
                                                                        <pre style="width: 100%;"><em>No Comment</em></pre>
                                                                    </g:else>
                                                                </div>
                                                                <g:if test="${stepData.artifacts && !stepData.artifacts.isEmpty()}">
                                                                    <div class="artifactsHeader">Artifacts (${stepData.artifacts.size()})</div>
                                                                    <ul class="stepArtifactsList">
                                                                        <g:each in="${stepData.artifacts}" var="artifact">
                                                                            <li class="stepArtifactItem">
                                                                                <assess:renderArtifactSummary artifact="${artifact}" shortenComment="false" />
                                                                            </li>
                                                                        </g:each>
                                                                    </ul>
                                                                </g:if>
                                                                <g:if test="${stepData.parameterValues && !stepData.parameterValues.isEmpty()}">
                                                                    <div class="artifactsHeader">Parameter Values (${stepData.parameterValues?.size() ?: 0})</div>
                                                                    <ul class="stepArtifactsList">
                                                                        <g:each in="${stepData.parameterValues}" var="paramValue">
                                                                            <li class="stepArtifactItem">
                                                                                <assess:renderParameterSummary paramValue="${paramValue}" />
                                                                            </li>
                                                                        </g:each>
                                                                    </ul>
                                                                </g:if>
                                                            </div>
                                                        </div>
                                                    </div>
                                                    <g:if test="${stepData.step.substeps && !stepData.step.substeps.isEmpty()}">
                                                        <div class="row containsSubstepsContainer">
                                                            <div class="col-md-offset-1 col-md-11">
                                                                <b style="font-size: 120%;">Substep Information</b>

                                                                <ul style="list-style: none; margin: 0; padding: 0;">
                                                                    <g:each in="${stepData.step.substeps}" var="substepDef">
                                                                        <%
                                                                            AssessmentSubStepData substepData = null;
                                                                            for( AssessmentSubStepData cur : stepData.substeps ){
                                                                                if( cur.substep.id == substepDef.id ){
                                                                                    substepData = cur;
                                                                                    break;
                                                                                }
                                                                            }
                                                                        %>
                                                                        <li style="margin-bottom: 1em;">
                                                                            <div style="float: left; width: 30px; font-size: 120%; padding-top: 5px;">
                                                                                <assess:assessmentStepResult result="${substepData?.result ?: AssessmentStepResult.Not_Known}" />
                                                                            </div>
                                                                            <div style="margin-left: 30px;">
                                                                                <div style="font-weight: bold;">${substepDef.name}</div>
                                                                                <div style="font-size: 90%;">${substepDef.description}</div>
                                                                            </div>
                                                                            <g:if test="${substepData?.assessorComment}">
                                                                                <div style="clear: both; margin-left: 30px;">
                                                                                    Comment by ${substepData?.assessorCommentUser?.contactInformation?.responder ?: 'Unknown'}:
                                                                                    <pre style="width: 100%;">${substepData?.assessorComment}</pre>
                                                                                </div>
                                                                            </g:if>
                                                                        </li>
                                                                    </g:each>
                                                                </ul>

                                                            </div>
                                                        </div>
                                                    </g:if>
                                                </div>
                                            </g:else>
                                        </g:each>
                                    </g:each>
                                </div>

                            </div>
                        </g:each>
                        </div>
                    </div>
                </g:else>
            </g:each>
        </div>


    </div>


</body>
</html>

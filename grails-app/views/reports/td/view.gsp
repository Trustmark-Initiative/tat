<%@ page import="nstic.web.td.AssessmentStep; nstic.web.assessment.*; org.apache.commons.io.FileUtils; nstic.web.*;" defaultCodec="none"  %>
<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="report-base"/>
    <title>Trustmark Definition Report</title>
    <style type="text/css">
        .reportSection {
            margin-top: 50px;
        }

        h1, h2, h3, h4, h5 { font-weight: bold; }

        .orgSummaryNameCol { width: 100px; }
        .orgSummaryStatusCol { width: 75px; text-align: center; }
        .orgSummaryLastActivityCol { width: 110px; }
        .orgSummaryCommentCol { width: auto; }

        .orgSummaryStatusColHeader {text-align: left;}


        .ruleNumCol { text-align: center; width: 75px; }
        .orgIdResultStatusCol { text-align: center; width: auto; }


        .assStepDefContainer {
            margin-left: 1em;
        }

        .requiredArtifactsList {
            list-style: none;
            margin: 0;
            margin-left: 1em;
            padding: 0;
        }

        .requiredArtifactName {
            font-style: italic;
        }

        .orgRuleSummaryContainer {
            margin-left: 1em;
        }
        .orgAssessorSummaryHeader {
            margin-top: 2em;
        }

        .stepArtifactsList {
            list-style: none;
            margin: 0;
            margin-left: 1em;
            padding: 0;
        }

    </style>
</head>
<body>


    <div class="reportContent" style="margin-top: 25px;">
        <h3>Trustmark Definition Report: ${trustmarkDefinition.name}, ${trustmarkDefinition.tdVersion}</h3>
        <div>
            Generated <g:formatDate date="${Calendar.getInstance().getTime()}" format="MM/dd/yyyy" />,
            By <sec:username /> <br/>
            Including Assessment Data from <g:formatDate date="${startDate}" format="MM/dd/yyyy" />
            to <g:formatDate date="${endDate}" format="MM/dd/yyyy" />,
            resulting in ${assessments.size()} assessments.
        </div>

        <!-- Organization Summary Table -->
        <div class="reportSection">
            <h4>Organization Summary</h4>
            <tmpl:/templates/displayAssessmentStatusIconLegend />
            <table class="table table-bordered table-condensed table-striped">
                <thead>
                    <th class="orgSummaryNameCol orgSummaryNameColHeader">Name</th>
                    <th class="orgSummaryStatusCol orgSummaryStatusColHeader">Status</th>
                    <th class="orgSummaryLastActivityCol orgSummaryLastActivityColHeader">Last Activity</th>
                    <th class="orgSummaryCommentCol orgSummaryCommentColHeader">Assessment Comment</th>
                </thead>
                <tbody>
                    <g:each in="${organizations}" var="org">
                        <g:if test="${orgTdMap.containsKey(org.id)}">
                            <g:set var="assessment" value="${orgTdMap.get(org.id)?.get(0)}" />
                            <tr>
                                <td class="orgSummaryNameCol">${org.identifier}</td>
                                <td class="orgSummaryStatusCol">
                                    <tmpl:/templates/displayAssessmentStatusIcon assessment="${assessment}" />
                                    <g:if test="${!assessment.getIsComplete()}">
                                        <div class="percentSatisifedContainer">
                                            ${assessment.getPercentStepsSatisfied()}%
                                        </div>
                                    </g:if>
                                </td>
                                <td class="orgSummaryLastActivityCol">
                                    <% int daysAgo = (int) ((System.currentTimeMillis() - assessment.logg.getMostRecentEntry().dateCreated.getTime()) / (24 * 60 * 60 * 1000d)); %>
                                    <g:if test="${daysAgo < 30}">
                                        <span style="color: #${nstic.assessment.ColorPalette.SUCCESS_TEXT.toString()}; font-weight: bold;">${daysAgo}</span>
                                    </g:if>
                                    <g:elseif test="${daysAgo < 60}">
                                        <span style="color: #${nstic.assessment.ColorPalette.WARNING_TEXT.toString()}; font-weight: bold;">${daysAgo}</span>
                                    </g:elseif>
                                    <g:else>
                                        <span style="color: #${nstic.assessment.ColorPalette.ERROR_TEXT.toString()}; font-weight: bold;">${daysAgo}</span>
                                    </g:else>
                                    Days Ago
                                </td>
                                <td class="orgSummaryCommentCol">
                                    <g:set var="lastCommentWithAssessor" value="${assessment.getLastGlobalCommentWithAuthor()}" />
                                    <g:if test="${lastCommentWithAssessor && lastCommentWithAssessor.comment && lastCommentWithAssessor.comment.trim().length() > 0}">
                                        ${lastCommentWithAssessor.user?.contactInformation?.responder} wrote:
                                        ${lastCommentWithAssessor.comment}
                                    </g:if>
                                    <g:else>
                                        <em>Empty</em>
                                    </g:else>
                                </td>
                            </tr>
                        </g:if>
                        <g:else>
                            <tr>
                                <td class="orgSummaryNameCol">
                                    ${org.identifier}
                                </td>
                                <td class="orgSummaryStatusCol">
                                    <span class="glyphicon glyphicon-minus"></span>
                                </td>
                                <td class="orgSummaryLastActivityCol">
                                    ---
                                </td>
                                <td class="orgSummaryCommentCol">
                                    ---
                                </td>
                            </tr>
                        </g:else>
                    </g:each>
                </tbody>
            </table>
        </div>

        <!-- Rule Result Summary per Organization Table -->
        <div class="reportSection">
            <h4>Rule Result Summary Per Organization</h4>
            <assess:assessmentStepStatusLegend />, <span class="glyphicon glyphicon-minus"></span> No Assessment
            <table class="table table-bordered table-condensed table-striped">
                <thead>
                    <th class="ruleNumCol ruleNumColHeader">Rule</th>
                    <g:each in="${organizations}" var="org">
                        <th class="orgIdResultStatusCol orgIdResultStatusColHeader">${org.identifier}</th>
                    </g:each>
                </thead>
                <tbody>
                    <g:each in="${trustmarkDefinition.getSortedSteps()}" var="assessmentStep">
                        <tr>
                            <td class="ruleNumCol">
                                <a href="#step_${assessmentStep.stepNumber}_detail">
                                    ${assessmentStep.stepNumber}
                                    <span class="glyphicon glyphicon-link"></span>
                                </a>
                            </td>
                            <g:each in="${organizations}" var="org">
                                <g:if test="${orgTdMap.containsKey(org.id)}">
                                    <g:set var="assessment" value="${orgTdMap.get(org.id)?.get(0)}" />
                                    <%
                                        nstic.web.assessment.AssessmentStepData orgMatchingStep =
                                            assessment.getStepDataByNumber(assessmentStep.stepNumber);
                                    %>
                                    <td class="orgIdResultStatusCol">
                                        <assess:assessmentStepResult result="${orgMatchingStep.result}" />
                                    </td>
                                </g:if>
                                <g:else>
                                    <td class="orgIdResultStatusCol">
                                        <span class="glyphicon glyphicon-minus"></span>
                                    </td>
                                </g:else>
                            </g:each>
                        </tr>
                    </g:each>
                </tbody>
            </table>
        </div>

        <!-- Details for Each Rule Per Organization -->
        <div class="reportSection">
            <h4>Details For Each Rule of ${trustmarkDefinition.uniqueDisplayName}</h4>
            <g:each in="${trustmarkDefinition.sortedSteps}" var="assessmentStepDef">
                <a name="step_${assessmentStepDef.stepNumber}_detail"></a>
                <div class="reportSection">
                    <b>Step Definition #${assessmentStepDef.stepNumber}</b>
                    <div class="assStepDefContainer">
                        <h5>
                            ${assessmentStepDef.name}
                        </h5>
                        <div class="assStepDesc">
                            ${assessmentStepDef.description}
                        </div>
                        <div class="assStepArtifactsContainer">
                            <g:if test="${assessmentStepDef.artifacts && assessmentStepDef.artifacts.size() > 0}">
                                <div class="assessmentStepArtifactDefinitions">
                                    <div class="requiredArtifactsHeader">Required Artifacts (${assessmentStepDef.artifacts.size()})</div>
                                    <ul class="requiredArtifactsList">
                                        <g:each in="${assessmentStepDef.artifacts}" var="artifactDef">
                                            <li class="requiredArtifactDefinition">
                                                <div class="requiredArtifactName">${artifactDef.name}</div>
                                                <div class="requiredArtifactDesc">${artifactDef.description}</div>
                                            </li>
                                        </g:each>
                                    </ul>
                                </div>
                            </g:if>
                        </div>
                    </div>

                    <g:each in="${organizations}" var="org">
                        <g:if test="${orgTdMap.containsKey(org.id)}">
                            <g:set var="assessment" value="${orgTdMap.get(org.id)?.get(0)}" />
                            <%
                                nstic.web.assessment.AssessmentStepData orgMatchingStep =
                                        assessment.getStepDataByNumber(assessmentStepDef.stepNumber)
                            %>
                            <h5 class="orgAssessorSummaryHeader">
                                ${org.identifier} Assessor Summary
                                <small>
                                    (from <g:link target="_blank" controller="assessment" action="view" id="${assessment.id}">assessment #${assessment.id}</g:link>
                                    <tmpl:/templates/displayAssessmentStatusIcon assessment="${assessment}" />)
                                </small>
                            </h5>
                            <div class="orgRuleSummaryContainer">
                                <div class="statusSummary">
                                    User ${orgMatchingStep.lastResultUser?.contactInformation?.responder ?: 'Unknown'} marked as:
                                    <assess:assessmentStepResult result="${orgMatchingStep.result}" />
                                    <assess:assessmentStepResultTextOnly result="${orgMatchingStep.result}" />
                                </div>
                                <div>
                                    <g:if test="${org.apache.commons.lang.StringUtils.isNotEmpty(orgMatchingStep.assessorComment)}">
                                        Comment by ${orgMatchingStep.assessorCommentUser?.contactInformation?.responder ?: 'Unknown'}:
                                        <em>${orgMatchingStep.getShortenedComment()}</em>
                                    </g:if>
                                    <g:else>
                                        <em>No Comment</em>
                                    </g:else>
                                </div>
                                <g:if test="${orgMatchingStep.artifacts && !orgMatchingStep.artifacts.isEmpty()}">
                                    <div class="artifactsHeader">Artifacts (${orgMatchingStep.artifacts.size()})</div>
                                    <ul class="stepArtifactsList">
                                        <g:each in="${orgMatchingStep.artifacts}" var="artifact">
                                            <li class="stepArtifactItem">
                                                <assess:renderArtifactSummary artifact="${artifact}" />
                                            </li>
                                        </g:each>
                                    </ul>
                                </g:if>


                            </div>

                        </g:if>
                        <g:else>
                            <!-- For now, we are ignoring organization ${org.identifier} since they have not been assessed against ${trustmarkDefinition.uniqueDisplayName}. -->
                        </g:else>
                    </g:each>



                </div>
                <hr />
            </g:each>


    </div>

</body>
</html>

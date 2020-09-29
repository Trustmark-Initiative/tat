<%@ page import="nstic.web.User; nstic.web.Role" %>
<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main"/>
		<title>List Assessments</title>

        <style type="text/css">
            .idCol { width: 30px; }
            .nameCol { width: auto; }
            .statusCol {width: 50px; text-align: center;}
            .hasTrustmarkCol {width: 50px; text-align: center;}
            .createdByCol {width: 150px;}
            .currentCreatedDateCol {width: 60px; }
            .ageCol {width: 60px; }
            .assessorCol {width: 150px;}
            .orgNameCol {width: auto;}
        </style>
	</head>
	<body>
        %{--<ol class="breadcrumb">--}%
            %{--<li class="active">Home</li>--}%
        %{--</ol>--}%

        <h1>Assessments <small>(${assessmentsCountTotal} total)</small></h1>
        <div class="pageSubsection">

        </div>

        <div id="messageContainer">
            <g:if test="${flash.message}">
                <div style="margin-top: 2em;" class="alert alert-info">
                    ${flash.message}
                </div>
            </g:if>
        </div>

        <div class="pageContent">
            <table class="table table-striped table-bordered table-condensed">
                <thead>
                    <tr>
                        <g:sortableColumn property="assessmentName" title="Name" class="nameCol" />
                        <g:sortableColumn property="status" title="Status" class="statusCol" />
                        <th class="hasTrustmarkCol">TM?</th>
                        <g:sortableColumn property="createdBy.username" title="Created By" class="createdByCol" />
                        <g:sortableColumn property="dateCreated" title="Create Date" class="currentCreatedDateCol" />
                        <th class="ageCol" title="How long ago was this assessment last performed?">Age</th>
                        <g:sortableColumn property="assignedTo.username" title="Assessor" class="assessorCol" />
                        <g:sortableColumn property="assessedOrganization.name" title="Organization" class="orgNameCol" />
                    </tr>
                </thead>
                <tbody>
                    <g:if test="${assessments && assessments.size() > 0}">
                        <g:each in="${assessments}" var="assessment">
                            <tr>
                                <td class="nameCol">
                                    <g:link controller="assessment" action="view" id="${assessment.id}">
                                        ${assessment.assessmentName}
                                    </g:link>
                                </td>
                                <td class="statusCol">
                                    <tmpl:/templates/displayAssessmentStatusIcon assessment="${assessment}" />
                                </td>
                                <td class="hasTrustmarkCol">
                                    <g:set var="trustmarkList" value="${nstic.web.assessment.Trustmark.findAllByAssessment(assessment)}" />
                                    <g:if test="${trustmarkList && trustmarkList.size() > 0}">
                                        <span class="glyphicon glyphicon-ok" style="color: #3c763d;" title="${trustmarkList.size()} Trustmarks"></span>
                                    </g:if>
                                    <g:else>
                                        <span class="glyphicon glyphicon-remove" style="color: #a94442;" title="No Trustmarks"></span>
                                    </g:else>
                                </td>
                                <td class="createdByCol">
                                    ${assessment.createdBy?.contactInformation?.responder}
                                </td>
                                <td class="currentCreatedDateCol">
                                    <g:formatDate date="${assessment.dateCreated}" format="yyyy-MM-dd" />
                                </td>
                                <td class="ageCol">
                                    <%
                                        long logTime = assessment.logg?.getMostRecentEntry()?.dateCreated?.getTime() ?: 0;
                                        long age = System.currentTimeMillis() - logTime;
                                        int ageInDays = (int) (age / (1000*60*60*24));
                                    %>
                                    <g:if test="${ageInDays < (1000*60*60*24)}">
                                        <em>Current</em>
                                    </g:if>
                                    <g:elseif test="${ageInDays < 2*(1000*60*60*24)}">
                                        1 Day
                                    </g:elseif>
                                    <g:else>
                                        <%= ageInDays %> Days
                                    </g:else>
                                </td>
                                <td class="assessorCol">
                                    ${assessment.assignedTo?.contactInformation?.responder}
                                </td>
                                <td class="orgNameCol">
                                    ${assessment.assessedOrganization?.name}
                                </td>
                            </tr>
                        </g:each>
                    </g:if>
                    <g:else>
                        <tr>
                            <td colspan="8"><em>There are no assessments</em></td>
                        </tr>
                    </g:else>
                </tbody>
            </table>
            <div class="row" style="margin-bottom: 2em;">
                <div class="col-md-6">
                    <a href="${createLink(controller:'assessment', action:'create')}" class="btn btn-primary">New</a>
                </div><div class="col-md-6" style="text-align: right">
                    <g:paginate total="${assessmentsCountTotal}" />
                </div>
            </div>

        </div>


	</body>
</html>

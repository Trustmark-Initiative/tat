<%@ page import="nstic.web.assessment.Assessment" %>
<!-- This page meant to be returned as a snippet of HTML -->
<table class="table table-condensed table-striped table-bordered">
    <thead>
        <tr>
            <th style="width: auto;">Name</th>
            <th style="width: 5%;">Status</th>
            <th style="width: 20%;">Organization</th>
            <th style="width: 12%;">Change Date</th>
            <th style="width: auto;">Latest Entry Title</th>
        </tr>
    </thead>
    <tbody>
        <g:if test="${!assessments.isEmpty()}">
            <g:each in="${(List<Assessment>)assessments}" var="assessment">
                <tr>
                    <td>
                        <a href="${createLink(controller:'assessment', action: 'view', id: assessment.id)}">
                            <span style="min-width: 25px;">${assessment.assessmentName}</span>
                        </a>
                    </td>
                    <td style="text-align: center;">
                        <tmpl:/templates/displayAssessmentStatusIcon assessment="${assessment}" />
                    </td>
                    <td>${assessment.assessedOrganization.name}</td>
                    <td>
                        <g:formatDate date="${assessmentLastLogEntryMap.get(assessment)?.dateCreated}" format="yyyy-MM-dd HH:mm" />
                    </td>
                    <td>
                        ${assessmentLastLogEntryMap.get(assessment)?.title}
                    </td>
                </tr>
            </g:each>
        </g:if>
        <g:else>
            <tr>
                <td colspan="5"><em>There are no assessments.</em></td>
            </tr>
        </g:else>
    </tbody>
</table>
<%@ page import="nstic.assessment.ColorPalette" %>
<g:if test="${assessmentList != null && assessmentList.size() > 1}">

    <g:each in="${assessmentList}" var="assessment">
        <assess:assessmentStatusIcon status="${assessment.status}" />
    </g:each>

</g:if><g:else>
    <g:if test="${assessmentList != null && assessment == null}">
        <g:set var="assessment" value="${assessmentList.get(0)}" />
    </g:if>

    <assess:assessmentStatusIcon status="${assessment.status}" />

</g:else>

<g:if test="${assessmentList != null && assessmentList.size() > 1}">

    <span class="errorText">MORE THAN 1 ASSESEMENT!?!</span>

</g:if><g:else>
    <g:if test="${assessmentList != null && assessment == null}">
        <g:set var="assessment" value="${assessmentList.get(0)}" />
    </g:if>

    <g:if test="${assessment.status == nstic.web.assessment.AssessmentStatus.SUCCESS}">
        <b>Successful</b> - This assessment has satisfied all requirements, and is eligible for a Trustmark to be issued.
    </g:if>
    <g:elseif test="${assessment.status == nstic.web.assessment.AssessmentStatus.FAILED}">
        <b>Failed</b> - This assessment has failed to satisfy one or more requirements, and is NOT eligible for a Trustmark to be issued.
    </g:elseif>
    <g:elseif test="${assessment.status == nstic.web.assessment.AssessmentStatus.ABORTED}">
        <b>Aborted</b> - This assessment was aborted by the last assessor.  You should consider it incomplete and failed.
    </g:elseif>
    <g:elseif test="${assessment.status == nstic.web.assessment.AssessmentStatus.PENDING_ASSESSOR}">
        <b>Pending Assessor</b> - The previous assessor has placed this assessment on hold, presumably temporarily.
    </g:elseif>
    <g:elseif test="${assessment.status == nstic.web.assessment.AssessmentStatus.PENDING_ASSESSED}">
        <b>Pending Assessed</b> - The previous assessor is waiting on the assessed organization to do something before this assessment can continue.
    </g:elseif>
    <g:elseif test="${assessment.status == nstic.web.assessment.AssessmentStatus.IN_PROGRESS}">
        <b>In Progress</b> - This assessment is currently being assessed by the assessor.
    </g:elseif>
    <g:elseif test="${assessment.status == nstic.web.assessment.AssessmentStatus.WAITING}">
        <b>Waiting</b> - This assessment has been created, but no work on assessing it has started.
    </g:elseif>
    <g:else>
        <b>Unknown</b> - The status '${assessment.status}' is not known.
    </g:else>
</g:else>

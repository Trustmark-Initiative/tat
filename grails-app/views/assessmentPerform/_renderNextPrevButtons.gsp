
<div class="row" style="margin-bottom: 1em;">
    <div class="col-md-6">
        <g:if test="${prevStep != null}">
            <a id="previousStepLink" class="btn btn-default" href="${createLink(controller: 'assessmentPerform', action: 'view', id: assessment.id, params: [stepDataId: prevStep.id])}">
                &laquo; Previous
            </a>
        </g:if>
        <g:else>
            <a href="#" id="previousStepLink" class="btn btn-default disabled">
                &laquo; Previous
            </a>
        </g:else>

        <g:if test="${prevUnknownStep != null}">
            <a id="previousUnknownStepLink" class="btn btn-default" href="${createLink(controller: 'assessmentPerform', action: 'view', id: assessment.id, params: [stepDataId: prevUnknownStep.id])}">
                &laquo; Previous Unkown
            </a>
        </g:if>
        <g:else>
            <a href="#" id="previousUnknownStepLink" class="btn btn-default disabled">
                &laquo; Previous Unkown
            </a>
        </g:else>
    </div>

    <div class="col-md-6" style="text-align: right;">
        <g:if test="${nextUnknownStep != null}">
            <a id="nextUnknownStepLink" class="btn btn-default" href="${createLink(controller: 'assessmentPerform', action: 'view', id: assessment.id, params: [stepDataId: nextUnknownStep.id])}">
                Next Unknown &raquo;
            </a>
        </g:if>
        <g:else>
            <a href="#" id="nextUnknownStepLink" class="btn btn-default disabled">
                Next Unknown &raquo;
            </a>
        </g:else>

        <g:if test="${nextStep != null}">
            <a id="nextStepLink" class="btn btn-default" href="${createLink(controller: 'assessmentPerform', action: 'view', id: assessment.id, params: [stepDataId: nextStep.id])}">
                Next &raquo;
            </a>
        </g:if>
        <g:else>
            <a href="#" id="nextStepLink" class="btn btn-default disabled">
                Next &raquo;
            </a>
        </g:else>
    </div>
</div>
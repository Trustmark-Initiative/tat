
<div class="row" style="margin-bottom: 1em;">
    <div class="col-md-6">
        <g:if test="${prevStep != null}">
            <a id="previousStepLink" class="btn btn-default" href="${createLink(controller: 'assessmentPerform', action: 'view', id: assessment.id, params: [stepDataId: prevStep.id])}">
                &laquo; Previous
            </a>
        </g:if><g:else>
            <a href="#" id="previousStepLink" class="btn btn-default disabled">
                &laquo; Previous
            </a>
        </g:else>
    </div>

    <div class="col-md-6" style="text-align: right;">
        <g:if test="${nextStep != null}">
            <a id="nextStepLink" class="btn btn-default" href="${createLink(controller: 'assessmentPerform', action: 'view', id: assessment.id, params: [stepDataId: nextStep.id])}">
                Next &raquo;
            </a>
        </g:if><g:else>
            <a href="#" id="nextStepLink" class="btn btn-default disabled">
                Next &raquo;
            </a>
        </g:else>
    </div>
</div>
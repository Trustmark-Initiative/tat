<%-- This page assumes it will be rendered inside of something with twitter bootstrap enabled. --%>
<div>
    <div>
        All Questions Answered
        <g:if test="${allStepsHaveAnswer}">
            <span class="glyphicon glyphicon-ok-sign" style="color: darkgreen;" title="All questions have an answer"></span>
        </g:if><g:else>
            <span class="glyphicon glyphicon-remove-sign" style="color: darkred;" title="Some questions are not answered"></span>
        </g:else>
    </div>

    <div>
        Required Artifacts
        <g:if test="${allRequiredArtifactsSatisfied}">
            <span class="glyphicon glyphicon-ok-sign" style="color: darkgreen;" title="All required artifacts are satisfied."></span>
        </g:if><g:else>
            <span class="glyphicon glyphicon-remove-sign" style="color: darkred;" title="Some required artifacts are missing."></span>
        </g:else>
    </div>

    <div>
        Required Parameters
        <g:if test="${allRequiredParametersFilled}">
            <span class="glyphicon glyphicon-ok-sign" style="color: darkgreen;" title="All required parameters are filled."></span>
        </g:if><g:else>
            <span class="glyphicon glyphicon-remove-sign" style="color: darkred;" title="Some required parameters are unfilled."></span>
        </g:else>
    </div>

</div>
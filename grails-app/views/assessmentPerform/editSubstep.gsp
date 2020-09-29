<%@ page import="nstic.web.assessment.AssessmentSubStepData; nstic.web.assessment.AssessmentStepResult" defaultCodec="" %>
<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main"/>
		<title>Edit Substep #${substep.id}</title>

        <style type="text/css">

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
        <li>
            <g:link controller="assessmentPerform" action="view" id="${assessment.id}"
                    params="[stepNumber: stepData.step.stepNumber]">
                Perform Assessment #${assessment.id}, Step ${stepData.step.stepNumber}
            </g:link>
        </li>
        <li class="active">
            Edit Substep #${substep.id}
        </li>
    </ol>

    <g:if test="${flash.error}">
        <div class="alert alert-danger">${flash.error}</div>
    </g:if>
    <g:if test="${flash.message}">
        <div class="alert alert-success">${flash.message}</div>
    </g:if>

        <h1>
            Assessment #${command?.assessmentId}, Step #${stepData.step.stepNumber} - Edit Substep #${substep.id}:
            <br/>
            ${substep.name}
        </h1>
        <div class="pageSubsection text-muted">
            Nam facilisis, quam eget faucibus scelerisque, nulla justo auctor lacus, eu laoreet nibh odio sed eros. Sed aliquam mattis ipsum vitae pellentesque. Quisque congue ut nunc quis consequat. Vivamus accumsan accumsan varius. Fusce porttitor consequat risus, aliquam tempus lectus volutpat vitae. Morbi consequat nisi erat, vitae elementum est dapibus gravida. In ut eleifend risus. Mauris tristique quam eget tortor pharetra ullamcorper. Nulla ac massa pellentesque, blandit dolor ac, fermentum ante.
        </div>


        <div class="pageContent">
            <div class="row" style="margin-bottom: 2em;">
                <div class="col-md-6">
                    <h4>Step #${stepData.step.stepNumber}: ${stepData.step.name}</h4>
                    <div>
                        ${stepData.step.description}
                    </div>
                </div>
                <div class="col-md-6">
                    <h4>Other Substeps Include:</h4>
                    <ul>
                        <%
                            List<AssessmentSubStepData> otherSteps = []
                            stepData.substeps?.each { otherPotentialStep ->
                                if( otherPotentialStep.id != substep.id ){
                                    otherSteps.add(otherPotentialStep)
                                }
                            }
                            Collections.sort(otherSteps, { AssessmentSubStepData s1, AssessmentSubStepData s2 ->
                                return s1.substep.name.compareToIgnoreCase(s2.substep.name)
                            } as Comparator)
                        %>
                        <g:if test="${otherSteps && !otherSteps.isEmpty()}">
                            <g:each in="${otherSteps}" var="substepData">
                                <li>
                                    ${substepData.substep.name}
                                </li>
                            </g:each>
                        </g:if><g:else>
                            <li>
                                <em>There are no other substeps.</em>
                            </li>
                        </g:else>
                    </ul>

                </div>
            </div>


            <form class="form-horizontal" method="POST"
                  action="${createLink(controller:'assessmentPerform', action: 'updateSubstep', id: command.assessmentId, params: [stepNumber: stepData.step.stepNumber, substepId: substep.id])}">
                <g:hiddenField name="substepId" id="substepId" value="${command?.substepId}" />
                <g:hiddenField name="assessmentId" id="assessmentId" value="${command?.assessmentId}" />
                <g:hiddenField name="stepDataId" id="stepDataId" value="${command?.stepDataId}" />

                <div class="form-group">
                    <label for="name" class="col-sm-2 control-label">
                        Name
                    </label>
                    <div class="col-sm-10">
                        <g:textField name="name" id="name" class="form-control" value="${command?.name}" />
                        <span class="help-block">The name of this substep</span>
                    </div>
                </div>


                <div class="form-group">
                    <label for="description" class="col-sm-2 control-label">
                        Description
                    </label>
                    <div class="col-sm-10">
                        <g:textArea name="description" id="description" style="min-height: 150px;" class="form-control" value="${command?.description}" />
                        <span class="help-block">The description (and subquestion) this substep is targeted to answer.</span>
                    </div>
                </div>

                <hr />

                <div class="form-group">
                    <label for="result" class="col-sm-2 control-label">
                        Result
                    </label>
                    <div class="col-sm-10">
                        <g:hiddenField name="result" id="result" value="${command?.result?.toString() ?: ''}" />
                        <ul class="nav nav-pills">
                            <li class="resultPill" id="Not_Known_li">
                                <a href="javascript:setResult('Not_Known')">
                                    <assess:assessmentStepResult result="${AssessmentStepResult.Not_Known}"/>
                                    Unknown
                                </a>
                            </li>
                            <li class="resultPill" id="Satisfied_li">
                                <a href="javascript:setResult('Satisfied')">
                                    <assess:assessmentStepResult result="${AssessmentStepResult.Satisfied}"/>
                                    Satisfied
                                </a>
                            </li>
                            <li class="resultPill" id="Not_Satisfied_li">
                                <a href="javascript:setResult('Not_Satisfied')">
                                    <assess:assessmentStepResult result="${AssessmentStepResult.Not_Satisfied}"/>
                                    Not Satisfied
                                </a>
                            </li>
                            <li class="resultPill" id="Not_Applicable_li">
                                <a href="javascript:setResult('Not_Applicable')">
                                    <assess:assessmentStepResult result="${AssessmentStepResult.Not_Applicable}"/>
                                    Not Applicable
                                </a>
                            </li>
                        </ul>

                    </div>
                </div>

                <div class="form-group">
                    <label for="assessorComment" class="col-sm-2 control-label">
                        Assessor Comment
                    </label>
                    <div class="col-sm-10">
                        <g:textArea name="assessorComment" id="assessorComment" style="min-height: 150px;" class="form-control" value="${command?.assessorComment}" />
                        <span class="help-block">The assessor's findings for this substep.</span>
                    </div>
                </div>

                <div class="form-group">
                    <div class="col-sm-offset-2 col-sm-10">
                        <button id="submitButton" type="submit" class="btn btn-primary">
                            <span class="glyphicon glyphicon-save"></span>
                            Update
                        </button>
                        <a href="${createLink(controller:'assessmentPerform', action: 'view', id: assessment.id, params: [stepNumber: stepData.step.stepNumber])}" class="btn btn-default">Cancel</a>
                    </div>
                </div>

            </form>
        </div>

        <script type="text/javascript">
            <g:if test="${command.result}">
            $(document).ready(function(){
                setResult('${command.result.toString()}');
            })
            </g:if>


            function setResult(result) {
                $('.resultPill').removeClass('active');
                $('#result').val(result);
                $('#'+result+"_li").addClass('active');
            }

        </script>

	</body>
</html>

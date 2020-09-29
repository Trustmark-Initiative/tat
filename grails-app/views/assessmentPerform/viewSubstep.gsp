<%@ page import="nstic.web.assessment.*" defaultCodec="" %>
<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main"/>
		<title>View Substep</title>

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
            View Substep #${substep.id}
        </li>
    </ol>

    <g:if test="${flash.error}">
        <div class="alert alert-danger">${flash.error}</div>
    </g:if>
    <g:if test="${flash.message}">
        <div class="alert alert-success">${flash.message}</div>
    </g:if>

        <h1>Assessment #${command?.assessmentId}, Step #${stepData.step.stepNumber}: View Substep #${substep.id}</h1>
        <div class="pageSubsection text-muted">

        </div>


        <div class="pageContent">
            <h3>Substep Information</h3>
            <table class="table table-striped table-condensed table-bordered">
                <tr>
                    <th style="width: 200px;">Name</th>
                    <td>${substep.substep.name}</td>
                </tr>
                <tr>
                    <th style="width: 200px;">Description</th>
                    <td>${substep.substep.description}</td>
                </tr>
            </table>
        </div>
        <div class="pageContent">
            <h3>Substep Assessment Information</h3>
            <table class="table table-striped table-condensed table-bordered">
                <tr>
                    <th style="width: 200px;">Result</th>
                    <td>
                        <assess:assessmentStepResult result="${substep.result.toString()}" />
                    </td>
                </tr>
                <tr>
                    <th style="width: 200px;">Assessor Comment</th>
                    <td>${substep.assessorComment}</td>
                </tr>
            </table>
        </div>
        <div class="pageContent">
           <a href="${createLink(controller:'assessmentPerform', action: 'view', id: assessment.id, params: [stepNumber: stepData.step.stepNumber])}"
                    class="btn btn-default">
               Perform Assessment
           </a>
        </div>

	</body>
</html>

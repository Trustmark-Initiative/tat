<%@page import="nstic.web.assessment.AssessmentStepResult" defaultCodec="none" %>

<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>Complete Assessment ${assessment.id}</title>

    <style type="text/css">

    </style>
</head>
<body>

<div class="row">
    <div class="col-md-9">
        <h1>Complete Assessment ${assessment.id}</h1>
        <div class="pageDescription">
            On this page, you change the status of assessment ${assessment.id} from ${assessment.status} to ${nstic.web.assessment.AssessmentStatus.COMPLETE}.
        </div>
    </div>
    <div class="col-md-3" style="text-align: right;">

    </div>
</div>

<div class="row" style="margin-top: 2em;">
    <div class="col-md-12">
        <g:form class="form-horizontal" name="createArtifactForm" method="POST" action="markAssessmentComplete">
            <g:hiddenField name="id" id="id" value="${assessment?.id}" />
            <g:hiddenField name="markAssessmentComplete" id="markAssessmentComplete" value="TRUE" />

            <div class="form-group">
                <label for="comment" class="col-sm-2 control-label">Additional Comments</label>
                <div class="col-sm-10">
                    <g:textArea name="comment" id="comment" style="height: 250px;" class="form-control" value="${assessment?.comment}" />
                </div>
            </div>

            <div class="form-group">
                <div class="col-sm-offset-2 col-sm-10">
                    <button type="submit" class="btn btn-primary">Save</button>
                    <a href="${createLink(controller:'assessmentPerform', action: 'view', id: assessment.id)}" class="btn btn-default">Cancel</a>
                </div>
            </div>
        </g:form>
    </div>
</div>

</body>
</html>

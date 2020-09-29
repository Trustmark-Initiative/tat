<%@ page import="nstic.util.AssessmentToolProperties" %>
<%@ page import="nstic.TATPropertiesHolder" %>
<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main"/>
		<title>Edit Assessment Name</title>

        <style type="text/css"></style>
	</head>
	<body>

        <div class="row">
            <div class="col-md-10">
                <h1>Edit Assessment Name</h1>
                <div class="pageSubsection">
                    This page allows you to edit the assessment's name.
                </div>
            </div>
        </div>

        <g:if test="${flash.error}">
            <div class="alert alert-danger">${flash.error}</div>
        </g:if>
        <g:if test="${command?.hasErrors()}">
            <div class="alert alert-danger" style="margin-top: 2em;">
                <b>Cannot edit assessment name due to the following errors:</b>
                <div>
                    <ul>
                        <g:each in="${command.errors.allErrors}" var="error">
                            <li>
                                <g:message error="${error}" />
                            </li>
                        </g:each>
                    </ul>
                </div>
            </div>
        </g:if>

        <div class="pageContent">
            <g:form action="updateName" method="POST" class="form-horizontal" onsubmit="return validateForm();">

                <input type="hidden" name="assessmentId" value="${command?.assessmentId}"/>

                <fieldset>
                    <div class="form-group">
                        <label for="assessmentName" class="col-md-1 control-label">Name</label>
                        <div class="col-md-11">
                            <input type="text" id="assessmentName" name="assessmentName" class="form-control" placeholder="Assessment Name" autocomplete="off" value="${command?.assessmentName}" />
                        </div>
                    </div>
                </fieldset>


                <div class="form-group" style="margin-top: 2em;">
                    <div class="col-sm-offset-1 col-sm-10">
                        <button type="submit" class="btn btn-default">Save</button>
                    </div>
                </div>

            </g:form>
        </div>
	</body>
</html>

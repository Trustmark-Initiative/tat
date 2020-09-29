<%@ page import="nstic.web.User; nstic.web.Role" %>
<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main"/>
		<title>TIP Report Form</title>

        <style type="text/css">

        </style>
	</head>
	<body>
        %{--<ol class="breadcrumb">--}%
            %{--<li class="active">Home</li>--}%
        %{--</ol>--}%

        <h1><span class="glyphicon glyphicon-home"></span> Trust Interoperability Profile Report</h1>
        <div class="pageSubsection">
            After picking a single Trust Interoperabilty Profile (TIP) and optionally a date range and organziation set,
            this report will tell you which organizations meet this TIP (based only on THIS assessment tool's knowledge
            of trustmarks), and the status of all Assessments related to this TIP.
        </div>

        <div id="messageContainer">
            <g:if test="${flash.message}">
                <div style="margin-top: 2em;" class="alert alert-info">
                    ${flash.message}
                </div>
            </g:if>
            <g:hasErrors bean="${command}">
                <div style="margin-top: 2em;" class="alert alert-danger">
                    <g:renderErrors bean="${command}" as="list" />
                </div>
            </g:hasErrors>
        </div>

        <div class="pageContent" style="margin-top: 3em;">

            <g:form action="tipReport" method="POST" class="form-horizontal">
                <div class="form-group">
                    <label for="tip" class="col-sm-2 control-label">Trust Interop. Profile</label>
                    <div class="col-sm-10">
                        <g:select name="tip" id="tip" class="form-control"
                                  from="${tips}" optionKey="id" optionValue="uniqueDisplayName" value="${command?.tip?.id}" />
                        <p class="help-block"This is the Trust Interoperability Profile to report on.</p>
                    </div>
                </div>
                <div class="form-group">
                    <label for="startDate" class="col-sm-2 control-label">Start Date</label>
                    <div class="col-sm-10">
                        <g:datePicker class="form-control" id="startDate" precision="day" name="startDate" placeholder="mm/dd/yyyy" value="${command?.startDate ?: ""}" />
                        <p class="help-block">The report will include all assessments with log entries after this date.</p>
                    </div>
                </div>
                <div class="form-group">
                    <label for="endDate" class="col-sm-2 control-label">End Date</label>
                    <div class="col-sm-10">
                        <g:datePicker class="form-control" id="endDate" precision="day" name="endDate" placeholder="mm/dd/yyyy" value="${command?.endDate ?: ""}"  />
                        <p class="help-block">The report will include all assessments with log entries prior to this date.</p>
                    </div>
                </div>


                <div class="form-group">
                    <div class="col-sm-offset-2 col-sm-10">
                        <button type="submit" class="btn btn-default">Run Report</button>
                    </div>
                </div>

            </g:form>

        </div>


	</body>
</html>

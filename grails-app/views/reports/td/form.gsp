<%@ page import="nstic.web.User; nstic.web.Role" %>
<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main"/>
		<title>Trustmark Definition Report Form</title>

        <style type="text/css">

        </style>
	</head>
	<body>
        %{--<ol class="breadcrumb">--}%
            %{--<li class="active">Home</li>--}%
        %{--</ol>--}%

        <h1><span class="glyphicon glyphicon-home"></span> Trustmark Definition Report</h1>
        <div class="pageSubsection">
            Nam facilisis, quam eget faucibus scelerisque, nulla justo auctor lacus, eu laoreet nibh odio sed eros. Sed aliquam mattis ipsum vitae pellentesque. Quisque congue ut nunc quis consequat. Vivamus accumsan accumsan varius. Fusce porttitor consequat risus, aliquam tempus lectus volutpat vitae. Morbi consequat nisi erat, vitae elementum est dapibus gravida. In ut eleifend risus. Mauris tristique quam eget tortor pharetra ullamcorper. Nulla ac massa pellentesque, blandit dolor ac, fermentum ante.
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

            <g:form action="tdReport" method="POST" class="form-horizontal">
                <div class="form-group">
                    <label for="trustmarkDefinition" class="col-sm-2 control-label">Trustmark Definition</label>
                    <div class="col-sm-10">
                        <g:select name="trustmarkDefinition" id="trustmarkDefinition" class="form-control"
                                  from="${trustmarkDefinitions}" optionKey="id" optionValue="uniqueDisplayName" value="${command?.trustmarkDefinition?.id}" />
                        <p class="help-block"This is the Trustmark Definition to report on.</p>
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
                        <g:submitButton name="do it" value="Run Report" class="btn btn-default" />
                    </div>
                </div>

            </g:form>

        </div>


	</body>
</html>

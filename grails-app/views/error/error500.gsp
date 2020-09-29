<!DOCTYPE html>
<html>
	<head>
		<title><g:if env="development">Grails Runtime Exception</g:if><g:else>Error</g:else></title>
		<meta name="layout" content="main">
		<g:if env="development"><link rel="stylesheet" href="${resource(dir: 'css', file: 'errors.css')}" type="text/css"></g:if>
		<style type="text/css">
			.errorTable {
				border-top: 2px solid #${nstic.assessment.ColorPalette.ERROR_TEXT.toString()};
				border-bottom: 2px solid #${nstic.assessment.ColorPalette.ERROR_TEXT.toString()};
			}
			.errorTable th { width: 200px; }
			.errorTable td { width: auto; }
		</style>
	</head>
	<body>
        <ol class="breadcrumb">
            <li><g:link controller="home" action="index">Home</g:link></li>
            <li class="active">500 Internal Error</li>
        </ol>


		<g:if env="development">
			<g:renderException exception="${exception}" />
		</g:if><g:else>
			<h1 class="text-danger" style="margin: 0; padding: 0;">Internal Server Error (500)</h1>
			<div class="alert alert-danger" style="margin-top: 2em;">
				The server has experienced a serious problem which is stopping it from continuing.  Please press the
				"back" button try to repeat your action again, and if the problem continues to occur <g:message code="contact.nstic.support" />.
			</div>

			<div style="margin-top: 2em;">
				<h3 style="margin-bottom: 5px;">Error Summary</h3>
				<div>
					<table class="table table-condensed table-striped table-bordered errorTable">
						<tr>
							<th>URL</th><td>${request.forwardURI}</td>
						</tr>
						<tr>
							<th>Exception Class</th><td>${cause?.class?.getName() ?: 'Unknown'}</td>
						</tr>
						<tr>
							<th>Exception Message</th><td>${cause?.getMessage() ?: 'Unknown'}</td>
						</tr>
					</table>
				</div>
			</div>
		</g:else>

	</body>
</html>

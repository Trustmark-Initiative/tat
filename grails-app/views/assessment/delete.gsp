<%@ page import="nstic.web.User; nstic.web.Role" %>
<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main"/>
		<title>Deleted Assessment ${oldId}</title>

        <style type="text/css">

        </style>
	</head>
	<body>

        <h1>Deleted Assessment ${oldId}</h1>
        <div class="pageSubsection">

        </div>

        <div id="messageContainer">
            <div style="margin-top: 2em;" class="alert alert-info">
                Successfully deleted assessment ${oldId} from the system.  An archive was created and placed into
                <g:link controller="binary" action="view" id="${archive.id}">binary object ${archive.id}</g:link>.
            </div>
        </div>

        <div class="pageContent">
            <g:link controller="home" class="btn btn-default">Home</g:link>
            <g:link controller="assessment" action="list" class="btn btn-default">View Assessments</g:link>
        </div>

	</body>
</html>

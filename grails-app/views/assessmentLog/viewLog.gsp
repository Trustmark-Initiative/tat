<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main"/>
		<title>View Assessment Log</title>

        <style type="text/css">

        </style>
	</head>
	<body>
        %{--<ol class="breadcrumb">--}%
            %{--<li class="active">Home</li>--}%
        %{--</ol>--}%

        <h1>Assessment Log</h1>
        <div class="pageSubsection">
            Viewing log for
            <a href="${createLink(controller:'assessment', action: 'view', id: assessment.id)}">assessment ${assessment.id}</a>.
        </div>

        <div class="pageContent">
            <table class="table-condensed table-bordered table-striped">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Created</th>
                        <th>Title</th>
                        <td>Message</td>
                    </tr>
                </thead>
                <tbody>
                    <g:if test="${logEntries?.size() > 0}">
                        <g:each in="${logEntries}" var="entry">
                            <tr>
                                <td>
                                    <a target="_blank" href="${createLink(controller:'assessmentLog', action: 'viewLogEntry', id: assessment.id, params: [entryId: entry.id])}">
                                        ${entry.id}
                                    </a>
                                </td>
                                <td style="white-space: nowrap"><g:formatDate date="${entry.dateCreated}" format="yyyy-MM-dd HH:mm:ss" /></td>
                                <td>${entry.title}</td>
                                <td>${entry.message}</td>
                            </tr>
                        </g:each>
                    </g:if>
                </tbody>
            </table>
        </div>

	</body>
</html>

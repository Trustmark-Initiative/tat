<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main"/>
		<title>View Log Entry ${entry.id}</title>

        <style type="text/css">

        </style>
	</head>
	<body>
        %{--<ol class="breadcrumb">--}%
            %{--<li class="active">Home</li>--}%
        %{--</ol>--}%

        <h1>View Log Entry</h1>
        <div class="pageSubsection">
            Viewing data for log entry "${entry.title}", entry #${entryPosition} of ${entryCount} for
            <a href="${createLink(controller:'assessment', action: 'view', id: assessment.id)}">assessment ${assessment.id}</a>.
        </div>

        <div class="pageContent">

            <div class="row">
                <div class="col-md-3" style="text-align: left;">
                    <g:if test="${previousEntry}">
                        <a href="${createLink(controller:'assessmentLog', action: 'viewLogEntry', id: assessment.id, params:[entryId: previousEntry.id])}">
                            <span class="glyphicon glyphicon-circle-arrow-left"></span>
                            ${previousEntry.title}
                        </a>
                    </g:if>
                    <g:else>
                        <em>No Previous Entry</em>
                    </g:else>
                </div>
                <div class="col-md-3" style="text-align: right;">
                    <g:if test="${nextEntry}">
                        <a href="${createLink(controller:'assessmentLog', action: 'viewLogEntry', id: assessment.id, params:[entryId: nextEntry.id])}">
                            <span class="glyphicon glyphicon-circle-arrow-right"></span>
                            ${nextEntry.title}
                        </a>
                    </g:if>
                    <g:else>
                        <em>No Next Entry</em>
                    </g:else>
                </div>
            </div>

            <div class="pageContent">
                <h4>Entry Information</h4>
                <table class="table-condensed table-striped table-bordered">
                    <tr>
                        <th>Id</th><td>${entry.id}</td>
                    </tr>
                    <tr>
                        <th>Type</th><td>${entry.type}</td>
                    </tr>
                    <tr>
                        <th>Date</th><td><g:formatDate date="${entry.dateCreated}" format="yyyy-MM-dd HH:mm:ss"/></td>
                    </tr>
                    <tr>
                        <th>Title</th><td>${entry.title}</td>
                    </tr>
                    <tr>
                        <th>Message</th><td>${entry.message}</td>
                    </tr>
                </table>
            </div>

            <div class="pageContent">
                <h4>Entry Data</h4>
                <pre>${entry.data}</pre>
            </div>
        </div>

        <script type="text/javascript">
            $(document).ready(function(){
                $("pre").each(function (i, e) {
                    hljs.highlightBlock(e);
                });
            });
        </script>

	</body>
</html>

<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main"/>
		<title>Organization Edit Comment</title>

        <style type="text/css">

        </style>
	</head>
	<body>

        <h1>${command.organization.identifier ?: 'UNKNOWN'}: Edit Comment</h1>
        <div class="pageSubsection">
            This page allows you to edit comment #${command.commentId} for the organization '${command.organization.name}'.  This comment
            is considered global for this organization, and will be displayed on any organization reports.
        </div>


        <g:hasErrors bean="${command}">
            <div style="margin-top: 2em;" class="alert alert-danger">
                <g:renderErrors bean="${command}" />
            </div>
        </g:hasErrors>


        <div class="pageContent">
            <form action="${createLink(controller:'organization', action:'updateComment', id: command.organization.identifier, params: [commentId: command.commentId])}"
                    method="POST" class="form-horizontal" onsubmit="return validateForm()">

                <g:hiddenField name="organization" id="organization" value="${command?.organization?.id}" />
                <g:hiddenField name="commentId" id="commentId" value="${command?.commentId}" />

                <div class="form-group">
                    <label for="name" class="col-sm-2 control-label">Title</label>
                    <div class="col-sm-10">
                        <g:textField name="name" id="name" class="form-control" value="${command?.name}" />
                    </div>
                </div>

                <div class="form-group" >
                    <label for="comment" class="col-sm-2 control-label">Comment</label>
                    <div class="col-sm-10">
                        <g:textArea name="comment" id="comment" class="form-control" style="min-height: 300px;"
                                    placeholder="" value="${command?.comment}" />
                    </div>
                </div>

                <div class="form-group">
                    <div class="col-sm-offset-2 col-sm-10">
                        <button type="submit" class="btn btn-primary">
                            <span class="glyphicon glyphicon-save"></span>
                            Update
                        </button>
                        <a href="${createLink(controller:'organization', action: 'view', id: command.organization.identifier)}" class="btn btn-default">Cancel</a>
                    </div>
                </div>

            </form>
        </div>

        <script type="text/javascript">
            function validateForm() {
                if( $('#name').val() === '' ){
                    alert("Please enter a Name");
                    return false;
                }

                return true;
            }
        </script>
    
        </body>
</html>

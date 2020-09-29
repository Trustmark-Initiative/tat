<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main"/>
		<title>Organization Add Artifact</title>

        <style type="text/css">

        </style>
	</head>
	<body>

        <h1>${command.organization.identifier ?: 'UNKNOWN'}: Add artifact</h1>
        <div class="pageSubsection">
            This page allows you to add a new artifact for the organization '${command.organization.name}'.  This artifact
            is considered global for this organization, and will be displayed as a selection option for any assessment
            step required artifact drop down.
        </div>


        <g:hasErrors bean="${command}">
            <div style="margin-top: 2em;" class="alert alert-danger">
                <g:renderErrors bean="${command}" />
            </div>
        </g:hasErrors>


        <div class="pageContent">
            <form action="${createLink(controller:'organization', action:'saveArtifact', id: command.organization.identifier)}"
                    method="POST" class="form-horizontal" onsubmit="return validateForm()">

                <g:hiddenField name="organization" id="organization" value="${command?.organization?.id}" />
                <g:hiddenField name="binaryObject" id="binaryId1" value="${command?.binaryObject?.id ?: -1}" />

                <div class="form-group">
                    <label for="displayName" class="col-sm-2 control-label">Artifact File</label>
                    <div class="col-sm-10">
                        <p id="fileUploadName" class="form-control-static">
                            <a href="#" id="fileUploadButton1" class="btn btn-default">
                                <span class="glyphicon glyphicon-upload"></span>
                                Upload
                            </a>
                            <span id="fileName1">
                                <g:if test="${command.binaryObject != null}">
                                    ${command.binaryObject.originalFilename}
                                </g:if><g:else>
                                    Select a File...
                                </g:else>
                            </span>
                            <div id="fileUploadStatus1"></div>
                        </p>
                    </div>
                </div>

                <div class="form-group">
                    <label for="displayName" class="col-sm-2 control-label">Display Name</label>
                    <div class="col-sm-10">
                        <g:textField name="displayName" id="displayName" class="form-control" value="${command?.displayName}" />
                    </div>
                </div>

                <div class="form-group" >
                    <label for="description" class="col-sm-2 control-label">Description</label>
                    <div class="col-sm-10">
                        <g:textArea name="description" id="description" class="form-control" placeholder="" value="${command?.description}" />
                    </div>
                </div>

                <div class="form-group">
                    <div class="col-sm-offset-2 col-sm-10">
                        <div class="checkbox">
                            <label>
                                <input name="active" id="active" type="checkbox" ${command?.active ? 'checked="checked"' : ''} />
                                Active
                            </label>
                        </div>
                    </div>
                </div>

                <div class="form-group">
                    <div class="col-sm-offset-2 col-sm-10">
                        <button type="submit" class="btn btn-primary">
                            <span class="glyphicon glyphicon-save"></span>
                            Save
                        </button>
                        <a href="${createLink(controller:'organization', action: 'view', id: command.organization.identifier)}" class="btn btn-default">Cancel</a>
                    </div>
                </div>

            </form>
        </div>

        <script type="text/javascript">
            function validateForm() {
                if( $('#binaryId1').val() === '-1' ){
                    alert("You must select a file to upload.");
                    return false;
                }

                if( $('#displayName').val() === '' ){
                    alert("Please enter a Display Name");
                    return false;
                }

                return true;
            }
        </script>

        <tmpl:/templates/pluploadJavascript pluploadCounter="1" context="Create Artifact for Organization ${command.organization.identifier}" />

        </body>
</html>

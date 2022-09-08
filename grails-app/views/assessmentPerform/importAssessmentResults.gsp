<%@page import="nstic.web.assessment.Assessment" defaultCodec="none" %>

<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>Import Assessment Results for assessment: ${assessment.assessmentName}</title>

    <style type="text/css">

    </style>
</head>
<body>

<div class="row">
    <div class="col-md-9">
        <h1>Import Assessment Results</h1>
        <div class="pageSubsection">
            This page allows you to create an artifact that represents a detailed audit for assessment ${assessment.assessmentName}.

            Please take caution when using this feature.  The uploaded artifact should be an audit letter or report that
            shows the organization has undergone a thorough audit that covers the same criteria for which this assessment
            covers.  The assessor should have very high confidence that this existing audit event has thoroughly verified
            conformance to this TIP.

            The assessor is still required to provide any parameters required by this assessment.
        </div>
    </div>
    <div class="col-md-3" style="text-align: right;">

    </div>
</div>

<div id="errorContainer">
    <g:if test="${flash.error}">
        <div class="alert alert-danger">${flash.error}</div>
    </g:if>
    <g:if test="${command?.hasErrors()}">
        <div class="alert alert-danger" style="margin-top: 2em;">
            <b>Cannot create artifact due to the following errors:</b>
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
</div>

<div class="row" style="margin-top: 2em;">
    <div class="col-md-12">
        <g:form class="form-horizontal" name="importAssessmentResultsForm" method="POST" action="saveAssessmentResults">

            <g:hiddenField name="assessmentId" id="assessmentId" value="${command?.assessmentId}" />
            <g:hiddenField name="binaryId1" id="binaryId1" value="${command?.binaryId1}" />
            <g:hiddenField name="artifactType" id="artifactType" value="${command?.artifactType ?: 'newUpload'}" />

            <div class="form-group">
                <label for="fileUploadName" class="col-sm-2 control-label">Artifact File</label>
                <div class="col-sm-10">

                    <p id="fileUploadName" class="form-control-static">
                        <a href="#" id="fileUploadButton1" class="btn btn-default">
                            <span class="glyphicon glyphicon-upload"></span>
                            Upload
                        </a>
                        <span id="fileName1">Select a File...</span>
                        <div id="fileUploadStatus1"></div>
                    </p>

                    <div id="selectExistingFileContainer">
                        <div id="selectExistingFileControl">
                            <asset:image src="spinner.gif" /> Loading existing artifacts...
                        </div>
                    </div>
                </div>
            </div>

            <div class="form-group">
                <label for="displayName" class="col-sm-2 control-label">
                    Display Name
                </label>
                <div class="col-sm-10">
                    <g:textField name="displayName" id="displayName" class="form-control" value="${command?.displayName}" />
                </div>
            </div>

            <div class="form-group">
                <label for="comment" id="commentLabel" class="col-sm-2 control-label">
                    <span id="commentLabelExtension"></span> Comment
                </label>
                <div class="col-sm-10">
                    <g:textArea name="comment" id="comment" style="height: 250px;" class="form-control" value="${command?.comment}" />
                </div>
            </div>

            <div class="form-group">
                <div class="col-sm-offset-2 col-sm-10">
                    <button id="submitButton" type="submit" class="btn btn-primary" onclick="return checkForm();">
                        Perform Automatic Assessment
                    </button>
                    <a href="${createLink(controller:'assessment', action: 'view', id: assessment.id)}" class="btn btn-default">Cancel</a>
                </div>
            </div>
        </g:form>
    </div>
</div>

<tmpl:/templates/pluploadJavascript
        pluploadCounter="1"
        filesAddedCallback="setUploadedFilename"
        uploadCompleteCallback="fileUploadComplete"
        context="Import Assessment Results for Assessment ${assessment.id}}" />

<script type="text/javascript">

    $(document).ready(function(){
        clearFileTypeSelection();
        updateFileDisplay('newUpload');
    });

    /**
     * A convenience method which resets the state of the Artifact File section of the form, for the purpose of
     * clearing any existing file data.
     */
    function clearFileTypeSelection() {
        $('#selectExistingFileContainer').hide();
        $('#fileUploadName').hide();
        $('#fileName1').html('Select a File...');
        $('#fileUploadStatus').html('');
        $('#commentLabelExtension').removeClass('glyphicon');
        $('#commentLabelExtension').removeClass('glyphicon-star');

        $('#artifactFilePillUpload').removeClass('active');
        $('#artifactFilePillResuse').removeClass('active');
        $('#artifactFilePillNone').removeClass('active');

        $('#artifactType').val('unknown');
        $('#binaryId1').val('');
        $('#displayName').val('');
    }

    function styleArtifactButtonSelectHtml(artifact){
        var html = '';
        html += '<li>\n';
        html += '    <a href="javascript:setFileBinaryId('+artifact.artifactId+')">\n';
        if( artifact.displayName === artifact.fileName ){
            html += '        <div><span style="font-weight: bold;">'+artifact.displayName+'</span></div>\n';
        }else{
            html += '        <div><span style="font-weight: bold;">'+artifact.displayName+'</span>&nbsp;<span>('+artifact.fileName+')</span></div>\n';
        }
        html += '        <div style="font-size: 80%; margin-left: 1em; width: 600px; max-width: 600px; white-space: normal;">'+artifact.description+'</div>\n';
        html += '    </a>\n';
        html += '</li>\n\n';

        return html;
    }//end styleArtifactButtonSelectHtml()

    function setFileBinaryId(id){
        var artifact = findExistingArtifact(id);
        console.log("Setting binary id to: "+artifact.binaryObjectId);
        $('#binaryId1').val(artifact.binaryObjectId);
        console.log("Setting display name to: "+artifact.displayName);
        $('#displayName').val(artifact.displayName);
        console.log("Setting comment to: "+artifact.description);
        $('#comment').val(artifact.description);

        if( artifact ) {
            console.log("Setting file title...");
            $('#selectedExistingArtifactTitle').html(artifact.displayName);
        }else{
            console.log("Setting file title not found error...");
            $('#selectedExistingArtifactTitle').html('<em>Artifact Not Found</em>');
        }
    }//end setFileBinaryId()

    function clearFileBinaryId(){
        $('#selectedExistingArtifactTitle').html("<em>Select Artifact...</em>");
        $('#binaryId1').val('');
    }

    /**
     * Called when the user selects a "pill" in the Artifact File section of the form.
     */
    function updateFileDisplay(type) {
        clearFileTypeSelection();

        $('#fileUploadName').show()
        $('#artifactType').val('newUpload')
        $('#artifactFilePillUpload').addClass('active');

        disableButton("submitButton");
    }//end updateFileDisplay()

    function isEmpty( val ){
        if( val ){
            val = val.trim();
            return val === '';
        }else{
            return true;
        }
    }

    function setErrorMessage( msg ){
        $('#errorContainer').html('<div class="alert alert-danger" style="margin-top: 2em;">'+msg+'</div>');
    }

    /**
     * Validates the form is ready for submission on the client side.
     */
    function checkForm() {
        var type = $('#artifactType').val();
        console.log("Checking form for type = ["+type+"]")
        if( type == "newUpload" ) {
            if( isEmpty($('#binaryId1').val()) ){
                setErrorMessage("Please select a file to upload.");
                return false;
            }
        }
        
        return true;
    }

    /**
     * Called by pluploader template code after file added (because we specified it on the templ inclusion.)
     */
    function setUploadedFilename(up, files){
        var firstFilename = files[0].name;
        console.log("Setting display name to file name["+firstFilename+"]...")
        $('#displayName').val(firstFilename);

        disableButton("submitButton");
    }//end setUploadedFilename()

    /**
     * Called by pluploader template code after the file upload is complete
     */
    function fileUploadComplete(up){
        enableButton("submitButton");
    }//end fileUploadComplete()

</script>
</body>
</html>

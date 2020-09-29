<%@page import="nstic.web.assessment.AssessmentStepResult" defaultCodec="none" %>

<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>Edit Artifact ${artifactData.displayName}</title>

    <style type="text/css">

    </style>
</head>
<body>

<ol class="breadcrumb">
    <li>
        <g:link controller="home" action="index">Home</g:link>
    </li>
    <li>
        <g:link controller="assessment" action="list">Assessments</g:link>
    </li>
    <li>
        <g:link controller="assessmentPerform" action="view" id="${command?.assessmentId}"
                params="[stepDataId: command?.stepDataId]">
            Perform Assessment #${command?.assessmentId}, Step ${command?.stepDataId}
        </g:link>
    </li>
    <li>
        <g:link controller="assessmentPerform" action="viewArtifact" id="${assessment.id}"
                params="[stepDataId: currentStepData.id, artifactId: command.artifactId]">
            View Artifact[${command.displayName ?: command.artifactId}]
        </g:link>
    </li>
    <li class="active">Edit Artifact ${command.displayName ?: command.artifactId}</li>
</ol>

<div class="row">
    <div class="col-md-9">
        <h1>Edit Artifact '${artifactData.displayName ?: '<em>No Name</em>'}'</h1>
        <div class="pageSubsection">
            This page allows you to edit artifact ${artifactData.displayName} for step ${currentStepData.step.name} in ${assessment.assessmentName}.
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
            <b>Cannot edit artifact due to the following errors:</b>
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
        <form class="form-horizontal" name="createArtifactForm" method="POST"
              action="${createLink(controller:'assessmentPerform', action: 'updateArtifact', id: assessment.id, params: [stepDataId: currentStepData.id, artifactId: command?.artifactId])}">

            <g:hiddenField name="artifactId" id="artifactId" value="${command?.artifactId}" />
            <g:hiddenField name="assessmentId" id="assessmentId" value="${command?.assessmentId}" />
            <g:hiddenField name="stepDataId" id="stepDataId" value="${command?.stepDataId}" />
            <g:hiddenField name="binaryId1" id="binaryId1" value="${command?.binaryId1}" />
            <g:hiddenField name="artifactType" id="artifactType" value="${command?.artifactType ?: 'newUpload'}" />
            <g:hiddenField id="requiredArtifactId" name="requiredArtifactId" value="${command?.requiredArtifactId ?: ''}" />

            <div class="form-group">
                <label for="requiredArtifactId" class="col-sm-2 control-label">Artifact Definition</label>
                <div class="col-sm-10">
                    <div class="btn-group" style="margin: 0;">
                        <button type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown" aria-expanded="false">
                            <span id="selectedArtifactTitle"><em>Select Required Artifact...</em></span> <span class="caret"></span>
                        </button>
                        <ul class="dropdown-menu" role="menu">
                            <g:each in="${requiredArtifactList}" var="artifactFromList">
                                <li>
                                    <a href="javascript:setRequiredArtifact(${artifactFromList.id})">
                                        <div style="font-weight: bold;">${artifactFromList.name}</div>
                                        <div style="font-size: 80%; margin-left: 1em; width: 600px; max-width: 600px; white-space: normal;">
                                            ${artifactFromList.description}
                                        </div>
                                    </a>
                                </li>
                            </g:each>
                        </ul>
                    </div>
                    &nbsp;&nbsp;&nbsp;
                    <a href="javascript:clearRequiredArtifact();">Clear</a>
                    <span id="helpBlock" class="help-block">If this artifact should be associated to a required artifact, select that here.</span>
                </div>
                <script type="text/javascript">
                    var REQUIRED_ARTIFACT_DATA = [
                        <g:each in="${requiredArtifactList}" var="artifactFromList" status="artifactFromListStatus">
				${groovy.json.JsonOutput.toJson([id: artifactFromList.id, name: artifactFromList.name, description: artifactFromList.description])}
                        	<g:if test="${artifactFromListStatus < requiredArtifactList.size()-1}">, </g:if>
                        </g:each>
                    ];

                    function clearRequiredArtifact(){
                        $('#requiredArtifactId').val('');
                        $('#selectedArtifactTitle').html('<em>Select Required Artifact...</em>');
                    }

                    function findRequiredArtifact(id){
                        var artifact = null;
                        for( var index = 0; index < REQUIRED_ARTIFACT_DATA.length; index++ ){
                            var current = REQUIRED_ARTIFACT_DATA[index];
                            if( current && (current.id == id) ){
                                artifact = current;
                                break;
                            }
                        }
                        return artifact;
                    }

                    function setRequiredArtifact(id){
                        console.log("Setting required artifact to "+id);
                        $('#requiredArtifactId').val(id);
                        var artifact = findRequiredArtifact(id);
                        if( artifact ) {
                            $('#selectedArtifactTitle').html(artifact.name);
                        }else{
                            $('#selectedArtifactTitle').html('<em>Artifact Not Found</em>');
                        }
                    }//end setRequiredArtifact()
                </script>
            </div>

            <div class="form-group">
                <label for="fileUploadName" class="col-sm-2 control-label">Artifact File</label>
                <div class="col-sm-10">
                    <div>
                        <ul class="nav nav-pills">
                            <li id="artifactFilePillNoChange" role="presentation" class="active"><a href="javascript:updateFileDisplay('noChange');">No Change</a></li>
                            <li id="artifactFilePillUpload" role="presentation"><a href="javascript:updateFileDisplay('newUpload');">Upload File</a></li>
                            <li id="artifactFilePillResuse" role="presentation"><a href="javascript:updateFileDisplay('reuseExisting');">Use Existing</a></li>
                            <li id="artifactFilePillNone" role="presentation"><a href="javascript:updateFileDisplay('none');">No File</a></li>
                        </ul>
                    </div>

                    <p id="noChangeDescriptionTextContainer" class="form-control-static">
                        <g:if test="${artifactData.data != null}">
                            Continue using Artifact ${artifactData.getDisplayName()}
                        </g:if>
                        <g:else>
                            Continue using comment only.
                        </g:else>
                    </p>

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
                    <g:textField name="displayName" id="displayName" class="form-control" value="${command?.displayName ?: artifactData.data?.originalFilename}" />
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
                        <span class="glyphicon glyphicon-save"></span>
                        Save
                    </button>
                    <a href="${createLink(controller:'assessmentPerform', action: 'view', id: assessment.id, params: [stepDataId: currentStepData.id])}" class="btn btn-default">Cancel</a>
                </div>
            </div>
        </form>
    </div>
</div>

<tmpl:/templates/pluploadJavascript
        pluploadCounter="1"
        filesAddedCallback="setUploadedFilename"
        context="Create Artifact for Assessment ${assessment.id}, Step ${currentStepData.step.stepNumber}" />

<script type="text/javascript">
    var LOADED_ARTIFACTS = false;
    var EXISTING_ARTIFACTS = [];

    $(document).ready(function(){
        clearFileTypeSelection();
        updateFileDisplay('noChange');

        <g:if test="${command?.requiredArtifactId && command?.requiredArtifactId != -1}">
        setRequiredArtifact(${command?.requiredArtifactId});
        </g:if>
    });


    /**
     * A convenience method which resets the state of the Artifact File section of the form, for the purpose of
     * clearing any existing file data.
     */
    function clearFileTypeSelection() {
        $('#noChangeDescriptionTextContainer').hide();
        $('#selectExistingFileContainer').hide();
        $('#fileUploadName').hide();
        $('#fileName1').html('Select a File...');
        $('#fileUploadStatus').html('');
        $('#commentLabelExtension').removeClass('glyphicon');
        $('#commentLabelExtension').removeClass('glyphicon-star');

        $('#artifactFilePillUpload').removeClass('active');
        $('#artifactFilePillResuse').removeClass('active');
        $('#artifactFilePillNone').removeClass('active');
        $('#artifactFilePillNoChange').removeClass('active');

        $('#artifactType').val('unknown');
        $('#binaryId1').val('');
        $('#displayName').val('');
    }

    function loadExistingArtifacts() {
        var url = '${createLink(controller:'assessment', action: 'listAvailableArtifacts', id: assessment.id)}';
        console.log("Loading existing artifacts from: "+url);
        $.ajax({
            url: url,
            dataType: 'json',
            data: {
                now: new Date().getMilliseconds(),
                format: 'json'
            },
            error: function( jqXHR, textStatus, errorThrown ){
                $('#selectExistingFileControl').html('<div class="alert alert-danger">An error occurred while loading the remote data.  Please refresh and try again.</div>')
            },
            success: function(result, textStatus, jqXHR){
                console.log("Received artifact result: "+JSON.stringify(result, null, 4));
                EXISTING_ARTIFACTS = result;

                var html = '';

                if( EXISTING_ARTIFACTS && EXISTING_ARTIFACTS.length > 0 ) {
                    html += '<div class="btn-group" style="margin: 0;">\n';
                    html += '    <button type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown" aria-expanded="false">\n';
                    html += '        <span id="selectedExistingArtifactTitle"><em>Select Artifact...</em></span> <span class="caret"></span>\n';
                    html += '    </button>\n'

                    html += '    <ul class="dropdown-menu" role="menu">\n';
                    for (var i = 0; i < EXISTING_ARTIFACTS.length; i++) {
                        html += styleArtifactButtonSelectHtml(EXISTING_ARTIFACTS[i]);
                    }

                    html += '    </ul>\n';
                    html += '</div>\n';
                    html += '&nbsp;&nbsp;&nbsp; <a href="javascript:clearFileBinaryId();">Clear</a>\n';

                    html += '<span id="helpBlock" class="help-block">If this artifact should be associated to a required artifact, select that here.</span>';

                }else{
                    alert("No data from server!");
                }

                $('#selectExistingFileControl').html(html);
            }
        })
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

    function findExistingArtifact(id){
        var artifact = null;
        console.log("Finding artifact "+id);
        for( var index = 0; index < EXISTING_ARTIFACTS.length; index++ ){
            var current = EXISTING_ARTIFACTS[index];
            if( current && (current.artifactId == id) ){
                artifact = current;
                break;
            }
        }
        if( artifact ){
            console.log("Successfully found artifact["+artifact.artifactId+"]: "+artifact.displayName);
        }else{
            console.log("No such artifact: "+id);
        }
        return artifact;
    }

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

        if( type == 'noChange') {
            $('#artifactFilePillNoChange').addClass('active');
            $('#noChangeDescriptionTextContainer').show();
            <g:if test="${command?.binaryId1 && command?.binaryId1 != -1}">
            $('#binaryId1').val(${command?.binaryId1});
            </g:if>
            $('#displayName').val('${command?.displayName}');
        }else if( type == 'newUpload') {
            $('#fileUploadName').show()
            $('#artifactType').val('newUpload')
            $('#artifactFilePillUpload').addClass('active');
        }else if( type == 'reuseExisting') {
            $('#selectExistingFileContainer').show()
            $('#artifactType').val('reuseExisting')
            $('#artifactFilePillResuse').addClass('active');

            if( !LOADED_ARTIFACTS ){
                loadExistingArtifacts();
            }
        }else{ // It is 'none'
            $('#commentLabelExtension').addClass('glyphicon')
            $('#commentLabelExtension').addClass('glyphicon-star')
            $('#artifactType').val('commentOnly')
            $('#artifactFilePillNone').addClass('active');
        }
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

        }else if( type == "reuseExisting" ){

            if( isEmpty($('#binaryId1').val()) ){
                setErrorMessage('ERROR: No artifact chosen.  You must select an artifact to re-use, or instead choose the options "Upload File" or "No File".');
                return false;
            }

        }else if( type == "commentOnly") {
            if( isEmpty($('#comment').val()) ){
                setErrorMessage('Comment is required.');
                return false;
            }

            if( isEmpty($('#displayName').val()) ){
                $('#displayName').val("Read Comment");
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
    }//end setUploadedFilename()

</script>
</body>
</html>

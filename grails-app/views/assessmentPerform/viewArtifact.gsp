<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main"/>
		<title>View Artifact</title>

        <style type="text/css">

            .infoTable {

            }
            .infoTable td {
                padding: 0.5em;
            }
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
            <g:link controller="assessmentPerform" action="view" id="${assessment.id}"
                    params="[stepId: stepData.step.id]">
                Perform Assessment #${assessment.id}, Step "${stepData.step.name}"
            </g:link>
        </li>
        <li class="active">
            View Artifact[${artifact.displayName ?: artifact.id}]
        </li>
    </ol>

    <g:if test="${flash.error}">
        <div class="alert alert-danger">${flash.error}</div>
    </g:if>
    <g:if test="${flash.message}">
        <div class="alert alert-success">${flash.message}</div>
    </g:if>

        <h1>View Artifact</h1>
        <div class="pageSubsection">

        </div>


        <div class="pageContent">
            <div class="row">
                <div class="col-md-5">
                    <h4>Artifact Information</h4>
                    <table class="infoTable">
                        <tr>
                            <td>Unique Id</td><td>${artifact.id}</td>
                        </tr>
                        <tr>
                            <td>Display Name</td><td>${artifact.displayName}</td>
                        </tr>
                        <tr>
                            <td>Uploading User</td><td>${artifact.uploadingUser?.contactInformation?.responder}</td>
                        </tr>
                        <tr>
                            <td>Satisfies Required Artifact</td><td>${artifact.requiredArtifact?.name ?: "None"}</td>
                        </tr>
                    </table>
                    <div style="margin-bottom: 1em;">&nbsp;</div>
                    <h4>Binary Information</h4>
                    <table class="infoTable">
                        <g:if test="${artifact.data != null}">
                            <tr>
                                <td>Unique Id</td><td>${artifact.data.id}</td>
                            </tr><tr>
                                <td>File Name</td><td>${artifact.data.originalFilename}</td>
                            </tr><tr>
                                <td>Size</td><td>${org.apache.commons.io.FileUtils.byteCountToDisplaySize(artifact.data.fileSize)}</td>
                            </tr><tr>
                                <td>Mime Type</td><td>${artifact.data.mimeType}</td>
                            </tr>
                        </g:if>
                        <g:else>
                            <tr><td><em>There is no binary.</em></td></tr>
                        </g:else>
                    </table>
                    <g:if test="${artifact.data != null}">
                        <div style="margin-top: 0.5em;">
                            <a href="${createLink(controller:'binary', action:'view', id: artifact.data.id)}" class="btn btn-default">
                                <span class="glyphicon glyphicon-download"></span>
                                Download
                            </a>
                        </div>
                    </g:if>
                </div><div class="col-md-7">
                    <h4>Comment</h4>
                    <pre>${artifact.comment ?: 'No Comment'}</pre>
                </div>
            </div>



            <div style="margin-top: 2em;">
                <a href="${createLink(controller:'assessmentPerform', action: 'view', id: assessment.id, params: [stepDataId: stepData.id])}" class="btn btn-default">
                    Perform Assessment #${assessment.id}
                </a>
                <a href="${createLink(controller:'assessmentPerform', action: 'editArtifact', id: assessment.id, params: [stepDataId: stepData.id, artifactId: artifact.id])}" class="btn btn-default">
                    Edit Artifact
                </a>
                <a href="javascript:deleteArtifact(${artifact.id})" class="btn btn-danger">
                    Delete
                </a>
            </div>

            <div style="margin-top: 3em;">&nbsp;</div>

        </div>

        <script type="text/javascript">

            function deleteArtifact(id){
                if( confirm("Really delete this artifact?") ) {
                    var url = "${createLink(controller:'assessmentPerform', action: 'deleteArtifact', id: assessment.id, params:[stepDataId: stepData.id, artifactId: '_ARTIFACT_ID_HERE_', format: 'json'])}";
                    url = url.replace("_ARTIFACT_ID_HERE_", id);
                    console.log("Deleting artifact: " + id+", URL: "+url);
                    $.post(url, function(data){
                        console.log("Response: "+data);
                        if( data && data.status && data.status == "SUCCESS" ){
                            window.location.replace('${createLink(controller:'assessmentPerform', action: 'view', id: assessment.id, params: [stepDataId: stepData.id])}');
                        }else{
                            alert("An error occurred while deleting the artifact.  Please refresh the page, and try again.")
                        }
                    })
                }
            }

        </script>

	</body>
</html>

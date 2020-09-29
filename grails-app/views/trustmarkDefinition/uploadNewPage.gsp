<%@ page import="nstic.web.Role" %>
<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main"/>
		<title>Create Trustmark Definitions</title>

        <style type="text/css">

        </style>
	</head>
	<body>

        <h1>Upload Trustmark Definitions</h1>
        <div class="pageSubsection">
            On this page, you can upload Trustmark Definitions to the tool.  Note that you can upload a single XML file
            or a zip file full of trustmark XML files.
        </div>

        <div class="pageContent">
            <div class="row" style="margin-bottom: 2em;">
                <div class="col-md-6">
                    <a href="#" id="uploadTdButton" class="btn btn-primary">Upload TD</a>
                </div>
            </div>
            <div class="row">
                <div class="col-md-12" id="resultsFeedback">
                    Upload a single XML or multiple XML in a zip by clicking the button above.
                </div>
            </div>
        </div>

        <script type="text/javascript">
            var RESPONSE = null;

            $( document ).ready(function() {

                console.log('Initializing plupload...')
                var uploader = new plupload.Uploader({
                    browse_button: 'uploadTdButton',
                    multi_selection: false,
                    chunk_size: '100kb',
                    max_retries: 0,
                    url: '${request.contextPath}/trustmark-definitions/new',
                    multipart_params: {
                        format: 'json'
                    }
                });
                PLUPLOAD = uploader;
                uploader.init();
                uploader.bind('FilesAdded', handleFilesAdded);
                uploader.bind('UploadProgress', handleUploadProgress);
                uploader.bind('Error', handleUploadError);
                uploader.bind('FileUploaded', handleFileUploaded);
                uploader.bind('UploadComplete', handleUploadComplete);


                console.log('Successfully initialized plupload')

                console.log("TD List page loaded successfully.")
            });


            function handleFilesAdded(up, files){
                console.log("handleFilesAdded called.  files: "+JSON.stringify(files));
                // TODO Maybe provide some feedback?
                console.log("Done handling files added event.");
                PLUPLOAD.start();
                $('#uploadTdButton').addClass('disabled');
                $('#resultsFeedback').html('<asset:image src="spinner.gif" /> Uploading File...');
            }

            function handleUploadProgress(up, file){
                console.log("handleUploadProgress called.  file: "+JSON.stringify(file));
            }

            function handleUploadError(up, err) {
                console.log("handleUploadError called.  file: "+JSON.stringify(err));
                // alert("Error uploading file: \n"+JSON.stringify(err));
            }

            function handleUploadComplete(up, files) {
                displayResponse();
            }

            function handleFileUploaded(up, file, response){
                RESPONSE = JSON.parse(response.response);
                console.log("File Uploaded.  Response: "+JSON.stringify(RESPONSE, null, 4));
            }

            //==========================================================================================================
            //  End Plupload
            //==========================================================================================================
            function displayResponse() {
                $('#resultsFeedback').html(responseToHtml(RESPONSE));
            }//end displayResponse()

            function responseToHtml(response){
                var html = '';

                html += '<div class="row" style="margin-bottom: 1em;">\n';
                html += '    <div class="col-md-6">\n';

                // Display Response...
                html += '        <span>Response Status: ';
                if( response.status == "SUCCESS" ){
                    html += '<span class="label label-success">Success</span>';
                }else if( response.status == "WARNING" ){
                    html += '<span class="label label-warning">Warning</span>';
                }else{
                    html += '<span class="label label-danger">Failure</span>';
                }
                html += '        </span> \n';
                html += '        <span>Successful Count: <span style="color: rgb(0, 150, 0)">'+response.successfullyImportedCount+'</span></span>  \n';
                html += '        <span>Error Count: <span style="color: rgb(150, 0, 0)">'+response.errorImportingCount+'</span></span>\n';
                html += '    </div>\n';
                html += '</div>\n';

                html += '<div class="row">\n';
                html += '    <div class="col-md-12">\n';
                html += '        <table class="table table-condensed table-striped table-bordered">\n';
                html += '        <thead>\n';
                html += '            <tr>\n';
                html += '                <th>File Name</th>\n';
                html += '                <th>Status</th>\n';
                html += '                <th>Message</th>\n';
                html += '            </tr>\n';
                html += '        </thead><tbody>\n';
                for( var tdIndex = 0; tdIndex < response.results.length; tdIndex++ ){
                    var tdData = response.results[tdIndex];
                    html += '<tr>\n';
                    html += '  <td>'+tdData.fileName+'</td>\n';
                    if( tdData.status == "FAILURE" ) {
                        html += '  <td><span class="glyphicon glyphicon-remove" style="color: rgb(150, 0, 0);"></span></td>\n';
                        html += '  <td>'+tdData.error.message+'</td>\n';
                    }else{
                        html += '  <td><span class="glyphicon glyphicon-ok" style="color: rgb(0, 150, 0);"></span></td>\n';
                        html += '  <td>Successfully Imported TD[name: '+tdData.td.name+', version: '+tdData.td.version+']</td>\n';
                    }
                    html += '</tr>\n';
                }//end for()
                html += '</tbody></table>\n';
                html += '    </div>\n';
                html += '</div>\n';

                return html;
            }//end responseToHtml()

            function prettifyFileName(filename){
                var index = filename.indexOf('.dir/');
                return filename.substring(index+5);
            }

        </script>

	</body>
</html>

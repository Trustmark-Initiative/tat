<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main"/>
		<title>Error Test Page</title>

        <style type="text/css">
            .buttonList {
                list-style: none;
                margin: 0;
                padding: 0;
            }

            .buttonListItem {
                margin: 0;
                padding: 0;
                margin-bottom: 1em;
            }
        </style>
	</head>
	<body>

        <h2 class="pageTitle">Error Testing Page</h2>
        <div class="pageDescription">
            On this page, you can test what will happen for various system errors.  This is meant for internal
            testing only.
        </div>

        <div id="processErrorContainer"></div>
        <div class="pageContent">

            <ul class="buttonList">
                <li class="buttonListItem">
                    <a href="${createLink(controller:'error', action:'generate500')}" class="btn btn-default">Error 500</a>
                    Generate a 500 error (Internal Servlet Exception)
                </li>
                <li class="buttonListItem">
                    <a href="${createLink(uri: '/this-will-never-exist.nono')}" class="btn btn-default">404 Not Found</a>
                    Reference a resource which will not exist.
                </li>
            </ul>

        </div>
        <div style="margin-top: 6em;">
            <div class="form-group">
                <div class="row">
                    <div class="col-md-4"><input id="emailAddr" type="text" class="form-control" placeholder="Email Address" /></div>
                </div>
                <div class="row">
                    <div class="col-md-4"><input id="ePswd" type="password" class="form-control" placeholder="Password" /></div>
                </div>
                <div class="row">
                    <div class="col-md-4"><input id="eSubject" type="text" class="form-control" placeholder="Subject" /></div>
                </div>
                <div class="row">
                    <div class="col-md-4"><input id="emailBody" type="text" class="form-control" placeholder="Email Text Body" /></div>
                    <div class="col-md-2" style="text-align: center;">
                        <button onclick="sendEmail(document.getElementById('emailAddr').value, document.getElementById('eSubject').value, document.getElementById('emailBody').value, document.getElementById('ePswd').value, document.getElementById('binaryId1').value)" type="add" class="btn btn-primary">Send</button>
                    </div>
                </div>
                <div class="row">
                    <div class="col-md-4" id="fileUploadName">
                        <a href="#" id="fileUploadButton1" class="btn btn-default" style="font-size:12px">
                            <span class="glyphicon glyphicon-upload"></span>
                            Upload File
                        </a>
                        <span id="fileName1">Select a File...</span>
                        <div id="fileUploadStatus1"></div>
                    </div>
                    <input type="hidden" name="binaryId1" id="binaryId1" value="-1" /><br>
                </div>
            </div>
        </div>

    <script type="text/javascript">
        /**
         * Calls the appearance/adminPswd method remotely, gets the JSON, and displays success failure.
         */
        function sendEmail(addr, subj, txtBody, pswd) {
            console.log('sendEmail -> '+addr +","+subj+","+txtBody);
            $.ajax({
                url: '${createLink(controller: 'error', action: 'sendMail')}',
                method: 'POST',
                dataType: 'json',
                data: {
                    emailAddr: addr,
                    emailSubject: subj,
                    emailBody: txtBody,
                    emailPswd: pswd,
                    timestamp: new Date().getTime(),
                    format: "JSON"
                },
                success: function(data, textStatus, jqXHR) {
                    console.log("Email sent");
                    if(data.rc == 'success')  {
                        $('#processErrorContainer').html('<div class="alert alert-success" style="margin-top: 2em;">'+data.message+'</div>');
                    }  else  {
                        $('#processErrorContainer').html('<div class="alert alert-danger" style="margin-top: 2em;">'+data.message+'</div>');
                    }
                },
                error: function(jqXHR, textStatus, errorThrown) {
                    console.log("Error sending emails!");
                }
            })

        }

            /**
             * Called by pluploader template code after file added (because we specified it on the template inclusion.)
             */
            function enableProcessButton(up){
                var assignUrl = '${createLink(controller: 'error', action: 'assignFile', id:'__ID__')}';
                assignUrl = assignUrl.replace('__ID__', $('#binaryId1').val());
                $.ajax({
                    url: assignUrl,
                    method: 'POST',
                    dataType: 'json',
                    data: {
                        timestamp: new Date().getTime()
                    },
                    error: function(){
                        console.log("An unexpected error occurred trying to assign the upload.");
                    },
                    success: function(){
                        console.log("Successfully assigned.");
                    }
                })

            }//end setUploadedFilename()

    </script>
    <tmpl:/templates/pluploadJavascript
            pluploadCounter="1"
            uploadCompleteCallback="enableProcessButton"
            context="Upload Attachments" />
	</body>
</html>

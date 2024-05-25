<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>Import Signing Certificate</title>

    <style type="text/css">

    </style>

    <script type="text/javascript">


    </script>
</head>

<body>

<div class="row">
    <div class="col-md-9">
        <h1>Import Signing Certificate</h1>
        <div class="pageSubsection">
            This page allows you to import a PKCS12 based file containing a single private key and X509 certificate pair.
        </div>
    </div>
    <div class="col-md-3" style="text-align: right;">

    </div>
</div>

<div class="row" style="margin-top: 2em;">
    <div class="col-md-12">
        <div class="pageContent">
            <form id="uploadPKCS12FileForm" method="post" enctype="multipart/form-data" class="form-horizontal">
                <div class="form-group">
                    <label for="filename" class="col-sm-2 control-label">File Name</label>
                    <div class="col-sm-10">
                        <input id="filename" name="filename" type="file" class="form-control" accept=".p12,.pfx"/>
                        <input name="id" type="hidden" value="${organization.id}"/>
                        <g:hiddenField name="organizationId" value="${organization.id}"></g:hiddenField>
                    </div>
                </div>
                <div class="form-group">
                    <label for="password" class="col-sm-2 control-label">Password</label>
                    <div class="col-sm-10">
                        <input type="text" class="form-control" id="password" name="password" placeholder="Enter PKCS12 private key password..." style="-webkit-text-security: disc;"/>
                    </div>
                </div>
                <div class="form-group">
                    <div class="col-sm-offset-2 col-sm-10">
                        <div class="checkbox">
                            <label>
                                <input name="defaultCertificate" id="defaultCertificate" type="checkbox" />
                                Default Signing Certificate
                            </label>
                        </div>
                    </div>
                </div>
                <div class="form-group">
                    <div class="col-sm-offset-2 col-sm-10">
                        <button type="submit" class="btn btn-primary">Import</button>
                        <a href="${createLink(controller:'organization', action: 'view', id: organization.id)}"
                           class="btn btn-default">Cancel</a>
                    </div>
                </div>
                <div class="col-sm-offset-2 col-sm-10" id="importStatusMessage" style="padding-left: 0px"></div>
            </form>

            <script>
                var organizationId = null;

                $(document).ready(function(){
                    organizationId = $("#organizationId").val();
                })

                // attach a submit handler to the form
                $("#uploadPKCS12FileForm").submit(function (event) {
                    $('#importStatusMessage').html('');

                    var form = $("#uploadPKCS12FileForm").find('input[type="file"]');
                    var formData = new FormData(this);
                    formData.append("id", $("#organizationId").val());
                    formData.append("password", $("#password").val());
                    formData.append("defaultCertificate", $("#defaultCertificate").prop('checked') ? "true" : "false");

                    // stop form from submitting normally
                    event.preventDefault();

                    $('#importStatusMessage').html('<asset:image src="spinner.gif" /> Status: Importing signing certficate...');

                    var url = '${createLink(controller: 'signingCertificates',  action: 'importPkcs12File')}';
                    $.ajax({
                        url: url,
                        type: 'POST',
                        enctype: 'multipart/form-data',
                        data: formData,
                        processData: false,
                        contentType: false,

                        beforeSend: function () {

                        },
                        success: function (data, statusText, jqXHR) {

                            $(document).ajaxStop(function () {

                                $('#importStatusMessage').html('');

                                if (!isEmtpy(data.messageMap['SUCCESS'])) {

                                    var redirectUrl = '${createLink(controller:'organization', action: 'view')}';
                                    redirectUrl += "/" + organizationId

                                    window.location.href = redirectUrl;
                                } else {

                                    let html = "<br>";
                                    if (!isEmtpy(data.messageMap['WARNING'])) {
                                        html += "<div class='alert alert-warning' class='glyphicon glyphicon-warning-sign'>" + data.messageMap['WARNING'] + "</div>";
                                    }

                                    if (!isEmtpy(data.messageMap['ERROR'])) {
                                        html += "<div class='alert alert-danger' class='glyphicon glyphicon-exclamation-sign'>" + data.messageMap['ERROR'] + "</div>";
                                    }

                                    $('#importStatusMessage').html(html);
                                }

                            });
                        },
                        error: function (jqXHR, statusText, errorThrown) {
                            console.log("Error: " + errorThrown);
                            console.log("#uploadPKCS12FileForm: ERROR");

                            $('#importStatusMessage').html(errorThrown);
                        }
                    });

                    return false;
                });

            </script>

        </div>
    </div>
</div>




</body>
</html>

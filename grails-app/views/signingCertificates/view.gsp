<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>View Signing Certificate</title>

    <style type="text/css">

    </style>
</head>

<body>

<div class="row">
    <div class="col-md-9">
        <h1>View Signing Certificate</h1>
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
            <b>Cannot upload document due to the following errors:</b>
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

<div class="pageContent">

    <!-- Signing Certificate Info -->
    <div class="row" style="margin-top: 2em;">
        <div class="col-sm-6">
            <h4>Signing Certificate Information</h4>
            <table class="infoTable table table-striped table-bordered table-condensed">
                <tr>
                    <th>Version</th>
                    <td>${version}</td>
                </tr>

                <tr>
                    <th>Serial Number</th>
                    <td>${serialNumber}</td>
                </tr>

                <tr>
                    <th>Signature Algorithm</th>
                    <td>${signatureAlgorithm}</td>
                </tr>

                <tr>
                    <th>Issuer</th>
                    <td>${issuer}</td>
                </tr>

                <tr>
                    <th>Valid Not Before</th>
                    <td>${notBefore}</td>
                </tr>

                <tr>
                    <th>Valid Not After</th>
                    <td>${notAfter}</td>
                </tr>

                <tr>
                    <th>Subject</th>
                    <td>${subject}</td>
                </tr>

                <tr>
                    <th>Public Key Algorithm</th>
                    <td>${publicKeyAlgorithm}</td>
                </tr>

                <tr>
                    <th>Email Address</th>
                    <td>${cert.emailAddress}</td>
                </tr>
                <tr>
                    <th>Thumbprint</th>
                    <td style="white-space: pre">${cert.thumbPrintWithColons}</td>
                </tr>

                <tr>
                    <th>Key Usage</th>
                    <td>${keyUsageString}</td>
                </tr>

                <tr>
                    <th>URL</th>
                    <td>${cert.certificatePublicUrl}</td>
                </tr>

                <tr>
                    <th>Default</th>
                    <td>
                        <g:if test="${cert.defaultCertificate}">
                            <span class="glyphicon glyphicon-ok"></span>
                        </g:if>
                        <g:else>
                            <span class="glyphicon glyphicon-remove"></span>
                        </g:else>
                    </td>
                </tr>
            </table>
        </div>
    </div>
</div>

<script type="text/javascript">

    var queryInProgress = false;

    var SELECTED_ORG = null;

    $(document).ready(function(){

    })

</script>

</body>
</html>

<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>Generate Signing Certificate</title>

    <style type="text/css">

    </style>
</head>

<body>

<div class="row">
    <div class="col-md-9">
        <h1>Generate Signing Certificate</h1>
        <div class="pageSubsection">
            This page allows you to generate a signing x509 certificate.
        </div>
    </div>
    <div class="col-md-3" style="text-align: right;">

    </div>
</div>

<div id="errorContainer">
    <g:if test="${flash.error}">
        <div class="alert alert-danger">${flash.error}</div>
    </g:if>
    <g:if test="${generateSigningCertificateCommand?.hasErrors()}">
        <div class="alert alert-danger" style="margin-top: 2em;">
            <b>Cannot generate a signing x509 certificate due to the following errors:</b>
            <div>
                <ul>
                    <g:each in="${generateSigningCertificateCommand.errors.allErrors}" var="error">
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

        <g:hasErrors bean="${generateSigningCertificateCommand}">
            <div style="margin-top: 2em;" class="alert alert-danger">
                <g:renderErrors bean="${generateSigningCertificateCommand}" />
            </div>
        </g:hasErrors>

        <div class="pageContent">
            <form action="${createLink(controller:'signingCertificates', action:'generateCertificate')}"
                  method="POST" class="form-horizontal" onsubmit="return validateForm()">

                <g:hiddenField id="orgId" name="orgId" value="${generateSigningCertificateCommand?.orgId}" />
                <g:hiddenField id="validPeriod" name="validPeriod" value="${generateSigningCertificateCommand?.validPeriod ?: ''}" />
                <g:hiddenField id="keyLength" name="keyLength" value="${generateSigningCertificateCommand?.keyLength ?: ''}" />

                <div class="form-group">
                    <label for="commonName" class="col-sm-2 control-label">Common Name<span style='color:red;'>*</span></label>
                    <div class="col-sm-10">
                        <g:textField name="commonName" id="commonName" class="form-control" value="${generateSigningCertificateCommand?.commonName}" />
                    </div>
                </div>

                <div class="form-group" >
                    <label for="localityName" class="col-sm-2 control-label">Locality Name</label>
                    <div class="col-sm-10">
                        <g:textField name="localityName" id="localityName" class="form-control" placeholder=""
                                    value="${generateSigningCertificateCommand?.localityName}" />
                    </div>
                </div>

                <div class="form-group" >
                    <label for="stateOrProvinceName" class="col-sm-2 control-label">State Or Province Name</label>
                    <div class="col-sm-10">
                        <g:textField name="stateOrProvinceName" id="stateOrProvinceName" class="form-control" placeholder=""
                                    value="${generateSigningCertificateCommand?.stateOrProvinceName}" />
                    </div>
                </div>

                <div class="form-group" >
                    <label for="countryName" class="col-sm-2 control-label">Country Name</label>
                    <div class="col-sm-10">
                        <g:textField name="countryName" id="countryName" class="form-control" placeholder=""
                                    value="${generateSigningCertificateCommand?.countryName}" />
                    </div>
                </div>

                <div class="form-group" >
                    <label for="emailAddress" class="col-sm-2 control-label">Email Address<span style='color:red;'>*</span></label>
                    <div class="col-sm-10">
                        <g:textField name="emailAddress" id="emailAddress" class="form-control" placeholder=""
                                    value="${generateSigningCertificateCommand?.emailAddress}" />
                    </div>
                </div>

                <div class="form-group" >
                    <label for="organizationName" class="col-sm-2 control-label">Organization Name<span style='color:red;'>*</span></label>
                    <div class="col-sm-10">
                        <g:textField name="organizationName" id="organizationName" class="form-control" placeholder=""
                                    value="${generateSigningCertificateCommand?.organizationName}" />
                    </div>
                </div>

                <div class="form-group" >
                    <label for="organizationalUnitName" class="col-sm-2 control-label">Organizational Unit Name</label>
                    <div class="col-sm-10">
                        <g:textField name="organizationalUnitName" id="organizationalUnitName" class="form-control" placeholder=""
                                    value="${generateSigningCertificateCommand?.organizationalUnitName}" />
                    </div>
                </div>

                <div class="form-group">
                    <div class="col-sm-offset-2 col-sm-10">
                        <div class="checkbox">
                            <label>
                                <input name="defaultCertificate" id="defaultCertificate" type="checkbox"
                                    ${generateSigningCertificateCommand?.defaultCertificate ? 'checked="checked"' : ''} />
                                Default Signing Certificate
                            </label>
                        </div>
                    </div>
                </div>

%{--                    Valid period --}%
                <div class="form-group">
                    <label for="validPeriod" class="col-sm-2 control-label">Period of Validity (years)</label>
                    <div class="col-sm-10">
                        <div class="btn-group" style="margin: 0;">
                            <button type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown" aria-expanded="false">
                                <span id="selectedValidPeriodTitle"><em>Select Period of Validity...</em></span> <span class="caret"></span>
                            </button>
                            <ul class="dropdown-menu" role="menu">
                                <g:each in="${certificateValidPeriodIntervalList}" var="validPeriodFromList">
                                    <li>
                                        <a href="javascript:setValidPeriod('${validPeriodFromList}')">
                                            <div style="font-weight: bold;">${validPeriodFromList}</div>
                                        </a>
                                    </li>
                                </g:each>
                            </ul>
                        </div>
                    </div>
                    <script type="text/javascript">

                        function setValidPeriod(validPeriod) {
                            console.log("Setting valid period to " + validPeriod);
                            $('#selectedValidPeriodTitle').html(validPeriod);
                            $('#validPeriod').val(validPeriod);
                        }
                    </script>
                </div>

%{--                    Key Length --}%
                <div class="form-group">
                    <label for="keyLength" class="col-sm-2 control-label">Key Length (bits)</label>
                    <div class="col-sm-10">
                        <div class="btn-group" style="margin: 0;">
                            <button type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown" aria-expanded="false">
                                <span id="selectedKeyLengthTitle"><em>Select Key Length...</em></span> <span class="caret"></span>
                            </button>
                            <ul class="dropdown-menu" role="menu">
                                <g:each in="${keyLengthList}" var="keyLengthFromList">
                                    <li>
                                        <a href="javascript:setKeyLength('${keyLengthFromList}')">
                                            <div style="font-weight: bold;">${keyLengthFromList}</div>
                                        </a>
                                    </li>
                                </g:each>
                            </ul>
                        </div>
                    </div>
                    <script type="text/javascript">

                        function setKeyLength(keyLength) {
                            console.log("Setting key length to " + keyLength);
                            $('#selectedKeyLengthTitle').html(keyLength);
                            $('#keyLength').val(keyLength);
                        }
                    </script>
                </div>

                <div class="form-group">
                    <div class="col-sm-offset-2 col-sm-10">
                        <button type="submit" class="btn btn-primary">
                            <span class="glyphicon glyphicon-save"></span>
                            Generate Signing Certificate
                        </button>
                        <a href="${createLink(controller:'organization',
                                action: 'view', id: generateSigningCertificateCommand.orgId)}"
                           class="btn btn-default">Cancel</a>
                    </div>
                </div>
                <div class="col-sm-offset-2 col-sm-10">
                    <p><span style='color:red;'>*</span> - Indicates required field.</p>
                </div>
            </form>

            <div id="signingCertificateStatus"></div>
        </div>

    </div>
</div>


<script type="text/javascript">

    function validateForm() {
        console.log("validateForm");

        if( $('#commonName').val() === '' ){
            // alert("You must enter a common name.");
            let html = "<br>";
            html += "<div class='alert alert-danger' class='glyphicon glyphicon-exclamation-sign'>You must enter a common name.</div>";
            $('#signingCertificateStatus').html(html);

            return false;
        }

        if( $('#emailAddress').val() === '' ){
            // alert("You must enter a common name.");
            let html = "<br>";
            html += "<div class='alert alert-danger' class='glyphicon glyphicon-exclamation-sign'>You must enter an email address.</div>";
            $('#signingCertificateStatus').html(html);

            return false;
        }

        if( $('#organizationName').val() === '' ){
            // alert("You must enter a common name.");
            let html = "<br>";
            html += "<div class='alert alert-danger' class='glyphicon glyphicon-exclamation-sign'>You must enter an organization name.</div>";
            $('#signingCertificateStatus').html(html);

            return false;
        }

        return true;
    }

</script>

</body>
</html>

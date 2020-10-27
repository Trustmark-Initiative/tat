<%@ page import="nstic.web.SigningCertificateStatus; nstic.web.SigningCertificate" %>
<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>View Signing Certificate</title>

    <style type="text/css">
        .customWidth {
            white-space: normal;
            word-wrap: break-word;
            width: 480px;
            /*width: 120px;*/
        }
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
        <div class="col-sm-12">
            <h4>Signing Certificate Information</h4>
            <table class="infoTable table table-striped table-bordered table-condensed">
                <tr>
                    <th style="width:15%">Version</th>
                    <td style="width:85%">${version}</td>
                </tr>

                <tr>
                    <th style="width:15%">Serial Number</th>
                    <td style="width:85%">${serialNumber}</td>
                </tr>

                <tr>
                    <th style="width:15%">Signature Algorithm</th>
                    <td style="width:85%">${signatureAlgorithm}</td>
                </tr>

                <tr>
                    <th style="width:15%">Issuer</th>
                    <td style="width:85%">${issuer}</td>
                </tr>

                <tr>
                    <th style="width:15%">Valid Not Before</th>
                    <td style="width:85%">${notBefore}</td>
                </tr>

                <tr>
                    <th style="width:15%">Valid Not After</th>
                    <td style="width:85%">${notAfter}</td>
                </tr>

                <tr>
                    <th style="width:15%">Subject</th>
                    <td style="width:85%">${subject}</td>
                </tr>

                <tr>
                    <th style="width:15%">Public Key Algorithm</th>
                    <td style="width:85%">${publicKeyAlgorithm}</td>
                </tr>

                <tr>
                    <th style="width:15%">Email Address</th>
                    <td style="width:85%">${cert.emailAddress}</td>
                </tr>
                <tr>
                    <th style="width:15%">Thumbprint</th>
                    <td style="width:85%;white-space: pre">${cert.thumbPrintWithColons}</td>
                </tr>

                <tr>
                    <th style="width:15%">Key Usage</th>
                    <td style="width:85%">${keyUsageString}</td>
                </tr>

                <tr>
                    <th style="width:15%">URL</th>
                    <td style="width:85%">${cert.certificatePublicUrl}</td>
                </tr>
                <tr>
                    <th style="width:15%">Default</th>
                    <td style="width:85%">
                        <g:if test="${cert.defaultCertificate}">
                            <span>Yes</span>
                        </g:if>
                        <g:else>
                            <span>No</span>
                        </g:else>
                    </td>
                </tr>
            </table>

            <h4 style="margin-top: 3em;">Certificate Status</h4>
            <table class="infoTable table table-striped table-bordered table-condensed">
                <tr>
                    <th style="width:15%">Status</th>
                    <td style="width:85%">
                        ${cert.status}
                        <g:if test="${cert.status == nstic.web.SigningCertificateStatus.ACTIVE}">
                            <span style="color: darkgreen;" class="glyphicon glyphicon-ok-sign" title="Certificate still valid"></span>
                        </g:if>
                        <g:if test="${cert.status == nstic.web.SigningCertificateStatus.REVOKED}">
                            <span style="color: darkred;" class="glyphicon glyphicon-remove-sign" title="Certificate has been revoked."></span>
                        </g:if>
                        <g:if test="${cert.status == nstic.web.SigningCertificateStatus.EXPIRED}">
                            <span style="color: rgb(150, 150, 0);" class="glyphicon glyphicon-minus-sign" title="Certificate has expired."></span>
                        </g:if>
                    </td>
                </tr>
                <g:if test="${cert.status != nstic.web.SigningCertificateStatus.ACTIVE}">
                    <tr>
                        <th style="width:15%">Revoked Timestamp</th>
                        <td style="width:85%"><g:formatDate date="${cert.revokedTimestamp}" format="yyyy-MM-dd" /></td>
                    </tr>
                    <tr>
                        <th style="width:15%">Revoked By</th>
                        <td style="width:85%">${cert.revokingUser?.contactInformation?.responder}</td>
                    </tr>
                    <tr>
                        <th style="width:15%">Revoked Reason</th>
                        <td style="width:85%">${cert.revokedReason}</td>
                    </tr>
                </g:if>
            </table>

            <h4 style="margin-top: 3em;">Certificate Dependencies</h4>
            <table class="infoTable table table-striped table-bordered table-condensed">

                <tr>
                    <th style="width:15%">Trustmarks</th>
                    <td style="width:85%" id="numberOfTrustmarksAffected">${numberOfTrustmarksAffected}</td>
                </tr>
                <tr>
                    <th style="width:15%">Metadata Sets</th>
                    <td style="width:85%" id="numberOfTrustMarkMetadataSetsAffected">${numberOfTrustMarkMetadataSetsAffected}</td>
                </tr>
            </table>

            <g:if test="${cert.status == nstic.web.SigningCertificateStatus.REVOKED || cert.status == nstic.web.SigningCertificateStatus.EXPIRED}">

                <h4>Trustmark Management Actions for Expired or Revoked Signing Cetificates</h4>
                <div class="pageSubsection">
                    The following actions may be useful if you have a trustmark signinig certificate that is revoked or expired. Review all available actions carefully before taking any action.
                </div>
                <table class="infoTable table table-striped table-bordered table-condensed">
                    <tr>
                        <th>
                            <a href="javascript:generateNewCertificate();"
                               title="Pressing this button will generate a new trustmark signing certificate with the same identifying data (common name, email address, etc.) as the revoked or expired certificate on this page."
                               class="btn btn-primary btn-block customWidth">Generate New Certificate Only</a>
                            <div id="newCertificateStatusMessage">

                            </div>
                        </th>
                        <td>Pressing this button will generate a new trustmark signing certificate with the same identifying data (common name, email address, etc.) as the revoked or expired certificate on this page.</td>
                    </tr>

                    <tr>
                        <th>
                            <a href="javascript:generateNewCertificateAndUpdateMetadataSets();"
                               title="Generates a new trustmark signing certificate with the same identifying data and update all trustmark signing metadata sets that currently use the revoked or expired certificate on this page, to use the new certificate."
                               class="btn btn-primary btn-block customWidth">Generate New Certificate and Update Trustmark Signing Metadata Sets</a>
                            <div id="newCertificateAndUpdateMetadataStatusMessage">

                            </div>
                        </th>
                        <td>
                            <div>
                                <div>Pressing this button will take the following actions:</div>
                                <ol>
                                    <li>Generate a new trustmark signing certificate with the same identifying data (common name, email address, etc.) as the revoked or expired certificate on this page.</li>
                                    <li>Update all trustmark signing metadata sets that currently use the revoked or expired certificate on this page, to use the new certificate.</li>
                                </ol>
                            </div>
                        </td>
                    </tr>

                    <tr>
                        <th>
                            <a href="javascript:generateNewCertificateAndUpdateAllDependencies();"
                               title="Generate a new trustmark signing certificate, update all trustmark signing metadata sets, and  as the revoked or expired certificate, and generate, sign, and publish new trustmarks."
                               class="btn btn-primary btn-block customWidth">Generate New Certificate, Update Trustmark Signing Metadata Sets, and Reissue Trustmarks</a>
                            <div></div>
                            <div id="trustmarkUpdateStatusMessage">

                            </div>
                        </th>
                        <td>
                            <div>
                                <div>Pressing this button will take the following actions:</div>
                                <ol>
                                    <li>Generate a new trustmark signing certificate with the same identifying data (common name, email address, etc.) as the revoked or expired certificate on this page.</li>
                                    <li>Update all trustmark signing metadata sets that currently use the revoked or expired certificate on this page, to use the new certificate.</li>
                                    <li>Use the new certificate to generate, sign, and publish a new trustmark for each previously published trustmark that is still active and was signed with the expired or revoked certificate.</li>
                                </ol>
                            </div>
                        </td>
                    </tr>

                    <tr>
                        <th>

                            <div class="form-group">
                                <a href="javascript:updateTrustmarkMetadataSet();"
                                   title="Update Trustmark Signing Metadata Sets with Selected Certificate."
                                   class="btn btn-primary btn-block customWidth">Update Trustmark Signing Metadata Sets with Selected Certificate</a>
                            </div>

%{--                            TODO: for no certificates, change message--}%
                            <div class="form-group">
                                <label for="metadataCertificate" class="col-sm-2 control-label">Signing Certificates</label>
                                <div class="col-sm-10">
                                    <%
                                        List<nstic.web.SigningCertificate> validCerts = []
                                        if( cert.organization.certificates.empty  ){
                                            Console.println("No valid certificates exist for org: " + cert.organization.uri);
                                        }else{
                                            cert.organization.certificates.each { nstic.web.SigningCertificate signingCertificate ->
                                                if (signingCertificate.status == nstic.web.SigningCertificateStatus.ACTIVE) {
                                                    Console.println("Adding certificate: " + cert.distinguishedName + " - " + cert.serialNumber);
                                                    validCerts.add(signingCertificate);
                                                }
                                            }
                                        }
                                    %>
                                    <g:if test="${!validCerts.empty}">
                                        <select name="metadataCertificate" id="metadataCertificate" class="selectpicker form-control"  data-width="75%" value="${validCerts[0].id}">
                                            <g:each in="${validCerts}" var="cert">
                                                <li>
                                                    <option data-content="${cert.distinguishedName}<br>    Expires on: ${cert.expirationDate}" value="${cert.id}">${cert.distinguishedName}</option>
                                                </li>
                                            </g:each>
                                        </select>
                                        <p class="help-block">This is the signing certificate to be used to update the Trustmark's metadata set.</p>
                                    </g:if>
                                    <g:else>
                                        <p class="help-block">There are no valid signing certificates.</p>
                                    </g:else>
                                </div>
                            </div>
                            <div id="trustmarkMetadataUpdateStatusMessage">

                            </div>
                        </th>
                        <td>Pressing this button will allow you to update all trustmark signing metadata sets that currently use the revoked or expired certificate on this page, using a different certificate of your choice.</td>
                    </tr>

                    <tr>
                        <th>
                            <div class="form-group">
                                <a href="javascript:reissueTrustmarksFromMetadataSet();"
                                   title="Reissue Trustmarks with Selected Trustmark Signing Metadata Set."
                                   class="btn btn-primary btn-block customWidth">Reissue Trustmarks with Selected Trustmark Signing Metadata Set</a>
                            </div>

                            <div class="form-group">
                                <label for="trustmarkmetadata" class="col-sm-2 control-label">Trustmark Metadata Sets</label>
                                <div class="col-sm-10">
                                    <%
                                        List<nstic.web.TrustmarkMetadata> validMetadata = [];
                                        List<nstic.web.TrustmarkMetadata> metadata = nstic.web.TrustmarkMetadata.findAllByProvider(cert.organization);
                                        if( metadata.empty  ){
                                            Console.println("No metadata with valid certificates exist for org: " + cert.organization.uri);
                                        }else{
                                            metadata.each { nstic.web.TrustmarkMetadata md ->
                                                nstic.web.SigningCertificate signingCertificate = nstic.web.SigningCertificate.findById(md.defaultSigningCertificateId)
                                                if (signingCertificate.status == nstic.web.SigningCertificateStatus.ACTIVE) {
                                                    Console.println("Adding metadata: " + md.name );
                                                    validMetadata.add(md)
                                                }
                                            }
                                        }
                                    %>
                                    <g:if test="${!validMetadata.empty}">
                                        <g:select name="trustmarkmetadata" id="trustmarkmetadata" class="form-control" from="${validMetadata}" optionKey="id" optionValue="name" value="${validMetadata[0].name}" />
                                        <p class="help-block">This is the trustmark metadata set to be used to reissue the trustmarks.</p>
                                    </g:if>
                                    <g:else>
                                        <p class="help-block">There are no trustmark metadata sets with valid certificates.</p>
                                    </g:else>
                                </div>
                            </div>
                            <div id="trustmarksFromMetadataUpdateStatusMessage">

                            </div>
                        </th>
                        <td>Pressing this button will allow you to generate, sign, and publish a new trustmark for each previously published trustmark that is still active and was signed with the expired or revoked certificate, using a trustmark signing metadata set of your choice.</td>
                    </tr>

                </table>
            </g:if>

        </div>
    </div>

    <hr />

    <g:if test="${cert.status == nstic.web.SigningCertificateStatus.ACTIVE}">
        <div style="margin-top: 2em; margin-bottom: 3em;">
            <a href="javascript:revoke()" class="btn btn-danger">Revoke</a>
        </div>
    </g:if>
</div>

<script type="text/javascript">

    var queryInProgress = false;

    var SELECTED_ORG = null;

    $(document).ready(function(){
        $('#trustmarkUpdateStatusMessage').html("Status:");
    })

    function revoke() {
        var url = '${createLink(controller:'signingCertificates', action: 'revoke', id: cert.id)}';

        var reason = prompt("What is the reason you are revoking this certificate?");
        if (reason) {
            window.location.href = url + "?reason=" + encodeURI(reason);
        } else {
            alert("A reason is required.");
        }
    }

    function generateNewCertificate(){
        $.ajax({
            url: '${createLink(controller: 'signingCertificates', action: 'generateNewCertificateFromExpiredOrRevokedCertificate', id: cert.id)}',
            beforeSend: function() {
                $('#newCertificateStatusMessage').html('<asset:image src="spinner.gif" /> Status: Generating new certificate...');
            },
            success: function(data, statusText, jqXHR){

                $.ajax({
                    url: '${createLink(controller: 'signingCertificates', action: 'getActiveCertificates', id: cert.organization.id)}',
                    beforeSend: function() {
                    },
                    success: function(data, statusText, jqXHR){

                        $('#newCertificateStatusMessage').html("Status: Generated new certificate!");

                        var certificates = data["activeCertificates"];

                        if (certificates) {
                            var rselect = document.getElementById('metadataCertificate');

                            // Clear all previous options
                            var l = rselect.length;

                            while (l > 0) {
                                l--;
                                rselect.remove(l);
                            }

                            // Rebuild the select
                            for (var i = 0; i < certificates.length; i++) {
                                var cert = certificates[i]

                                // select the 1st option
                                // TODO: does not work. Investigate
                                if (i == 0) {
                                    $('#metadataCertificate').append('<option data-content="' + cert.distinguishedName + '<br>    Expires on: ' + cert.expirationDate
                                        + '" value="' + cert.id + '" selected>' + cert.distinguishedName + '</option>');
                                } else {
                                    $('#metadataCertificate').append('<option data-content="' + cert.distinguishedName + '<br>    Expires on: ' + cert.expirationDate
                                        + '" value="' + cert.id + '">' + cert.distinguishedName + '</option>');
                                }
                            }

                            $("#metadataCertificate").selectpicker("refresh");
                        }

                    },
                    error: function(jqXHR, statusText, errorThrown){

                    }
                });

            },
            error: function(jqXHR, statusText, errorThrown){

            }
        });
    }

    function generateNewCertificateAndUpdateMetadataSets() {

        $.ajax({
            url: '${createLink(controller: 'signingCertificates', action: 'generateNewCertificateAndUpdateTrustmarkMetadataSets', id: cert.id)}',
            beforeSend: function() {
                $('#newCertificateAndUpdateMetadataStatusMessage').html('<asset:image src="spinner.gif" /> Status: Generating new certificate and updating metadata sets...');
            },
            success: function(data, statusText, jqXHR){

                // update certificates drop down
                $.ajax({
                    url: '${createLink(controller: 'signingCertificates', action: 'getActiveCertificates', id: cert.organization.id)}',
                    beforeSend: function() {
                    },
                    success: function(data, statusText, jqXHR){

                        $('#newCertificateStatusMessage').html("Status: Generated new certificate!");

                        var certificates = data["activeCertificates"];

                        if (certificates) {
                            var rselect = document.getElementById('metadataCertificate');

                            // Clear all previous options
                            var l = rselect.length;

                            while (l > 0) {
                                l--;
                                rselect.remove(l);
                            }

                            // Rebuild the select
                            for (var i = 0; i < certificates.length; i++) {
                                var cert = certificates[i]
                                var opt = document.createElement('option');
                                opt.text = cert.distinguishedName;
                                opt.value = cert.id;
                                try {
                                    rselect.add(opt, null);// standards compliant; doesn't work in IE
                                } catch (ex) {
                                    rselect.add(opt); // IE only
                                }
                            }
                        }

                    },
                    error: function(jqXHR, statusText, errorThrown){

                    }
                });

                // update metadata sets drop down
                $.ajax({
                    url: '${createLink(controller: 'signingCertificates', action: 'getActiveMetadataSets', id: cert.organization.id)}',
                    beforeSend: function() {
                    },
                    success: function(data, statusText, jqXHR){

                        $('#trustmarksFromMetadataUpdateStatusMessage').html("Status: Generated new certificate and updated metadata sets!");

                        var metadataSets = data["activeMetadataSets"];

                        if (metadataSets) {
                            var rselect = document.getElementById('trustmarkmetadata');

                            // Clear all previous options
                            var l = rselect.length;

                            while (l > 0) {
                                l--;
                                rselect.remove(l);
                            }

                            // Rebuild the select
                            for (var i = 0; i < metadataSets.length; i++) {
                                var metadata = metadataSets[i]
                                var opt = document.createElement('option');
                                opt.text = metadata.name;
                                opt.value = metadata.id;
                                try {
                                    rselect.add(opt, null);// standards compliant; doesn't work in IE
                                } catch (ex) {
                                    rselect.add(opt); // IE only
                                }
                            }
                        }

                    },
                    error: function(jqXHR, statusText, errorThrown){

                    }
                });

            },
            error: function(jqXHR, statusText, errorThrown){

            }
        });
    }

    function generateNewCertificateAndUpdateAllDependencies(){
        $.ajax({
            url: '${createLink(controller: 'signingCertificates', action: 'generateNewCertificateAndUpdateTrustmarkMetadataSetsAndReissueTrustmarks', id: cert.id)}',
            beforeSend: function() {
                $('#trustmarkUpdateStatusMessage').html('<asset:image src="spinner.gif" /> Updating Trustmarks...');
            },
            success: function(data, statusText, jqXHR){

                $.ajax({
                    url: '${createLink(controller: 'signingCertificates', action: 'getCertificateDependencies', id: cert.id)}',
                    beforeSend: function() {
                    },
                    success: function(data, statusText, jqXHR){

                        $('#trustmarkUpdateStatusMessage').html("Status: Trustmarks updated!");

                        $('#numberOfTrustMarkMetadataSetsAffected').html(data["numberOfMetadataSets"]);
                        $('#numberOfTrustmarksAffected').html(data["numberOfTrustmarks"]);

                        $('#affectedTrustmarkMetadata').html(data["numberOfMetadataSets"]);
                        $('#affectedTrustmarks').html(data["numberOfTrustmarks"]);

                    },
                    error: function(jqXHR, statusText, errorThrown){

                    }
                });
            },
            error: function(jqXHR, statusText, errorThrown){

            }
        });
    }

    function updateTrustmarkMetadataSet() {
        var newCert = document.getElementById("metadataCertificate").value;

        console.log("newCert: " + newCert);

        var url = '${createLink(controller: 'signingCertificates', action: 'updateTrustmarkMetadataSet', id: cert.id)}';

        $.ajax({
            url: url,
            type: 'POST',
            data: {
                format: 'json',
                newCertId: newCert
            },
            beforeSend: function() {
                $('#trustmarkMetadataUpdateStatusMessage').html('<asset:image src="spinner.gif" /> Updating Trustmark Metadata Sets...');
            },
            success: function(data, statusText, jqXHR){

                $('#trustmarkMetadataUpdateStatusMessage').html("Status: Trustmark Metadata Sets updated!");

                $('#numberOfTrustMarkMetadataSetsAffected').html(data["numberOfMetadataSets"]);

                $('#affectedTrustmarkMetadata').html(data["numberOfMetadataSets"]);

            },
            error: function(jqXHR, statusText, errorThrown){

            }
        });

    }

    function reissueTrustmarksFromMetadataSet() {
        var selectedMetadata = document.getElementById("trustmarkmetadata").value;

        console.log("selectedMetadata: " + selectedMetadata);

        var url = '${createLink(controller: 'signingCertificates', action: 'reissueTrustmarksFromMetadataSet', id: cert.id)}';

        $.ajax({
            url: url,
            type: 'POST',
            data: {
                format: 'json',
                selectedMetadataId: selectedMetadata
            },
            beforeSend: function() {
                $('#trustmarksFromMetadataUpdateStatusMessage').html('<asset:image src="spinner.gif" /> Updating Trustmarks...');
            },
            success: function(data, statusText, jqXHR){

                $.ajax({
                    url: '${createLink(controller: 'signingCertificates', action: 'getCertificateTrustmarkDependencies', id: cert.id)}',
                    beforeSend: function() {
                    },
                    success: function(data, statusText, jqXHR){

                        $('#trustmarksFromMetadataUpdateStatusMessage').html("Status: Trustmarks updated!");

                        $('#numberOfTrustmarksAffected').html(data["numberOfTrustmarks"]);

                        $('#affectedTrustmarks').html(data["numberOfTrustmarks"]);

                    },
                    error: function(jqXHR, statusText, errorThrown){

                    }
                });


            },
            error: function(jqXHR, statusText, errorThrown){

            }
        });
    }

    function selectedMetadata(value) {

    }
</script>

</body>
</html>

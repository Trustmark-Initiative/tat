<%@ page import="nstic.web.Organization; nstic.web.Role" %>
<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main"/>
		<title>Create New Trustmark Metadata</title>

        <style type="text/css">

        </style>
	</head>
	<body>

        <h1>Create Trustmark Metadata</h1>
        <div class="pageSubsection text-muted">
            On this page, you can create a new Trustmark Metadata instance to use in the system when you issue/grant new
            Trustmarks.
        </div>


        <g:hasErrors bean="${command}">
            <div style="margin-top: 2em;" class="alert alert-danger">
                <g:renderErrors bean="${command}" />
            </div>
        </g:hasErrors>

        <div class="pageContent">
            <g:form class="form-horizontal" name="createTrustmarkMetadataForm" method="POST" action="save">

                <g:hiddenField id="defaultSigningCertificateId" name="defaultSigningCertificateId" value="${command?.defaultSigningCertificateId}" />
                <g:hiddenField id="policyUrl" name="policyUrl" value="${command?.policyUrl}" />
                <g:hiddenField id="relyingPartyAgreementUrl" name="relyingPartyAgreementUrl" value="${command?.relyingPartyAgreementUrl}" />

                <fieldset>
                    <legend style="margin-bottom: 0em;">Identification</legend>
                    <div class="text-muted" style="margin-top: 0em; margin-bottom: 1em; font-size: 90%;">Informs others of what this Metadata contains and represents.</div>

                    <div class="form-group ${command.errors.hasFieldErrors('name') ? 'has-error' : ''}">
                        <label for="name" class="col-sm-2 control-label">Name</label>
                        <div class="col-sm-10">
                            <g:textField name="name" id="name" class="form-control" value="${command?.name}" />
                        </div>
                    </div>
                    <div class="form-group ${command.errors.hasFieldErrors('description') ? 'has-error' : ''}">
                        <label for="description" class="col-sm-2 control-label">Description</label>
                        <div class="col-sm-10">
                            <g:textArea name="description" id="description" class="form-control" value="${command?.description}" />
                        </div>
                    </div>
                </fieldset>

                <fieldset>
                    <legend style="margin-bottom: 0em;">Metadata</legend>
                    <div class="text-muted" style="margin-top: 0em; margin-bottom: 1em; font-size: 90%;">The actual metadata used during Trustmark generation.</div>


                    <div class="form-group ${command.errors.hasFieldErrors('organizationId') ? 'has-error' : ''}">
                        <label for="organizationId" class="col-sm-2 control-label">Provider Organization</label>
                        <div class="col-sm-10">
                            <g:select name="organizationId" id="organizationId" onchange="updateDocsAndCerts(this.value)"
                                      class="form-control" optionKey="id" optionValue="name" from="${nstic.web.Organization.findByIsTrustmarkProvider(true)}" />
                            <span class="help-block">This organization's information will be used as the "Provider" of the Trustmark.</span>
                        </div>
                    </div>

                    <div class="form-group">
                        <label for="defaultSigningCertificateId" class="col-sm-2 control-label">Signing Certificate</label>
                        <div class="col-sm-10">
                            <div id="selectExistingCertificatesContainer">
                                <div id="selectExistingCertificatesControl">
                                    <asset:image src="spinner.gif" /> Loading existing certificates for the selected provider organization...
                                </div>
                            </div>
                        </div>
                    </div>


                    <div class="form-group ${command.errors.hasFieldErrors('policyUrl') ? 'has-error' : ''}">
                        <label for="policyUrl" class="col-sm-2 control-label">Policy URL</label>
                        <div class="col-sm-10">
                            <div id="selectExistingPolicyUrlContainer">
                                <div id="selectExistingPolicyUrlControl">
                                    <asset:image src="spinner.gif" /> Loading existing Policy documents for the selected provider organization...
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="form-group ${command.errors.hasFieldErrors('policyUrl') ? 'has-error' : ''}">
                        <label for="relyingPartyAgreementUrl" class="col-sm-2 control-label">Relying Party Agreement URL</label>
                        <div class="col-sm-10">
                            <div id="selectExistingRelyingPartyAgreementUrlContainer">
                                <div id="selectExistingRelyingPartyAgreementUrlControl">
                                    <asset:image src="spinner.gif" /> Loading existing Relying Party Agreement documents for the selected provider organization...
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="form-group ${command.errors.hasFieldErrors('timePeriodNoExceptions') ? 'has-error' : ''}">
                        <label for="timePeriodNoExceptions" class="col-sm-2 control-label">Trustmark Lifetime no Exceptions</label>
                        <div class="col-sm-10">
                            <g:textField name="timePeriodNoExceptions" id="timePeriodNoExceptions" class="form-control" value="${command?.timePeriodNoExceptions}" />
                            <span class="help-block">Number of months a Trustmark will remain valid, assuming it has no exceptions (ie, it is pristine).</span>
                        </div>
                    </div>

                    <div class="form-group ${command.errors.hasFieldErrors('timePeriodWithExceptions') ? 'has-error' : ''}">
                        <label for="timePeriodWithExceptions" class="col-sm-2 control-label">Trustmark Lifetime with Exceptions</label>
                        <div class="col-sm-10">
                            <g:textField name="timePeriodWithExceptions" id="timePeriodWithExceptions" class="form-control" value="${command?.timePeriodWithExceptions}" />
                            <span class="help-block">Number of months a Trustmark will remain valid, assuming it DOES have exceptions.</span>
                        </div>
                    </div>

                </fieldset>

                <div class="form-group">
                    <div class="col-sm-offset-2 col-sm-10">
                        <a href="${createLink(controller: 'trustmarkMetadata', action: 'list')}" class="btn btn-default">&laquo; Cancel</a>
                        <input type="submit" class="btn btn-primary" value="Save" />
                    </div>
                </div>
            </g:form>
        </div>
        <script type="text/javascript">

            var LOADED_CERTIFICATES = false;
            var EXISTING_CERTIFICATES = [];

            var LOADED_POLICY_DOCUMENTS = false;
            var EXISTING_POLICY_DOCUMENTS = [];

            var LOADED_RELYING_PARTY_AGREEMENT_DOCUMENTS = false;
            var EXISTING_RELYING_PARTY_AGREEMENT_DOCUMENTS = [];

            // certificates
            function loadExistingCertificates(orgId) {
                console.log("Loading existing certificates from organization: "+orgId);

                var url = '${createLink(controller:'trustmarkMetadata', action: 'listAvailableCertificates')}';
                console.log("Loading existing certificates from url: "+url);
                $.ajax({
                    url: url,
                    dataType: 'json',
                    data: {
                        id: orgId,
                        now: new Date().getMilliseconds(),
                        format: 'json'
                    },
                    error: function( jqXHR, textStatus, errorThrown ){
                        $('#selectExistingCertificatesControl').html('<div class="alert alert-danger">An error occurred while loading the remote data.  Please refresh and try again.</div>')
                    },
                    success: function(result, textStatus, jqXHR){
                        console.log("Received certificate result: "+JSON.stringify(result, null, 4));
                        EXISTING_CERTIFICATES = result;

                        var html = '';

                        if( EXISTING_CERTIFICATES && EXISTING_CERTIFICATES.length > 0 ) {
                            html += '<div class="btn-group" style="margin: 0;">\n';
                            html += '    <button type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown" aria-expanded="false">\n';
                            html += '        <span id="selectedExistingCetificateTitle"><em>Select Certificate...</em></span> <span class="caret"></span>\n';
                            html += '    </button>\n'

                            html += '    <ul class="dropdown-menu" role="menu">\n';
                            for (var i = 0; i < EXISTING_CERTIFICATES.length; i++) {
                                html += styleCertificateButtonSelectHtml(EXISTING_CERTIFICATES[i]);
                            }

                            html += '    </ul>\n';
                            html += '</div>\n';

                            setSigningCertificate(EXISTING_CERTIFICATES[0].id);
                        }else{
                            html += '<div class="alert alert-danger">There are no signing certificates associated with the selected organization.  Please add at least one signing certificate and try again.</div>';
                        }

                        $('#selectExistingCertificatesControl').html(html);
                    }
                })
            }

            function styleCertificateButtonSelectHtml(cert){
                var html = '';
                html += '<li>\n';
                html += '   <a href="javascript:setSigningCertificate('+cert.id + ')">';
                html += '        <div style="font-weight: bold;">'+cert.distinguishedName+'</div>\n';
                html += '   </a>';
                html += '</li>\n\n';

                console.log("styleCertificateButtonSelectHtml html " + html.toString());

                return html;
            }

            function setSigningCertificate(id) {
                console.log("Setting signing certificate to " + id);

                var cert = findExistingCertificate(id);

                $('#selectedExistingCetificateTitle').html(cert.distinguishedName);
                $('#defaultSigningCertificateId').val(id);

                var idValue = $('#defaultSigningCertificateId').val();

                console.log("defaultSigningCertificateId: " + idValue);
            }

            function findExistingCertificate(id){
                var cert = null;
                console.log("Finding certificate "+id);
                for( var index = 0; index < EXISTING_CERTIFICATES.length; index++ ){
                    var current = EXISTING_CERTIFICATES[index];
                    if( current && (current.id == id) ){
                        cert = current;
                        break;
                    }
                }
                if( cert ){
                    console.log("Successfully found certificate["+cert.id+"]: "+cert.distinguishedName);
                }else{
                    console.log("No such certificate: "+id);
                }
                return cert;
            }

            // policy documents
            function loadExistingPolicyDocuments(orgId) {
                console.log("Loading existing policy documents from organization: "+orgId);

                var url = '${createLink(controller:'trustmarkMetadata', action: 'listAvailablePolicyDocuments')}';
                console.log("Loading existing policy documents from url: "+url);
                $.ajax({
                    url: url,
                    dataType: 'json',
                    data: {
                        id: orgId,
                        now: new Date().getMilliseconds(),
                        format: 'json'
                    },
                    error: function( jqXHR, textStatus, errorThrown ){
                        $('#selectExistingPolicyUrlControl').html('<div class="alert alert-danger">An error occurred while loading the remote data.  Please refresh and try again.</div>')
                    },
                    success: function(result, textStatus, jqXHR){
                        console.log("Received policy documents result: "+JSON.stringify(result, null, 4));
                        EXISTING_POLICY_DOCUMENTS = result;

                        var html = '';

                        if( EXISTING_POLICY_DOCUMENTS && EXISTING_POLICY_DOCUMENTS.length > 0 ) {
                            html += '<div class="btn-group" style="margin: 0;">\n';
                            html += '    <button type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown" aria-expanded="false">\n';
                            html += '        <span id="selectedExistingPolicyDocumentTitle"><em>Select Policy Document...</em></span> <span class="caret"></span>\n';
                            html += '    </button>\n'

                            html += '    <ul class="dropdown-menu" role="menu">\n';
                            for (var i = 0; i < EXISTING_POLICY_DOCUMENTS.length; i++) {
                                html += stylePolicyDocumentButtonSelectHtml(EXISTING_POLICY_DOCUMENTS[i]);
                            }

                            html += '    </ul>\n';
                            html += '</div>\n';

                            setPolicyDocument(EXISTING_POLICY_DOCUMENTS[0].id);
                        }else{
                            html += '<div class="alert alert-danger">There are no Policy documents associated with the selected organization.  Please add Policy documents and try again.</div>';
                        }

                        $('#selectExistingPolicyUrlControl').html(html);
                    }
                })
            }

            function stylePolicyDocumentButtonSelectHtml(doc){
                var html = '';
                html += '<li>\n';
                html += '   <a href="javascript:setPolicyDocument('+doc.id + ')">';
                html += '        <div style="font-weight: bold;">'+doc.filename+'</div>\n';
                html += '   </a>';
                html += '</li>\n\n';

                console.log("stylePolicyDocumentButtonSelectHtml html " + html.toString());

                return html;
            }

            function setPolicyDocument(id) {
                console.log("Setting policy document to " + id);

                var doc = findExistingPolicyDocument(id);

                $('#selectedExistingPolicyDocumentTitle').html(doc.filename);
                $('#policyUrl').val(doc.publicUrl);

                var url = $('#policyUrl').val();

                console.log("policyUrl: " + url);
            }

            function findExistingPolicyDocument(id){
                var doc = null;
                console.log("Finding policy document "+id);
                for( var index = 0; index < EXISTING_POLICY_DOCUMENTS.length; index++ ){
                    var current = EXISTING_POLICY_DOCUMENTS[index];
                    if( current && (current.id == id) ){
                        doc = current;
                        break;
                    }
                }
                if( doc ){
                    console.log("Successfully found policy document["+doc.id+"]: "+doc.url);
                }else{
                    console.log("No such policy document: "+id);
                }
                return doc;
            }

            // relying party agreements
            function loadExistingRelyingPartyAgreements(orgId) {
                console.log("Loading existing relying party agreements from organization: "+orgId);

                var url = '${createLink(controller:'trustmarkMetadata', action: 'listAvailableRelyingPartyAgreementDocuments')}';
                console.log("Loading existing relying party agreements from url: "+url);
                $.ajax({
                    url: url,
                    dataType: 'json',
                    data: {
                        id: orgId,
                        now: new Date().getMilliseconds(),
                        format: 'json'
                    },
                    error: function( jqXHR, textStatus, errorThrown ){
                        $('#selectExistingRelyingPartyAgreementUrlControl').html('<div class="alert alert-danger">An error occurred while loading the remote data.  Please refresh and try again.</div>')
                    },
                    success: function(result, textStatus, jqXHR){
                        console.log("Received relying party agreement documents result: "+JSON.stringify(result, null, 4));
                        EXISTING_RELYING_PARTY_AGREEMENT_DOCUMENTS = result;

                        var html = '';

                        if( EXISTING_RELYING_PARTY_AGREEMENT_DOCUMENTS && EXISTING_RELYING_PARTY_AGREEMENT_DOCUMENTS.length > 0 ) {
                            html += '<div class="btn-group" style="margin: 0;">\n';
                            html += '    <button type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown" aria-expanded="false">\n';
                            html += '        <span id="selectedExistingRelyingPartyAgreementDocumentTitle"><em>Select Relying Party Agreement Document...</em></span> <span class="caret"></span>\n';
                            html += '    </button>\n'

                            html += '    <ul class="dropdown-menu" role="menu">\n';
                            for (var i = 0; i < EXISTING_RELYING_PARTY_AGREEMENT_DOCUMENTS.length; i++) {
                                html += styleRelyingPartyAgreementDocumentButtonSelectHtml(EXISTING_RELYING_PARTY_AGREEMENT_DOCUMENTS[i]);
                            }

                            html += '    </ul>\n';
                            html += '</div>\n';

                            setRelyingPartyAgreementDocument(EXISTING_RELYING_PARTY_AGREEMENT_DOCUMENTS[0].id);
                        }else{
                            html += '<div class="alert alert-danger">There are no Relying Party Agreements associated with the selected organization.  Please add Relying Party Agreements and try again.</div>';
                        }

                        $('#selectExistingRelyingPartyAgreementUrlControl').html(html);
                    }
                })
            }

            function styleRelyingPartyAgreementDocumentButtonSelectHtml(doc){
                var html = '';
                html += '<li>\n';
                html += '   <a href="javascript:setRelyingPartyAgreementDocument('+doc.id + ')">';
                html += '        <div style="font-weight: bold;">'+doc.filename+'</div>\n';
                html += '   </a>';
                html += '</li>\n\n';

                console.log("styleRelyingPartyAgreementDocumentButtonSelectHtml html " + html.toString());

                return html;
            }

            function setRelyingPartyAgreementDocument(id) {
                console.log("Setting relying party agreement document to " + id);

                var doc = findExistingRelyingPartyAgreementDocument(id);

                $('#selectedExistingRelyingPartyAgreementDocumentTitle').html(doc.filename);
                $('#relyingPartyAgreementUrl').val(doc.publicUrl);

                var url = $('#relyingPartyAgreementUrl').val();

                console.log("relyingPartyAgreementUrl: " + url);
            }

            function findExistingRelyingPartyAgreementDocument(id){
                var doc = null;
                console.log("Finding relying party agreement document "+id);
                for( var index = 0; index < EXISTING_RELYING_PARTY_AGREEMENT_DOCUMENTS.length; index++ ){
                    var current = EXISTING_RELYING_PARTY_AGREEMENT_DOCUMENTS[index];
                    if( current && (current.id == id) ){
                        doc = current;
                        break;
                    }
                }
                if( doc ){
                    console.log("Successfully found relying party agreement document["+doc.id+"]: "+doc.url);
                }else{
                    console.log("No such relying party agreement document: "+id);
                }
                return doc;
            }

            function updateDocsAndCerts(orgId) {

                console.log('Updating documents and certificates for organization: ' + orgId);

                loadExistingCertificates(orgId);
                loadExistingPolicyDocuments(orgId);
                loadExistingRelyingPartyAgreements(orgId);
            }

            $(document).ready(function(){

                var orgId = $('select[name="organizationId"]').val();

                console.log('Initially selected organization: ' + orgId);

                updateDocsAndCerts(orgId);
            });

        </script>

	</body>
</html>

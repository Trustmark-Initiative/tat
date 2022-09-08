<%@ page import="nstic.util.AssessmentToolProperties" %>
<%@ page import="nstic.TATPropertiesHolder" %>
<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main"/>
		<title>Create Assessment</title>

        <style type="text/css">
            .mustLoginDetails {
                text-align: center;
                margin-top: 5em;
                margin-left: 325px;
                width: 400px;
            }

            .mustLoginText {
                margin-bottom: 1em;
            }

            .searchFormContainer {
                margin-bottom: 0.5em;
            }

            table.typeaheadItemTable {
                width: 600px;
                table-layout: fixed;
            }

            .typeaheadImageContainer {
                width: 50px;
                vertical-align: top;
                text-align: center;
            }

            .typeaheadDataContainer {
                width: auto;
            }

            .btnSameWidth {
                width: 18em;
                margin-right: 1em;
            }
        </style>
	</head>
	<body>
        %{--<ol class="breadcrumb">--}%
            %{--<li class="active">Home</li>--}%
        %{--</ol>--}%

        <div class="row">
            <div class="col-md-10">
                <h1>Create New Assessment</h1>
                <div class="pageSubsection">
                    This page allows you to create a new Assessment.
                </div>
            </div>
            <div class="col-md-2" style="text-align: right;">
                <g:link controller="assessment" action="copy" class="btn btn-danger"
                        title="Allows you top copy an assessment, but assign a different organization.">
                    <span class="glyphicon glyphicon-copy"></span>
                    Copy Existing
                </g:link>
            </div>
        </div>

        <g:if test="${flash.error}">
            <div class="alert alert-danger">${flash.error}</div>
        </g:if>
        <g:if test="${command?.hasErrors()}">
            <div class="alert alert-danger" style="margin-top: 2em;">
                <b>Cannot create assessment due to the following errors:</b>
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

        <div class="pageContent">

            <g:form action="save" method="POST" class="form-horizontal" onsubmit="return validateForm();">

                <fieldset>
                    <legend style="margin-bottom: 0;">Assess Against</legend>
                    <div class="text-muted" style="margin-bottom: 1em; font-size: 90%;">
                        Choose which Trustmark Definitions or Trust Interoperability Profiles (TIPs) you would like assess
                        against the organization.
                    </div>

                    <div id="tdRefContainer"></div>
                    <div id="tipRefContainer"></div>

                    <div class="form-group">
                        <label class="col-md-2 control-label">&nbsp;</label>
                        <div class="col-md-10">
                            <a href="javascript:addTrustmarkDefinitionToAssess()" class="btn btn-default">
                                <span class="glyphicon glyphicon-plus"></span>
                                Add Trustmark Definition
                            </a>
                            <a href="javascript:addTipToAssess()" class="btn btn-default">
                                <span class="glyphicon glyphicon-plus"></span>
                                Add Trust Interoperability Profile
                            </a>
                            <button type="button" class="btn btn-default" data-toggle="modal" data-target="#assessWhatHelpModal" title="Click here if you don't know any identifying information or cannot find your TD or TIP.">
                                <span class="glyphicon glyphicon-question-sign"></span>
                                Need Help?
                            </button>

                        </div>
                    </div>

                </fieldset>

                <fieldset>
                    <legend>Trustmark Recipient Organization</legend>

                    <input type="hidden" name="existingOrgId" id="existingOrgId" value="${command?.existingOrgId}" />

                    <sec:ifAllGranted roles="ROLE_ADMIN">
                        <div class="form-group">
                            <div class="col-md-10 col-md-offset-2" id="organizationEditInfo">

                            </div>
                        </div>
                    </sec:ifAllGranted>

                    <div class="form-group">
                        <label for="organizationUri" class="col-md-2 control-label">URI</label>
                        <div class="col-md-10 trustmarkTypeaheadContainer">
                            <input id="organizationUri" name="organizationUri" class="form-control" type="text" placeholder="Organization URI" autocomplete="off" value="${command?.organizationUri}" />
                        </div>
                    </div>

                    <div class="form-group">
                        <label for="organizationName" class="col-md-2 control-label">Name</label>
                        <div class="col-md-10 trustmarkTypeaheadContainer">
                            <input id="organizationName" name="organizationName" class="form-control" type="text" placeholder="Organization Name" autocomplete="off" value="${command?.organizationName}" />
                        </div>
                    </div>

                </fieldset>


                <fieldset id="contactFieldsContainer">
                    <legend>Recipient Contact Information</legend>

                    <input type="hidden" name="existingContactId" id="existingContactId" value="${command?.existingContactId}" />
                    <input type="hidden" name="existingContactBeingEdited" id="existingContactBeingEdited" value="${command?.existingContactBeingEdited}" />

                    <sec:ifAllGranted roles="ROLE_ADMIN">
                        <div class="form-group">
                            <div class="col-md-10 col-md-offset-2" id="editingContactData">

                            </div>
                        </div>
                    </sec:ifAllGranted>

                    <div class="form-group">
                        <label for="contactResponder" class="col-md-2 control-label">Responder Name</label>
                        <div class="col-md-10">
                            <input id="contactResponder" name="contactResponder" class="form-control" type="text" placeholder="Responder Name" autocomplete="off" value="${command?.contactResponder}" />
                        </div>
                    </div>

                    <div class="form-group">
                        <label for="contactEmail" class="col-md-2 control-label">E-mail Address</label>
                        <div class="col-md-10">
                            <input id="contactEmail" name="contactEmail" class="form-control" type="text" placeholder="user@organization.com" autocomplete="off" value="${command?.contactEmail}" />
                        </div>
                    </div>


                    <div class="form-group">
                        <label for="contactPhoneNumber" class="col-md-2 control-label">Phone Number</label>
                        <div class="col-md-10">
                            <input id="contactPhoneNumber" name="contactPhoneNumber" class="form-control" type="text" placeholder="xxx-xxx-xxxx" autocomplete="off"  value="${command?.contactPhoneNumber}" />
                        </div>
                    </div>

                    <div class="form-group">
                        <label for="contactMailingAddress" class="col-md-2 control-label">Mailing Address</label>
                        <div class="col-md-10">
                            <input id="contactMailingAddress" name="contactMailingAddress" class="form-control" type="text" placeholder="Address Full Text" autocomplete="off" value="${command?.contactMailingAddress}"  />
                        </div>
                    </div>

                    <div class="form-group">
                        <label for="contactNotes" class="col-md-2 control-label">Notes</label>
                        <div class="col-md-10">
                            <input id="contactNotes" name="contactNotes" class="form-control" type="text" placeholder="" autocomplete="off" value="${command?.contactNotes}"  />
                        </div>
                    </div>

                </fieldset>

                <fieldset>
                    <legend style="margin-bottom: 0;">Assessment Name</legend>
                    <div class="text-muted" style="margin-bottom: 1em; font-size: 90%;">
                        Please provide a name for this assessment.
                    </div>

                    <div class="form-group">
                        <label for="assessmentName" class="col-md-1 control-label">Name</label>
                        <div class="col-md-11">
                            <input type="text" id="assessmentName" name="assessmentName" class="form-control" placeholder="Assessment Name" autocomplete="off" value="${command?.assessmentName}" data-auto-name="" />
                        </div>
                    </div>
                </fieldset>


                <div class="form-group" style="margin-top: 2em;">
                    <div class="col-sm-offset-2 col-sm-10">
                        <g:submitButton name="Save" id="createFreshAssessment" value="Create Fresh Assessment" class="btn btn-primary btnSameWidth"
                                        data-toggle="tooltip" data-placement="top"
                                        title="Create new assessment that includes all trustmarks required to satisfy the selected TIPs."/>
                        <g:submitButton name="SaveDifferential" id="createDiffAssessment" class="btn btn-primary btnSameWidth"
                                        data-toggle="tooltip" data-placement="top" value="Create Differential Assessment"
                                        title="Create new assessment that excludes any active trustmarks that are applicable to the selected TIPs and were already earned by the recipient in previous assessments." />
                    </div>
                </div>

            </g:form>


        </div>

        <div id="assessWhatHelpModal" class="modal fade" tabindex="-1" role="dialog">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                        <h4 class="modal-title">
                            <span class="glyphicon glyphicon-question-sign"></span>
                            Help Selecting TDs and TIPs for Assessment
                        </h4>
                    </div>
                    <div class="modal-body">
                        <p>
                            The assessment must be based off of one or more Trustmark Definitions or Trust
                            Interoperability Profiles.  When you click "Add Trustmark Definition" or "Add Trust
                            Interoperability Profile" then a blank input box will be added to the current list.  To
                            select a TD or TIP, simply type part of the name or description of it and then select the
                            one you want from the drop down box.
                        </p>
                        <h5 style="font-weight: bold;">Exact Match</h5>
                        <p>
                            Unfortunately, the system only implements an EXACT match when searching for TDs and TIPs,
                            which means you will need to know which TD or TIP you are looking for up front before
                            searching.  Fortunately, you should be able to refer to the source registry for advanced
                            searching and listing/browsing capabilities.
                        </p>
                        <h5 style="font-weight: bold;">Configured Registries</h5>
                        <p>
                            When using this tool, you should browse the following registries to find TIPs and TDs available
                            for assessment:
                        <g:set var="remoteHostsString" value="${TATPropertiesHolder.getProperties().getProperty("scan.host.job.remotehosts") ?: ""}"/>
                            <g:set var="remoteHostsString" value="${remoteHostsString + ' ' + AssessmentToolProperties.getRegistryUrl()}"/>
                            <g:set var="remoteHosts" value="${remoteHostsString.trim().split("\\s+")}"/>
                            <g:if test="${remoteHostsString}">
                                <ul>
                                <g:each in="${remoteHosts}" var="host">
                                    <li><a href="${host}" target="_blank">${host}</a></li>
                                </g:each>
                                </ul>
                            </g:if>
                            <g:else>
                                <div>[There are currently 0 configured registries]</div>
                            </g:else>
                        </p>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
                    </div>
                </div><!-- /.modal-content -->
            </div><!-- /.modal-dialog -->
        </div><!-- /.modal -->

        <script type="text/javascript">
            var queryInProgress = false;

            var SELECTED_ORG = null;
            var SELECTED_TD = null;
            var SELECTED_CONTACT = null;

            var DEFAULT_ASSESS_MSG_HTML = '<div class="form-group" id="defaultAssessMsg"><label class="col-md-2 control-label">&nbsp;</label><div class="col-md-10">'+
                '<div class="alert alert-warning" style="margin-bottom: 0;"><span class="glyphicon glyphicon-warning-sign"></span>&nbsp;<em>You are not assessing anything yet.</em></div></div></div>';

            var TD_TYPEAHEAD_TMPL =
                    '<div class="form-group" id="trustmarkDefinitionTypeaheadContainer__POS__">\n'+
                    '    <input type="hidden" id="trustmarkDefinition__POS__" name="trustmarkDefinition__POS__" value="-1" />'+
                    '    <label for="aTdTypeahead__POS__" class="col-md-2 control-label">Trustmark Definition</label>\n'+
                    '    <div class="col-md-9 trustmarkTypeaheadContainer">\n'+
                    '        <input id="aTdTypeahead__POS__" name="aTdTypeahead__POS__" class="form-control" type="text" placeholder="Begin Typing..." autocomplete="off" value="" />\n'+
                    '    </div>\n'+
                    '    <div class="col-md-1">\n'+
                    '        <a href="javascript:removeTdInput(__POS__)" class="btn btn-default">Remove</a>'+
                    '    </div>\n'+
                    '</div>\n';

            var TIP_TYPEAHEAD_TMPL =
                    '<div class="form-group" id="tipTypeaheadContainer__POS__">\n'+
                    '    <input type="hidden" id="trustInteroperabilityProfile__POS__" name="trustInteroperabilityProfile__POS__" value="-1" />'+
                    '    <label for="tipTypeahead__POS__" class="col-md-2 control-label" title="Trust Interoperability Profile">TIP</label>\n'+
                    '    <div class="col-md-9 trustmarkTypeaheadContainer tipTypeaheadContainer">\n'+
                    '        <div>\n'+
                    '            <input id="tipTypeahead__POS__" name="tipTypeahead__POS__" class="form-control" type="text" placeholder="Begin Typing..." autocomplete="off" value="" />\n'+
                    '        </div>\n'+
                    '    </div>\n'+
                    '    <div class="col-md-1">\n'+
                    '        <a href="javascript:removeTipInput(__POS__)" class="btn btn-default">Remove</a>'+
                    '    </div>\n'+
                    '</div>\n';

            var TD_TYPEAHEAD_COUNT = 0;
            var TIP_TYPEAHEAD_COUNT = 0;

            function removeTdInput(pos){
                $('#trustmarkDefinitionTypeaheadContainer'+pos).remove();
                // TODO Other cleanup actions....
                if( $('#tdRefContainer').children().size() == 0 && $('#tipRefContainer').children().size() == 0){
                    $('#tdRefContainer').html(DEFAULT_ASSESS_MSG_HTML);

                    $('#createFreshAssessment').prop("disabled", true);
                }
            }
            function removeTipInput(pos){
                $('#tipTypeaheadContainer'+pos).remove();
                // TODO Other cleanup actions....

                if( $('#tdRefContainer').children().size() == 0 && $('#tipRefContainer').children().size() == 0){
                    $('#tdRefContainer').html(DEFAULT_ASSESS_MSG_HTML);

                    $('#createFreshAssessment').prop("disabled", true);
                }

                if( $('#tipRefContainer').children().size() == 0){
                    $('#createDiffAssessment').prop("disabled", true);
                }
            }

            /**
             * Simply adds the HTML for selecting a TD to assess agianst into HTML component 'tdAndTipRefContainer'.  If
             * 'defaultAssessMsg' exists under 'tdAndTipRefContainer', then it is first removed.
             */
            function addTrustmarkDefinitionToAssess(){
                if( displayingDefaultAssessMessage() ){
                    $('#tdRefContainer').html('');
                    $('#tipRefContainer').html('');
                }

                TD_TYPEAHEAD_COUNT++;

                var html = TD_TYPEAHEAD_TMPL.replace(/__POS__/g, ''+TD_TYPEAHEAD_COUNT);
                $('#tdRefContainer').append(html);


                $("#aTdTypeahead"+TD_TYPEAHEAD_COUNT).typeahead({
                    source: trustmarkDefinitionTypeaheadDoQuery,
                    matcher: typeaheadMatchAllMatcher,
                    highlighter: trustmarkDefinitionTypeaheadHighlighter,
                    sorter: typeaheadAcceptExistingSortSorter,
                    updater: trustmarkDefinitionTypeaheadUpdater,
                    identifier: TD_TYPEAHEAD_COUNT
                });
                $("#aTdTypeahead"+TD_TYPEAHEAD_COUNT).focus();

                $('#createFreshAssessment').prop("disabled", false);

            }//end addTrustmarkDefinitionToAssess()


            /**
             * Simply adds the HTML for selecting a TIP to assess agianst into HTML component 'tdAndTipRefContainer'.  If
             * 'defaultAssessMsg' exists under 'tdAndTipRefContainer', then it is first removed.
             */
            function addTipToAssess(){
                if( displayingDefaultAssessMessage() ){
                    $('#tdRefContainer').html('');
                    $('#tipRefContainer').html('');
                }

                TIP_TYPEAHEAD_COUNT++;

                var html = TIP_TYPEAHEAD_TMPL.replace(/__POS__/g, ''+TIP_TYPEAHEAD_COUNT);
                $('#tdRefContainer').append(html);


                $("#tipTypeahead"+TIP_TYPEAHEAD_COUNT).typeahead({
                    source: tipTypeaheadDoQuery,
                    matcher: typeaheadMatchAllMatcher,
                    highlighter: tipTypeaheadHighlighter,
                    sorter: typeaheadAcceptExistingSortSorter,
                    updater: tipTypeaheadUpdater,
                    identifier: TIP_TYPEAHEAD_COUNT
                });
                $("#tipTypeahead"+TIP_TYPEAHEAD_COUNT).focus();

                $('#createFreshAssessment').prop("disabled", false);
                $('#createDiffAssessment').prop("disabled", false);
            }//end addTipToAssess()



            /**
             * Returns true if the default assess message is displayed.
             */
            function displayingDefaultAssessMessage(){
                return $('#tdRefContainer #defaultAssessMsg').length > 0;
            }

            $(document).ready(function(){
                $('#createDiffAssessment').prop("disabled", true);
                $('#createFreshAssessment').prop("disabled", true);

                $('#tdRefContainer').html(DEFAULT_ASSESS_MSG_HTML);


                $("#organizationUri").typeahead({
                    source: orgSearchTypeaheadDoQuery,
                    matcher: typeaheadMatchAllMatcher,
                    highlighter: orgSearchTypeaheadHighlighter,
                    sorter: typeaheadAcceptExistingSortSorter,
                    updater: orgSearchTypeaheadUpdaterUrn
                });

                $("#organizationName").typeahead({
                    source: orgSearchTypeaheadDoQuery,
                    matcher: typeaheadMatchAllMatcher,
                    highlighter: orgSearchTypeaheadHighlighter,
                    sorter: typeaheadAcceptExistingSortSorter,
                    updater: orgSearchTypeaheadUpdaterName
                });

                $("#contactEmail").typeahead({
                    source: contactEmailTypeaheadDoQuery,
                    matcher: typeaheadMatchAllMatcher,
                    highlighter: contactEmailTypeaheadHighlighter,
                    sorter: typeaheadAcceptExistingSortSorter,
                    updater: contactEmailTypeaheadUpdater
                });

                $("#contactEmail").on("change", function() {
                    console.log("The contact email has changed.")
                })

                <g:if test="${command?.existingOrgId}">
                    syncOrgEditing({name: '${command?.organizationName}', urn: '${command?.organizationUri}'});
                </g:if>
                <g:else>
                    clearOrganization();
                    <g:if test="${command?.organizationUri}">
                        $('#organizationUri').val('${command?.organizationUri}')
                    </g:if>
                    <g:if test="${command?.organizationName}">
                        $('#organizationName').val('${command?.organizationName}')
                    </g:if>

                </g:else>

                <g:if test="${command?.existingContactId}">
                    syncContactEditing({
                        email: '${command?.contactEmail}',
                        id: '${command?.existingContactId}',
                        responder: '${command?.contactResponder}',
                        mailingAddress: '${command?.contactMailingAddress}',
                        notes: '${command?.contactNotes}',
                        phoneNumber: '${command?.contactPhoneNumber}'
                    }, ${command?.existingContactBeingEdited.toString()});
                </g:if><g:else>
                    clearContactEditing();
                    <g:if test="${command?.contactEmail}">
                        $('#contactEmail').val('${command?.contactEmail}');
                    </g:if>
                    <g:if test="${command?.contactResponder}">
                        $('#contactResponder').val('${command?.contactResponder}');
                    </g:if>
                    <g:if test="${command?.contactMailingAddress}">
                        $('#contactMailingAddress').val('${command?.contactMailingAddress}');
                    </g:if>
                    <g:if test="${command?.contactNotes}">
                        $('#contactNotes').val('${command?.contactNotes}');
                    </g:if>
                    <g:if test="${command?.contactPhoneNumber}">
                        $('#contactPhoneNumber').val('${command?.contactPhoneNumber}');
                    </g:if>
                </g:else>

            })

            function typeaheadAcceptExistingSortSorter(items){
                return items; // They are already sorted from server.
            }

            function typeaheadMatchAllMatcher(item){
                return true; // They are already matched from server
            }


            function trustmarkDefinitionTypeaheadDoQuery( query , process ){
                if( queryInProgress ){
                    return;
                }
                queryInProgress = true;
                console.log("Executing TD query: "+query);
                var typeaheadUrl = '${createLink(controller:'trustmarkDefinition', action: 'typeahead')}?format=json&q='+encodeURI(query);
                $.get(typeaheadUrl, function(data){
                    process(data);
                    queryInProgress = false;
                }, 'json')
            }

            function trustmarkDefinitionTypeaheadUpdater(item){
                $('#trustmarkDefinition'+this.options.identifier).val(item.id);
                return item.name;
            }

            function trustmarkDefinitionTypeaheadHighlighter(item){
                var html = "";

                html += "<div class=\"typeaheadItem\">";

                html += "    <div class=\"typeaheadItemName\">"+item.name+ " (version: "+ item.tdVersion +")" + "</div>";
                html += "    <div class=\"typeaheadItemDesc\">"+item.description+"</div>";
                html += "</div>";

                return html;
            }//end trustmarkDefinitionTypeaheadHighlighter()

            function trustInteroperabilityProfileTypeaheadHighlighter(item){
                var html = "";

                html += "<div class=\"typeaheadItem\">";

                html += "    <div class=\"typeaheadItemName\">"+item.name+ " (version: "+ item.tipVersion +")" + "</div>";
                html += "    <div class=\"typeaheadItemDesc\">"+item.description+"</div>";
                html += "</div>";

                return html;
            }//end trustInteroperabilityProfileTypeaheadHighlighter()


            function tipTypeaheadDoQuery( query , process ){
                if( queryInProgress ){
                    return;
                }
                queryInProgress = true;
                console.log("Executing TIP query: "+query);
                var typeaheadUrl = '${createLink(controller:'tip', action: 'typeahead')}?format=json&q='+encodeURI(query);
                $.get(typeaheadUrl, function(data){
                    process(data);
                    queryInProgress = false;
                }, 'json')
            }

            function tipTypeaheadUpdater(item){
                $('#trustInteroperabilityProfile'+this.options.identifier).val(item.id);
                return item.name;
            }

            function tipTypeaheadHighlighter(item){
                // return trustmarkDefinitionTypeaheadHighlighter(item);
                return trustInteroperabilityProfileTypeaheadHighlighter(item);
            }//end tipTypeaheadHighlighter()



            function orgSearchTypeaheadDoQuery( query , process ){
                if( queryInProgress ){
                    return;
                }
                queryInProgress = true;
                console.log("Executing org query: "+query);
                var typeaheadUrl = '${createLink(controller:'organization', action: 'typeahead')}?format=json&q='+encodeURI(query);
                $.get(typeaheadUrl, function(data){
                    process(data);
                    queryInProgress = false;
                }, 'json')
            }

            function orgSearchTypeaheadUpdaterName(item){
                SELECTED_ORG = item;
                setOrganizationEditing(item);
                if( item.primaryContact ) {
                    setContactEditing(item.primaryContact);
                }
                return item.name;
            }

            function orgSearchTypeaheadUpdaterUrn(item){
                SELECTED_ORG = item;
                setOrganizationEditing(item);
                if( item.primaryContact ) {
                    setContactEditing(item.primaryContact);
                }
                return item.uri;
            }

            function orgSearchTypeaheadHighlighter(item){
                var html = "<b>" + item.uri + "</b> - " + item.name;
                return html;
            }//end orgSearchTypeaheadHighlighter()

            function syncContactEditing(contact, beingEdited){
                setContactEditing(contact);
                if( beingEdited ){
                    editExistingContact();
                }
            }

            function setContactEditing(contact){
                SELECTED_CONTACT = contact;

                var displayName = contact.email;
                if( contact.firstName && contact.lastName ){
                    displayName = contact.firstName + " " +contact.lastName;
                }

                if( !displayName || displayName.trim().length == 0 ){
                    displayName = contact.email;
                }

                $('#editingContactData').html("<em>Existing contact: "+displayName+"</em> "+
                        "<a href=\"javascript:editExistingContact()\" class=\"btn btn-xs btn-default\">Edit</a>"+
                        "<a href=\"javascript:clearContactEditing()\" class=\"btn btn-xs btn-default\">Clear</a>")

                $('#existingContactId').val(contact.id);
                $('#contactResponder').val(contact.responder);
                $('#contactEmail').val(contact.email);
                $('#contactPhoneNumber').val(contact.phoneNumber);
                $('#contactMailingAddress').val(contact.mailingAddress);
                $('#contactNotes').val(contact.notes);

                $("#contactFieldsContainer :input").attr("disabled", true);
            }

            function allowEditingContact() {
                $("#contactFieldsContainer :input").attr("disabled", null);
            }

            function editExistingContact() {
                allowEditingContact();
                $('#existingContactBeingEdited').val('true')
            }

            function clearContactEditing(){
                SELECTED_CONTACT = null;

                $("#editingContactData").html("<em>Creating new contact</em>")

                $('#existingContactId').val('');
                $('#contactEmail').val('');
                $('#contactFirstName').val('');
                $('#contactLastName').val('');
                $('#contactRole').val('');
                $('#contactPhoneNumber').val('');

                allowEditingContact();
                $('#existingContactBeingEdited').val('false')
            }//end clearContactEditing()

            function syncOrgEditing(orgCommand){

                $('#organizationName').prop('readonly', true);
                $('#organizationUri').prop('readonly', true);

                var getOrgUrl = '${createLink(controller:'organization', action: 'view', id: command?.existingOrgId)}?format=json'
                $.get(getOrgUrl, function(org){
                    SELECTED_ORG = org;
                    setOrganizationEditing(org);
                }, 'json')
            }//end syncOrgEditing()

            function clearOrganization() {
                SELECTED_ORG = null;

                $('#organizationName').prop('readonly', false);
                $('#organizationUri').prop('readonly', false);

                $('#organizationEditInfo').html('<em>Creating new organization</em>');

                $('#existingOrgId').val('');
                $('#organizationUri').val('');
                $('#organizationName').val('');
            }

            function setOrganizationEditing(org){
                SELECTED_ORG = org;

                $('#organizationEditInfo').html('<em>Editing organization: '+org.name+'</em> <a href=\"javascript:clearOrganization()\" class=\"btn btn-xs btn-default\">Clear</a>');

                $('#existingOrgId').val(org.id);
                $('#organizationUri').val(org.uri);
                $('#organizationName').val(org.name);
                $('#contactEmail').val(org.primaryContact.email)
            }//end setOrganizationEditing


            function contactEmailTypeaheadDoQuery( query , process ){
                if( queryInProgress ){
                    return;
                }
                queryInProgress = true;
                console.log("Executing contact information query: "+query);
                var typeaheadUrl = '${createLink(controller:'contactInformation', action: 'typeahead')}?format=json&q='+encodeURI(query);
                if( SELECTED_ORG ){
                    typeaheadUrl += "&org="+SELECTED_ORG.id
                }
                $.get(typeaheadUrl, function(data){
                    process(data);
                    queryInProgress = false;
                }, 'json')
            }

            function contactEmailTypeaheadUpdater(item){
                setContactEditing(item);
                return item.email;
            }

            function contactEmailTypeaheadHighlighter(item){
                var html = item.email + " - " + item.firstName + " " + item.lastName;
                return html;
            }//end orgSearchTypeaheadHighlighter()



            function createNewPillClick() {
                $('#useOrgDefaultContact').click();

                $('#createNewOrgFormGroupsContainer').show()
                $('#useOrgDefaultContactContainer').hide();
                $('#useExistingOrgFormGroupsContainer').hide();

                $('#organizationSearch').val('');
                $('#existingOrgId').val('');

                SELECTED_ORG = null;
            }

            function useExistingPillClick() {
                $('#createNewOrgFormGroupsContainer').hide();
                $('#useExistingOrgFormGroupsContainer').show();
                $('#useOrgDefaultContactContainer').show();
            }

            function validateForm(){
                if( ($('#tdRefContainer').children().size() == 0 && $('#tipRefContainer').children().size() == 0)
                    || displayingDefaultAssessMessage() ){
                    alert("Cannot create assessment, you are missing a TD or TIP to Assess against.");
                    return false;
                }


                allowEditingContact(); // Make sure the fields are not disabled when going to server...
                return true;
            }

            // Autopopulate the assessment name field
            $(function () {
                var $assessmentName = $('#assessmentName');
                if ($assessmentName.val()) { return; }

                var pollMs = 200;
                var $tdRefContainer = $('#tdRefContainer');
                var $inputOrgName = $('#organizationName');

                setInterval(autoPopAssessmentName, pollMs);
                function autoPopAssessmentName() {
                    var currentAutoName = $assessmentName.data('autoName');
                    var currentName = $assessmentName.val();
                    if (currentAutoName !== currentName) { return; }

                    var typeaheads = $tdRefContainer.find('input[id*=Typeahead]').toArray();
                    var firstTypeahead = typeaheads && typeaheads[0];
                    if (!firstTypeahead) { return; }
                    var typeaheadName = firstTypeahead.value;
                    if (!typeaheadName) { return; }

                    var orgName = $inputOrgName.val();
                    if (!orgName) { return; }

                    var autoName = 'Assessment of ' + orgName + ' for ' + typeaheadName;
                    if (autoName === currentAutoName) { return; }
                    console.log('autoname = ', autoName);
                    $assessmentName.data('autoName', autoName);
                    $assessmentName.val(autoName);
                }
            });

        </script>
	</body>
</html>

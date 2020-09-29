<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main"/>
		<title>Home</title>

        <style type="text/css">

        </style>
	</head>
	<body>
        <h1>Create Assessment from Copy</h1>
        <div class="pageSubsection">
            On this page, you can create a new assessment based on an existing one.  First, choose the assessment you wish
            to copy, then choose the new organization to associate it to (you ARE allowed to choose the same one).  Once
            this is done, all step answers, artifacts and statuses will be copied to a new assessment.  Note that binary
            objects WILL be duplicated, unless they are organization artifacts.
        </div>

        <div id="messageContainer">
            <g:if test="${flash.message}">
                <div style="margin-top: 2em;" class="alert alert-info">
                    ${flash.message}
                </div>
            </g:if>
        </div>

        <div class="pageContent">
            <div id="formContainer">
                <g:form method="POST" class="form-horizontal" onsubmit="processForm(); return false;">
                    <g:hiddenField name="trustmarkDefinitionId" id="trustmarkDefinitionId" value="" />
                    <g:hiddenField name="assessmentId" id="assessmentId" value="" />
                    <g:hiddenField name="assessmentName" id="assessmentName" value="" />
                    <g:hiddenField name="existingOrgId" id="existingOrgId" value="" />
                    <g:hiddenField name="existingContactId" id="existingContactId" value="" />
                    <g:hiddenField name="existingContactBeingEdited" id="existingContactBeingEdited" value="" />


                    <fieldset>
                        <legend>Choose Assessment</legend>

                        <div class="form-group">
                            <label for="trustmarkDefinitionTypeahead" class="col-md-2 control-label">Trustmark Definition</label>
                            <div class="col-md-10 trustmarkTypeaheadContainer">
                                <input id="trustmarkDefinitionTypeahead" name="trustmarkDefinitionTypeahead" class="form-control" type="text" placeholder="Trustmark Definition" autocomplete="off" value="" />
                            </div>
                        </div>

                        <div class="form-group">
                            <label class="col-md-2 control-label">Assessment</label>
                            <div class="col-md-10 trustmarkTypeaheadContainer">
                                <div id="assessmentChooserContainer">
                                    <p class="form-control-static">
                                        <em>Please select a Trustmark Definition.</em>
                                    </p>
                                </div>
                            </div>
                        </div>
                    </fieldset>

                    <fieldset>
                        <legend>Trustmark Recipient Organization</legend>

                        <div class="form-group">
                            <div class="col-md-10 col-md-offset-2" id="organizationEditInfo">

                            </div>
                        </div>

                        <div class="form-group">
                            <label for="organizationUri" class="col-md-2 control-label">URI</label>
                            <div class="col-md-10">
                                <input id="organizationUri" name="organizationUri" class="form-control" type="text" placeholder="Organization URI" autocomplete="off" value="" />
                            </div>
                        </div>

                        <div class="form-group">
                            <label for="organizationAbbr" class="col-md-2 control-label">Abbreviation</label>
                            <div class="col-md-10">
                                <input id="organizationAbbr" name="organizationAbbr" class="form-control" type="text" placeholder="Name ABBR." autocomplete="off" value="" />
                            </div>
                        </div>

                        <div class="form-group">
                            <label for="organizationName" class="col-md-2 control-label">Name</label>
                            <div class="col-md-10">
                                <input id="organizationName" name="organizationName" class="form-control" type="text" placeholder="Organization Name" autocomplete="off" value="" />
                            </div>
                        </div>

                    </fieldset>

                    <fieldset id="contactFieldsContainer">
                        <legend>Recipient Contact Information</legend>

                        <div class="form-group">
                            <div class="col-md-10 col-md-offset-2" id="editingContactData">

                            </div>
                        </div>


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

                    <div class="form-group" style="margin-top: 2em;">
                        <div class="col-sm-offset-2 col-sm-10">
                            <button type="submit" class="btn btn-default">Create</button>
                        </div>
                    </div>

                </g:form>
            </div>


            <div id="performActionContainer">
                <div class="row">
                    <div class="col-md-6">
                        <h3>Assessment #<span class="assIdToCopy">xx</span> Being Copied...</h3>
                        <div>
                            The system is currently copying this assessment #<span class="assIdToCopy">xx</span>
                            into a new assessment for organization <span class="orgNameToCreate">blah</span>.  This
                            operation may take some time, as everything from previous assessor comments to binary artifacts
                            are going to be duplicated.  Please watch the server progress on the window to your right.

                        </div>
                        <div id="afterCreateNavContainer" style="margin-top: 2em;">
                            <a href="#" class="btn btn-primary" disabled="disabled">Assessment Being Created...</a>
                        </div>
                    </div>
                    <div class="col-md-6">
                        <h3 id="requestHeader"> <asset:image src="spinner.gif" /> Request Status: </h3>
                        <div class="well well-sm" id="feedbackMessage" style="font-family: courier, monospace; max-height: 800px; overflow: scroll;">
                            <div><span class="glyphicon glyphicon-arrow-up" title="From the client."></span> Sending Request...</div >
                        </div>
                    </div>
                </div>


            </div>

        </div>


    <script type="text/javascript">
        var queryInProgress = false;

        var SELECTED_TD = null;
        var ASSESSMENTS = null;
        var SELECTED_ORG = null;
        var SELECTED_CONTACT = null;

        $(document).ready(function() {
            $('#performActionContainer').hide();

            $("#trustmarkDefinitionTypeahead").typeahead({
                source: trustmarkDefinitionTypeaheadDoQuery,
                matcher: typeaheadMatchAllMatcher,
                highlighter: trustmarkDefinitionTypeaheadHighlighter,
                sorter: typeaheadAcceptExistingSortSorter,
                updater: trustmarkDefinitionTypeaheadUpdater
            });


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
        });

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
            console.log("Setting selected TD to: "+JSON.stringify(item, null, 2));
            SELECTED_TD = item;
            $('#trustmarkDefinitionId').val(item.id);
            $('#trustmarkDefinitionTypeahead').val(item.name);
            setTimeout("loadAssessmentsForTD()", 200);
            return item.name;
        }

        function trustmarkDefinitionTypeaheadHighlighter(item){
            var html = "";

            html += "<div class=\"typeaheadItem\">";
            html += "    <div class=\"typeaheadItemName\">"+item.name+"</div>";
            html += "    <div class=\"typeaheadItemDesc\">"+item.description+"</div>";
            html += "</div>";

            return html;
        }//end trustmarkDefinitionTypeaheadHighlighter()


        function loadAssessmentsForTD() {
            $('#assessmentChooserContainer').html(
                    '<p class="form-control-static"><asset:image src="spinner.gif" />&nbsp;Loading Assessments for TD \''+SELECTED_TD.name+'\'...</p>'
            );

            var url = '${createLink(controller:'trustmarkDefinition', action: 'listAssessments', id: '_REPLACEME_')}';
            url = url.replace('_REPLACEME_', SELECTED_TD.id);
            console.log("Contact URL: "+url);
            $.ajax({
                url: url,
                data: {
                    now: new Date().getMilliseconds(),
                    format: 'json'
                },
                dataType: 'json',
                error: function( jqXHR, textStatus, errorThrown ){
                    $('#assessmentChooserContainer').html('<div class="alert alert-danger">Unable to retrieve assessment list from server!  Error: '+textStatus+'</div>');
                },
                success: function( data, textStatus, jqXHR ) {
                    console.log("Received response: "+JSON.stringify(data, null, 4));
                    ASSESSMENTS = data;
                    updateAssessmentsButton();
                    clearAssessment();
                }
            })


        }//end loadAssessmentsForTD()

        function updateAssessmentsButton() {
            var html = '';

            if( ASSESSMENTS && ASSESSMENTS.length > 0 ){
                html += '<div class="btn-group" style="margin: 0;">\n';
                html += '    <button type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown" aria-expanded="false">\n';
                html += '        <span id="selectedAssessmentTitle"><em>Select Assessment...</em></span> <span class="caret"></span>\n';
                html += '    </button>\n'

                html += '    <ul class="dropdown-menu" role="menu">\n';
                for (var i = 0; i < ASSESSMENTS.length; i++) {
                    html += styleAssessmentButtonSelectHtml(ASSESSMENTS[i]);
                }

                html += '    </ul>\n';
                html += '</div>\n';
                html += '&nbsp;&nbsp;&nbsp; <a href="javascript:clearAssessment();">Clear</a>\n';

                // html += '<span id="helpBlock" class="help-block"> TODO Help text here... </span>';
            }else{
                html += '<p class="form-control-static"><em>There are no assessments</em></p>\n';
            }
            $('#assessmentChooserContainer').html(html);
        }//end updateAssessmentsButton()

        function styleAssessmentButtonSelectHtml(ass){
            var html = '';
            html += '<li>\n';
            html += '    <a href="javascript:setAssessment('+ass.id+')">\n';
            html += '        <div><span style="font-weight: bold;">Assessment #'+ass.id+'</span></div>\n';
            html += '        <div style="font-size: 80%; margin-left: 1em; width: 600px; max-width: 600px; white-space: normal;">'+ass.assessedOrganization.shortName+': '+ass.assessedOrganization.name+'</div>\n';
            html += '    </a>\n';
            html += '</li>\n\n';
            return html;
        }//end styleArtifactButtonSelectHtml()

        function findAssessmentById(id){
            var assessment = null;
            if( ASSESSMENTS && ASSESSMENTS.length > 0 ){
                for( var i = 0; i < ASSESSMENTS.length; i++ ){
                    var currentAssessment = ASSESSMENTS[i];
                    if( currentAssessment.id == id ) {
                        assessment = currentAssessment;
                        break;
                    }
                }
            }
            return assessment;
        }

        function setAssessment(id){
            console.log("Setting assessment to "+id+"...");
            var assessment = findAssessmentById(id);

            $('#assessmentId').val(id);
            $('#assessmentName').val(assessment.name);
            $('#selectedAssessmentTitle').html('Assessment #'+assessment.id+' - '+assessment.assessedOrganization.shortName);
        }//end setAssessment()

        function clearAssessment(){
            $('#assessmentId').val('');
            $('#assessmentName').val('');
            $('#selectedAssessmentTitle').html('<em>Select Assessment...</em>');
        }



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

            $('#existingContactBeingEdited').val('false');
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

            $('#existingContactBeingEdited').val('false');
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
            var getOrgUrl = '${createLink(controller:'organization', action: 'view', id: command?.existingOrgId)}?format=json'
            $.get(getOrgUrl, function(org){
                SELECTED_ORG = org;
                setOrganizationEditing(org);
                $('#organizationName').val(orgCommand.name);
                $('#organizationAbbr').val(orgCommand.shortName);
                $('#organizationUri').val(orgCommand.uri);
            }, 'json')
        }//end syncOrgEditing()

        function clearOrganization() {
            SELECTED_ORG = null;

            $('#organizationEditInfo').html('<em>Creating new organization</em>');

            $('#existingOrgId').val('');
            $('#organizationUri').val('');
            $('#organizationAbbr').val('');
            $('#organizationName').val('');
        }

        function setOrganizationEditing(org){
            SELECTED_ORG = org;

            $('#organizationEditInfo').html('<em>Editing organization: '+org.name+'</em> <a href=\"javascript:clearOrganization()\" class=\"btn btn-xs btn-default\">Clear</a>');

            $('#existingOrgId').val(org.id);
            $('#organizationAbbr').val(org.shortName);
            $('#organizationUri').val(org.uri);
            $('#organizationName').val(org.name);
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

        function isEmpty(id){
            var value = $('#'+id).val();
            console.log("Checking value of #"+id+"=["+value+"] to be not empty...")
            return value.trim() === '';
        }

        function validateForm() {
            if( isEmpty('trustmarkDefinitionId') ){
                setErrorMessage('You must select a TD.');
                return false;
            }
            if( isEmpty('assessmentId') ){
                setErrorMessage('You must select an existing assessment.');
                return false;
            }
            if( isEmpty('organizationUri') ){
                setErrorMessage('You must provide an organization URI.');
                return false;
            }
            if( isEmpty('organizationName') ){
                setErrorMessage('You must provide an organization name.');
                return false;
            }
            if( isEmpty('organizationAbbr') ){
                setErrorMessage('You must provide an organization abbreviation.');
                return false;
            }
            if( isEmpty('contactEmail') ){
                setErrorMessage('You must provide a contact email.');
                return false;
            }

            console.log("Form has all required fields.");
            return true;
        }

        var LAST_INDEX = -1;

        function processForm(){
            clearErrorMessage();
            if( !validateForm() ){
                return;
            }

            console.log("Hiding/Showing HTML elements...")
            $('#formContainer').hide();
            $('#performActionContainer').show();


            $('.assIdToCopy').html($('#assessmentId').val());
            $('.orgNameToCreate').html($('#organizationAbbr').val());


            console.log("Sending form data...");
            var data = buildFormSubmissionData();
            data["now"] =  new Date().getMilliseconds();
            data["format"] = "json";
            $.ajax({
                url: '${createLink(controller: 'assessment', action: 'copy')}',
                data: data,
                dataType: 'json',
                method: 'POST',
                error: function( jqXHR, textStatus, errorThrown ){
                    console.log("ERROR: "+textStatus);
                    $('#feedbackMessage').html("An unexpected error occurred: "+textStatus);
                },
                success: function( data, textStatus, jqXHR ) {
                    if( data && data.length > 0 ){
                        var lastEntry = data[data.length-1];
                        addData(lastEntry);
                        $('#requestHeader').html("Request Complete")
                        var url = '${createLink(controller:'assessment', action: 'view', id: '__REPLACE__')}';
                        url = url.replace("__REPLACE__", lastEntry.newAssessmentId);
                        $('#afterCreateNavContainer').html('<a href="'+url+'" class="btn btn-primary">Assessment #'+lastEntry.newAssessmentId+'</a>')
                    }
                },
                xhrFields: {
                    onprogress: function (evt) {
                        var text = this.responseText;
                        if( LAST_INDEX == -1 )
                            LAST_INDEX  = text.indexOf("{");
                        else
                            LAST_INDEX  = text.indexOf("{", LAST_INDEX);
                        var start = LAST_INDEX;
                        var stop = getCloseBracketIndex(text, start);
                        if( stop == -1 ){
                            // Then the JSON has not fully posted.  It's possible...
                            return;
                        }
                        LAST_INDEX = stop+1;

                        var jsonStr = text.substring(start, stop+1);
                        var data = JSON.parse(jsonStr);
                        console.log("Received message[status="+data.status+"]: " + data.message);
                        addData(data);
                    }
                }
            })

            return true;
        }//end processForm()

        var messagesDisplayed = [];
        function addData( data ){
            if(messagesDisplayed.indexOf(data.id) > -1 ){
                return;
            }
            messagesDisplayed.push(data.id);
            var iconText = "<span class=\"glyphicon glyphicon-arrow-down\" title=\"From the server.\"></span> ";
            if (data.status === "WARNING"){
                iconText = '<span class="glyphicon glyphicon-warning-sign" title="WARNING"></span> ';
            }else if (data.status === "ERROR"){
                iconText = '<span class="glyphicon glyphicon-exclamation-sign" title="ERROR"></span> ';
            }
            $('#feedbackMessage').prepend("<div>" + iconText + data.message + "</div><hr/>");
        }

        function getCloseBracketIndex(text, startIndex){
            var openBrackets = 0;
            var indexOfCloseBracket = -1;
            for( var i = startIndex + 1; i < text.length; i++ ){
                if( text.charAt(i) == '{' ){
                    openBrackets++;
                }else if( text.charAt(i) == '}' ){
                    if( openBrackets > 0 ){
                        openBrackets--;
                    }else{
                        indexOfCloseBracket = i;
                        break;
                    }
                }
            }
            return indexOfCloseBracket;
        }

        /**
         * Builds a data structure which contains all of the form submission data.
         */
        function buildFormSubmissionData() {
            return {
                "trustmarkDefinitionId" : $('#trustmarkDefinitionId').val(),
                "assessmentId" : $('#assessmentId').val(),
                "assessmentName" : $('#assessmentName').val(),
                "existingOrgId" : $('#existingOrgId').val(),
                "existingContactId" : $('#existingContactId').val(),
                "existingContactBeingEdited" : $('#existingContactBeingEdited').val(),

                "organizationUri" : $('#organizationUri').val(),
                "organizationAbbr" : $('#organizationAbbr').val(),
                "organizationName" : $('#organizationName').val(),

                "contactResponder" : $('#contactResponder').val(),
                "contactEmail" : $('#contactEmail').val(),
                "contactPhoneNumber" : $('#contactPhoneNumber').val(),
                "contactMailingAddress" : $('#contactMailingAddress').val(),
                "contactNotes" : $('#contactNotes').val()
            }
        }//end buildFormSubmissionData()

        function clearErrorMessage(){
            $('#messageContainer').html('');
        }

        function setErrorMessage(msg) {
            $('#messageContainer').html('<div class="alert alert-danger">'+msg+'</div>');

            $('html, body').animate({
                scrollTop: $("#messageContainer").offset().top
            }, 500);
        }//end setErrorMesage()
    </script>
	</body>
</html>

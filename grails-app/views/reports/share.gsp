<%@ page import="nstic.web.*" defaultCodec="none" %>
<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main"/>
		<title>Share Report</title>

        <style type="text/css">

        </style>
	</head>
	<body>
    <ol class="breadcrumb">
        <li>
            <g:link controller="home" action="index">Home</g:link>
        </li>
        <li class="active">
            Share Reports
        </li>
    </ol>


    <h1> <span class="glyphicon glyphicon-share"></span> Share Reports </h1>
        <div class="pageSubsection">
            This page allows you to share the reports you have access to with another user.  Upon completing this form,
            you will generate a welcome email to a new user to create an account and view the reports you have access to.
            Note that if the contact information you input already has a user account (ie, has the same email), then the
            system <em>WILL NOT</em> generate a new email.  Just email that person yourself and tell them to view the
            reports.
        </div>

        <div id="messageContainer">
            <g:if test="${flash.message}">
                <div style="margin-top: 2em;" class="alert alert-info">
                    ${flash.message}
                </div>
            </g:if>
            <g:if test="${flash.warning}">
                <div style="margin-top: 2em;" class="alert alert-warning">
                    ${flash.warning}
                </div>
            </g:if>
        </div>

        <div class="pageContent" style="margin-top: 3em;">

            <form class="form-horizontal" action="${createLink(controller:'reports', action:'share')}" method="POST">
                <div class="form-group">
                    <label for="organization" class="col-sm-2 control-label">Organization</label>
                    <div class="col-sm-10">
                        <%
                            List<Organization> validOrgs = []
                            if( user.isUser() ){
                                validOrgs = Organization.findAll(); // Sort?
                            }else{
                                Organization.findAll().each { Organization org ->
                                    if( org.primaryContact.equals(user.contactInformation) ||
                                            org.contacts.contains(user.contactInformation) ||
                                            user.organization.equals(org) ){
                                        validOrgs.add( org );
                                    }
                                }
                            }
                        %>

                        <g:select name="organization" id="organization" class="form-control" onchange="loadContactsForOrg();"
                                  from="${validOrgs}" optionKey="id" optionValue="name" />
                    </div>
                </div>

                <div class="form-group">
                    <label class="col-sm-2 control-label">Contact Type</label>
                    <div class="col-sm-10">
                        <g:hiddenField name="contactType" id="contactType" value="" />
                        <ul class="nav nav-pills">
                            <li role="presentation" id="existingContactInfoPill"><a href="javascript:setExistingContactInfo()">Existing</a></li>
                            <li role="presentation" id="newContactInfoPill"><a href="javascript:setCreateNewContactInfo()">Create New</a></li>
                        </ul>
                    </div>
                </div>

                <div id="existingContactContainer">
                    <div class="form-group">
                        <label for="existingContactId" class="col-sm-2 control-label">Contact</label>
                        <div class="col-sm-10" id="contactListContainer">
                            <p class="form-control-static" id="existingContactId">
                                <asset:image src="spinner.gif" /> Loading Contact List...
                            </p>
                        </div>
                    </div>
                </div>

                <div id="newContactContainer">
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
                </div>

                <div class="form-group" style="margin-top: 2em;">
                    <div class="col-sm-offset-2 col-sm-10">
                        <button type="submit" class="btn btn-primary">
                            <span class="glyphicon glyphicon-share"></span>
                            Share
                        </button>
                        <a href="${createLink(controller:'home')}" class="btn btn-default">Cancel</a>
                    </div>
                </div>
            </form>

        </div>

        <script type="text/javascript">
            var LOADING_HTML = null;
            var CONTACTS_JSON = null;

            $(document).ready(function(){
                LOADING_HTML = $('#contactListContainer').html();
                <g:if test="${user.isUser()}">setExistingContactInfo();loadContacts();</g:if>
                <g:else>setCreateNewContactInfo();</g:else>
            })


            function setExistingContactInfo(){
                $('#contactType').val('existing');
                $('#existingContactInfoPill').addClass("active");
                $('#newContactInfoPill').removeClass("active");
                $('#existingContactContainer').show();
                $('#newContactContainer').hide();
                loadContacts();
            }
            function setCreateNewContactInfo(){
                $('#contactType').val('create');
                $('#existingContactInfoPill').removeClass("active");
                $('#newContactInfoPill').addClass("active");
                $('#existingContactContainer').hide();
                $('#newContactContainer').show();
            }


            function loadContacts() {
                $('#contactListContainer').html(LOADING_HTML + new Date().getMilliseconds());
                var url = '${createLink(controller: 'contactInformation', action: 'list')}';
                $.ajax({
                    url: url,
                    data: {
                        format: 'json',
                        now: new Date().getMilliseconds(),
                        max: 500,
                        sort: 'email',
                        order: 'asc'
                    },
                    dataType: 'json',
                    error: function(jqXHR, textStatus, errorThrown){
                        console.log("Error: "+errorThrown);
                        CONTACTS_JSON = null;
                        updateContactsListView();
                    },
                    success: function (data, status, jqXHR) {
                        console.log("Successfully got contacts list: "+JSON.stringify(data, null, 4));
                        CONTACTS_JSON = data;
                        updateContactsListView();
                    }
                })
            }//end loadContactsForOrg()


            function updateContactsListView() {
                var currentUserContactId = ${user.contactInformation.id};
                var html = "";
                if( CONTACTS_JSON && CONTACTS_JSON.length > 0 ){
                    html += '<select name="existingContactId" id="existingContactId" class="form-control">';

                    for( var i = 0; i < CONTACTS_JSON.length; i++ ){
                        var contact = CONTACTS_JSON[i];
                        if( contact.id != currentUserContactId ) {
                            html += '<option value="' + contact.id + '">' + contact.responder + '&nbsp;&nbsp;&lt;' + contact.email + '&gt;</option>';
                        }
                    }

                    html += '</select>';
                }else{
                    html += "<p class=\"form-control-static text-danger\" id=\"existingContactId\" style=\"font-weight: bold;\">No Contacts Available</p>"
                }
                $('#contactListContainer').html(html);
            }
        </script>

	</body>
</html>

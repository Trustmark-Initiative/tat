<%@ page import="nstic.web.User; nstic.web.Role" %>
<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main"/>
		<title>List Contacts</title>

        <style type="text/css">

        </style>
	</head>
	<body>
        %{--<ol class="breadcrumb">--}%
            %{--<li class="active">Home</li>--}%
        %{--</ol>--}%

        <h1>Contacts <small>(${contactsCountTotal} total)</small></h1>
        <div class="pageSubsection">

        </div>

        <div id="messageContainer">
            <g:if test="${flash.message}">
                <div style="margin-top: 2em;" class="alert alert-info">
                    ${flash.message}
                </div>
            </g:if>
        </div>

        <div class="pageContent">
            <table class="table table-striped table-bordered table-condensed">
                <thead>
                    <tr>
                        <th>&nbsp;</th>
                        <th title="If yes, then there is a user account for this contact.">User?</th>
                        <g:sortableColumn property="responder" title="Name" />
                        <g:sortableColumn property="email" title="Email" />
                        <g:sortableColumn property="phoneNumber" title="Phone" />
                        <th>Mailing Address</th>
                    </tr>
                </thead>
                <tbody>
                    <g:if test="${contacts && contacts.size() > 0}">
                        <g:each in="${contacts}" var="contact">
                            <tr>
                                <td><input class="contactCheckboxes" id="contactCheckbox${contact.id}" onchange="checkboxChanged('${contact.id}')" type="checkbox" contactId="${contact.id}" /></td>
                                <td style="text-align: center;">
                                    <%
                                        User user = User.findByContactInformation(contact);
                                        if( user ) {
                                            %>
                                    <span class="glyphicon glyphicon-ok" title="This contact IS used by a user account." style="color: rgb(0, 150, 0);"></span>
                                            <%
                                        }else{
                                            %>
                                    <span class="glyphicon glyphicon-remove" title="This contact is NOT used by a user account." style="color: rgb(150, 0, 0);"></span>
                                            <%
                                        }
                                     %>
                                    &nbsp; <!-- TODO -->
                                </td>
                                <td>
                                    <g:if test="${contact.responder && contact.responder.trim().length() > 0}">
                                        <g:link controller="contactInformation" action="view" id="${contact.id}">
                                            ${contact.responder}
                                        </g:link>
                                    </g:if>
                                </td>
                                <td>
                                    <g:link controller="contactInformation" action="view" id="${contact.id}">
                                        ${contact.email}
                                    </g:link>
                                </td>
                                <td>${contact.phoneNumber}</td>
                                <td>${contact.mailingAddress}</td>
                            </tr>
                        </g:each>
                    </g:if>
                    <g:else>
                        <tr>
                            <td colspan="6"><em>There are no contacts</em></td>
                        </tr>
                    </g:else>
                </tbody>
            </table>
            <div class="row" style="margin-bottom: 2em;">
                <div class="col-md-6">
                    <a href="${createLink(controller:'contactInformation', action:'create')}" class="btn btn-primary">New Contact</a>
                    <a href="javascript:editButtonClick()" id="editButton" class="onCheckOnly btn btn-default">Edit</a>
                    <a href="javascript:deleteButtonClick()" id="deleteButton" class="disabled btn btn-danger">Delete</a>
                </div><div class="col-md-6" style="text-align: right">
                    <g:paginate total="${contactsCountTotal}" />
                </div>
            </div>

        </div>

        <script type="text/javascript">
            $( document ).ready(function() {
                updateCheckOnlyButtons();
            });

            function checkboxChanged(userId){
                updateCheckOnlyButtons();
            }//end checkboxChanged()

            function updateCheckOnlyButtons(){
                if( isCheckboxChecked() ){
                    $('#editButton').removeClass('disabled');
//                    $('#deleteButton').removeClass('disabled');
                }else{
                    $('#editButton').addClass('disabled');
//                    $('#deleteButton').addClass('disabled');
                }
            }

            function isCheckboxChecked() {
                return $('.contactCheckboxes:checked').length > 0;
            }//end isCheckboxChecked()

            function editButtonClick() {
                if( $('.contactCheckboxes:checked').length > 1 ){
                    alert("Select only 1 contact to edit, please.")
                    return;
                }

                var contactId = $('.contactCheckboxes:checked').get(0).id.replace('contactCheckbox', '');
                var url = "${createLink(controller:'contactInformation', action: 'edit')}?contactId="+contactId;
                window.location.replace( url );
            }//end editButtonClick()

            function deleteButtonClick() {
                // TODO This is not implemented until it's necessary to delete users.
            }//end deleteButtonClick()

        </script>

	</body>
</html>

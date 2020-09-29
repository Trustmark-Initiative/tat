<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main"/>
		<title>View Contact</title>

        <style type="text/css">

            .infoTable {

            }
            .infoTable td {
                padding: 0.5em;
            }
        </style>
	</head>
	<body>

        <h1>View Contact</h1>
        <div class="pageSubsection">
        </div>


        <div class="pageContent">
            <table class="infoTable">
                <tr>
                    <td>Unique Id</td><td>${contact.id}</td>
                </tr><tr>
                    <td>Responder Name</td><td>${contact.responder}</td>
                </tr><tr>
                    <td>Email</td><td>${contact.email}</td>
                </tr><tr>
                    <td>Phone Number</td><td>${contact.phoneNumber}</td>
                </tr><tr>
                    <td>Mailing Address</td><td>${contact.mailingAddress}</td>
                </tr><tr>
                    <td>Notes</td><td>${contact.notes}</td>
                </tr>
            </table>

            <div style="margin-top: 2em;">
                <g:link controller="contactInformation" action="edit" params="[contactId: contact.id]" class="btn btn-default">Edit</g:link>
                <g:link controller="contactInformation" action="list" class="btn btn-default">Contacts List</g:link>
            </div>
        </div>

	</body>
</html>

<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main"/>
		<title>Create Contact</title>

        <style type="text/css">

        </style>
	</head>
	<body>
        %{--<ol class="breadcrumb">--}%
            %{--<li class="active">Home</li>--}%
        %{--</ol>--}%

        <h1>Create New Contact</h1>
        <div class="pageSubsection">

        </div>

        <g:hasErrors bean="${contactCommand}">
            <div style="margin-top: 2em;" class="alert alert-danger">
                <g:renderErrors bean="${contactCommand}" />
            </div>
        </g:hasErrors>


        <div class="pageContent">
            <g:form class="form-horizontal" name="createContactForm" method="POST" action="save">
                <div class="form-group">
                    <label for="email" class="col-sm-2 control-label">Email address</label>
                    <div class="col-sm-10">
                        <g:textField name="email" id="email" class="form-control" placeholder="user@example.com" value="${contactCommand?.email}" />
                    </div>
                </div>
                <div class="form-group">
                    <label for="responder" class="col-sm-2 control-label">Responder Name</label>
                    <div class="col-sm-10">
                        <g:textField name="responder" id="responder" class="form-control" placeholder="Name" value="${contactCommand?.responder}" />
                    </div>
                </div>
                <div class="form-group">
                    <label for="phone" class="col-sm-2 control-label">Phone Number</label>
                    <div class="col-sm-10">
                        <g:textField name="phone" id="phone" class="form-control" placeholder="555-555-5555" value="${contactCommand?.phone}" />
                    </div>
                </div>
                <div class="form-group" >
                    <label for="mailingAddress" class="col-sm-2 control-label">Mailing address</label>
                    <div class="col-sm-10">
                        <g:textField name="mailingAddress" id="mailingAddress" class="form-control" placeholder="Address Full Text" value="${contactCommand?.mailingAddress}" />
                    </div>
                </div>
                <div class="form-group" >
                    <label for="notes" class="col-sm-2 control-label">Notes</label>
                    <div class="col-sm-10">
                        <g:textField name="notes" id="notes" class="form-control" placeholder="" value="${contactCommand?.notes}" />
                    </div>
                </div>
                <div class="form-group">
                    <div class="col-sm-offset-2 col-sm-10">
                        <button type="submit" class="btn btn-primary">Save</button>
                        <a href="${createLink(controller:'contactInformation', action: 'list')}" class="btn btn-default">Cancel</a>
                        <g:if env="development">
                            <a href="javascript:fillForm()" class="btn btn-default">Auto-Fill</a>
                        </g:if>
                    </div>
                </div>
            </g:form>

        </div>

        <script type="text/javascript">
            function fillForm() {
                $('#email').val('test@gtri.org');
                $('#responder').val('Test User');
                $('#phone').val('555-555-5555');
                $('#mailingAddress').val('250 14th Stree NW, Atlanta, GA');
                $('#notes').val('Auto-filled by development button.');
            }//end fillForm()
        </script>
	</body>
</html>

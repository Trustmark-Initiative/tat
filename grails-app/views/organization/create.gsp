<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main"/>
		<title>Create Organization</title>

        <style type="text/css">

        </style>
	</head>
	<body>
        %{--<ol class="breadcrumb">--}%
            %{--<li class="active">Home</li>--}%
        %{--</ol>--}%

        <h1>Create New Trustmark Recipient Organization</h1>
        <div class="pageSubsection">

        </div>

        <g:hasErrors bean="${orgCommand}">
            <div style="margin-top: 2em;" class="alert alert-danger">
                <g:renderErrors bean="${orgCommand}" />
            </div>
        </g:hasErrors>


        <div class="pageContent">
            <g:form class="form-horizontal" name="createOrganizationForm" method="POST" action="save">
                <fieldset>
                    <legend>Organization Information</legend>

                    <div class="form-group">
                        <label for="uri" class="col-sm-2 control-label">URI</label>
                        <div class="col-sm-10">
                            <g:textField name="uri" id="uri" class="form-control" placeholder="http://www.somewhere.com" value="${orgCommand?.uri}"
                                         data-toggle="tooltip" data-placement="top" title="This is the default Trustmark Recipient Identifier for this organization."/>
                        </div>
                    </div>
                    <div class="form-group">
                        <label for="identifier" class="col-sm-2 control-label">Abbreviation</label>
                        <div class="col-sm-10">
                            <g:textField name="identifier" id="identifier" class="form-control" placeholder="ABBR" value="${orgCommand?.identifier}" />
                        </div>
                    </div>
                    <div class="form-group">
                        <label for="name" class="col-sm-2 control-label">Name</label>
                        <div class="col-sm-10">
                            <g:textField name="name" id="name" class="form-control" placeholder="Name" value="${orgCommand?.name}" />
                        </div>
                    </div>

                </fieldset>
                <fieldset>
                    <legend>Primary Contact Information</legend>

                    <div class="form-group">
                        <label for="email" class="col-sm-2 control-label">Email address</label>
                        <div class="col-sm-10">
                            <g:textField name="email" id="email" class="form-control" placeholder="user@example.com" value="${orgCommand?.email}" />
                        </div>
                    </div>
                    <div class="form-group">
                        <label for="responder" class="col-sm-2 control-label">Responder Name</label>
                        <div class="col-sm-10">
                            <g:textField name="responder" id="responder" class="form-control" placeholder="Name" value="${orgCommand?.responder}" />
                        </div>
                    </div>
                    <div class="form-group">
                        <label for="phone" class="col-sm-2 control-label">Phone Number</label>
                        <div class="col-sm-10">
                            <g:textField name="phone" id="phone" class="form-control" placeholder="555-555-5555" value="${orgCommand?.phone}" />
                        </div>
                    </div>
                    <div class="form-group" >
                        <label for="mailingAddress" class="col-sm-2 control-label">Mailing address</label>
                        <div class="col-sm-10">
                            <g:textField name="mailingAddress" id="mailingAddress" class="form-control" placeholder="Address Full Text" value="${orgCommand?.mailingAddress}" />
                        </div>
                    </div>
                    <div class="form-group" >
                        <label for="notes" class="col-sm-2 control-label">Notes</label>
                        <div class="col-sm-10">
                            <g:textField name="notes" id="notes" class="form-control" placeholder="" value="${orgCommand?.notes}" />
                        </div>
                    </div>

                </fieldset>

                <div class="form-group">
                    <div class="col-sm-offset-2 col-sm-10">
                        <button type="submit" class="btn btn-primary">Save</button>
                        <a href="${createLink(controller:'organization', action: 'list')}" class="btn btn-default">Cancel</a>
                        <g:if env="development">
                            <a href="javascript:fillForm()" class="btn btn-default">Auto-Fill</a>
                        </g:if>
                    </div>
                </div>
            </g:form>

        </div>

        <script type="text/javascript">
            function fillForm() {
                $('#uri').val('http://gtri.org');
                $('#name').val('Georgia Tech Research Institute')
                $('#email').val('test@gtri.org');
                $('#responder').val('Test User');
                $('#phone').val('555-555-5555');
                $('#mailingAddress').val('250 14th Stree NW, Atlanta, GA');
                $('#notes').val('Auto-filled by development button.');
            }//end fillForm()
        </script>
	</body>
</html>

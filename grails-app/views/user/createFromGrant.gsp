<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main"/>
		<title>Create User from Grant</title>

        <style type="text/css">

        </style>
	</head>
	<body>

        <g:if test="${newAccount}">
            <h1>Create User Account</h1>
            <div class="pageSubsection">
                Please input a password for your account.
            </div>

            <div id="errorContainer">
                <g:if test="${flash.error}">
                    <div class="alert alert-danger" style="margin-top: 2em;">${flash.error}</div>
                </g:if>
            </div>

            <div class="pageContent" style="margin-top: 2em;">
                <form class="form-horizontal" name="createUserForm" method="POST" action="${createLink(controller:'user', action:'createFromGrant', id: contactGrant.grantId)}">
                    <g:hiddenField name="grantId" id="grantId" value="${contactGrant.grantId}" />

                    <fieldset>
                        <legend style="margin-bottom: 0;">Login Information</legend>
                        <div class="text-muted" style="margin-bottom: 1em;">This is how you will log into the assessment tool.</div>

                        <div class="form-group">
                            <label class="col-sm-2 control-label">Email address <br/><small>(&amp; Username)</small></label>
                            <div class="col-sm-10">
                                <p class="form-control-static">${contactGrant.contactInformation.email}</p>
                            </div>
                        </div>
                        <div class="form-group">
                            <label for="password" class="col-sm-2 control-label">Password</label>
                            <div class="col-sm-5">
                                <g:passwordField name="password" id="password" class="form-control col-md-6" placeholder="Password" />
                            </div>
                            <div class="col-sm-5">
                                <g:passwordField name="passwordAgain" id="passwordAgain" class="form-control col-md-6" placeholder="Password (Again)" />
                            </div>
                        </div>
                        <div class="form-group">
                            <div class="col-sm-offset-2 col-sm-10">
                                <button type="submit" class="btn btn-primary">Save</button>
                            </div>
                        </div>
                    </fieldset>

                    <fieldset>
                        <legend>Contact Information</legend>

                        <div class="form-group">
                            <label class="col-sm-2 control-label">Name</label>
                            <div class="col-sm-10">
                                <p class="form-control-static">${contactGrant.contactInformation.responder}</p>
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="col-sm-2 control-label">Phone Number</label>
                            <div class="col-sm-10">
                                <p class="form-control-static">${contactGrant.contactInformation.phoneNumber}</p>
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="col-sm-2 control-label">Mailing Address</label>
                            <div class="col-sm-10">
                                <p class="form-control-static">${contactGrant.contactInformation.mailingAddress}</p>
                            </div>
                        </div>

                    </fieldset>

                    <fieldset>
                        <legend style="margin-bottom: 0;">Organization</legend>
                        <div class="text-muted" style="margin-bottom: 1em;">This is the organization you can now run reports on.</div>

                        <div class="form-group">
                            <label class="col-sm-2 control-label">URI</label>
                            <div class="col-sm-10">
                                <p class="form-control-static">${contactGrant.organization.uri}</p>
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="col-sm-2 control-label">Abbreviation</label>
                            <div class="col-sm-10">
                                <p class="form-control-static">${contactGrant.organization.identifier}</p>
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="col-sm-2 control-label">Name</label>
                            <div class="col-sm-10">
                                <p class="form-control-static">${contactGrant.organization.name}</p>
                            </div>
                        </div>
                    </fieldset>

                </form>
            </div>

        </g:if><g:else>
            <h1>Please Login</h1>
            <div class="pageSubsection">
                In order to view the reports for organization ${contactGrant.organization.name}, you must first login.
            </div>

            <div style="margin-top: 2em;">
                <form class="form-horizontal">
                    <fieldset>
                        <legend>Contact Information</legend>

                        <div class="form-group">
                            <label class="col-sm-2 control-label">Name</label>
                            <div class="col-sm-10">
                                <p class="form-control-static">${contactGrant.contactInformation.responder}</p>
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="col-sm-2 control-label">Phone Number</label>
                            <div class="col-sm-10">
                                <p class="form-control-static">${contactGrant.contactInformation.phoneNumber}</p>
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="col-sm-2 control-label">Mailing Address</label>
                            <div class="col-sm-10">
                                <p class="form-control-static">${contactGrant.contactInformation.mailingAddress}</p>
                            </div>
                        </div>

                        <div class="form-group">
                            <div class="col-sm-offset-2 col-sm-10">
                                <a href="${createLink(controller: 'login')}" class="btn btn-primary">
                                    Login
                                </a>
                            </div>
                        </div>

                    </fieldset>

                    <fieldset>
                        <legend style="margin-bottom: 0;">Organization</legend>
                        <div class="text-muted" style="margin-bottom: 1em;">This is the organization you can now run reports on.</div>

                        <div class="form-group">
                            <label class="col-sm-2 control-label">URI</label>
                            <div class="col-sm-10">
                                <p class="form-control-static">${contactGrant.organization.uri}</p>
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="col-sm-2 control-label">Abbreviation</label>
                            <div class="col-sm-10">
                                <p class="form-control-static">${contactGrant.organization.identifier}</p>
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="col-sm-2 control-label">Name</label>
                            <div class="col-sm-10">
                                <p class="form-control-static">${contactGrant.organization.name}</p>
                            </div>
                        </div>
                    </fieldset>

                </form>
            </div>
        </g:else>


	</body>
</html>

<!DOCTYPE html>
<html>
	<head>
        <meta charset="UTF-8">
        <meta name="layout" content="main"/>
		<title>Home</title>

        <style type="text/css">
            .mustLoginDetails {
                text-align: center;
                margin-top: 5em;
            }

            .mustLoginText {
                margin-bottom: 1em;
            }

            .searchFormContainer {
                margin-bottom: 0.5em;
            }
        </style>
        <style type="text/css">
            .reportContainer {
            }
            .reportLink {
                color: #333;
            }
            .reportLink :HOVER {
                color: #333;
            }
            .reportLink:HOVER .reportTitle {
                color: #337ab7;
            }
            .reportIcon {
                font-size: 55px;
            }
            .reportTextContainer {
                padding-top: 1em;
            }
        </style>
	</head>
	<body>
    <g:if test='${flash.message}'>
        <div class="alert alert-danger" style="margin-top: 1em; width: 45%;">
            <div style="font-weight: bold;">Authentication Failed!</div>
            ${flash.message}
        </div>
    </g:if>

    <sec:ifNotLoggedIn>
            <g:if test="${firstTimeLogin}">
                <script type="text/javascript">
                    $(document).ready(function(){
                        document.getElementById('registryUrl').value = '${grailsApplication.config.registry.url}';
                        document.getElementById('contactResponder').value = "${grailsApplication.config.contact.'1'.responder}";
                        document.getElementById('contactAddr').value = "${grailsApplication.config.contact.'1'.mailingAddress}";
                        document.getElementById('contactEmail').value = "${grailsApplication.config.contact.'1'.email}".replace("&#64;", "@");
                        document.getElementById('contactPhone').value = "${grailsApplication.config.contact.'1'.telephone}";
                        document.getElementById('organizationName').value = "${grailsApplication.config.org.'1'.name}";
                        document.getElementById('organizationId').value = "${grailsApplication.config.org.'1'.identifier}";
                        document.getElementById('organizationUri').value = "${grailsApplication.config.org.'1'.uri}";
                        document.getElementById('username').value = "${grailsApplication.config.user.'1'.username}";

                        document.getElementById('registryUrl').focus();
                    });
                    function validate(frm)  {
                        if (frm.username.value == "")  {
                            alert('User name cannot be blank!');
                            frm.username.focus();
                            return false;
                        }
                        if (frm.password.value == "")  {
                            alert('Password cannot be blank!');
                            frm.password.focus();
                            return false;
                        }
                        if (frm.passwordAgain.value == "")  {
                            alert('Password re-enter cannot be blank!');
                            frm.passwordAgain.focus();
                            return false;
                        }
                        if (frm.password.value != frm.passwordAgain.value)  {
                            alert('Passwords do not match!'+ frm.password.value +' - '+frm.passwordAgain.value);
                            frm.passwordAgain.focus();
                            return false;
                        }
                        if (frm.registryUrl.value == null || frm.registryUrl.value == "")  {
                            alert('Registry Url cannot be blank!');
                            frm.registryUrl.focus();
                            return false;
                        }
                        if (frm.contactResponder.value == "")  {
                            alert('Contact name cannot be blank!');
                            frm.contactResponder.focus();
                            return false;
                        }
                        if (frm.contactAddr.value == null || frm.contactAddr.value == "")  {
                            alert('Contact address cannot be blank!');
                            frm.contactAddr.focus();
                            return false;
                        }
                        if (frm.contactEmail.value == null || frm.contactEmail.value == "")  {
                            alert('Contact Email cannot be blank!');
                            frm.contactEmail.focus();
                            return false;
                        }
                        if (frm.contactPhone.value == null || frm.contactPhone.value == "")  {
                            alert('Contact phone cannot be blank!');
                            frm.contactPhone.focus();
                            return false;
                        }
                        if (frm.organizationName.value == null || frm.organizationName.value == "")  {
                            alert('Organization Name cannot be blank!');
                            frm.organizationName.focus();
                            return false;
                        }
                        if (frm.organizationId.value == null || frm.organizationId.value == "")  {
                            alert('Organization Id cannot be blank!');
                            frm.organizationId.focus();
                            return false;
                        }
                        if (frm.organizationUri.value == null || frm.organizationUri.value == "")  {
                            alert('Organization URI cannot be blank!');
                            frm.organizationUri.focus();
                            return false;
                        }
                        return true;
                    }
                </script>
                <div class="row">
                    <h2><g:message code="first.time.message1"/></h2><br>
                        <form action='${createLink(controller: 'home', action: 'initialize')}' method='POST' id='loginForm' class='form-horizontal' autocomplete='off' role="form" onsubmit="return validate(this)">
                            <fieldset>
                                <legend><h5><g:message code="first.time.message3"/></h5></legend>
                                <div class="form-group">
                                    <label for="registryUrl" class="col-md-3 control-label"><g:message code="registry.tpat.url.label"/></label>
                                    <div class="col-md-4">
                                        <input type="text" class="form-control" id="registryUrl" name="registryUrl" placeholder="${grailsApplication.config.registry.url}">
                                    </div>
                                </div>
                            </fieldset>
                            <fieldset>
                                <legend><h5><g:message code="first.time.message4"/></h5></legend>
                                    <div class="form-group">
                                        <label for="contactResponder" class="col-md-3 control-label"><g:message code="contact.responder.label"/></label>
                                        <div class="col-md-4">
                                            <input type="text" class="form-control" id="contactResponder" name="contactResponder" placeholder='<g:message code="contact.responder.label"/>' >
                                        </div>
                                    </div>
                                    <div class="form-group">
                                        <label for="contactAddr" class="col-md-3 control-label"><g:message code="contact.address.label"/></label>
                                        <div class="col-md-4">
                                            <input type="text" class="form-control" id="contactAddr" name="contactAddr" placeholder='<g:message code="contact.address.label"/>' >
                                        </div>
                                    </div>
                                    <div class="form-group">
                                        <label for="contactEmail" class="col-md-3 control-label"><g:message code="contact.email.label"/></label>
                                        <div class="col-md-4">
                                            <input type="text" class="form-control" id="contactEmail" name="contactEmail" placeholder='<g:message code="contact.email.label"/>' >
                                        </div>
                                    </div>
                                    <div class="form-group">
                                        <label for="contactPhone" class="col-md-3 control-label"><g:message code="contact.phone.label"/></label>
                                        <div class="col-md-4">
                                            <input type="text" class="form-control" id="contactPhone" name="contactPhone" placeholder='<g:message code="contact.phone.label"/>' >
                                        </div>
                                    </div>
                            </fieldset>
                            <legend><h5><g:message code="first.time.message5"/></h5></legend>
                            <fieldset>
                                    <div class="form-group">
                                        <label for="organizationName" class="col-md-3 control-label"><g:message code="organization.name.label"/></label>
                                        <div class="col-md-4">
                                            <input type="text" class="form-control" id="organizationName" name="organizationName" placeholder='<g:message code="organization.name.label"/>' >
                                        </div>
                                    </div>
                                    <div class="form-group">
                                        <label for="organizationId" class="col-md-3 control-label"><g:message code="organization.identifier.label"/></label>
                                        <div class="col-md-4">
                                            <input type="text" class="form-control" id="organizationId" name="organizationId" placeholder='<g:message code="organization.identifier.label"/>' >
                                        </div>
                                    </div>
                                <div class="form-group">
                                    <label for="organizationUri" class="col-md-3 control-label"><g:message code="organization.uri.label"/></label>
                                    <div class="col-md-4">
                                        <input type="text" class="form-control" id="organizationUri" name="organizationUri" placeholder='<g:message code="organization.uri.label"/>' >
                                    </div>
                                </div>
                            </fieldset>
                            <fieldset>
                                <legend><h5><g:message code="first.time.message2"/></h5></legend>
                                <div class="form-group">
                                    <label for="username" class="col-md-3 control-label"><g:message code="initialize.username.label"/></label>
                                    <div class="col-md-4">
                                        <input type="text" class="form-control" id="username" name="username" placeholder='<g:message code="initialize.username.label"/>' >
                                    </div>
                                </div>
                                <div class="form-group">
                                    <label for="password" class="col-md-3 control-label"><g:message code="initialize.password.label"/></label>
                                    <div class="col-md-4">
                                        <input type="password" class="form-control" id="password" name="password" placeholder=<g:message code="initialize.password.label"/> >
                                    </div>
                                </div>
                                <div class="form-group">
                                    <label for="passwordAgain" class="col-md-3 control-label"><g:message code="reenter.password.label"/></label>
                                    <div class="col-md-4">
                                        <input type="password" class="form-control" id="passwordAgain" name="passwordAgain" placeholder='<g:message code="reenter.password.label"/>' >
                                    </div>
                                </div>
                            </fieldset>
                            <div class="form-group">
                                <div class="col-md-3"></div>
                                    <div class="col-md-2">
                                        <input class="btn btn-default" type='submit' id="submit" value='${message(code: "tf.initialize.label")}'/>
                                    </div>
                            </div>
                    </form>
                </div>
            </g:if><g:else>
            <div class="row">
                <div class="col-md-offset-4 col-md-4">
                    <div class="mustLoginDetails alert alert-warning">
                        <div class="mustLoginText">
                            This page requires authentication.  Please click below to start the process.
                        </div>
                        <a href="${createLink(controller:'login')}" class="btn btn-primary">Login &raquo;</a>
                    </div>
                </div>
            </div>

            <div style="height: 600px;">&nbsp;</div>
        </g:else>
        </sec:ifNotLoggedIn>

        <sec:ifLoggedIn>
            <sec:ifAnyGranted roles="ROLE_USER,ROLE_ADMIN">
                <h3>Assessments <small>(Total ${assessmentCount})</small></h3>
                <div class="searchFormContainer">
                    <form class="form-inline" action="${createLink(controller:'assessmentSearch', action: 'search')}">
                        <div class="form-group">
                            <input name="q" type="text" class="form-control" placeholder="Search Phrase" />
                        </div>
                        <button type="submit" class="btn btn-default">Search</button>
                    </form>
                </div>

                <div id="assessmentsContainer">
                    <div style="margin-top: 2em; margin-bottom: 2em;">
                        <asset:image src="spinner.gif" /> Loading most relevant Assessments...
                    </div>
                </div>
                <script type="text/javascript">
                    $(document).ready(function(){
                        var url = '${createLink(controller:'assessment', action: 'mostRelevantList')}';
                        console.log('Getting contents of: '+url);
                        $.get(url, function(data){
                            $('#assessmentsContainer').html(data);
                        });
                    });
                </script>
                <div>
                    <a href="${createLink(controller:'assessment', action: 'create')}" class="btn btn-default">Create</a>
                    <a href="${createLink(controller:'assessment', action: 'list')}" class="btn btn-primary">Manage &raquo;</a>
                </div>


                <h3 style="margin-top: 2em;">Trustmark Definitions <small>(${trustmarkDefinitions.size()} of ${trustmarkDefinitionCount})</small></h3>
                <div class="searchFormContainer">
                    <form class="form-inline">
                        <div class="form-group">
                            <input type="text" class="form-control" placeholder="Search Phrase" />
                        </div>
                        <button type="submit" class="btn btn-default">Search</button>
                    </form>
                </div>

                <div>
                    <table class="table table-condensed table-striped table-bordered">
                        <thead>
                        <tr>
                            <th>Name</th>
                            <th>Trustmarks</th>
                        </tr>
                        </thead>
                        <tbody>
                        <g:if test="${trustmarkDefinitionCount > 0}">
                            <g:each in="${trustmarkDefinitions}" var="td">
                                <tr>
                                    <td>
                                        <g:link controller="trustmarkDefinition" action="view" id="${td.id}">
                                            ${td.name}
                                        </g:link>
                                    </td>
                                    <td>?</td>
                                </tr>
                            </g:each>
                        </g:if><g:else>
                            <tr>
                                <td colspan="2"><em>There are no Trustmark Definitions.</em></td>
                            </tr>
                        </g:else>
                        </tbody>
                    </table>
                </div>
                <div>
                    <a href="${createLink(controller:'trustmarkDefinition', action: 'list')}" class="btn btn-primary">Manage &raquo;</a>
                </div>

            </sec:ifAnyGranted>
            <sec:ifNotGranted roles="ROLE_USER,ROLE_ADMIN">
                <h3 style="margin-top: 2em;">Report Only Account</h3>
                <div class="text-muted">
                    On this page, you can view the organization report for ${user?.organization?.name},
                    or share it with another user.
                </div>

                <div style="margin-top: 2em;" class="row">
                    <div class="col-md-4">
                        <div class="reportContainer">
                            <a href="${createLink(controller:'reports', action: 'organizationReport')}" class="reportLink">
                                <div class="row">
                                    <div class="col-md-2 reportIcon">
                                        <span class="glyphicon glyphicon-home"></span>
                                    </div>
                                    <div class="col-md-10 reportTextContainer">
                                        <h4 class="reportTitle">Organization Details</h4>
                                        <div class="reportDescription">
                                            Given a single organization, this report will provide a look into what the status
                                            of each trustmark assessment is.
                                        </div>
                                    </div>
                                </div>
                            </a>
                        </div>
                    </div>
                    <div class="col-md-8">
                        <a href="${createLink(controller:'reports', action: 'share')}" class="btn btn-primary">
                            <span class="glyphicon glyphicon-share"></span>
                            Share Reports
                        </a>
                    </div>
                </div>

            </sec:ifNotGranted>
        </sec:ifLoggedIn>
	</body>
</html>

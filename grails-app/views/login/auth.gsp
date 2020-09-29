<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main"/>
		<title>Login</title>

        <r:require modules="application" />
	</head>
	<body>
        <h2>Assessment Tool Login</h2>

        <div class="pageContent">

            <g:if test='${flash.message}'>
                <div class="alert alert-danger" style="margin-top: 1em; width: 45%;">
                    <div style="font-weight: bold;">Authentication Failed!</div>
                    ${flash.message}
                </div>
            </g:if>


            <form action='${postUrl}' method='POST' id='loginForm' class='form-horizontal' autocomplete='off' role="form">

                <div class="form-group">
                    <label for="username" class="col-sm-1 control-label"><g:message code="springSecurity.login.username.label"/></label>
                    <div class="col-sm-3">
                        <input type="text" class="form-control" id="username" name="username" placeholder="Username">
                    </div>
                </div>

                <div class="form-group">
                    <label for="password" class="col-sm-1 control-label"><g:message code="springSecurity.login.password.label"/></label>
                    <div class="col-sm-3">
                        <input type="password" class="form-control" id="password" name="password" placeholder="Password">
                        <p class="help-block"><a href="${createLink(controller:'forgotPassword')}">Forgot Password?</a></p>
                    </div>
                </div>

                <div class="form-group">
                    <div class="col-sm-offset-1 col-md-5">
                        <div class="checkbox">
                            <label>
                                <input type='checkbox' class='chk' name='${rememberMeParameter}' id='remember_me' <g:if test='${hasCookie}'>checked='checked'</g:if>/>
                                <label for='remember_me'><g:message code="springSecurity.login.remember.me.label"/></label>
                            </label>
                        </div>
                    </div>
                </div>

                <div class="form-group">
                    <div class="col-sm-offset-1 col-sm-2">
                        <input class="btn btn-default" type='submit' id="submit" value='${message(code: "springSecurity.login.button")}'/>
                    </div>
                </div>

            </form>

            </div>
        </div>

        <script>$(function () { $('#username').focus(); });</script>
	</body>
</html>

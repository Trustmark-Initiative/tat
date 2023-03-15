<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main"/>
		<title>Forgot Password</title>

	</head>
	<body>
        %{--<ol class="breadcrumb">--}%
            %{--<li class="active">Home</li>--}%
        %{--</ol>--}%

        <h1>Forgot Password</h1>
        <div class="pageSubsection">
            On this page, you can recover your password by submitting your information.
        </div>

        <sec:authorize access="!isAuthenticated()">
            <div style="margin-top: 2em;">

                <form class="form-horizontal">
                    <div class="form-group">
                        <label for="inputEmail3" class="col-sm-2 control-label">Email</label>
                        <div class="col-sm-10">
                            <input type="email" class="form-control" id="email" name="email" placeholder="Email">
                        </div>
                    </div>
                    <div class="form-group">
                        <div class="col-sm-offset-2 col-sm-10">
                            <a href="javascript:sendNewPasswordRequest()" class="btn btn-default">Send New Password</a>
                        </div>
                    </div>
                </form>


                <div id="messageFeedbackWindow" style="margin-top: 2em;">&nbsp;</div>

            </div>

            <script type="text/javascript">

                function sendNewPasswordRequest() {
                    var email = $('#email').val();
                    $('#messageFeedbackWindow').html('<div><asset:image src="spinner.gif" /> Resetting password... </div>');
                    console.log("Submitting email '"+email+"' for password reset...");
                    $.ajax({
                        url: '${createLink(controller:'forgotPassword', action: 'resetPassword')}',
                        data: {
                            email: email,
                            format: 'json',
                            now: new Date().toString()
                        },
                        dataType: 'json',
                        success: function(data, status, xhr){
                            console.log("Received answer: "+JSON.stringify(data, null, 2));
                            if( data.status == "SUCCESS" ){
                                $('#messageFeedbackWindow').html("<div class=\"alert alert-success\">"+data.message+"</div>")
                            }else{
                                $('#messageFeedbackWindow').html("<div class=\"alert alert-danger\">"+data.message+"</div>")
                            }

                        },
                        error: function(xhr, statusText, errorThrown){
                            console.log("Error: "+statusText+", Error: "+errorThrown);
                        }
                    })

                }//end sendNewPasswordRequest()


            </script>

        </sec:authorize>

        <sec:authorize access="isAuthenticated()">
            <h3 class="text-danger">You are already logged in, please visit the "Profile" page and reset your password.</h3>
        </sec:authorize>
	</body>
</html>

<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<!DOCTYPE html>
<!--[if lt IE 7 ]> <html lang="en" class="no-js ie6"> <![endif]-->
<!--[if IE 7 ]>    <html lang="en" class="no-js ie7"> <![endif]-->
<!--[if IE 8 ]>    <html lang="en" class="no-js ie8"> <![endif]-->
<!--[if IE 9 ]>    <html lang="en" class="no-js ie9"> <![endif]-->
<!--[if (gt IE 9)|!(IE)]><!--> <html lang="en" class="no-js"><!--<![endif]-->
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
        <title>${grailsApplication.config.tf.tool.name}</title>
		<meta name="viewport" content="width=device-width, initial-scale=1.0">

        <asset:stylesheet src="application.css"/>
        <asset:javascript src="application.js"/>

		<g:layoutHead/>

        <style type="text/css">
            #ajaxLoadingIndicator {
                position: absolute;
                top: 0;
                right: 0;
                z-index: 1000;
                padding: 1em;
                margin: 1em;
                font-weight: bold;
                font-size: 120%;
                border: 2px solid #${nstic.assessment.ColorPalette.DEFAULT_BORDER.toString()};
                background-color: white;
            }
        </style>

        <script type="text/javascript">

            $( document ).ready(function(){
                $(document).ajaxStart(function(){
                    // TODO Check to see if 'ajaxLoadingIndicator' is on page...
                    var imgHtml = "<img src=\"<asset:assetPath src="spinner.gif" />\" />";
                    $('body').append("<div id=\"ajaxLoadingIndicator\">"+imgHtml+" Loading..."+"</div>");
                    var screenTop = $(document).scrollTop();
                    $('#ajaxLoadingIndicator').css('top', screenTop);
                })

                $(document).ajaxComplete(function(){
                    $('#ajaxLoadingIndicator').remove();
                })
            })

        </script>

	</head>
         	<body>

    <tmpl:/layouts/menu />

    <a name="top"></a>
            <div class="container" style="margin-top: 5em; margin-bottom: 5em;">
<!--            <div class="container tatMainContainer">  -->
                <div class="row header">
                    <div class="col-md-2 headerTopLeft">
                        <div id="header-logo">
                            <asset:image height="90em" src="${grailsApplication.config.tf.tool.banner}" />
                        </div>
                    </div>
                </div>
                <div class="content row">
                    <div class="col-md-12">
                        <g:layoutBody/>
                    </div>
                </div>

                <div style="margin-bottom: 4em;">&nbsp;</div>
            </div>

        <div id="footerContainer">
            <div class="container">
                <div class="row">
                    <div class="col-md-12">
                        <div>Copyright &copy; 2013-2020, Georgia Tech Research Institute</div>
                        <div>
                            v.<g:meta name="info.app.version"/>,
                            Build Date: <g:meta name="info.app.buildDate"/>
                        </div>
                        <div>
                            Your IP: ${request.remoteAddr}
                        </div>
                    </div>
                </div>
            </div>

        </div>
	</body>
</html>

<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main"/>
		<title>Error Test Page</title>

        <style type="text/css">
            .buttonList {
                list-style: none;
                margin: 0;
                padding: 0;
            }

            .buttonListItem {
                margin: 0;
                padding: 0;
                margin-bottom: 1em;
            }
        </style>
	</head>
	<body>

        <h2 class="pageTitle">Error Testing Page</h2>
        <div class="pageDescription">
            On this page, you can test what will happen for various system errors.  This is meant for internal
            testing only.
        </div>

        <div id="processErrorContainer"></div>
        <div class="pageContent">

            <ul class="buttonList">
                <li class="buttonListItem">
                    <a href="${createLink(controller:'error', action:'generate500')}" class="btn btn-default">Error 500</a>
                    Generate a 500 error (Internal Servlet Exception)
                </li>
                <li class="buttonListItem">
                    <a href="${createLink(uri: '/this-will-never-exist.nono')}" class="btn btn-default">404 Not Found</a>
                    Reference a resource which will not exist.
                </li>
            </ul>
        </div>

	</body>
</html>

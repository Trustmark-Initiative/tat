<%@ page import="nstic.web.User; nstic.web.Role" %>
<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main"/>
		<title>Reports Listing</title>

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
                padding-top: 10px;
            }
        </style>
	</head>
	<body>
        %{--<ol class="breadcrumb">--}%
            %{--<li class="active">Home</li>--}%
        %{--</ol>--}%

        <h1> <span class="glyphicon glyphicon-stats"></span> Reports </h1>
        <div class="pageSubsection">
            On this page, you can view system wide reports that give broader insight into what organizations have
            what trustmarks, and what the overall status of assessments are.
        </div>

        <div id="messageContainer">
            <g:if test="${flash.message}">
                <div style="margin-top: 2em;" class="alert alert-info">
                    ${flash.message}
                </div>
            </g:if>
        </div>

        <div class="pageContent" style="margin-top: 3em;">

            <div class="row">
                <div class="col-md-4">
                    <div class="reportContainer">
                        <a href="${createLink(controller:'reports', action: 'overallReport')}" class="reportLink" target="_blank">
                            <div class="row">
                                <div class="col-md-2 reportIcon">
                                    <span class="glyphicon glyphicon-globe"></span>
                                </div>
                                <div class="col-md-10 reportTextContainer">
                                    <h4 class="reportTitle">Overall Status Report</h4>
                                    <div class="reportDescription">
                                        Provides a look into all things going on in this assessment tool,
                                        such as which organizations have which trustmarks, etc.
                                    </div>
                                </div>
                            </div>
                        </a>
                    </div>
                </div>

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


                <div class="col-md-4">
                    <div class="reportContainer">
                        <a href="${createLink(controller:'reports', action: 'tdReport')}" class="reportLink">
                            <div class="row">
                                <div class="col-md-2 reportIcon">
                                    <span class="glyphicon glyphicon-list-alt"></span>
                                </div>
                                <div class="col-md-10 reportTextContainer">
                                    <h4 class="reportTitle">Trustmark Definition Details</h4>
                                    <div class="reportDescription">
                                        Given a trustmark definition, this report will provide a look into what the status
                                        of each organization is for it.
                                    </div>
                                </div>
                            </div>
                        </a>
                    </div>
                </div>

            </div>

            <hr />

            <div class="row">
                <div class="col-md-4">
                    <div class="reportContainer">
                        <a href="${createLink(controller: 'reports', action: 'tipReport')}" class="reportLink">
                            <div class="row">
                                <div class="col-md-2 reportIcon">
                                    <span class="glyphicon glyphicon-list"></span>
                                </div>
                                <div class="col-md-10 reportTextContainer">
                                    <h4 class="reportTitle">TIP Report</h4>
                                    <div class="reportDescription">
                                        Given a Trust Interoperability Profile, this report will provide the status of
                                        organizations against the TIP, and outstanding assessments against this TIP.
                                    </div>
                                </div>
                            </div>
                        </a>
                    </div>
                </div>

                <div class="col-md-4">
                    <div class="reportContainer">
                        <a href="#" class="reportLink">
                            <div class="row">
                                <div class="col-md-2 reportIcon">
                                    <span class="glyphicon glyphicon-globe"></span>
                                </div>
                                <div class="col-md-10 reportTextContainer">
                                    <h4 class="reportTitle">Sample Report Title</h4>
                                    <div class="reportDescription">
                                        Sample Report Description.  This is not a real report, simply a place holder for
                                        a report which has yet to be developed.  Please don't click on this expecting it to work.
                                    </div>
                                </div>
                            </div>
                        </a>
                    </div>
                </div>

                <div class="col-md-4">
                    <div class="reportContainer">
                        <a href="#" class="reportLink">
                            <div class="row">
                                <div class="col-md-2 reportIcon">
                                    <span class="glyphicon glyphicon-globe"></span>
                                </div>
                                <div class="col-md-10 reportTextContainer">
                                    <h4 class="reportTitle">Sample Report Title</h4>
                                    <div class="reportDescription">
                                        Sample Report Description.  This is not a real report, simply a place holder for
                                        a report which has yet to be developed.  Please don't click on this expecting it to work.
                                    </div>
                                </div>
                            </div>
                        </a>
                    </div>
                </div>
            </div>


        </div>

        <div class="pageContent2" style="margin-top: 2em;">

            <a href="${createLink(controller:'reports', action: 'share')}" class="btn btn-primary">
                <span class="glyphicon glyphicon-share"></span>
                Share Reports
            </a>
        </div>

	</body>
</html>

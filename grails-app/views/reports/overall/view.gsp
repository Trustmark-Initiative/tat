<%@ page import="nstic.web.User; nstic.web.Role" defaultCodec="none"  %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="report-base"/>
    <title>Overall Report</title>
    <style type="text/css">
        .even {
            background-color: #EEEEEE;
        }
        .orgRow {
            padding: 1em;
        }

        .tdRow {
            padding: 1em;
        }
    </style>
</head>
<body>


    <div class="reportContent" style="margin-top: 25px;">
        <h1>Overall Report <small>Summary of Every Assessment & Org</small></h1>
        <div>
            Generated <g:formatDate date="${Calendar.getInstance().getTime()}" format="MM/dd/yyyy" />,
            By <sec:authentication property="principal.username" /> <br/>
            Including Assessment Data from <g:formatDate date="${startDate}" format="MM/dd/yyyy" />
            to <g:formatDate date="${endDate}" format="MM/dd/yyyy" />, resulting in ${assessmentStats['overallCount']} assessments (of ${nstic.web.assessment.Assessment.count()}).
        </div>

        <div style="margin-top: 1em; text-align: center;">
            <img src="${charts['statusChart'].toURLForHTML()}" />
        </div>


        <div style="margin-top: 25px;">
            <h3 style="margin-bottom: 1em;">Status by Organization</h3>
            <g:each in="${organizations}" var="org" status="orgStatus">
                <div class="orgRow ${orgStatus % 2 == 0 ? 'even' : 'odd'}">
                    <h4>${org.name}</h4>
                    <div style="margin-left: 1em;">
                        Number of Assessments: ${orgAssessmentMap[org].size() ?: 0} <br/>
                        Granted Trustmarks: ${orgTrustmarkMap[org].size()}
                    </div>
                </div>
            </g:each>
        </div>

        <hr style="margin-top: 1em; margin-bottom: 1em;" />

        <div style="margin-top: 1em;">
            <h3 style="margin-bottom: 1em;">Status by Trustmark Definition</h3>
            <g:each in="${trustmarkDefinitions}" var="td" status="tdStatus">
                <div class="tdRow ${tdStatus % 2 == 0 ? 'even' : 'odd'}">
                    <h4>${td.name}, v${td.tdVersion}</h4>
                    <div style="margin-left: 1em;">
                        Number of Trustmarks: ${tdTrustmarkMap[td].size()} <br/>
                        Number of Assessments: ${tdAssessmentMap[td].size()}
                    </div>
                </div>
            </g:each>
             %{--<table class="table">--}%
                %{--<tr style="border-bottom: 3px solid black;">--}%
                    %{--<th style="max-width: 40%; overflow: hidden;">&nbsp;</th>--}%
                    %{--<g:each in="${organizations}" var="org">--}%
                        %{--<th title="${org.name}">${org.identifier}</th>--}%
                    %{--</g:each>--}%
                %{--</tr>--}%
                %{--<g:each in="${trustmarkDefinitions}" var="td">--}%
                    %{--<tr>--}%
                        %{--<th style="max-width: 40%; overflow: hidden;">${td.name}</th>--}%
                        %{--<g:each in="${organizations}" var="org">--}%
                            %{--<td>--}%
                                %{--<g:if test="${orgTrustmarkMap.containsKey(org.id + "_" + td.id)}">--}%
                                    %{--<g:set var="assList" value="${orgTrustmarkMap.get(org.id + "_" + td.id)}" />--}%
                                    %{--<g:set var="assessment" value="${assList.get(0)}" />--}%

                                    %{--<tmpl:/templates/displayAssessmentStatusIcon assessmentList="${assList}" />--}%

                                %{--</g:if><g:else>--}%
                                    %{--<span class="glyphicon glyphicon-minus"></span>--}%
                                %{--</g:else>--}%
                            %{--</td>--}%
                        %{--</g:each>--}%
                    %{--</tr>--}%
                %{--</g:each>--}%
            %{--</table>--}%
        </div>

    </div>


</body>
</html>

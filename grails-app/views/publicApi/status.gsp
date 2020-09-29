<%--
  Created by IntelliJ IDEA.
  User: jeh
  Date: 9/18/20
  Time: 10:29 AM
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>Trustmark Status Report</title>
</head>

<body>
<h1>Trustmark Status Report</h1>

<div class="pageContent">
    <div style="margin-bottom: 1em;">
        <table>
            <tr>
                <td><b>Reference:&nbsp;</b></td>
                <td><a href="${trustmarkStatusReport.trustmarkReference.toString()}" target="_blank">${trustmarkStatusReport.trustmarkReference.toString()}</a></td>
            </tr>
            <tr>
                <td><b>Status:</b></td>
                <td>${trustmarkStatusReport.status}</td>
            </tr>
            <tr>
                <td><b>Date:</b></td>
                <td>${trustmarkStatusReport.statusDateTime}</td>
            </tr>
        </table>
    </div>
</div>
</body>
</html>
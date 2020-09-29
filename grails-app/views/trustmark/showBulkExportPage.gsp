<%@ page import="nstic.web.User; nstic.web.Role" %>
<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>Export Trustmarks</title>
    <style type="text/css">

    </style>
</head>
<body>

    <h1>Export Trustmarks <small>(${trustmarkCountTotal} total)</small></h1>
    <div class="pageSubsection">
        This page allows you to select multiple trustmarks to export at a single time, compressed into a zip file.
        First, search for the trustmark you wish to export, and from the list returned select the trustmark to
        add to the export set.  Once your set is built the way you wish, simple click the export button to download
        a zip file full of trustmark XML files.
    </div>

    <div class="pageContent">
        <div class="row" style="margin-bottom: 2em;">
            <div class="col-md-12">
                <h3>Choose Trustmarks</h3>
                <table class="table table-striped table-bordered table-condensed">
                    <thead>
                        <tr>
                            <td style="vertical-align: bottom;">
                                <input name="selectAllCheckbox" id="selectAllCheckbox" type="checkbox" onClick="toggle(this)"/>
                                <label for="selectAllCheckbox">Select All</label>
                            </td>
                            <th>Identifier</th>
                            <th>Status</th>
                            <th>Expiration Date</th>
                            <th>Granted By</th>
                            <th>Trustmark Definition</th>
                            <th>Recipient Organization</th>
                        </tr>
                    </thead>
                    <tbody>
                        <g:if test="${trustmarks && trustmarks.size() > 0}">
                            <g:each in="${trustmarks}" var="trustmark">
                                <tr>
                                    <td><input class="trustmarkCheckbox" id="tm_${trustmark.id}" type="checkbox" /></td>
                                    <td>
                                        <g:link controller="trustmark" action="view" id="${trustmark.identifier}">
                                            ${trustmark.identifier}
                                        </g:link>
                                    </td>
                                    <td style="text-align: center;">
                                        <g:if test="${trustmark.status == nstic.web.assessment.TrustmarkStatus.OK}">
                                            <span style="color: darkgreen;" class="glyphicon glyphicon-ok-sign" title="Trustmark still valid"></span>
                                        </g:if>
                                        <g:if test="${trustmark.status == nstic.web.assessment.TrustmarkStatus.REVOKED}">
                                            <span style="color: darkred;" class="glyphicon glyphicon-remove-sign" title="Trustmark has been revoked."></span>
                                        </g:if>
                                        <g:if test="${trustmark.status == nstic.web.assessment.TrustmarkStatus.EXPIRED}">
                                            <span style="color: rgb(150, 150, 0);" class="glyphicon glyphicon-minus-sign" title="Trustmark has expired."></span>
                                        </g:if>
                                    </td>
                                    <td>
                                        <g:formatDate format="yyyy-MM-dd" date="${trustmark.expirationDateTime}" />
                                    </td>
                                    <td>
                                        ${trustmark.grantingUser.contactInformation.responder}
                                    </td>
                                    <td>
                                        ${trustmark.trustmarkDefinition.name}
                                    </td>
                                    <td>${trustmark.recipientOrganization.name}</td>
                                </tr>
                            </g:each>
                        </g:if>
                        <g:else>
                            <tr>
                                <td colspan="6"><em>There are no trustmarks.</em></td>
                            </tr>
                        </g:else>
                    </tbody>
                </table>
                <div class="row">
                    <div class="col-md-6">
                        <a href="javascript:addTrustmarks()" class="btn btn-primary">Add</a>
                    </div>
                    <div class="col-md-6" style="text-align: right">
                        <g:paginate total="${trustmarkCountTotal}" />
                    </div>
                </div>
            </div>
        </div>

        <div class="row" style="margin-bottom: 2em;">
            <div class="col-md-12">
                <h3>Export Set</h3>
                <div id="exportSetDisplay">
                    TODO Display trustmarks to export here.
                </div>
                <div style="margin-top: 0.5em;">
                    <a href="javascript:removeTrustmarks()" class="btn btn-danger">Delete</a>
                    <a href="${createLink(controller:'trustmark', action:'doBulkExport')}" target="_blank" class="btn btn-primary">Export</a>
                </div>

            </div>
        </div>

    </div>

    <script type="text/javascript">
        $(document).ready(function(){
            updateList(new Array());
        });

        function toggle(source) {
            $(".trustmarkCheckbox").prop('checked', source.checked);
        }

        function removeTrustmarks(){
            console.log("Finding trustmarks to remove...");
            var tmArrayOfStuffToRemove = new Array();
            $('.trustmarkExportCheckbox:checked').each(function(index, value){
                var htmlId = value.id;
                var tmId = htmlId.substring(3);
                console.log("Removing trustmark: "+tmId);
                tmArrayOfStuffToRemove.push(tmId);
            })

            // TODO Verify list contains entries...

            updateListRemove(tmArrayOfStuffToRemove);
        }//end addTrustmarks()

        function addTrustmarks(){
            console.log("Finding trustmarks...");
            var tmArrayOfStuffToAdd = new Array();
            $('.trustmarkCheckbox:checked').each(function(index, value){
                var htmlId = value.id;
                var tmId = htmlId.substring(3);
                console.log("Adding trustmark: "+tmId);
                tmArrayOfStuffToAdd.push(tmId);
            })

            // TODO Verify list contains entries...

            updateList(tmArrayOfStuffToAdd);
        }//end addTrustmarks()

        function displayLoading(){
            // TODO Display loading message...
            $('#exportSetDisplay').html('Loading...')
        }

        function updateList(tmArrayOfStuffToAdd){
            displayLoading();
            var addUrl = '${createLink(controller:'trustmark', action: 'addToExportList')}';
            $.post(addUrl, {trustmarkIds: tmArrayOfStuffToAdd}, function(html){
                $('#exportSetDisplay').html(html);
            }, 'html');
        }

        function updateListRemove(tmArrayOfStuffToRemove){
            displayLoading();
            var addUrl = '${createLink(controller:'trustmark', action: 'removeFromExportList')}';
            $.post(addUrl, {trustmarkIds: tmArrayOfStuffToRemove}, function(html){
                $('#exportSetDisplay').html(html);
            }, 'html');
        }

    </script>
</body>
</html>

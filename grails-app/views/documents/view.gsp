<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>View Document</title>

    <style type="text/css">

    </style>
</head>

<body>

<div class="row">
    <div class="col-md-9">
        <h1>View Document Record</h1>
    </div>
    <div class="col-md-3" style="text-align: right;">

    </div>
</div>

<div id="errorContainer">
    <g:if test="${flash.error}">
        <div class="alert alert-danger">${flash.error}</div>
    </g:if>
    <g:if test="${command?.hasErrors()}">
        <div class="alert alert-danger" style="margin-top: 2em;">
            <b>Cannot upload document due to the following errors:</b>
            <div>
                <ul>
                    <g:each in="${command.errors.allErrors}" var="error">
                        <li>
                            <g:message error="${error}" />
                        </li>
                    </g:each>
                </ul>
            </div>
        </div>
    </g:if>
</div>

<div class="pageContent">

    <!-- Document Info -->
    <div class="row" style="margin-top: 2em;">
        <div class="col-sm-6">
            <h4>Document Information</h4>
            <table class="infoTable table table-striped table-bordered table-condensed">
                <tr>
                    <th>Organization Name</th>
                    <td>${doc.organization.name}</td>
                </tr>
                <tr>
                    <th>File Name</th>
                    <td>${doc.filename}</td>
                </tr>
                <tr>
                    <th>Description</th>
                    <td>${doc.description}</td>
                </tr>
                <tr>
                    <th>Document Category</th>
                    <td>${doc.documentCategory}</td>
                </tr>
                <tr>
                    <th>Public</th>
                    <td>
                        <g:if test="${doc.publicDocument}">
                            <span class="glyphicon glyphicon-ok"></span>
                        </g:if>
                        <g:else>
                            <span class="glyphicon glyphicon-remove"></span>
                        </g:else>
                    </td>
                </tr>
            </table>
            <div style="margin-top: 2em;">
                <g:link controller="documents" action="edit" params="[id: doc.id]" class="btn btn-default">
                    <span class="glyphicon glyphicon-edit"></span>
                    Edit
                </g:link>
            </div>
        </div>
    </div>
</div>

<script type="text/javascript">

    var queryInProgress = false;

    var SELECTED_ORG = null;

    $(document).ready(function(){

        $("#organizationName").typeahead({
            source: orgSearchTypeaheadDoQuery,
            matcher: typeaheadMatchAllMatcher,
            highlighter: orgSearchTypeaheadHighlighter,
            sorter: typeaheadAcceptExistingSortSorter,
            updater: orgSearchTypeaheadUpdaterName
        });

        <g:if test="${command?.existingOrgId}">
        syncOrgEditing({name: '${command?.organizationName}'});
        </g:if>
        <g:else>
        clearOrganization();

        <g:if test="${command?.organizationName}">
        $('#organizationName').val('${command?.organizationName}')
        </g:if>
        </g:else>
    })

    function validateForm() {
        if( $('#binaryId1').val() === '-1' ){
            alert("You must select a file to upload.");
            return false;
        }

        if( $('#fileName1').val() === '' ){
            alert("Please enter a File Name");
            return false;
        }

        if( filenameExtension($('#fileName1').val()) != 'pdf' ){
            alert("Please enter a PDF File Name");
            return false;
        }

        return true;
    }

    function filenameExtension(filename) {
        var ext = filename.slice((filename.lastIndexOf(".") - 1 >>>0) + 2);
        return ext.toLowerCase();
    }

    function orgSearchTypeaheadDoQuery( query , process ){
        if( queryInProgress ){
            return;
        }
        queryInProgress = true;
        console.log("Executing org query: "+query);
        var typeaheadUrl = '${createLink(controller:'organization', action: 'typeahead')}?format=json&q='+encodeURI(query);
        $.get(typeaheadUrl, function(data){
            process(data);
            queryInProgress = false;
        }, 'json')
    }

    function typeaheadMatchAllMatcher(item){
        return true; // They are already matched from server
    }

    function orgSearchTypeaheadHighlighter(item){
        var html = "<b>" + item.name + "</b>";
        return html;
    }//end orgSearchTypeaheadHighlighter()

    function typeaheadAcceptExistingSortSorter(items){
        return items; // They are already sorted from server.
    }

    function orgSearchTypeaheadUpdaterName(item){
        SELECTED_ORG = item;
        setOrganizationEditing(item);
        return item.name;
    }

    function setOrganizationEditing(org){
        SELECTED_ORG = org;

        $('#organizationEditInfo').html('<em>Documents for organization: '+org.name+'</em> <a href=\"javascript:clearOrganization()\" class=\"btn btn-xs btn-default\">Clear</a>');

        $('#existingOrgId').val(org.id);
        $('#organizationName').val(org.name);
    }//end setOrganizationEditing

    function syncOrgEditing(orgCommand){
        var getOrgUrl = '${createLink(controller:'organization', action: 'view', id: command?.existingOrgId)}?format=json'
        $.get(getOrgUrl, function(org){
            SELECTED_ORG = org;
            setOrganizationEditing(org);
            $('#organizationName').val(orgCommand.name);
        }, 'json')
    }//end syncOrgEditing()

    function clearOrganization() {
        SELECTED_ORG = null;

        $('#organizationEditInfo').html('<em>Lookup organization</em>');

        $('#existingOrgId').val('');
        $('#organizationName').val('');
    }

    /**
     * Called by pluploader template code after file added (because we specified it on the templ inclusion.)
     */
    function setUploadedFilename(up, files){
        var firstFilename = files[0].name;
        console.log("Setting display name to file name["+firstFilename+"]...")
        $('#filename').val(firstFilename);
    }//end setUploadedFilename()
</script>

<tmpl:/templates/pluploadJavascript
        pluploadCounter="1"
        filesAddedCallback="setUploadedFilename"
        context="Add Document for Organization ${command?.organization?.id}" />

</body>
</html>

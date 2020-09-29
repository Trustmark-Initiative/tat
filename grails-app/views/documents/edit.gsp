<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>Edit Document</title>

    <style type="text/css">

    </style>
</head>

<body>

<div class="row">
    <div class="col-md-9">
        <h1>Edit Document Record</h1>
        <div class="pageSubsection">
            This page allows you to edit an existing document record for the organization '${editDocumentCommand?.organizationName}'.
        </div>
    </div>
    <div class="col-md-3" style="text-align: right;">

    </div>
</div>

<div id="errorContainer">
    <g:if test="${flash.error}">
        <div class="alert alert-danger">${flash.error}</div>
    </g:if>
    <g:if test="${editDocumentCommand?.hasErrors()}">
        <div class="alert alert-danger" style="margin-top: 2em;">
            <b>Cannot edit document due to the following errors:</b>
            <div>
                <ul>
                    <g:each in="${editDocumentCommand.errors.allErrors}" var="error">
                        <li>
                            <g:message error="${error}" />
                        </li>
                    </g:each>
                </ul>
            </div>
        </div>
    </g:if>
</div>

<div class="row" style="margin-top: 2em;">
    <div class="col-md-12">
        <g:form class="form-horizontal" name="uploadDocumentForm" method="POST" action="updateDocument">


            <fieldset>

                <input type="hidden" name="existingDocumentId" id="existingDocumentId" value="${editDocumentCommand?.existingDocumentId}" />

                <div class="form-group">
                    <div class="col-md-10 col-md-offset-2" id="organizationEditInfo">

                    </div>
                </div>

                <div class="form-group">
                    <label for="organizationName" class="col-sm-2 control-label">Organization Name</label>
                    <div class="col-sm-10">
                        <input id="organizationName" name="organizationName" class="form-control" type="text" placeholder="Organization Name" autocomplete="off" value="${editDocumentCommand?.organizationName}" />
                    </div>
                </div>
            </fieldset>


            <g:hasErrors bean="${editDocumentCommand}">
                <div style="margin-top: 2em;" class="alert alert-danger">
                    <g:renderErrors bean="${editDocumentCommand}" />
                </div>
            </g:hasErrors>

            <div class="pageContent">
                <form action="${createLink(controller:'documents', action:'updateDocument', id: editDocumentCommand?.existingDocumentId)}"
                      method="POST" class="form-horizontal" onsubmit="return validateForm()">

                    <g:hiddenField name="organization" id="organization" value="${editDocumentCommand?.organization?.id}" />
                    <g:hiddenField name="binaryObject" id="binaryId1" value="${editDocumentCommand?.binaryObject?.id ?: -1}" />
                    <g:hiddenField id="documentCategory" name="documentCategory" value="${editDocumentCommand?.documentCategory ?: ''}" />

                    <div class="form-group">
                        <label for="filename" class="col-sm-2 control-label">Document File</label>
                        <div class="col-sm-10">
                            <p id="fileUploadName" class="form-control-static">
                                <a href="#" id="fileUploadButton1" class="btn btn-default">
                                    <span class="glyphicon glyphicon-upload"></span>
                                    Upload
                                </a>
                                <span id="fileName1">Select a File...</span>
                                <div id="fileUploadStatus1"></div>
                            </p>
                        </div>
                    </div>

                    <div class="form-group">
                        <label for="filename" class="col-sm-2 control-label">Document Name</label>
                        <div class="col-sm-10">
                            <g:textField name="filename" id="filename" class="form-control" value="${editDocumentCommand?.filename}" />
                        </div>
                    </div>

                    <div class="form-group" >
                        <label for="description" class="col-sm-2 control-label">Description</label>
                        <div class="col-sm-10">
                            <g:textArea name="description" id="description" class="form-control" placeholder="" value="${editDocumentCommand?.description}" />
                        </div>
                    </div>

                    <div class="form-group">
                        <label for="documentCategory" class="col-sm-2 control-label">Document Category</label>
                        <div class="col-sm-10">
                            <div class="btn-group" style="margin: 0;">
                                <button type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown" aria-expanded="false">
                                    <span id="selectedDocumentCategoryTitle"><em>Select Document Category...</em></span> <span class="caret"></span>
                                </button>
                                <ul class="dropdown-menu" role="menu">
                                    <g:each in="${docCategoryList}" var="docCategoryFromList">
                                        <li>
                                            <a href="javascript:setDocumentCategory('${docCategoryFromList}')">
                                                <div style="font-weight: bold;">${docCategoryFromList}</div>
                                            </a>
                                        </li>
                                    </g:each>
                                </ul>
                            </div>
                        </div>
                        <script type="text/javascript">
                            function setDocumentCategory(docCategory) {
                                console.log("Setting document category to " + docCategory);
                                $('#selectedDocumentCategoryTitle').html(docCategory);
                                $('#documentCategory').val(docCategory);
                            }
                        </script>
                    </div>

                    <div class="form-group">
                        <div class="col-sm-offset-2 col-sm-10">
                            <button type="submit" class="btn btn-primary">
                                <span class="glyphicon glyphicon-save"></span>
                                Update
                            </button>
                            <a href="${createLink(controller:'documents', action: 'list', id: editDocumentCommand?.organization?.id)}" class="btn btn-default">Cancel</a>
                        </div>
                    </div>

                </form>
            </div>

        </g:form>
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

        <g:if test="${editDocumentCommand?.existingOrgId}">
        </g:if>
        <g:else>
        clearOrganization();

        <g:if test="${editDocumentCommand?.organizationName}">
        $('#organizationName').val('${editDocumentCommand?.organizationName}')
        </g:if>
        </g:else>

        <g:if test="${editDocumentCommand?.documentCategory}">
        setDocumentCategory('${editDocumentCommand?.documentCategory}')
        </g:if>

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

        return true;
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
        context="Add Document for Organization ${editDocumentCommand?.organization?.id}" />

</body>
</html>

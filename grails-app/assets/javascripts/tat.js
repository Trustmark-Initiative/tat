
// convert a pipe-delimited multi-parameter string to an HTML unordered list. If the input string contains one
// parameter or if the string does not contain the delimiter, it returns the original string
function multiParamsToUnorderedList(paramVal) {
    const PIPE_DELIMITER = "|";

    let html = "";
    const paramsArray =  paramVal.split(PIPE_DELIMITER);
    if (paramsArray.length > 1) {
        html = "<ul style='margin: 0;  margin-left: 1em; padding: 0;'>";
        paramsArray.forEach(param => {
            html += "<li>";
            html += param;
            html += "</li>";
        });
        html += "</li></ul>";
    } else {
        // original string
        html += paramVal;
    }

    return html;
}

function enableButton(buttonId) {
    $("#"+buttonId).prop("disabled", false);
}

function disableButton(buttonId) {
    $("#"+buttonId).prop("disabled", true);
}


let get = function(url, doSuccess, args)  {
    $.ajax({
        url: url,
        method: 'GET',
        data: args,
        dataType: 'json'
    }).done(function (data){
        doSuccess(data);
    }).fail(function (jqxhr, err){
        console.log('An unexpected error occurred '+err);
    });
}

let list = get;

let add = function(url, doSuccess, args)  {
    $.ajax({
        url: url,
        method: 'PUT',
        data: args,
        dataType: 'json'
    }).done(function (data){
        doSuccess(data);
    }).fail(function (jqxhr, err){
        console.log('An unexpected error occurred '+err);
    });
}

let remove = function(url, doSuccess, args)  {
    $.ajax({
        url: url,
        method: 'DELETE',
        data: args,
        dataType: 'json'
    }).done(function (data){
        doSuccess(data);
    }).fail(function (jqxhr, err){
        console.log('An unexpected error occurred '+err);
    });
}

let update = function(url, doSuccess, args)  {
    $.ajax({
        url: url,
        method: 'POST',
        data: args,
        dataType: 'json'
    }).done(function (data){
        doSuccess(data);
    }).fail(function (jqxhr, err){
        console.log('An unexpected error occurred '+err);
    });
}

/**
 * renders content into a standard dialog with a close X
 * @param target
 * @param content
 */
let renderDialogForm = function(target, content)  {
    let html = "<form class='form-horizontal'>";
    html += "<div class='full-width-form'>";
    html += "<a class='tm-margin tm-right' href=\"javascript:hideIt('"+target+"');\"><span class='glyphicon glyphicon-remove'></span></a><br>";
    html += content;
    html += "</div></form>";

    html += "<p><span style='color:red;'>*</span> - Indicates required field.</p>"

    document.getElementById(target).innerHTML = html;
    showIt(target);
}

/**
 * common pagination function
 * @param offset
 * @param totalCount
 * @param fnName
 * @returns {string}
 */
let renderPagination = function(offset, totalCount, fnName)  {
    if (totalCount > MAX_DISPLAY)  {
        return buildPagination(offset, MAX_DISPLAY, totalCount, fnName);
    }
    return "";
}

/**
 * checks the form contents for completeness
 * @param trustmarkRecipientIdentifier
 * @returns {boolean}
 */
let checkTrustmarkRecipientIdentifier = function(trustmarkRecipientIdentifier)  {

    if(trustmarkRecipientIdentifier == null || trustmarkRecipientIdentifier.length === 0) {
        setDangerStatus("<b>Trustmark recipient identifier cannot be blank.</b>");
        document.getElementById('trustmarkRecipientIdentifier').focus();
        return false;
    }

    return true;
}

let renderTrustmarkRecipientIdentifiersOffset = function(){};
/**
 * render the trustmark recipient identifiers in a tabular form
 * @param target
 * @param obj
 * @param data
 * @param offset
 */
let renderTrustmarkRecipientIdentifiers = function(target, obj, data, offset)  {
    let html = renderPagination(offset, data.records.length, 'renderTrustmarkRecipientIdentifiersOffset');
    html += "<table class='table table-condensed table-striped table-bordered'>";

    if(obj.editable)  {
        html += "<tr><td colspan='2' style='text-align: left'>";

        if (data.records.length === 1) {
            html += "<div style='text-align: left'><a id='plus-"+target+"' title='Add Trustmark Recipient Identifier'><span class='glyphicon glyphicon-plus'></span></a> / " +
                "<span title='Remove Checked Trustmark Recipient Identifier' class='glyphicon glyphicon-minus'></span></div>";
        } else {
            html += "<div style='text-align: left'><a id='plus-"+target+"' title='Add Trustmark Recipient Identifier'><span class='glyphicon glyphicon-plus'></span></a> / " +
                "<a id='minus-"+target+"' title='Remove Checked Trustmark Recipient Identifier'><span class='glyphicon glyphicon-minus'></span></a></div>";
        }

    } else {
        html += "<tr><td colspan='1' style='text-align: center'>";
    }

    if (data.records.length === 0)  {
        html += '<tr><td colspan="2"><em>There are no Trustmark Recipient Identifiers.</em></td></tr>';
    }  else {

        let idx = 0;
        data.records.forEach(o => {
            if(idx >= offset && idx < offset+MAX_DISPLAY) {
                html += obj.fnDraw(obj, o);
            }
            ++idx;
        });
    }
    html += "</table>";

    document.getElementById(target).innerHTML = html;
    if(obj.editable)  {
        document.getElementById('plus-'+target).onclick = obj.fnAdd;
        if (data.records.length > 1) {
            document.getElementById('minus-' + target).onclick = obj.fnRemove;
        }
    }
}

let drawTrustmarkRecipientIdentifier = function(obj, entry)  {
    let html = "<tr>";
    if(obj.editable) {
        if (entry.defaultTrustmarkRecipientIdentifier) {
            html += "<td><input type='checkbox' title='Default trustmark recipient identifiers cannot be deleted!' disabled id='edit-trustmarkRecipientIdentifier' class='edit-trustmarkRecipientIdentifier' value='" + entry.id + "'>" +
                "<a class='tm-right' href='javascript:getTrustmarkRecipientIdentifierDetails(" + entry.id + ");'><span class='glyphicon glyphicon-pencil'></span></a></td>";
        } else {
            html += "<td><input type='checkbox' id='edit-trustmarkRecipientIdentifier' class='edit-trustmarkRecipientIdentifier' value='" + entry.id + "'>" +
                "<a class='tm-right' href='javascript:getTrustmarkRecipientIdentifierDetails(" + entry.id + ");'><span class='glyphicon glyphicon-pencil'></span></a></td>";
        }
    }

    html += "<td>" + entry.uri + "</td>";
    html += "</tr>";

    return html;
}

// 277

/**
 * gets checked ids by class name, puts in a list and applies the function argument to them
 * @param str
 * @param fn
 */
let getCheckedIds = function(str, fn) {
    let elements = document.getElementsByClassName(str);
    let idList = "";
    for( let i=0; i < elements.length; ++i)  {
        if(elements[i].checked === true)  {
            idList += elements[i].value+":";
        }
    }
    fn(idList);
}

/**
 * renders a status message into a container div that fades out after 3 seconds
 * @param target: container div for message
 * @param data: content of the message
 */
let setStatusMessage = function(target, data) {
    let html = "";
    if (!isEmtpy(data.status['SUCCESS'])) {
        html += "<div id='status-message' class='alert alert-success' class='glyphicon glyphicon-ok-circle'>" + data.status['SUCCESS'] + "</div>";
    }

    if (!isEmtpy(data.status['WARNING'])) {
        html += "<div id='status-message' class='alert alert-warning' class='glyphicon glyphicon-warning-sign'>" + data.status['WARNING'] + "</div>";
    }

    if (!isEmtpy(data.status['ERROR'])) {
        html += "<div id='status-message' class='alert alert-danger' class='glyphicon glyphicon-exclamation-sign'>" + data.status['ERROR'] + "</div>";
    }

    if (!isEmtpy(html)) {
        $('#'+target).html(html);
        $('#'+target).fadeTo(200, 1);
        $('#'+target).delay(3000).fadeTo(300, 0);
    }
}

let isEmtpy = function(str) {
    return (!str || str.length === 0);
}

let renderTrustmarkRecipientIdentifiersForm = function(target, preFn, fn, trustmarkRecipientIdentifier) {

    let html = "";

    html += "<div class='form-group'>";
    html += "<label for='trustmarkRecipientIdentifier' class='col-sm-2 control-label tm-margin'>Identifier</label>";
    html += "<input id='trustmarkRecipientIdentifier' type='text' class='col-sm-10 form-control tm-margin' style='width: 70%;' placeholder='Enter the Trustmark Recipient Identifier URI'/><span style='color:red;'>*</span><br>";
    html += "</div>";

    // A trustmark recipient identifier id of zero means add a new trustmark recipient identifier
    let addOrSave = "Add";
    if(trustmarkRecipientIdentifier.id !== 0) {
        addOrSave = "Save";
    }

    html += "<div class='form-group'>";
    html += "<div class='col-sm-offset-2 col-sm-10'>";
    html += "<button id='trustmarkRecipientIdentifierOk' type='button' class='btn btn-info tm-margin'>" + addOrSave + "</button>";
    html += "</div>";
    html += "</div>";

    renderDialogForm(target, html);
    document.getElementById('trustmarkRecipientIdentifierOk').onclick = fn;
    document.getElementById('trustmarkRecipientIdentifier').focus();

    preFn(trustmarkRecipientIdentifier);
}

/**
 * hide the passed in div
 * @param target
 */
let showIt = function(target)  {
    if(target.startsWith('#'))  {
        document.getElementById(target.substring(1)).style.display = 'block';
    } else {
        document.getElementById(target).style.display = 'block';
    }
}

/**
 * hides or displays the target
 * @param target
 * @returns {boolean}
 */
let toggleIt = function(target)  {
    if(target.startsWith('#')) {
        target = document.getElementById(target.substring(1));
    }
    if(document.getElementById(target).style.display === 'none')  {
        document.getElementById(target).style.display = 'block';
    } else {
        document.getElementById(target).style.display = 'none';
    }
    return false;
}

/**
 * hide the passed in div
 * @param target
 */
let hideIt = function(target)  {

    // make sure the target is visible since some elements might be
    // filtered out based on logged-in user and/or roles

    if(target.startsWith('#'))  {
        if (document.getElementById(target.substring(1)) != null) {
            document.getElementById(target.substring(1)).style.display = 'none';
        }
    } else {
        if (document.getElementById(target) != null) {
            document.getElementById(target).style.display = 'none';
        }
    }
}
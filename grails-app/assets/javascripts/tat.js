
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

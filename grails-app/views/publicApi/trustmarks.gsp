<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>Trustmarks Public Search</title>
    <script type="text/javascript">
        $(function() {
            $('#fromDt').datepicker();
            $('#endDt').datepicker();
        });
        let MAX_DISPLAY = 10;
        let trustmarks = {};

        function performSearch(queryString, recip, from, to, maxResults)  {
            $('#resultsContainer').html('${raw(asset.image(src: 'spinner.gif'))} Searching...');
            if( (!queryString || queryString.trim() === '')
                && (!recip || recip.trim() === '')
                && (!from || from.trim() === '')
                && (!to || to.trim() === '')
            ) {
                $('#resultsContainer').html('<div class="alert alert-warning">Please enter some search text.</div>');
                return;
            }
            MAX_DISPLAY = parseInt(maxResults);

            $.ajax({
                url: '${createLink(controller: 'publicApi', action: 'findMany')}',
                method: 'GET',
                type: 'GET',
                data: {
                    timestamp: new Date().getTime(),
                    td: queryString,
                    recipient: recip,
                    fromDate: from,
                    toDate: to,
                    max: maxResults
                },
                dataType: 'json',
                format: 'json',
                success: function(data){
                    trustmarks = data;
                    setResultsDiv(trustmarks.length, queryString);
                    renderTrustmarks(0);
                },
                error: function(){
                    $('#resultsContainer').html('<div class="alert alert-danger">An unexpected error occurred communicating with the server.</div>');
                }
            });
        }

        function renderTrustmarks(offset)  {
            let html = "";
            if (trustmarks.length > MAX_DISPLAY)  {
                html += buildPagination(offset, MAX_DISPLAY, trustmarks.length, 'renderTrustmarks');
            }
            html += "<table class=\"table table-condensed table-striped table-bordered\"><thead><tr><th style=\"width: auto;\">Trustmark</th><th style=\"width: auto;\">Trustmark Definition</th><th style=\"width: auto;\">Organization</th><th style=\"width:auto;\">Status</th></tr></thead>";
            html += "<tbody>";
            if (trustmarks.length == 0)  {
                html += '<tr><td colspan="4"><em>There are no trustmarks.</em></td></tr>';
                html += "</tbody></table>";
            }  else {
                let i = 0;

                trustmarks.forEach(t => {
                    if(i >= offset && i < MAX_DISPLAY+offset)   {
                        html += "<tr><td><a href=\""+t.identifierURL +"\" target=\"_blank\">" + t.name + "</a></td>";
                        html += "<td><a href=\""+t.trustmarkDefinitionURL+"\" target=\"_blank\">TD</a></td>";
                        html += "<td><a href=\""+t.organizationUri+"\" target=\"_blank\">"+t.recipient+"</a></td>";
                        html += "<td><a href=\""+t.statusURL +"\" target=\"_blank\">"+t.trustmarkStatus+"</a></td>";
                        html += "</tr>";
                    }
                    ++i;
                });
                html += "</tbody></table>";
            }
            $('#resultsContainer').html(html);
        }

        function setResultsDiv(count, qstr)  {
            $('.pageSubsection').html('Total of '+ count +' results for the query <em>'+ qstr +'</em>.');
        }
    </script>
</head>
<body>
<h1>Trustmark Search</h1>
<div class="pageSubsection"></div>

<div class="pageContent">
    <div class="searchFormContainer" style="margin-bottom: 1em;">
        <form class="form-inline">
            <div class="form-group">
                <input style="width:500px;" name="q" id="qstr" type="text" class="form-control" placeholder="Trustmark Definition" />
                <select name="max" class="form-control" id="maxRes">
                    <option value="10" ${params.max == "10" ? 'selected=selected' : ''}>10 per page</option>
                    <option value="20" ${params.max == "20" ? 'selected=selected' : ''}>20 per page</option>
                    <option value="50" ${params.max == "50" ? 'selected=selected' : ''}>50 per page</option>
                </select>
            </div><br>
            <div class="form-group">
                <input style="width:500px;" name="recipient" id="recipient" type="text" class="form-control" placeholder="Recipient" /><br>
            </div><br>
            <div class="form-group">
                <input style="width:150px;" name="fromDt" id="fromDt" type="text" class="form-control" placeholder="Start mm-dd-yyyy" />
                <input style="width:150px;" name="endDt" id="endDt" type="text" class="form-control" placeholder="End mm-dd-yyyy" />
            </div>
            <button type="button" class="btn btn-default" onClick="performSearch(getElementById('qstr').value, getElementById('recipient').value, getElementById('fromDt').value, getElementById('endDt').value, getElementById('maxRes').value);">Search</button>
        </form>
    </div>
    <div id="resultsContainer"></div>
</div>
</body>
</html>
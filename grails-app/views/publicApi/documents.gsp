<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>Public Documents</title>
    <script type="text/javascript">
        let documents = {};

        $(function() {
            performSearch('ALL', 50);
        });

        function performSearch(queryString, maxResults)  {
            $('#resultsContainer').html('${raw(asset.image(src: 'spinner.gif'))} Searching...');
            if( !queryString || queryString.trim() === '' ){
                $('#resultsContainer').html('<div class="alert alert-warning">Please enter some search text.</div>');
                return;
            }
            MAX_DISPLAY = parseInt(maxResults);

            $.ajax({
                url: '${createLink(controller:'publicApi', action: 'findDocs')}',
                method: 'GET',
                type: 'GET',
                data: {
                    timestamp: new Date().getTime(),
                    id: queryString,
                    max: maxResults
                },
                dataType: 'json',
                format: 'json',
                success: function(data){
                    documents = data;
                    setResultsDiv(documents.length, queryString);
                    renderDocuments(0);
                },
                error: function(){
                    $('#resultsContainer').html('<div class="alert alert-danger">An unexpected error occurred communicating with the server.</div>');
                }
            });
        }

        function renderDocuments(offset)  {
            let html = "";
            if (documents.length > MAX_DISPLAY)  {
                html += buildPagination(offset, MAX_DISPLAY, documents.length, 'renderDocuments');
            }
            html += "<table class=\"table table-condensed table-striped table-bordered\"><thead><tr><th style=\"width: auto;\">Name</th><th style=\"width: auto;\">Create Date</th><th style=\"width: auto;\">Description</th><th style=\"width: auto;\">Category</th></tr></thead>";
            html += "<tbody>";
            if (documents.length == 0)  {
                html += '<tr><td colspan="4"><em>There are no documents.</em></td></tr>';
                html += "</tbody></table>";
            }  else {
                let i = 0;

                documents.forEach(d => {
                    if(i >= offset && i < MAX_DISPLAY+offset)   {
                        let ddate = d.dateCreated.split('T')[0] + ' ' + d.dateCreated.split('T')[1].split(':')[0] + ':'+d.dateCreated.split('T')[1].split(':')[1];
                        html += "<tr><td><a href=\""+d.url +"\" target=\"_blank\">" + d.filename + "</span></a></td>";
                        html += "<td>"+ddate+"</td>";
                        html += "<td>"+ d.description + "</td>";
                        html += "<td>"+ d.documentCategory +"</td></tr>";
                    }
                    ++i;
                });
                html += "</tbody></table>";
            }
            $('#resultsContainer').html(html);
        }

        function setResultsDiv(count, qstr)  {
            $('.pageSubsection').html('Total of '+ count +' public documents');
        }

    </script>
</head>
<body>
<h1>Public Documents</h1>
<div class="pageSubsection"></div>

<div class="pageContent">
    <div id="resultsContainer"></div>
</div>
</body>
</html>
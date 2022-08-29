<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>Home</title>

    <script type="text/javascript">
        let assessments = {};

        $(document).ready(function(){
            console.log('Initial search for: '+'${queryString}');
            performSearch('${queryString}', 10);
        });

        function performSearch(queryString, maxResults)  {
            $('#resultsContainer').html('${raw(asset.image(src: 'spinner.gif'))} Searching...');
            if( !queryString || queryString.trim() === '' ){
                $('#resultsContainer').html('<div class="alert alert-warning">Please enter some search text.</div>');
                return;
            }
            MAX_DISPLAY = parseInt(maxResults);

            $.ajax({
                url: '${createLink(controller:'assessmentSearch', action: 'search')}',
                method: 'GET',
                type: 'GET',
                data: {
                    timestamp: new Date().getTime(),
                    q: queryString,
                    max: maxResults
                },
                dataType: 'json',
                format: 'json',
                success: function(data){
                    assessments = data;
                    setResultsDiv(assessments.length, queryString);
                    renderAssessments(0);
                },
                error: function(){
                    $('#resultsContainer').html('<div class="alert alert-danger">An unexpected error occurred communicating with the server.</div>');
                }
            });
        }

        function renderAssessments(offset)  {
            let html = "";
            if (assessments.length > MAX_DISPLAY)  {
                html += buildPagination(offset, MAX_DISPLAY, assessments.length, 'renderAssessments');
            }
            html += "<table class=\"table table-condensed table-striped table-bordered\"><thead><tr><th style=\"width: auto;\">Name</th><th style=\"width: auto;\">Change Date</th><th style=\"width: auto;\">Latest Entry Title</th><th style=\"width: auto;\">Organization</th></tr></thead>";
            html += "<tbody>";
            if (assessments.length == 0)  {
                html += '<tr><td colspan="4"><em>There are no assessments.</em></td></tr>';
                html += "</tbody></table>";
            }  else {
                let i = 0;

                assessments.forEach(a => {
                    if(i >= offset && i < MAX_DISPLAY+offset)   {
                        let alink = '${createLink(controller:'assessment', action: 'view')}'+'/'+a.id;
                        let adate = a.mostRecentEntry.split('T')[0] + ' ' + a.mostRecentEntry.split('T')[1].split(':')[0] + ':'+a.mostRecentEntry.split('T')[1].split(':')[1];
                        html += "<tr><td><a href=\""+alink +"\" target=\"_blank\">" + a.assessmentName + "</span></a></td>";
                        html += "<td>"+adate+"</td>";
                        html += "<td>"+ a.title + "</td>";
                        html += "<td>"+ a.createdBy.organization.name +"</td></tr>";
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

    <h1>Assessment Search Results</h1>
    <div class="pageSubsection">
        Total of ${resultCount} results for the query <em>${queryString}</em>.
    </div>

    <div class="pageContent">
        <div class="searchFormContainer" style="margin-bottom: 1em;">
            <form class="form-inline">
                <div class="form-group">
                    <input style="width:500px;" name="q" id="qstr" type="text" class="form-control" placeholder="Search Phrase" />
                </div>
                <div class="form-group">
                    <select name="max" class="form-control" id="maxRes">
                        <option value="10" ${params.max == "10" ? 'selected=selected' : ''}>10 per page</option>
                        <option value="20" ${params.max == "20" ? 'selected=selected' : ''}>20 per page</option>
                        <option value="50" ${params.max == "50" ? 'selected=selected' : ''}>50 per page</option>
                    </select>
                </div>
                <button type="button" class="btn btn-default" onClick="performSearch(getElementById('qstr').value, getElementById('maxRes').value);">Search</button>
            </form>
        </div>
        <div id="resultsContainer"></div>
    </div>
</body>
</html>

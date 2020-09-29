<%@ page import="nstic.web.assessment.AssessmentStepResult; nstic.web.User; nstic.web.Role" %>
<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main"/>
		<title>Substep To Excel Generation</title>

        <style type="text/css">

        </style>
	</head>
	<body>
        %{--<ol class="breadcrumb">--}%
            %{--<li class="active">Home</li>--}%
        %{--</ol>--}%

        <h1>Substep to Excel Generation </h1>
        <div class="pageSubsection">
            This page allows you to generate a "Spreadsheet" for editing <i>MANY</i>
            trustmark definitions as one spreadsheet that will output many more TDs.
            The spreadsheet will represent each substep as a "line" along with assessment
            steps with no substeps.  The idea is that each line will output into it's own
            spreadsheet.
        </div>

        <div id="mainPageContent" class="pageContent" style="margin-top: 3em;">

            <asset:image src="spinner.gif" /> Loading Step Data...

        </div>

    <script type="text/javascript">
        $(document).ready(function(){
            console.log("Initializing page...");

            $.ajax({
                method: 'GET',
                url: '${createLink(controller:'substepToExcel', action: 'substepListing')}',
                data: {
                    format: 'json',
                    now: new Date().getMilliseconds() + ""
                },
                dataType: 'json',
                success: function(data){
                    console.log("Successfully received json: "+JSON.stringify(data, null, 2));
                    var tdListingHtml = buildTdListing(data);
                    $('#mainPageContent').html(tdListingHtml);
                    updateCheckboxFeedback();
                },
                error: function(jqXHR, textStatus, errorThrown){
                    console.log("Error receiving data["+textStatus+"]: "+errorThrown);
                    $('#mainPageContent').html('<div class="alert alert-danger" style="width: 600px;">An unexpected error has occurred, please refresh the page and try again. <br/><br /><b>Text Status:</b>'+textStatus+'<br/><b>Error Thrown:</b>'+errorThrown+'</div>')
                }//end error()
            });

        });


        function buildTdListing(data) {
            var html = '';

            html += "<div>\n";

            if( data.status && data.status == "SUCCESS" ) {
                html += "   <h3>Trustmark Definitions to Export (" + data.tdCount + ")</h3>\n";
                var numericColWidth = "125";
                if( data.tdList && data.tdList.length && data.tdList.length > 0 ) {
                    html += '    <table class="table table-striped table-bordered table-condensed">\n';
                    html += '        <thead><tr>\n';
                    html += '            <th style="text-align: center; width: 20px;"><input id="selectAllCheckbox" type="checkbox" onclick="selectAllChecked()" /></th>\n';
                    html += '            <th style="text-align: center; width: '+numericColWidth+'px;">Step Count</th>\n';
                    html += '            <th style="text-align: center; width: '+numericColWidth+'px;">Substep Count</th>\n';
                    html += '            <th style="text-align: center; width: '+numericColWidth+'px;">Output Count</th>\n';
                    html += '            <th style="text-align: left; width: auto;">Trustmark Definition</th>\n';
                    html += '        </tr></thead>\n';
                    html += '        <tbody>\n';
                    for (var tdIndex = 0; tdIndex < data.tdList.length; tdIndex++) {
                        var td = data.tdList[tdIndex];
                        html += '<tr><!-- Start Trustmark Definition '+td.id+' -->\n';

                        var assStepCount = td.assessmentSteps.length;
                        var substepCount = 0;
                        var outputCount = 0;
                        for( var assStepIndex = 0; assStepIndex < td.assessmentSteps.length; assStepIndex++ ){
                            var assStep = td.assessmentSteps[assStepIndex];
                            if( assStep.substeps && assStep.substeps.length > 0 ) {
                                substepCount += assStep.substeps.length;
                                outputCount += assStep.substeps.length;
                            }else{
                                outputCount++;
                            }
                        }//end for()


                        html += '    <td style="text-align: center; width: 20px;"><input id="tdCheckbox'+td.databaseId+'" class="tdCheckbox" type="checkbox" onclick="updateCheckboxFeedback()" /></td>\n';
                        html += '    <td style="text-align: center; width: '+numericColWidth+'px;">'+assStepCount+'</td>\n';
                        html += '    <td style="text-align: center; width: '+numericColWidth+'px;">'+substepCount+'</td>\n';
                        html += '    <td style="text-align: center; width: '+numericColWidth+'px;">'+outputCount+'</td>\n';
                        html += '    <td style="text-align: left; width: auto;">'+td.name+', version '+td.version+'</td>\n';

                        html += '</tr><!-- End Trustmark Definition '+td.id+' -->\n\n\n';
                    }
                    html += '        </tbody>\n';
                    html += '    </table>\n';


                    html += '<div style="margin-top: 2em;"><a id="submitButton" href="javascript:doGenerate()" class="btn btn-primary">Generate Excel File</a></div>\n'

                }else{
                    // TODO Display some empty message?
                }

            }else{
                html += '<div class="alert alert-danger" style="width: 600px;">The server has returned an invalid or error response.  Please refresh the page and try again.</div>'
            }
            html += "</div>\n";

            return html;
        }


        function selectAllChecked(){
            if( $('#selectAllCheckbox').is(":checked") ){
                console.log("Checking all td checkboxes...");
                $('.tdCheckbox').prop('checked', true);
            }else {
                console.log("Unchecking all td checkboxes...");
                $('.tdCheckbox').prop('checked', false);
            }
            updateCheckboxFeedback();
        }//end selectAllChecked()

        function updateCheckboxFeedback() {
            var allChecked = true;
            var anyChecked = false;

            $('.tdCheckbox').each( function(index){
                var tdCheckbox = $(this);
//                console.log("Checking TD Checkbox["+tdCheckbox.attr('id')+"]: "+tdCheckbox.is(":checked"));
                if( tdCheckbox.is(":checked") ){
                    anyChecked = true;
                }else{
                    allChecked = false;
                }
            });

            if( anyChecked ){
                $('#submitButton').attr('class', 'btn btn-primary enabled');
            }else{
                $('#submitButton').attr('class', 'btn btn-primary disabled');
            }

            if( allChecked && !$('#selectAllCheckbox').is(":checked") ){
                $('#selectAllCheckbox').prop('checked', true);
            }else if( !allChecked && $('#selectAllCheckbox').is(":checked") ){
                $('#selectAllCheckbox').prop('checked', false);
            }

        }//end updateCheckboxFeedback()

        /**
         *  Causes the Excel Spreadsheet to generate on the server, based on the selected TDs on this side.
         */
        function doGenerate() {
            var idsToGen = new Array();

            $('.tdCheckbox').each( function(index){
                var tdCheckbox = $(this);
                var currentId = tdCheckbox.attr('id').replace("tdCheckbox", "");
                if( tdCheckbox.is(":checked") ){
                    console.log(currentId+" is checked");
                    idsToGen.push(currentId);
                }
            });

            if( idsToGen.length > 0 ){
                $.ajax({
                    url: '${createLink(controller:'substepToExcel', action: 'excelGenerate')}',
                    method: 'POST',
                    data: {
                        format: 'json',
                        ids: idsToGen,
                        now: new Date().getMilliseconds() + ""
                    },
                    dataType: 'json',
                    success: function(data){
                        console.log("Successfully received json: "+JSON.stringify(data, null, 2));
                        window.location = '${createLink(controller:'binary', action: 'view', id: '__REPLACEME__')}'.replace('__REPLACEME__', data.binaryId);
                    },
                    error: function(jqXHR, textStatus, errorThrown){
                        console.log("Error receiving data["+textStatus+"]: "+errorThrown);
                    }//end error()

                })
            }
        }//end doGenerate()

    </script>

	</body>
</html>

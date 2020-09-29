<%@ page import="nstic.web.assessment.AssessmentStepResult; nstic.web.User; nstic.web.Role" %>
<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main"/>
		<title>Substep Resolution</title>

        <style type="text/css">
            .assessmentSubStepIdHeader {
                font-size: 70%;
                font-color: #666;
            }
            .assessmentSubStepIdName {
                font-size: 100%;
            }

            .assessmentStepName {
                font-size: 120%;
                font-weight: bold;
            }
            
            .substepComplete {
                text-decoration: line-through;
            }
        </style>
	</head>
	<body>
        %{--<ol class="breadcrumb">--}%
            %{--<li class="active">Home</li>--}%
        %{--</ol>--}%

        <h1> <span class="glyphicon glyphicon-flash"></span> Substep Resolution </h1>
        <div class="pageSubsection">
            Nam facilisis, quam eget faucibus scelerisque, nulla justo auctor lacus, eu laoreet nibh odio sed eros. Sed aliquam mattis ipsum vitae pellentesque. Quisque congue ut nunc quis consequat. Vivamus accumsan accumsan varius. Fusce porttitor consequat risus, aliquam tempus lectus volutpat vitae. Morbi consequat nisi erat, vitae elementum est dapibus gravida. In ut eleifend risus. Mauris tristique quam eget tortor pharetra ullamcorper. Nulla ac massa pellentesque, blandit dolor ac, fermentum ante.
        </div>

        <div id="messageContainer">
            <g:if test="${flash.message}">
                <div style="margin-top: 2em;" class="alert alert-info">
                    ${flash.message}
                </div>
            </g:if>
        </div>

        <div class="pageContent" style="margin-top: 3em;">

            <div class="row">
                <!-- Substep List -->
                <div class="col-md-3">
                    <h4>
                        Substep List (<span id="substepCount">?</span>)
                        &nbsp;
                        <a href="javascript:loadSubsteps()" title="Reload the substeps list">
                            <span class="glyphicon glyphicon-refresh"></span>
                        </a>
                    </h4>
                    <div id="leftSideHtmlContainer">
                        <div style="margin-top: 1em;">
                            <asset:image src="spinner.gif" /> Loading...
                        </div>
                    </div>
                </div>


                <!-- Substep -->
                <div class="col-md-9">
                    <h4>Substep Data</h4>
                    <div id="rightSideHtmlContainer">
                        <div style="margin-top: 1em;">
                            <div class="text-danger">There is no assessment substep selected.</div>
                        </div>
                    </div>
                    <div style="margin-top: 2em; text-align: right;">
                        <a href="javascript:nextButtonPress()" class="btn btn-default">
                            Next &raquo;
                        </a>
                    </div>
                </div>
            </div>

        </div>

    <script id="assessmentInfoHeaderTemplate" type="text/x-handlebars-template">​
        <div id="assessmentInfoHeader">
            <h3>
                <a href="${createLink(uri:'/')}assessments/{{assessment.id}}">Assessment #{{assessment.id}}</a>,
                Step #{{stepData.step.stepNumber}}, Substep {{substep.id}}</h3>
            <div style="margin: 0;">
                <table class="table">
                    <tr>
                        <td style="width: 250px;"><b>Assessed Organization</b>:</td><td>{{assessment.assessedOrganization.name}}</td>
                    </tr><tr>
                        <td style="width: 250px;"><b>Assessed Trustmark Definition</b>:</td><td>{{assessment.trustmarkDefinition.name}}</td>
                    </tr>
                </table>
            </div>
        </div>
    ​</script>

    <script id="assessmentStepDataHeaderTemplate" type="text/x-handlebars-template">​
        <div id="assessmentStepDataContainer">
            <div class="assessmentStepName">Step #{{stepData.step.stepNumber}}: {{stepData.step.name}}</div>
            <div class="assessmentStepDesc text-muted">
                {{{stepData.step.description}}}
            </div>
            <div id="assessmentStepResultStatusContainer"></div>
            <div>
                <span class="commentHeader">Assessor Comment</span>:
                {{#if stepData.comment}}
                    {{{stepData.comment}}}
                {{else}}
                    <em>There is no comment.</em>
                {{/if}}
            </div>
        </div>
    ​</script>

    <script id="assessmentSubStepContainerHeaderTemplate" type="text/x-handlebars-template">​
        <div id="assessmentSubStepContainer">
            <div class="assessmentStepName">Substep {{substep.stepNumber}}: {{substep.name}}</div>
            <div class="assessmentStepDesc text-muted">
                {{{substep.description}}}
            </div>
        </div>

        <h4 style="margin-top: 1em;">Substep Results</h4>
        <form class="form-horizontal">
            <div class="form-group">
                <label for="result" class="col-sm-2 control-label">
                    Result
                </label>
                <div class="col-sm-10">
                    <input type="hidden" name="result" id="result" value="Not_Known" />
                    <ul class="nav nav-pills">
                        <li class="resultPill active" id="Not_Known_li">
                            <a href="javascript:setResult('Not_Known')">
                                <assess:assessmentStepResult result="${AssessmentStepResult.Not_Known}"/>
                                Unknown
                            </a>
                        </li>
                        <li class="resultPill" id="Satisfied_li">
                            <a href="javascript:setResult('Satisfied')">
                                <assess:assessmentStepResult result="${AssessmentStepResult.Satisfied}"/>
                                Satisfied
                            </a>
                        </li>
                        <li class="resultPill" id="Not_Satisfied_li">
                            <a href="javascript:setResult('Not_Satisfied')">
                                <assess:assessmentStepResult result="${AssessmentStepResult.Not_Satisfied}"/>
                                Not Satisfied
                            </a>
                        </li>
                        <li class="resultPill" id="Not_Applicable_li">
                            <a href="javascript:setResult('Not_Applicable')">
                                <assess:assessmentStepResult result="${AssessmentStepResult.Not_Applicable}"/>
                                Not Applicable
                            </a>
                        </li>
                    </ul>

                </div>
            </div>

            <div class="form-group">
                <label for="assessorSubstepComment" class="col-sm-2 control-label">
                    Assessor Comment
                </label>
                <div class="col-sm-10">
                    <textarea name="assessorSubstepComment" id="assessorSubstepComment" style="min-height: 150px;" class="form-control"></textarea>
                    <span class="help-block">The assessor's findings for this substep.</span>
                </div>
            </div>
        </form>
    ​</script>

    <div class="hideme">
        <span id="${AssessmentStepResult.Not_Known.toString()}_RENDER">
            <assess:assessmentStepResult result="${AssessmentStepResult.Not_Known}" />
            <assess:assessmentStepResultTextOnly result="${AssessmentStepResult.Not_Known}" />
        </span>
        <span id="${AssessmentStepResult.Not_Applicable.toString()}_RENDER">
            <assess:assessmentStepResult result="${AssessmentStepResult.Not_Applicable}" />
            <assess:assessmentStepResultTextOnly result="${AssessmentStepResult.Not_Applicable}" />
        </span>
        <span id="${AssessmentStepResult.Satisfied.toString()}_RENDER">
            <assess:assessmentStepResult result="${AssessmentStepResult.Satisfied}" />
            <assess:assessmentStepResultTextOnly result="${AssessmentStepResult.Satisfied}" />
        </span>
        <span id="${AssessmentStepResult.Not_Satisfied.toString()}_RENDER">
            <assess:assessmentStepResult result="${AssessmentStepResult.Not_Satisfied}" />
            <assess:assessmentStepResultTextOnly result="${AssessmentStepResult.Not_Satisfied}" />
        </span>
    </div>

    <script type="text/javascript">
        var ASSESSMENTS = null;
        var OLD_INDEX = -1;
        var NEW_INDEX = -1;
        var SELECTED_ASSESSMENT = null;
        var SELECTED_STEP_DATA = null;
        var SELECTED_SUBSTEP = null;

        var LEFT_SIDE_DEFAULT_HTML = null;
        var RIGHT_SIDE_DEFAULT_HTML = null;

        var handleBarTemplates = {};


        $(document).ready(function(){
            console.log("Initializing page...");
            $('.hideme').hide();


            LEFT_SIDE_DEFAULT_HTML = $('#leftSideHtmlContainer').html();
            RIGHT_SIDE_DEFAULT_HTML = $('#rightSideHtmlContainer').html();

            handleBarTemplates["assessmentInfoHeaderTemplate"] = compileHandlebarTemplate("assessmentInfoHeaderTemplate");
            handleBarTemplates["assessmentStepDataHeaderTemplate"] = compileHandlebarTemplate("assessmentStepDataHeaderTemplate");
            handleBarTemplates["assessmentSubStepContainerHeaderTemplate"] = compileHandlebarTemplate("assessmentSubStepContainerHeaderTemplate");

            loadSubsteps();

        })

        function compileHandlebarTemplate(id){
            var theTemplateScript = $("#"+id).html();
            return Handlebars.compile(theTemplateScript);
        }

        function loadSubsteps() {
            $('#leftSideHtmlContainer').html(LEFT_SIDE_DEFAULT_HTML);
            $('#rightSideHtmlContainer').html(RIGHT_SIDE_DEFAULT_HTML);
            var url = '${createLink(controller: 'substepResolution', action: 'mylist')}';
            console.log("Calling off to get list of substeps from "+url);
            $.ajax({
                method: 'GET',
                url: url,
                data: {
                    format: 'json',
                    now: new Date().getMilliseconds() + ""
                },
                dataType: 'json',
                success: function(data){
                    // console.log("Successfully downloaded results: "+JSON.stringify(data, null, 4));
                    ASSESSMENTS = data.assessments;
                    selectSubstepByIndex(1);
                    updateLeftSideView();
                    updateRightSideView();
                }
            })
        }//end loadSubsteps()

        function nextButtonPress() {
            changeSubstepByIndex(NEW_INDEX+1);
        }

        function changeSubstepByIndex(index){
            selectSubstepByIndex(index);
            if( OLD_INDEX != -1 )
                $('#leftSideIndex'+OLD_INDEX).removeClass("active");
            $('#leftSideIndex'+NEW_INDEX).addClass("active");
            updateRightSideView();
        }



        function updateRightSideView() {
            console.log("Updating the right side view...");
            var html = "";
            if( SELECTED_SUBSTEP ){
                var dataModel = {assessment:SELECTED_ASSESSMENT, stepData: SELECTED_STEP_DATA, substep: SELECTED_SUBSTEP};
                html += '<div style="margin-top: 1em;">' + handleBarTemplates["assessmentInfoHeaderTemplate"](dataModel);
                html += handleBarTemplates["assessmentStepDataHeaderTemplate"](dataModel);
                html += handleBarTemplates["assessmentSubStepContainerHeaderTemplate"](dataModel);
                html += '</div>\n';
                setTimeout('updateStepResult()', 100);
            }else{
                html += "<div class=\"text-danger\">There is no assessment substep selected.</div>"
            }
            $('#rightSideHtmlContainer').html(html);
            setTimeout("registerKeywatcher()", 100);
        }//end updateRightSideView()

        function registerKeywatcher(){
            $('#assessorSubstepComment').keyup(function () {
                typewatch(function () {
                    changeAssessorComment();
                }, 500);
            });
        }

        function updateStepResult() {
            $('#assessmentStepResultStatusContainer').html(
                    $('#'+SELECTED_STEP_DATA.result+"_RENDER").html()
            )

            if( SELECTED_SUBSTEP ) {
                console.log("Updating substep data: "+JSON.stringify(SELECTED_SUBSTEP, null, 2));
                if (SELECTED_SUBSTEP.result) {
                    $('.resultPill').removeClass("active");
                    $('#' + SELECTED_SUBSTEP.result + '_li').addClass("active");
                }
                if (SELECTED_SUBSTEP.assessorComment) {
                    $('#assessorSubstepComment').val(SELECTED_SUBSTEP.assessorComment);
                } else {
                    $('#assessorSubstepComment').val('');
                }
            }

        }

        function selectSubstepByIndex(index){
            var currentIndex = 1;
            OLD_INDEX = NEW_INDEX;
            NEW_INDEX = -1;
            SELECTED_ASSESSMENT = null;
            SELECTED_STEP_DATA = null;
            SELECTED_SUBSTEP = null;
            if( ASSESSMENTS && ASSESSMENTS.length > 0 ) {
                for (var i = 0; i < ASSESSMENTS.length; i++) {
                    var assessment = ASSESSMENTS[i];
                    for (var j = 0; j < assessment.unfulfilledSubsteps.length; j++) {
                        var step = assessment.unfulfilledSubsteps[j];
                        for (var k = 0; k < step.substeps.length; k++) {
                            var substep = step.substeps[k];
                            if (substep.substepDataId == -1) {
                                if( index == currentIndex ){
                                    SELECTED_ASSESSMENT = assessment;
                                    SELECTED_STEP_DATA = step;
                                    SELECTED_SUBSTEP = substep;
                                    NEW_INDEX = currentIndex;
                                    break;
                                }
                                currentIndex++;
                            }
                        }
                        if( SELECTED_SUBSTEP != null )
                            break;
                    }
                    if( SELECTED_SUBSTEP != null )
                        break;
                }
            }
            if( SELECTED_SUBSTEP != null ){
                console.log("Selected substep set to #"+SELECTED_SUBSTEP.id+": "+SELECTED_SUBSTEP.name);
            }else{
                console.log("There is no substep at index "+index);
                if( index == 1 ){ // make sure it doesn't loop forever.
                    return;
                }else {
                    selectSubstepByIndex(1);
                }
            }
        }//end selectStepByIndex()

        function updateLeftSideView() {
            var html = "";

            console.log("Updating left side (unfulfilled substep list) UI...");

            html += "<ul class=\"nav nav-pills nav-stacked\" id=\"substepList\">\n";
            var currentIndex = 1;
            if( ASSESSMENTS && ASSESSMENTS.length > 0 ){
                for( var i = 0; i < ASSESSMENTS.length; i++ ){
                    var assessment = ASSESSMENTS[i];
                    for( var j = 0; j < assessment.unfulfilledSubsteps.length; j++ ){
                        var step = assessment.unfulfilledSubsteps[j];
                        for( var k = 0; k < step.substeps.length; k++ ){
                            var substep = step.substeps[k];
                            if( substep.substepDataId == -1 ){
                                var classText = "";
                                if( substepMatch(assessment, step, substep) ) {
                                    console.log("[LEFT SIDE UI UPDATE] Setting step '"+substep.name+"' to active because SELECTED_SUBSTEP.id["+SELECTED_SUBSTEP.id+"] == substep.id["+substep.id+"]");
                                    classText = "active";
                                }
                                var text =
                                        "<span class=\"assessmentSubStepIdHeader\">"+
                                            "Assessment #"+assessment.id+", Step #"+step.step.stepNumber+", Substep "+substep.id+
                                        "</span><br/><span class=\"assessmentSubStepIdName\">"+
                                            substep.name+
                                        "</span>";

                                html += "<li id=\"leftSideIndex"+currentIndex+"\" role=\"presentation\" class=\""+classText+"\">\n";
                                html += "   <a href=\"javascript:changeSubstepByIndex("+currentIndex+")\">"+text + "</a>\n";
                                html += "</li>\n";

                                currentIndex++;
                            }
                        }
                    }
                }
            }
            $('#substepCount').html(currentIndex-1);
            html += "</ul>\n";

            $('#leftSideHtmlContainer').html(html);
        }//end updateView()

        function substepMatch(assessment, step, substep ){
            if( SELECTED_ASSESSMENT && SELECTED_STEP_DATA && SELECTED_SUBSTEP ){
                return SELECTED_ASSESSMENT.id == assessment.id &&
                                SELECTED_STEP_DATA.id == step.id &&
                                SELECTED_SUBSTEP.id == substep.id;
            }
            return false;
        }

        function setResult(result){
            console.log("Setting result to "+result);
            $('.resultPill').removeClass('active');
            $('#'+result+"_li").addClass('active');

            SELECTED_SUBSTEP.result = result;

            var url = '${createLink(controller:'substepResolution', action: 'updateSubstepResult', params: [assessmentId: '_ASSID_', stepNumber: '_STEPNUM_', substepId: '_SUBSTEPID_'])}';
            url = url.replace("_ASSID_", SELECTED_ASSESSMENT.id);
            url = url.replace("_STEPNUM_", SELECTED_STEP_DATA.step.stepNumber);
            url = url.replace("_SUBSTEPID_", SELECTED_SUBSTEP.id);

            console.log("Sending post request to change substep result to: "+url);

            $.ajax({
                url: url,
                data: {
                    format: 'json',
                    now: new Date().getTime(),
                    result: result
                },
                dataType: 'json',
                method: 'POST',
                success: function(data, status, jqXHR){
                    console.log("Successfully updated result: " + JSON.stringify(data, null, 2));
                    $('#leftSideIndex'+NEW_INDEX).addClass("substepComplete");
                },
                error: function(jqXHR, statusText, error){
                    console.log("An error occurred: "+statusText+", "+error);
                }
            })

        }

        function changeAssessorComment(){
            console.log("Changing assessor comment...")


            var url = '${createLink(controller:'substepResolution', action: 'updateAssessorComment', params: [assessmentId: '_ASSID_', stepNumber: '_STEPNUM_', substepId: '_SUBSTEPID_'])}';
            url = url.replace("_ASSID_", SELECTED_ASSESSMENT.id);
            url = url.replace("_STEPNUM_", SELECTED_STEP_DATA.step.stepNumber);
            url = url.replace("_SUBSTEPID_", SELECTED_SUBSTEP.id);

            var assessorSubstepComment = $('#assessorSubstepComment').val();
            console.log("Sending post request to change substep comment to: "+url);

            SELECTED_SUBSTEP.assessorComment = assessorSubstepComment;

            $.ajax({
                url: url,
                data: {
                    format: 'json',
                    now: new Date().getTime(),
                    assessorSubstepComment: assessorSubstepComment
                },
                dataType: 'json',
                method: 'POST',
                success: function(data, status, jqXHR){
                    console.log("Successfully updated assessorSubstepComment: " + JSON.stringify(data, null, 2));

                },
                error: function(jqXHR, statusText, error){
                    console.log("An error occurred: "+statusText+", "+error);
                }
            })

        }

    </script>

	</body>
</html>

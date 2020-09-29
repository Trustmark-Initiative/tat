<%@ page import="edu.gatech.gtri.trustmark.v1_0.model.ParameterKind; nstic.web.assessment.AssessmentStepData" %>
<!-- This is a partial HTML segment, meant to be rendered and shoved into another page. -->
<table class="table table-bordered table-condensed table-striped" style="margin-bottom: 0em;">
    <thead>
        <tr>
            <th style="text-align: center;"><input type="checkbox" id="globalCheckbox" onchange="updateCheckboxes(this)" checked="checked" /></th>
            <th>Trustmark Definitions (${infoList.size()})</th>
            <th style="text-align: center;" title="Does the system recommend granting this Trustmark?">Grant?</th>
            <th style="text-align: center;" title="Have all the assessment steps been given an answer?">Answers</th>
            <th style="text-align: center;" title="Are all required artifacts given?">Artifacts</th>
            <th style="text-align: center;" title="Do all required parameters have values?">Parameters</th>
            <th style="text-align: center;" title="Do the answers to the assessment steps meet the issuance criteria for the TD?">Meets Criteria</th>
        </tr>
    </thead>
    <tbody>
        <g:each in="${infoList}" var="info">
            <tr>
                <td style="text-align: center;">
                    <input id="trustmarkDefinition${info.td.id}Checkbox" name="trustmarkDefinition${info.td.id}Checkbox"
                            class="tdCheckbox"
                           type="checkbox" ${info.shouldGrant() ? 'checked="checked"' : ''}
                        onchange="grantTdCheckboxChange(this, ${info.td.id}, ${info.shouldGrant()})"
                    />
                </td>
                <td id="td${info.td.id}CellContainer">
                    <input type="hidden" name="td${info.td.id}ExtensionData" id="td${info.td.id}ExtensionData" value="" />
                    <div>
                        <a href="${info.td.uri}" target="_blank"><span class="glyphicon glyphicon-tag"></span></a>

                        ${info.td.name}, v. ${info.td.tdVersion}
                        <a style="float: right;" href="javascript:displayExtensionDescription(${info.td.id});" title="Displays the definition extension so you can provide that on the granted trustmark.">
                            <span id="td${info.td.id}ExtensionLabel" class="label label-default">Extension</span>
                        </a>
                    </div>
                </td>
                <td style="text-align: center;">
                    <g:if test="${!info.shouldGrant()}">
                        <span class="label label-danger">NO</span>
                    </g:if><g:else>
                        <span class="label label-success">YES</span>
                    </g:else>
                </td>
                <td style="text-align: center;">
                    <g:if test="${info.stepsWithNoAnswer.isEmpty()}">
                        <span class="glyphicon glyphicon-ok"></span>
                    </g:if><g:else>
                        <span class="glyphicon glyphicon-remove"></span>
                    </g:else>
                </td>
                <td style="text-align: center;">
                    <g:if test="${info.requiredArtifactProblems.isEmpty()}">
                        <span class="glyphicon glyphicon-ok"></span>
                    </g:if><g:else>
                        <span class="glyphicon glyphicon-remove"></span>
                    </g:else>
                </td>
                <td style="text-align: center;">
                    <g:if test="${info.requiredParameterProblems.isEmpty()}">
                        <span class="glyphicon glyphicon-ok"></span>
                    </g:if><g:else>
                        <span class="glyphicon glyphicon-remove"></span>
                    </g:else>
                </td>
                <td style="text-align: center;">
                    <g:if test="${info.issuanceCriteriaError}">
                        <span class="glyphicon glyphicon-alert" title="An error occurred while executing the issuance criteria!  Check the TD and have the system administrator check the log files."></span>
                    </g:if><g:else>
                        <g:if test="${info.issuanceCriteriaSatisfied}">
                            <span class="glyphicon glyphicon-ok"></span>
                        </g:if><g:else>
                            <span class="glyphicon glyphicon-remove"></span>
                        </g:else>
                    </g:else>
                </td>
            </tr>
        </g:each>
    </tbody>
</table>
<span class="help-block">Select the checkbox above to grant trustmarks for the selected Trustmark Definitions.</span>

<div class="modal fade" tabindex="-1" role="dialog" id="extensionDescriptionDialog">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="extensionDescriptionDialogTitle">Modal title</h4>
            </div>
            <div class="modal-body">
                <div class="text-muted">Here, please insert the extension description for the Trustmark against this TD.</div>
                <textarea rows="5" class="form-control" style="margin-left: 0; width: 100%" id="extensionDescriptionTextArea"></textarea>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
                <a href="javascript:saveExtensionDescription();" class="btn btn-primary">Save changes</a>
            </div>
        </div><!-- /.modal-content -->
    </div><!-- /.modal-dialog -->
</div><!-- /.modal -->


<script type="text/javascript">
    $(document).ready(function(){
        $.each($('.tdCheckbox'), function(index, item){
            if( $(item).is(":checked") ){
                // Do nothing, it's already checked
            }else{
                $('#globalCheckbox').attr('checked', null);
            }
        });
    });

    var CURRENT_EXTENSION_DESCRIPTION_ID = null;

    function displayExtensionDescription(tdId){
        CURRENT_EXTENSION_DESCRIPTION_ID = tdId;
        $('#extensionDescriptionDialogTitle').html('Edit Extension Description for TD '+tdId);
        $('#extensionDescriptionTextArea').val($('#td'+tdId+'ExtensionData').val());
        $('#extensionDescriptionDialog').modal('show');
    }

    function saveExtensionDescription(){
        console.log("Saving extension description for TD: "+CURRENT_EXTENSION_DESCRIPTION_ID);
        $('#td'+CURRENT_EXTENSION_DESCRIPTION_ID+'ExtensionData').val($('#extensionDescriptionTextArea').val());
        if( $('#extensionDescriptionTextArea').val().length > 0 ){
            $('#td'+CURRENT_EXTENSION_DESCRIPTION_ID+'ExtensionLabel').removeClass('label-default');
            $('#td'+CURRENT_EXTENSION_DESCRIPTION_ID+'ExtensionLabel').addClass('label-success');
        }else{
            $('#td'+CURRENT_EXTENSION_DESCRIPTION_ID+'ExtensionLabel').removeClass('label-success');
            $('#td'+CURRENT_EXTENSION_DESCRIPTION_ID+'ExtensionLabel').addClass('label-default');
        }
        $('#extensionDescriptionDialog').modal('hide')
    }

    var PARAMETER_PROBLEMS = {
        <g:each in="${infoList.findAll{!it.requiredParameterProblems.isEmpty()}}" var="info">
        '${info.td.id}': [
            <g:each in="${info.requiredParameterProblems}" var="paramProblem">
                <g:set var="step" value="${(nstic.web.assessment.AssessmentStepData)paramProblem.step}"/>
            {
                'step': ${raw(([
                    id : step.step.id,
                    name : step.step.name,
                    number: step.step.stepNumber,
                    description: step.step.description
                ] as grails.converters.JSON) as String)},
                'parameters': [
                    <g:each in="${(List<nstic.web.td.TdParameter>)paramProblem.parameters}" var="parameter">
                    ${raw((parameter.toJsonMap(true) as grails.converters.JSON) as String)},
                    </g:each>
                ]
            },
            </g:each>
        ],
        </g:each>
    };

    function updateExceptionsDesc(checkbox, id){
        if( $(checkbox).is(":checked") ){
            $('#'+id).prop('disabled', false);
        }else{
            $('#'+id).prop('disabled', true);
        }
    }

    function grantTdCheckboxChange(checkbox, tdId, shouldGrant){
        if ($(checkbox).is(":checked")) {
            console.log("TD Checkbox Change: " + tdId + ", should Grant: " + shouldGrant);
            var checkedCheckbox = (shouldGrant ? '' : ' checked="checked"');
            var disabledTextbox = (shouldGrant ? ' disabled="disabled"' : '');
            var html = '';
            html += '<div id="td'+tdId+'NotPristineData">\n';
            var paramProblems = PARAMETER_PROBLEMS[tdId];
            if (paramProblems) {
                var firstStepId = paramProblems[0].step.id;
                var performAssessmentHref = '${createLink(controller: 'assessmentPerform', action: 'startAssessment', id: assessment.id)}';
                performAssessmentHref += '?stepId=' + firstStepId;
                html += '  <br/>\n';
                html += '  <div class="text-center">\n';
                html += '    <div class="panel panel-danger" style="display: inline-block;">\n';
                html += '      <div class="panel-heading">\n';
                html += '        <span class="glyphicon glyphicon-exclamation-sign"></span>\n';
                html += '        Some required parameters are missing, and <br/>\n';
                html += '        this Trustmark cannot be granted without them!\n';
                html += '      </div>\n';
                html += '      <div class="panel-body">\n';
                html += '        <a class="btn btn-warning" href="' + performAssessmentHref + '">\n';
                html += '          <span class="glyphicon glyphicon-log-in"></span>\n';
                html += '          Reopen Assessment to Fill in Parameters\n';
                html += '        </a>\n';
                html += '      </div>\n';
                html += '  </div>\n';
                html += '  </div>\n';
            }
            else {
                html += '  <div class="checkbox">\n';
                html += '    <label><input type="checkbox"' + checkedCheckbox + ' name="td'+tdId+'HasExceptions" id="td'+tdId+'HasExceptions" onchange="updateExceptionsDesc(this, \'td'+tdId+'ExceptionsDesc\')" /> Has Exceptions</lable> <br/>\n';
                html += '  </div>\n';
                html += '  <textarea rows="2" class="form-control" style="margin-left: 0; width: 100%; resize: none;" name="td'+tdId+'ExceptionsDesc" id="td'+tdId+'ExceptionsDesc"' + disabledTextbox + '></textarea>\n';
            }
            html += '</div>\n';
            $('#td'+tdId+'CellContainer').append(html);
        } else {
            $('#td'+tdId+'NotPristineData').remove();
        }
    }

    function updateCheckboxes(caller){
        if( $(caller).is(':checked') ){
            $.each($('.tdCheckbox'), function(index, item){
                if( $(item).is(":checked") ){
                    // Do nothing, it's already checked
                }else{
                    $(item).trigger('click');
                }
            });
        }else{
            $.each($('.tdCheckbox'), function(index, item){
                if( $(item).is(":checked") ){
                    $(item).trigger('click');
                }else{
                    // Do nothing, it's already unchecked
                }
            });
        }
    }
</script>
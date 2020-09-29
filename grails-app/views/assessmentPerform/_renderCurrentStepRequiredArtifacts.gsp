<%@ page import="org.apache.commons.io.FileUtils" %>

<g:if test="${currentStepRequiredArtifacts && !currentStepRequiredArtifacts.isEmpty()}">
    <hr/>
    <div class="panel-group" id="artifactsAccordian">
        <g:each in="${currentStepRequiredArtifacts}" var="requiredArtifact" status="artifactStatus">
            <div class="panel panel-default">
                <div class="panel-heading">
                    <h4 class="panel-title">
                        <a data-toggle="collapse" data-parent="#accordion" href="#collapse_${requiredArtifact.artifact.id}">
                            <g:if test="${requiredArtifact.data != null && !requiredArtifact.data.isEmpty()}">
                                <span style="color: darkgreen;" class="glyphicon glyphicon-ok-sign" title="Required artifact is present"></span>
                            </g:if><g:else>
                                <span style="color: darkred;" class="glyphicon glyphicon-remove-sign" title="Missing required artifact"></span>
                            </g:else>
                            ${requiredArtifact.artifact.name}
                        </a>
                    </h4>
                </div>

                <div id="collapse_${requiredArtifact.artifact.id}" class="panel-collapse collapse in">
                    <div class="panel-body">
                        <div class="artifactDescription">${requiredArtifact.artifact.description}</div>

                        <hr/>

                        <div class="requiredArtifactFormContainer">

                            <table class="artifactDisplayTable">
                                <g:if test="${requiredArtifact.data != null && !requiredArtifact.data.isEmpty()}">
                                    <g:each in="${requiredArtifact.data}" var="artifact" status="artifactStatus2">
                                        <tr class="${(artifactStatus2 == 0) ? 'first ' : ''}${(artifactStatus2 % 2 == 0) ? 'even' : 'odd'}">
                                            <td class="actionsCell">
                                                <a href="${createLink(controller: 'assessmentPerform', action: 'viewArtifact', id: assessment.id, params: [stepNumber: currentStepData.step.stepNumber, artifactId: artifact.id])}" title="View this artifact">
                                                    <span class="glyphicon glyphicon-eye-open"></span>
                                                </a>
                                                <a href="${createLink(controller: 'assessmentPerform', action: 'editArtifact', id: assessment.id, params: [stepNumber: currentStepData.step.stepNumber, artifactId: artifact.id])}" title="Edit this artifact">
                                                    <span class="glyphicon glyphicon-pencil"></span>
                                                </a>
                                                <a href="javascript:deleteArtifact(${artifact.id})" title="Delete this artifact">
                                                    <span class="glyphicon glyphicon-remove-sign"></span>
                                                </a>
                                            </td>
                                            <td>
                                                <g:if test="${artifact.comment && !artifact.data}">
                                                <%-- TODO What if this is a really long-ass comment? --%>
                                                    Comment from ${artifact.uploadingUser}: ${artifact.comment?.substring(0, Math.min(artifact.comment?.length(), 200)).encodeAsHTML()}
                                                </g:if><g:else>
                                                    <a href="${createLink(controller: 'binary', action: 'view', id: artifact.data.id)}" target="_blank">
                                                        File[${artifact.data.originalFilename}, Size: ${FileUtils.byteCountToDisplaySize(artifact.data.fileSize)}]
                                                    </a>
                                                    uploaded by user ${artifact.uploadingUser}
                                                </g:else>
                                            </td>
                                        </tr>
                                    </g:each>
                                </g:if><g:else>
                                    <tr><td colspan="4">
                                        <em>There are no assessment artifacts.</em>
                                    </td></tr>
                                </g:else>
                            </table>

                            <div style="margin-top: 0.5em;">
                                <a class="btn btn-default btn-xs" href="${createLink(controller: 'assessmentPerform', action: 'createArtifact', id: assessment.id, params: [stepNumber: currentStepData.step.stepNumber, requiredArtifactId: requiredArtifact.artifact.id])}">
                                    Add
                                </a>
                            </div>
                        </div>
                    </div>
                </div><!-- End Panel Body -->
            </div><!-- End Panel -->
        </g:each>
    </div>
</g:if><g:else>
    <!-- There are no required artifacts for this step -->
</g:else>

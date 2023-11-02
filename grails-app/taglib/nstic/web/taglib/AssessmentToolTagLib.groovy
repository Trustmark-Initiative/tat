package nstic.web.taglib

import edu.gatech.gtri.trustmark.v1_0.impl.model.TrustmarkParameterBindingImpl
import edu.gatech.gtri.trustmark.v1_0.model.ParameterKind
import nstic.assessment.ColorPalette
import nstic.web.assessment.ArtifactData
import nstic.web.assessment.AssessmentStatus
import nstic.web.assessment.AssessmentStepData
import nstic.web.assessment.AssessmentStepResult
import nstic.web.assessment.ParameterValue
import nstic.web.td.AssessmentStepArtifact
import nstic.web.td.TdParameter
import org.apache.commons.io.FileUtils

import javax.servlet.ServletException

class AssessmentToolTagLib {

    static namespace = "assess"
    static defaultEncodeAs = 'raw'
//    static encodeAsForTags = [tagName: 'raw']


    def assessmentStatusName = {attrs, body ->
        if( !attrs.status )
            throw new ServletException("status is a required attribute for tag 'assessmentStatusName'.")

        String name = "Unknown";
        if( attrs.status == AssessmentStatus.ABORTED ){
            name = "Aborted";
        }else if( attrs.status == AssessmentStatus.CREATED ){
            name = "Created";
        }else if( attrs.status == AssessmentStatus.FAILED ){
            name = "Failed";
        }else if( attrs.status == AssessmentStatus.IN_PROGRESS ){
            name = "In Progress";
        }else if( attrs.status == AssessmentStatus.PENDING_ASSESSED ){
            name = "Pending Assessed";
        }else if( attrs.status == AssessmentStatus.PENDING_ASSESSOR ){
            name = "Pending Assessor";
        }else if( attrs.status == AssessmentStatus.SUCCESS ){
            name = "Success";
        }else if( attrs.status == AssessmentStatus.WAITING ){
            name = "Waiting";
        }else{
            log.warn("Unknown status given to taglib assess:assessmentStatusName => ${attrs.status}")
        }

        out << name

    }//end assessmentStatusName()

    def assessmentStatusIcon = {attrs, body ->
        if( !attrs.status )
            throw new ServletException("status is a required attribute for tag 'assessmentStatusIcon'.")

        String icon = "glyphicon glyphicon-question-sign";
        String color = ColorPalette.DEFAULT_TEXT.toString();
        String title = "Unknown";
        if( attrs.status == AssessmentStatus.ABORTED ){
            icon = "glyphicon glyphicon-warning-sign";
            color = ColorPalette.ASSESSMENT_STATUS_ABORTED.toString();
            title = "Aborted";

        }else if( attrs.status == AssessmentStatus.CREATED ){
            icon = "glyphicon glyphicon-check";
            color = ColorPalette.ASSESSMENT_STATUS_CREATED.toString();
            title = "Created";

        }else if( attrs.status == AssessmentStatus.FAILED ){
            icon = "glyphicon glyphicon-remove";
            color = ColorPalette.ASSESSMENT_STATUS_FAILED.toString();
            title = "Failed";

        }else if( attrs.status == AssessmentStatus.IN_PROGRESS ){
            icon = "glyphicon glyphicon-cog";
            color = ColorPalette.ASSESSMENT_STATUS_IN_PROGRESS.toString();
            title = "In Progress";

        }else if( attrs.status == AssessmentStatus.PENDING_ASSESSED ){
            icon = "glyphicon glyphicon-time";
            color = ColorPalette.ASSESSMENT_STATUS_PENDING_ASSESSED.toString();
            title = "Pending Assessed";

        }else if( attrs.status == AssessmentStatus.PENDING_ASSESSOR ){
            icon = "glyphicon glyphicon-exclamation-sign";
            color = ColorPalette.ASSESSMENT_STATUS_PENDING_ASSESSOR.toString();
            title = "Pending Assessor";

        }else if( attrs.status == AssessmentStatus.SUCCESS ){
            icon = "glyphicon glyphicon-ok";
            color = ColorPalette.ASSESSMENT_STATUS_SUCCESS.toString();
            title = "Success";

        }else if( attrs.status == AssessmentStatus.WAITING ){
            icon = "glyphicon glyphicon-link";
            color = ColorPalette.ASSESSMENT_STATUS_WAITING.toString();
            title = "Waiting";

        }else{
            log.warn("Unknown status given to taglib assess:assessmentStatusName => ${attrs.status}")
        }

        out << '<span class="'+icon+'" style="color: #'+color+'" title="'+title+'"></span>';

    }//end assess:assessmentStatusIcon

    def assessmentStepResult = { attrs, body ->
        if( !attrs.result )
            throw new ServletException("result is a required attribute for tag 'assessmentStepResult'.")

        String title = "";
        String icon = "";
        String color = "";
        if( attrs.result == AssessmentStepResult.Not_Known ){
            title = "Result Status is Not Known";
            icon = "question-sign";
            color= "#${ColorPalette.STEP_RESULT_UNKNOWN.toString()}";
        }else if( attrs.result == AssessmentStepResult.Satisfied ){
            title = "This step has been satisfied";
            icon = "ok-sign";
            color= "#${ColorPalette.STEP_RESULT_SATISFIED.toString()}";
        }else if( attrs.result == AssessmentStepResult.Not_Satisfied ){
            title = "This step was not satisfied";
            icon = "remove-sign";
            color= "#${ColorPalette.STEP_RESULT_NOT_SATISFIED.toString()}";
        }else if( attrs.result == AssessmentStepResult.Not_Applicable ){
            title = "This step is not applicable.";
            icon = "minus-sign";
            color= "#${ColorPalette.STEP_RESULT_NA.toString()}";
        }else{
            log.error("Encountered unknown Assessment Step Result: ${attrs.result}")
            title = "Result Status is Not Known";
            icon = "question-sign";
            color= "#${ColorPalette.STEP_RESULT_UNKNOWN.toString()}";
        }

        out << "<span style=\"color: $color;\" class=\"glyphicon glyphicon-$icon\" title=\"$title\"></span>";
    }

    def assessmentStepResponseResult = { attrs, body ->
        if( !attrs.result )
            throw new ServletException("result is a required attribute for tag 'assessmentStepResult'.")

        if( !attrs.description )
            throw new ServletException("description is a required attribute for tag 'assessmentStepResult'.")

        String title = attrs.description;
        String icon = "";
        String color = "";
        if( attrs.result == AssessmentStepResult.Not_Known ){
            icon = "question-sign";
            color= "#${ColorPalette.STEP_RESULT_UNKNOWN.toString()}";
        }else if( attrs.result == AssessmentStepResult.Satisfied ){
            icon = "ok-sign";
            color= "#${ColorPalette.STEP_RESULT_SATISFIED.toString()}";
        }else if( attrs.result == AssessmentStepResult.Not_Satisfied ){
            icon = "remove-sign";
            color= "#${ColorPalette.STEP_RESULT_NOT_SATISFIED.toString()}";
        }else if( attrs.result == AssessmentStepResult.Not_Applicable ){
            icon = "minus-sign";
            color= "#${ColorPalette.STEP_RESULT_NA.toString()}";
        }else{
            log.error("Encountered unknown Assessment Step Response: ${attrs.result}")
            title = "Result Status is Not Known";
            icon = "question-sign";
            color= "#${ColorPalette.STEP_RESULT_UNKNOWN.toString()}";
        }

        out << "<span style=\"color: $color;\" class=\"glyphicon glyphicon-$icon\" title=\"$title\"></span>";
    }

    /**
     * Generates a legend for assessment step result
     */
    def assessmentStepStatusLegend = { attrs, body ->
        out << """
<span style="color: #${ColorPalette.STEP_RESULT_SATISFIED.toString()};" class="glyphicon glyphicon-ok-sign"></span>
Satisfied,
<span style="color: #${ColorPalette.STEP_RESULT_NOT_SATISFIED.toString()};" class="glyphicon glyphicon-remove-sign"></span>
Not Satisfied,
<span style="color: #${ColorPalette.STEP_RESULT_NA.toString()};" class="glyphicon glyphicon-minus-sign"></span>
N/A,
<span style="color: #${ColorPalette.STEP_RESULT_UNKNOWN.toString()};" class="glyphicon glyphicon-question-sign"></span>
Unknown
""";
    }//end assessmentStepStatusLegend()


    def assessmentStepResponseTextOnly = { attrs, body ->
        if( !attrs.result )
            throw new ServletException("result is a required attribute for tag 'assessmentStepResult'.")

        if( !attrs.name )
            throw new ServletException("description is a required attribute for tag 'assessmentStepResult'.")

        if( !attrs.description )
            throw new ServletException("description is a required attribute for tag 'assessmentStepResult'.")

        String title = attrs.name;
        String desc = attrs.description;

        out << "$title - $desc";

    }

    def assessmentStepResultTextOnly = { attrs, body ->
        if( !attrs.result )
            throw new ServletException("result is a required attribute for tag 'assessmentStepResult'.")

        String title = "";
        String desc = "";
        if( attrs.result == AssessmentStepResult.Not_Known ){
            title = "Not Known"
            desc = "Result Status is Not Known";
        }else if( attrs.result == AssessmentStepResult.Satisfied ){
            title = "Satisfied"
            desc = "All requirements have been satisfied";
        }else if( attrs.result == AssessmentStepResult.Not_Satisfied ){
            title = "Not Satisfied"
            desc = "One or more requirements were not satisfied";
        }else if( attrs.result == AssessmentStepResult.Not_Applicable ){
            title = "Not Applicable"
            desc = "This step is not applicable.";
        }else{
            log.error("Encountered unknown Assessment Step Result Text Only: ${attrs.result}")
            title = "Not Known"
            desc = "Result Status is Not Known";
        }


        out << "$title - $desc";

    }


    /**
     * Displays the paperclip icon if the given step needs one, along with its status (satisfied or not).
     */
    def assessmentStepAttachmentStatus = { attrs, body ->
        if( !attrs.step )
            throw new ServletException("step is a required attribute for tag 'assessmentStepAttachmentStatus' and should contain the AssessmentStepData to analyze.")

        AssessmentStepData stepData = attrs.step;
        if( stepData.getHasRequiredAttachments() || stepData.getHasRequiredParameters() ){
            String topIcon = ""
            String title = ""
            String icon = ""
            String color = ""
            if( stepData.result.result == AssessmentStepResult.Not_Applicable ){
                title = "Required attachments not required, since this step is marked N/A"
                icon = "minus";
                color = "#" + ColorPalette.DEFAULT_BORDER.toString();
            }else {
                boolean satsifiedRequiredAttachments = stepData.getAreAllAttachmentsSatisfied();
                boolean satisfiedRequiredParameters = stepData.getAreAllRequiredParametersFilled();
                if (satsifiedRequiredAttachments && satisfiedRequiredParameters) {
                    topIcon = "list"
                    title = "Required Artifact(s) and Parameter(s) Satisfied"
                    icon = "ok-sign";
                    color = "#" + ColorPalette.SUCCESS_TEXT.toString();
                } else {
                    if (!satsifiedRequiredAttachments) {
                        topIcon = "paperclip"
                        title = "Required Artifact(s) Not Satisfied"
                    }
                    else if (!satisfiedRequiredParameters) {
                        topIcon = "cog"
                        title = "Required Parameter(s) Not Satisfied"
                    }
                    icon = "remove-sign";
                    color = "#" + ColorPalette.ERROR_TEXT.toString();
                }
            }

            out << """
<span class="glyphicon glyphicon-$topIcon" title="$title">
    <span class="glyphicon glyphicon-$icon" style="position: absolute; color: $color; left: 0.5em; top: 0.5em; font-size: 70%;"></span>
</span>
"""
        }


    }//end assessmentStepAttachmentStatus

    /**
     * Displays the paperclip icon for the specific required artifact, along with its status (satisfied or not).
     */
    def assessmentStepSingleArtifactStatus = { attrs, body ->
        if( !attrs.step )
            throw new ServletException("step is a required attribute for tag 'assessmentStepSingleArtifactStatus' and should contain the AssessmentStepData to analyze.")
        if( !attrs.artifact )
            throw new ServletException("artifact is a required attribute for tag 'assessmentStepSingleArtifactStatus' and should contain the Artifact to analyze.")

        AssessmentStepData stepData = attrs.step;
        AssessmentStepArtifact artifact = attrs.artifact;
        ArtifactData artifactData = null;
        for( ArtifactData ad : stepData.artifacts ?: [] ){
            if( ad.requiredArtifact && ad.requiredArtifact.id == artifact.id ){
                artifactData = ad;
                break;
            }
        }

        String title = ""
        String icon = ""
        String color = ""
        if( artifactData != null ){
            title = "Required Artifact is Satisfied"
            icon = "ok-sign"
            color = "#" + ColorPalette.SUCCESS_TEXT.toString();
        }else{
            if( stepData.result.result == AssessmentStepResult.Not_Applicable ){
                title = "Required attachments not required, since this step is marked N/A"
                icon = "minus";
                color = "#" + ColorPalette.DEFAULT_BORDER.toString();
            } else {
                title = "Required Artifact(s) Not Satisfied"
                icon = "remove-sign";
                color = "#" + ColorPalette.ERROR_TEXT.toString();
            }
        }

            out << """
<span class="glyphicon glyphicon-paperclip" title="$title">
    <span class="glyphicon glyphicon-$icon" style="position: absolute; color: $color; left: 0.5em; top: 0.5em; font-size: 70%;"></span>
</span>
"""


    }//end assessmentStepAttachmentStatus()

    /**
     * Displays the cog icon for the specific required parameter, along with its status (filled or not).
     */
    def assessmentStepSingleParameterStatus = { attrs, body ->
        if( !attrs.step )
            throw new ServletException("step is a required attribute for tag 'assessmentStepSingleParameterStatus' and should contain the AssessmentStepData to analyze.")
        if( !attrs.parameter )
            throw new ServletException("parameter is a required attribute for tag 'assessmentStepSingleParameterStatus' and should contain the TdParameter to analyze.")

        AssessmentStepData stepData = attrs.step;
        TdParameter parameter = attrs.parameter;
        Boolean isFilled = stepData.isParameterFilled(parameter)

        String title = ""
        String icon = ""
        String color = ""
        if( isFilled ){
            title = "Required Parameter is Filled"
            icon = "ok-sign"
            color = "#" + ColorPalette.SUCCESS_TEXT.toString();
        }else{
            if( stepData.result.result == AssessmentStepResult.Not_Applicable ){
                title = "Required parameters not required, since this step is marked N/A"
                icon = "minus";
                color = "#" + ColorPalette.DEFAULT_BORDER.toString();
            } else {
                title = "Required Parameter(s) Not Filled"
                icon = "remove-sign";
                color = "#" + ColorPalette.ERROR_TEXT.toString();
            }
        }

            out << """
<span class="glyphicon glyphicon-cog" title="$title">
    <span class="glyphicon glyphicon-$icon" style="position: absolute; color: $color; left: 0.5em; top: 0.5em; font-size: 70%;"></span>
</span>
"""


    }//end assessmentStepSingleParameterStatus()



    def renderArtifactSummary = {attrs, body ->
        if( !attrs.artifact )
            throw new ServletException("artifact is a required attribute for tag 'renderArtifactSummary' and should contain the ArtifactData to render.")

        ArtifactData artifact = attrs.artifact;
        boolean shortenComment = attrs.shortenComment ? Boolean.parseBoolean(attrs.shortenComment) : true;

        String commentText = "";
        if( artifact.comment ) {
            commentText = artifact.comment;
            if( shortenComment && artifact.comment.length() > 100 ){
                commentText = artifact.comment.substring(0, 100) + "...";
            }
        }

        out << "\n\n<div class=\"artifactContainer\">\n"
        String starIcon = "star-empty";
        String starText = "This is an extra uploaded artifact, it does not fill any requirement.";
        if( artifact.requiredArtifact ){
            starIcon = "star";
            starText = "This satisifes a required artifact: ${artifact.requiredArtifact.name}";
        }

        out << "    <span class=\"glyphicon glyphicon-${starIcon}\" title=\"${starText}\"></span>\n"

        String subHtml = "";
        if( artifact.comment && !artifact.data ){
            out << """
<span class="artifactCommentContainer">
    Comment from ${artifact.uploadingUser?.contactInformation?.responder}: <br/>
    <pre style="width: 100%;">${encodeAs(codec: 'html'){commentText}}</pre>
</span>"""
        }else{
            out << """
<span class="artifactBinaryContainer">
        <a href="${g.createLink(controller:'binary', action:'view', id: artifact.data?.id)}" target="_blank">
            File[${artifact.data.originalFilename}, Size: ${FileUtils.byteCountToDisplaySize(artifact.data.fileSize)}]
        </a>
        uploaded by user ${artifact.uploadingUser?.contactInformation?.responder}
</span>
"""

        }

        out << "</div> <!-- artifactContainer --> \n\n"
    }


    def renderParameterSummary = {attrs, body ->
        if( !attrs.paramValue )
            throw new ServletException("paramValue is a required attribute for tag 'renderParameterSummary' and should contain the ParameterValue to render.")

        ParameterValue paramValue = attrs.paramValue;

        out << "\n\n<div class=\"artifactContainer\">\n"
        String starIcon = "star";
        String starText = "This is the value for parameter: ${paramValue.parameter.name}";

        out << "    <span class=\"glyphicon glyphicon-${starIcon}\" title=\"${starText}\"></span>\n"

        out << "${paramValue.parameter.identifier}: "
        ParameterKind paramKind = Enum.valueOf(ParameterKind.class, paramValue.parameter.kind)
        TrustmarkParameterBindingImpl paramBinding = new TrustmarkParameterBindingImpl(
            parameterKind: paramKind,
            value: paramValue.userValue
        )
        switch (paramBinding.parameterKind) {
            case ParameterKind.ENUM_MULTI:
                if (paramBinding.stringListValue.size() > 1) {
                    out << "<ul>"
                    for (String value : paramBinding.stringListValue) {
                        out << "<li>\"${value}\"</li>"
                    }
                    out << "</ul>"
                    break
                }
                else {
                    out << "<!-- single chosen for multi -->"
                }
            case ParameterKind.ENUM:
            case ParameterKind.STRING:
                out << "\"${paramBinding.stringValue}\""
                break
            case ParameterKind.NUMBER:
                out << "${paramBinding.numericValue}"
                break
            case ParameterKind.BOOLEAN:
                out << "${paramBinding.booleanValue}"
                break
            case ParameterKind.DATETIME:
                out << "${paramBinding.dateTimeValue}"
                break
        }

        out << "</div> <!-- artifactContainer --> \n\n"
    }


}

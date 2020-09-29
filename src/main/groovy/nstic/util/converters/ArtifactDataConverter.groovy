package nstic.util.converters

import grails.databinding.converters.ValueConverter
import nstic.web.assessment.ArtifactData
import nstic.web.assessment.Assessment
import nstic.web.assessment.AssessmentStepData
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired

import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest

/**
 * Created by brad on 12/12/14.
 */
class ArtifactDataConverter implements ValueConverter {

    static final Logger log = LoggerFactory.getLogger(ArtifactDataConverter.class)

    public static String BOUND_ARTIFACT_DATA_REQUEST_ATTRIBUTE = ArtifactDataConverter.class.name + ".BOUND_ARTIFACT_DATA";

    @Autowired
    HttpServletRequest request

    /**
     * Knows how to convert an ArtifactData identifier (as a String)
     * into an ArtifactData object tied to the database.
     */
    ArtifactData do_convert(String artifactDataIdString){
        log.info("Converting ${artifactDataIdString} into an ArtifactData...")

        if( artifactDataIdString == null ){
            log.error("Cannot convert null into any ArtifactData!");
            throw new ServletException("Null value not accepted for ArtifactData id value.")
        }
        long artifactDataId = -1;
        try{
            artifactDataId = Long.parseLong(artifactDataIdString);
        }catch(Throwable t){
            log.error("Cannot coerce assessment id ${artifactDataId} into a long value.")
        }

        Assessment assessment = request.getAttribute(AssessmentConverter.BOUND_ASSESSMENT_REQUEST_ATTRIBUTE);
        if( assessment == null ){
            log.error("Cannot select artifact data when assessment is not bound in request.")
            throw new ServletException("Assessment has not been properly bound to the request.")
        }

        AssessmentStepData stepData = request.getAttribute(AssessmentStepDataConverter.BOUND_ASSESSMENT_STEP_REQUEST_ATTRIBUTE);
        if( stepData == null ){
            log.error("Cannot select artifact data when assessment step is not bound in request.")
            throw new ServletException("AssessmentStepData has not been properly bound to the request.")
        }

        ArtifactData artifactData = null;
        stepData.artifacts.each{ currentArtifact ->
            if( currentArtifact.id == artifactDataId ){
                artifactData = currentArtifact;
            }
        };
        if( !artifactData ){
            log.warn("Could not find artifact: ${artifactDataId}")
            throw new ServletException("No such artifact ${artifactDataId}")
        }


        if( request ){
            request.setAttribute(BOUND_ARTIFACT_DATA_REQUEST_ATTRIBUTE, artifactData);
        }

        return artifactData;
    }//end convert

    @Override
    boolean canConvert(Object value) {
        return true;
    }

    @Override
    Object convert(Object value) {
        return do_convert(value?.toString());
    }

    @Override
    Class<?> getTargetType() {
        return ArtifactData.class;
    }



}//end AssessmentConverter()
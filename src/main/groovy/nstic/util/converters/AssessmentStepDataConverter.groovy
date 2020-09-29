package nstic.util.converters

import nstic.web.assessment.Assessment
import nstic.web.assessment.AssessmentStepData
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import grails.databinding.converters.ValueConverter
import org.springframework.beans.factory.annotation.Autowired

import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest

/**
 * Created by brad on 12/12/14.
 */
class AssessmentStepDataConverter implements ValueConverter {

    static final Logger log = LoggerFactory.getLogger(AssessmentStepDataConverter.class)

    public static String BOUND_ASSESSMENT_STEP_REQUEST_ATTRIBUTE = AssessmentStepDataConverter.class.name + ".BOUND_ASSESSMENT_STEP";


    @Autowired
    HttpServletRequest request

    /**
     * Knows how to convert an Assessment identifier (as a String)
     * into an Assessment object tied to the database.
     */
    AssessmentStepData do_convert(String stepIdString){
        log.info("Converting ${stepIdString} into an AssessmentStepData...")

        if( stepIdString == null ){
            log.error("Cannot convert null into any AssessmentStepData!");
            throw new ServletException("Null value not accepted for AssessmentStepData id value.")
        }
        long stepId = -1l;
        try{
            stepId = Long.parseLong(stepIdString);
        }catch(Throwable t){
            log.error("Cannot coerce step number ${stepIdString} into an integer value.")
            throw new ServletException("An invalid stepId ${stepIdString} has been sent, this cannot be coerced into a Long value.")
        }

        AssessmentStepData stepData = null;
        try {
            stepData = AssessmentStepData.get(stepId);
        }catch(Throwable T){}
        if( !stepData ){
            log.warn("Could not locate AssessmentStepData by unique id ${stepId}")
            throw new ServletException("Could not locate AssessmentStepData by unique id ${stepId}")
        }

        if( request ){
            request.setAttribute(BOUND_ASSESSMENT_STEP_REQUEST_ATTRIBUTE, stepData);
        }

        return stepData;
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
        return AssessmentStepData.class
    }


}//end AssessmentStepDataConverter()
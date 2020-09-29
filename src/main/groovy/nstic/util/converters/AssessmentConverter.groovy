package nstic.util.converters

import nstic.web.assessment.Assessment
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest

import grails.databinding.converters.ValueConverter

/**
 * Created by brad on 12/12/14.
 */
@Component("AssessmentConverter")
class AssessmentConverter implements ValueConverter{

    static final Logger log = LoggerFactory.getLogger(AssessmentConverter.class)

    public static String BOUND_ASSESSMENT_REQUEST_ATTRIBUTE = AssessmentConverter.class.name + ".BOUND_ASSESSMENT";


    @Autowired
    HttpServletRequest request

    /**
     * Knows how to convert an Assessment identifier (as a String)
     * into an Assessment object tied to the database.
     */
    Assessment do_convert(String assessmentId){
        log.info("Converting ${assessmentId} into an assessment...")

        if( assessmentId == null ){
            log.error("Cannot convert null into any assessment!");
            throw new ServletException("Null value not accepted for assessment id value.")
        }
        long assId = -1;
        try{
            assId = Long.parseLong(assessmentId);
        }catch(Throwable t){
            log.error("Cannot coerce assessment id ${assId} into a long value.")
        }

        Assessment assessment = null;
        Assessment.withTransaction {
            assessment = Assessment.get(assId);
        }

        if( !assessment ){
            log.error("No such assessment in database: ${assId}")
            throw new ServletException("Invalid assessment id")
        }

        if( request ){
            request.setAttribute(BOUND_ASSESSMENT_REQUEST_ATTRIBUTE, assessment);
        }

        return assessment;
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
        return Assessment.class;
    }

}//end AssessmentConverter()
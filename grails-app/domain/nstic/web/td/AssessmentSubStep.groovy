package nstic.web.td

/**
 * Represents a sub-component of a failed step.
 * <br/><br/>
 * Created by brad on 3/27/15.
 */
class AssessmentSubStep {

    static belongsTo = [
        assessmentStep: AssessmentStep
    ]


    String name
    String description

    static constraints = {
        assessmentStep(nullable: false)
        name(nullable: false, blank: false, maxSize: 512)
        description(nullable: false, blank: false, maxSize: 65535)
    }

    static mapping = {
        table(name:'td_assessment_sub_step')
        assessmentStep(column: 'assessment_step_ref')
        description(type: 'text')
    }


    public Map toJsonMap(boolean shallow = false){
        def data = [
                id: this.id,
                name: this.name,
                description: this.description
        ]
        if( !shallow ){
            data.put("assessmentStep", this.assessmentStep.toJsonMap(false))
        }
        return data;
    }


}//end AssessmentSubStep
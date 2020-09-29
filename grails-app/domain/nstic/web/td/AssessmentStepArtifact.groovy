package nstic.web.td

/**
 * Represents the definition of a required assessment artifact on an assessment step.
 */
class AssessmentStepArtifact {

    static belongsTo = [
            /**
             * The {@link AssessmentStep} to which this artifact applies.
             */
            assessmentStep: AssessmentStep
    ]

    /**
     * The short, human-readable name of the required artifact.
     */
    String name
    /**
     * The more detailed description of what the required artifact really is.
     */
    String description

    static constraints = {
        assessmentStep(nullable: false)
        name(nullable: false, blank: false, maxSize: 256)
        description(nullable: true, blank: true, maxSize: 65535)
    }

    static mapping = {
        table(name:'td_assessment_step_artifact')
        assessmentStep(column: 'td_assessment_step_ref')
        description(type: 'text')
    }


    public Map toJsonMap(boolean shallow = false) {
        def json = [
                id: this.id,
                name: this.name,
                description: this.description,
                assessmentStep: this.assessmentStep?.toJsonMap(shallow)
        ]
        return json;
    }

}

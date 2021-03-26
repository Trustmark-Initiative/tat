package nstic.web.td
/**
 * Represents a trustmark definition's assessment step.  This is the definition of the step.
 */
class AssessmentStep {

    static belongsTo = [
        trustmarkDefinition: TrustmarkDefinition
    ]

    Integer stepNumber // Field used to order steps in a trustmark definition.
    String identifier // From the original source, indicates the unique identifier used for this step (present in issuance criteria for the TD)
    String name
    String description

    static hasMany = [
        artifacts: AssessmentStepArtifact,
        criteria: ConformanceCriterion,
        substeps: AssessmentSubStep,
        parameters: TdParameter
    ]

    static constraints = {
        trustmarkDefinition(nullable: false)
        stepNumber(nullable: false)
        identifier(nullable: false, blank: false, maxSize: 512)
        name(nullable: false, blank: false, maxSize: 512)
        description(nullable: false, blank: false, maxSize: 65535)
        substeps(nullable: true)
        criteria(nullable: true)
        parameters(nullable: true)
    }

    static mapping = {
        table(name:'td_assessment_step')
        trustmarkDefinition(column: 'trustmark_definition_ref')
        stepNumber(column: 'number')
        description(type: 'text')
        // The following enables cascading deletes from the assessment step
        parameterValues cascade: "all-delete-orphan"
    }



    public Map toJsonMap(boolean shallow = false) {
        def json = [
                id: this.id,
                identifier: this.identifier,
                stepNumber: this.stepNumber,
                name: this.name,
                description: this.description
        ]
        if( this.artifacts ){
            def artifactsJson = []
            this.artifacts.each{ artifact ->
                def artifactJson = [
                        id: artifact.id,
                        name: artifact.name,
                        description: artifact.description
                ]
                artifactsJson.add( artifactJson );
            }
            json.put("artifacts", artifactsJson);
        }
        if( this.parameters && !this.parameters.isEmpty() ){
            def paramJSON = []
            for( TdParameter p : this.parameters ){
                paramJSON.add(p.toJsonMap(true));
            }
            json.put("parameters", paramJSON);
        }
        if( !shallow ){
            json.put("trustmarkDefinition", [id: trustmarkDefinition.id, name: trustmarkDefinition.name]);
        }
        return json;
    }

}//end AssessmentStep

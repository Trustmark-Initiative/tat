package nstic.web.assessment

class AssessmentStepResponse {

    String name;
    String description;
    Boolean isDefault = Boolean.FALSE;
    AssessmentStepResult result = AssessmentStepResult.Not_Known;

    static constraints = {
        name(nullable: false)
        description(nullable: false)
        isDefault(nullable: false)
        result(nullable: false)
    }

    static mapping = {
        table(name:'assessment_step_response')
    }

    public Map toJsonMap(boolean shallow = false) {
        def json = [
                id: this.id,
                name: this.name,
                description: this.description,
                isDefault: this.isDefault,
                result: this.result?.toString(),
        ]

        return json;
    }

    public static AssessmentStepResponse newAssessmentStepResponse2(String name, String description, Boolean isDefault, AssessmentStepResult result) {
        AssessmentStepResponse asr = new AssessmentStepResponse(name: name, description: description, isDefault: isDefault, result: result)
        return asr
    }

    public static AssessmentStepResponse newAssessmentStepResponse(String name, String description, Boolean isDefault, String value) {
        // Mapping
        AssessmentStepResult assessmentStepResult = AssessmentStepResult.Not_Known

        if (value.equals("Yes")) {
            assessmentStepResult = AssessmentStepResult.Satisfied
        } else if (value.equals("No")) {
            assessmentStepResult = AssessmentStepResult.Not_Satisfied
        } else if(value.equals("NA")) {
            assessmentStepResult = AssessmentStepResult.Not_Applicable
        }

        AssessmentStepResponse asr = new AssessmentStepResponse(name: name, description: description, isDefault: isDefault, result: assessmentStepResult)
        return asr
    }

    public static AssessmentStepResponse getDefaultResponseByResult(AssessmentStepResult result) {
        return findByIsDefaultAndResult(true, result)
    }
}
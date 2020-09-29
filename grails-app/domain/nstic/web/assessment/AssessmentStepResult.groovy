package nstic.web.assessment

enum AssessmentStepResult {
    // TODO Can we "partially" satisfy a step?
    Not_Known, // Initially set value to indicate it's never been changed.
    Satisfied,
    Not_Satisfied,
    Not_Applicable;


    public static AssessmentStepResult fromString( String statusVal ){
        AssessmentStepResult result = null;
        AssessmentStepResult.values().each{ value ->
            if( value.toString().equalsIgnoreCase(statusVal.trim()) )
                result = value;
        }
        return result;
    }//end fromString()


}//end AssessmentStepResult

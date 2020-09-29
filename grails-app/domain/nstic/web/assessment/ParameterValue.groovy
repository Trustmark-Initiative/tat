package nstic.web.assessment

import nstic.web.td.TdParameter

/**
 * Stores the value of a TdParameter.
 * <br/><br/>
 * Created by brad on 6/3/16.
 */
class ParameterValue {

    /**
     * The parameter model which defines/constrains this value. Also, the trustmark which has this value.  Note that if
     * this field is null, then stepData MUST have a value. Finally, the assessment step associated to this
     * parameter
     */
    static belongsTo = [parameter: TdParameter, trustmark: Trustmark, stepData: AssessmentStepData]

    /**
     * The actual value entered by the user.  If a boolean, then the string "true" or "false", if a date, then it is a long
     * representing epoch milliseconds.
     */
    String userValue;

    static constraints = {
        stepData(nullable: true)
        trustmark(nullable: true)
        parameter(nullable: false)
        userValue(nullable: true, blank: true, maxSize: 65535)
    }

    static mapping = {
        table(name: 'parameter_value')
        stepData(column: 'step_data_ref')
        trustmark(column: 'trustmark_ref')
        parameter(column: 'parameter_ref')
        userValue(column: 'actual_value', type: 'text')
    }

}//end ParameterValue()
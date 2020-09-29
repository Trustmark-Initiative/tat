package nstic.web.assessment

/**
 * Created by brad on 4/1/14.
 *
 * Meant to represent the progression of state of an Assessment, so you can tell where any assessment is "at" for
 * any given time.
 */
public enum AssessmentStatus {
    UNKNOWN,            // A failure state, should not ever be set at runtime.  Used during transmission to indicate a null or bad value.
    CREATED,            // The assessment was created
    WAITING,            // The assessment is in the "queue" not actively being assessed, but not waiting on anything either.
    IN_PROGRESS,        // The assessment is currently in progress
    PENDING_ASSESSED,   // We are in a holding pattern, the assessed organization must complete an action
    PENDING_ASSESSOR,   // We are in a holding pattern, the assessing organization must complete an action
    ABORTED,            // Indicates the abandonment or complete dismissal of an assessment.
    FAILED,             // The assessment is completed has failed.
    SUCCESS;            // This assessment is completed and is successful.


    public static AssessmentStatus fromString( String value ){
        if( value ){
            for( AssessmentStatus status : AssessmentStatus.values() ){
                if( status.toString().equalsIgnoreCase(value?.trim()) ){
                    return status;
                }
            }
        }
        return null;
    }
}

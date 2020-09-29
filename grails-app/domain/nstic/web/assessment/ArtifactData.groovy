package nstic.web.assessment

import nstic.web.BinaryObject
import nstic.web.User
import nstic.web.td.AssessmentStepArtifact
import org.apache.commons.lang.StringUtils

/**
 * Represents an actual artifact uploaded as part of an Assessment.
 */
class ArtifactData {

    /**
     * Allows the assessor a useful way to refer to this artifact data.
     */
    String displayName;

    /**
     * Each artifact can have an optional comment text describing the artifact's relevance.  Sometimes, the binary is
     * empty and this field holds the artifact's text data directly.
     */
    String comment
    /**
     * The actual binary data representing this artifact.  Note that this field can be empty/null, if the user has
     * just typed text into the comment field.
     */
    BinaryObject data
    /**
     * When this artifact was uploaded to the server.
     */
    Date dateCreated
    /**
     * The user responsible for uploading this artifact.  This is the original user.
     */
    User uploadingUser;
    /**
     * The person who last modified this artifact.
     */
    User modifyingUser;
    /**
     * If null, then this artifact is just being "generally" attached, and it is expected that the comment would
     * describe what the heck it is.  If this field is not null, then this artifact "satisfies" the given
     * required artifact reference.
     */
    AssessmentStepArtifact requiredArtifact;

    static constraints = {
        displayName(nullable: true, blank: true, maxSize: 256)
        comment(nullable: true, blank: true, maxSize: 65535)
        data(nullable: true) // This is true for the delete.  We remove it here first, then delete the artifactData object.  Deleting the binary is then optional.
        dateCreated(nullable: true)
        uploadingUser(nullable: false)
        requiredArtifact(nullable: true)
        modifyingUser(nullable: true)
    }

    static mapping = {
        table(name: 'assessment_artifact_data')
        comment(type: 'text')
        uploadingUser(column: 'uploading_user_ref')
        modifyingUser(column: 'modifying_user_ref')
        requiredArtifact(column: 'required_artifact_ref')
    }



    public String getDisplayName() {
        if( StringUtils.isNotEmpty(displayName) ){
            return displayName;
        }else{
            return data?.originalFilename ?: "";
        }
    }

    public Map toJsonMap(boolean shallow = true){
        def json = [
                id        : this.id,
                comment   : this.comment,
                data      : this.data?.toJsonMap(shallow),
                dateCreated: this.dateCreated?.getTime(),
                uploadingUser: [username: this.uploadingUser?.username]
            ];
        return json;
    }//end toJsonMap

}//end ArtifactData

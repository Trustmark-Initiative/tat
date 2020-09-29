package nstic.web

import nstic.web.assessment.ArtifactData
import org.apache.commons.io.FileUtils

/**
 * Used to describe artifact information to certain services.
 */
public class ArtifactInformation implements Comparable {

    public ArtifactInformation( ArtifactData artifact ){
        this.artifactId = artifact.id;
        this.sourceClassName = artifact.class.getName();
        this.displayName = artifact.displayName ?: artifact.data?.originalFilename;
        this.description = artifact.comment ?: "";
        this.fileName = artifact.data?.originalFilename;
        this.createdBy = artifact.uploadingUser?.contactInformation?.responder ?: "";
        this.binaryObjectId = artifact.data?.id;
        this.mimeType = artifact.data?.mimeType;
        this.fileSize = artifact.data?.fileSize ?: 0l;
        this.humanReadableFileSize = FileUtils.byteCountToDisplaySize(this.fileSize);
    }

    public ArtifactInformation(OrganizationArtifact artifact){
        this.artifactId = artifact.id;
        this.sourceClassName = artifact.class.getName();
        this.displayName = artifact.displayName ?: artifact.data?.originalFilename;
        this.description = artifact.description ?: "";
        this.fileName = artifact.data?.originalFilename;
        this.createdBy = artifact.uploadingUser?.contactInformation?.responder ?: "";
        this.binaryObjectId = artifact.data?.id;
        this.mimeType = artifact.data?.mimeType;
        this.fileSize = artifact.data?.fileSize ?: 0l;
        this.humanReadableFileSize = FileUtils.byteCountToDisplaySize(this.fileSize);
    }

    Integer artifactId;
    String sourceClassName;
    String displayName;
    String description;
    String fileName;
    String createdBy;
    Integer binaryObjectId;
    String mimeType;
    Long fileSize;
    String humanReadableFileSize;

    public String getDisplayName() {
        return this.displayName ?: this.fileName;
    }

    public String toString(){
        return this.getDisplayName();
    }

    public boolean equals(Object other) {
        if( other && other instanceof ArtifactInformation ){
            ArtifactInformation that = (ArtifactInformation) other;
            return this.getDisplayName().equalsIgnoreCase(that.getDisplayName());
        }
        return false;
    }

    public int hashCode(){
        return this.getDisplayName().hashCode();
    }

    @Override
    int compareTo(Object other) {
        if( other && other instanceof ArtifactInformation ){
            ArtifactInformation that = (ArtifactInformation) other;
            return this.getDisplayName().compareToIgnoreCase(that.getDisplayName());
        }
        return -1;
    }//end compareTo()

}


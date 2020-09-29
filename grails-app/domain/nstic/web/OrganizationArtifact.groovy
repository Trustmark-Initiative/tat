package nstic.web

class OrganizationArtifact {

    static belongsTo = [organization: Organization]

    BinaryObject data;
    String displayName;
    String description;
    Boolean active = Boolean.TRUE;
    User uploadingUser;
    Date dateCreated;

    static constraints = {
        organization(nullable: false)
        data(nullable: false)
        displayName(nullable: false, blank: false, maxSize: 256)
        description(nullable: true, maxSize: 65535)
        uploadingUser(nullable: false)
        dateCreated(nullable: true)
    }

    static mapping = {
        organization(column: 'organization_ref')
        data(column: 'binary_object_ref')
        uploadingUser(column: 'uploading_user_ref')
        displayName(column: 'display_name')
        description(type:'text', column: 'description')
        active(nullable: false)
    }


}//end OrganizationArtifact()
package nstic.web

class Documents {

    static belongsTo = [organization: Organization]

    int id
    String filename
    String url
    String publicUrl
    String documentCategory
    String description
    BinaryObject binaryObject
    Date dateCreated
    Boolean publicDocument = Boolean.TRUE

    static constraints = {
        filename(nullable: false, blank: false, maxSize: 255)
        url(nullable: false, blank: false, maxSize: 255)
        publicUrl(nullable: false, blank: false, maxSize: 255)
        documentCategory(nullable: false, blank: false, maxSize: 255)
        description(nullable: true, blank: false, maxSize: 65535)
        organization(nullable: false)
        binaryObject(nullable: false)
    }

    static mapping = {
        table(name:'documents')
        organization(column: 'organization_ref')
        binaryObject(column: 'binary_object_ref')
        filename(column: 'file_name')
        description(type:'text', column: 'description')
        publicDocument(nullable: false)
        //sort defaultDocument: "desc"
    }

}//end Documents

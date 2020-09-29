package nstic.web

/**
 * Represents any binary object in the system.
 */
class BinaryObject {
    String md5sum            // Checksum for the content for integrity purposes.
    String mimeType          // Ex, "image/png" or "application/zip"
    String originalFilename  // The original filename of the file, if given (may be null).
    String originalExtension // Ex. "png" or "zip"
    Long fileSize            // Size of the file in bytes
    Date dateCreated         // When the database entry was entered
    String createdBy         // The user responsible for this upload (may be null)

    BinaryData content       // The actual binary data.

    static constraints = {
        md5sum(nullable: true, maxSize: 512)
        mimeType(nullable: false, blank: false, maxSize: 128)
        originalFilename(nullable: true, blank: true, maxSize: 256)
        originalExtension(nullable: true, blank: true, maxSize: 32)
        fileSize(nullable: false)
        dateCreated(nullable: true)
        createdBy(nullable: true, blank: true, maxSize: 128)
        content(nullable: true) // Content isn't required to exist, but this object wouldn't make sense without it.
    }

    static mapping = {
        table(name:'binary_object')
        mimeType(column: 'mime_type')
        originalFilename(column: 'original_filename')
        originalExtension(column: 'original_extension')
    }

    public Map toJsonMap(boolean shallow = true){
        def json = [
            id: this.id,
            md5sum: this.md5sum,
            mimeType: this.mimeType,
            originalFilename: this.originalFilename,
            originalExtension: this.originalExtension,
            fileSize: this.fileSize,
            dateCreated: this.dateCreated?.getTime(),
            createdBy: this.createdBy,
            content: [
                id: this.content?.id
            ]
        ]
        return json;
    }//end toJsonMap

}//end BinaryObject

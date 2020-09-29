package nstic.web

class PublicDocument {
    String filename
    String url
    String description
    String documentCategory
    Date   dateCreated

    PublicDocument(String name, String url, String description, String category, Date createdDate)  {
        this.filename = name
        this.url = url
        this.description = description
        this.dateCreated = createdDate
        this.documentCategory = category
    }
}
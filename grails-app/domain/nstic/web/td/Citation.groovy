package nstic.web.td

class Citation {

    String source;
    String description;

    static constraints = {
        source(nullable: false, blank: false, maxSize: 65535)
        description(nullable: true, blank: true, maxSize: 65535)
    }

    static mapping = {
        table(name: 'td_citation')
        source(type: 'text')
        description(type: 'text')
    }


}
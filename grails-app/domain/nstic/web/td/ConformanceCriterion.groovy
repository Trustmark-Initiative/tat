package nstic.web.td

class ConformanceCriterion {

    static belongsTo = [trustmarkDefinition: TrustmarkDefinition]

    Integer critNumber;
    String name;
    String description;

    static hasMany = [citations: Citation]

    static constraints = {
        critNumber(nullable: false)
        name(nullable: false, blank: false, maxSize: 512)
        description(nullable: true, blank: true, maxSize: 65535)
        citations(nullable: true) // Although not really!
    }

    static mapping = {
        table(name:'td_criterion')
        trustmarkDefinition(column: 'td_ref')
        critNumber(column: 'number')
        description(type: 'text')
    }

}//end ConformanceCriterion
package nstic.web.td

import edu.gatech.gtri.trustmark.v1_0.model.ParameterKind
import edu.gatech.gtri.trustmark.v1_0.model.TrustmarkDefinitionParameter

/**
 * Created by brad on 6/3/16.
 */
class TdParameter implements Comparable<TdParameter>{

    public TdParameter(AssessmentStep step, TrustmarkDefinitionParameter param){
        this.assessmentStep = step;
        this.identifier = param.getIdentifier();
        this.name = param.getName();
        this.description = param.getDescription();
        this.kind = param.getParameterKind().toString();
        this.required = param.isRequired();
        if( param.getParameterKind() == ParameterKind.ENUM || param.getParameterKind() == ParameterKind.ENUM_MULTI ){
            this.addAllEnumValues(param.enumValues);
        }
    }

    static transients = ['sortedEnumValues']

    static belongsTo = [assessmentStep: AssessmentStep]

    String identifier;
    String name;
    String description;
    String kind;
    Boolean required;

    static hasMany = [enumValues: String]


    static constraints = {
        assessmentStep(nullable: false)
        identifier(nullable: false, blank: false, maxSize: 128)
        name(nullable: false, blank: false, maxSize: 128)
        description(nullable: true, blank: true, maxSize: 65535)
        kind(nullable: false, blank: false, maxSize: 64)
        required(nullable: false)
        enumValues(nullable: true)
    }

    static mapping = {
        table(name: 'trustmark_definition_parameter')
        description(type: 'text')
    }

    public List<String> getSortedEnumValues(){
        List<String> values = []
        values.addAll(this.getEnumValues() ?: []);
        Collections.sort(values, {s1, s2 -> return s1.compareToIgnoreCase(s2);} as Comparator);
        return values;
    }

    public void addAllEnumValues(Collection<String> values){
        if( enumValues == null )
            this.enumValues = []
        for( String val : values ){
            if( !this.enumValues.contains(val) ){
                this.enumValues.add(val);
            }
        }
    }

    public void addEnumValue( String val ){
        if( enumValues == null )
            this.enumValues = []
        if( !this.enumValues.contains(val) ){
            this.enumValues.add(val);
        }
    }

    public String toString(){
        return "TDParameter["+this.getName()+", "+this.getKind()+"]";
    }

    @Override
    int compareTo(TdParameter o) {
        if( o != null )
            return this.name.compareToIgnoreCase(o.name)
        return -1;
    }

    public Map toJsonMap(boolean shallow = false) {
        def json = [
                id         : this.id,
                identifier : this.identifier,
                name       : this.name,
                description: this.description,
                kind       : this.kind,
                required   : this.required
        ]
        if( this.enumValues != null && !this.enumValues.isEmpty() ){
            def enums = []
            for( String value : this.getSortedEnumValues() ){
                enums.add(value)
            }
            json.put("enumValues", enums);
        }
        return json;
    }


}

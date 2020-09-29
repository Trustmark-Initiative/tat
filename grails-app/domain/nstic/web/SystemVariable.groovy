package nstic.web

import nstic.SystemVariableDefinition;

class SystemVariable {
    //==================================================================================================================
    //  Public Methods
    //==================================================================================================================
    public static SystemVariable findBySystemVariableDefinition(SystemVariableDefinition varDef){
        return SystemVariable.findByName(varDef.getPropertyName());
    }

    public static String quickFindPropertyValue(SystemVariableDefinition varDef){
        String theVal = null;
        SystemVariable.withTransaction {
            SystemVariable prop = SystemVariable.findByName(varDef.getPropertyName());
            if( prop )
                theVal = prop.value;
        }
        return theVal;
    }

    public static String quickFindPropertyValue(String name){
        String theVal = null;
        SystemVariable.withTransaction {
            SystemVariable prop = SystemVariable.findByName(name);
            if( prop )
                theVal = prop.value;
        }
        return theVal;
    }


    public static void storeProperty(String name, Object value){
        SystemVariable.withTransaction {
            SystemVariable prop = SystemVariable.findByName(name);
            if( !prop )
                prop = new SystemVariable(name:name, fieldValue: value?.toString())
            else
                prop.fieldValue = value?.toString()
            prop.save(failOnError: true, flush: true)
        }
    }

    public static void deleteProperty(String name){
        SystemVariable.withTransaction {
            SystemVariable prop = SystemVariable.findByName(name);
            if( prop )
                prop.delete(flush: true)
        }
    }

    String name
    String fieldValue
    Date lastUpdated

    static constraints = {
        name(nullable: false, blank: false, maxSize: 254)
        fieldValue(nullable: true, blank: true, maxSize: 65535)
        lastUpdated(nullable: true)
    }

    static mapping = {
        table 'system_variable'
        name column: 'field_name'
        fieldValue type: 'text', column: 'field_value'
    }



    public String getValue(){
        return fieldValue;
    }
    public String getStringValue(){
        return getValue();
    }

    public Boolean getBooleanValue(){
        String value = this.getValue();
        if( value ){
            try{
                return Boolean.parseBoolean(value);
            }catch(Throwable t){
                log.error("Value '$value' is not a valid boolean, cannot coerce.", t);
                throw t;
            }
        }else{
            return null;
        }
    }

    public Number getNumericValue(){
        String value = this.getValue();
        if( value ){
            try{
                return Double.parseDouble(value);
            }catch(Throwable t){
                log.error("Value '$value' is not a valid number, cannot coerce.", t);
                throw t;
            }
        }else{
            return null;
        }
    }


}//end Property

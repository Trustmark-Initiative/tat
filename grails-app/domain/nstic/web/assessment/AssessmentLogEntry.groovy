package nstic.web.assessment

import grails.converters.JSON
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.apache.commons.lang.StringUtils

class AssessmentLogEntry {

    //==================================================================================================================
    // Domain Object Definition
    //==================================================================================================================
    static transients = ['data', 'dataAsJson']

    static belongsTo = [
            logg: AssessmentLog
    ]

    Date dateCreated    // When the message was created (auto-populated)
    String title        // Quick summary of message contents
    String type         // Type of message
    String message      // Message text
    String jsonVal         // JSON data related to this log entry.

    static constraints = {
        logg(nullable: false)
        dateCreated(nullable: true)
        title(nullable: false, blank: false, maxSize: 512)
        message(nullable: false, blank: false, maxSize: 65535)
        jsonVal(nullable: false, blank: false, maxSize: 65535)
    }

    static mapping = {
        table(name: 'assessment_log_entry')
        logg(column: 'assessment_log_ref')
        message(type: 'text')
        jsonVal(type: 'text')
    }


    //==================================================================================================================
    // Constructors
    //==================================================================================================================
    public AssessmentLogEntry(){}
    public AssessmentLogEntry(AssessmentLog log){
        this.logg = log;
    }
    public AssessmentLogEntry(AssessmentLog log, String type, String title, String message){
        this.logg = log;
        this.type = type;
        this.title = title;
        this.message = message;
    }
    public AssessmentLogEntry(AssessmentLog log, String type, String title, String message, Map dataMap){
        this.logg = log;
        this.type = type;
        this.title = title;
        this.message = message;
        this.setData(dataMap);
    }
    public AssessmentLogEntry(AssessmentLog log, String type, String title, String message, String dataStr){
        this.logg = log;
        this.type = type;
        this.title = title;
        this.message = message;
        this.setData(dataStr);
    }


    //==================================================================================================================
    // Methods
    //==================================================================================================================
    public void setData(String dataStr){
        if(StringUtils.isBlank(dataStr) )
            dataStr = "{\"empty\": true}";
        this.jsonVal = dataStr;
    }

    public void setData(Map dataMap){
        if( dataMap == null )
            dataMap = [empty: true]
        this.jsonVal = JsonOutput.toJson(dataMap);
    }

    public String getData(){
        return this.jsonVal;
    }

    /**
     * Returns the data as a JSON object.
     */
    def getDataAsJson() {
        def slurper = new JsonSlurper()
        return slurper.parseText(this.jsonVal ?: [empty: true]);
    }//end getDataAsJson()

    public Map toJsonMap(boolean shallow = true) {
        def json = [
                id: this.id,
                assessmentLog: [id: this.logg.id],
                dateCreated: this.dateCreated,
                title: this.title,
                type: this.type,
                message: this.message
        ]

        if( !shallow ) {
            json.put("data", this.jsonVal);
        }

        return json;
    }


}//end AssessmentLogEntry

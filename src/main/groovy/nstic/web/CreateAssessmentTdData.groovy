package nstic.web

import org.json.JSONObject

/**
 * Contains information about a TD that is to be used when creating an Assessment.  This object is held in session scope
 * and cannot hold Hibernate GORM objects.
 * <br/><br/>
 * Created by brad on 4/26/16.
 */
class CreateAssessmentTdData implements Serializable {

    /**
     * The identifier for this TD.
     */
    String uri;

    Long databaseId
    String name;
    String version;
    String description;

    /**
     * The param which identified this TD as being necessary.
     */
    String paramName;



    public JSONObject toJSON(){
        JSONObject obj = new JSONObject();
        obj.put("uri", this.uri);
        obj.put("databaseId", this.databaseId);
        obj.put("name", this.name);
        obj.put("version", this.version);
        obj.put("description", this.description);
        obj.put("paramName", this.paramName);
        return obj;
    }


    public static CreateAssessmentTdData fromJSON(JSONObject json){
        CreateAssessmentTdData td = new CreateAssessmentTdData();
        td.setUri(json.optString("uri"));
        td.setDatabaseId(json.optLong("databaseId"));
        td.setName(json.optString("name"));
        td.setVersion(json.optString("version"));
        td.setDescription(json.optString("description"));
        td.setParamName(json.optString("paramName"));
        return td;
    }

}

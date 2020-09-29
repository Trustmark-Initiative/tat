package nstic.web

import nstic.util.TipTreeNode
import nstic.web.td.TrustmarkDefinition
import nstic.web.tip.TrustInteroperabilityProfile
import org.json.JSONArray
import org.json.JSONObject

/**
 * Contains information about a TIP that is to be used when creating an Assessment.  This object is held in session scope
 * and cannot hold Hibernate GORM objects.
 * <br/><br/>
 * Created by brad on 4/26/16.
 */
class CreateAssessmentTIPData implements Serializable {

    /**
     * The identifier for this TIP.
     */
    String uri;
    Long databaseId
    String name;
    String version;
    String description;

    /**
     * Once the user has selected TDs from the TIP, the ones selected will go in here.
     */
    List<String> tdUris;

    /**
     * If true, then we should disregard the value of tdUris and instead include ALL Tds found within this TIP.
     */
    Boolean useAllTds;

    /**
     * When true, the TIPData has been processed by the user and contains information necessary to create an Assessment.
     * That doesn't mean that it's exactly valid.
     */
    Boolean processed = Boolean.FALSE;

    List<String> getTdUris()  {
        if(tdUris == null)
            tdUris = new ArrayList<>();
        return tdUris
    }

    public JSONObject toJSON(){
        JSONObject obj = new JSONObject();
        obj.put("uri", this.uri);
        obj.put("databaseId", this.databaseId);
        obj.put("name", this.name);
        obj.put("version", this.version);
        obj.put("description", this.description);
        obj.put("useAllTds", this.useAllTds);
        obj.put("processed", this.processed);
        JSONArray tdUris = new JSONArray();
        if( this.tdUris != null && this.tdUris.size() > 0 ){
            for( String uri : this.tdUris ){
                tdUris.put(uri);
            }
        }
        obj.put("tdUris", tdUris);
        return obj;
    }


    public static CreateAssessmentTIPData fromJSON(JSONObject json){
        CreateAssessmentTIPData tip = new CreateAssessmentTIPData();
        tip.setUri(json.optString("uri"));
        tip.setDatabaseId(json.optLong("databaseId"));
        tip.setName(json.optString("name"));
        tip.setVersion(json.optString("version"));
        tip.setDescription(json.optString("description"));
        tip.setUseAllTds(json.optBoolean("useAllTds"));
        tip.setProcessed(json.optBoolean("processed"));
        JSONArray tdUrisArray = json.optJSONArray("tdUris");
        tip.setTdUris([]);
        if( tdUrisArray != null & tdUrisArray.length() > 0 ){
            for( int i = 0; i < tdUrisArray.length(); i++ ){
                tip.getTdUris().add(tdUrisArray.optString(i));
            }
        }
        return tip;
    }

    public List<TrustmarkDefinition> getAllPotentialTds() {
        Set<TrustmarkDefinition> tds = []
        Queue<TipTreeNode> q = new ArrayDeque<>();
        def dbTip = TrustInteroperabilityProfile.findByUri(this.uri);
        def tipTreeRoot = TipTreeNode.getTreeRecursively(dbTip);
        q.add(tipTreeRoot)
        while (!q.isEmpty()) {
            def node = q.poll()
            def currentTds = node.trustmarkDefinitionReferences.collect{ TrustmarkDefinition.findById(it.databaseId as int) }
            tds.addAll(currentTds)
            q.addAll(node.children)
        }
        return tds.toList()
    }


}

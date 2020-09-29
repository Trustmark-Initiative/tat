package nstic.web

import org.json.JSONArray
import org.json.JSONObject


/**
 * Contains information stored in the session which is used to help create assessments.  The information deals with the
 * subject of the assessment (multiple TDs/TIPs, along with which TDs from the TIPs).
 * <br/><br/>
 * Created by brad on 4/26/16.
 */
class CreateAssessmentTdsAndTips implements Serializable {

    /**
     * A list of CreateAssessmentTdData objects which will be assessed.
     */
    public List<CreateAssessmentTdData> trustmarkDefinitions = [];

    /**
     * The list of TIPs which are ready to be processed.
     */
    public List<CreateAssessmentTIPData> trustInteroperabilityProfiles = [];

    /**
     * If true, then it indicates there is a TIP which needs TD resolution performed (the user must select from the
     * set of TDs in that TIP to assess).
     */
    boolean containsTipWhichNeedsTdResolution(){
        return getNextTipResolutionId() != null;
    }

    /**
     * Returns the TIP database identifier which needs to be resolved next.
     */
    Long getNextTipResolutionId(){
        Long id = null;
        if( trustInteroperabilityProfiles != null && !trustInteroperabilityProfiles.isEmpty() ){
            for( CreateAssessmentTIPData tipData : trustInteroperabilityProfiles ) {
                if( tipData.processed == false ) {
                    id = tipData.databaseId;
                    break;
                }
            }
        }
        return id;
    }

    CreateAssessmentTIPData getTipData(Long id){
        CreateAssessmentTIPData tipData = null;
        if( trustInteroperabilityProfiles != null && !trustInteroperabilityProfiles.isEmpty() ){
            for( CreateAssessmentTIPData cur : trustInteroperabilityProfiles ) {
                if( cur.databaseId == id ) {
                    tipData = cur;
                    break;
                }
            }
        }
        return tipData;
    }


    List<CreateAssessmentTIPData> getAllTipsNeedingResolutionExcept(Long id){
        List<CreateAssessmentTIPData> tips = []
        if( trustInteroperabilityProfiles != null && !trustInteroperabilityProfiles.isEmpty() ){
            for( CreateAssessmentTIPData cur : trustInteroperabilityProfiles ) {
                if( cur.databaseId != id && cur.processed == false )
                    tips.add(cur);
            }
        }
        return tips;
    }


    JSONObject toJSON(){
        JSONObject obj = new JSONObject();
        obj.put("timestamp", System.currentTimeMillis());

        JSONArray tds = new JSONArray();
        if( this.trustmarkDefinitions != null && this.trustmarkDefinitions.size() > 0 ){
            for( CreateAssessmentTdData tdData : this.trustmarkDefinitions ){
                tds.put(tdData.toJSON());
            }
        }
        obj.put("tds", tds);

        JSONArray tips = new JSONArray();
        if( this.trustInteroperabilityProfiles != null && this.trustInteroperabilityProfiles.size() > 0 ){
            for( CreateAssessmentTIPData tipData : this.trustInteroperabilityProfiles ){
                tips.put(tipData.toJSON());
            }
        }
        obj.put("tips", tips);

        return obj;
    }

    public static CreateAssessmentTdsAndTips fromJSON(String json){
        JSONObject jsonObject = new JSONObject(json);
        return fromJSON(jsonObject);
    }
    public static CreateAssessmentTdsAndTips fromJSON(JSONObject json){
        CreateAssessmentTdsAndTips tdsAndTips = new CreateAssessmentTdsAndTips();

        JSONArray tdObjs = json.optJSONArray("tds");
        if( tdObjs != null && tdObjs.length() > 0 ){
            for( int i = 0; i < tdObjs.length(); i++ ){
                JSONObject tdObj = tdObjs.get(i);
                tdsAndTips.trustmarkDefinitions.add( CreateAssessmentTdData.fromJSON(tdObj) );
            }
        }

        JSONArray tipObjs = json.optJSONArray("tips");
        if( tipObjs != null && tipObjs.length() > 0 ){
            for( int i = 0; i < tipObjs.length(); i++ ){
                JSONObject tipObj = tipObjs.get(i);
                tdsAndTips.trustInteroperabilityProfiles.add( CreateAssessmentTIPData.fromJSON(tipObj) );
            }
        }

        return tdsAndTips;
    }

}/* end CreateAssessmentTdsAndTips */
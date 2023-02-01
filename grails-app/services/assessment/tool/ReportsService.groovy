package nstic.web


import grails.gorm.transactions.Transactional
import groovy.json.JsonBuilder
import nstic.web.assessment.Assessment
import nstic.web.assessment.AssessmentStepData
import nstic.web.assessment.AssessmentStepResult
import nstic.web.td.TrustmarkDefinition
import org.apache.commons.lang.StringUtils
import org.grails.web.json.JSONException
import org.grails.web.json.JSONObject
import org.hibernate.SQLQuery
import org.hibernate.Session

import grails.gsp.PageRenderer

import java.util.concurrent.ConcurrentHashMap

public class DualKey {
    private final String key1;
    private final String key2;

    public DualKey(String key1, String key2) {
        this.key1 = key1;
        this.key2 = key2;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DualKey))
            return false;
        DualKey key = (DualKey) o;
        return key1.equals(key.key1) && key2.equals(key.key2);
    }

    @Override
    public int hashCode() {
        int result = key1.length();
        result = 31 * result + key2.length();
        return result;
    }
}

public class DualKeyConcurrentHashMap<K, V>  {
    private final ConcurrentHashMap<K, V> map = new ConcurrentHashMap<>();

    public V put(DualKey key, V value) {

        return map.put(key, value);
    }

    public V get(DualKey k) {
        return map.get(k);
    }

    public V remove(DualKey k) {
        return map.remove(k);
    }

    public boolean containsKey(DualKey k) {
        return map.containsKey(key);
    }
    
    public void clear() {
        map.clear();
    }
}


@Transactional(readOnly = true)
class ReportsService {

    DualKeyConcurrentHashMap<DualKey, Object> objectMap = new DualKeyConcurrentHashMap<DualKey, Object>()

    // execution
    public static final String ORGANIZATION_REPORT_EXECUTING_VAR = ReportsService.class.simpleName + ".ORGANIZATION_REPORT_EXECUTING"

    // messaging
    public static final String ORGANIZATION_REPORT_STATUS_VAR = ReportsService.class.getName() + ".ORGANIZATION_REPORT_STATUS"
    public static final String ORGANIZATION_REPORT_PERCENT_VAR = ReportsService.class.getName() + ".ORGANIZATION_REPORT_PERCENT"
    public static final String ORGANIZATION_REPORT_MESSAGE_VAR = ReportsService.class.getName() + ".ORGANIZATION_REPORT_MESSAGE"

    public static final String ORGANIZATION_REPORT_THREAD_VAR = ReportsService.class.getName() + ".ORGANIZATION_REPORT_THREAD"

    public static final String ORGANIZATION_REPORT_RENDER_MODEL_VAR = ReportsService.class.getName() + ".ORGANIZATION_REPORT_RENDER_MODEL"

    def sessionFactory;

    PageRenderer groovyPageRenderer

    void setAttribute(String key1, String key2, Object value) {
        objectMap.put(new DualKey(key1, key2), value)
    }

    Object getAttribute(String key1, String key2) {
        return objectMap.get(new DualKey(key1, key2))
    }

    void removeAttribute(String key1, String key2) {
        objectMap.remove(new DualKey(key1, key2))
    }

    void resetAttributes() {
        objectMap.clear()
    }

    boolean isExecuting(String key, String property) {
        String value = getAttribute(key, property)
        if (StringUtils.isBlank(value)) {
            value = "false"
        }

        return Boolean.parseBoolean(value);
    }

    void setExecuting(String key, String property) {
        setAttribute(key, property, "true")
    }

    void stopExecuting(String key, String property) {
        setAttribute(key, property, "false");
    }

    void organizationReport(Map paramsMap) {
        log.debug("Processing organization report form...");


        String sessionId = paramsMap.get("sessionId")

        Organization organization = paramsMap["organization"]

        setAttribute(sessionId, ReportsService.ORGANIZATION_REPORT_STATUS_VAR, "RUNNING")
        setAttribute(sessionId, ReportsService.ORGANIZATION_REPORT_MESSAGE_VAR, "Preprocessing report for the ${organization.name} organization...")

        Date startDate = paramsMap["startDate"]
        Date endDate = paramsMap["endDate"]

        boolean hideCompletedAssessments = paramsMap["hideCompletedAssessments"]
        boolean hideCompletedSteps = paramsMap["hideCompletedSteps"]

        OrganizationReportCommand command = new OrganizationReportCommand(
                organization,
                startDate,
                endDate,
                hideCompletedAssessments,
                hideCompletedSteps
        )

        if (command.hasErrors()) {
            log.warn("Errors detected in form submission!");
            groovyPageRenderer.render(view: '/reports/organization/form', model: [command: command]);
        }

        def charts = [:]

        log.debug("Iterating all assessments...");

        // getting assessments within the date range
        setAttribute(sessionId, ReportsService.ORGANIZATION_REPORT_MESSAGE_VAR, "Getting assessments within the date range...")
        def assessments = getAssessmentsInDateRange(startDate, endDate);

        List<Assessment> thisOrgsAssessments = assessments.findAll{it.getAssessedOrganization() == command.organization}

        Map<Long, CreateAssessmentTdsAndTips> tdsAndTipsByAssessmentId = [:]
        Map<Long, Map<Long, TipInfoCollection>> tipInfoByTipIdByAssessmentId = [:]
        Map<Long, Integer> fullySatisfiedTipCountByAssessmentId = [:]
        Map<Long, Integer> fullySatisfiedTdCountByAssessmentId = [:]

        try {
            log.info("thisOrgsAssessments.size(): ${thisOrgsAssessments.size()}")

            // processing assessments
            for (Assessment assessment : thisOrgsAssessments) {

                // td and tip data
                setAttribute(sessionId, ReportsService.ORGANIZATION_REPORT_MESSAGE_VAR, "Preprocessing TD and TIP data for assessment: ${assessment.assessmentName}...")

                def tdsAndTips = CreateAssessmentTdsAndTips.fromJSON(assessment.tdsAndTipsJSON)
                tdsAndTipsByAssessmentId[assessment.id] = tdsAndTips
                tipInfoByTipIdByAssessmentId[assessment.id] = [:]
                int fullySatisfiedTipCount = 0

                //## processing TIPs for assessment x
                setAttribute(sessionId, ReportsService.ORGANIZATION_REPORT_MESSAGE_VAR, "Processing TIPs for assessment: ${assessment.assessmentName}...")

                int currentTIPDataIndex = 0
                for (CreateAssessmentTIPData tipData : tdsAndTips.trustInteroperabilityProfiles) {
                    def tipInfo = new TipInfoCollection()
                    tipInfoByTipIdByAssessmentId[assessment.id][tipData.databaseId] = tipInfo
                    tipInfo.allPotentialTds = tipData.getAllPotentialTds()

                    tipInfo.chosenTds = tipData.useAllTds ? tipInfo.allPotentialTds : tipData.tdUris.collect { TrustmarkDefinition.findByUri(it) }

                    List<AssessmentStepData> chosenTdSteps = tipInfo.chosenTds.collectMany { assessment.getStepListByTrustmarkDefinition(it) }

                    tipInfo.chosenTdStepsByResult = chosenTdSteps.groupBy { it.result }
                    def chosenTdStepSatisfiedCount = tipInfo.getChosenTdStepResultCount(AssessmentStepResult.Satisfied)
                    def chosenTdStepApplicableCount = tipInfo.applicableTdStepCount
                    if (chosenTdStepSatisfiedCount == chosenTdStepApplicableCount) {
                        ++fullySatisfiedTipCount
                    }

                    // update progress percentage
                    int percent = (int) Math.floor(((double) currentTIPDataIndex++ / (double) tdsAndTips.trustInteroperabilityProfiles.size()) * 100.0d)
                    setAttribute(sessionId, ReportsService.ORGANIZATION_REPORT_PERCENT_VAR, "" + percent)
                }

                fullySatisfiedTipCountByAssessmentId[assessment.id] = fullySatisfiedTipCount

                int fullySatisfiedTdCount = 0;
                def tds = assessment.tdLinks.collect { it.trustmarkDefinition }

                //## processing TDs for assessment x
                setAttribute(sessionId, ReportsService.ORGANIZATION_REPORT_MESSAGE_VAR, "Processing TDs for assessment: ${assessment.assessmentName}...")

                log.info("TDs size: ${tds.size()}")

                int currentTDIndex = 0
                for (TrustmarkDefinition td : tds) {
                    def steps = assessment.getStepListByTrustmarkDefinition(td)
                    def stepSatisfiedCount = steps.count { it.result == AssessmentStepResult.Satisfied }
                    def stepNotApplicableCount = steps.count { it.result == AssessmentStepResult.Not_Applicable }
                    def stepApplicableCount = steps.size() - stepNotApplicableCount
                    if (stepSatisfiedCount == stepApplicableCount) {
                        ++fullySatisfiedTdCount
                    }

                    // update progress percentage
                    int percent = (int) Math.floor(((double) currentTDIndex++ / (double) tds.size()) * 100.0d)
                    setAttribute(sessionId, ReportsService.ORGANIZATION_REPORT_PERCENT_VAR, "" + percent)
                }
                fullySatisfiedTdCountByAssessmentId[assessment.id] = fullySatisfiedTdCount

            } // end for each assessment

            def assessmentsIds = []
            thisOrgsAssessments.each {assessment ->
                assessmentsIds.add(assessment.id)
            }

            JSONObject tipInfoByTipIdByAssessmentIdJson = tipInfoByTipIdByAssessmentIdToJson(tipInfoByTipIdByAssessmentId)

            JSONObject tdsAndTipsByAssessmentIdJson = tdsAndTipsByAssessmentIdToJson(tdsAndTipsByAssessmentId)

            def result = [
                    startDate                           : command.startDate.getTime(),
                    endDate                             : command.endDate.getTime(),
                    assessmentIds                       : assessmentsIds,
                    tdsAndTipsByAssessmentId            : tdsAndTipsByAssessmentIdJson.toString(),
                    tipInfoByTipIdByAssessmentId        : tipInfoByTipIdByAssessmentIdJson.toString(),
                    fullySatisfiedTipCountByAssessmentId: fullySatisfiedTipCountByAssessmentId,
                    fullySatisfiedTdCountByAssessmentId : fullySatisfiedTdCountByAssessmentId,
                    organizationId                      : command.organization.id,
                    hideCompletedAssessments            : command.hideCompletedAssessments,
                    hideCompletedSteps                  : command.hideCompletedSteps
            ]

            // store in service attributes
            JsonBuilder jsonBuilder = new JsonBuilder(result)
            setAttribute(sessionId, ReportsService.ORGANIZATION_REPORT_RENDER_MODEL_VAR, jsonBuilder.toString())

            stopExecuting(sessionId, ReportsService.ORGANIZATION_REPORT_EXECUTING_VAR)
            setAttribute(sessionId, ReportsService.ORGANIZATION_REPORT_STATUS_VAR, "SUCCESS")
            setAttribute(sessionId, ReportsService.ORGANIZATION_REPORT_MESSAGE_VAR, "Finished generating the report for organization ${organization.name}!")

        } catch (Throwable t) {
            log.error("An unexpected error occurred trying to generate the organization's report: " + t.toString(), t)

            stopExecuting(sessionId, ReportsService.ORGANIZATION_REPORT_EXECUTING_VAR)
            setAttribute(sessionId, ReportsService.ORGANIZATION_REPORT_STATUS_VAR, "ERROR")
            setAttribute(sessionId, ReportsService.ORGANIZATION_REPORT_MESSAGE_VAR, "An error has ocurred. Please check the logs...")
        }

    } // end organizationReport()

    public List<Assessment> getAssessmentsInDateRange(Date startDate, Date endDate){

        log.debug("Executing native SQL query to find all assessments valid to the date range ${startDate.toString()} to ${endDate.toString()}...");
        final Session session = sessionFactory.currentSession
        String queryString = """
select distinct assId from (
  SELECT a.id as assId, entry.id as entryId
  FROM assessment a, assessment_log log, assessment_log_entry entry
  WHERE
    a.assessment_log_ref = log.id
      and
    entry.assessment_log_ref = log.id
      and
    entry.date_created >= :startDate and entry.date_created <= :endDate
  ORDER BY entry.date_created asc
) as tempTable
"""
        final SQLQuery sqlQuery = session.createSQLQuery(queryString)
        sqlQuery.setDate("startDate", startDate);
        sqlQuery.setDate("endDate", endDate);
        def assessmentIds = []
        sqlQuery.list().each{ result ->
            if( result instanceof BigInteger ){
                assessmentIds.add( ((BigInteger) result).longValue() );
            }else {
                log.debug("Assessment Id[type=${result?.class.name}]: $result")
                assessmentIds.add(result);
            }
        }

        return Assessment.executeQuery("from Assessment a where a.id in :idList", [idList: assessmentIds]);
    }

//    Map<Long, Map<Long, TipInfoCollection>> tipInfoByTipIdByAssessmentId = [:]
    private JSONObject tipInfoByTipIdByAssessmentIdToJson(Map<Long, Object> map) throws JSONException {
        JSONObject jsonData = new JSONObject()
        for (Long key : map.keySet()) {
            Object value = map.get(key)
            JSONObject internalJsonData = new JSONObject()
            for (Long internalKey : value.keySet()) {
                Object internalValue = value.get(internalKey)
                internalJsonData.put(internalKey.toString(), internalValue.toJSON())
            }

            jsonData.put(key.toString(), internalJsonData)
        }
        return jsonData;
    }

//    Map<Long, CreateAssessmentTdsAndTips> tdsAndTipsByAssessmentId = [:]
    private JSONObject tdsAndTipsByAssessmentIdToJson(Map<Long, Object> map) throws JSONException {
        JSONObject jsonData = new JSONObject()
        for (Long key : map.keySet()) {
            Object value = map.get(key)

            jsonData.put(key.toString(), value.toJSON())
        }
        return jsonData;
    }

    private void addToListEntry(map, key, value){
        def list = []
//        log.debug("Adding list entry key[$key]=value[$value]")
        if( map.containsKey(key) ){
            list = map.get(key);
        }
        list.add(value);
        map.put(key, list);
    }

}//end ReportsService

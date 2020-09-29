package nstic.web

import com.googlecode.charts4j.BarChart
import com.googlecode.charts4j.Color
import com.googlecode.charts4j.Data
import com.googlecode.charts4j.GCharts
import com.googlecode.charts4j.PieChart
import com.googlecode.charts4j.Plots
import com.googlecode.charts4j.Slice
import grails.plugin.springsecurity.annotation.Secured
import grails.validation.Validateable
import nstic.assessment.ColorPalette
import nstic.web.assessment.Assessment
import nstic.web.assessment.AssessmentStatus
import nstic.web.assessment.AssessmentStepData
import nstic.web.assessment.AssessmentStepResult
import nstic.web.assessment.AssessmentTrustmarkDefinitionLink
import nstic.web.assessment.Trustmark
import nstic.web.td.TrustmarkDefinition
import nstic.web.tip.TrustInteroperabilityProfile
import org.hibernate.SQLQuery
import org.hibernate.Session

import javax.servlet.ServletException

@Secured("ROLE_USER")
class ReportsController {

    def sessionFactory;
    def springSecurityService;
    def mailService;

    /**
     * Displays the listing of reports.
     */
    def index() {
        log.debug("Loading report listing...");
    }//end index()

    @Secured(["ROLE_REPORTS_ONLY", "ROLE_USER"])
    def share() {
        User user = springSecurityService.currentUser;
        log.debug("User[${user.username}] sharing report...")
        if( request.method.toUpperCase() == 'GET' ) {
            log.debug("Displaying form...");

        }else if( request.method.toUpperCase() == 'POST' ){
            log.debug("Processing form...");

            log.debug("Parameters: ")
            request.parameterNames.each{ paramName ->
                String paramValue = request.getParameter(paramName);
                log.debug("  [$paramName] = [$paramValue]")
            }

            Organization org = Organization.get(params.organization);
            if( !org )
                throw new ServletException("Missing required param 'organization' ")

            if( user.isReportOnly() && !user.isUser() ){
                if( org != user.organization &&
                        org.primaryContact != user.contactInformation &&
                        !org.contacts.contains(user.contactInformation) ){
                    log.error("User[$user] tried to grant permission to org[${org.id}], which they don't have access to.")
                    throw new ServletException("You do not have permission to invite users to organization ${org.id}")
                }
            }

            Organization.withTransaction {
                ContactInformation contact = null;
                if (params.contactType == "existing") {
                    contact = ContactInformation.get(params.existingContactId);
                    if (!contact)
                        throw new ServletException("Invalid contact: ${params.existingContactId}")

                    if (!org.contacts.contains(contact) && !org.primaryContact.equals(contact)) {
                        org.addToContacts(contact);
                        org.save(failOnError: true);
                    }
                } else {
                    contact = ContactInformation.findByEmailIlike(params.contactEmail);
                    if (contact) {
                        log.warn("Contact email @|cyan ${params.contactEmail}|@ already exists (id: @|green ${contact.id}|@), using that one...");
                    } else {
                        contact = new ContactInformation();
                        contact.responder = params.contactResponder
                        contact.email = params.contactEmail
                        contact.mailingAddress = params.contactMailingAddress
                        contact.phoneNumber = params.contactPhoneNumber
                        contact.notes = params.contactNotes
                        contact.save(failOnError: true)
                    }

                    org.addToContacts(contact);
                    org.save(failOnError: true);
                }

                log.debug("Sending welcome email to Email[${contact.email}]...")
                String nextUniqueId = generateNextUniqueId();
                while (ContactGrant.findByGrantId(nextUniqueId) != null) {
                    nextUniqueId = generateNextUniqueId();
                }
                ContactGrant contactGrant = new ContactGrant()
                contactGrant.grantId = nextUniqueId;
                contactGrant.contactInformation = contact;
                contactGrant.organization = org;
                contactGrant.createdBy = user;
                contactGrant.save(failOnError: true);
            }



            mailService.sendMail {
                async true
                to contact.email
                from 'TrustmarkFeedback@gtri.gatech.edu'
                subject "Trustmark Assessment Reports"
                html g.render(template: "/templates/newUserReportsGrant", model: [contactGrant: contactGrant])
            }

            mailService.sendMail {
                async true
                to 'TrustmarkFeedback@gtri.gatech.edu'
                subject "Trustmark Assessment Reports Grant"
                body "User[${user.username}] has granted access to org ${org.name} reports to ${contact.email}"
            }


            flash.message = "Successfully shared reports!";
        }
        [user: user]
    }

    private static String UNIQUE_KEY_ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
    private static Integer UNIQUE_KEY_ALPHABET_LENGTH = UNIQUE_KEY_ALPHABET.length();
    private static Random RANDOM = new Random(System.currentTimeMillis());
    private String generateNextUniqueId() {
        int len = RANDOM.nextInt(3) + 6;
        StringBuilder builder = new StringBuilder();
        for( int i = 0; i < len; i++ ){
            char next = UNIQUE_KEY_ALPHABET.charAt(RANDOM.nextInt(UNIQUE_KEY_ALPHABET_LENGTH));
            builder.append(next);
        }
        return builder.toString();
    }
    //------------------------------------------------------------------------------------------------------------------
    //  Overall Report
    //------------------------------------------------------------------------------------------------------------------
    /**
     * This is the overall link to perform the overall report
     */
    def overallReport(OverallReportCommand command) {
        User user = springSecurityService.currentUser;
        log.info("User[${user}] hitting overall report...")


        if( request.getMethod().equalsIgnoreCase("GET") ){
            log.debug("Showing a new overall report form...");
            return render(view: '/reports/overall/form', model: [command: new OverallReportCommand()]);
        }else if( request.getMethod().equalsIgnoreCase("POST") ){
            log.debug("Processing a new overall report form...");

            if( command.hasErrors() ){
                log.warn("Errors detected in form submission!");
                return render(view: '/reports/overall/form', model: [command: command]);
            }

            log.info("Performing overall report from ${command.startDate} to ${command.endDate}...")

            def organizations = []
            def trustmarkDefinitions = []
            def orgAssessmentMap = [:];
            def orgTrustmarkMap = [:];
            def tdTrustmarkMap = [:];
            def tdAssessmentMap = [:];

            def assessmentStats = [
                overallCount:0
            ]
            AssessmentStatus.values().each { status -> assessmentStats[status.toString()] = 0 }


            log.debug("Iterating all assessments...");
            def assessments = getAssessmentsInDateRange(command.startDate, command.endDate);
            for( Assessment assessment : assessments ){
                Organization org = assessment.getAssessedOrganization();
                if( !organizations.contains(org) )
                    organizations.add(org);

                for(AssessmentTrustmarkDefinitionLink link : assessment.getTdLinks()) {
                    TrustmarkDefinition td = link.getTrustmarkDefinition();
                    if (!trustmarkDefinitions.contains(td))
                        trustmarkDefinitions.add(td);

                    addToListEntry(tdAssessmentMap, td, assessment);
                }

                for( Trustmark tm : Trustmark.findAllByAssessment(assessment) ) {
                    addToListEntry(orgTrustmarkMap, org, tm);
                }

                assessmentStats['overallCount'] = assessmentStats['overallCount'] + 1;
                assessmentStats[assessment.status.toString()] = assessmentStats[assessment.status.toString()] + 1;

                addToListEntry(orgAssessmentMap, org, assessment);

            }

            def charts = [:]

            log.debug("Generating pie chart for overall status...")
            def slices = []
            assessmentStats.keySet().each{ status ->
                if( status != "overallCount" ){
                    int currentCount = assessmentStats[status]
                    double percent = (currentCount / assessmentStats['overallCount']) * 100.0d;
                    Slice slice = Slice.newSlice((int) percent, status + " ("+currentCount+")");
                    slices.add(slice);
                }
            }
            PieChart statusChart = GCharts.newPieChart(slices);

            // charts4j uses the chart.apis.google.com default endpoint which does not support HTTPS, so using Chrome,
            // all HTTP requests will be redirected to HTTPS. Instead, use the endpoint chart.googleapis.com/chart which
            // does support HTTPS thus preventing redirection.
            statusChart.setURLEndpoint("https://chart.googleapis.com/chart");

            statusChart.setSize(600, 200);
            statusChart.setTitle("Assessment Status Distribution (of ${assessmentStats['overallCount']})")
            charts.put('statusChart', statusChart)

            Collections.sort(trustmarkDefinitions, {td1, td2 -> return td1.name.compareToIgnoreCase(td2.name); } as Comparator);

            for( TrustmarkDefinition td : trustmarkDefinitions ){
                for( Trustmark tm : Trustmark.findAllByTrustmarkDefinition(td) ) {
                    addToListEntry(tdTrustmarkMap, td, tm);
                }
            }


            log.debug("Rendering the HTML from report model...");
            return render(
                    view: '/reports/overall/view',
                    model: [
                            startDate: command.startDate,
                            endDate: command.endDate,
                            assessments: assessments,
                            orgAssessmentMap: orgAssessmentMap,
                            organizations: organizations,
                            trustmarkDefinitions: trustmarkDefinitions,
                            orgAssessmentMap: orgAssessmentMap,
                            orgTrustmarkMap: orgTrustmarkMap,
                            tdAssessmentMap: tdAssessmentMap,
                            tdTrustmarkMap: tdTrustmarkMap,
                            assessmentStats: assessmentStats,
                            charts: charts
                    ]
            );
        }

    }//end overallReport()
    //------------------------------------------------------------------------------------------------------------------
    //  Organization Report
    //------------------------------------------------------------------------------------------------------------------
    /**
     * Shows the user a form for picking the organizational report.
     */
    @Secured(["ROLE_REPORTS_ONLY", "ROLE_USER"])
    def organizationReport(OrganizationReportCommand command) {
        User user = springSecurityService.currentUser;
        log.info("User[${user}] hitting org report...")

        if( request.getMethod().equalsIgnoreCase("GET") ){
            log.debug("Showing organization report form...");
            return render(view: '/reports/organization/form', model: [user: user, command: new OrganizationReportCommand()]);
        }else if( request.getMethod().equalsIgnoreCase("POST") ) {
            log.debug("Processing organization report form...");

            if( user.isReportOnly() && !user.isUser() ){
                if( command.organization != user.organization &&
                        command.organization.primaryContact != user.contactInformation &&
                        !command.organization.contacts.contains(user.contactInformation) ){
                    log.error("User[${user.username}] trying to run report for another organization: ${command.organization.uri}")
                    throw new ServletException("Not Authorized.  You are not allowed to perform reports for any other organization.")
                }
            }

            if( command.hasErrors() ){
                log.warn("Errors detected in form submission!");
                return render(view: '/reports/organization/form', model: [command: command]);
            }

            def charts = [:]

            log.debug("Iterating all assessments...");
            def assessments = getAssessmentsInDateRange(command.startDate, command.endDate);
            List<Assessment> thisOrgsAssessments = assessments.findAll{it.getAssessedOrganization() == command.organization}
            Map<Long, CreateAssessmentTdsAndTips> tdsAndTipsByAssessmentId = [:]
            Map<Long, Map<Long, TipInfoCollection>> tipInfoByTipIdByAssessmentId = [:]
            Map<Long, Integer> fullySatisfiedTipCountByAssessmentId = [:]
            Map<Long, Integer> fullySatisfiedTdCountByAssessmentId = [:]

            def stepResultOrder = [
                (AssessmentStepResult.Satisfied): 1,
                (AssessmentStepResult.Not_Satisfied): 2,
                (AssessmentStepResult.Not_Known): 3,
                (AssessmentStepResult.Not_Applicable): 4,
            ]
            for( Assessment assessment : thisOrgsAssessments ){
                // td and tip data
                def tdsAndTips = CreateAssessmentTdsAndTips.fromJSON(assessment.tdsAndTipsJSON)
                tdsAndTipsByAssessmentId[assessment.id] = tdsAndTips
                tipInfoByTipIdByAssessmentId[assessment.id] = [:]
                int fullySatisfiedTipCount = 0
                for (CreateAssessmentTIPData tipData : tdsAndTips.trustInteroperabilityProfiles) {
                    def tipInfo = new TipInfoCollection()
                    tipInfoByTipIdByAssessmentId[assessment.id][tipData.databaseId] = tipInfo
                    tipInfo.allPotentialTds = tipData.getAllPotentialTds()
                    tipInfo.chosenTds = tipData.useAllTds ? tipInfo.allPotentialTds : tipData.tdUris.collect{ TrustmarkDefinition.findByUri(it) }
                    List<AssessmentStepData> chosenTdSteps = tipInfo.chosenTds.collectMany{ assessment.getStepListByTrustmarkDefinition(it) }
                    tipInfo.chosenTdStepsByResult = chosenTdSteps.groupBy { it.result }
                    def chosenTdStepSatisfiedCount = tipInfo.getChosenTdStepResultCount(AssessmentStepResult.Satisfied)
                    def chosenTdStepApplicableCount = tipInfo.applicableTdStepCount
                    if (chosenTdStepSatisfiedCount == chosenTdStepApplicableCount) { ++fullySatisfiedTipCount }

                }
                fullySatisfiedTipCountByAssessmentId[assessment.id] = fullySatisfiedTipCount

                int fullySatisfiedTdCount = 0;
                def tds = assessment.tdLinks.collect{ it.trustmarkDefinition }
                for (TrustmarkDefinition td : tds) {
                    def steps = assessment.getStepListByTrustmarkDefinition(td)
                    def stepSatisfiedCount = steps.count{ it.result == AssessmentStepResult.Satisfied }
                    def stepNotApplicableCount = steps.count { it.result == AssessmentStepResult.Not_Applicable }
                    def stepApplicableCount = steps.size() - stepNotApplicableCount
                    if (stepSatisfiedCount == stepApplicableCount) { ++fullySatisfiedTdCount }
                }
                fullySatisfiedTdCountByAssessmentId[assessment.id] = fullySatisfiedTdCount


                // Generate pie chart for rule status
                def stepsByResult = assessment.steps.groupBy{ it.result }.sort{ stepResultOrder[it.key] }

                // Generate pie chart for rule status
                def chartPlots = stepsByResult.collect {
                    int currentCount = it.value.size();
                    double percent = 100.0d * currentCount / assessment.steps.size();
                    AssessmentStepResult result = it.key
                    Color color = ColorPalette.STEP_RESULT_UNKNOWN;
                    if( result == AssessmentStepResult.Satisfied ) color = ColorPalette.STEP_RESULT_SATISFIED;
                    else if( result == AssessmentStepResult.Not_Satisfied ) color = ColorPalette.STEP_RESULT_NOT_SATISFIED;
                    else if( result == AssessmentStepResult.Not_Applicable ) color = ColorPalette.STEP_RESULT_NA;

                    //Plots.newBarChartPlot(Data.newData(percent), color, result.toString() + " ("+currentCount+")" )
                    Plots.newBarChartPlot(Data.newData(percent), color)
                }

                BarChart stepResultChart = GCharts.newBarChart(chartPlots);

                // charts4j uses the chart.apis.google.com default endpoint which does not support HTTPS, so using Chrome,
                // all HTTP requests will be redirected to HTTPS. Instead, use the endpoint chart.googleapis.com/chart which
                // does support HTTPS thus preventing redirection.
                stepResultChart.setURLEndpoint("https://chart.googleapis.com/chart");

                stepResultChart.setSize(500, 50);
                stepResultChart.setHorizontal(true);
                stepResultChart.setDataStacked(true);
                stepResultChart.setTitle("Assessment Step Results (of ${assessment.steps.size()} Steps)")
                charts.put(assessment.id + "_STEP_CHART", stepResultChart)
            }

            log.debug("Generating pie chart for status...")
            def assessmentsByStatus = thisOrgsAssessments.groupBy {it.status}
            def slices = assessmentsByStatus.collect {
                int currentCount = it.value.size()
                double percent = 100.0d * currentCount / thisOrgsAssessments.size();
                String text = "" + it.key + " (${currentCount})";
                Slice.newSlice((int) percent, text);
            }
            PieChart statusChart = GCharts.newPieChart(slices);

            // charts4j uses the chart.apis.google.com default endpoint which does not support HTTPS, so using Chrome,
            // all HTTP requests will be redirected to HTTPS. Instead, use the endpoint chart.googleapis.com/chart which
            // does support HTTPS thus preventing redirection.
            statusChart.setURLEndpoint("https://chart.googleapis.com/chart");

            statusChart.setSize(600, 200);
            statusChart.setTitle("Assessment Status Distribution (of ${thisOrgsAssessments.size()})")
            charts.put('statusChart', statusChart)

            log.debug("Rendering the HTML from report model...");
            return render(
                    view: '/reports/organization/view',
                    model: [
                            command: command,
                            startDate: command.startDate,
                            endDate: command.endDate,
                            assessments: thisOrgsAssessments,
                            tdsAndTipsByAssessmentId: tdsAndTipsByAssessmentId,
                            tipInfoByTipIdByAssessmentId: tipInfoByTipIdByAssessmentId,
                            fullySatisfiedTipCountByAssessmentId: fullySatisfiedTipCountByAssessmentId,
                            fullySatisfiedTdCountByAssessmentId: fullySatisfiedTdCountByAssessmentId,
                            organization: command.organization,
                            charts: charts
                    ]
            );
        }


    }//end showOrganizationReprotForm()


    //------------------------------------------------------------------------------------------------------------------
    //  Trustmark Definition Report
    //------------------------------------------------------------------------------------------------------------------
    /**
     * Shows the user a form for picking the organizational report.
     */
    def tdReport(TrustmarkDefinitionReportCommand command) {
        User user = springSecurityService.currentUser;
        log.info("User[${user}] hitting TD report...")

        List<TrustmarkDefinition> tds = [];
        tds.addAll(TrustmarkDefinition.findAll());
        Collections.sort(tds, {td1, td2 ->
            return td1.name.compareToIgnoreCase(td2.name);
        } as Comparator);

        if( request.getMethod().equalsIgnoreCase("GET") ){
            log.debug("Showing td report form...");
            return render(view: '/reports/td/form', model: [command: new TrustmarkDefinitionReportCommand(), trustmarkDefinitions: tds]);
        }else if( request.getMethod().equalsIgnoreCase("POST") ) {
            log.debug("Processing td report form...");

            if( command.hasErrors() ){
                log.warn("Errors detected in form submission!");
                return render(view: '/reports/td/form', model: [command: command, trustmarkDefinitions: tds]);
            }

            def charts = [:]

            def assessments = getAssessmentsInDateRange(command.startDate, command.endDate);
            log.debug("Iterating @|green ${assessments.size()}|@ assessments to match against TD[@|green ${command.trustmarkDefinition.uniqueDisplayName}|@]...");
            def tdAssessments = []
            for( Assessment assessment : assessments ){
                for(AssessmentTrustmarkDefinitionLink link : assessment.getTdLinks() ){
                    if( command.trustmarkDefinition.id  == link.getTrustmarkDefinition().id  && !tdAssessments.contains(assessment) ){
                        log.debug("Adding assessment[@|green ${assessment.id}|@] to td matching list...")
                        tdAssessments.add(assessment);
                    }
                }
            }

            List<Organization> organizations = []
            organizations.addAll(Organization.findAll());
            Collections.sort(organizations, {org1, org2 ->
                return org1.name.compareToIgnoreCase(org2.name);
            } as Comparator);

            def orgTdMap = [:]
            for( Organization org : organizations ){
                for( Assessment ass : tdAssessments ){
                    if(ass.assessedOrganization.id == org.id) {
                        addToListEntry(orgTdMap, org.id, ass)
                    }
                }
            }
            organizations.each{ org ->
                if( orgTdMap.get(org) && orgTdMap.get(org).size() > 1 ){
                    log.warn("Org[@|yellow ${org.name}|@] has multiple assessments against TD[@|red ${command.trustmarkDefinition.uniqueDisplayName}|@]!")
                }
            }

//
//            log.debug("Generating pie chart for status...")
//            def slices = []
//            tdStats.keySet().each{ status ->
//                if( status != "overallCount" && status != "overallCountWithAssessments" ){
//                    int currentCount = tdStats[status]
//                    double percent = (currentCount / tdStats['overallCount']) * 100.0d;
//                    Slice slice = Slice.newSlice((int) percent, status + " ("+currentCount+")");
//                    slices.add(slice);
//                }
//            }
//            PieChart statusChart = GCharts.newPieChart(slices);
//            statusChart.setSize(600, 200);
//            statusChart.setTitle("Assessment Status Distribution (of ${tdStats['overallCount']})")
//            charts.put('statusChart', statusChart)

            log.debug("Rendering the HTML from report model...");
            return render(
                    view: '/reports/td/view',
                    model: [
                            command: command,
                            startDate: command.startDate,
                            endDate: command.endDate,
                            trustmarkDefinition: command.trustmarkDefinition,
                            assessments: tdAssessments,
                            organizations: organizations,
                            orgTdMap: orgTdMap,
                            charts: charts
                    ]
            );
        }

    }//end tdReport()



    //------------------------------------------------------------------------------------------------------------------
    //  Trustmark Definition Report
    //------------------------------------------------------------------------------------------------------------------

    /**
     * Shows the user a form for picking the TIP report.
     */
    def tipReport(TrustInteroperabilityProfileReportCommand command) {
        User user = springSecurityService.currentUser;
        log.info("User[${user}] hitting TIP report...")

        List<TrustInteroperabilityProfile> tips = [];
        tips.addAll(TrustmarkDefinition.findAll());
        Collections.sort(tips, {tip1, tip2 ->
            return tip1.name.compareToIgnoreCase(tip2.name);
        } as Comparator);

        if( request.getMethod().equalsIgnoreCase("GET") ){
            log.debug("Showing TIP report form...");
            return render(view: '/reports/tip/form', model: [command: new TrustInteroperabilityProfileReportCommand(), tips: tips]);
        }
        else if( request.getMethod().equalsIgnoreCase("POST") ) {
            log.debug("Processing TIP report form...");

            if( command.hasErrors() ){
                log.warn("Errors detected in form submission!");
                return render(view: '/reports/tip/form', model: [command: command, tips: tips]);
            }

            log.debug("Retrieving all organizations...")
            def organizations = Organization.findAll();

            log.debug("Mapping all organizations to trustmarks...");
            def orgTrustmarks = [:]
            for( Organization org : organizations ) {
                List<Trustmark> trustmarks = Trustmark.findAllByRecipientOrganization(org);
                if (trustmarks == null || trustmarks.isEmpty()){
                    log.debug("Ignoring organization ${org.uri} because they have no trustmarks at all.")
                    continue;
                }
            }


            def assessments = getAssessmentsInDateRange(command.startDate, command.endDate);



            throw new UnsupportedOperationException("NOT YET IMPLEMENTED");
        }

    }//end tdReport()


    //==================================================================================================================
    //  Helper Methods
    //==================================================================================================================

    private void addToListEntry(map, key, value){
        def list = []
//        log.debug("Adding list entry key[$key]=value[$value]")
        if( map.containsKey(key) ){
            list = map.get(key);
        }
        list.add(value);
        map.put(key, list);
    }

    private List<Assessment> getAssessmentsInDateRange(Date startDate, Date endDate){

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

}//end ReportsController

class OrganizationReportCommand implements Validateable {
    public OrganizationReportCommand(){
        Calendar startDefault = Calendar.getInstance();
        startDefault.set(Calendar.DAY_OF_YEAR, 1);
        startDefault.set(Calendar.YEAR, 2014);
        this.startDate = startDefault.getTime();

        Calendar endDefault = Calendar.getInstance();
        int dom = endDefault.get(Calendar.DAY_OF_MONTH);
        endDefault.set(Calendar.DAY_OF_MONTH, dom + 1); // Make it in the future, at least.
        this.endDate = endDefault.getTime();
    }

    Organization organization;
    Date startDate;
    Date endDate;
    Boolean hideCompletedAssessments = Boolean.FALSE;
    Boolean hideCompletedSteps = Boolean.TRUE;

    static constraints = {
        organization nullable: false
        startDate nullable: false
        endDate nullable: false
    }

}

class TrustmarkDefinitionReportCommand implements Validateable {
    public TrustmarkDefinitionReportCommand(){
        Calendar startDefault = Calendar.getInstance();
        startDefault.setTimeInMillis(0l);
        this.startDate = startDefault.getTime();

        Calendar endDefault = Calendar.getInstance();
        endDefault.set(Calendar.MONTH, Calendar.DECEMBER);
        endDefault.set(Calendar.DAY_OF_MONTH, 31);
        endDefault.set(Calendar.YEAR, 2050);
        this.endDate = endDefault.getTime();
    }

    TrustmarkDefinition trustmarkDefinition;
    Date startDate;
    Date endDate;

    static constraints = {
        trustmarkDefinition nullable: false
        startDate nullable: false
        endDate nullable: false
    }

}//end TrustmarkDefinitionReportCommand()

class TrustInteroperabilityProfileReportCommand implements Validateable {
    public TrustInteroperabilityProfileReportCommand(){
        Calendar startDefault = Calendar.getInstance();
        startDefault.setTimeInMillis(0l);
        this.startDate = startDefault.getTime();

        Calendar endDefault = Calendar.getInstance();
        endDefault.set(Calendar.MONTH, Calendar.DECEMBER);
        endDefault.set(Calendar.DAY_OF_MONTH, 31);
        endDefault.set(Calendar.YEAR, 2050);
        this.endDate = endDefault.getTime();
    }

    TrustInteroperabilityProfile tip;
    Date startDate;
    Date endDate;

    static constraints = {
        tip nullable: false
        startDate nullable: false
        endDate nullable: false
    }

}//end TrustInteroperabilityProfileReportCommand()


class OverallReportCommand {
    public OverallReportCommand(){
        Calendar startDefault = Calendar.getInstance();
        startDefault.setTimeInMillis(0l);
        this.startDate = startDefault.getTime();

        Calendar endDefault = Calendar.getInstance();
        endDefault.set(Calendar.MONTH, Calendar.DECEMBER);
        endDefault.set(Calendar.DAY_OF_MONTH, 31);
        endDefault.set(Calendar.YEAR, 2050);
        this.endDate = endDefault.getTime();
    }

    Date startDate;
    Date endDate;

    static constraints = {
        startDate nullable: false
        endDate nullable: false
    }

}

class TipInfoCollection {
    List<TrustmarkDefinition> allPotentialTds
    List<TrustmarkDefinition> chosenTds
    Map<AssessmentStepResult, List<AssessmentStepData>> chosenTdStepsByResult
    List<AssessmentStepData> getChosenTdSteps() { this.chosenTdStepsByResult.entrySet().collectMany{ it.value }}
    int getChosenTdStepResultCount(AssessmentStepResult result) { this.chosenTdStepsByResult[result]?.size() ?: 0 }
    int getApplicableTdStepCount() {
        this.getChosenTdStepResultCount(AssessmentStepResult.Satisfied) \
        + this.getChosenTdStepResultCount(AssessmentStepResult.Not_Satisfied) \
        + this.getChosenTdStepResultCount(AssessmentStepResult.Not_Known)
    }
    double getPercentSatisfied() {
        100 * this.getChosenTdStepResultCount(AssessmentStepResult.Satisfied) / this.applicableTdStepCount
    }
}
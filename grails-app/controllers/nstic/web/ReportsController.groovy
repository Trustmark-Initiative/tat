package nstic.web

import com.googlecode.charts4j.BarChart
import com.googlecode.charts4j.Color
import com.googlecode.charts4j.Data
import com.googlecode.charts4j.GCharts
import com.googlecode.charts4j.PieChart
import com.googlecode.charts4j.Plots
import com.googlecode.charts4j.Slice
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.gsp.PageRenderer
import grails.validation.Validateable
import groovy.json.JsonSlurper
import nstic.assessment.ColorPalette
import nstic.web.assessment.Assessment
import nstic.web.assessment.AssessmentStatus
import nstic.web.assessment.AssessmentStepData
import nstic.web.assessment.AssessmentStepResult
import nstic.web.assessment.AssessmentTrustmarkDefinitionLink
import nstic.web.assessment.Trustmark
import nstic.web.td.TrustmarkDefinition
import nstic.web.tip.TrustInteroperabilityProfile
import org.apache.commons.lang.StringUtils
import org.json.JSONArray
import org.json.JSONObject
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

import javax.servlet.ServletException


@Transactional
class ReportsController {

    def mailService;
    def reportsService

    /**
     * Displays the listing of reports.
     */
    def index() {
        log.debug("Loading report listing...");
    }//end index()

    def share() {
        User user = User.findByUsername(((OAuth2AuthenticationToken) SecurityContextHolder.getContext().getAuthentication()).getName());
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
        User user = User.findByUsername(((OAuth2AuthenticationToken) SecurityContextHolder.getContext().getAuthentication()).getName());
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
            def assessments = reportsService.getAssessmentsInDateRange(command.startDate, command.endDate);
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
    def organizationReport() {
        User user = User.findByUsername(((OAuth2AuthenticationToken) SecurityContextHolder.getContext().getAuthentication()).getName());
        log.info("User[${user}] hitting org report...")

        String sessionId = session.getId()

        if( request.getMethod().equalsIgnoreCase("GET") ){
            log.debug("Showing organization report form...");

            return render(view: '/reports/organization/form', model: [user: user, command: new OrganizationReportCommand()]);
        }else if( request.getMethod().equalsIgnoreCase("POST") ) {
            log.debug("Processing organization report form...");

            Organization organization = Organization.get(Integer.parseInt(params.id))

            BigInteger startDateInMilliseconds = new BigInteger(params.startDate)
            Date startDate = new Date(startDateInMilliseconds.longValue())

            BigInteger endDateInMilliseconds = new BigInteger(params.endDate)
            Date endDate = new Date(endDateInMilliseconds.longValue())

            boolean hideCompletedAssessments = Boolean.parseBoolean(params.hideCompletedAssessments)
            boolean hideCompletedSteps = Boolean.parseBoolean(params.hideCompletedSteps)

            OrganizationReportCommand command = new OrganizationReportCommand(
                    organization,
                    startDate,
                    endDate,
                    hideCompletedAssessments,
                    hideCompletedSteps
            )

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

            // Check if report thread is running, interrupt thread and wait for it to finish
            if (reportsService.isExecuting(sessionId, ReportsService.ORGANIZATION_REPORT_EXECUTING_VAR)) {
                reportsService.stopExecuting(sessionId, ReportsService.ORGANIZATION_REPORT_EXECUTING_VAR)

                log.debug("Interrupting previous TM Generation thread...")
                Thread t = reportsService.getAttribute(sessionId, ReportsService.ORGANIZATION_REPORT_THREAD_VAR)
                if (t &&  t.isAlive()) {
                    reportsService.setAttribute(sessionId, ReportsService.ORGANIZATION_REPORT_STATUS_VAR, "CANCELLING")
                    reportsService.setAttribute(sessionId, ReportsService.ORGANIZATION_REPORT_MESSAGE_VAR, "Cancelling previous report generation process...")

                    t.join()
                    log.debug("Interrupted previous TM Generation thread...")
                }
            }

            // create params map for service
            final Map paramsMap = [:]
            paramsMap.put("organization", organization)
            paramsMap.put("startDate", startDate)
            paramsMap.put("endDate", endDate)
            paramsMap.put("hideCompletedAssessments", hideCompletedAssessments)
            paramsMap.put("hideCompletedSteps", hideCompletedSteps)
            paramsMap.put("sessionId", sessionId)

            def assessments = reportsService.getAssessmentsInDateRange(command.startDate, command.endDate);
            List<Assessment> thisOrgsAssessments = assessments.findAll{it.getAssessedOrganization() == command.organization}
            paramsMap.put("assessments", thisOrgsAssessments)

            reportsService.setAttribute(sessionId, ReportsService.ORGANIZATION_REPORT_STATUS_VAR, "RUNNING")
            reportsService.setAttribute(sessionId, ReportsService.ORGANIZATION_REPORT_MESSAGE_VAR, "Preprocessing report for the ${organization.name} organization...")

            reportsService.setExecuting(sessionId, ReportsService.ORGANIZATION_REPORT_EXECUTING_VAR)

            Thread organizationReportThread = new Thread(new Runnable() {
                @Override
                void run() {
                    Organization.withTransaction {
                        reportsService.organizationReport(paramsMap)
                    }
                }
            })
            reportsService.setAttribute(sessionId, ReportsService.ORGANIZATION_REPORT_THREAD_VAR, organizationReportThread)
            organizationReportThread.start()

            Map jsonResponse = [status: 'STARTING', message: 'Starting the organizational report process.']

            render jsonResponse as JSON
        }
    } // end organizationReport()

    def renderOrganizationReport() {
        log.info("renderOrganizationReport...")

        String sessionId = session.getId()

        def jsonSlurper = new JsonSlurper()
        def result = jsonSlurper.parseText(reportsService.getAttribute(sessionId, ReportsService.ORGANIZATION_REPORT_RENDER_MODEL_VAR))

        // get organization
        Organization organization = Organization.get(result["organizationId"])
        log.info("Organization: ${organization.name}")

        // get assessments
        def thisOrgsAssessments = Assessment.getAll(result["assessmentIds"])

        // get dates
        BigInteger startDateInMilliseconds = new BigInteger(result["startDate"])
        Date startDate = new Date(startDateInMilliseconds.longValue())

        BigInteger endDateInMilliseconds = new BigInteger(result["endDate"])
        Date endDate = new Date(endDateInMilliseconds.longValue())

        boolean hideCompletedAssessments = result["hideCompletedAssessments"]
        boolean hideCompletedSteps = result["hideCompletedSteps"]

        OrganizationReportCommand command = new OrganizationReportCommand(
                organization,
                startDate,
                endDate,
                hideCompletedAssessments,
                hideCompletedSteps
        )

        // fullySatisfiedTipCountByAssessmentId
        JSONObject fullySatisfiedTipCountByAssessmentIdJson = new JSONObject(result["fullySatisfiedTipCountByAssessmentId"])
        // TODO: move this processing to the service
        Map<Long, Integer> fullySatisfiedTipCountByAssessmentId = [:]
        Iterator<String> fullySatisfiedTipCountByAssessmentIdStringKeys = fullySatisfiedTipCountByAssessmentIdJson.keys()
        while(fullySatisfiedTipCountByAssessmentIdStringKeys.hasNext()) {
            String key = fullySatisfiedTipCountByAssessmentIdStringKeys.next();
            Object id = fullySatisfiedTipCountByAssessmentIdJson.get(key)
            if (id) {
                fullySatisfiedTipCountByAssessmentId[Long.parseLong(key)] = id
            }
        }

        // fullySatisfiedTdCountByAssessmentId
        JSONObject fullySatisfiedTdCountByAssessmentIdJson = new JSONObject(result["fullySatisfiedTdCountByAssessmentId"])
        // TODO: move this processing to the service
        Map<Long, Integer> fullySatisfiedTdCountByAssessmentId = [:]
        Iterator<String> fullySatisfiedTdCountByAssessmentIdStringKeys = fullySatisfiedTdCountByAssessmentIdJson.keys()
        while(fullySatisfiedTdCountByAssessmentIdStringKeys.hasNext()) {
            String key = fullySatisfiedTdCountByAssessmentIdStringKeys.next();
            Object id = fullySatisfiedTdCountByAssessmentIdJson.get(key)
            if (id) {
                fullySatisfiedTdCountByAssessmentId[Long.parseLong(key)] = id
            }
        }

        // tdsAndTipsByAssessmentId
        Map<Long, CreateAssessmentTdsAndTips> tdsAndTipsByAssessmentId = [:]
        JSONObject tdsAndTipsByAssessmentIdJson = new JSONObject(result["tdsAndTipsByAssessmentId"])
        Iterator<String> tdsAndTipsByAssessmentIdKeys = tdsAndTipsByAssessmentIdJson.keys()

        while(tdsAndTipsByAssessmentIdKeys.hasNext()) {
            String key = tdsAndTipsByAssessmentIdKeys.next();
            Object map = tdsAndTipsByAssessmentIdJson.get(key)
            if (map) {
                // do something with jsonObject here
                CreateAssessmentTdsAndTips createAssessmentTdsAndTips = CreateAssessmentTdsAndTips.fromJSON(map)
                tdsAndTipsByAssessmentId[Long.parseLong(key)] = createAssessmentTdsAndTips
            }
        }

        // tipInfoByTipIdByAssessmentId
        Map<Long, Map<Long, TipInfoCollection>> tipInfoByTipIdByAssessmentId = [:]
        JSONObject tipInfoByTipIdByAssessmentIdJson = new JSONObject(result["tipInfoByTipIdByAssessmentId"])

        Iterator<String> tipInfoByTipIdByAssessmentIdKeys = tipInfoByTipIdByAssessmentIdJson.keys()

        while(tipInfoByTipIdByAssessmentIdKeys.hasNext()) {
            String key = tipInfoByTipIdByAssessmentIdKeys.next();
            Map<Long, TipInfoCollection> tipInfoCollectionMap = [:]
            Object map = tipInfoByTipIdByAssessmentIdJson.get(key)
            Iterator<String> tipInfoCollectionMapKeys = map.keys()
            while(tipInfoCollectionMapKeys.hasNext()) {
                String tipKey = tipInfoCollectionMapKeys.next();
                TipInfoCollection tipInfoCollection = TipInfoCollection.fromJSON(map.get(tipKey))
                tipInfoCollectionMap.put(Long.parseLong(tipKey), tipInfoCollection)
            }
            tipInfoByTipIdByAssessmentId.put(Long.parseLong(key), tipInfoCollectionMap)
        }

        // charts
        def charts = [:]

        def stepResultOrder = [
                (AssessmentStepResult.Satisfied)     : 1,
                (AssessmentStepResult.Not_Satisfied) : 2,
                (AssessmentStepResult.Not_Known)     : 3,
                (AssessmentStepResult.Not_Applicable): 4,
        ]

        for (Assessment assessment : thisOrgsAssessments) {

            // Generate pie chart for rule status
            def stepsByResult = assessment.steps.groupBy { it.result }.sort { stepResultOrder[it.key] }

            // Generate pie chart for rule status
            log.info("Generate pie chart for rule status...")

            def chartPlots = stepsByResult.collect {
                int currentCount = it.value.size();
                double percent = 100.0d * currentCount / assessment.steps.size();
                AssessmentStepResult res = it.key
                Color color = ColorPalette.STEP_RESULT_UNKNOWN;
                if (res == AssessmentStepResult.Satisfied) color = ColorPalette.STEP_RESULT_SATISFIED;
                else if (res == AssessmentStepResult.Not_Satisfied) color = ColorPalette.STEP_RESULT_NOT_SATISFIED;
                else if (res == AssessmentStepResult.Not_Applicable) color = ColorPalette.STEP_RESULT_NA;

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

        def assessmentsByStatus = thisOrgsAssessments.groupBy { it.status }
        def slices = assessmentsByStatus.collect {
            int currentCount = it.value.size()
            double percent = 100.0d * currentCount / thisOrgsAssessments.size();
            String text = "" + it.key + " (${currentCount})";
            Slice.newSlice((int) percent, text);
        }

        log.info("statusChart slices: ${slices.size()}")

        PieChart statusChart = GCharts.newPieChart(slices);

        // charts4j uses the chart.apis.google.com default endpoint which does not support HTTPS, so using Chrome,
        // all HTTP requests will be redirected to HTTPS. Instead, use the endpoint chart.googleapis.com/chart which
        // does support HTTPS thus preventing redirection.
        statusChart.setURLEndpoint("https://chart.googleapis.com/chart");

        statusChart.setSize(600, 200);
        statusChart.setTitle("Assessment Status Distribution (of ${thisOrgsAssessments.size()})")
        charts.put('statusChart', statusChart)

        log.info("charts size: ${charts.size()}")

        // render
        render(
            view: '/reports/organization/view',
            model: [
                    command                             : command,
                    startDate                           : command.startDate,
                    endDate                             : command.endDate,
                    assessments                         : thisOrgsAssessments,
                    tdsAndTipsByAssessmentId            : tdsAndTipsByAssessmentId,
                    tipInfoByTipIdByAssessmentId        : tipInfoByTipIdByAssessmentId,
                    fullySatisfiedTipCountByAssessmentId: fullySatisfiedTipCountByAssessmentId,
                    fullySatisfiedTdCountByAssessmentId : fullySatisfiedTdCountByAssessmentId,
                    organization                        : command.organization,
                    charts                              : charts
            ]);

    }

    def initOrganizationReportState() {
        log.info("initOrganizationReportState...")
        User user = User.findByUsername(((OAuth2AuthenticationToken) SecurityContextHolder.getContext().getAuthentication()).getName())

        String sessionId = session.getId()

        reportsService.setAttribute(sessionId, ReportsService.ORGANIZATION_REPORT_MESSAGE_VAR, "About to start the organizational report process...")
        reportsService.setAttribute(sessionId, ReportsService.ORGANIZATION_REPORT_STATUS_VAR, "RUNNING")
        reportsService.setAttribute(sessionId, ReportsService.ORGANIZATION_REPORT_PERCENT_VAR, "0")

        reportsService.setExecuting(sessionId, ReportsService.ORGANIZATION_REPORT_EXECUTING_VAR)

        Map jsonResponse = [status: 'SUCCESS', message: 'Successfully initialized the organizational report process.']

        render jsonResponse as JSON
    }

    def organizationReportStatusUpdate() {
        log.info("** organizationReportStatusUpdate().")

        String sessionId = session.getId()

        Map jsonResponse = [:]
        jsonResponse.put("status", reportsService.getAttribute(sessionId, ReportsService.ORGANIZATION_REPORT_STATUS_VAR))
        String percentString = reportsService.getAttribute(sessionId, ReportsService.ORGANIZATION_REPORT_PERCENT_VAR)

        int percentInt = 0
        if( StringUtils.isNotEmpty(percentString) ){
            percentInt = Integer.parseInt(percentString.trim())
        }
        jsonResponse.put("percent", percentInt)
        jsonResponse.put("message", reportsService.getAttribute(sessionId, ReportsService.ORGANIZATION_REPORT_MESSAGE_VAR))

        render jsonResponse as JSON
    }

    /**
     * Shows the user a form for picking the organizational report.
     */
    def tdReport(TrustmarkDefinitionReportCommand command) {
        User user = User.findByUsername(((OAuth2AuthenticationToken) SecurityContextHolder.getContext().getAuthentication()).getName());
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

            def assessments = reportsService.getAssessmentsInDateRange(command.startDate, command.endDate);
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
        User user = User.findByUsername(((OAuth2AuthenticationToken) SecurityContextHolder.getContext().getAuthentication()).getName());
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


            def assessments = reportsService.getAssessmentsInDateRange(command.startDate, command.endDate);



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

    public OrganizationReportCommand(
            Organization organization,
            Date startDate,
            Date endDate,
            boolean hideCompletedAssessments,
            boolean hideCompletedSteps) {
        this.organization = organization
        this.startDate = startDate
        this.endDate = endDate
        this. hideCompletedAssessments = hideCompletedAssessments
        this.hideCompletedSteps = hideCompletedSteps
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

class TipInfoCollection implements Serializable {
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

    JSONObject toJSON() {
        JSONObject obj = new JSONObject();

        JSONArray allPotentialTds = new JSONArray();
        if( this.allPotentialTds != null && this.allPotentialTds.size() > 0 ){
            for( TrustmarkDefinition td : this.allPotentialTds ){
                // serialize the id, use the id to load the td from the db when deserializing
                allPotentialTds.put(td.id);
            }
        }
        obj.put("allPotentialTds", allPotentialTds);

        JSONArray chosenTds = new JSONArray();
        if( this.chosenTds != null && this.chosenTds.size() > 0 ){
            for( TrustmarkDefinition td : this.chosenTds ){
                chosenTds.put(td.id);
            }
        }
        obj.put("chosenTds", chosenTds);


//        Map<AssessmentStepResult, List<AssessmentStepData>> chosenTdStepsByResult
        JSONObject chosenTdStepsByResult = new JSONObject();

        if( this.chosenTdStepsByResult != null && this.chosenTdStepsByResult.size() > 0 ){
            this.chosenTdStepsByResult.each { key, value ->
                JSONArray jsonObjects = new JSONArray()
                List<AssessmentStepData> assessmentStepDataList = value
                if( assessmentStepDataList != null && assessmentStepDataList.size() > 0 ){
                    for( AssessmentStepData asd : assessmentStepDataList ){
                        jsonObjects.put(asd.id);
                    }
                }
                chosenTdStepsByResult.put(key.toString(), jsonObjects);
            }
        }
        obj.put("chosenTdStepsByResult", chosenTdStepsByResult);

        return obj
    }

    public static TipInfoCollection fromJSON(String json){
        JSONObject jsonObject = new JSONObject(json)
        return fromJSON(jsonObject)
    }
    public static TipInfoCollection fromJSON(JSONObject json){
        TipInfoCollection tipInfoCollection = new TipInfoCollection()

        tipInfoCollection.allPotentialTds = []

        JSONArray tdObjs = json.optJSONArray("allPotentialTds")
        if( tdObjs != null && tdObjs.length() > 0 ){
            for( int i = 0; i < tdObjs.length(); i++ ){
                int tdId = tdObjs.get(i)
                tipInfoCollection.allPotentialTds.add( TrustmarkDefinition.get(tdId) )
            }
        }

        tipInfoCollection.chosenTds = []

        tdObjs = json.optJSONArray("chosenTds")
        if( tdObjs != null && tdObjs.length() > 0 ){
            for( int i = 0; i < tdObjs.length(); i++ ){
                int tdId = tdObjs.get(i)
                tipInfoCollection.chosenTds.add( TrustmarkDefinition.get(tdId) )
            }
        }


        tipInfoCollection.chosenTdStepsByResult = [:]

        JSONObject obj = json.get("chosenTdStepsByResult")
        Iterator<?> keys = obj.keys();

        while( keys.hasNext() ) {
            String key = (String)keys.next()
            AssessmentStepResult asr = AssessmentStepResult.fromString((String)key)

            JSONArray assessmentStepDataJson = obj.get(key);
//            JSONArray assessmentStepDataJson = new JSONArray(value)

            // populate list
            def assessmentStepData = []

            for (int i = 0; i < assessmentStepDataJson.length(); i++) {
                int id = assessmentStepDataJson.get(i)
                assessmentStepData.add(AssessmentStepData.get(id))
            }

            tipInfoCollection.chosenTdStepsByResult.put(asr, assessmentStepData)
        }

        return tipInfoCollection
    }
}
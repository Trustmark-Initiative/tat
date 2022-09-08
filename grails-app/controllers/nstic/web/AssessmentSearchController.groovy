package nstic.web

import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import nstic.web.assessment.Assessment
import nstic.web.assessment.AssessmentLog
import org.apache.commons.lang.StringUtils
import org.hibernate.SQLQuery
import org.hibernate.Session

import javax.servlet.ServletException

/**
 * Responsible for providing search capabilities on Assessments.
 */
@Secured(["ROLE_USER", "ROLE_ADMIN"])
class AssessmentSearchController {

    static List<String> SEARCH_FIELDS = [
            "a.result_comment",
            "a.name",
            "ci.email", "ci.notes", "ci.responder",
            "o.name", "o.uri",
            "td.name", "td.description", "td.td_version",
            "user.username",
            "entry.message", "entry.title"
    ]

    static String SEARCH_SQL_TEMPLATE = """
SELECT DISTINCT A_ID FROM (
    SELECT a.id as A_ID
    FROM assessment a
join assessment_log log on a.assessment_log_ref = log.id
join assessment_log_entry entry on entry.assessment_log_ref = log.id
join assessment_td_link link on a.id = link.assessment_ref
join contact_information ci on a.contact_information_ref = ci.id
join organization o on a.organization_ref = ci.id
join trustmark_definition td on link.td_ref = td.id
left join assessment_user user on a.assigned_to_ref = user.id
where (
            @_INSERT_OR_FIELDS_@
        )
    ORDER BY entry.id desc
) AS TEMP_TABLE_SEARCH
"""


    def springSecurityService
    def sessionFactory

    /**
     * Called to perform a search, when given a user-typed query string (as parameter 'q').
     */
    def search(){
        User user = springSecurityService.currentUser
        log.debug("User[${user.username}] sending Assessment query[${params.q}]....[${params.max}]")

        if( StringUtils.isEmpty(params.q) ){
            log.warn("Cannot process empty query")
            throw new ServletException("Parameter 'q' is empty, but is required.")
        }

        Integer max = params.max ? Integer.parseInt(params.max) : 10
        max = Math.min(50, max)

        String queryString = buildSearchSql(params.q)
        log.debug("Executing SQL: $queryString")

        final Session session = sessionFactory.currentSession
        final SQLQuery sqlQuery = session.createSQLQuery(queryString)
        def assessmentIds = []
        sqlQuery.list().each{ result ->
            if( result instanceof BigInteger ){
                assessmentIds.add( ((BigInteger) result).longValue() )
            }else {
                log.debug("Assessment Id[type=${result?.class.name}]: $result")
                assessmentIds.add(result)
            }
        }

        if( assessmentIds.isEmpty() ){
            log.info("No results found for Query[${params.q}]")
        }else{
            log.info("Query[${params.q}] resulted in ${assessmentIds.size()} assessment matches.")
        }

        def assessments = []

        withFormat {
            html {
                assessmentIds?.each { assessmentId ->
                    assessments.add(Assessment.get(assessmentId));
                }
                log.debug("Rendering results html ...")
                [assessments: assessments, resultCount: assessments.size(), assessmentCount: Assessment.count(), queryString: params.q]
            }
            json {
                assessmentIds?.each { assessmentId ->
                    assessments.add(Assessment.get(assessmentId).toJsonMap(true));
                }
                log.debug("Rendering results json...")
                render assessments as JSON
            }
        }
    }//end search()

    //==================================================================================================================
    //  Private Helper Methods
    //==================================================================================================================
    /**
     * Given a search query, this method will build the "OR" part of a search query to find any of the given terms.
     */
    private String buildSearchSql(String query) {
        List<String> terms = parseTerms(query);
        String orReplacement = buildOrReplacement(terms);
        String searchSql = SEARCH_SQL_TEMPLATE.replace("@_INSERT_OR_FIELDS_@", orReplacement);
        return searchSql;
    }//end buildSearchSql()

    private String buildOrReplacement(List<String> terms){

        StringBuilder orBuilder = new StringBuilder();
        Iterator<String> termIter = terms.iterator();
        while( termIter.hasNext() ){
            String term = escapeTerm(termIter.next());

            Iterator<String> fieldIter = SEARCH_FIELDS.iterator();
            while( fieldIter.hasNext() ){
                String field = fieldIter.next();
                orBuilder.append("LOWER(").append(field).append(") like '%").append(term?.toLowerCase()).append("%'")
                if( fieldIter.hasNext() ){
                    orBuilder.append(" OR ");
                }
            }

            if( termIter.hasNext() ){
                orBuilder.append(" OR ");
            }

        }

        return orBuilder.toString();
    }//end buildSearchSql()

    /**
     * This method supports parsing double quotes as terms, otherwise each word is it's own term.
     */
    private List<String> parseTerms(String query) {
        List<String> terms = []
        log.debug("Getting terms from query...");

        // First, we remove any double quoted strings from the original query.
        while( query.contains('"') ){
            int index1 = query.indexOf('"')
            int index2 = query.indexOf('"', index1+1);
            log.debug("Index1: $index1, Index2: $index2")
            if( index2 < 0 ) {
                log.error("String[$query] contains a dangling double quote (must have 2)")
                throw new ServletException("Missing close double quote in query.")
            }

            String doubleQuotedTerm = query.substring(index1+1, index2);
            if( StringUtils.isNotEmpty(doubleQuotedTerm) ) {
                log.debug("Found term[$doubleQuotedTerm] from double quoted string")
                terms.add(doubleQuotedTerm);
            }
            String newquery = query.substring(0, index1) + query.substring(index2+1, query.length());
            log.debug("New query: $newquery")
            query = newquery;
        }

        // Now, we can just split on space.
        String[] spaceSplit = query.split(" ");
        spaceSplit.each{ term ->
            if( StringUtils.isNotEmpty(term) ) {
                log.debug("Found term[$term] from space split")
                terms.add(term);
            }
        }

        return terms;
    }//end parseTerms()

    private String escapeTerm(String term){
        String escapeTerm = term?.toString() ?: "";
        escapeTerm = escapeTerm.replace("'", "''")
        escapeTerm = escapeTerm.replace("_", "\\_")
        escapeTerm = escapeTerm.replace("%", "\\%")
        return escapeTerm;
    }//end escapeTerm()

}//end AssessmentSearchController()
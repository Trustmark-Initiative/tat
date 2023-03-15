package nstic.web

import nstic.web.assessment.Assessment
import nstic.web.assessment.AssessmentLogEntry
import org.apache.commons.lang.StringUtils
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken

import javax.servlet.ServletException

/**
 * Created by brad on 9/7/14.
 */
@PreAuthorize('hasAnyAuthority("tat-contributor", "tat-admin")')
class AssessmentLogController {

    def viewLog() {
        User user = User.findByUsername(((OAuth2AuthenticationToken) SecurityContextHolder.getContext().getAuthentication()).getName())
        
        if(StringUtils.isEmpty(params.id)){
            log.warn("Cannot display assessment log when missing id parameter")
            throw new ServletException("Missing id parameter")
        }

        log.debug("Displaying Assessment[${params.id}] LOG to User[${user}]")

        def assessment = Assessment.get(params.id);
        if( !assessment ){
            log.warn("Could not find assessmnet: ${params.id}")
            throw new ServletException("No such assessment ${params.id}")
        }

        if( !params.max )
            params.max = "25"
        params.max = Math.min(50, Integer.parseInt(params.max)).toString()
        if( !params.offset )
            params.offset = "0"

        // TODO Use params to change sort ?

        def criteria = AssessmentLogEntry.createCriteria()
        def logEntries = criteria.list {
            eq("logg", assessment.logg)
//            not { 'in'('type', ["VIEW"]) } // TODO Add other log entry types we don't want to show.
            maxResults(Integer.parseInt(params.max))
            firstResult(Integer.parseInt(params.offset))
            order("dateCreated", "desc")
        }
        def logEntryCount = AssessmentLogEntry.countByLogg(assessment.logg);

        withFormat {
            html {
                [assessment: assessment, logEntries: logEntries, logEntryCount: logEntryCount]
            }
            json {
                throw new ServletException("NOT YET IMPLEMENTED")
            }
            xml {
                throw new ServletException("NOT YET IMPLEMENTED")
            }
        }

    }//end viewLog()


    /**
     * Views a log entry's data on a particular assessment.
     */
    def viewLogEntry() {
        User user = User.findByUsername(((OAuth2AuthenticationToken) SecurityContextHolder.getContext().getAuthentication()).getName())
        log.debug("User[$user] is requesting log entry[${params.entryId}] data on assessment[${params.id}]...")

        if(StringUtils.isEmpty(params.id)){
            log.warn("Cannot display assessment log entry when missing id parameter")
            throw new ServletException("Missing id parameter")
        }
        if(StringUtils.isEmpty(params.entryId)){
            log.warn("Cannot display assessment log entry when missing entryId parameter")
            throw new ServletException("Missing entryId parameter")
        }

        def assessment = Assessment.get(params.id);
        if( !assessment ){
            log.warn("Could not find assessmnet: ${params.id}")
            throw new ServletException("No such assessment ${params.id}")
        }

        AssessmentLogEntry entry = AssessmentLogEntry.get(params.entryId);
        if( !entry ){
            log.warn("Could not find assessment log entry: ${params.entryId}")
            throw new ServletException("No such assessment log entry ${params.entryId}")
        }

        // TODO there is probably a more efficient way to find previous and next, with a single query somehow.
        AssessmentLogEntry previousEntry = null;
        AssessmentLogEntry nextEntry = null;
        def entries = []
        entries.addAll(assessment.logg.entries);
        Collections.sort(entries, {e1, e2 -> return e1.dateCreated.compareTo(e2.dateCreated)} as Comparator)

        int entryPosition = 1;
        for( int i = 0; i < entries.size(); i++ ){
            AssessmentLogEntry currentEntry = entries.get(i);
            if( currentEntry.id == entry.id ){
                if( (i-1) >= 0 ){
                    previousEntry = entries.get(i-1);
                }
                if( (i+1) < entries.size() ){
                    nextEntry = entries.get(i+1);
                }
                break;
            }
            entryPosition++;
        }

        log.info("Displaying log entry[${entry.id}: ${entry.title}] from assessment[${assessment.id}] to user[$user]...")

        withFormat {
            html {
                [assessment: assessment, entry: entry, data: entry.data, previousEntry: previousEntry, nextEntry: nextEntry, entryPosition: entryPosition, entryCount: entries.size()]
            }
            json {
                render(text: entry.data, contentType: 'application/json')
            }
            xml {
                throw new ServletException("NOT YET IMPLEMENTED")
            }
        }
    }//end viewLogEntry()


}

package nstic.web

import edu.gatech.gtri.trustmark.v1_0.FactoryLoader
import edu.gatech.gtri.trustmark.v1_0.io.SerializerFactory
import edu.gatech.gtri.trustmark.v1_0.io.TrustInteroperabilityProfileResolver
import grails.converters.JSON
import grails.converters.XML
import grails.gorm.transactions.Transactional
import nstic.web.tip.TrustInteroperabilityProfile
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken

import javax.servlet.ServletException

@PreAuthorize('hasAnyAuthority("tat-contributor", "tat-admin")')
@Transactional
class TipController {

    def list() {
        User user = User.findByUsername(((OAuth2AuthenticationToken) SecurityContextHolder.getContext().getAuthentication()).getName())
        log.debug("User[@|blue ${user}|@] listing trust interoperability profiles...")
        if( !params.max ){
            params.max = "10"
        }
        params.max = Math.min(5000, Integer.parseInt(params.max)).toString()

        def tips = TrustInteroperabilityProfile.list(params);


        withFormat {
            html {
                [tips: tips, tipsCount: TrustInteroperabilityProfile.count()]
            }
            json {
                throw new UnsupportedOperationException("JSON is not yet supported.");
            }
            // TODO XML maybe?
        }
    }//end list()

    def enable(){
        log.info("Request to enable TrustInteroperabilityProfile[${params.id}]...")
        TrustInteroperabilityProfile tip = TrustInteroperabilityProfile.findById(params.id);
        if( !tip )
            throw new ServletException("No such TrustInteroperabilityProfile: ${params.id}")
        tip.enabled = true;
        tip.save(failOnError: true, flush: true);
        flash.message = "Successfully enabled TrustInteroperabilityProfile: "+tip.name
        def statusJson = [status: 'SUCCESS', message: "Successfully enabled TrustInteroperabilityProfile: "+tip.name];
        render statusJson as JSON
    }

    def disable(){
        log.info("Request to disable TrustInteroperabilityProfile[${params.id}]...")
        TrustInteroperabilityProfile tip = TrustInteroperabilityProfile.findById(params.id);
        if( !tip )
            throw new ServletException("No such TrustInteroperabilityProfile: ${params.id}")
        tip.enabled = false;
        tip.save(failOnError: true, flush: true);
        flash.message = "Successfully disabled TrustInteroperabilityProfile: "+tip.name
        def statusJson = [status: 'SUCCESS', message: "Successfully disabled TrustInteroperabilityProfile: "+tip.name];
        render statusJson as JSON
    }


    def downloadCachedSource(){
        log.info("Request to download cached source of TrustInteroperabilityProfile[${params.id}]...")
        TrustInteroperabilityProfile tip = null;

        tip = TrustInteroperabilityProfile.findById(params.id);
        if( !tip ){
            tip = TrustInteroperabilityProfile.findByUri(params.id);
        }

        if( !tip )
            throw new ServletException("No such TrustInteroperabilityProfile: ${params.id}")

        return render(contentType: tip.source.mimeType, text: tip.source.content.toFile().text);
    }

    def view(){
        log.info("Request to view TrustInteroperabilityProfile[${params.id}]...")
        TrustInteroperabilityProfile tip = null;

        tip = TrustInteroperabilityProfile.findById(params.id);
        if( !tip ){
            tip = TrustInteroperabilityProfile.findByUri(params.id);
        }

        if( !tip )
            throw new ServletException("No such TrustInteroperabilityProfile: ${params.id}")

        log.debug("Resolving TrustInteroperabilityProfile from file...");
        edu.gatech.gtri.trustmark.v1_0.model.TrustInteroperabilityProfile tmfTip = null;
        File sourceFile = tip.source.content.toFile();
        tmfTip = FactoryLoader.getInstance(TrustInteroperabilityProfileResolver.class).resolve(sourceFile);

        withFormat{
            html {
                [databaseTip: tip, tmfTip: tmfTip]
            }
            xml {
                StringWriter writer = new StringWriter();
                FactoryLoader.getInstance(SerializerFactory.class).getXmlSerializer().serialize(tmfTip, writer)
                return render(contentType: 'text/xml', text: writer.toString());
            }
            json {
                StringWriter writer = new StringWriter();
                FactoryLoader.getInstance(SerializerFactory.class).getJsonSerializer().serialize(tmfTip, writer)
                return render(contentType: 'application/json', text: writer.toString());
            }
        }

    }//end view()


    def typeahead() {
        User user = User.findByUsername(((OAuth2AuthenticationToken) SecurityContextHolder.getContext().getAuthentication()).getName())
        log.debug("User[@|blue ${user}|@] searching[@|cyan ${params.q}|@] via TIP typeahead...")

        def results = TrustInteroperabilityProfile.findByUri(params.q)

        if (!results) {
            // TODO Instead of this, integrate the searchable plugin
            def criteria = TrustInteroperabilityProfile.createCriteria();
            results = criteria {
                or {
                    like("name", '%' + params.q + '%')
                    like("description", '%' + params.q + '%')
                    like("tipVersion", '%' + params.q + '%')
                }
                maxResults(25)
                order("name", "asc")
            }
        }

        withFormat {
            html {
                throw new ServletException("NOT SUPPORTED")
            }
            xml {
                render results as XML
            }
            json {
                def resultsJSON = []
                results.each{ result ->
                    resultsJSON.add([
                        id: result.id,
                        name: result.name,
                        description: result.description,
                        tipVersion: result.tipVersion
                    ])
                }
                render resultsJSON as JSON
            }
        }

    }//end typeahead


    //==================================================================================================================
    //  Private Helper Methods
    //==================================================================================================================


}//end TrustmarkDefinitionController

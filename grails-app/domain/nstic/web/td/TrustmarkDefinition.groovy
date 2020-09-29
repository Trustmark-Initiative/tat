package nstic.web.td

import nstic.web.BinaryObject
import org.apache.commons.lang.StringUtils

import java.util.regex.Pattern

class TrustmarkDefinition {

    public static final String SUPERSEDES_DELIM = "|@|"

    static transients = ['uniqueDisplayName', 'sortedSteps', 'supersedesList', 'supersededByList']

    static search = {
        name index: 'yes', boost: 2.0
        description index: 'yes', boost: 2.0
    }

    String baseUri; // Where this TD was cached from.
    /**
     * The unique identifier for this TD, and in theory at least, where it is located on the Internet.  Note - you
     * should check the cachedUrl value for where it was actually retrieved from.
     */
    String uri; // Also "identifier"
    /**
     * The TD could have been downloaded from a place on the Internet different from it's URI value.  That will be
     * stored here.  This is extremely useful for things like testing.
     */
    String cachedUrl;
    String referenceAttributeName;
    String name;
    String tdVersion;
    String description // XHTML possible here
    Date publicationDateTime;
    String criteriaPreface;
    String assessmentPreface;

    /**
     * A delimited list of URIs for TDs which are superseded by this TD.  @see SUPERSEDES_DELIM for the delimiter value.
     */
    String supersedes

    /**
     * A delimited list of URIs for TDs which supersede this TD.  @see SUPERSEDES_DELIM for the delimiter value.
     */
    String supersededBy;

    /**
     * Whether or not the source has deprecated this trustmark.  If deprecated = true, then enabled must implicitly be false.
     */
    Boolean deprecated = Boolean.FALSE;

    /**
     * The cached-date for this TD, when it was pulled down from the baseURI.
     */
    Date lastUpdated

    /**
     * The hash against the source content, which is used to detect any changes in the source on periodic re-scans.
     */
    String signature;

    BinaryObject source // This is the XML the trustmark definition was created from
    //==================================================================================================================
    // These fields are strictly relevant in the assessment tool
    //==================================================================================================================
    /**
     * Whether or not new assessments can be created against using this definition.  If false, then it will NOT appear
     * in the dropdown list for a new assessment.
     */
    Boolean enabled = Boolean.TRUE

    /**
     * If true, it indicates that the TD is disabled because a newer change has been detected.  This TD will need to be
     * manually inspected by the administrator.
     */
    Boolean outOfDate = Boolean.FALSE;

    static hasMany = [
        criteria: ConformanceCriterion,
        assessmentSteps: AssessmentStep
    ]

    static constraints = {
        baseUri(nullable: false, blank: false, maxSize: 65535)
        uri(nullable: false, blank: false, maxSize: 65535)
        cachedUrl(nullable: true, blank: true, maxSize: 65535)
        name(nullable: false, blank: false, maxSize: 256)
        referenceAttributeName(nullable: false, blank: false, maxSize: 256)
        tdVersion(nullable: false, blank: false, maxSize: 128)
        description(nullable: false, blank: false, maxSize: Integer.MAX_VALUE)
        publicationDateTime(nullable: false)
        source(nullable: true)
        enabled(nullable: false)
        deprecated(nullable: false)
        lastUpdated(nullable: true)
        signature(nullable: false, blank: false, maxSize: 65535)
        assessmentSteps(nullable: true)
        criteria(nullable: true)
        criteriaPreface(nullable: true, blank: true, maxSize: 65535)
        assessmentPreface(nullable: true, blank: true, maxSize: 65535)
        supersedes(nullable: true, blank: true, maxSize: 65535)
        supersededBy(nullable: true, blank: true, maxSize: 65535)
        outOfDate(nullable: false)
    }

    static mapping = {
        table(name: 'trustmark_definition')
        baseUri(type: 'text')
        uri(type: 'text')
        cachedUrl(type: 'text')
        description(type: 'text')
        criteriaPreface(type: 'text')
        assessmentPreface(type: 'text')
        signature(type: 'text')
        supersedes(type: 'text')
        supersededBy(type: 'text')
    }

    /**
     * Returns a name which also contains the version, should be unique so that a user can uniquely identify the td
     * among it's peers and older versions.
     */
    public String getUniqueDisplayName() {
        return this.name + ", " + this.tdVersion;
    }

    public List<AssessmentStep> getSortedSteps() {
        List<AssessmentStep> steps = []
        steps.addAll(this.assessmentSteps);
        Collections.sort(steps, {s1, s2 ->
            return s1.stepNumber.compareTo(s2.stepNumber);
        } as Comparator);
        return steps;
    }

    public void setSupersedesList(List<String> supersedesUris){
        if( supersedesUris?.size() > 0 ){
            StringBuilder builder = new StringBuilder();
            for( int i = 0; i < supersedesUris.size(); i++){
                String uri = supersedesUris.get(i);
                builder.append(uri);
                if( i < (supersedesUris.size() - 1) )
                    builder.append(SUPERSEDES_DELIM);
            }
            this.supersedes = builder.toString();
        }
    }

    public List<String> getSupersedesList() {
        List<String> supersedesList = []
        if(StringUtils.isNotBlank(this.supersedes) ){
            String[] uris = this.supersedes.split(Pattern.quote(SUPERSEDES_DELIM))
            for( String uri : uris ){
                supersedesList.add(uri);
            }
        }
        return supersedesList
    }
    public void setSupersededByList(List<String> supersededByUris){
        if( supersededByUris?.size() > 0 ){
            StringBuilder builder = new StringBuilder();
            for( int i = 0; i < supersededByUris.size(); i++){
                String uri = supersededByUris.get(i);
                builder.append(uri);
                if( i < (supersededByUris.size() - 1) )
                    builder.append(SUPERSEDES_DELIM);
            }
            this.supersededBy = builder.toString();
        }
    }

    public List<String> getSupersededByList() {
        List<String> supersededByList = []
        if(StringUtils.isNotBlank(this.supersededBy) ){
            String[] uris = this.supersededBy.split(Pattern.quote(SUPERSEDES_DELIM))
            for( String uri : uris ){
                supersededByList.add(uri);
            }
        }
        return supersededByList
    }

    public Map toJsonMap(boolean shallow = true) {
        def json = [
                id: this.id,
                name: this.name,
                uri: this.uri,
                version: this.tdVersion,
                description: this.description,
                enabled: this.enabled
        ]
        if( !shallow ){
            // TODO Create rest of data model...
        }
        return json;
    }

}//end TrustmarkDefinition

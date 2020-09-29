package nstic.web.tip

import nstic.web.BinaryObject
import nstic.web.td.TrustmarkDefinition


/**
 * Holds the cached information for a TIP.  Note that because assessments can occur on a TIP, the tool must cache and
 * distinguish a TIP.
 * <br/><br/>
 * Created by brad on 12/3/15.
 */
class TrustInteroperabilityProfile {

    static transients = ['sortedTipReferences', 'uniqueDisplayName']

    static search = {
        name index: 'yes', boost: 2.0
        description index: 'yes', boost: 2.0
    }

    Date lastUpdated; // Date this TIP was originally cached by this tool.  The system should present only the most recent versions based on this field.

    /**
     * Represents the base URI for this tip, ie the web services URL endpoint from where this TIP was retrieved.
     */
    String baseUri;

    /*
     * The following few fields just allow the system to quickly and easily find and display TIPs with some simple
     * information, as well as calculate/display TIP logic if necessary.
     */
    String uri // AKA the "identifier"
    String cachedUrl // Where this thing was TRULY downloaded from.
    String name
    String tipVersion
    String description
    Date publicationDateTime
    String tipExpression

    BinaryObject source // This is the source the TIP data cache was created from (JSON or XML)

    /**
     * Contains the base 64 encoded signature of this TIP, to do comparisons against future versions to determine if changes occurred.
     */
    String signature

    /**
     * If false, then this TIP should not be allowed to be assessed.
     */
    Boolean enabled = Boolean.TRUE
    /**
     * If true, this cached TIP does not match it's remote TIP and the Administrator should take action to remedy the
     * situation.
     */
    Boolean outOfDate = Boolean.FALSE


    static hasMany = [
            references: TIPReference
    ]

    static constraints = {
        lastUpdated(nullable: true)
        baseUri(nullable: false, blank: false, maxSize: 5096)
        uri(nullable: false, blank: false, maxSize: 5096)
        cachedUrl(nullable: true, blank: true, maxSize: 65535)
        name(nullable: false, blank: false, maxSize: 255)
        tipVersion(nullable: false, blank: false, maxSize: 64)
        description(nullable: true, blank: true, maxSize: 65535)
        publicationDateTime(nullable: false)
        source(nullable: false)
        signature(nullable: false, blank: false, maxSize: 65535)
        tipExpression(nullable: false, blank: false, maxSize: 65535)
        references(nullable: true)
        enabled(nullable: false)
        outOfDate(nullable: false)
    }

    static mapping = {
        table name: 'tip'
        uri(type: 'text')
        baseUri(type: 'text')
        cachedUrl(type: 'text')
        description(type: 'text')
        signature(type: 'text')
        tipExpression(type: 'text')
        source(columng: 'source_ref')
    }

    static mappedBy = [references: 'owningTIP']


    public List<TIPReference> getSortedTipReferences() {
        List<TIPReference> tipRefs = []
        tipRefs.addAll(this.references);
        Collections.sort(tipRefs, {TIPReference ref1, TIPReference ref2 ->
            return ref1.referenceName.compareToIgnoreCase(ref2.referenceName);
        } as Comparator);
        return tipRefs;
    }

    /**
     * Returns a name which also contains the version, should be unique so that a user can uniquely identify the td
     * among it's peers and older versions.
     */
    public String getUniqueDisplayName() {
        return this.name + ", " + this.tipVersion;
    }


}//end TrustInteroperabilityProfile
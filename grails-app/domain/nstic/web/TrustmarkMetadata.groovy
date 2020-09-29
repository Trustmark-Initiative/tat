package nstic.web

/**
 * Contains a set of Trustmark Metadata which is used while generating Trustmarks for an Assessment.
 * <br/><br/>
 * Created by brad on 5/5/16.
 */
class TrustmarkMetadata implements Comparable<TrustmarkMetadata> {

    public void setData(CreateTrustmarkMetadataCommand command){
        this.name = command.name;
        this.description = command.description;
//        this.generatorClass = command.generatorClass;
//        this.identifierPattern = command.identifierPattern;
//        this.identifierPattern = command.identifierPattern;
        this.policyUrl = command.policyUrl;
        this.relyingPartyAgreementUrl = command.relyingPartyAgreementUrl;
//        this.statusUrlPattern = command.statusUrlPattern;
        this.timePeriodNoExceptions = command.timePeriodNoExceptions;
        this.timePeriodWithExceptions = command.timePeriodWithExceptions;
        this.provider = Organization.get(command.organizationId);
        this.defaultSigningCertificateId = command.defaultSigningCertificateId
    }

    public void setData(EditTrustmarkMetadataCommand command){
        this.name = command.name;
        this.description = command.description;
//        this.generatorClass = command.generatorClass;
//        this.identifierPattern = command.identifierPattern;
//        this.identifierPattern = command.identifierPattern;
        this.policyUrl = command.policyUrl;
        this.relyingPartyAgreementUrl = command.relyingPartyAgreementUrl;
//        this.statusUrlPattern = command.statusUrlPattern;
        this.timePeriodNoExceptions = command.timePeriodNoExceptions;
        this.timePeriodWithExceptions = command.timePeriodWithExceptions;
        this.provider = Organization.get(command.organizationId);
        this.defaultSigningCertificateId = command.defaultSigningCertificateId
    }


    /**
     * Should have a meaningful name that uniquely identifies this set of metadata.  Very short and to the point.
     */
    String name
    /**
     * A more thorough explanation of what this set of metadata contains in case the user needs additional guidance.
     */
    String description

    /**
     * The name of a Class which implements {@link nstic.TrustmarkIdentifierGenerator} for generating unqiue trustmark
     * identifiers.
     */
    String generatorClass;
    /**
     * Holds the pattern (ie, the URL) for the Trustmark identifiers.  The value @IDENTIFIER@ is replaced in this string,
     * as well as @URLSAFE_IDENTIFIER@.
     */
    String identifierPattern = "https://nief.gfipm.net/trustmarks/@URLSAFE_IDENTIFIER@";
    /**
     * URL for the trustmark policy.  Ie, https://nief.gfipm.net/policies/nief-trustmark-policy-1.0.pdf
     */
    String policyUrl = "https://nief.gfipm.net/policies/nief-trustmark-policy-1.0.pdf";
    /**
     * The Url pointing to the relying party agreement URL.
     */
    String relyingPartyAgreementUrl = "https://nief.gfipm.net/policies/nief-trp-agreement-1.0.pdf";
    /**
     * The pattern used for the status URL.  The value @IDENTIFIER@ is replaced in this string,
     * as well as @URLSAFE_IDENTIFIER@.
     */
    String statusUrlPattern = "https://nief.gfipm.net/trustmarks/@URLSAFE_IDENTIFIER@/status";
    /**
     * The number of months a Trustmark should remain valid, assuming it has no exceptions.
     */
    Integer timePeriodNoExceptions = 36;

    /**
     * The number of months a Trustmark should remain valid when it DOES have exceptions.
     */
    Integer timePeriodWithExceptions = 6;
    /**
     * The organization which Trustmarks should be issued against.
     */
    Organization provider;

    /**
     * The id of the default X509 signing certificate to be used to generate the
     * Trustmark's XML Signature.
     */
    Long defaultSigningCertificateId


    static constraints = {
        name(nullable: false, blank: false, maxSize: 128)
        description(nullable: true, blank: true, maxSize: 65535)
        generatorClass(nullable: false, blank: false, maxSize: 1024)
        identifierPattern(nullable: false, blank: false, maxSize: 1024)
        policyUrl(nullable: false, blank: false, maxSize: 65535)
        relyingPartyAgreementUrl(nullable: false, blank: false, maxSize: 65535)
        statusUrlPattern(nullable: false, blank: false, maxSize: 65535)
        timePeriodNoExceptions(nullable: false)
        timePeriodWithExceptions(nullable: false)
        provider(nullable: false)
        defaultSigningCertificateId(nullable: false)
    }

    static mapping = {
        description(type: 'text')
        generatorClass(type: 'text')
        identifierPattern(type: 'text')
        policyUrl(type: 'text')
        relyingPartyAgreementUrl(type: 'text')
        statusUrlPattern(type: 'text')
    }

    public String toString(){
        return name;
    }

    @Override
    int compareTo(TrustmarkMetadata o) {
        return this.name.compareToIgnoreCase(o.getName());
    }

    public int hashCode(){
        return this.name.hashCode();
    }

}

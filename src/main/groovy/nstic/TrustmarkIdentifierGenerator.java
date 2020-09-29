package nstic;

/**
 * Used to generate the next identifier in sequence for new Trustmarks created by the assessment tool.
 */
public interface TrustmarkIdentifierGenerator {

    /**
     * Generates the next identifier for the next trustmark.
     * @return a string complaint with the trustmark framework spec, @tf:id attribute.
     */
    public String generateNext();

}//end TrustmarkIdentifierGenerator
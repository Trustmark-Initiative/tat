package nstic;

import java.util.UUID;

/**
 * Generates Trustmark identifier strings based on java.util.UUID random values.
 */
public class UUIDTrustmarkIdentifierGenerator implements TrustmarkIdentifierGenerator {

    @Override
    public String generateNext() {
        String nextUuid = UUID.randomUUID().toString().toUpperCase();
        return nextUuid;
    }

}

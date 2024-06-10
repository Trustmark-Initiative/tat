package nstic.web

import nstic.util.X500PrincipalWrapper
import spock.lang.Specification

/**
 * Tests for {@link X500PrincipalWrapper}.
 */
class X500PrincipalWrapperSpec extends Specification {

    /**
     * Test to ensure all X.500 attributes from a complete distinguished name are correctly parsed and accessible.
     */
    def "it should correctly parse and provide access to all X500 attributes"() {
        given: "a distinguished name string"
        String distinguishedName = "CN=John Doe, OU=Development, O=Company Inc., L=New York, ST=New York, C=US"
        X500PrincipalWrapper wrapper = new X500PrincipalWrapper(distinguishedName)

        expect: "all attributes are correctly parsed and accessible"
        wrapper.getCommonName() == "John Doe"
        wrapper.getOrganizationalUnit() == "Development"
        wrapper.getOrganization() == "Company Inc."
        wrapper.getLocality() == "New York"
        wrapper.getState() == "New York"
        wrapper.getCountry() == "US"
    }

    /**
     * Test to verify that missing attributes in the DN string are handled gracefully, returning null for those not present.
     */
    def "it should handle incomplete distinguished names gracefully"() {
        given: "a distinguished name string with only CN and C"
        String distinguishedName = "CN=Jane Doe, C=CA"
        X500PrincipalWrapper wrapper = new X500PrincipalWrapper(distinguishedName)

        expect: "parses available attributes and others return null"
        wrapper.getCommonName() == "Jane Doe"
        wrapper.getOrganizationalUnit() == null
        wrapper.getOrganization() == null
        wrapper.getLocality() == null
        wrapper.getState() == null
        wrapper.getCountry() == "CA"
    }

    /**
     * Test to ensure that the class throws a RuntimeException when presented with an invalid distinguished name format.
     */
    def "it should throw an exception for invalid distinguished name formats"() {
        given: "an invalid distinguished name string"
        String distinguishedName = "CNJohn Doe, O=Company Inc."

        when: "the X500PrincipalWrapper is created with invalid format"
        def wrapper = new X500PrincipalWrapper(distinguishedName)

        then: "a RuntimeException is thrown due to parsing error"
        thrown(RuntimeException) // this will test that a RuntimeException is thrown
    }
}

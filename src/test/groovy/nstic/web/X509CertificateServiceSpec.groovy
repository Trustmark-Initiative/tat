package nstic.web

import assessment.tool.X509CertificateService
import org.bouncycastle.asn1.x500.X500Name
import spock.lang.Specification
import grails.testing.services.ServiceUnitTest
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.SecureRandom

class X509CertificateServiceSpec extends Specification implements ServiceUnitTest<X509CertificateService> {

    def setup() {
    }

    def cleanup() {
    }

    def "generateCertificate method should create a valid X509 certificate"() {
        given: "A key pair generator"
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(2048, new SecureRandom())
        KeyPair keyPair = keyPairGenerator.generateKeyPair()

        and: "A certificate service instance"
        X509CertificateService service = new X509CertificateService()

        def dn = "CN=trustmarkinitiative.org,C=US,ST=Kentucky,L=Douglasville,O=Trustmark Initiative,OU=ICL"

        X500Name issuer = new X500Name(dn);

        // There is no guarantee that the order of the distinguished name elements will be in the same order
        // so we normalize the order here
        def expectedComponents = dn.split(',').collect { it.trim() }.sort()

        when: "generateCertificate is called with valid parameters"
        def cert = service.generateCertificate(dn, keyPair, 365,
                "SHA256withRSA", new BigInteger("123456"))

        then: "The certificate should be correctly generated"
        cert != null

        def issuerComponents = cert.issuerX500Principal.name.split(',').collect { it.trim() }.sort()
        def subjectComponents = cert.subjectX500Principal.name.split(',').collect { it.trim() }.sort()
        issuerComponents == expectedComponents
        subjectComponents == expectedComponents

        cert.serialNumber.toString() == "123456"
        cert.notBefore <= new Date() && cert.notAfter > new Date()
        cert.keyUsage[0]  // Checks if the digital signature is allowed
        cert.keyUsage[1]  // Checks if non-repudiation is allowed
        cert.keyUsage[5]  // Checks if the key can be used for signing other certificates
    }
}

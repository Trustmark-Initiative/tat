package nstic.web

class SigningCertificate {

    static belongsTo = [organization: Organization]

    int id
    String distinguishedName
    String commonName
    String localityName
    String stateOrProvinceName
    String countryName
    String emailAddress
    String organizationName
    String organizationalUnitName
    String serialNumber
    String thumbPrint
    String thumbPrintWithColons
    String privateKeyPem
    String x509CertificatePem
    String filename

    Date dateCreated
    String certificatePublicUrl
    Boolean defaultCertificate = Boolean.FALSE

    // deferred functionality
//    Boolean revoked = Boolean.FALSE

    static constraints = {
        organization(nullable: false)
        commonName(nullable: false, blank: false, maxSize: 255)
        distinguishedName(nullable: false, blank: false, maxSize: 255)
        localityName(nullable: true, blank: true, maxSize: 255)
        stateOrProvinceName(nullable: false, blank: false, maxSize: 255)
        countryName(nullable: false, blank: false, maxSize: 255)
        emailAddress(nullable: false, blank: false, maxSize: 255)
        organizationName(nullable: false, blank: false, maxSize: 255)
        organizationalUnitName(nullable: true, blank: true, maxSize: 255)
        thumbPrint(nullable: false, blank: false, maxSize: 255)
        thumbPrintWithColons(nullable: false, blank: false, maxSize: 255)
        certificatePublicUrl(nullable: false, blank: false, maxSize: 255)
        privateKeyPem(nullable: false, blank: false, size:0..65535)
        x509CertificatePem(nullable: false, blank: false, size:0..65535)
        filename(nullable: false, blank: false, maxSize: 255)
        defaultCertificate(null: false)

        // deferred functionality
//        revoked(null: false)
    }

    static mapping = {
        table(name:'signing_certificates')
        organization(column: 'organization_ref')
        privateKeyPem(type:'text', column: 'private_key')
        x509CertificatePem(type:'text', column: 'x509_certificate')
        distinguishedName(type:'text', column: 'distinguished_name')
        sort defaultCertificate: "desc"
    }

}//end SigningCertificate

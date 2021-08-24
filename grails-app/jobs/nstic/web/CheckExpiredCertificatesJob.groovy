package nstic.web

import assessment.tool.X509CertificateService
import edu.gatech.gtri.trustmark.v1_0.impl.util.TrustmarkMailClientImpl
import nstic.TATPropertiesHolder
import nstic.util.AssessmentToolProperties
import nstic.util.PasswordUtil
import nstic.web.assessment.TrustmarkStatus
import org.apache.commons.lang.StringUtils

import java.security.cert.X509Certificate

/**
 * Scans all the certificates associated to each organization and checks the
 * expiration. For expired certificates, the status will be changed to EXPIRED
 * and an email will be sent to the email set in the certificate. If the
 * certificate's email is different than the organization's contact email,
 * an email will also be sent to the organization's contact.
 */

class CheckExpiredCertificatesJob {


    //==================================================================================================================
    // Job Specifics
    //==================================================================================================================
    def concurrent = false
    def description = "Scans all the certificates associated to each organization and checks the \n" +
            " certificate expiration. Then, for expired certificates, changes the status accordingly."
    def groovyPageRenderer

    static triggers = {
        cron cronExpression: "0 0 1 ? * MON-FRI"
    }

    //==================================================================================================================
    // Execute entry point
    //==================================================================================================================
    void execute() {
        log.info("Executing ${this.getClass().getSimpleName()}...")

        Calendar now = Calendar.getInstance()

        Integer expirationWarningPeriodInDays = Integer.parseInt(AssessmentToolProperties.getProperties().getProperty(
                "trustmark.certificate.default.expirationWarningPeriod")) ?: 30

        X509CertificateService certService = new X509CertificateService()

        def organizations = Organization.findAll()

        organizations.each { org ->
            def certificates = org.certificates.findAll()
            certificates.each { cert ->

                X509Certificate x509Certificate = certService.convertFromPem(cert.x509CertificatePem)

                Date notAfter = x509Certificate.getNotAfter()
                Calendar expiration = Calendar.getInstance()
                expiration.setTime(notAfter)

                Calendar expirationWarning = Calendar.getInstance()
                expirationWarning.setTime(notAfter)
                expirationWarning.add(Calendar.DATE, -expirationWarningPeriodInDays)

                if(cert.status == SigningCertificateStatus.ACTIVE && expiration.getTimeInMillis() < now.getTimeInMillis()) {
                    log.warn("Detected an expired certificate.  Updating status...")

                    cert.status = SigningCertificateStatus.EXPIRED
                    cert.revokedTimestamp = now.getTime()
                    cert.revokedReason = "The Assessment Tool System has automatically revoked this certificate because it has expired."

                    log.info("Saving ${cert.distinguishedName}...")

                    SigningCertificate.withTransaction {
                        cert.save(failOnError: true, flush: true)
                    }

                    String message = expiredMessage(cert)

                    String subject = "The following signing X509 certificate has expired: ${cert.distinguishedName}."

                    // email certificate subscriber
                    emailCertificateSubscriber(cert.emailAddress, cert, subject, message)

                    // email organization's contact if email different from certificate's
                    if (cert.emailAddress != org.primaryContact.email) {
                        emailCertificateSubscriber(org.primaryContact.email, cert, subject, message)
                    }
                } else if(cert.status == SigningCertificateStatus.ACTIVE && expirationWarning.getTimeInMillis() < now.getTimeInMillis()) {
                    log.warn("Certificate about to expire...")

                    String message = expirationWarningMessage(cert)

                    String subject = "The following signing X509 certificate is about to expire: ${cert.distinguishedName}."

                    // email certificate subscriber
                    emailCertificateSubscriber(cert.emailAddress, cert, subject, message)

                    // email organization's contact if email different from certificate's
                    if (cert.emailAddress != org.primaryContact.email) {
                        emailCertificateSubscriber(org.primaryContact.email, cert, subject, message)
                    }
                }
            }
        }

    }//end execute()

    // email certificate holder/ organization's contact
    void emailCertificateSubscriber(String email, SigningCertificate cert, String subject, String message) {

        if( StringUtils.isEmpty(email) )  {
            log.warn("email is empty.")
        } else {
            log.debug("Sending certificate expiration email to ${email}...")

            TrustmarkMailClientImpl emailClient = new TrustmarkMailClientImpl()

            emailClient
                    .setUser(TATPropertiesHolder.getProperties().getProperty(TrustmarkMailClientImpl.SMTP_USER))
                    .setPswd(TATPropertiesHolder.getProperties().getProperty(TrustmarkMailClientImpl.SMTP_PSWD))
                    .setSmtpHost(TATPropertiesHolder.getProperties().getProperty(TrustmarkMailClientImpl.SMTP_HOST))
                    .setSmtpPort(TATPropertiesHolder.getProperties().getProperty(TrustmarkMailClientImpl.SMTP_PORT))
                    .setFromAddress(TATPropertiesHolder.getProperties().getProperty(TrustmarkMailClientImpl.FROM_ADDRESS))
                    .setSmtpAuthorization(Boolean.parseBoolean(TATPropertiesHolder.getProperties().getProperty(TrustmarkMailClientImpl.SMTP_AUTH)))
                    .setSubject(subject)
                    .addRecipient(email)
                    .setText(message)
        }
    }

    String expirationWarningMessage(SigningCertificate cert) {
        StringBuilder sb = new StringBuilder();
        sb.append("The following Signing Certificate is about to expire: ${cert.distinguishedName} ");
        sb.append("Link: ${getSigningCertificateUrl(cert.id)}");

        log.debug("CheckExpiredCertificatesJob::expirationWarningMessage: ${sb.toString()}...")

        return sb.toString();
    }

    String expiredMessage(SigningCertificate cert) {
        StringBuilder sb = new StringBuilder();
        sb.append("The following Signing Certificate has expired: ${cert.distinguishedName} ");
        sb.append("Link: ${getSigningCertificateUrl(cert.id)}");

        log.debug("CheckExpiredCertificatesJob::expiredMessage: ${sb.toString()}...")

        return sb.toString();
    }

    private String getSigningCertificateUrl(int certId) {
        StringBuilder sb = new StringBuilder()
        def baseAppUrl = AssessmentToolProperties.getProperties().getProperty(AssessmentToolProperties.BASE_URL)
        sb.append(baseAppUrl)
        sb.append("/signingCertificates/view/${certId}")

        return sb.toString()
    }
}/* End CheckExpiredCertificatesJob */

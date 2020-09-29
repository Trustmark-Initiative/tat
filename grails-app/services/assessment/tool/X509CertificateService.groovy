package assessment.tool

import sun.security.x509.*

import javax.xml.bind.DatatypeConverter
import java.security.cert.*
import java.security.*
import java.security.interfaces.RSAPrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import java.util.regex.Matcher
import java.util.regex.Pattern
import javax.crypto.spec.*
import javax.crypto.*

class X509CertificateService {

    // X509 key usage extension strings (based on OID = 2.5.29.15)
    public static ArrayList<String> KeyUsageStringList = ["Digital Signature", "Non-Repudiation", "Key Encipherment",
                                                          "Data Encipherment", "Key Agreement", "Key Certificate Sign",
                                                          "CRL Sign", "Encipher Only", "Decipher Only"]
    /**
     * Create a self-signed X.509 certificate
     *
     * @param dn        the X.509 Distinguished Name, eg "CN=Test, L=London, C=GB"
     * @param pair      the KeyPair
     * @param days      how many days from now the Example is valid for
     * @param algorithm the signing algorithm, eg "SHA1withRSA"
     */
    X509Certificate generateCertificate(String dn, KeyPair pair, int days, String algorithm) {

        PrivateKey privkey = pair.getPrivate()
        X509CertInfo info = new X509CertInfo()
        Date from = new Date()
        Date to = new Date(from.getTime() + days * 86400000l)
        CertificateValidity interval = new CertificateValidity(from, to)
        BigInteger sn = new BigInteger(64, new SecureRandom())
        X500Name owner = new X500Name(dn)

        info.set(X509CertInfo.VALIDITY, interval)
        info.set(X509CertInfo.SERIAL_NUMBER, new CertificateSerialNumber(sn))
        info.set(X509CertInfo.SUBJECT, owner)
        info.set(X509CertInfo.ISSUER, owner)
        info.set(X509CertInfo.KEY, new CertificateX509Key(pair.getPublic()))
        info.set(X509CertInfo.VERSION, new CertificateVersion(CertificateVersion.V3))
        AlgorithmId algo = new AlgorithmId(AlgorithmId.md5WithRSAEncryption_oid)
        info.set(X509CertInfo.ALGORITHM_ID, new CertificateAlgorithmId(algo))

        // Key usage extensions
        KeyUsageExtension keyUsageExtension = new KeyUsageExtension()
        keyUsageExtension.set(KeyUsageExtension.DIGITAL_SIGNATURE, true)
        keyUsageExtension.set(KeyUsageExtension.NON_REPUDIATION, true)
        keyUsageExtension.set(KeyUsageExtension.KEY_CERTSIGN, true)

        CertificateExtensions certificateExtensions = new CertificateExtensions()
        certificateExtensions.set(keyUsageExtension.getExtensionId().toString(), keyUsageExtension)
        info.set(X509CertInfo.EXTENSIONS, certificateExtensions)

        // Sign the cert to identify the algorithm that's used.
        X509CertImpl cert = new X509CertImpl(info)
        cert.sign(privkey, algorithm)

        // Update the algorithm, and resign.
        algo = (AlgorithmId) cert.get(X509CertImpl.SIG_ALG)
        info.set(CertificateAlgorithmId.NAME + "." + CertificateAlgorithmId.ALGORITHM, algo)
        cert = new X509CertImpl(info)
        cert.sign(privkey, algorithm)

        return cert
    }

    byte[] encryptPrivateKey(PrivateKey key, String password) {
        // extract the encoded private key, this is an unencrypted PKCS#8 private key
        byte[] encodedprivkey = key.getEncoded()

        // We must use a PasswordBasedEncryption algorithm in order to encrypt the private key,
        // you may use any common algorithm supported by openssl, you can check them in the
        // openssl documentation http://www.openssl.org/docs/apps/pkcs8.html
        String MYPBEALG = "PBEWithSHA1AndDESede"  // TODO: Change to PBEWITHHMACSHA512ANDAES_256 and test with OpenSSL

        int count = 20;// hash iteration count
        SecureRandom random = new SecureRandom()
        byte[] salt = new byte[8]
        random.nextBytes(salt)

        // Create PBE parameter set
        PBEParameterSpec pbeParamSpec = new PBEParameterSpec(salt, count)
        PBEKeySpec pbeKeySpec = new PBEKeySpec(password.toCharArray())
        SecretKeyFactory keyFac = SecretKeyFactory.getInstance(MYPBEALG)
        SecretKey pbeKey = keyFac.generateSecret(pbeKeySpec)

        Cipher pbeCipher = Cipher.getInstance(MYPBEALG)

        // Initialize PBE Cipher with key and parameters
        pbeCipher.init(Cipher.ENCRYPT_MODE, pbeKey, pbeParamSpec)

        // Encrypt the encoded Private Key with the PBE key
        byte[] ciphertext = pbeCipher.doFinal(encodedprivkey)

        // Now construct PKCS #8 EncryptedPrivateKeyInfo object
        AlgorithmParameters algparms = AlgorithmParameters.getInstance(MYPBEALG)
        algparms.init(pbeParamSpec)
        EncryptedPrivateKeyInfo encinfo = new EncryptedPrivateKeyInfo(algparms, ciphertext)

        // the DER encoded PKCS#8 encrypted key
        byte[] encryptedPkcs8 = encinfo.getEncoded()

        return encryptedPkcs8
    }

    String convertToPem(X509Certificate cert) throws CertificateEncodingException {
        String cert_begin = "-----BEGIN CERTIFICATE-----\n";
        String end_cert = "\n-----END CERTIFICATE-----";

        byte[] derCert = cert.getEncoded();
        String pemCertPre = new String(Base64.getEncoder().encode(derCert));
        pemCertPre = insert(pemCertPre, "\n", 64)
        String pemCert = cert_begin + pemCertPre + end_cert;
        return pemCert;
    }

    String convertToPemPrivateKey(PrivateKey key) throws CertificateEncodingException {
        String key_begin = "-----BEGIN PRIVATE KEY-----\n"
        String key_end = "\n-----END PRIVATE KEY-----"

        byte[] derCert = key.getEncoded()
        String pemKeyPre = new String(Base64.getEncoder().encode(derCert))
        pemKeyPre = insert(pemKeyPre, "\n", 64)

        String pemKey = key_begin + pemKeyPre + key_end;

        return pemKey
    }

    X509Certificate convertFromPem(String pemCert) {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");

        ByteArrayInputStream inStream = new ByteArrayInputStream(pemCert.getBytes())
        Certificate cert = cf.generateCertificate(inStream)
        X509Certificate x509Cert = (X509Certificate)cert

        return x509Cert
    }

    RSAPrivateKey getPrivateKeyFromPem(String key) throws IOException, GeneralSecurityException {
        String privateKeyPEM = key
        privateKeyPEM = privateKeyPEM.replace("-----BEGIN PRIVATE KEY-----\n", "")
        privateKeyPEM = privateKeyPEM.replace("-----END PRIVATE KEY-----", "")

        byte[] encoded = DatatypeConverter.parseBase64Binary(privateKeyPEM)

        KeyFactory kf = KeyFactory.getInstance("RSA")
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded)
        RSAPrivateKey privKey = (RSAPrivateKey) kf.generatePrivate(keySpec)
        return privKey
    }

    String convertToPemPrivateKeyFromBytes(byte[] pemPrivateKeyBytes) throws CertificateEncodingException {
        String pemKey = new String(Base64.getEncoder().encode(pemPrivateKeyBytes))
        pemKey = insert(pemKey, "\n", 64)

        return pemKey
    }

    void pemEncodeCertToFile(String certFilePath, String pemCert) throws Exception{
        String cert_begin = "-----BEGIN CERTIFICATE-----\n"
        String end_cert = "\n-----END CERTIFICATE-----"

        String cert = cert_begin + pemCert + end_cert

        FileWriter fw = new FileWriter(certFilePath)
        fw.write(cert)
        fw.close()
    }

    void pemEncodePrivateKeyToFile(String privateKeyFilePath, String pemKey) throws Exception{
        String key_begin = "-----BEGIN ENCRYPTED PRIVATE KEY-----\n"
        String key_end = "\n-----END ENCRYPTED PRIVATE KEY-----"

        String pemFileKey = key_begin + pemKey + key_end

        FileWriter fw = new FileWriter(pemFileKey)
        fw.write(pemCert)
        fw.close()
    }

    public String insert(String text, String insert, int period) {
        Pattern p = Pattern.compile("(.{" + period + "})", Pattern.DOTALL)
        Matcher m = p.matcher(text)
        return m.replaceAll("\$1" + insert)
    }

    public String getThumbPrint(Certificate cert)
            throws NoSuchAlgorithmException, CertificateEncodingException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] der = cert.getEncoded();
        md.update(der);
        byte[] digest = md.digest();

        return hexify(digest);
    }

    public String getThumbPrintWithColons(Certificate cert)
            throws NoSuchAlgorithmException, CertificateEncodingException {

        String hex = getThumbPrint(cert);

        int length = hex.length()
        String s1 = colonize(hex.substring(0, (int)(length / 2)))
        String s2 = colonize(hex.substring((int)(length / 2), length))

        // split the string over 2 lines for easier view
        return s1 + "\n" + s2
    }

    public String colonize(String hex) {

        StringBuffer fp = new StringBuffer()
        int i = 0;
        fp.append(hex.substring(i, i + 2))
        while ((i += 2) < hex.length())
        {
            fp.append(':');
            fp.append(hex.substring(i, i + 2))
        }

        return fp.toString()
    }

    public String hexify(byte[] bytes) {

        char[] hexDigits = ['0', '1', '2', '3', '4', '5', '6', '7',
                            '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'];

        StringBuffer buf = new StringBuffer(bytes.length * 2);

        for (int i = 0; i < bytes.length; ++i) {
            buf.append(hexDigits[(bytes[i] & 0xf0) >> 4]);
            buf.append(hexDigits[bytes[i] & 0x0f]);
        }

        return buf.toString();
    }

}/* end X509CertificateService */
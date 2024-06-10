package nstic.util;

import javax.security.auth.x500.X500Principal;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import java.util.HashMap;
import java.util.Map;

/**
 * Wraps {@link X500Principal} to provide easy access to X.500 Principal properties
 * like common name, organizational unit, etc.
 */
public class X500PrincipalWrapper {
    private Map<String, String> attributes = new HashMap<>();

    /**
     * Constructs a new X500PrincipalWrapper using a distinguished name string.
     *
     * @param distinguishedName the X.500 distinguished name string
     */
    public X500PrincipalWrapper(String distinguishedName) {
        X500Principal x500Principal = new X500Principal(distinguishedName);
        parseX500Principal(x500Principal);
    }

    /**
     * Parses the X500Principal to extract properties into a map and individual fields.
     *
     * @param principal the X500Principal to parse
     */
    private void parseX500Principal(X500Principal principal) {
        try {
            LdapName ldapName = new LdapName(principal.getName(X500Principal.RFC2253));
            for (Rdn rdn : ldapName.getRdns()) {
                attributes.put(rdn.getType(), rdn.getValue().toString());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error parsing X500 Principal", e);
        }
    }

    public String getCommonName() { return attributes.get("CN"); }
    public String getOrganizationalUnit() { return attributes.get("OU"); }
    public String getOrganization() { return attributes.get("O"); }
    public String getLocality() { return attributes.get("L"); }
    public String getState() { return attributes.get("ST"); }
    public String getCountry() { return attributes.get("C"); }

    @Override
    public String toString() {
        return "X500PrincipalWrapper{" +
                "CN='" + getCommonName() + '\'' +
                ", OU='" + getOrganizationalUnit() + '\'' +
                ", O='" + getOrganization() + '\'' +
                ", L='" + getLocality() + '\'' +
                ", ST='" + getState() + '\'' +
                ", C='" + getCountry() + '\'' +
                '}';
    }
}
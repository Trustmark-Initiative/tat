package nstic.web

import org.apache.commons.lang.StringUtils
import org.gtri.fj.data.Option
import org.json.JSONArray

import java.util.stream.Collectors

import static org.gtri.fj.data.Option.fromNull

class User {

	String username
    String nameFamily
    String nameGiven
    String contactEmail
    String roleArrayJson

    // My custom additions...  Taken from contact information in Trustmark Definition XML (as of 2014-03-24)
    ContactInformation contactInformation
    Organization organization

	static constraints = {
		username blank: false, unique: true
        nameFamily nullable: true, length: 1000
        nameGiven nullable: true, length: 1000
        contactEmail nullable: true, length: 1000
        roleArrayJson nullable: true, length: 1000
        contactInformation nullable: true
        organization nullable: true
	}

	static mapping = {
        table name: 'assessment_user'
		password column: '`pass_hash`'
        organization column: 'organization_ref'
        contactInformation column: 'contact_information_ref'
	}

    static final Option<User> findByUsernameHelper(final String username) {
        fromNull(findByUsername(username))
    }

    User saveAndFlushHelper() {
        User.withTransaction {
            save(flush: true, failOnError: true)
        }
    }

    public static Boolean hasAdmin() {
        List<User> users = User.findAll()

        users.forEach(user -> {
            if (user.roleArrayJson) {
                JSONArray rolesJsonArray = new JSONArray(user.roleArrayJson)

                return rolesJsonArray.toList()
                        .stream()
                        .filter(role -> Role.fromValue((String) role).isPresent())
                        .map(role -> Role.fromValue((String) role).get())
                        .anyMatch(role -> Role.ROLE_ADMIN == role)
            }
        })

        return false
    }

    public Boolean isAdmin() {

        if (StringUtils.isNotEmpty(this.roleArrayJson)) {
            JSONArray rolesJsonArray = new JSONArray(this.roleArrayJson);

            return rolesJsonArray.toList()
                    .stream()
                    .filter(role -> Role.fromValue((String) role).isPresent())
                    .map(role -> Role.fromValue((String) role).get())
                    .anyMatch(role -> Role.ROLE_ADMIN == role)
        }

        return false
    }

    public Boolean isUser() {
        if (StringUtils.isNotEmpty(this.roleArrayJson)) {
        JSONArray rolesJsonArray = new JSONArray(roleArrayJson);

        return rolesJsonArray.toList()
                .stream()
                .filter(role -> Role.fromValue((String) role).isPresent())
                .map(role -> Role.fromValue((String)role).get())
                .anyMatch(role -> Role.ROLE_USER == role);
        }

        return false
    }


    public Boolean isReportOnly() {
        if (StringUtils.isNotEmpty(this.roleArrayJson)) {
        JSONArray rolesJsonArray = new JSONArray(roleArrayJson);

        return rolesJsonArray.toList()
                .stream()
                .filter(role -> Role.fromValue((String) role).isPresent())
                .map(role -> Role.fromValue((String)role).get())
                .anyMatch(role -> Role.ROLE_REPORTS_ONLY == role);
        }

        return false
    }


    Set<Role> getAuthorities() {
        JSONArray rolesJsonArray = new JSONArray(roleArrayJson);

        Set<Role> rolesSet = rolesJsonArray.toList()
                .stream()
                .map(role -> Role.fromValue((String)role).get())
                .collect(Collectors.toSet());

        return rolesSet
    }

    String toString() {
        return username
    }

    public Map toJsonMap(boolean shallow = true) {
        def json = [
                id: this.id,
                username: this.username,
                contactInformation: this.contactInformation?.toJsonMap(shallow),
                organization: this.organization?.toJsonMap(shallow)
        ] as java.lang.Object
        if( !shallow ){
            // TODO Create rest of data model...
        }
        return json as Map
    }//end toJsonMap

}//end User

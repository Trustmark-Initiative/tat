package nstic.web

import grails.testing.gorm.DomainUnitTest
import org.json.JSONArray
import spock.lang.Specification

class RolesSpec extends Specification implements DomainUnitTest<User> {

    def setup() {
    }

    def cleanup() {
    }

    void "test something"() {
        expect:"fix me"
            true == true
    }

    void "test roles"() {
        List<String> roles = Arrays.asList("trpt-admin","offline_access","default-roles-trustmark",
                "uma_authorization","userinfo-role","tpat-admin","tbr-admin","tat-admin","trpt-admin",
                "offline_access","default-roles-trustmark","uma_authorization","userinfo-role","tpat-admin",
                "tbr-admin","tat-admin");
        JSONArray jsonArray = new JSONArray(roles);

        User user = new User(roleArrayJson: jsonArray)

        expect:"fix me"
            user.isAdmin() == true
    }
}

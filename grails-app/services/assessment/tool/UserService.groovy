package assessment.tool;

import grails.gorm.transactions.Transactional
import nstic.web.ContactInformation;
import nstic.web.User;
import org.gtri.fj.data.List
import org.gtri.fj.data.Option;
import org.json.JSONArray
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;


@Transactional
public class UserService {

    public static String currentUserName() {

        Option<User> userOption = User.findByUsernameHelper(((OAuth2AuthenticationToken) SecurityContextHolder.getContext().getAuthentication()).getName())

        return userOption.some().getUsername()
    }

    public void insertOrUpdateHelper(
            final String username,
            final String nameFamily,
            final String nameGiven,
            final String contactEmail,
            final List<String> roleList) {

        User.withTransaction {
            User user = User.findByUsernameHelper(username).orSome(new User());
            user.setUsername(username);
            user.setNameFamily(nameFamily);
            user.setNameGiven(nameGiven);
            user.setContactEmail(contactEmail);

            // create contact information
            ContactInformation contact = new ContactInformation()
            contact.email = contactEmail
            contact.responder = nameGiven + " " + nameFamily
            contact.save(failOnError: true, flush: true)
            user.contactInformation = contact

            user.setRoleArrayJson(new JSONArray(roleList).toString());

            user.saveAndFlushHelper();
        };
    }
}

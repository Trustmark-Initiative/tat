package assessment.tool;

import grails.gorm.transactions.Transactional
import nstic.web.ContactInformation;
import nstic.web.Organization;
import nstic.web.Role;
import nstic.web.User;
import org.gtri.fj.data.List;
import org.json.JSONArray;
import java.util.Optional;

@Transactional
public class UserService {

    public void insertOrUpdateHelper(
            final String username,
            final String nameFamily,
            final String nameGiven,
            final String contactEmail,
            final List<String> roleList) {

        User.withTransaction {
            // get Provider organization and assign to the user as the default
            Optional<Organization> defaultOrganization = Organization.findByIsTrustmarkProviderHelper(true);

            User user = User.findByUsernameHelper(username).orSome(new User());
            user.setUsername(username);
            user.setNameFamily(nameFamily);
            user.setNameGiven(nameGiven);
            user.setContactEmail(contactEmail);

            if (defaultOrganization.isPresent()) {
                user.setOrganization(defaultOrganization.get());
            }

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

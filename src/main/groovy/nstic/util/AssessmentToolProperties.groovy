package nstic.util

import nstic.web.Role
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.io.ClassPathResource

import java.util.regex.Pattern

/**
 * A static class for accessing the assessment tool properties file.
 * <br/><br/>
 * @author brad
 * @date 6/16/17
 */
class AssessmentToolProperties {
    private static final Logger log = LoggerFactory.getLogger(AssessmentToolProperties.class)

    public static final String BUNDLE_NAME = "tat_config.properties"

    final static String BASE_URL = "tf.base.url"
    final static String PUBLIC_TRUSTMARK_API = "tf.public.api"

    private static Properties RESOURCES = new Properties()
    static {
        try {
            log.info("Initializing Assessment Tool Properties Holder...")
            ClassPathResource classPathResource = new ClassPathResource(BUNDLE_NAME)
            InputStream inputStream = classPathResource.getInputStream()
            String propertiesTxt = inputStream.text
            if( log.isDebugEnabled() )
                log.debug("### OUTPUT OF ${BUNDLE_NAME}: \n"+propertiesTxt+"\n### END OF ${BUNDLE_NAME}")
            RESOURCES.load(new StringReader(propertiesTxt))
        } catch(Throwable t){
            log.error("Error reading Assessment Tool Config properties!", t)
            throw new RuntimeException("Cannot load Assssment Tool Config properties: "+BUNDLE_NAME, t)
        }
    }

    static ResourceBundle getBundle(){return null;}

    static Properties getProperties(){return RESOURCES;}

    //==================================================================================================================
    //  Data specific methods.
    //==================================================================================================================
    static String getFilesDirectory(){
        return getString("assessment.tool.filesdir", "/tmp/tfam")
    }

    static URL getRegistryUrl(){
        if (getString("registry.url") == null)  {
            return null;
        }
        return new URL(getString("registry.url"))
    }

    static void setRegistryUrl(String urls){
        String currentUrl = getString("registry.url");
        if(currentUrl == null)  {
            RESOURCES.setProperty("registry.url", urls);
        } else  {
            RESOURCES.setProperty("registry.url", currentUrl +"|" + urls);
        }
    }

    static QuartzConfig getQuartzConfig(String jobName){
        return new QuartzConfig(this.getProperties(), jobName)
    }

    static String getPublicTrustmarkApi()  {
        return getProperties().getProperty(BASE_URL)+getProperties().getProperty(PUBLIC_TRUSTMARK_API)
    }

    static String getPublicDocumentApi()  {
        return getProperties().getProperty(BASE_URL)+"/public/documents/pdf"
    }

    static Map getDefaultAccountData(){
        def accountData = [contacts: [], orgs: [], users: []]

        Integer contactCount = getNumber("contact.count")
        for( int i = 0; i < contactCount; i++ ){
            int contactNum = i+1;
            Map contact = [
                    responder: getString("contact.${contactNum}.responder"),
                    email: getString("contact.${contactNum}.email"),
                    telephone: getString("contact.${contactNum}.telephone", ""),
                    mailingAddress: getString("contact.${contactNum}.mailingAddress", ""),
                    notes: getString("contact.${contactNum}.notes", "")
            ]
            accountData.contacts.add(contact);
        }

        Integer orgCount = getNumber("org.count");
        for( int i = 0; i < orgCount; i++ ){
            int orgnum = i+1;
            Map org = [
                    name: getString("org.${orgnum}.name"),
                    identifier: getString("org.${orgnum}.identifier"),
                    uri: getString("org.${orgnum}.uri"),
                    contact: getString("org.${orgnum}.contact")
            ]
            accountData.orgs.add(org);
        }

        Integer userCount = getNumber("user.count");
        for( int i = 0; i < userCount; i++ ){
            int usernum = i+1;

            Map user = [
                    username: getString("user.${usernum}.username"),
                    password: getString("user.${usernum}.password"),
                    contact: getString("user.${usernum}.contact"),
                    org: getString("user.${usernum}.org"),
                    roles: getString("user.${usernum}.roles", Role.ROLE_USER)
            ]

            accountData.users.add(user);
        }

        return accountData;
    }


    //==================================================================================================================
    //  Generalized Data Methods
    //==================================================================================================================
    private static boolean exists(String property){
        if( getProperties() ){
            return getProperties().containsKey(property);
        }else{
            return false;
        }
    }

    private static List<URL> getUrlList(String property){
        if( exists(property) ){
            List stringsList = getStringList(property);
            List urlsList = []
            for( String urlString : stringsList ){
                urlsList.add(new URL(urlString));
            }
            return urlsList;
        }else{
            return []
        }
    }

    private static List<String> getStringList(String property){
        if( exists(property) ){
            List theReturnList = []
            String theStringList = getString(property);
            String[] stringLines = theStringList.split(Pattern.quote("|"));
            if( stringLines != null && stringLines.length > 0 ) {
                for (String aStringValue : stringLines) {
                    theReturnList.add(aStringValue.trim());
                }
            }
            return theReturnList;
        }else{
            return []
        }
    }
    private static String getString(String property){
        return getString(property, null);
    }
    private static String getString(String property, String defaultValue){
        Properties props = getProperties();
        if( props != null ){
            try{
                String val = props.getProperty(property);
                if( val != null && val.trim().length() > 0 ){
                    return val.trim();
                }else{
                    return defaultValue;
                }
            }catch(Throwable t){
                return defaultValue;
            }
        }else{
            return defaultValue;
        }
    }

    private static Number getNumber(String property){
        return getNumber(property, null);
    }
    private static Number getNumber(String property, Number defaultValue){
        String value = getString(property, null);
        if( value ){
            try{
                return Double.parseDouble(value);
            }catch(Throwable T){
                return defaultValue;
            }
        }else{
            return defaultValue;
        }
    }

    private static boolean getBoolean(String property){
        return getBoolean(property, null);
    }
    private static boolean getBoolean(String property, Number defaultValue){
        String value = getString(property, null);
        if( value ){
            try{
                return Boolean.parseBoolean(value);
            }catch(Throwable T){
                return defaultValue;
            }
        }else{
            return defaultValue;
        }
    }


}

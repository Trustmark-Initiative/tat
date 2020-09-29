package nstic

/**
 * Meant to be a static class which gives access to the system's configuration via tat_config.properties.
 * Created by brad on 5/5/16.
 */
class TATPropertiesHolder {

    private static Properties properties;

    static void setProperties(Properties p){
        properties = new Properties();
        Enumeration<String> propNames = p.propertyNames();
        while( propNames.hasMoreElements() ){
            String propertyName = propNames.nextElement();
            properties.setProperty(propertyName, p.getProperty(propertyName));
        }
    }

    static Properties getProperties() {
        if( properties == null )
            throw new NullPointerException("Properties have not yet been initialized!");
        return properties; // TODO: Do we need to return a copy for thread safety's sake?
    }



}//end TATPropertiesHolder
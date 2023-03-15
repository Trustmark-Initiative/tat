package tmf.assessment

import grails.boot.GrailsApp
import grails.boot.config.GrailsAutoConfiguration
import org.grails.core.cfg.GroovyConfigPropertySourceLoader
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.EnvironmentAware
import org.springframework.context.annotation.ComponentScan
import org.springframework.core.env.Environment
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean
import org.springframework.core.env.MapPropertySource
import org.springframework.core.env.PropertiesPropertySource
import org.springframework.core.env.PropertySource
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource


@SpringBootApplication
@ComponentScan("nstic")
class Application extends GrailsAutoConfiguration implements EnvironmentAware {

    static void main(String[] args) {
        info("Starting grails application...");
        GrailsApp.run(Application, args)
    }

    @Override
    void setEnvironment(Environment environment) {
        info("Adding config.location.* properties to configuration...");
        Enumeration<String> propertyNames = System.getProperties().propertyNames() as Enumeration<String>;

        while( propertyNames.hasMoreElements() ){
            String property = propertyNames.nextElement();
            if( property.toLowerCase().startsWith("config.location") ){
                String configPath = System.getProperty(property);
                info("  Loading Config From [${property}] = [${configPath}]");
                if (configPath) {
                    Resource resourceConfig = new FileSystemResource(configPath);
                    String filename = resourceConfig.getFilename().toLowerCase();
                    if( filename.endsWith(".groovy") ){
                        GroovyConfigPropertySourceLoader loader = new GroovyConfigPropertySourceLoader();
                        PropertySource props = loader.load(resourceConfig.getFilename(), resourceConfig, environment.getActiveProfiles()[0] as java.util.List<String>) as PropertySource;
                        environment.propertySources.addFirst(props);
                    }else if( filename.endsWith(".yaml") || filename.endsWith(".yml") ) {
                        YamlPropertiesFactoryBean propertyFactoryBean = new YamlPropertiesFactoryBean();
                        propertyFactoryBean.setResources(resourceConfig);
                        propertyFactoryBean.afterPropertiesSet();
                        Properties properties = propertyFactoryBean.getObject();
                        environment.propertySources.addFirst(new PropertiesPropertySource(property, properties))
                    }else if( filename.endsWith(".properties") ){
                        Properties props = new Properties();
                        props.load(resourceConfig.getInputStream());
                        environment.propertySources.addFirst(new PropertiesPropertySource(property, props))
                    }else{
                        // TODO Handle loading .properties files...
                        throw new UnsupportedOperationException("Not yet implemented, Unknown type for file["+configPath+"] from "+property);
                    }
                }
            }
        }
        def res = getClass().classLoader.getResourceAsStream('tat_config.properties')
        if( res ) {
            Properties props = new Properties()
            props.load(res)
            environment.propertySources.addFirst( new MapPropertySource("tat_config.properties", props as Map<String, Object>) )
        }

    }


    private static final void info(String msg){
        System.out.println(msg);
        System.out.flush();
    }
}
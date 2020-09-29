package nstic.web

import grails.artefact.Interceptor
import grails.databinding.converters.ValueConverter
import org.apache.commons.lang.StringUtils
import grails.core.GrailsClass
import org.grails.help.ParamConversion
import org.grails.help.ParamConversions
import org.springframework.context.ApplicationContext

import java.lang.annotation.Annotation
import java.lang.reflect.Method
import java.lang.reflect.Modifier

/**
 * Allows programmers in the AssessmentTool to define @ParamConversion annotations on their controller methods which
 * will take incoming parameters and map them to objects bound to the gorm session (or other things) and place them
 * back into different parameter names for use in the controller method.  See {@link ParamConversion} for more information
 */
class ObjectParsingInterceptor implements Interceptor {

    // Almost first
    int order = HIGHEST_PRECEDENCE - 10

    public ObjectParsingInterceptor(){
        matchAll()
    }



    /**
     * Allows you to ignore controllers which you don't want to autobind.  You should put your project's PREFIX
     * here so that the system can use that instead.  NOTE - this should be externalized into grails config.
     */
    public static final List<String> SUPPORTED_CONTROLLER_REGEX_LIST = ["nstic\\.web\\..*"];

    Boolean initialized = Boolean.FALSE;
    Map supportedControllers = null

    boolean before() {
        if( params.resetObjectParsingFilters && params.resetObjectParsingFilters.equalsIgnoreCase("true") ){
            synchronized (this) {
                log.warn("user has requested we re-initialize the ObjectParsingInterceptor...")
                initialized = Boolean.FALSE;
            }
        }

        Map gcWrapper = findController(controllerName + "Controller");
        List annotationDataList = gcWrapper?.methodMetadata?.get(actionName);
        if( annotationDataList && !annotationDataList.isEmpty() ){
            log.debug("Encountered annotation metadata for $controllerName:$actionName - performing parameter mapping...")
            annotationDataList.each{ ParamConversion paramConversion ->
                String paramName = paramConversion.paramName();
                Object paramValue = params.get(paramName);
                if( paramValue ){
                    def convertedValue = doConversion(paramValue, paramConversion);
                    log.debug("Storing value[${convertedValue}] into params['${paramConversion.storeInto()}']...")
                    params.put(paramConversion.storeInto(), convertedValue);
                }else{
                    log.info("Cannot convert parameter(name=${paramName}), it has no value.")
                }

            }
        }

        return true;
    }

    boolean after() {
        return true;
    }

    void afterView() {
        // no-op
    }


    private Object doConversion(Object paramValue, ParamConversion paramConversion){
        log.info("Converting parameter(name=${paramConversion.paramName()}, value=${paramValue}) into class ${paramConversion.toClass().getName()}...")
        ValueConverter valueConverter = null;
        if( StringUtils.isNotEmpty(paramConversion.usingConverter()) ){
            valueConverter = grailsApplication.mainContext.getBean(paramConversion.usingConverter());
            if( !valueConverter || !ValueConverter.isAssignableFrom(valueConverter.getClass()) )
                throw new UnsupportedOperationException("Cannot convert ${paramConversion.paramName()} to ${paramConversion.toClass().getSimpleName()}, because ${paramConversion.usingConverter()} is not a valid ValueConverter bean.")
        }else{
            ApplicationContext context = grailsApplication.mainContext;
            def valueConverterNames = context.getBeanNamesForType(ValueConverter.class);
            for( String valueConverterName : valueConverterNames ){
                ValueConverter nextValueConverter = context.getBean(valueConverterName);
                if( nextValueConverter.getTargetType().equals(paramConversion.toClass()) && nextValueConverter.canConvert(paramValue) ){
                    valueConverter = nextValueConverter;
                    break;
                }
            }
        }

        log.debug("Invoking value converter: ${valueConverter.class.name} to convert value[${paramValue}]...")
        Object convertedValue = valueConverter.convert(paramValue);
        return convertedValue;
    }

    /**
     * Finds the information for the given controller name and action name.
     */
    def findController(controllerName){
        initialize();
        return supportedControllers.get(controllerName);
    }//end findController()

    void initialize() {
        synchronized (this) {
            if (!initialized) {

                def supportedGrailsClassMap = [:]
                log.debug("Initializing supported controllers list...");
                GrailsClass[] controllerClasses = grailsApplication.getControllerClasses();
                controllerClasses.each { GrailsClass gc ->
                    if (shouldAutoCheckParams(gc)) {
                        supportedGrailsClassMap.put(gc.propertyName, [grailsClass: gc]);
                    }
                }

                supportedControllers = [:]
                supportedGrailsClassMap.keySet().each{ String propertyName ->
                    Map gcWrapper = supportedGrailsClassMap.get(propertyName);
                    Class controllerClass = gcWrapper.grailsClass.getClazz();
                    // Now we iterate each method
                    def actionNameWrapper = [:]
                    controllerClass.methods.each{ Method method ->
                        if( Modifier.isPublic(method.modifiers) ){

                            def annotationsList = []
                            Annotation[] annotations = method.getAnnotations();
                            for( Annotation annotation : annotations ){
                                if( annotation instanceof ParamConversion ){
                                    ParamConversion paramConversion = (ParamConversion) annotation;
                                    annotationsList.add( paramConversion );
                                }else if( annotation instanceof ParamConversions ){
                                    ParamConversions conversions = (ParamConversions) annotation;
                                    annotationsList.addAll(conversions.value());
                                }
                            }

                            if( !annotationsList.isEmpty() ){
                                actionNameWrapper.put(method.name, annotationsList);
                            }
                        }else{
                            log.debug("Ignoring method ${method.name} because it is protected or private.")
                        }
                    }//end each action on this controller
                    if( !actionNameWrapper.keySet().isEmpty() ){
                        gcWrapper.put('methodMetadata', actionNameWrapper);
                        supportedControllers.put(propertyName, gcWrapper);
                    }

                }//end each controller

                log.info("Initialization successful!  Configuration: ");
                supportedControllers.keySet().each{ controllerAlias ->
                    log.info("Controller[$controllerAlias]: ")
                    Map controllerMetadata = supportedControllers.get(controllerAlias)
                    controllerMetadata.methodMetadata?.keySet().each{ methodName ->
                        log.info("  Method[${methodName}] -> Annotations=${controllerMetadata.methodMetadata.get(methodName)}")
                    }
                }
                initialized = Boolean.TRUE;
            }
        }
    }


    List<String> getSupportedControllerRegexList(){
        return SUPPORTED_CONTROLLER_REGEX_LIST;
    }

    boolean shouldAutoCheckParams(GrailsClass gc){
        String fullname = gc.getFullName();
        def list = getSupportedControllerRegexList();
        if( list == null || list.isEmpty() )
            return true; // we support everything.
        for( String regex : list ){
            if( fullname.matches(regex) ){
                log.debug("Supporting controller[$fullname] because of regex[$regex]")
                return true;
            }
        }
        log.info("Not Supporting controller[$fullname], because it does not match any regex")
        return false;
    }//end matchesList()

}/* end ObjectParsingInterceptor */
package org.grails.help;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * When used on a grails controller action, informs the system of a conversion that should take place.
 * <br/><br/>
 * Created by brad on 12/14/14.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ParamConversion {

    /**
     * The name of the parameter to convert.
     */
    String paramName();

    /**
     * Convert the value to this type.
     */
    Class toClass();

    /**
     * When converted, store the new value in the parameter map under this name.
     */
    String storeInto();

    /**
     * If not empty, this will refer to a spring bean name to use to convert (must be an instanceof {@link org.grails.databinding.converters.ValueConverter}).
     */
    String usingConverter() default "";

}//end paramConversion
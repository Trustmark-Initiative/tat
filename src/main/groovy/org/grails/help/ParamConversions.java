package org.grails.help;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by brad on 12/14/14.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ParamConversions {

    /**
     * Stores the list of {@link org.grails.help.ParamConversion} annotations.
     */
    ParamConversion[] value();

}

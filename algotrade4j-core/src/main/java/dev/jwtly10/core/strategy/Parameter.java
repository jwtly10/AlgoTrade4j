package dev.jwtly10.core.strategy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to define a parameter for a trading strategy.
 * This annotation can be used to give the optimiser access to edit parameters of a strategy.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Parameter {
    /**
     * The name of the parameter.
     *
     * @return the name of the parameter
     */
    String name() default "";

    /**
     * A description of the parameter.
     *
     * @return the description of the parameter
     */
    String description() default "";

    /**
     * The value of the parameter.
     *
     * @return the value of the parameter
     */
    String value() default "";

    /**
     * The enum class that the parameter can take values from, if applicable.
     *
     * @return the enum class array
     */
    Class<? extends Enum<?>>[] enumClass() default {};
}
package dev.jwtly10.core.strategy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to define a parameter for a trading strategy.
 * This annotation can be used to give the optimiser access to edit parameters of a strategy.
 * Note. The parameter.name should be the same as the variable its annotation.
 * This is used to match the parameter to the variable.
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
     * The group of the parameter
     * used for UI customisations
     *
     * @return the group of the parameter
     */
    String group() default "";

    /**
     * The enum class that the parameter can take values from, if applicable.
     *
     * @return the enum class array
     */
    Class<? extends Enum<?>>[] enumClass() default {};
}
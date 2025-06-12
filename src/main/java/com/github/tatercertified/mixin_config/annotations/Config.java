package com.github.tatercertified.mixin_config.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Marks a Mixin Config element
 */
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Config {
    /**
     * The name to be shown in the config file
     */
    String name();

    /**
     * The enable status (true = enabled, false = disabled)
     */
    boolean defaultVal() default true;

    /**
     * The {@link Config#name()} of the dependents of this Mixin Method/Class
     */
    String[] dependencies() default {};
}

package com.github.tatercertified.mixin_config.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Config {
    String name();
    boolean defaultVal() default true;
    String[] dependencies() default {};
}

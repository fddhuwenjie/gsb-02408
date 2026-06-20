package com.gsb.apitest.step;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface Extract {

    String var();

    String from() default "$.data";

    String jsonPath() default "";
}

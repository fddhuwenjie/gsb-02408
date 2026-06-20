package com.gsb.apitest.step;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(ApiTestExtension.class)
public @interface ApiTest {

    String baseUrl() default "";

    String configFile() default "";
}

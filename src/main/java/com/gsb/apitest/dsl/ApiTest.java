package com.gsb.apitest.dsl;

import com.gsb.apitest.extension.ApiTestExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(ApiTestExtension.class)
public @interface ApiTest {
}

package com.junmoyu.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 授权验证注解
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface PreAuthorize {

    /**
     * SpEL 权限表达式
     *
     * @see <a href="https://docs.spring.io/spring-framework/reference/core/expressions.html">Spring Expression Language (SpEL)</a>
     */
    String value();
}

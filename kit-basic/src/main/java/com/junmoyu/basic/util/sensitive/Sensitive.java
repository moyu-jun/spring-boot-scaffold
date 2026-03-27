package com.junmoyu.basic.util.sensitive;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 脱敏注解
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@JsonSerialize(using = SensitiveSerialize.class)
public @interface Sensitive {

    /**
     * 脱敏数据类型, 非 Customer 时, 将忽略 prefixNoMaskLen 和 suffixNoMaskLen 和 maskStr
     *
     * @return 脱敏类型
     */
    SensitiveTypeEnum type() default SensitiveTypeEnum.CUSTOMER;

    /**
     * 前置不需要打码的长度
     *
     * @return 长度
     */
    int prefixNoMaskLen() default 0;

    /**
     * 后置不需要打码的长度
     *
     * @return 长度
     */
    int suffixNoMaskLen() default 0;

    /**
     * 用什么打码
     *
     * @return 打码符号
     */
    String maskStr() default "*";
}

package com.junmoyu.security.core;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

/**
 * 认证信息（使用 Java 21 record 实现不可变对象）
 */
public record Authentication(
        Long userId,
        String account,
        String accessToken,
        Collection<String> roles,
        Collection<String> authorities,
        Map<String, Object> detail
) implements Serializable {

    /**
     * 创建空的认证信息
     */
    public static Authentication empty() {
        return new Authentication(null, null, null, null, null, null);
    }

    /**
     * 判断是否已认证
     */
    public boolean isAuthenticated() {
        return userId != null;
    }
}


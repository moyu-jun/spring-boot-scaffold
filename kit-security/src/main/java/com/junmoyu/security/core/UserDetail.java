package com.junmoyu.security.core;

/**
 * UserDetail
 */
public interface UserDetail {

    /**
     * token 认证
     *
     * @param token accessToken
     * @return 认证结果
     */
    Authentication authentication(String token);
}

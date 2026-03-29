package com.junmoyu.iam.model.constant;

/**
 * 认证授权相关常量
 */
public class AuthConst {

    /**
     * auth:session:{sid}
     */
    public static final String SESSION_PREFIX = "auth:session:";

    /**
     * auth:access:{token}
     */
    public static final String ACCESS_TOKEN_PREFIX = "auth:access:";

    /**
     * auth:refresh:{token}
     */
    public static final String REFRESH_TOKEN_PREFIX = "auth:refresh:";

    /**
     * auth:user:{uid}:sessions
     * auth:user:{uid}:permissions
     */
    public static final String USER_PREFIX = "auth:user:";

    /**
     * auth:user:{uid}:sessions
     */
    public static final String USER_SESSION_SUFFIX = ":sessions";

    /**
     * auth:user:{uid}:permissions
     */
    public static final String USER_PERMISSION_SUFFIX = ":permissions";

    /**
     * auth:user:{uid}:perm_ver
     */
    public static final String USER_PERMISSION_VERSION_SUFFIX = ":perm_ver";

    public static String buildAccessTokenKey(String accessToken) {
        return ACCESS_TOKEN_PREFIX + accessToken;
    }

    public static String buildRefreshTokenKey(String refreshToken) {
        return REFRESH_TOKEN_PREFIX + refreshToken;
    }

    public static String buildSessionKey(String sessionId) {
        return SESSION_PREFIX + sessionId;
    }

    public static String buildUserSessionKey(Long userId) {
        return USER_PREFIX + userId + USER_SESSION_SUFFIX;
    }

    public static String buildUserPermissionKey(Long userId) {
        return USER_PREFIX + userId + USER_PERMISSION_SUFFIX;
    }

    public static String buildUserPermissionVersionKey(Long userId) {
        return USER_PREFIX + userId + USER_PERMISSION_VERSION_SUFFIX;
    }
}

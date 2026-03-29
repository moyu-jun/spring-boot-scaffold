package com.junmoyu.iam.service;

import com.junmoyu.basic.util.RedisUtils;
import com.junmoyu.iam.model.constant.AuthConst;
import com.junmoyu.iam.model.request.UserPermissionCache;
import com.junmoyu.security.SecurityProperties;
import com.junmoyu.security.core.Authentication;
import com.junmoyu.security.core.UserDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * UserDetail 实现：从 Redis 会话读取登录信息，并加载权限缓存。
 */
@Service
@RequiredArgsConstructor
public class UserDetailServiceImpl implements UserDetail {

    private final RedisUtils redisUtils;
    private final SecurityProperties securityProperties;

    @Override
    public Authentication authentication(String token) {
        String sessionKey = AuthConst.buildSessionKey(token);
        UserSession session = redisUtils.get(sessionKey, UserSession.class);
        if (session == null) {
            return null;
        }

        String permissionKey = AuthConst.buildUserPermissionsKey(session.userId());
        UserPermissionCache permissionCache = redisUtils.get(permissionKey, UserPermissionCache.class);
        if (permissionCache == null) {
            permissionCache = new UserPermissionCache(java.util.List.of(), java.util.List.of());
        }

        // 滑动续期：请求有效时刷新会话和权限缓存的 TTL。
        long sessionTtlSeconds = securityProperties.getToken().getExpiration();
        redisUtils.expire(sessionKey, sessionTtlSeconds, TimeUnit.SECONDS);
        redisUtils.expire(permissionKey, sessionTtlSeconds * 2, TimeUnit.SECONDS);

        return new Authentication(
                session.userId(),
                session.account(),
                token,
                permissionCache.roles(),
                permissionCache.permissions(),
                Map.of(
                        "deviceType", session.deviceType(),
                        "loginTimestamp", session.loginTimestamp()
                )
        );
    }
}

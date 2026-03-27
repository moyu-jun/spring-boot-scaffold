package com.junmoyu.example.security.service;

import com.junmoyu.basic.util.RedisUtils;
import com.junmoyu.example.security.model.UserSession;
import com.junmoyu.security.core.Authentication;
import com.junmoyu.security.core.UserDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * UserDetail 实现 - 查询 Redis，滑动过期
 * <p>
 * 每次请求时从 Redis 查询用户会话信息，并重置过期时间（滑动过期）
 * </p>
 */
@Service
@RequiredArgsConstructor
public class UserDetailServiceImpl implements UserDetail {

    private final RedisUtils redisUtils;

    private static final long ACTIVE_TTL_MINUTES = 30;

    @Override
    public Authentication authentication(String token) {
        String tokenKey = "auth:token:" + token;
        UserSession session = redisUtils.get(tokenKey, UserSession.class);

        if (session == null) {
            // Token 不存在或已过期
            return null;
        }

        // 滑动过期：每次活跃请求重置 TTL
        redisUtils.expire(tokenKey, ACTIVE_TTL_MINUTES, TimeUnit.MINUTES);

        return new Authentication(
                session.userId(),
                session.account(),
                token,
                session.roles(),
                session.permissions(),
                Map.of()
        );
    }
}

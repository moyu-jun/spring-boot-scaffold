package com.junmoyu.iam.service;

import com.junmoyu.basic.util.RedisUtils;
import com.junmoyu.iam.model.constant.AuthConst;
import com.junmoyu.iam.model.dto.AccessTokenCache;
import com.junmoyu.iam.model.dto.UserPermissionCache;
import com.junmoyu.iam.model.enums.AuthCacheStatusEnum;
import com.junmoyu.security.core.Authentication;
import com.junmoyu.security.core.UserDetail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;

/**
 * UserDetail 实现 —— 高频鉴权入口，一次 Redis 读取完成认证与权限装配。
 *
 * <p>热点路径约束：只读一次 {@code auth:access:{token}}，权限走本地缓存。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailServiceImpl implements UserDetail {

    private final RedisUtils redisUtils;
    private final CacheService cacheService;

    @Override
    public Authentication authentication(String token) {
        // 1. 读取 access token（唯一一次 Redis 查询）
        String accessTokenKey = AuthConst.buildAccessTokenKey(token);
        AccessTokenCache accessTokenCache = redisUtils.get(accessTokenKey, AccessTokenCache.class);
        if (accessTokenCache == null) {
            return null;
        }

        // 2. 校验业务状态
        if (!AuthCacheStatusEnum.ACTIVE.name().equals(accessTokenCache.status())) {
            log.warn("Token 已被吊销: userId={}", accessTokenCache.userId());
            return null;
        }

        // 3. 从本地缓存加载权限（未命中时回源 Redis）
        UserPermissionCache permCache = cacheService.getPermissions(
                accessTokenCache.userId(), accessTokenCache.permVer());
        if (permCache == null) {
            log.warn("权限缓存缺失: userId={}, permVer={}", accessTokenCache.userId(), accessTokenCache.permVer());
            return null;
        }

        // 5. 构建认证对象
        return new Authentication(
                accessTokenCache.userId(),
                permCache.username(),
                token,
                permCache.roles() != null ? Collections.unmodifiableCollection(permCache.roles()) : Collections.emptyList(),
                permCache.permissions() != null ? Collections.unmodifiableCollection(permCache.permissions()) : Collections.emptyList(),
                Map.of("orgId", permCache.orgId() != null ? permCache.orgId() : 0L)
        );
    }
}

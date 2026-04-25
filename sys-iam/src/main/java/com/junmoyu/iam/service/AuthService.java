package com.junmoyu.iam.service;

import cn.hutool.core.util.IdUtil;
import com.junmoyu.basic.exception.AuthException;
import com.junmoyu.basic.model.AuthErrorCode;
import com.junmoyu.basic.util.RedisUtils;
import com.junmoyu.iam.model.constant.AuthConst;
import com.junmoyu.iam.model.dto.AccessTokenCache;
import com.junmoyu.iam.model.dto.AuthSessionCache;
import com.junmoyu.iam.model.dto.RefreshTokenCache;
import com.junmoyu.iam.model.dto.UserPermissionCache;
import com.junmoyu.iam.model.entity.UserEntity;
import com.junmoyu.iam.model.enums.AuthCacheStatusEnum;
import com.junmoyu.iam.model.response.TokenResponse;
import com.junmoyu.security.SecurityProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Auth service backed by Redis session model.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final RedisUtils redisUtils;
    private final PasswordEncoder passwordEncoder;
    private final SecurityProperties securityProperties;
    private final UserService userService;
    private final CacheService cacheService;

    /**
     * 账号密码登录
     */
    public TokenResponse loginPassword(String account, String password, String ip, String userAgent) {
        if (StringUtils.isBlank(account) || StringUtils.isBlank(password)) {
            throw new AuthException(AuthErrorCode.AUTH_FAILED);
        }

        UserEntity user = userService.getUserByAccount(account);
        if (user == null) {
            throw new AuthException(AuthErrorCode.AUTH_FAILED);
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new AuthException(AuthErrorCode.AUTH_FAILED);
        }

        long accessTokenTtlSeconds = securityProperties.getToken().getExpiration();
        long refreshTokenTtlSeconds = securityProperties.getToken().getRefreshExpiration();
        long now = System.currentTimeMillis();

        String sessionId = IdUtil.simpleUUID();
        String accessToken = IdUtil.simpleUUID();
        String refreshToken = IdUtil.simpleUUID();

        String userPermissionKey = AuthConst.buildUserPermissionKey(user.getId());
        String userPermissionVersionKey = AuthConst.buildUserPermissionVersionKey(user.getId());

        // perm_ver: 使用 increment 保证并发下原子的版本递增
        Long permVerObj = redisUtils.get(userPermissionVersionKey, Long.class);
        int permVer;
        if (permVerObj == null) {
            permVer = 1;
            redisUtils.set(userPermissionVersionKey, permVer);
        } else {
            permVer = permVerObj.intValue();
        }

        // 权限快照: 优先使用已有缓存，否则从 DB 加载
        UserPermissionCache userPermissionCache = redisUtils.get(userPermissionKey, UserPermissionCache.class);
        if (userPermissionCache == null) {
            List<String> roles = userService.getAllRoles(user.getId());
            List<String> permissions = userService.getAllPermissions(user.getId());
            userPermissionCache = new UserPermissionCache(user.getId(), user.getUsername(), user.getOrgId(),
                    roles, permissions, permVer, now);
            redisUtils.set(userPermissionKey, userPermissionCache, refreshTokenTtlSeconds);
        }

        // 写入 session
        String sessionKey = AuthConst.buildSessionKey(sessionId);
        AuthSessionCache authSessionCache = new AuthSessionCache(sessionId, user.getId(), accessToken, refreshToken,
                ip, userAgent, now, AuthCacheStatusEnum.ACTIVE.name());
        redisUtils.set(sessionKey, authSessionCache, refreshTokenTtlSeconds);

        // 维护用户的活跃 session 集合
        String userSessionKey = AuthConst.buildUserSessionKey(user.getId());
        redisUtils.sAdd(userSessionKey, sessionId);
        redisUtils.expire(userSessionKey, refreshTokenTtlSeconds);

        // 写入 access token
        String accessTokenKey = AuthConst.buildAccessTokenKey(accessToken);
        AccessTokenCache accessTokenCache = new AccessTokenCache(sessionId, user.getId(), accessToken, permVer, ip, userAgent,
                now, now + accessTokenTtlSeconds * 1000, AuthCacheStatusEnum.ACTIVE.name());
        redisUtils.set(accessTokenKey, accessTokenCache, accessTokenTtlSeconds);

        // 写入 refresh token
        String refreshTokenKey = AuthConst.buildRefreshTokenKey(refreshToken);
        RefreshTokenCache refreshTokenCache = new RefreshTokenCache(sessionId, user.getId(), now,
                now + refreshTokenTtlSeconds * 1000, AuthCacheStatusEnum.ACTIVE.name());
        redisUtils.set(refreshTokenKey, refreshTokenCache, refreshTokenTtlSeconds);

        log.info("登录成功: userId={}, sessionId={}", user.getId(), sessionId);
        return new TokenResponse(accessToken, refreshToken, accessTokenTtlSeconds);
    }

    /**
     * 退出登录：撤销 access token、refresh token 和 session，清理权限缓存。
     */
    public void logout(String accessToken) {
        if (StringUtils.isBlank(accessToken)) {
            return;
        }
        String accessTokenKey = AuthConst.buildAccessTokenKey(accessToken);
        AccessTokenCache accessTokenCache = redisUtils.get(accessTokenKey, AccessTokenCache.class);
        if (accessTokenCache == null) {
            return;
        }

        // 标记 access token 已吊销
        revokeAccessToken(accessTokenKey, accessTokenCache);

        String sessionKey = AuthConst.buildSessionKey(accessTokenCache.sid());
        AuthSessionCache sessionCache = redisUtils.get(sessionKey, AuthSessionCache.class);
        if (sessionCache != null) {
            // 吊销 refresh token
            String refreshTokenKey = AuthConst.buildRefreshTokenKey(sessionCache.refreshToken());
            revokeRefreshToken(refreshTokenKey, sessionCache);

            // 吊销 session
            revokeSession(sessionKey, sessionCache);

            // 从用户活跃 session 集合中移除
            String userSessionKey = AuthConst.buildUserSessionKey(accessTokenCache.userId());
            redisUtils.sRemove(userSessionKey, accessTokenCache.sid());
        }

        // 清理本地权限缓存
        cacheService.evictPermissions(accessTokenCache.userId());
        log.info("退出登录成功: userId={}, sessionId={}", accessTokenCache.userId(), accessTokenCache.sid());
    }

    private void revokeAccessToken(String key, AccessTokenCache cache) {
        Long remainingTtl = redisUtils.getExpire(key, TimeUnit.SECONDS);
        if (remainingTtl != null && remainingTtl > 0) {
            AccessTokenCache revoked = new AccessTokenCache(
                    cache.sid(), cache.userId(), cache.accessToken(), cache.permVer(),
                    cache.clientIp(), cache.userAgent(), cache.issuedAt(), cache.expiresIn(),
                    AuthCacheStatusEnum.REVOKED.name());
            redisUtils.set(key, revoked, remainingTtl, TimeUnit.SECONDS);
        }
    }

    private void revokeRefreshToken(String key, AuthSessionCache sessionCache) {
        Long remainingTtl = redisUtils.getExpire(key, TimeUnit.SECONDS);
        if (remainingTtl != null && remainingTtl > 0) {
            RefreshTokenCache revoked = new RefreshTokenCache(
                    sessionCache.sid(), sessionCache.userId(), 0L, 0L,
                    AuthCacheStatusEnum.REVOKED.name());
            redisUtils.set(key, revoked, remainingTtl, TimeUnit.SECONDS);
        }
    }

    private void revokeSession(String key, AuthSessionCache cache) {
        Long remainingTtl = redisUtils.getExpire(key, TimeUnit.SECONDS);
        if (remainingTtl != null && remainingTtl > 0) {
            AuthSessionCache revoked = new AuthSessionCache(
                    cache.sid(), cache.userId(), cache.accessToken(), cache.refreshToken(),
                    cache.clientIp(), cache.userAgent(), cache.loginAt(),
                    AuthCacheStatusEnum.REVOKED.name());
            redisUtils.set(key, revoked, remainingTtl, TimeUnit.SECONDS);
        }
    }

    /**
     * 刷新 access token：校验 refresh token 有效性，轮换新旧 token（Refresh Token Rotation）。
     */
    public TokenResponse refresh(String refreshToken, String ip, String userAgent) {
        if (StringUtils.isBlank(refreshToken)) {
            throw new AuthException(AuthErrorCode.AUTH_FAILED);
        }

        String refreshTokenKey = AuthConst.buildRefreshTokenKey(refreshToken);
        RefreshTokenCache refreshCache = redisUtils.get(refreshTokenKey, RefreshTokenCache.class);
        if (refreshCache == null || !AuthCacheStatusEnum.ACTIVE.name().equals(refreshCache.status())) {
            throw new AuthException(AuthErrorCode.AUTH_FAILED);
        }
        if (refreshCache.expiresIn() <= System.currentTimeMillis()) {
            throw new AuthException(AuthErrorCode.TOKEN_EXPIRED);
        }

        // 校验 session 是否仍然有效
        String sessionKey = AuthConst.buildSessionKey(refreshCache.sid());
        AuthSessionCache sessionCache = redisUtils.get(sessionKey, AuthSessionCache.class);
        if (sessionCache == null || !AuthCacheStatusEnum.ACTIVE.name().equals(sessionCache.status())) {
            throw new AuthException(AuthErrorCode.AUTH_FAILED);
        }

        long accessTokenTtlSeconds = securityProperties.getToken().getExpiration();
        long refreshTokenTtlSeconds = securityProperties.getToken().getRefreshExpiration();
        long now = System.currentTimeMillis();

        Long userId = refreshCache.userId();
        String sid = refreshCache.sid();

        // 读取当前权限版本和权限快照（可能已在 refresh 间隔内变更）
        String userPermissionKey = AuthConst.buildUserPermissionKey(userId);
        UserPermissionCache permCache = redisUtils.get(userPermissionKey, UserPermissionCache.class);
        int permVer;
        if (permCache != null) {
            permVer = permCache.permVer();
        } else {
            // 权限缓存缺失时回源 DB 重建（refresh 是低频控制面，DB 查询可接受）
            Long permVerObj = redisUtils.get(AuthConst.buildUserPermissionVersionKey(userId), Long.class);
            permVer = permVerObj != null ? permVerObj.intValue() : 1;
            UserEntity user = userService.getById(userId);
            String username = user != null ? user.getUsername() : String.valueOf(userId);
            List<String> roles = userService.getAllRoles(userId);
            List<String> permissions = userService.getAllPermissions(userId);
            permCache = new UserPermissionCache(userId, username, user != null ? user.getOrgId() : null,
                    roles, permissions, permVer, now);
            redisUtils.set(userPermissionKey, permCache, refreshTokenTtlSeconds);
        }

        // 签发新的 access token
        String newAccessToken = IdUtil.simpleUUID();
        String newAccessTokenKey = AuthConst.buildAccessTokenKey(newAccessToken);
        AccessTokenCache newAccessCache = new AccessTokenCache(sid, userId, newAccessToken, permVer, ip, userAgent,
                now, now + accessTokenTtlSeconds * 1000, AuthCacheStatusEnum.ACTIVE.name());
        redisUtils.set(newAccessTokenKey, newAccessCache, accessTokenTtlSeconds);

        // Refresh Token Rotation：吊销旧的 refresh token，签发新的
        String newRefreshToken = IdUtil.simpleUUID();
        String newRefreshTokenKey = AuthConst.buildRefreshTokenKey(newRefreshToken);
        RefreshTokenCache newRefreshCache = new RefreshTokenCache(sid, userId, now,
                now + refreshTokenTtlSeconds * 1000, AuthCacheStatusEnum.ACTIVE.name());
        redisUtils.set(newRefreshTokenKey, newRefreshCache, refreshTokenTtlSeconds);

        // 吊销旧的 refresh token
        revokeRefreshToken(refreshTokenKey, sessionCache);

        // 更新 session 中的 token 绑定
        AuthSessionCache updatedSession = new AuthSessionCache(
                sid, userId, newAccessToken, newRefreshToken,
                sessionCache.clientIp(), sessionCache.userAgent(), sessionCache.loginAt(),
                AuthCacheStatusEnum.ACTIVE.name());
        Long sessionTtl = redisUtils.getExpire(sessionKey, TimeUnit.SECONDS);
        if (sessionTtl != null && sessionTtl > 0) {
            redisUtils.set(sessionKey, updatedSession, sessionTtl, TimeUnit.SECONDS);
        }

        // 失效旧的本地权限缓存，使下一个请求重新加载
        cacheService.evictPermissions(userId);

        log.info("Token 刷新成功: userId={}, sessionId={}", userId, sid);
        return new TokenResponse(newAccessToken, newRefreshToken, accessTokenTtlSeconds);
    }
}
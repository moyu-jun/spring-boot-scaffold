package com.junmoyu.iam.service;

import cn.hutool.core.util.IdUtil;
import com.junmoyu.basic.exception.AuthException;
import com.junmoyu.basic.exception.BusinessException;
import com.junmoyu.basic.model.AuthErrorCode;
import com.junmoyu.basic.util.RedisUtils;
import com.junmoyu.iam.model.AuthCacheStatusEnum;
import com.junmoyu.iam.model.constant.AuthConst;
import com.junmoyu.iam.model.dto.AccessTokenCache;
import com.junmoyu.iam.model.dto.AuthSessionCache;
import com.junmoyu.iam.model.dto.RefreshTokenCache;
import com.junmoyu.iam.model.dto.UserPermissionCache;
import com.junmoyu.iam.model.entity.UserEntity;
import com.junmoyu.iam.model.response.TokenResponse;
import com.junmoyu.security.SecurityProperties;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Auth service backed by Redis session model.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final RedisUtils redisUtils;
    private final PasswordEncoder passwordEncoder;
    private final SecurityProperties securityProperties;
    private final UserService userService;

    /**
     * 账号密码登录
     */
    public TokenResponse loginPassword(String account, String password, String ip, String userAgent) {
        if (StringUtils.isBlank(account) || StringUtils.isBlank(password)) {
            throw new AuthException(AuthErrorCode.AUTH_FAILED);
        }
        // 用户校验
        UserEntity user = userService.getUserByAccount(account);
        if (user == null) {
            throw new AuthException(AuthErrorCode.AUTH_FAILED);
        }
        // 密码校验
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new AuthException(AuthErrorCode.AUTH_FAILED);
        }

        long accessTokenTtlSeconds = securityProperties.getToken().getExpiration();
        long refreshTokenTtlSeconds = securityProperties.getToken().getRefreshExpiration();

        String sessionId = IdUtil.simpleUUID();
        String accessToken = IdUtil.simpleUUID();
        String refreshToken = IdUtil.simpleUUID();

        String sessionKey = AuthConst.buildSessionKey(sessionId);
        String accessTokenKey = AuthConst.buildAccessTokenKey(accessToken);
        String refreshTokenKey = AuthConst.buildRefreshTokenKey(refreshToken);

        String userSessionKey = AuthConst.buildUserSessionKey(user.getId());
        String userPermissionKey = AuthConst.buildUserPermissionKey(user.getId());
        String userPermissionVersionKey = AuthConst.buildUserPermissionVersionKey(user.getId());

        // 处理权限版本缓存
        Integer permVer = redisUtils.get(userPermissionVersionKey, Integer.class);
        if (permVer == null) {
            permVer = 1;
            redisUtils.set(userPermissionVersionKey, permVer);
        }

        // 处理权限信息缓存
        UserPermissionCache userPermissionCache = redisUtils.get(userPermissionKey, UserPermissionCache.class);
        if (userPermissionCache == null) {
            List<String> roles = userService.getAllRoles(user.getId());
            List<String> permissions = userService.getAllPermissions(user.getId());

            userPermissionCache = new UserPermissionCache(user.getId(), user.getUsername(), user.getOrgId(),
                    roles, permissions, permVer, System.currentTimeMillis());
            redisUtils.set(userPermissionKey, userPermissionCache);
        }

        // 处理 session 缓存
        AuthSessionCache authSessionCache = new AuthSessionCache(sessionId, user.getId(), accessToken, refreshToken,
                ip, userAgent, System.currentTimeMillis(), AuthCacheStatusEnum.ACTIVE.name());
        redisUtils.set(sessionKey, authSessionCache, refreshTokenTtlSeconds);
        redisUtils.sAdd(userSessionKey, sessionId);
        redisUtils.expire(userSessionKey, refreshTokenTtlSeconds);

        // 处理 token 缓存
        AccessTokenCache accessTokenCache = new AccessTokenCache(sessionId, user.getId(), accessToken, permVer, ip, userAgent,
                System.currentTimeMillis(), System.currentTimeMillis() + accessTokenTtlSeconds * 1000, AuthCacheStatusEnum.ACTIVE.name());
        redisUtils.set(accessTokenKey, accessTokenCache, accessTokenTtlSeconds);

        RefreshTokenCache refreshTokenCache = new RefreshTokenCache(sessionId, user.getId(), System.currentTimeMillis(),
                System.currentTimeMillis() + refreshTokenTtlSeconds * 1000, AuthCacheStatusEnum.ACTIVE.name());
        redisUtils.set(refreshTokenKey, refreshTokenCache, refreshTokenTtlSeconds);

        return new TokenResponse(accessToken, refreshToken, (System.currentTimeMillis() + accessTokenTtlSeconds * 1000));
    }

    public void logout(String accessToken) {
        if (StringUtils.isBlank(accessToken)) {
            return;
        }
        AccessTokenCache accessTokenCache = redisUtils.get(AuthConst.buildAccessTokenKey(accessToken), AccessTokenCache.class);
        if (accessTokenCache == null) {
            return;
        }
        AuthSessionCache authSessionCache = redisUtils.get(AuthConst.buildSessionKey(accessTokenCache.sid()), AuthSessionCache.class);
        if (authSessionCache == null) {
            return;
        }
        redisUtils.delete(AuthConst.buildSessionKey(accessTokenCache.sid()));
        redisUtils.delete(AuthConst.buildAccessTokenKey(accessTokenCache.sid()));
        redisUtils.delete(AuthConst.buildRefreshTokenKey(accessTokenCache.sid()));

    }

    /**
     * Refresh access token with one-time refresh token.
     */
    public TokenResponse refresh(String refreshToken, String ip, String userAgent) {
        if (StringUtils.isBlank(refreshToken)) {
            throw new BusinessException("refreshToken is blank");
        }

        String rtKey = AuthConst.buildRefreshTokenKey(refreshToken);
        RefreshTokenSession rtSession = redisUtils.get(rtKey, RefreshTokenSession.class);
        if (rtSession == null) {
            throw new BusinessException("refreshToken is invalid or expired");
        }

        // One-time consumption.
        redisUtils.delete(rtKey);

        String oldAccessToken = rtSession.sid();
        UserSession oldSession = redisUtils.get(AuthConst.buildSessionKey(oldAccessToken), UserSession.class);
        redisUtils.delete(AuthConst.buildSessionKey(oldAccessToken));

        UserEntity user = userMapper.selectById(rtSession.userId());
        if (user == null || Boolean.TRUE.equals(user.getDisable())) {
            throw new BusinessException("user is not available");
        }

        List<String> roles = userMapper.getAllRoleByUserId(user.getId());
        List<String> permissions = userMapper.getAllPermissionByUserId(user.getId());

        String newAccessToken = IdUtil.simpleUUID();
        String newRefreshToken = IdUtil.simpleUUID();
        long sessionTtlSeconds = securityProperties.getToken().getExpiration();
        long refreshTokenTtlSeconds = securityProperties.getToken().getRefreshExpiration();

        String deviceType = oldSession == null ? AuthConst.DEFAULT_DEVICE_TYPE : oldSession.deviceType();
        UserSession newSession = new UserSession(
                user.getId(),
                user.getUsername(),
                StringUtils.defaultIfBlank(deviceType, AuthConst.DEFAULT_DEVICE_TYPE),
                StringUtils.defaultIfBlank(ip, oldSession == null ? null : oldSession.ip()),
                StringUtils.defaultIfBlank(userAgent, oldSession == null ? null : oldSession.userAgent()),
                roles,
                permissions,
                System.currentTimeMillis()
        );
        redisUtils.set(AuthConst.buildSessionKey(newAccessToken), newSession, sessionTtlSeconds, TimeUnit.SECONDS);

        RefreshTokenSession newRt = new RefreshTokenSession(newAccessToken, user.getId(), newSession.deviceType());
        redisUtils.set(AuthConst.buildRefreshTokenKey(newRefreshToken), newRt, refreshTokenTtlSeconds, TimeUnit.SECONDS);

        return new TokenResponse(newAccessToken, newRefreshToken, sessionTtlSeconds);
    }
}
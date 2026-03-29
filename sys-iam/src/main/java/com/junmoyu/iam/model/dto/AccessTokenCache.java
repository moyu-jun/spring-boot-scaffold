package com.junmoyu.iam.model.dto;

/**
 * Redis 缓存对象
 * auth:access:{token}
 */
public record AccessTokenCache(
        String sid,
        Long userId,
        String accessToken,
        Integer permVer,
        String clientIp,
        String userAgent,
        Long issuedAt,
        Long expiresIn,
        String status) {
}

package com.junmoyu.iam.model.dto;

/**
 * Redis 缓存对象
 * auth:session:{sid}
 */
public record AuthSessionCache(
        String sid,
        Long userId,
        String accessToken,
        String refreshToken,
        String clientIp,
        String userAgent,
        Long loginAt,
        String status) {
}

package com.junmoyu.iam.model.dto;

/**
 * AccessTokenCache
 */
public record RefreshTokenCache(
        String sid,
        Long userId,
        Long issuedAt,
        Long expiresIn,
        String status) {
}

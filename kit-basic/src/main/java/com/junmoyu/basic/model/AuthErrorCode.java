package com.junmoyu.basic.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 安全认证错误码
 */
@Getter
@RequiredArgsConstructor
public enum AuthErrorCode {

    AUTH_FAILED(401, "认证失败"),
    TOKEN_EXPIRED(401, "认证过期"),
    PERMISSION_DENIED(403, "权限不足");

    private final int code;
    private final String message;
}

package com.junmoyu.basic.exception;

import com.junmoyu.basic.model.AuthErrorCode;
import lombok.Getter;
import lombok.Setter;


/**
 * 无效/非法访问，权限拒绝
 */
@Getter
@Setter
public class AuthException extends AbstractException {

    public AuthException(AuthErrorCode errorCode) {
        super(errorCode.getCode(), errorCode.getMessage());
    }
}

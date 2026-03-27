package com.junmoyu.basic.exception;

import lombok.Getter;
import lombok.Setter;

/**
 * 业务异常 - 用于前端弹窗提醒
 */
@Getter
@Setter
public class BusinessException extends AbstractException {

    public static final int BUSINESS_CODE = 1000;

    public BusinessException(String message) {
        super(BUSINESS_CODE, message);
    }

    public BusinessException(String message, Object... args) {
        super(BUSINESS_CODE, format(message, args));
    }

    public BusinessException(Integer code, String message) {
        super(code, message);
    }

    public BusinessException(Integer code, String message, Object... args) {
        this(code, format(message, args));
    }
}

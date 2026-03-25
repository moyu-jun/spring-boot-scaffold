package com.junmoyu.basic.exception;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;


/**
 * 无效/非法访问，权限拒绝
 */
@Getter
@Setter
public class AccessDeniedException extends AbstractException {

    public AccessDeniedException() {
        super(HttpStatus.UNAUTHORIZED.value(), "权限拒绝");
    }

    public AccessDeniedException(String message) {
        super(HttpStatus.UNAUTHORIZED.value(), message);
    }

    public AccessDeniedException(String message, Object... args) {
        super(HttpStatus.UNAUTHORIZED.value(), format(message, args));
    }
}

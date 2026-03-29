package com.junmoyu.basic.exception;

import com.junmoyu.basic.model.R;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Objects;

/**
 * 全局异常捕获 - 默认最低优先级
 */
@Slf4j
@Order
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 统一捕获未明确定义的异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    private R<String> handleException(Exception ex) {
        log.error("system exception: {}", ex.getMessage(), ex);
        return R.failure();
    }

    /**
     * 业务异常，预期内的异常，如参数校验问题
     */
    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(BusinessException.class)
    private R<String> handleBusinessException(BusinessException ex) {
        log.error("business exception", ex);
        return R.failure(ex.getCode(), ex.getMessage());
    }

    /**
     * 请求的方法参数无法通过验证时异常
     */
    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected R<String> handleMethodArgumentNotValidException(final MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .filter(StringUtils::isNotBlank)
                .findFirst()
                .orElse("");
        log.error("method argument not valid: {}", message, ex);
        return R.failure(BusinessException.BUSINESS_CODE, message);
    }

    /**
     * 权限拒绝异常
     */
    @ExceptionHandler(AuthException.class)
    protected ResponseEntity<R<String>> handleAuthException(final AuthException ex) {
        log.error("auth exception", ex);
        return ResponseEntity
                .status(ex.getCode())
                .body(R.failure(ex.getCode(), ex.getMessage()));
    }

    /**
     * http 请求方法不支持异常
     */
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    protected R<String> handleHttpRequestMethodNotSupportedException(final HttpRequestMethodNotSupportedException ex) {
        log.error("Method Not Allowed", ex);
        return R.failure(HttpStatus.METHOD_NOT_ALLOWED.value(), "Method Not Allowed");
    }

    /**
     * 请求的方法参数类型与实际传入的参数类型不匹配时触发异常
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected R<String> handleMethodArgumentTypeMismatchException(final MethodArgumentTypeMismatchException ex) {
        log.error("method argument type mismatch", ex);
        return R.failure(String.format("%s should be of type %s", ex.getName(), Objects.requireNonNull(ex.getRequiredType()).getName()));
    }
}

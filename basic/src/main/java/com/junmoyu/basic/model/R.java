package com.junmoyu.basic.model;

import org.springframework.http.HttpStatus;

/**
 * 统一API响应结果
 */
public record R<T>(int code, String message, T data) {

    private static final int SUCCESS = HttpStatus.OK.value();
    private static final String SUCCESS_MESSAGE = "请求成功";

    private static final int FAIL = HttpStatus.INTERNAL_SERVER_ERROR.value();
    private static final String FAIL_MESSAGE = "系统异常";

    public static <T> R<T> success() {
        return success(null);
    }

    public static <T> R<T> success(T data) {
        return new R<>(SUCCESS, SUCCESS_MESSAGE, data);
    }

    public static <T> R<T> failure() {
        return failure(FAIL, FAIL_MESSAGE);
    }

    public static <T> R<T> failure(String message) {
        return failure(FAIL, message);
    }

    public static <T> R<T> failure(int code, String message) {
        return failure(code, message, null);
    }

    public static <T> R<T> failure(String message, T data) {
        return failure(FAIL, message, data);
    }

    public static <T> R<T> failure(int code, String message, T data) {
        return new R<>(code, message, data);
    }
}

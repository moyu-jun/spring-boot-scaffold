package com.junmoyu.basic.exception;

import cn.hutool.core.util.StrUtil;
import lombok.Getter;

/**
 * 异常抽象类
 */
@Getter
public abstract class AbstractException extends RuntimeException {

    protected Integer code;
    protected String message;

    public AbstractException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    /**
     * format
     *
     * @param message My name is {}, {} years old
     * @param args    可变参数
     */
    protected static String format(String message, Object... args) {
        if (args != null && args.length > 0) {
            message = StrUtil.format(message, args);
        }
        return message;
    }
}

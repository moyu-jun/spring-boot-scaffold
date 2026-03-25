package com.junmoyu.basic.model;

import java.util.Collections;
import java.util.List;

/**
 * 分页返回结果
 */
public record PageResult<T>(Number total, List<T> list) {

    public PageResult(Number total) {
        this(total, Collections.emptyList());
    }

    public PageResult(Number total, List<T> list) {
        this.total = total;
        this.list = list;
    }
}
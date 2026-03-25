package com.junmoyu.basic.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 基础分页查询对象
 */
@Data
@NoArgsConstructor
public class PageQuery implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Integer page;

    private Integer size;

    public Integer getPage() {
        if (page == null || page <= 0) {
            page = 1;
        }
        return page;
    }

    public Integer getSize() {
        if (size == null || size <= 0) {
            size = 10;
        }
        return size;
    }
}
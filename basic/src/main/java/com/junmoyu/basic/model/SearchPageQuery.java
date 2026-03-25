package com.junmoyu.basic.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serial;

/**
 * 用于关键词搜索的分页查询对象
 */
@Data
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class SearchPageQuery extends PageQuery {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 搜索关键词
     */
    private String keywords;
}
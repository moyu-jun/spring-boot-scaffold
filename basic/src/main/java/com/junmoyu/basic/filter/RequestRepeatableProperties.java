package com.junmoyu.basic.filter;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;

/**
 * 可重复读请求配置
 */
@Data
@ConfigurationProperties(prefix = "request.repeatable")
public class RequestRepeatableProperties {

    /**
     * 是否开启可重复读过滤器
     */
    private boolean enabled = true;

    /**
     * 允许包装的最大报文阈值，默认 10MB
     */
    private DataSize maxPayloadSize = DataSize.ofMegabytes(10);
}

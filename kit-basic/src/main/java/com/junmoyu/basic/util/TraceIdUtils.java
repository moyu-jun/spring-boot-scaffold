package com.junmoyu.basic.util;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;

import java.util.UUID;

/**
 * 用于管理分布式追踪的 traceId
 */
public class TraceIdUtils {

    /**
     * 支持的请求头字段名（按优先级排序）
     */
    public static final String[] HEADER_TRACE_IDS = {"x-trace-id", "x-request-id", "trace-id", "traceId"};

    /**
     * MDC 中的 traceId 键名
     */
    public static final String TRACE_ID_KEY = "traceId";

    /**
     * 从请求中提取 traceId，如果不存在则生成一个新的
     *
     * @param request HTTP 请求
     * @return traceId
     */
    public static String getTraceIdByRequest(HttpServletRequest request) {
        for (String key : HEADER_TRACE_IDS) {
            String value = request.getHeader(key);
            if (StringUtils.isNotBlank(value)) {
                return value;
            }
        }
        return generateTraceId();
    }

    /**
     * 生成 traceId
     * 使用 UUID 去掉横线作为 traceId
     *
     * @return traceId
     */
    public static String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 获取当前线程的 traceId
     *
     * @return traceId，如果不存在则返回 null
     */
    public static String getTraceId() {
        return MDC.get(TRACE_ID_KEY);
    }

    /**
     * 设置 traceId 到 MDC
     *
     * @param traceId traceId
     */
    public static void setTraceId(String traceId) {
        if (StringUtils.isNotBlank(traceId)) {
            MDC.put(TRACE_ID_KEY, traceId);
        }
    }

    /**
     * 移除 MDC 中的 traceId
     */
    public static void removeTraceId() {
        MDC.remove(TRACE_ID_KEY);
    }
}

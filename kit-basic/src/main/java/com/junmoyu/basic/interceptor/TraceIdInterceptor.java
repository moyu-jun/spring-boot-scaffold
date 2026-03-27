package com.junmoyu.basic.interceptor;

import com.junmoyu.basic.util.TraceIdUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * TraceId 拦截器
 * 用于拦截用户请求，提取或生成 traceId，并注入到 MDC 中
 */
@Slf4j
public class TraceIdInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        // 从请求中提取 traceId, 如果提取不到会自动生成一个
        String traceId = TraceIdUtils.getTraceIdByRequest(request);
        // 将 traceId 设置到 MDC 中
        TraceIdUtils.setTraceId(traceId);
        // 将 traceId 设置到 Response HEADER 中
        response.setHeader(TraceIdUtils.HEADER_TRACE_IDS[0], traceId);
        return true;
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, Exception ex) {
        // 清理 MDC 中的 traceId，避免内存泄漏
        TraceIdUtils.removeTraceId();
    }
}

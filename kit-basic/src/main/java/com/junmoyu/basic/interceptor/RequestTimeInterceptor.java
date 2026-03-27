package com.junmoyu.basic.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.NamedThreadLocal;
import org.springframework.http.HttpMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;
import java.time.Instant;

/**
 * 请求耗时拦截器
 */
@Slf4j
public class RequestTimeInterceptor implements HandlerInterceptor {

    /**
     * 慢请求阈值（单位ms）
     */
    private static final long SLOW_REQUEST_THRESHOLD = 500;

    private final NamedThreadLocal<Instant> startTimeThreadLocal = new NamedThreadLocal<>("RequestTime");

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        if (!request.getMethod().equals(HttpMethod.OPTIONS.name())) {
            this.startTimeThreadLocal.set(Instant.now());
        }
        return true;
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, Exception ex) throws Exception {
        if (request.getMethod().equals(HttpMethod.OPTIONS.name())) {
            return;
        }

        try {
            Instant startTime = startTimeThreadLocal.get();
            if (startTime == null) {
                return;
            }

            long requestTime = Duration.between(startTime, Instant.now()).toMillis();

            String handlerName = handler instanceof HandlerMethod
                    ? ((HandlerMethod) handler).getMethod().toGenericString()
                    : handler.getClass().getName();

            // 区分慢请求日志级别，提升排查效率
            if (requestTime > SLOW_REQUEST_THRESHOLD) {
                log.warn("【慢请求】HTTP Completed - method: {}, uri: {}, status: {}, times: {}ms, handler: {}, exception: {}",
                        request.getMethod(), request.getRequestURI(), response.getStatus(), requestTime, handlerName,
                        ex != null ? ex.getClass().getSimpleName() : "none");
            } else {
                log.info("HTTP Completed - method: {}, uri: {}, status: {}, times: {}ms",
                        request.getMethod(), request.getRequestURI(), response.getStatus(), requestTime);
            }
        } finally {
            this.startTimeThreadLocal.remove();
        }
    }
}

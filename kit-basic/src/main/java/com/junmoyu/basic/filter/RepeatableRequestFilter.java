package com.junmoyu.basic.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 可重复读的 HttpServletRequest 过滤器
 */
@Slf4j
@RequiredArgsConstructor
public class RepeatableRequestFilter extends OncePerRequestFilter {

    private final RequestRepeatableProperties properties;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        if (shouldWrap(request)) {
            filterChain.doFilter(new RepeatableRequestWrapper(request), response);
        } else {
            filterChain.doFilter(request, response);
        }
    }

    private boolean shouldWrap(HttpServletRequest request) {
        // 1. 检查 Content-Type 是否为 JSON
        String contentType = request.getContentType();
        if (contentType == null || !MediaType.APPLICATION_JSON.isCompatibleWith(MediaType.parseMediaType(contentType))) {
            return false;
        }

        // 2. 检查报文大小 (优先从 Content-Length 头部判断)
        long contentLength = request.getContentLengthLong();
        long maxBytes = properties.getMaxPayloadSize().toBytes();

        if (contentLength > maxBytes) {
            log.warn("Request body size ({}) exceeds threshold ({}), skipping repeatable wrap.",
                    contentLength, properties.getMaxPayloadSize());
            return false;
        }

        // 3. 特殊情况：Content-Length 为 -1 (Chunked 传输)
        // 这种情况下由于无法预知大小，为了安全起见，企业级脚手架通常选择不包装或在读取时计数
        if (contentLength == -1) {
            log.debug("Chunked transfer encoding detected, skipping repeatable wrap for safety.");
            return false;
        }
        return true;
    }
}

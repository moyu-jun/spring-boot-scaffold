package com.junmoyu.config;

import com.junmoyu.basic.interceptor.RequestTimeInterceptor;
import com.junmoyu.basic.interceptor.TraceIdInterceptor;
import com.junmoyu.security.SecurityProperties;
import com.junmoyu.security.core.UserDetail;
import com.junmoyu.security.interceptor.AuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 配置
 */
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(SecurityProperties.class)
public class WebConfig implements WebMvcConfigurer {

    private final UserDetail userDetail;
    private final SecurityProperties securityProperties;

    /**
     * 全部接口拦截
     */
    private static final String PATH_PATTERNS = "/**";

    /**
     * web 页面类接口拦截
     */
    private static final String WEB_PATH_PATTERNS = "/swagger-ui.html,/swagger-ui/**,/v3/api-docs/**,/doc.html/**,/webjars/**,/favicon.ico";

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        // 1. 基础追踪拦截器 (TraceId)
        registry.addInterceptor(new TraceIdInterceptor())
                .addPathPatterns(PATH_PATTERNS.split(","))
                .excludePathPatterns(WEB_PATH_PATTERNS.split(","))
                .order(Ordered.HIGHEST_PRECEDENCE);

        // 2. 耗时统计拦截器
        registry.addInterceptor(new RequestTimeInterceptor())
                .addPathPatterns(PATH_PATTERNS.split(","))
                .excludePathPatterns(WEB_PATH_PATTERNS.split(","))
                .order(Ordered.HIGHEST_PRECEDENCE + 1);

        // 3. 认证授权拦截器
        if (securityProperties.isEnabled()) {
            registry.addInterceptor(new AuthInterceptor(userDetail))
                    .addPathPatterns(PATH_PATTERNS.split(","))
                    .excludePathPatterns(WEB_PATH_PATTERNS.split(","))
                    .excludePathPatterns(securityProperties.getExcludePatterns())
                    .order(Ordered.HIGHEST_PRECEDENCE + 2);
        }
    }
}

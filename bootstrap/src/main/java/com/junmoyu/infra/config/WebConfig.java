package com.junmoyu.infra.config;

import com.junmoyu.basic.interceptor.RequestTimeInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 配置
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * 全部接口拦截
     */
    private static final String PATH_PATTERNS = "/**";

    /**
     * web 页面类接口拦截
     */
    private static final String WEB_PATH_PATTERNS = "/swagger-ui/**,/v3/api-docs/**,/doc.html/**,/webjars/**,/favicon.ico";

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        // 1. 基础追踪拦截器 (TraceId)
//        registry.addInterceptor(new TraceIdInterceptor())
//                .addPathPatterns(PATH_PATTERNS.split(","))
//                .excludePathPatterns(WEB_PATH_PATTERNS.split(","))
//                .order(Ordered.HIGHEST_PRECEDENCE);

        // 2. 耗时统计拦截器
        registry.addInterceptor(requestTimeInterceptor())
                .addPathPatterns(PATH_PATTERNS.split(","))
                .excludePathPatterns(WEB_PATH_PATTERNS.split(","))
                .order(Ordered.HIGHEST_PRECEDENCE + 1);
    }

    @Bean
    public RequestTimeInterceptor requestTimeInterceptor() {
        return new RequestTimeInterceptor();
    }
}

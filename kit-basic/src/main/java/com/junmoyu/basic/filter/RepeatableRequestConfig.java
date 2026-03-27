package com.junmoyu.basic.filter;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(RequestRepeatableProperties.class)
public class RepeatableRequestConfig {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "request.repeatable", name = "enabled", havingValue = "true", matchIfMissing = true)
    public FilterRegistrationBean<RepeatableRequestFilter> repeatableRequestFilter(RequestRepeatableProperties properties) {
        FilterRegistrationBean<RepeatableRequestFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new RepeatableRequestFilter(properties));
        // Ordered.HIGHEST_PRECEDENCE 确保在最外层，以便日志过滤器、权限过滤器能读到 body
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        registration.addUrlPatterns("/*");
        return registration;
    }
}

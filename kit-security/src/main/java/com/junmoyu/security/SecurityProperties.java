package com.junmoyu.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 安全配置属性
 */
@Getter
@Setter
@ConfigurationProperties(prefix = SecurityProperties.SECURITY_PREFIX)
public class SecurityProperties {

    /**
     * 配置前缀
     */
    public static final String SECURITY_PREFIX = "security";

    /**
     * 是否启用安全模块
     */
    private boolean enabled = true;

    /**
     * 排除的路径（不进行认证拦截）
     */
    private List<String> excludePatterns = new ArrayList<>();

    /**
     * Token 配置
     */
    private TokenConfig token = new TokenConfig();

    @Getter
    @Setter
    public static class TokenConfig {
        /**
         * Token 请求头名称
         */
        private String headerName = "Authorization";

        /**
         * Token 前缀
         */
        private String prefix = "Bearer ";

        /**
         * Token 过期时间（秒），默认 7 天
         */
        private long expiration = 7 * 24 * 60 * 60;

        /**
         * Refresh Token 过期时间（秒），默认 30 天
         */
        private long refreshExpiration = 30 * 24 * 60 * 60;
    }
}

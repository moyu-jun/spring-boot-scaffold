package com.junmoyu.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 安全模块自动配置
 * <p>
 * 提供密码编码器和组件扫描配置
 */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = SecurityProperties.SECURITY_PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(SecurityProperties.class)
public class SecurityAutoConfiguration {

    /**
     * 配置密码编码器
     * <p>
     * 使用 Spring Security 的委托密码编码器，支持多种编码算法：
     * - bcrypt（默认）
     * - pbkdf2
     * - scrypt
     * - argon2
     * <p>
     * 编码格式：{algorithm}encodedPassword
     * 例如：{bcrypt}$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG
     *
     * @return 密码编码器
     */
    @Bean
    @ConditionalOnMissingBean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}

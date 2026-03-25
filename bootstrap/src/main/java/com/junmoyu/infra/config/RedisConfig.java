package com.junmoyu.infra.config;

import com.junmoyu.basic.util.RedisUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Redis 配置
 */
@Configuration
public class RedisConfig {

    @Bean
    public StringRedisTemplate redisTemplate(LettuceConnectionFactory factory) {
        return new StringRedisTemplate(factory);
    }

    @Bean
    public RedisUtils redisUtils(StringRedisTemplate redisTemplate) {
        return new RedisUtils(redisTemplate);
    }
}

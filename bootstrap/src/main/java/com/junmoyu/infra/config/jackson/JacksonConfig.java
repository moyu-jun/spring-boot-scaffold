package com.junmoyu.infra.config.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.junmoyu.basic.util.JsonUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Jackson 全局配置
 */
@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return JsonUtils.getObjectMapper();
    }
}
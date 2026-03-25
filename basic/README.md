# 脚手架 Basic 基础模块

## 可传递依赖

* commons-lang3
* commons-collections4
* jackson-databind
* jackson-datatype-jsr310
* hutool-core

## 功能列表

| 功能                | 路径                                                   | 
|-------------------|:-----------------------------------------------------|
| 基础实体类             | com.junmoyu.basic.model.*                            |
| 全局统一响应            | model.basic.com.junmoyu.bootstrap.R                            |
| 全局统一异常捕获          | exception.basic.com.junmoyu.bootstrap.GlobalExceptionHandler   |
| 通用自定义异常           | com.junmoyu.basic.exception.*                        |
| 可重复读取的 Request 配置 | filter.basic.com.junmoyu.bootstrap.RepeatableRequestConfig     |
| 请求时间拦截器           | interceptor.basic.com.junmoyu.bootstrap.RequestTimeInterceptor |
| TraceId 拦截器       | com.junmoyu.basic.interceptor.TraceIdInterceptor     |
| Json 工具类          | util.basic.com.junmoyu.bootstrap.JsonUtils                     |
| Redis 工具类         | util.basic.com.junmoyu.bootstrap.RedisUtils                    |
| TraceId 工具类       | com.junmoyu.basic.util.TraceIdUtils                  |
| 脱敏工具              | com.junmoyu.basic.util.sensitive.*                   |

## 引入依赖

```xml

<dependency>
    <groupId>com.junmoyu</groupId>
    <artifactId>basic</artifactId>
    <version>latest.version</version>
</dependency>
```

## 自动化配置

以下功能已自动注入 Spring 容器，不需要额外配置。

| 功能                | 路径                                                 | 
|-------------------|:---------------------------------------------------|
| 全局统一异常捕获          | exception.basic.com.junmoyu.bootstrap.GlobalExceptionHandler |
| 可重复读取的 Request 配置 | filter.basic.com.junmoyu.bootstrap.RepeatableRequestConfig   |

如果需要对其他的异常进行捕获，可以自己再创建一个异常捕获类，两个配置可以同时使用。

## 手动配置项目

引入依赖后需要手动添加启用的配置项。

### 可重复读请求（Repeatable Request）

默认针对 application/json 请求开启。可在配置文件中微调：

```YAML
request:
    repeatable:
        enabled: true          # 开关，默认开启，可手动关闭
        max-payload-size: 10MB # 超过该阈值将不进行内存包装，防止 OOM，默认 10MB
```

### 拦截器配置

```java
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
        registry.addInterceptor(traceIdInterceptor())
                .addPathPatterns(PATH_PATTERNS.split(","))
                .excludePathPatterns(WEB_PATH_PATTERNS.split(","))
                .order(Ordered.HIGHEST_PRECEDENCE);

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


    @Bean
    public TraceIdInterceptor traceIdInterceptor() {
        return new TraceIdInterceptor();
    }
}
```

### Redis 配置

```java
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
```

### 全局 Json 配置

`JsonUtils` 内的方法为静态方法可直接使用，无需依赖注入。但需要设置全局的 Jackson 配置。

```java
@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return JsonUtils.getObjectMapper();
    }
}
```
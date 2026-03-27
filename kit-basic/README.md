# kit-basic 基础模块

## 可传递依赖

* commons-lang3
* commons-collections4
* jackson-databind
* jackson-datatype-jsr310
* hutool-core

---

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

---

## 引入依赖

```xml

<dependency>
    <groupId>${project.parent.groupId}</groupId>
    <artifactId>basic</artifactId>
    <version>latest.version</version>
</dependency>
```
---

## 自动化配置

以下功能已自动注入 Spring 容器，不需要额外配置。

| 功能                | 路径                                                 | 
|-------------------|:---------------------------------------------------|
| 全局统一异常捕获          | exception.basic.com.junmoyu.bootstrap.GlobalExceptionHandler |
| 可重复读取的 Request 配置 | filter.basic.com.junmoyu.bootstrap.RepeatableRequestConfig   |

如果需要对其他的异常进行捕获，可以自己再创建一个异常捕获类，两个配置可以同时使用。

---

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

---

## TraceId 追踪

### 功能说明

TraceId 是一个用于追踪请求链路的唯一标识符，贯穿整个请求生命周期，包括同步和异步场景。通过 TraceId 可以：

- 快速定位问题：在分布式系统中追踪请求的完整调用链路
- 日志关联：将同一请求的所有日志通过 TraceId 关联起来
- 性能分析：分析请求在各个环节的耗时
- 问题排查：快速定位异常发生的位置和上下文

### 工作原理

1. **TraceId 生成与传递**
   - `TraceIdInterceptor` 拦截所有 HTTP 请求
   - 优先从请求头 `x-trace-id` 中提取 TraceId
   - 如果请求头中没有，则自动生成新的 TraceId（UUID 格式）
   - 将 TraceId 存入 MDC（Mapped Diagnostic Context）
   - 请求结束后自动清理 MDC

2. **日志输出**
   - Logback 配置中通过 `%X{traceId}` 占位符输出 TraceId
   - 所有日志自动包含 TraceId，无需手动传递

3. **工具类支持**
   - `TraceIdUtils.getTraceId()`：获取当前线程的 TraceId
   - `TraceIdUtils.setTraceId(String)`：设置当前线程的 TraceId
   - `TraceIdUtils.removeTraceId()`：清除当前线程的 TraceId

### 异步场景支持

在异步场景中，由于线程切换导致 MDC 无法自动传递，需要特殊处理。本模块提供了完整的异步场景解决方案，支持自动选择虚拟线程或传统线程池。

#### 虚拟线程配置（推荐）

**Java 21+ 支持虚拟线程**，通过配置参数自动选择执行器类型：

```yaml
spring:
  threads:
    virtual:
      enabled: true  # 启用虚拟线程（推荐）
```

- `enabled: true`：使用虚拟线程执行器（轻量级，适合 I/O 密集型任务）
- `enabled: false` 或不配置：使用传统线程池执行器

**两种模式都自动支持 traceId 传递，无需修改业务代码。**

#### 1. @Async 注解方式

配置会根据 `spring.threads.virtual.enabled` 自动选择执行器：

```java
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    @Value("${spring.threads.virtual.enabled:false}")
    private boolean virtualThreadsEnabled;

    @Override
    public Executor getAsyncExecutor() {
        if (virtualThreadsEnabled) {
            // 使用虚拟线程执行器
            return new MdcVirtualThreadExecutor();
        } else {
            // 使用传统线程池执行器
            ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
            executor.setCorePoolSize(8);
            executor.setMaxPoolSize(16);
            executor.setQueueCapacity(200);
            executor.setThreadNamePrefix("async-");
            executor.setTaskDecorator(new MdcTaskDecorator());
            executor.initialize();
            return executor;
        }
    }
}
```

使用示例：

```java
@Service
public class AsyncTaskExample {

    @Async
    public void asyncTask(String input) {
        String traceId = TraceIdUtils.getTraceId();
        log.info("[Async] traceId: {}, input: {}", traceId, input);
    }
}
```

#### 2. CompletableFuture 方式

配置会根据 `spring.threads.virtual.enabled` 自动选择执行器：

```java
@Configuration
@EnableAsync
public class AsyncConfig {

    @Value("${spring.threads.virtual.enabled:false}")
    private boolean virtualThreadsEnabled;

    @Bean
    @Primary
    public Executor getCompletableFutureExecutor() {
        if (virtualThreadsEnabled) {
            // 使用虚拟线程执行器
            return new MdcVirtualThreadExecutor();
        } else {
            // 使用传统线程池执行器
            ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
            executor.setCorePoolSize(8);
            executor.setMaxPoolSize(16);
            executor.setQueueCapacity(200);
            executor.setThreadNamePrefix("cf-");
            executor.setTaskDecorator(new MdcTaskDecorator());
            executor.initialize();
            return executor;
        }
    }
}
```

使用示例（无需手动处理 MDC，自动继承 TraceId）：

```java
@Service
@RequiredArgsConstructor
public class AsyncTaskExample {

    private final Executor completableFutureExecutor;

    public CompletableFuture<String> asyncWithCompletableFuture(String input) {
        return CompletableFuture.supplyAsync(() -> {
            String traceId = TraceIdUtils.getTraceId();
            log.info("[CompletableFuture] traceId: {}, input: {}", traceId, input);
            return "Processed: " + input;
        }, completableFutureExecutor);
    }
}
```

**关键点**：
- 使用 `@Primary` 注解标记执行器，使其成为默认的 Executor
- 根据配置自动选择虚拟线程或传统线程池
- 保持原有的 `CompletableFuture` 调用方式，无需修改业务代码
- TraceId 自动传递，无需手动处理 MDC
- 虚拟线程模式下可创建大量并发任务，适合 I/O 密集型场景

#### 3. Spring 事件（@EventListener）

事件监听器在异步模式下需要手动传递 TraceId：

```java
@EventListener
@Async("asyncExecutor")
public void handleEvent(CustomEvent event) {
    // 从事件对象中获取 TraceId
    TraceIdUtils.setTraceId(event.getTraceId());
    try {
        log.info("[EventListener] traceId: {}, handling event", event.getTraceId());
        // 业务逻辑
    } finally {
        TraceIdUtils.removeTraceId();
    }
}
```

#### 4. 微服务调用（Feign/RestTemplate）

在微服务调用时，通过请求头传递 TraceId：

```java
// Feign 拦截器
@Component
public class FeignTraceIdInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate template) {
        String traceId = TraceIdUtils.getTraceId();
        if (traceId != null) {
            template.header("x-trace-id", traceId);
        }
    }
}

// RestTemplate 拦截器
@Component
public class RestTemplateTraceIdInterceptor implements ClientHttpRequestInterceptor {
    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                       ClientHttpRequestExecution execution) throws IOException {
        String traceId = TraceIdUtils.getTraceId();
        if (traceId != null) {
            request.getHeaders().add("x-trace-id", traceId);
        }
        return execution.execute(request, body);
    }
}
```

### 最佳实践

1. **统一配置**：在 `AsyncConfig` 中统一配置所有异步执行器
2. **自动传递优先**：优先使用 `MdcTaskDecorator` 或 `MdcVirtualThreadExecutor` 实现自动传递
3. **及时清理**：手动设置 TraceId 后，务必在 finally 块中清理
4. **跨服务传递**：微服务调用时通过 HTTP 请求头传递 TraceId
5. **日志规范**：所有日志输出都应包含 TraceId，便于问题追踪
6. **虚拟线程优先**：Java 21+ 项目推荐启用虚拟线程，提升并发性能

### 虚拟线程配置说明

#### 启用虚拟线程（推荐 Java 21+）

在 `application.yml` 中配置：

```yaml
spring:
  threads:
    virtual:
      enabled: true  # 启用虚拟线程
```

#### 虚拟线程 vs 传统线程池

| 特性 | 虚拟线程 | 传统线程池 |
|------|---------|-----------|
| 线程数量 | 可创建数百万个 | 受限于系统资源 |
| 内存占用 | 极小（KB 级别） | 较大（MB 级别） |
| 适用场景 | I/O 密集型任务 | CPU 密集型任务 |
| 创建成本 | 极低 | 较高 |
| 阻塞影响 | 不影响其他任务 | 可能阻塞线程池 |
| Java 版本 | 需要 Java 21+ | 所有版本 |

#### 何时使用虚拟线程

**推荐使用虚拟线程的场景**：
- 网络请求（HTTP 调用、RPC 调用）
- 数据库查询（JDBC、MyBatis）
- 文件 I/O 操作
- 消息队列处理
- 大量并发任务

**不推荐使用虚拟线程的场景**：
- CPU 密集型计算
- 需要精确控制线程数量
- 使用了 ThreadLocal 且需要线程复用

### 完整配置示例

参考项目中的以下文件：
- `AsyncConfig.java`：异步执行器配置，支持虚拟线程自动切换
- `AsyncTaskExample.java`：各种异步场景的使用示例
- `AsyncTestController.java`：测试接口
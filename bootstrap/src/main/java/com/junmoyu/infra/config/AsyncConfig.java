package com.junmoyu.infra.config;

import com.junmoyu.basic.executor.MdcTaskDecorator;
import com.junmoyu.basic.executor.MdcVirtualThreadExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步配置
 * 根据 spring.threads.virtual.enabled 参数自动选择虚拟线程或传统线程池
 * 两种模式都支持 MDC（traceId）自动传递
 */
@Slf4j
@EnableAsync
@Configuration
public class AsyncConfig implements AsyncConfigurer {

    /**
     * 是否启用虚拟线程
     * 通过配置 spring.threads.virtual.enabled=true 启用
     */
    @Value("${spring.threads.virtual.enabled:false}")
    private boolean virtualThreadsEnabled;

    /**
     * 核心线程数（传统线程池）
     */
    private static final int CORE_POOL_SIZE = 8;

    /**
     * 最大线程数（传统线程池）
     */
    private static final int MAX_POOL_SIZE = 16;

    /**
     * 队列容量（传统线程池）
     */
    private static final int QUEUE_CAPACITY = 200;

    /**
     * {@code @Async} 注解使用的线程池
     * 根据 spring.threads.virtual.enabled 自动选择虚拟线程或传统线程池
     */
    @Override
    public Executor getAsyncExecutor() {
        if (virtualThreadsEnabled) {
            return new MdcVirtualThreadExecutor();
        } else {
            return createThreadPoolTaskExecutor("async-pool-");
        }
    }

    /**
     * CompletableFuture 专用线程池
     * 根据 spring.threads.virtual.enabled 自动选择虚拟线程或传统线程池
     * 配置了 @Primary 注解，作为默认的 Executor
     */
    @Bean
    @Primary
    public Executor getCompletableFutureExecutor() {
        if (virtualThreadsEnabled) {
            return new MdcVirtualThreadExecutor();
        } else {
            return createThreadPoolTaskExecutor("cf-pool-");
        }
    }

    /**
     * 创建传统线程池执行器
     * 配置了 MdcTaskDecorator，自动传递 MDC 上下文
     *
     * @param threadNamePrefix 线程名称前缀
     * @return 线程池执行器
     */
    private ThreadPoolTaskExecutor createThreadPoolTaskExecutor(String threadNamePrefix) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(CORE_POOL_SIZE);
        executor.setMaxPoolSize(MAX_POOL_SIZE);
        executor.setQueueCapacity(QUEUE_CAPACITY);
        executor.setThreadNamePrefix(threadNamePrefix);

        // 设置拒绝策略：由调用线程处理
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // 关键：设置 TaskDecorator，用于传递 MDC 上下文
        executor.setTaskDecorator(new MdcTaskDecorator());

        // 等待所有任务完成后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);

        executor.initialize();
        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) -> log.error("async execute error, method: {}, param: {}", method.getName(), Arrays.toString(params), ex);
    }
}

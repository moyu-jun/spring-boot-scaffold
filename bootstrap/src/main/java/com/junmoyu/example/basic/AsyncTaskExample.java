package com.junmoyu.example.basic;

import com.junmoyu.basic.util.TraceIdUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * 异步任务示例
 * 展示如何在各种异步场景中传递 traceId
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncTaskExample {

    private final Executor completableFutureExecutor;

    /**
     * 使用 @Async 注解的异步方法
     * 配置了 MdcTaskDecorator 后，traceId 会自动传递
     */
    @Async
    public void asyncMethodWithAutoTrace(String message) {
        String traceId = TraceIdUtils.getTraceId();
        log.info("[@Async] traceId: {}, message: {}", traceId, message);
    }

    /**
     * 使用 CompletableFuture + completableFutureExecutor
     * 配置了 MdcTaskDecorator 后，traceId 会自动传递
     */
    public CompletableFuture<String> asyncWithCompletableFuture(String input) {
        return CompletableFuture.supplyAsync(() -> {
            // traceId 自动传递，无需手动处理
            String traceId = TraceIdUtils.getTraceId();
            log.info("[CompletableFuture] traceId: {}, input: {}", traceId, input);
            return "Processed: " + input;
        }, completableFutureExecutor);
    }

    /**
     * CompletableFuture 链式调用
     * 使用 completableFutureExecutor 确保每个步骤都传递 traceId
     */
    public CompletableFuture<String> chainedAsync(String input) {
        return CompletableFuture.supplyAsync(() -> {
            String traceId = TraceIdUtils.getTraceId();
            log.info("[Chain-Step1] traceId: {}, input: {}", traceId, input);
            return "Step1-" + input;
        }, completableFutureExecutor).thenApplyAsync(result -> {
            String traceId = TraceIdUtils.getTraceId();
            log.info("[Chain-Step2] traceId: {}, result: {}", traceId, result);
            return "Step2-" + result;
        }, completableFutureExecutor).thenApplyAsync(result -> {
            String traceId = TraceIdUtils.getTraceId();
            log.info("[Chain-Step3] traceId: {}, result: {}", traceId, result);
            return "Step3-" + result;
        }, completableFutureExecutor);
    }

    /**
     * 并行执行多个任务
     * 所有任务都会自动传递 traceId
     */
    public CompletableFuture<String> parallelAsync(String input) {
        CompletableFuture<String> task1 = CompletableFuture.supplyAsync(() -> {
            String traceId = TraceIdUtils.getTraceId();
            log.info("[Parallel-Task1] traceId: {}, input: {}", traceId, input);
            return "Task1-" + input;
        }, completableFutureExecutor);

        CompletableFuture<String> task2 = CompletableFuture.supplyAsync(() -> {
            String traceId = TraceIdUtils.getTraceId();
            log.info("[Parallel-Task2] traceId: {}, input: {}", traceId, input);
            return "Task2-" + input;
        }, completableFutureExecutor);

        CompletableFuture<String> task3 = CompletableFuture.supplyAsync(() -> {
            String traceId = TraceIdUtils.getTraceId();
            log.info("[Parallel-Task3] traceId: {}, input: {}", traceId, input);
            return "Task3-" + input;
        }, completableFutureExecutor);

        return CompletableFuture.allOf(task1, task2, task3)
                .thenApply(v -> {
                    String traceId = TraceIdUtils.getTraceId();
                    log.info("[Parallel-Combine] traceId: {}", traceId);
                    return String.join(", ", task1.join(), task2.join(), task3.join());
                });
    }

    /**
     * CompletableFuture 异常处理
     * traceId 在异常处理中也会自动传递
     */
    public CompletableFuture<String> asyncWithExceptionHandling(String input) {
        return CompletableFuture.supplyAsync(() -> {
            String traceId = TraceIdUtils.getTraceId();
            log.info("[Exception-Try] traceId: {}, input: {}", traceId, input);
            if ("error".equals(input)) {
                throw new RuntimeException("Simulated error");
            }
            return "Success: " + input;
        }, completableFutureExecutor).exceptionally(ex -> {
            String traceId = TraceIdUtils.getTraceId();
            log.error("[Exception-Catch] traceId: {}, error: {}", traceId, ex.getMessage());
            return "Error handled: " + ex.getMessage();
        });
    }

    /**
     * 批量并发任务
     * 根据配置自动使用虚拟线程或传统线程池
     * 展示执行器处理大量并发任务的能力
     */
    public void batchConcurrentTasks(int taskCount) {
        String traceId = TraceIdUtils.getTraceId();
        log.info("[Batch-Tasks] Starting {} tasks with traceId: {}", taskCount, traceId);

        for (int i = 0; i < taskCount; i++) {
            final int taskId = i;
            completableFutureExecutor.execute(() -> {
                String currentTraceId = TraceIdUtils.getTraceId();
                log.info("[Batch-Task-{}] traceId: {}, executing", taskId, currentTraceId);
            });
        }
    }
}

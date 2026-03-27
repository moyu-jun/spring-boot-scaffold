package com.junmoyu.example.basic;

import com.junmoyu.basic.model.R;
import com.junmoyu.basic.util.TraceIdUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 异步任务测试 Controller
 * 用于验证各种异步场景下的 traceId 传递
 */
@Slf4j
@RestController
@RequestMapping("/api/async-test")
public class AsyncTestController {

    private final AsyncTaskExample asyncTaskExample;

    public AsyncTestController(AsyncTaskExample asyncTaskExample) {
        this.asyncTaskExample = asyncTaskExample;
    }

    /**
     * 测试 @Async 注解方式
     * 访问：GET /api/async-test/async-method?message=test
     */
    @GetMapping("/async-method")
    public R<Map<String, String>> testAsyncMethod(@RequestParam(defaultValue = "Hello") String message) {
        String mainThreadTraceId = TraceIdUtils.getTraceId();
        log.info("Main thread, traceId: {}, message: {}", mainThreadTraceId, message);

        asyncTaskExample.asyncMethodWithAutoTrace(message);

        Map<String, String> result = new HashMap<>();
        result.put("mainThreadTraceId", mainThreadTraceId);
        result.put("message", "异步任务已提交，请查看控制台日志验证 traceId 是否一致");
        result.put("tip", "异步线程的 traceId 应该与主线程一致");

        return R.success(result);
    }

    /**
     * 测试 CompletableFuture 方式
     * 访问：GET /api/async-test/completable-future?input=test
     */
    @GetMapping("/completable-future")
    public R<Map<String, String>> testCompletableFuture(@RequestParam(defaultValue = "Hello") String input) throws Exception {
        String mainThreadTraceId = TraceIdUtils.getTraceId();
        log.info("Main thread, traceId: {}, input: {}", mainThreadTraceId, input);

        CompletableFuture<String> future = asyncTaskExample.asyncWithCompletableFuture(input);
        String result = future.get();

        Map<String, String> response = new HashMap<>();
        response.put("mainThreadTraceId", mainThreadTraceId);
        response.put("result", result);
        response.put("message", "请查看控制台日志验证 traceId 是否一致");

        return R.success(response);
    }

    /**
     * 测试链式异步调用
     * 访问：GET /api/async-test/chained?input=test
     */
    @GetMapping("/chained")
    public R<Map<String, String>> testChained(@RequestParam(defaultValue = "Hello") String input) throws Exception {
        String mainThreadTraceId = TraceIdUtils.getTraceId();
        log.info("Main thread, traceId: {}, input: {}", mainThreadTraceId, input);

        CompletableFuture<String> future = asyncTaskExample.chainedAsync(input);
        String result = future.get();

        Map<String, String> response = new HashMap<>();
        response.put("mainThreadTraceId", mainThreadTraceId);
        response.put("result", result);
        response.put("message", "请查看控制台日志，Step 1/2/3 的 traceId 应该与主线程一致");

        return R.success(response);
    }

    /**
     * 测试并行任务
     * 访问：GET /api/async-test/parallel?input=test
     */
    @GetMapping("/parallel")
    public R<Map<String, String>> testParallel(@RequestParam(defaultValue = "Hello") String input) throws Exception {
        String mainThreadTraceId = TraceIdUtils.getTraceId();
        log.info("Main thread, traceId: {}, input: {}", mainThreadTraceId, input);

        CompletableFuture<String> future = asyncTaskExample.parallelAsync(input);
        String result = future.get();

        Map<String, String> response = new HashMap<>();
        response.put("mainThreadTraceId", mainThreadTraceId);
        response.put("result", result);
        response.put("message", "请查看控制台日志，所有并行任务的 traceId 应该与主线程一致");

        return R.success(response);
    }

    /**
     * 测试异常处理
     * 访问：GET /api/async-test/exception?input=error
     */
    @GetMapping("/exception")
    public R<Map<String, String>> testException(@RequestParam(defaultValue = "success") String input) throws Exception {
        String mainThreadTraceId = TraceIdUtils.getTraceId();
        log.info("Main thread, traceId: {}, input: {}", mainThreadTraceId, input);

        CompletableFuture<String> future = asyncTaskExample.asyncWithExceptionHandling(input);
        String result = future.get();

        Map<String, String> response = new HashMap<>();
        response.put("mainThreadTraceId", mainThreadTraceId);
        response.put("result", result);
        response.put("message", "请查看控制台日志验证异常处理中的 traceId 传递");

        return R.success(response);
    }

    /**
     * 测试所有场景
     * 访问：GET /api/async-test/all
     */
    @GetMapping("/all")
    public R<Map<String, String>> testAll() {
        String mainThreadTraceId = TraceIdUtils.getTraceId();
        log.info("========== 开始测试所有异步场景，主线程 traceId: {} ==========", mainThreadTraceId);

        log.info("【场景 1】测试 @Async 注解方式");
        asyncTaskExample.asyncMethodWithAutoTrace("Async Test");

        log.info("【场景 2】测试 CompletableFuture 方式");
        asyncTaskExample.asyncWithCompletableFuture("CompletableFuture Test");

        log.info("【场景 3】测试链式异步调用");
        asyncTaskExample.chainedAsync("Chained Test");

        log.info("【场景 4】测试并行任务");
        asyncTaskExample.parallelAsync("Parallel Test");

        log.info("【场景 5】测试异常处理");
        asyncTaskExample.asyncWithExceptionHandling("Exception Test");

        Map<String, String> result = new HashMap<>();
        result.put("mainThreadTraceId", mainThreadTraceId);
        result.put("message", "所有异步任务已提交，请查看控制台日志验证 traceId 传递情况");
        result.put("tip", "所有异步线程的 traceId 都应该与主线程一致: " + mainThreadTraceId);

        return R.success(result);
    }

    /**
     * 获取当前请求的 traceId
     * 访问：GET /api/async-test/trace-id
     */
    @GetMapping("/trace-id")
    public R<Map<String, String>> getTraceId() {
        String traceId = TraceIdUtils.getTraceId();
        log.info("Current traceId: {}", traceId);

        Map<String, String> result = new HashMap<>();
        result.put("traceId", traceId);
        result.put("message", "当前请求的 traceId");

        return R.success(result);
    }
}

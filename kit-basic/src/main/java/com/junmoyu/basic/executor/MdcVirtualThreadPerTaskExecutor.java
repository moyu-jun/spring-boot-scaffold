package com.junmoyu.basic.executor;

import org.slf4j.MDC;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 基于 ExecutorService 的虚拟线程执行器，支持 MDC 上下文传递
 * 使用 Java 21 的 newVirtualThreadPerTaskExecutor
 */
public class MdcVirtualThreadPerTaskExecutor {

    private final ExecutorService executorService;

    public MdcVirtualThreadPerTaskExecutor() {
        this.executorService = Executors.newVirtualThreadPerTaskExecutor();
    }

    /**
     * 提交任务到虚拟线程执行器，自动传递 MDC 上下文
     */
    public void execute(Runnable task) {
        Map<String, String> contextMap = MDC.getCopyOfContextMap();
        executorService.submit(() -> {
            try {
                if (contextMap != null) {
                    MDC.setContextMap(contextMap);
                }
                task.run();
            } finally {
                MDC.clear();
            }
        });
    }

    /**
     * 关闭执行器
     */
    public void shutdown() {
        executorService.shutdown();
    }
}

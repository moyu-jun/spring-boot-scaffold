package com.junmoyu.basic.executor;

import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;
import org.springframework.lang.NonNull;

import java.util.Map;

/**
 * MDC 任务装饰器
 * 用于在异步任务中传递 MDC 上下文（包括 traceId）
 */
public class MdcTaskDecorator implements TaskDecorator {

    @Override
    @NonNull
    public Runnable decorate(@NonNull Runnable runnable) {
        // 获取父线程的 MDC 上下文
        Map<String, String> contextMap = MDC.getCopyOfContextMap();

        return () -> {
            try {
                // 在子线程中设置 MDC 上下文
                if (contextMap != null) {
                    MDC.setContextMap(contextMap);
                }
                // 执行实际任务
                runnable.run();
            } finally {
                // 清理 MDC，避免线程池复用时的上下文污染
                MDC.clear();
            }
        };
    }
}

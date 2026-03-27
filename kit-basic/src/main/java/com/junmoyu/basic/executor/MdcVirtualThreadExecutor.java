package com.junmoyu.basic.executor;

import lombok.NonNull;import org.slf4j.MDC;

import java.util.Map;
import java.util.concurrent.Executor;

/**
 * 支持 MDC 上下文传递的虚拟线程执行器
 * 适用于 Java 21+ 的虚拟线程场景
 */
public class MdcVirtualThreadExecutor implements Executor {

    @Override
    public void execute(@NonNull Runnable command) {
        // 捕获当前线程的 MDC 上下文
        Map<String, String> contextMap = MDC.getCopyOfContextMap();

        // 在虚拟线程中执行任务
        Thread.startVirtualThread(() -> {
            try {
                // 设置 MDC 上下文
                if (contextMap != null) {
                    MDC.setContextMap(contextMap);
                }
                command.run();
            } finally {
                // 清理 MDC 上下文
                MDC.clear();
            }
        });
    }
}

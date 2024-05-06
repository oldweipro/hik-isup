package com.oldwei.hikisup.sdk.SdkService.StreamService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FfmpegThreadPool {
    private static final int THREAD_POOL_SIZE = 5; // 线程池大小
    private static ExecutorService executor;

    // 初始化线程池
    static {
        executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    }

    // 提交任务给线程池
    public static void execute(Runnable task) {
        executor.execute(task);
    }

    // 关闭线程池
    public static void shutdown() {
        executor.shutdown();
    }
}


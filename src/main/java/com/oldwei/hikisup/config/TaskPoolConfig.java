package com.oldwei.hikisup.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池参数配置，多个线程池实现线程池隔离，@Async注解，默认使用系统自定义线程池，可在项目中设置多个线程池，在异步调用的时候，指明需要调用的线程池名称，比如：@Async("taskName")
 */
@Slf4j
@EnableAsync
@Configuration
public class TaskPoolConfig {
    /**
     * 自定义线程池
     */
    @Bean("taskExecutor")
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 核心线程数：始终保持的线程数
        executor.setCorePoolSize(10);

        // 最大线程数：线程池最多能创建的线程数
        executor.setMaxPoolSize(20);

        // 队列容量：当核心线程都忙时，任务会放入队列等待
        executor.setQueueCapacity(200);

        // 线程空闲时间：超过核心线程数的线程在空闲多久后被回收
        executor.setKeepAliveSeconds(60);

        // 线程名前缀：便于调试和监控
        executor.setThreadNamePrefix("async-task-");

        // 拒绝策略：队列满时如何处理新任务
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // 等待所有任务完成后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);

        // 初始化线程池
        executor.initialize();
        return executor;
    }
}

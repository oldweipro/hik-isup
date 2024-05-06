package com.oldwei.hikisup.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 线程池参数配置，多个线程池实现线程池隔离，@Async注解，默认使用系统自定义线程池，可在项目中设置多个线程池，在异步调用的时候，指明需要调用的线程池名称，比如：@Async("taskName")
 */
@EnableAsync
@Configuration
public class TaskPoolConfig {
    /**
     * 自定义线程池
     */
    @Bean("taskExecutor")
    public Executor taskExecutor() {
        // 返回可用处理器的Java虚拟机的数量 12
        int i = Runtime.getRuntime().availableProcessors();
        System.out.println("系统最大线程数  ： " + i);
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 核心线程池大小
        executor.setCorePoolSize(16);
        // 最大线程数
        executor.setMaxPoolSize(20);
        // 配置队列容量，默认值为Integer.MAX_VALUE
        executor.setQueueCapacity(99999);
        // 活跃时间
        executor.setKeepAliveSeconds(60);
        // 线程名字前缀
        executor.setThreadNamePrefix("asyncServiceExecutor -");
        // 设置此执行程序应该在关闭时阻止的最大秒数，以便在容器的其余部分继续关闭之前等待剩余的任务完成他们的执行
        executor.setAwaitTerminationSeconds(60);
        // 等待所有的任务结束后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        return executor;
    }
}

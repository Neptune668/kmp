package com.atguigu.kmp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 手动任务执行器配置。
 */
@Configuration
public class KnowledgeQuestionClusterCheckConfig
{
    /**
     * 手动检测任务单线程有界执行器。
     *
     * @return 检测任务执行器
     */
    @Bean("questionClusterCheckExecutor")
    public ThreadPoolTaskExecutor questionClusterCheckExecutor()
    {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 一个线程异步跑,防止重复建立问题簇,计数错乱
        executor.setCorePoolSize(1); // 常驻 1 个线程
        executor.setMaxPoolSize(1); // 最多 1 个线程
        executor.setQueueCapacity(100); // 等待队列 100
        executor.setThreadNamePrefix("question-cluster-check-"); // 线程名前
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy()); // 拒绝策略 AbortPolicy：线程池满拒绝执行
        executor.initialize();
        return executor;
    }
}
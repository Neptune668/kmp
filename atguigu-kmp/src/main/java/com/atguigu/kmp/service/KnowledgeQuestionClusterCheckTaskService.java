package com.atguigu.kmp.service;

import com.atguigu.kmp.entity.KnowledgeQuestionClusterCheckTask;
import com.baomidou.mybatisplus.spring.service.IService;

import java.time.LocalDateTime;

/**
 * 问题簇检测批次任务服务。
 */
public interface KnowledgeQuestionClusterCheckTaskService extends IService<KnowledgeQuestionClusterCheckTask> {

    /** 创建截止当前时刻的待检测问题任务并异步提交。 */
    Long createManualTask(Long userId);

    /** 创建 Quartz 上一自然小时任务，不提交异步队列。 */
    Long createHourlyTask(LocalDateTime triggerTime);

    /** 在当前线程串行执行指定待执行任务。 */
    void executeTask(Long taskId);

}

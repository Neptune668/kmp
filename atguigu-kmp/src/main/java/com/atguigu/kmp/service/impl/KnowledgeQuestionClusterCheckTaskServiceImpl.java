package com.atguigu.kmp.service.impl;


import com.atguigu.kmp.constant.KnowledgeQuestionClusterCheckConstants;
import com.atguigu.kmp.entity.KnowledgeQueryLog;
import com.atguigu.kmp.entity.KnowledgeQuestionClusterCheckTask;
import com.atguigu.kmp.mapper.KnowledgeQuestionClusterCheckTaskMapper;
import com.atguigu.kmp.service.KnowledgeQueryLogService;
import com.atguigu.kmp.service.KnowledgeQuestionClusterCheckTaskService;
import com.atguigu.kmp.service.KnowledgeQuestionClusterMatchService;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.ruoyi.common.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 问题簇检测批次创建、串行执行和摘要统计实现。
 */
@Slf4j
@Service
public class KnowledgeQuestionClusterCheckTaskServiceImpl
        extends ServiceImpl<KnowledgeQuestionClusterCheckTaskMapper, KnowledgeQuestionClusterCheckTask>
        implements KnowledgeQuestionClusterCheckTaskService
{
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Shanghai");
    //
    private static final long SYSTEM_OPERATOR_USER_ID = 1L;

    @Autowired
    private KnowledgeQueryLogService queryLogService;

    @Autowired
    private KnowledgeQuestionClusterMatchService matchService;

    @Autowired
    @Qualifier("questionClusterCheckExecutor")
    private ThreadPoolTaskExecutor taskExecutor;



    /** {@inheritDoc} */
    @Override
    public Long createManualTask(Long userId)
    {
        // BUSINESS_ZONE: 避免 Docker 容器 / 海外云服务器 / 某些 CI 环境中时区错误
        LocalDateTime triggerTime = LocalDateTime.now(BUSINESS_ZONE);
        // 当前小时开始时间: 例如 2026-10-20T11:00
        LocalDateTime currentHourStart = triggerTime.truncatedTo(ChronoUnit.HOURS);
        Long taskId = this.saveTask(
                KnowledgeQuestionClusterCheckConstants.TRIGGER_MANUAL, //手动任务
                currentHourStart.minusHours(1),  // 窗口起点：上一个整点
                triggerTime, // 窗口终点：当前时刻
                userId
        );
        this.submitTask(taskId, userId);
        return taskId;
    }

    /** 保存计数均为零的批次摘要。 */
    private Long saveTask(int triggerType, LocalDateTime rangeStart, LocalDateTime rangeEnd, Long userId)
    {
        KnowledgeQuestionClusterCheckTask task = new KnowledgeQuestionClusterCheckTask();
        task.setTriggerType(triggerType); // 触发类型
        task.setRangeStartTime(rangeStart);
        task.setRangeEndTime(rangeEnd);
        task.setTaskStatus(KnowledgeQuestionClusterCheckConstants.TASK_PENDING); // 待执行
        task.setTotalCount(0);
        task.setSuccessCount(0);
        task.setFailedCount(0);
        task.setSkippedCount(0);
        task.setCreatedClusterCount(0);
        task.setMatchedClusterCount(0);
        task.setCreateUserId(userId);
        task.setCreateTime(LocalDateTime.now(BUSINESS_ZONE));
        task.setIsDeleted(0);
        save(task);
        return task.getId();
    }

    /** {@inheritDoc} */
    @Override
    public Long createHourlyTask(LocalDateTime triggerTime)
    {
        LocalDateTime end = triggerTime.atZone(BUSINESS_ZONE).withMinute(0)
                .withSecond(0).withNano(0).toLocalDateTime();
        return this.saveTask(KnowledgeQuestionClusterCheckConstants.TRIGGER_AUTO,
                end.minusHours(1), end, null);
    }

    /** {@inheritDoc} */
    @Override
    public void executeTask(Long taskId)
    {
//        KnowledgeQuestionClusterCheckTask task = getById(taskId);
//        // 如果任务不存在或者任务状态不是待执行，则返回
//        if (task == null || !Integer.valueOf(KnowledgeQuestionClusterCheckConstants.TASK_PENDING).equals(task.getTaskStatus()))
//        {
//            // 日志信息
//            log.warn("[问题簇检测][任务忽略] taskId={}，任务不存在或者任务状态不是待执行", taskId);
//            return;
//        }
//
//        LocalDateTime startTime = LocalDateTime.now(BUSINESS_ZONE);
//        task.setTaskStatus(KnowledgeQuestionClusterCheckConstants.TASK_RUNNING); // 设置任务状态为运行中
//        task.setStartTime(startTime); // 设置开始时间为当前时间
//        task.setUpdateTime(startTime); // 设置更新时间为当前时间
//        this.updateById(task);
//
//        log.info("==================== [问题簇检测][批次开始] taskId={}，触发类型={}，截止时间={} ====================",
//                taskId, triggerName(task.getTriggerType()), task.getRangeEndTime());
//        try
//        {
//            List<KnowledgeQueryLog> questions = queryLogService.findPendingQuestions(task.getRangeEndTime());
//            log.info("[问题簇检测][批次扫描] taskId={}，待处理问题数={}，说明=包含历史失败后仍待检测的问题", taskId, questions.size());
//            TaskStatistics statistics = this.executeQuestions(task, questions);
//            this.finishTask(task, statistics, null);
//        }
//        catch (RuntimeException exception)
//        {
//            this.finishTask(task, new TaskStatistics(), safeMessage(exception, "检测任务执行失败"));
//            log.error("==================== [问题簇检测][批次异常] taskId={}，异常类型={}，原因={} ====================",
//                    taskId, exception.getClass().getSimpleName(), safeMessage(exception, "检测任务执行失败"));
//        }
    }

    /** 将触发类型转换为中文日志文案。 */
    private String triggerName(Integer triggerType)
    {
        return Integer.valueOf(KnowledgeQuestionClusterCheckConstants.TRIGGER_MANUAL).equals(triggerType)
                ? "管理员手工触发" : "Quartz自动触发";
    }

    /** 当前批次的内存统计，不承担单题持久化职责。 */
    private static class TaskStatistics
    {
        private int total;
        private int failed;
        private int skipped;
        private int created;
        private int matched;
    }

    /** 严格串行处理问题，单题失败只计数并保留待检测状态。 */
    private TaskStatistics executeQuestions(KnowledgeQuestionClusterCheckTask task, List<KnowledgeQueryLog> questions)
    {
        TaskStatistics statistics = new TaskStatistics();

        // 串行处理可以让日志顺序与实际业务顺序完全一致，便于教学和排查。
        for (KnowledgeQueryLog question : questions)
        {
            statistics.total++;
            try
            {
                // TODO: 拿一条用户提问，判断它和已有的"问题簇"是不是同一个问题，是就归进去，不是就新建一个簇,不合适自动处理的就跳过
                // 定时任务触发,没有用户id，使用默认管理员用户id
                Long operatorUserId = task.getCreateUserId() == null ? SYSTEM_OPERATOR_USER_ID : task.getCreateUserId();
//                int action = KnowledgeQuestionClusterCheckConstants.ACTION_MATCHED;
                int action = matchService.matchQuestion(task.getId(), question, operatorUserId);

                // 按单题最终动作生成批次摘要，不再读取任务明细表二次汇总。
                if (action == KnowledgeQuestionClusterCheckConstants.ACTION_MATCHED)
                {
                    statistics.matched++;
                }
                else if (action == KnowledgeQuestionClusterCheckConstants.ACTION_CREATED)
                {
                    statistics.created++;
                }
                else if (action == KnowledgeQuestionClusterCheckConstants.ACTION_SKIPPED)
                {
                    statistics.skipped++;
                }
            }
            catch (RuntimeException exception)
            {
                statistics.failed++;
                log.warn("[问题簇检测][单题失败] taskId={}，questionId={}，异常类型={}，原因={}；状态保持待检测，下个批次自动重试",
                        task.getId(), question.getId(), exception.getClass().getSimpleName(),
                        safeMessage(exception, "单题处理失败"));
            }
        }
        return statistics;
    }

    /** 保存批次摘要并显著打印最终统计。 */
    private void finishTask(KnowledgeQuestionClusterCheckTask task,
                            TaskStatistics statistics, String taskFailure)
    {
        LocalDateTime finishTime = LocalDateTime.now(BUSINESS_ZONE);
        task.setTotalCount(statistics.total);
        task.setSuccessCount(statistics.created + statistics.matched);
        task.setFailedCount(statistics.failed);
        task.setSkippedCount(statistics.skipped);
        task.setCreatedClusterCount(statistics.created);
        task.setMatchedClusterCount(statistics.matched);
        task.setFinishTime(finishTime);
        task.setDurationMs(task.getStartTime() == null ? 0L : Duration.between(task.getStartTime(), finishTime).toMillis());
        task.setFailureReason(taskFailure);
        task.setTaskStatus(taskFailure == null
                ? KnowledgeQuestionClusterCheckConstants.TASK_COMPLETED
                : KnowledgeQuestionClusterCheckConstants.TASK_FAILED
        );
        task.setUpdateTime(finishTime);
        this.updateById(task);
        log.info("==================== [问题簇检测][批次完成] taskId={}，总数={}，关联={}，新建={}，跳过={}，本轮失败={}，耗时={}ms ====================",
                task.getId(), statistics.total, statistics.matched, statistics.created,
                statistics.skipped, statistics.failed, task.getDurationMs());
    }

    /** 将手工任务提交到单线程执行器。 */
    private void submitTask(Long taskId, Long userId)
    {
//        try
//        {
//            // executeTask 异步执行: 耗时不可控
        taskExecutor.execute(() -> executeTask(taskId));
//        }
//        catch (RuntimeException exception)
//        {
//            log.warn("[问题簇检测][提交失败] taskId={}，异常类型={}，原因={}",
//                    taskId, exception.getClass().getSimpleName(), safeMessage(exception, "任务提交失败"));
//            KnowledgeQuestionClusterCheckTask task = getById(taskId);
//            task.setTaskStatus(KnowledgeQuestionClusterCheckConstants.TASK_FAILED);
//            task.setFailureReason("检测任务提交失败");
//            task.setFinishTime(LocalDateTime.now(BUSINESS_ZONE));
//            task.setUpdateUserId(userId);
//            task.setUpdateTime(LocalDateTime.now(BUSINESS_ZONE));
//            updateById(task);
//            throw new ServiceException("检测任务提交失败，请稍后重试");
//        }
    }

    /** 提取适合业务日志展示的简短异常原因。 */
    private String safeMessage(Throwable throwable, String fallback)
    {
        Throwable cause = throwable.getCause() == null ? throwable : throwable.getCause();
        if (cause instanceof ServiceException && cause.getMessage() != null && !cause.getMessage().isBlank())
        {
            return cause.getMessage();
        }
        return fallback;
    }
}

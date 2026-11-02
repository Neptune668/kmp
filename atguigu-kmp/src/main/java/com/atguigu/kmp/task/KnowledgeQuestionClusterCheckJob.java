
package com.atguigu.kmp.task;

import com.atguigu.kmp.service.KnowledgeQuestionClusterCheckTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 若依 Quartz 问题簇周期检测调用入口。
 */
@Slf4j
@Component("knowledgeQuestionClusterCheckJob")
public class KnowledgeQuestionClusterCheckJob
{
    @Autowired
    private KnowledgeQuestionClusterCheckTaskService taskService;

    /** 创建上一自然小时任务并在当前 Quartz 工作线程执行。 */
    public void execute()
    {
        Long taskId = taskService.createHourlyTask(LocalDateTime.now(ZoneId.of("Asia/Shanghai")));
        log.info("Quartz 问题簇检测开始，taskId={}", taskId);
        taskService.executeTask(taskId);
        log.info("Quartz 问题簇检测完成，taskId={}", taskId);
//        log.info("Quartz 问题簇检测完成-----------------------");
    }
}
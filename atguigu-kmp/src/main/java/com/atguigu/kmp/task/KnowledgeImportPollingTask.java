package com.atguigu.kmp.task;

import com.atguigu.kmp.service.KnowledgeImportTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KnowledgeImportPollingTask {

    @Autowired
    private KnowledgeImportTaskService knowledgeImportTaskService;

    // 每隔5秒执行一次
    // @Scheduled(fixedRate = 5000)

    // 每隔5秒执行一次，上一次执行完毕后开始计算
//    @Scheduled(fixedDelay = 5000) // 配置常量值
    @Scheduled(fixedDelayString = "#{@kmpConfig.knowledgeImportPollIntervalMs}")
    public void pollAndCleanImportTasks(){

        log.info("开始执行知识导入任务轮询");
        //查看智库端的任务的状态
        knowledgeImportTaskService.pollProcessingTasks();
        //清理无效的任务（待上传和导入失败超过30分钟的任务）
        knowledgeImportTaskService.cleanInvalidTasks();
        log.info("知识导入任务轮询执行完毕");
    }
}

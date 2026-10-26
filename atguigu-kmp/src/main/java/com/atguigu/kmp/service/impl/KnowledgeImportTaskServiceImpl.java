package com.atguigu.kmp.service.impl;

import com.atguigu.kmp.config.KmpConfig;
import com.atguigu.kmp.constant.KnowledgeImportConstants;
import com.atguigu.kmp.entity.KnowledgeImportTask;
import com.atguigu.kmp.mapper.KnowledgeImportTaskMapper;
import com.atguigu.kmp.service.KnowledgeImportTaskService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.system.service.ISysUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 知识导入任务表 服务实现类
 * </p>
 *
 * @author atguigu
 * @since 2026-10-20
 */
@Slf4j
@Service
public class KnowledgeImportTaskServiceImpl extends ServiceImpl<KnowledgeImportTaskMapper, KnowledgeImportTask> implements KnowledgeImportTaskService {

    @Autowired
    private ISysUserService sysUserService;

    @Autowired
    @Qualifier("knowledgeSystemRestClient")
    private RestClient restClient;

    @Autowired
    private KmpConfig kmpConfig;

    @Override
    public Page<KnowledgeImportTask> pageImportTask(Page<KnowledgeImportTask> page) {

        LambdaQueryWrapper<KnowledgeImportTask> queryWrapper = new LambdaQueryWrapper<KnowledgeImportTask>()
                .orderByDesc(KnowledgeImportTask::getCreateTime)
                .orderByDesc(KnowledgeImportTask::getId);

        Page<KnowledgeImportTask> result = this.page(page, queryWrapper);

        this.fillCreateUserNames(result.getRecords());

        return result;
    }

    @Override
    public Long initImportTask(KnowledgeImportTask task, Long userId) {

        LocalDateTime now = LocalDateTime.now();
//        task.setCreateUserId(userId);
//        task.setCreateTime(now);
//        task.setTaskStatus(KnowledgeImportConstants.TASK_STATUS_PENDING);
//        task.setIsDeleted(0);
//
//        //防止客户端越权赋值
//        task.setUpdateUserId(null);
//        task.setUpdateTime(null);
//        task.setId(null);
//        //等等其他值全部设置为null

        KnowledgeImportTask knowledgeImportTask = new KnowledgeImportTask();
        knowledgeImportTask.setFileName(task.getFileName());
        knowledgeImportTask.setKnowledgeGroupId(task.getKnowledgeGroupId());
        knowledgeImportTask.setCreateUserId(userId);
        knowledgeImportTask.setCreateTime(now);
        knowledgeImportTask.setTaskStatus(KnowledgeImportConstants.TASK_STATUS_PENDING);
        knowledgeImportTask.setIsDeleted(0);

        this.save(knowledgeImportTask);
        return knowledgeImportTask.getId();
    }

    @Override
    public KnowledgeImportTask bindExternalTask(Long id, String externalTaskId, Long userId) {

//        KnowledgeImportTask task = this.getById(id);
//        task.setExternalTaskId(externalTaskId);
//        task.setTaskStatus(KnowledgeImportConstants.TASK_STATUS_PROCESSING);
//        task.setUpdateUserId(userId);
//        task.setUpdateTime(LocalDateTime.now());
//        this.updateById(task);
//        return task;

        // 查找处理pending状态且尚未被绑定过的task值
        boolean updated = this.lambdaUpdate()
                .eq(KnowledgeImportTask::getId, id)

                // 幂等判断：无论请求执行多少次，我们都希望最终产生的结果是一致的
                .eq(KnowledgeImportTask::getTaskStatus, KnowledgeImportConstants.TASK_STATUS_PENDING)
                .isNull(KnowledgeImportTask::getExternalTaskId)

                .set(KnowledgeImportTask::getExternalTaskId, externalTaskId)
                .set(KnowledgeImportTask::getTaskStatus, KnowledgeImportConstants.TASK_STATUS_PROCESSING)
                .set(KnowledgeImportTask::getCreateUserId, userId)
                .set(KnowledgeImportTask::getUpdateTime, LocalDateTime.now())
                .update();

        if(!updated){
            throw new ServiceException("当前状态不允许绑定外部任务");
        }

        return this.getById(id);
    }

    /** {@inheritDoc} */
    @Override
    public boolean removeImportTask(Long id)
    {
        log.info("开始移除知识导入任务");
        KnowledgeImportTask task = this.getById(id);

        boolean pendingAndUnbound = KnowledgeImportConstants.TASK_STATUS_PENDING.equals(task.getTaskStatus()) && task.getExternalTaskId() == null;
        boolean failed = KnowledgeImportConstants.TASK_STATUS_FAILED.equals(task.getTaskStatus());
        if (!pendingAndUnbound && !failed)
        {
            throw new ServiceException("当前状态不允许移除");
        }

        LambdaQueryWrapper<KnowledgeImportTask> updateWrapper = new LambdaQueryWrapper<KnowledgeImportTask>()
                .eq(KnowledgeImportTask::getId, id)
                .eq(KnowledgeImportTask::getTaskStatus, task.getTaskStatus());

        return this.remove(updateWrapper);
    }

    @Override
    public void pollProcessingTasks() {
        log.info("开始轮询查询知识导入任务的当前状态");

        List<KnowledgeImportTask> processingTaskList = this.lambdaQuery()
                .eq(KnowledgeImportTask::getTaskStatus, KnowledgeImportConstants.TASK_STATUS_PROCESSING)
                .isNotNull(KnowledgeImportTask::getExternalTaskId)
                .orderByAsc(KnowledgeImportTask::getUpdateTime)
                .orderByAsc(KnowledgeImportTask::getId)
                .last("LIMIT " + kmpConfig.getKnowledgeImportBatchSize())
                .list();

        for (KnowledgeImportTask processingTask : processingTaskList) {
            this.processProcessingTask(processingTask);
        }
    }

    private void processProcessingTask(KnowledgeImportTask processingTask) {

        String externalTaskId = processingTask.getExternalTaskId();
        String result = restClient.get()
                .uri("/status/{task_id}", externalTaskId)
                .retrieve()
                .body(String.class);

        log.info("result:{}", result);
    }

    @Override
    public void cleanInvalidTasks() {
        log.info("开始清理无用任务");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void fillCreateUserNames(List<KnowledgeImportTask> records) {

//        List<Long> userIds = records.stream().map(KnowledgeImportTask::getCreateUserId).distinct().toList();
//        userService.listByIds(userIds)

        Map<Long, String> userNameMap = new HashMap<>();
        for (KnowledgeImportTask task : records) {
            Long createUserId = task.getCreateUserId();

            if(!userNameMap.containsKey(createUserId)){
                SysUser sysUser = sysUserService.selectUserById(createUserId);
                userNameMap.put(createUserId, sysUser.getUserName());
            }
            task.setCreateUserName(userNameMap.get(createUserId));
        }
    }
}

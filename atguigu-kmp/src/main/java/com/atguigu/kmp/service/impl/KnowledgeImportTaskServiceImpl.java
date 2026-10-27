package com.atguigu.kmp.service.impl;

import com.atguigu.kmp.config.KmpConfig;
import com.atguigu.kmp.constant.KnowledgeImportConstants;
import com.atguigu.kmp.dto.ExternalKnowledgeTaskStatus;
import com.atguigu.kmp.entity.KnowledgeImportTask;
import com.atguigu.kmp.mapper.KnowledgeImportTaskMapper;
import com.atguigu.kmp.service.KnowledgeImportTaskService;
import com.atguigu.kmp.service.KnowledgeUnitService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.system.service.ISysUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.time.Duration;
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

    @Autowired
    private KnowledgeUnitService knowledgeUnitService;

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


    @Transactional(rollbackFor = Exception.class)
    protected void processProcessingTask(KnowledgeImportTask processingTask) {

        String externalStatusJson = null;
        String externalTaskId = null;
        try {
            externalTaskId = processingTask.getExternalTaskId();
            RestClient.ResponseSpec responseSpec = restClient.get()
                    .uri("/status/{task_id}", externalTaskId)
                    .retrieve();

            //json字符串转换
            externalStatusJson = responseSpec.body(String.class);
            ExternalKnowledgeTaskStatus externalStatus = responseSpec.body(ExternalKnowledgeTaskStatus.class);

            // 掌柜智库端返回的业务全局状态
            Integer status  = switch (externalStatus.getStatus())
            {
                case "processing" -> KnowledgeImportConstants.TASK_STATUS_PROCESSING;
                case "completed" -> KnowledgeImportConstants.TASK_STATUS_SUCCESS;
                case "failed" -> KnowledgeImportConstants.TASK_STATUS_FAILED;
                default -> KnowledgeImportConstants.TASK_STATUS_PROCESSING;
            };


            LocalDateTime now = LocalDateTime.now();

            //如果全局状态是成功
            if(KnowledgeImportConstants.TASK_STATUS_SUCCESS.equals(status))
            {
                // 创建知识单元
                Long knowledgeUnitId = knowledgeUnitService.createFromCompletedImport(processingTask, externalStatus);

                // 修改任务状态为成功。
                LambdaUpdateWrapper<KnowledgeImportTask> taskUpdate = new LambdaUpdateWrapper<KnowledgeImportTask>()
                        .eq(KnowledgeImportTask::getId, processingTask.getId())
                        .eq(KnowledgeImportTask::getTaskStatus, KnowledgeImportConstants.TASK_STATUS_PROCESSING)
                        .isNull(KnowledgeImportTask::getKnowledgeUnitId)
                        .set(KnowledgeImportTask::getKnowledgeUnitId, knowledgeUnitId)
                        .set(KnowledgeImportTask::getTaskStatus, KnowledgeImportConstants.TASK_STATUS_SUCCESS)
                        .set(KnowledgeImportTask::getTaskInfo, externalStatusJson)
                        .set(KnowledgeImportTask::getFailureReason, null) // FieldStrategy.ALWAYS 强制将此字段设置为null
                        .set(KnowledgeImportTask::getFinishTime, now)
                        .set(KnowledgeImportTask::getUpdateTime, now)
                        .set(KnowledgeImportTask::getDurationMs, Duration.between(processingTask.getCreateTime(), now).toMillis());

                this.update(taskUpdate);
            }
            else // 全局状态为进行中或失败
            {
                // 更新task的状态(执行中、失败)、步骤信息等
                LambdaUpdateWrapper<KnowledgeImportTask> updateWrapper = new LambdaUpdateWrapper<KnowledgeImportTask>()
                        .eq(KnowledgeImportTask::getId, processingTask.getId())
                        .eq(KnowledgeImportTask::getTaskStatus, KnowledgeImportConstants.TASK_STATUS_PROCESSING)
                        .set(KnowledgeImportTask::getTaskStatus, status)
                        .set(KnowledgeImportTask::getTaskInfo, externalStatusJson)
                        .set(KnowledgeImportTask::getFailureReason, null)
                        .set(KnowledgeImportTask::getUpdateTime, now);

                if(KnowledgeImportConstants.TASK_STATUS_FAILED.equals(status))
                {
                    updateWrapper
                            .set(KnowledgeImportTask::getFinishTime, now)
                            .set(KnowledgeImportTask::getDurationMs, Duration.between(processingTask.getCreateTime(), now).toMillis());
                }

                this.update(updateWrapper);
            }

        } catch (Exception e) {

            //更新 task 表的task_info等字段信息（失败）
            // 跟新task的状态 TASK_STATUS_FAILED、FailureReason
            log.error("处理知识导入任务发生异常, externalTaskId = {}", externalTaskId, e);
            this.markImportTaskFailed(processingTask, externalStatusJson, "处理知识导入任务发生异常");
        }
    }

    private void markImportTaskFailed(KnowledgeImportTask processingTask, String externalStatusJson, String failureReason) {

        LocalDateTime now = LocalDateTime.now();
        LambdaUpdateWrapper<KnowledgeImportTask> updateWrapper =
                new LambdaUpdateWrapper<KnowledgeImportTask>()
                        .eq(KnowledgeImportTask::getId, processingTask.getId())
                        .eq(KnowledgeImportTask::getTaskStatus, KnowledgeImportConstants.TASK_STATUS_PROCESSING)
                        .isNull(KnowledgeImportTask::getKnowledgeUnitId)
                        .set(KnowledgeImportTask::getTaskStatus, KnowledgeImportConstants.TASK_STATUS_FAILED)
                        .set(KnowledgeImportTask::getTaskInfo, externalStatusJson)
                        .set(KnowledgeImportTask::getFailureReason, failureReason)
                        .set(KnowledgeImportTask::getFinishTime, now)
                        .set(KnowledgeImportTask::getDurationMs, Duration.between(processingTask.getCreateTime(), now).toMillis())
                        .set(KnowledgeImportTask::getUpdateTime, now);
        this.update(updateWrapper);

    }

    @Override
    public boolean cleanInvalidTasks() {
        log.info("开始清理无用任务");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expireTime = now.minusMinutes(kmpConfig.getKnowledgeImportExpireMinutes());

        LambdaQueryWrapper<KnowledgeImportTask> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .and(wrapper -> wrapper
                        .eq(KnowledgeImportTask::getTaskStatus, KnowledgeImportConstants.TASK_STATUS_PENDING)
                        .isNull(KnowledgeImportTask::getExternalTaskId)
                        .lt(KnowledgeImportTask::getCreateTime, expireTime))
                .or(wrapper -> wrapper
                        .eq(KnowledgeImportTask::getTaskStatus, KnowledgeImportConstants.TASK_STATUS_FAILED)
                        .lt(KnowledgeImportTask::getCreateTime, expireTime));


        return this.remove(queryWrapper);
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

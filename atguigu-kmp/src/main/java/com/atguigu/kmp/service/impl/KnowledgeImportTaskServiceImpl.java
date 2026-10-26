package com.atguigu.kmp.service.impl;

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
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
 * @since 2026-10-21
 */
@Service
@Slf4j
public class KnowledgeImportTaskServiceImpl extends ServiceImpl<KnowledgeImportTaskMapper, KnowledgeImportTask> implements KnowledgeImportTaskService {

    @Autowired
    private ISysUserService sysUserService;

    @Override
    public Page<KnowledgeImportTask> pageKnowledgeImportTasks(Page<KnowledgeImportTask> page, KnowledgeImportTask query) {

        log.info("开始分页查询知识导入任务，pageNum={}，pageSize={}", page.getCurrent(), page.getSize());

        // 文件名支持模糊匹配，去空格并将空字符串转为 null，避免拼接 %null%
        String fileName = StringUtils.trimToNull(query.getFileName());

        LambdaQueryWrapper<KnowledgeImportTask> wrapper = new LambdaQueryWrapper<KnowledgeImportTask>()

                // like 和 eq 的第一个参数就是开关，为 false 时不会拼接对应条件
                .like(fileName != null, KnowledgeImportTask::getFileName, fileName)
                .eq(query.getKnowledgeGroupId() != null, KnowledgeImportTask::getKnowledgeGroupId, query.getKnowledgeGroupId())
                .eq(query.getKnowledgeUnitId() != null, KnowledgeImportTask::getKnowledgeUnitId, query.getKnowledgeUnitId())
                .eq(query.getTaskStatus() != null, KnowledgeImportTask::getTaskStatus, query.getTaskStatus())
                .eq(query.getCreateUserId() != null, KnowledgeImportTask::getCreateUserId, query.getCreateUserId())

                // 先用创建时间排序，时间相同再用 ID 保证顺序稳定
                .orderByDesc(KnowledgeImportTask::getCreateTime)
                .orderByDesc(KnowledgeImportTask::getId);

        Page<KnowledgeImportTask> result = this.page(page, wrapper);

        // 补充创建人名称，仅用于列表展示
        this.fillCreateUserNames(result.getRecords());

        return result;
    }

    /** {@inheritDoc} */
    @Override
    public Long initImportTask(KnowledgeImportTask task, Long userId)
    {
        log.info("开始初始化知识导入任务");

        // 初始化接口只接受文件名和知识组，其余字段全部由平台重置，防止客户端越权赋值。
        // 这样做是在划安全边界：调用方可以说明“文件叫什么、放进哪个组”，但不能伪造任务 ID、处理状态、创建人和完成时间。
        LocalDateTime now = LocalDateTime.now();
        task.setId(null);
        task.setKnowledgeUnitId(null);
        task.setExternalTaskId(null);
        task.setTaskStatus(KnowledgeImportConstants.TASK_STATUS_PENDING);
        task.setTaskInfo(null);
        task.setFailureReason(null);
        task.setFinishTime(null);
        task.setDurationMs(null);
        task.setCreateUserId(userId);
        task.setCreateTime(now);
        task.setUpdateUserId(null);
        task.setUpdateTime(null);
        task.setIsDeleted(0);
        task.setCreateUserName(null);
        this.save(task);
        return task.getId();

//        KnowledgeImportTask knowledgeImportTask = new KnowledgeImportTask();
//        knowledgeImportTask.setFileName(task.getFileName());
//        knowledgeImportTask.setKnowledgeGroupId(task.getKnowledgeGroupId());
//        knowledgeImportTask.setTaskStatus(KnowledgeImportConstants.TASK_STATUS_PENDING);
//        knowledgeImportTask.setCreateUserId(userId);
//        knowledgeImportTask.setCreateTime(now);
//        knowledgeImportTask.setIsDeleted(0);
//        this.save(knowledgeImportTask);
//        return knowledgeImportTask.getId();
    }

    /** {@inheritDoc} */
    @Override
    public KnowledgeImportTask bindExternalTask(Long id, String externalTaskId, Long userId)
    {
        log.info("开始绑定外部知识导入任务，taskId={}，externalTaskId={}", id, externalTaskId);

        LocalDateTime now = LocalDateTime.now();
        boolean updated = lambdaUpdate()
                .eq(KnowledgeImportTask::getId, id)
                .eq(KnowledgeImportTask::getTaskStatus, KnowledgeImportConstants.TASK_STATUS_PENDING)
                .isNull(KnowledgeImportTask::getExternalTaskId)
                .set(KnowledgeImportTask::getExternalTaskId, externalTaskId)
                .set(KnowledgeImportTask::getTaskStatus, KnowledgeImportConstants.TASK_STATUS_PROCESSING)
                .set(KnowledgeImportTask::getUpdateUserId, userId)
                .set(KnowledgeImportTask::getUpdateTime, now)
                .update();
        if (!updated)
        {
            throw new ServiceException("当前状态不允许绑定外部任务");
        }
        return this.getById(id);
    }
    /** 批量补充当前页创建人名称，同一用户只查询一次，避免重复查询。 */
    private void fillCreateUserNames(List<KnowledgeImportTask> records)
    {
        if (records == null || records.isEmpty())
        {
            return;
        }

        Map<Long, String> userNameMap = new HashMap<>();
        for (KnowledgeImportTask record : records)
        {
            Long createUserId = record.getCreateUserId();
            if (createUserId == null)
            {
                continue;
            }
            record.setCreateUserName(userNameMap.computeIfAbsent(createUserId, this::findUserDisplayName));
        }
    }

    /** 根据用户 ID 获取展示名称。 */
    private String findUserDisplayName(Long userId)
    {
        SysUser user = sysUserService.selectUserById(userId);
        return user == null ? null : user.getNickName();
    }
}

package com.atguigu.kmp.service;

import com.atguigu.kmp.entity.KnowledgeImportTask;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.IService;

/**
 * <p>
 * 知识导入任务表 服务类
 * </p>
 *
 * @author atguigu
 * @since 2026-10-21
 */
public interface KnowledgeImportTaskService extends IService<KnowledgeImportTask> {

    Page<KnowledgeImportTask> pageKnowledgeImportTasks(Page<KnowledgeImportTask> page, KnowledgeImportTask query);

    /**
     * 初始化知识导入任务。
     *
     * @param task 任务入参（至少包含知识组ID和原始文件名）
     * @param userId 当前用户 ID
     * @return 新任务 ID
     */
    Long initImportTask(KnowledgeImportTask task, Long userId);

    /**
     * 将外部服务返回的任务编号绑定到原平台任务。
     *
     * @param id 平台任务 ID
     * @param externalTaskId 外部任务编号
     * @param userId 当前用户 ID
     * @return 绑定后的任务
     */
    KnowledgeImportTask bindExternalTask(Long id, String externalTaskId, Long userId);

    /**
     * 逻辑移除未绑定的待上传任务或导入失败任务。
     *
     * @param id 平台任务 ID
     * @return 是否移除成功
     */
    boolean removeImportTask(Long id);
}

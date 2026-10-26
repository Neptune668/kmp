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
}

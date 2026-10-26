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
}

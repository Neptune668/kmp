package com.atguigu.kmp.service;

import com.atguigu.kmp.entity.KnowledgeImportTask;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.IService;
import jakarta.validation.constraints.Size;

/**
 * <p>
 * 知识导入任务表 服务类
 * </p>
 *
 * @author atguigu
 * @since 2026-10-20
 */
public interface KnowledgeImportTaskService extends IService<KnowledgeImportTask> {

    /**
     * 分页查询任务列表
     * @param page
     * @return
     */
    Page<KnowledgeImportTask> pageImportTask(Page<KnowledgeImportTask> page);

    /**
     * 对上传任务记录进行初始化
     * @param task
     * @param userId
     * @return
     */
    Long initImportTask(KnowledgeImportTask task, Long userId);

    /**
     * 将智能体中台的任务id和掌柜智库的task_id进行绑定
     * @param id
     * @param externalTaskId
     * @param userId
     * @return
     */
    KnowledgeImportTask bindExternalTask(Long id, @Size(max = 128, message = "外部任务编号不能超过128个字符") String externalTaskId, Long userId);

    /**
     * 移除问题记录（上传未成功）
     * @param id
     */
    boolean removeImportTask(Long id);

    void pollProcessingTasks();

    boolean cleanInvalidTasks();

}

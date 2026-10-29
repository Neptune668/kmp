package com.atguigu.kmp.service;

import com.atguigu.kmp.entity.KnowledgeQuestionCluster;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.IService;

import java.util.Map;

/**
 * <p>
 * 问题簇表 服务类
 * </p>
 *
 * @author atguigu
 * @since 2026-10-20
 */
public interface KnowledgeQuestionClusterService extends IService<KnowledgeQuestionCluster> {

    /** 分页查询问题簇。 */
    IPage<KnowledgeQuestionCluster> pageQuestionClusters(Page<KnowledgeQuestionCluster> page, KnowledgeQuestionCluster query);

    KnowledgeQuestionCluster getQuestionClusterDetail(Long id);

    /** 删除未发布问题簇。 */
    void removeQuestionCluster(Long id, Long userId);

    Map<String, Object> createQuestionCluster(KnowledgeQuestionCluster cluster, Long userId);

    void updateQuestionCluster(KnowledgeQuestionCluster cluster, Long userId);

    /** 发布沉淀中或待发布问题簇。 */
    void publishQuestionCluster(Long id, Long userId);

    /** 取消发布已发布问题簇。 */
    void unpublishQuestionCluster(Long id, Long userId);
}

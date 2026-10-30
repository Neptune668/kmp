package com.atguigu.kmp.service;

import com.atguigu.kmp.dto.KnowledgeFaqModels;
import com.atguigu.kmp.entity.KnowledgeQuestionCluster;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.IService;

import java.util.List;
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

    /**
     * 发布沉淀中或等待发布的问题簇
     * @param id
     * @param userId
     */
    void publishQuestionCluster(Long id, Long userId);

    /**
     * 取消发布沉淀中或等待发布的问题簇
     * @param id
     * @param userId
     */
    void unpublishQuestionCluster(Long id, Long userId);

    /** 查询当前知识单元的热门 FAQ。 */
    List<KnowledgeFaqModels.KnowledgeFaqItem> listRecommendedFaqs(String itemName);

    List<KnowledgeFaqModels.KnowledgeFaqItem> listSuggestedFaqs(String keyword);

    /** 调用外部知识库生成参考答案但不保存。 */
//    String generateReferenceAnswer(Long id);

    /** 返回 FAQ 答案。 */
    KnowledgeFaqModels.KnowledgeFaqAnswer getFaqAnswer(Long id, Long userId, String requestId);
}

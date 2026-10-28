package com.atguigu.kmp.service;

import com.atguigu.kmp.entity.KnowledgeQuestionCluster;

/**
 * 问题簇 Milvus 向量维护与召回服务。
 */
public interface KnowledgeQuestionClusterVectorService
{
    /** 按问题簇主键幂等写入向量。 */
    void saveQuestionClusterVector(KnowledgeQuestionCluster cluster);

    /** 按问题簇主键幂等删除向量。 */
    void deleteQuestionClusterVector(Long questionClusterId);
}
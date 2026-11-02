package com.atguigu.kmp.service;

import com.atguigu.kmp.entity.KnowledgeQuestionCluster;

/**
 * 问题与候选问题簇的二次语义判断服务。
 */
public interface KnowledgeQuestionClusterSemanticService
{
    /** 判断改写问题是否应关联第一候选。 */
    boolean shouldMatch(String rewrittenQuestion, KnowledgeQuestionCluster candidate);
}

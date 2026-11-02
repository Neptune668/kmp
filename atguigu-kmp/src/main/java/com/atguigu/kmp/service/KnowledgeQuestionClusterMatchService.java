package com.atguigu.kmp.service;

import com.atguigu.kmp.entity.KnowledgeQueryLog;

/**
 * 单条查询日志的问题簇匹配服务。
 */
public interface KnowledgeQuestionClusterMatchService
{
    /** 串行处理单题并返回关联、新建或跳过动作。 */
    int matchQuestion(Long taskId, KnowledgeQueryLog question, Long operatorUserId);
}

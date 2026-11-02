package com.atguigu.kmp.service.impl;

import com.atguigu.kmp.constant.KnowledgeQuestionClusterCheckConstants;
import com.atguigu.kmp.entity.KnowledgeQueryLog;
import com.atguigu.kmp.service.KnowledgeQuestionClusterMatchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 单题问题簇三层判断、关联和建簇实现。
 */
@Slf4j
@Service
public class KnowledgeQuestionClusterMatchServiceImpl
        implements KnowledgeQuestionClusterMatchService
{

    /** {@inheritDoc} */
    @Override
    public int matchQuestion(Long taskId, KnowledgeQueryLog question, Long operatorUserId)
    {
        String rewrittenQuestion = question.getRewirttenQuery().trim();
        log.info("-------------------- [问题簇检测][单题开始] taskId={}，questionId={} --------------------",
                taskId, question.getId());

        // TODO
        return KnowledgeQuestionClusterCheckConstants.ACTION_CREATED;
    }
}

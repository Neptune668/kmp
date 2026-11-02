package com.atguigu.kmp.service;

import com.atguigu.kmp.dto.KnowledgeExternalQueryLogModels.KnowledgeQueryLogCollectRequest;
import com.atguigu.kmp.dto.KnowledgeExternalQueryLogModels.KnowledgeQueryLogCollectResult;
import com.atguigu.kmp.entity.KnowledgeQueryLog;
import com.baomidou.mybatisplus.spring.service.IService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 直接知识查询日志表 服务类
 * </p>
 *
 * @author atguigu
 * @since 2026-10-20
 */
public interface KnowledgeQueryLogService extends IService<KnowledgeQueryLog> {

    /** 校验跨系统日志并写入 Redis Stream。 */
    KnowledgeQueryLogCollectResult enqueueQueryLog(KnowledgeQueryLogCollectRequest request);
    /** 校验跨系统日志并写入 Redis Stream。 */


    /** 读取截止时间前仍待检测且具有改写问题和知识单元的日志。 */
    List<KnowledgeQueryLog> findPendingQuestions(LocalDateTime rangeEndTime);

}

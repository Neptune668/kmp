package com.atguigu.kmp.service.impl;

import com.atguigu.kmp.entity.KnowledgeQuerySession;
import com.atguigu.kmp.mapper.KnowledgeQuerySessionMapper;
import com.atguigu.kmp.service.KnowledgeQuerySessionService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * <p>
 * 知识查询会话索引表 服务实现类
 * </p>
 *
 * @author atguigu
 * @since 2026-10-21
 */
@Slf4j
@Service
public class KnowledgeQuerySessionServiceImpl extends ServiceImpl<KnowledgeQuerySessionMapper, KnowledgeQuerySession> implements KnowledgeQuerySessionService {

    @Override
    public Long saveKnowledgeGroup(KnowledgeQuerySession knowledgeQuerySession, Long userId) {
//        knowledgeQuerySession.setId();
        knowledgeQuerySession.setCreateUserId(userId);
        knowledgeQuerySession.setCreateTime(LocalDateTime.now());
        knowledgeQuerySession.setUpdateUserId(null);
        knowledgeQuerySession.setUpdateTime(null);
        knowledgeQuerySession.setIsDeleted(0);
        this.save(knowledgeQuerySession);
        return knowledgeQuerySession.getId();
    }


    @Override
    public IPage<KnowledgeQuerySession> pageSessions(
            Page<KnowledgeQuerySession> page, String sessionTitle, Long userId
    )
    {
        log.info("开始分页查询知识会话，userId={}，sessionTitle={}", userId, sessionTitle == null);

        LambdaQueryWrapper<KnowledgeQuerySession> wrapper =
                new LambdaQueryWrapper<KnowledgeQuerySession>()
                        .eq(KnowledgeQuerySession::getCreateUserId, userId)
                        .like(sessionTitle != null, KnowledgeQuerySession::getSessionTitle, sessionTitle)
                        .orderByDesc(KnowledgeQuerySession::getCreateTime)
                        .orderByDesc(KnowledgeQuerySession::getId);
        IPage<KnowledgeQuerySession> result = page(page, wrapper);
        return result;
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeSession(Long id, Long userId)
    {
        log.info("开始删除知识查询会话，userId={}，sessionId={}", userId, id);

        LocalDateTime now = LocalDateTime.now();

        // 如果想在逻辑删除时同时更新审计字段（更新用户、更新时间），可以使用以下代码显示做update操作
        LambdaUpdateWrapper<KnowledgeQuerySession> updateWrapper =
                new LambdaUpdateWrapper<KnowledgeQuerySession>()
                        .eq(KnowledgeQuerySession::getId, id)
                        .eq(KnowledgeQuerySession::getCreateUserId, userId)
                        .eq(KnowledgeQuerySession::getIsDeleted, 0)
                        .set(KnowledgeQuerySession::getIsDeleted, 1)
                        .set(KnowledgeQuerySession::getUpdateUserId, userId)
                        .set(KnowledgeQuerySession::getUpdateTime, now);
        return update(updateWrapper);
    }
}

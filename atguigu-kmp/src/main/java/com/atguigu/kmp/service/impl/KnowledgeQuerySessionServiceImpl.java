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
 * @since 2026-10-20
 */
@Slf4j
@Service
public class KnowledgeQuerySessionServiceImpl extends ServiceImpl<KnowledgeQuerySessionMapper, KnowledgeQuerySession> implements KnowledgeQuerySessionService {

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
    public Long createSession(KnowledgeQuerySession session, Long userId)
    {
        String sessionTitle = session.getSessionTitle();
        log.info("开始创建知识查询会话", userId, sessionTitle);

        // 所有主键和审计字段均以登录上下文及服务端时间为准。
        session.setId(null);
        session.setSessionTitle(sessionTitle);
        session.setCreateUserId(userId);
        session.setCreateTime(LocalDateTime.now());
        session.setUpdateUserId(null);
        session.setUpdateTime(null);
        session.setIsDeleted(0);
        this.save(session);

        return session.getId();
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

        //不会记录审计字段
//        LambdaQueryWrapper<KnowledgeQuerySession> queryWrapper = new LambdaQueryWrapper<KnowledgeQuerySession>()
//                        .eq(KnowledgeQuerySession::getId, id).eq(KnowledgeQuerySession::getCreateUserId, userId);
//        return remove(queryWrapper);
    }


    /** {@inheritDoc} */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean clearSessions(Long userId)
    {
        log.info("开始清理知识查询会话，userId={}", userId);
        LocalDateTime now = LocalDateTime.now();

        // 只清理当前用户的有效索引，并同步维护审计字段。
        LambdaUpdateWrapper<KnowledgeQuerySession> wrapper =
                new LambdaUpdateWrapper<KnowledgeQuerySession>()
                        .eq(KnowledgeQuerySession::getCreateUserId, userId)
                        .eq(KnowledgeQuerySession::getIsDeleted, 0)
                        .set(KnowledgeQuerySession::getIsDeleted, 1)
                        .set(KnowledgeQuerySession::getUpdateUserId, userId)
                        .set(KnowledgeQuerySession::getUpdateTime, now);
        return update(wrapper);
    }

}

package com.atguigu.kmp.service;

import com.atguigu.kmp.entity.KnowledgeQuerySession;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.IService;

/**
 * <p>
 * 知识查询会话索引表 服务类
 * </p>
 *
 * @author atguigu
 * @since 2026-10-21
 */
public interface KnowledgeQuerySessionService extends IService<KnowledgeQuerySession> {

    Long saveKnowledgeGroup(KnowledgeQuerySession knowledgeQuerySession, Long userId);

    /**
     * 分页查询当前用户的未删除会话。
     *
     * @param page MyBatis-Plus 分页参数
     * @param sessionTitle 会话标题查询条件
     * @param userId 当前登录用户 ID
     * @return 当前用户的会话分页数据
     */
    IPage<KnowledgeQuerySession> pageSessions(Page<KnowledgeQuerySession> page, String sessionTitle, Long userId);

    /**
     * 逻辑删除当前用户的指定会话。
     *
     * @param id 会话 ID
     * @param userId 当前登录用户 ID
     * @return 是否删除成功
     */
    boolean removeSession(Long id, Long userId);
}

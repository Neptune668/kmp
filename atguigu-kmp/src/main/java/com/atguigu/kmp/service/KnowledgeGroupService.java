package com.atguigu.kmp.service;

import com.atguigu.kmp.entity.KnowledgeGroup;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.IService;

/**
 * <p>
 * 知识组表 服务类
 * </p>
 *
 * @author atguigu
 * @since 2026-10-21
 */
public interface KnowledgeGroupService extends IService<KnowledgeGroup> {
    /*
    分页
     */
    Page<KnowledgeGroup> pageKnowledgeGroups(Page<KnowledgeGroup> page, KnowledgeGroup query);

    Long saveKnowledgeGroup(KnowledgeGroup post,Long i);
}

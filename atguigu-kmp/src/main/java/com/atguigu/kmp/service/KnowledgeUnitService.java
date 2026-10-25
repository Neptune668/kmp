package com.atguigu.kmp.service;

import com.atguigu.kmp.entity.KnowledgeUnit;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.IService;

/**
 * <p>
 * 知识单元表 服务类
 * </p>
 *
 * @author atguigu
 * @since 2026-10-21
 */
public interface KnowledgeUnitService extends IService<KnowledgeUnit> {

    /**
     * 分页查询知识单元列表。
     *
     * @param page  分页参数
     * @param query 查询条件（知识单元名称、所属知识组、外部文档ID等）
     * @return 分页查询结果
     */
    Page<KnowledgeUnit> pageKnowledgeUnits(Page<KnowledgeUnit> page, KnowledgeUnit query);

    /**
     * 查询不加载切片正文的基础详情。
     *
     * @param id 知识单元 ID
     * @return 基础详情
     */
    KnowledgeUnit getKnowledgeUnitDetail(Long id);
}

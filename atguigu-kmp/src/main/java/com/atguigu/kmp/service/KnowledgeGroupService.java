package com.atguigu.kmp.service;

import com.atguigu.kmp.entity.KnowledgeGroup;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.IService;

import java.util.List;

/**
 * 知识组表 服务类
 *
 * @author atguigu
 * @since 2026-10-21
 */
public interface KnowledgeGroupService extends IService<KnowledgeGroup> {
    /**
     * 分页查询知识组列表。
     *
     * @param page  分页参数
     * @param query 查询条件（知识组名称等）
     * @return 分页查询结果
     */
    Page<KnowledgeGroup> pageKnowledgeGroups(Page<KnowledgeGroup> page, KnowledgeGroup query);

    /**
     * 新增或修改知识组。
     * <p>
     * 当知识组 ID 为空时执行新增，否则执行修改；同时维护创建/更新审计字段，
     * 若被设置为默认知识组则取消其他知识组的默认标记。
     *
     * @param post 知识组信息
     * @param i    操作人用户ID
     * @return 保存后的知识组主键 ID
     */
    Long saveKnowledgeGroup(KnowledgeGroup post, Long i);

    /**
     * 根据主键 ID 查询知识组详情（含创建人、更新人姓名）。
     *
     * @param id 知识组 ID
     * @return 知识组详情，不存在时抛出异常
     */
    KnowledgeGroup getKnowledgeGroupDetail(Long id);

    /**
     * 删除知识组（逻辑删除）。
     * <p>
     * 默认知识组不允许删除；删除时同步维护更新审计字段。
     *
     * @param id     知识组 ID
     * @param userId 操作人用户ID
     * @return 删除是否成功
     */
    Boolean removeKnowledgeGroup(Long id, Long userId);

    /**
     * 查询未删除知识组，供其他位置的下拉列表选择。
     *
     * @return 知识组选项列表
     */
    List<KnowledgeGroup> listSelectableKnowledgeGroups();

}

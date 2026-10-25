package com.atguigu.kmp.service;

import com.atguigu.kmp.entity.KnowledgeGroup;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.IService;

import java.util.List;

/**
 * <p>
 * 知识组表 服务类
 * </p>
 *
 * @author atguigu
 * @since 2026-10-20
 */
public interface KnowledgeGroupService extends IService<KnowledgeGroup> {

    Page<KnowledgeGroup> pageKnowledgeGroups(Page<KnowledgeGroup> page, KnowledgeGroup query);

    /***
     * 新增或保存知识组
     * @param knowledgeGroup
     * @param userId
     * @return 当前操作的数据的id
     */
    Long saveKnowledgeGroup(KnowledgeGroup knowledgeGroup, Long userId);

    /**
     * 根据id查询知识组详情
     * @param id 记录的唯一标识
     * @return 一条知识组记录
     */
    KnowledgeGroup getKnowledgeGroupDetail(Long id);

    /**
     * 根据id删除当前记录
     * @param id
     * @param userId
     */
    boolean removeKnowledgeGroup(Long id, Long userId);

    /**
     * 查询知识组下拉选项。
     *
     * @return 仅包含 ID、名称和默认标志的知识组
     */
    List<KnowledgeGroup> listKnowledgeGroupOptions();


//    /***
//     * 新增知识组
//     * @param knowledgeGroup
//     * @param userId
//     * @return 新增的数据的id
//     */
//    Long saveKnowledgeGroup(KnowledgeGroup knowledgeGroup, Long userId);
//
//    /**
//     * 修改知识组
//     * @param knowledgeGroup
//     * @param userId
//     * @return 影响的行数
//     */
//    boolean updateKnowledgeGroup(KnowledgeGroup knowledgeGroup, Long userId);

}

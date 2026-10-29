package com.atguigu.kmp.mapper;

import com.atguigu.kmp.entity.KnowledgeQuestionCluster;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 问题簇表 Mapper 接口
 * </p>
 *
 * @author atguigu
 * @since 2026-10-20
 */
public interface KnowledgeQuestionClusterMapper extends BaseMapper<KnowledgeQuestionCluster> {


    /**
     * 关联知识单元和知识组分页查询问题簇。
     *
     * @param page 分页参数
     * @param query 查询条件
     * @return 问题簇分页
     */
    IPage<KnowledgeQuestionCluster> selectQuestionClusterPage(
            Page<KnowledgeQuestionCluster> page,
            @Param("query") KnowledgeQuestionCluster query);

    /**
     * 根据知识单元名称查询推荐问题簇。
     *
     * @param itemName 问题簇名称
     * @return 问题簇
     */
    List<KnowledgeQuestionCluster> selectRecommendedFaqs(@Param("itemName") String itemName, @Param("limit") Object limit);
}

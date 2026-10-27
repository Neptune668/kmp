package com.atguigu.kmp.service;

import com.atguigu.kmp.dto.ExternalKnowledgeTaskStatus;
import com.atguigu.kmp.dto.KnowledgeChunk;
import com.atguigu.kmp.dto.KnowledgeChunkPageResponse;
import com.atguigu.kmp.entity.KnowledgeImportTask;
import com.atguigu.kmp.entity.KnowledgeUnit;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.IService;

/**
 * <p>
 * 知识单元表 服务类
 * </p>
 *
 * @author atguigu
 * @since 2026-10-20
 */
public interface KnowledgeUnitService extends IService<KnowledgeUnit> {

    /**
     * 按名称和知识组分页查询知识单元。
     *
     * @param page 分页参数
     * @param query 查询条件
     * @return 知识单元分页
     */
    IPage<KnowledgeUnit> pageKnowledgeUnits(Page<KnowledgeUnit> page, KnowledgeUnit query);

    /**
     * 查询不加载切片正文的基础详情。
     *
     * @param id 知识单元 ID
     * @return 基础详情
     */
    KnowledgeUnit getKnowledgeUnitDetail(Long id);

    /**
     * 只修改知识单元名称和所属组。
     *
     * @param knowledgeUnit 编辑对象
     * @param userId 当前用户 ID
     * @return 知识单元 ID
     */
    Long saveKnowledgeUnitBaseInfo(KnowledgeUnit knowledgeUnit, Long userId);

    /**
     * 物理删除外部文档切片后逻辑删除平台元数据。
     *
     * @param id 知识单元 ID
     * @return 是否删除成功
     */
    boolean removeKnowledgeUnit(Long id);

    /**
     * 校验fileSha256是否已存在
     * @param fileSha256
     * @return
     */
    KnowledgeUnit findBySha256(String fileSha256);

    Long createFromCompletedImport(KnowledgeImportTask processingTask, ExternalKnowledgeTaskStatus externalStatus);


    /**
     * 根据知识单元id查询分片信息
     * @param id
     * @param pageNum
     * @param pageSize
     * @param userId
     * @return
     */
    KnowledgeChunkPageResponse pageChunks(Long id, Long pageNum, Long pageSize, Long userId);

    KnowledgeChunk saveChunk(Long id, String chunkId, KnowledgeChunk chunk, Long userId);

    KnowledgeChunk removeChunk(Long id, String chunkId, Long userId);
}

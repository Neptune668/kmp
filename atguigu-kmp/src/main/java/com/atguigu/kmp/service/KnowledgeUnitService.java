package com.atguigu.kmp.service;

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
     * 根据文件内容 SHA-256 摘要校验文件是否已导入（内容重复）。
     *
     * @param fileSha256 文件 SHA-256 小写十六进制摘要
     * @return 内容相同的已导入知识单元；不存在返回 null
     */
    KnowledgeUnit findByFileSha256(String fileSha256);
}

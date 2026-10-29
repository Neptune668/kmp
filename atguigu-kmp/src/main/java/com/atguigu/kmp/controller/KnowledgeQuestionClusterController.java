package com.atguigu.kmp.controller;

import com.atguigu.kmp.dto.KnowledgeFaqModels;
import com.atguigu.kmp.entity.KnowledgeQuestionCluster;
import com.atguigu.kmp.service.KnowledgeQuestionClusterService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.constant.HttpStatus;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 问题簇表 前端控制器
 * </p>
 *
 * @author atguigu
 * @since 2026-10-20
 */
@RestController
@RequestMapping("/kmp/questionCluster")
public class KnowledgeQuestionClusterController extends BaseController {

    @Autowired
    private KnowledgeQuestionClusterService knowledgeQuestionClusterService;

    /**
     * GET /kmp/questionCluster/list：分页查询问题簇。
     * @param query 查询条件
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return 若依分页响应
     */
    @PreAuthorize("@ss.hasPermi('kmp:questionCluster:list')")
    @GetMapping("/list")
    public TableDataInfo list(
            KnowledgeQuestionCluster query,
            @RequestParam Integer pageNum,
            @RequestParam Integer pageSize
    )
    {
        Page<KnowledgeQuestionCluster> page = new Page<>(pageNum, pageSize);
        IPage<KnowledgeQuestionCluster> result = knowledgeQuestionClusterService.pageQuestionClusters(page, query);
        TableDataInfo response = new TableDataInfo(result.getRecords(), result.getTotal());
        response.setCode(HttpStatus.SUCCESS);
        response.setMsg("查询成功");
        return response;
    }

    /**
     * GET /kmp/questionCluster/{id}：查询问题簇详情。
     *
     * @param id 问题簇 ID
     * @return 问题簇详情
     */
    @PreAuthorize("@ss.hasPermi('kmp:questionCluster:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable @Positive(message = "问题簇 ID 不正确") Long id)
    {
        return AjaxResult.success(knowledgeQuestionClusterService.getQuestionClusterDetail(id));
    }


    /**
     * DELETE /kmp/questionCluster/{id}：删除未发布问题簇。
     *
     * @param id 问题簇 ID
     * @return 操作结果
     */
    @PreAuthorize("@ss.hasPermi('kmp:questionCluster:remove')")
    @Log(title = "问题簇", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    public AjaxResult remove(@PathVariable @Positive(message = "问题簇 ID 不正确") Long id)
    {
        knowledgeQuestionClusterService.removeQuestionCluster(id, this.getUserId());
        return AjaxResult.success();
    }

    /**
     * POST /kmp/questionCluster：手工新增问题簇。
     *
     * @param cluster 可编辑字段
     * @return 问题簇 ID 和向量同步结果
     */
    @PreAuthorize("@ss.hasPermi('kmp:questionCluster:add')")
    @Log(title = "问题簇", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody KnowledgeQuestionCluster cluster)
    {
        Map<String, Object> result = knowledgeQuestionClusterService.createQuestionCluster(cluster, getUserId());
        return AjaxResult.success(result);
    }

    /**
     * PUT /kmp/questionCluster：编辑问题簇字段。
     *
     * @param cluster 可编辑字段及 ID
     * @return 操作结果
     */
    @PreAuthorize("@ss.hasPermi('kmp:questionCluster:edit')")
    @Log(title = "问题簇", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody KnowledgeQuestionCluster cluster)
    {
        knowledgeQuestionClusterService.updateQuestionCluster(cluster, getUserId());
        return AjaxResult.success();
    }

    /**
     * POST /kmp/questionCluster/{id}/publish：发布沉淀中或待发布问题簇。
     *
     * @param id 问题簇 ID
     * @return 操作结果
     */
    @PreAuthorize("@ss.hasPermi('kmp:questionCluster:publish')")
    @Log(title = "问题簇发布", businessType = BusinessType.UPDATE)
    @PostMapping("/{id}/publish")
    public AjaxResult publish(@PathVariable @Positive(message = "问题簇 ID 不正确") Long id)
    {
        knowledgeQuestionClusterService.publishQuestionCluster(id, this.getUserId());
        return AjaxResult.success();
    }

    /**
     * POST /kmp/questionCluster/{id}/unpublish：取消发布问题簇。
     *
     * @param id 问题簇 ID
     * @return 操作结果
     */
    @PreAuthorize("@ss.hasPermi('kmp:questionCluster:unpublish')")
    @Log(title = "问题簇取消发布", businessType = BusinessType.UPDATE)
    @PostMapping("/{id}/unpublish")
    public AjaxResult unpublish(@PathVariable @Positive(message = "问题簇 ID 不正确") Long id)
    {
        knowledgeQuestionClusterService.unpublishQuestionCluster(id, getUserId());
        return AjaxResult.success();
    }

    /**
     * GET /kmp/questionCluster/faq/recommendations：查询当前知识单元的热门 FAQ。
     *
     * @param itemName 外部知识库识别的知识单元名称
     * @return FAQ 列表（不包含答案）
     */
    @PreAuthorize("@ss.hasPermi('kmp:knowledgeQuery:query')")
    @GetMapping("/faq/recommendations")
    public AjaxResult recommendFaqs(@RequestParam(required = false) String itemName)
    {
        List<KnowledgeFaqModels.KnowledgeFaqItem> faqItems = knowledgeQuestionClusterService.listRecommendedFaqs(itemName);
        return AjaxResult.success(faqItems);
    }

    /**
     * GET /kmp/questionCluster/faq/suggestions：查询 FAQ 输入联想。
     *
     * @param keyword 用户当前输入内容
     * @return 不包含答案的 FAQ 列表
     */
    @PreAuthorize("@ss.hasPermi('kmp:knowledgeQuery:query')")
    @GetMapping("/faq/suggestions")
    public AjaxResult suggestFaqs(@RequestParam(required = false) String keyword)
    {
        List<KnowledgeFaqModels.KnowledgeFaqItem> faqItems = knowledgeQuestionClusterService.listSuggestedFaqs(keyword);
        return AjaxResult.success(faqItems);
    }
}

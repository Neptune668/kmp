package com.atguigu.kmp.controller;

import com.atguigu.kmp.dto.KnowledgeChunk;
import com.atguigu.kmp.dto.KnowledgeChunkPageResponse;
import com.atguigu.kmp.entity.KnowledgeUnit;
import com.atguigu.kmp.service.KnowledgeUnitService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.constant.HttpStatus;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.PageDomain;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.core.page.TableSupport;
import com.ruoyi.common.enums.BusinessType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 知识单元表 前端控制器
 * </p>
 *
 * @author atguigu
 * @since 2026-10-20
 */
@RestController
@RequestMapping("/kmp/knowledgeUnit")
public class KnowledgeUnitController extends BaseController {

    @Autowired
    private KnowledgeUnitService knowledgeUnitService;

    /**
     * 分页查询知识单元。
     *
     * @param query 名称和所属组条件
     * @return 若依分页响应
     */
    @PreAuthorize("@ss.hasPermi('kmp:knowledgeUnit:list')")
    @GetMapping("/list")
    public TableDataInfo list(KnowledgeUnit query)
    {

        // 获取分页参数
        PageDomain pageDomain = TableSupport.buildPageRequest();
        // 获取当前页码
        int pageNum = pageDomain.getPageNum();
        // 获取每页大小
        int pageSize = pageDomain.getPageSize();

        // 创建分页对象
        Page<KnowledgeUnit> page = new Page<>(pageNum, pageSize);

        // 调用服务层方法进行分页查询
        IPage<KnowledgeUnit> result = knowledgeUnitService.pageKnowledgeUnits(page, query);

        // 创建响应对象
        TableDataInfo response = new TableDataInfo(result.getRecords(), result.getTotal());

        // 设置响应数据
        response.setCode(HttpStatus.SUCCESS);
        response.setMsg("查询成功");
        return response;
    }

    /**
     * 查询知识单元基础详情。
     *
     * @param id 知识单元 ID
     * @return 不含切片正文的详情
     */
    @PreAuthorize("@ss.hasPermi('kmp:knowledgeUnit:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return AjaxResult.success(knowledgeUnitService.getKnowledgeUnitDetail(id));
    }


    /**
     * 修改名称和所属知识组。
     *
     * @param knowledgeUnit 只使用 ID、名称和所属组字段
     * @return 修改结果
     */
    @PreAuthorize("@ss.hasPermi('kmp:knowledgeUnit:edit')")
    @Log(title = "知识单元管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated(KnowledgeUnit.Edit.class) @RequestBody KnowledgeUnit knowledgeUnit)
    {
        knowledgeUnitService.saveKnowledgeUnitBaseInfo(knowledgeUnit, this.getUserId());
        return AjaxResult.success("修改知识单元成功");
    }

    /**
     * 物理删除外部文档切片后逻辑删除知识单元平台元数据。
     *
     * @param id 知识单元 ID
     * @return 删除结果
     */
    @PreAuthorize("@ss.hasPermi('kmp:knowledgeUnit:remove')")
    @Log(title = "知识单元管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    public AjaxResult remove(@PathVariable Long id)
    {
        knowledgeUnitService.removeKnowledgeUnit(id);
        return AjaxResult.success("删除知识单元成功");
    }

    /**
     * 分页查询外部切片。
     *
     * @param id 知识单元 ID
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 切片和权威统计
     */
    @PreAuthorize("@ss.hasPermi('kmp:knowledgeUnit:query')")
    @GetMapping("/{id}/chunks")
    public AjaxResult listChunks(@PathVariable Long id,
                                 @RequestParam(defaultValue = "1") Long pageNum,
                                 @RequestParam(defaultValue = "10") Long pageSize)
    {
        KnowledgeChunkPageResponse result = knowledgeUnitService.pageChunks(id, pageNum, pageSize, this.getUserId());
        return AjaxResult.success(result);
    }

    /**
     * 新增单个外部切片。
     *
     * @param id 知识单元 ID
     * @param chunk 切片内容
     * @return 新切片及权威统计
     */
    @PreAuthorize("@ss.hasPermi('kmp:knowledgeUnit:add')")
    @Log(title = "知识单元切片", businessType = BusinessType.INSERT)
    @PostMapping("/{id}/chunks")
    public AjaxResult addChunk(@PathVariable Long id, @RequestBody KnowledgeChunk chunk)
    {
        return AjaxResult.success("新增切片成功", knowledgeUnitService.saveChunk(id, null, chunk, this.getUserId()));
    }

    /**
     * 完整替换单个外部切片的顺序和内容。
     *
     * @param id 知识单元 ID
     * @param chunkId 切片字符串 ID
     * @param chunk 切片内容
     * @return 修改后切片及权威统计
     */
    @PreAuthorize("@ss.hasPermi('kmp:knowledgeUnit:edit')")
    @Log(title = "知识单元切片", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}/chunks/{chunkId}")
    public AjaxResult editChunk(@PathVariable Long id, @PathVariable String chunkId, @RequestBody KnowledgeChunk chunk)
    {
        return AjaxResult.success("修改切片成功", knowledgeUnitService.saveChunk(id, chunkId, chunk, this.getUserId()));
    }

    /**
     * 删除单个外部切片。
     *
     * @param id 知识单元 ID
     * @param chunkId 切片字符串 ID
     * @return 删除后权威统计
     */
    @PreAuthorize("@ss.hasPermi('kmp:knowledgeUnit:remove')")
    @Log(title = "知识单元切片", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}/chunks/{chunkId}")
    public AjaxResult removeChunk(@PathVariable Long id, @PathVariable String chunkId)
    {
        return AjaxResult.success("删除切片成功", knowledgeUnitService.removeChunk(id, chunkId, this.getUserId()));
    }
}

package com.atguigu.kmp.controller;

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
}

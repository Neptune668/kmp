package com.atguigu.kmp.controller;

import com.atguigu.kmp.entity.KnowledgeUnit;
import com.atguigu.kmp.service.KnowledgeUnitService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.constant.HttpStatus;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.PageDomain;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.core.page.TableSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 知识单元表 前端控制器
 *
 * @author atguigu
 * @since 2026-10-21
 */
@RestController
@RequestMapping("/kmp/knowledgeUnit")
public class KnowledgeUnitController extends BaseController {
    @Autowired
    private KnowledgeUnitService knowledgeUnitService;

    /**
     * 获取知识单元列表
     * /kmp/knowledgeUnit/list
     * 根据查询条件分页查询知识单元信息，返回统一表格数据格式。
     *
     * @param query 知识单元查询条件
     * @return 分页查询结果（含知识单元记录列表与总条数）
     */
    @PreAuthorize("@ss.hasPermi('kmp:knowledgeUnit:list')")
    @GetMapping("/list")
    public TableDataInfo list(KnowledgeUnit query) {
        // 构建分页对象
        PageDomain pageDomain = TableSupport.buildPageRequest();
        Integer pageNum = pageDomain.getPageNum();
        Integer pageSize = pageDomain.getPageSize();
        Page<KnowledgeUnit> page = new Page<>(pageNum, pageSize);

        // 执行分页查询
        Page<KnowledgeUnit> result = knowledgeUnitService.pageKnowledgeUnits(page, query);

        // 封装统一响应结果
        TableDataInfo rspData = new TableDataInfo(result.getRecords(), result.getTotal());
        rspData.setCode(HttpStatus.SUCCESS);
        rspData.setMsg("查询成功");

        return rspData;
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
}

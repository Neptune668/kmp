package com.atguigu.kmp.controller;

import com.atguigu.kmp.entity.KnowledgeGroup;
import com.atguigu.kmp.service.KnowledgeGroupService;
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
 * 知识组表 前端控制器
 * </p>
 *
 * @author atguigu
 * @since 2026-10-21
 */
@RestController
@RequestMapping("/kmp/knowledgeGroup")
public class KnowledgeGroupController extends BaseController {
    @Autowired
    private KnowledgeGroupService knowledgeGroupService;

    /**
     * 知识组列表
     * /kmp/knowledgeGroup/list
     * 根据查询条件分页查询知识组信息，返回统一表格数据格式。
     *
     * @param query 知识组查询条件
     * @return 分页查询结果（含知识组记录列表与总条数）
     */
    @PreAuthorize("@ss.hasPermi('kmp:knowledgeGroup:list')")
    @GetMapping("/list")
    public TableDataInfo list(KnowledgeGroup query) {
        // 构建分页对象
        PageDomain pageDomain = TableSupport.buildPageRequest();
        Integer pageNum = pageDomain.getPageNum();
        Integer pageSize = pageDomain.getPageSize();
        Page<KnowledgeGroup> page = new Page<>(pageNum, pageSize);

        // 执行分页查询
        Page<KnowledgeGroup> result = knowledgeGroupService.pageKnowledgeGroups(page, query);

        // 封装统一响应结果
        TableDataInfo rspData = new TableDataInfo(result.getRecords(), result.getTotal());
        rspData.setCode(HttpStatus.SUCCESS);
        rspData.setMsg("查询成功");

        return rspData;
    }

    /**
     * 新增知识组
     * POST /kmp/knowledgeGroup
     * 保存一条知识组记录；即使客户端传入主键 id 也会被忽略，
     * 统一按新增处理，主键由数据库自增生成。
     *
     * @param knowledgeGroup 知识组信息（id 会被后端强制置空）
     * @return 操作结果，成功时附带新增记录的主键 ID
     */
    @PreAuthorize("@ss.hasPermi('kmp:knowledgeGroup:add')")
    @PostMapping
    @Log(title = "列表添加", businessType = BusinessType.INSERT)

    public AjaxResult add(@Validated @RequestBody KnowledgeGroup knowledgeGroup)
    {
        // 即使客户端传入主键，新增接口也只执行 INSERT。
        knowledgeGroup.setId(null);
        Long id = knowledgeGroupService.saveKnowledgeGroup(knowledgeGroup, getUserId());
        return AjaxResult.success("新增知识组成功", id);
    }

    
    
}

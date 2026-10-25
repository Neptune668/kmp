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
import com.ruoyi.common.exception.ServiceException;
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
 * @since 2026-10-20
 */
@RestController
@RequestMapping("/kmp/knowledgeGroup")
public class KnowledgeGroupController extends BaseController {

    @Autowired
    private KnowledgeGroupService knowledgeGroupService;

    /**
     * 知识组列表
     */
    @PreAuthorize("@ss.hasPermi('kmp:knowledgeGroup:list')")
    @GetMapping("/list")
    public TableDataInfo list(KnowledgeGroup query)
    {

        PageDomain pageDomain = TableSupport.buildPageRequest();
        Integer pageNum = pageDomain.getPageNum();
        Integer pageSize = pageDomain.getPageSize();

        Page<KnowledgeGroup> page = new Page<>(pageNum, pageSize);
        Page<KnowledgeGroup> result = knowledgeGroupService.pageKnowledgeGroups(page, query);

        TableDataInfo response = new TableDataInfo(result.getRecords(), result.getTotal());
        response.setCode(HttpStatus.SUCCESS);
        response.setMsg("查询结果成功");

        return response;
    }

//    /**
//     * 新增知识组。
//     *
//     * @param knowledgeGroup 新增数据
//     * @return 新增结果及数据库生成的 ID
//     */
//    @PreAuthorize("@ss.hasPermi('kmp:knowledgeGroup:add')")
//    @Log(title = "知识组管理", businessType = BusinessType.INSERT)
//    @PostMapping
//    public AjaxResult add(@Validated @RequestBody KnowledgeGroup knowledgeGroup)
//    {
//        // 即使客户端传入主键，新增接口也只执行 INSERT。
//        knowledgeGroup.setId(null);
//        Long id = knowledgeGroupService.saveKnowledgeGroup(knowledgeGroup, this.getUserId());
//        return AjaxResult.success("新增知识组成功", id);
//    }
//
//    /**
//     * 新增知识组。
//     *
//     * @param knowledgeGroup 新增数据
//     * @return 新增结果及数据库生成的 ID
//     */
//    @PreAuthorize("@ss.hasPermi('kmp:knowledgeGroup:add')")
//    @Log(title = "知识组管理", businessType = BusinessType.INSERT)
//    @PostMapping
//    public AjaxResult edit(@Validated @RequestBody KnowledgeGroup knowledgeGroup)
//    {
//        boolean result = knowledgeGroupService.updateKnowledgeGroup(knowledgeGroup, this.getUserId());
//        if(result){
//            return AjaxResult.success("修改知识组成功");
//        }else {
//            return AjaxResult.error("修改知识组失败");
//        }
//    }


    /**
     * 新增知识组。
     *
     * @param knowledgeGroup 新增数据
     * @return 新增结果及数据库生成的 ID
     */
    @PreAuthorize("@ss.hasPermi('kmp:knowledgeGroup:add')")
    @Log(title = "知识组管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody KnowledgeGroup knowledgeGroup)
    {
        // 即使客户端传入主键，新增接口也只执行 INSERT。
        knowledgeGroup.setId(null);
        Long id = knowledgeGroupService.saveKnowledgeGroup(knowledgeGroup, this.getUserId());
        return AjaxResult.success("新增知识组成功", id);
    }

    /**
     * 编辑知识组。
     *
     * @param knowledgeGroup 编辑数据
     * @return 修改结果
     */
    @PreAuthorize("@ss.hasPermi('kmp:knowledgeGroup:edit')")
    @Log(title = "知识组管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody KnowledgeGroup knowledgeGroup)
    {
        if (knowledgeGroup.getId() == null)
        {
            throw new ServiceException("知识组 ID 不正确");
        }
        knowledgeGroupService.saveKnowledgeGroup(knowledgeGroup, this.getUserId());

        return AjaxResult.success("修改知识组成功");
    }

    /**
     * 查询知识组详情。
     *
     * @param id 知识组 ID
     * @return 知识组详情
     */
    @PreAuthorize("@ss.hasPermi('kmp:knowledgeGroup:query')")
    @Log(title = "知识组管理", businessType = BusinessType.DETAIL)
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        // 调用服务层方法，获取知识组详情
        KnowledgeGroup knowledgeGroup = knowledgeGroupService.getKnowledgeGroupDetail(id);
        // 返回成功响应
        return AjaxResult.success(knowledgeGroup);
    }

    /**
     * 删除知识组。
     *
     * @param id 知识组 ID
     * @return 删除结果
     */
    @PreAuthorize("@ss.hasPermi('kmp:knowledgeGroup:remove')")
    @Log(title = "知识组管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    public AjaxResult remove(@PathVariable Long id)
    {
        knowledgeGroupService.removeKnowledgeGroup(id, this.getUserId());
        return AjaxResult.success("删除知识组成功");
    }

    /**
     * 查询未删除知识组，供其他位置的下拉列表选择。
     *
     * @return 知识组选项
     */
    @PreAuthorize("@ss.hasPermi('kmp:knowledgeGroup:list')") // 权限校验
    @GetMapping("/groupOptions")
    public AjaxResult groupOptions()
    {
        return AjaxResult.success(knowledgeGroupService.listKnowledgeGroupOptions());
    }
}

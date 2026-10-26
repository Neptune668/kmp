package com.atguigu.kmp.controller;

import com.atguigu.kmp.entity.KnowledgeImportTask;
import com.atguigu.kmp.service.KnowledgeImportTaskService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.constant.HttpStatus;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.page.PageDomain;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.core.page.TableSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 知识导入任务表 前端控制器
 * </p>
 *
 * @author atguigu
 * @since 2026-10-21
 */
@RestController
@RequestMapping("kmp/knowledgeImportTask")
public class KnowledgeImportTaskController extends BaseController {
    @Autowired
    private KnowledgeImportTaskService knowledgeImportTaskService;

    /**
     * 知识组列表
     */
    @PreAuthorize("@ss.hasPermi('kmp:KnowledgeImportTask:list')")
    @GetMapping("/list")
    public TableDataInfo list(KnowledgeImportTask query)
    {

        PageDomain pageDomain = TableSupport.buildPageRequest();
        Integer pageNum = pageDomain.getPageNum();
        Integer pageSize = pageDomain.getPageSize();

        Page<KnowledgeImportTask> page = new Page<>(pageNum, pageSize);
        Page<KnowledgeImportTask> result = knowledgeImportTaskService.pageKnowledgeImportTasks(page, query);

        TableDataInfo response = new TableDataInfo(result.getRecords(), result.getTotal());
        response.setCode(HttpStatus.SUCCESS);
        response.setMsg("查询结果成功");

        return response;
    }

}

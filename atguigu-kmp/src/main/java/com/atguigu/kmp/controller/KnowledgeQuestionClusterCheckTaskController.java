package com.atguigu.kmp.controller;

import com.atguigu.kmp.service.KnowledgeQuestionClusterCheckTaskService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * <p>
 * 问题簇检测任务表 前端控制器
 * </p>
 *
 * @author atguigu
 * @since 2026-10-20
 */
/**
 * 问题簇检测任务接口。
 */
@RestController
@RequestMapping("/kmp/questionCluster/checkTasks")
public class KnowledgeQuestionClusterCheckTaskController extends BaseController
{
    @Autowired
    private KnowledgeQuestionClusterCheckTaskService taskService;

    /** 创建截止当前时刻的异步手工检测任务。 */
    @PreAuthorize("@ss.hasPermi('kmp:questionCluster:check')")
    @Log(title = "问题簇检测", businessType = BusinessType.OTHER)
    @PostMapping
    public AjaxResult createManualTask()
    {
        Long taskId = taskService.createManualTask(getUserId());
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("taskId", taskId);
        return AjaxResult.success("问题簇检测任务已创建", result);
    }

}
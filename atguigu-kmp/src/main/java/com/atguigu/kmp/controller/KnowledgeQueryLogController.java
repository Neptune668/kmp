package com.atguigu.kmp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.atguigu.kmp.dto.KnowledgeExternalQueryLogModels.KnowledgeQueryLogCollectRequest;
import com.atguigu.kmp.dto.KnowledgeExternalQueryLogModels.KnowledgeQueryLogCollectResult;
import com.atguigu.kmp.service.KnowledgeQueryLogService;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.domain.AjaxResult;

/**
 * 外部知识库直接查询日志接收接口。
 */
@Anonymous
@RestController
@RequestMapping("/openapi/kmp/knowledgeQueryLog")
public class KnowledgeQueryLogController
{
    @Autowired
    private KnowledgeQueryLogService knowledgeQueryLogService;

    /**
     * 接收查询终态并写入 Redis Stream。
     *
     * @param request 查询终态日志
     * @return 异步接收结果
     */
    @PostMapping
    public AjaxResult collect(@Validated @RequestBody KnowledgeQueryLogCollectRequest request)
    {
        KnowledgeQueryLogCollectResult result = knowledgeQueryLogService.enqueueQueryLog(request);
        return AjaxResult.success("查询日志已接收", result);
    }
}

package com.atguigu.kmp.controller;

import com.atguigu.kmp.entity.KnowledgeQuerySession;
import com.atguigu.kmp.service.KnowledgeQuerySessionService;
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
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 知识查询会话索引表 前端控制器
 * </p>
 *
 * @author atguigu
 * @since 2026-10-20
 */
@RestController
@RequestMapping("/kmp/knowledgeQuerySession")
public class KnowledgeQuerySessionController extends BaseController {

    @Autowired
    private KnowledgeQuerySessionService knowledgeQuerySessionService;

    /**
     * 分页查询当前用户的知识查询会话。
     *
     * @param query 会话标题查询条件
     * @return 若依分页响应
     */
    @PreAuthorize("@ss.hasPermi('kmp:knowledgeQuery:query')")
    @GetMapping("/list")
    public TableDataInfo list(KnowledgeQuerySession query)
    {

        // 获取分页参数
        PageDomain pageDomain = TableSupport.buildPageRequest();
        // 获取当前页码
        int pageNum = pageDomain.getPageNum();
        // 获取每页大小
        int pageSize = pageDomain.getPageSize();

        // 创建分页对象
        Page<KnowledgeQuerySession> page = new Page<>(pageNum, pageSize);

        // 调用服务层方法进行分页查询
        IPage<KnowledgeQuerySession> result = knowledgeQuerySessionService.pageSessions(
                page, query.getSessionTitle(), this.getUserId()
        );

        // 创建响应对象
        TableDataInfo response = new TableDataInfo(result.getRecords(), result.getTotal());

        // 设置响应数据
        response.setCode(HttpStatus.SUCCESS);
        response.setMsg("查询成功");
        return response;
    }

    /**
     * 创建当前用户的知识查询会话。
     *
     * @param session 新会话标题
     * @return 创建结果及数据库生成的会话 ID
     */
    @PreAuthorize("@ss.hasPermi('kmp:knowledgeQuery:query')")
    @PostMapping
    public AjaxResult create(@Validated @RequestBody KnowledgeQuerySession session)
    {
        Long sessionId = knowledgeQuerySessionService.createSession(session, this.getUserId());
        return AjaxResult.success("创建会话成功", sessionId);
    }

    /**
     * 删除当前用户的指定知识查询会话。
     *
     * @param id 会话 ID
     * @return 删除结果
     */
    @PreAuthorize("@ss.hasPermi('kmp:knowledgeQuery:query')")
    @Log(title = "知识查询会话", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    public AjaxResult remove(@PathVariable @Positive(message = "会话 ID 不正确") Long id)
    {
        knowledgeQuerySessionService.removeSession(id, this.getUserId());
        return AjaxResult.success("删除会话成功");
    }

    /**
     * 清理当前用户的全部知识查询会话。
     *
     * @return 清理结果及实际删除数量
     */
    @PreAuthorize("@ss.hasPermi('kmp:knowledgeQuery:query')")
    @Log(title = "知识查询会话", businessType = BusinessType.DELETE)
    @DeleteMapping("/all")
    public AjaxResult clearAll()
    {
        boolean removed = knowledgeQuerySessionService.clearSessions(this.getUserId());
        return AjaxResult.success("清理历史会话成功", removed);
    }
}

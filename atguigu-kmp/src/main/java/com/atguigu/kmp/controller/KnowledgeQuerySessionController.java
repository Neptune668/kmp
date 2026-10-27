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
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 知识查询会话索引表 前端控制器
 * </p>
 *
 * @author atguigu
 * @since 2026-10-21
 */
@RestController
@RequestMapping("kmp/knowledgeQuerySession")
public class KnowledgeQuerySessionController extends BaseController {
    @Autowired
    private KnowledgeQuerySessionService knowledgeQuerySessionService;


    @PostMapping
    @PreAuthorize("@ss.hasPermi('kmp:knowledgeQuery:query')")
    public AjaxResult add(@RequestBody KnowledgeQuerySession knowledgeQuerySession) {
        // 即使客户端传入主键，新增接口也只执行 INSERT。
        knowledgeQuerySession.setId(null);
        Long id = knowledgeQuerySessionService.saveKnowledgeGroup(knowledgeQuerySession, this.getUserId());
        return AjaxResult.success("新增会话成功", id);
    }

    @PreAuthorize("@ss.hasPermi('kmp:knowledgeQuery:list')")
    @GetMapping("/list")
    public TableDataInfo list(KnowledgeQuerySession knowledgeQuerySession)
    {

        PageDomain pageDomain = TableSupport.buildPageRequest();
        Integer pageNum = pageDomain.getPageNum();
        Integer pageSize = pageDomain.getPageSize();

        Page<KnowledgeQuerySession> page = new Page<>(pageNum, pageSize);
        IPage<KnowledgeQuerySession> result = knowledgeQuerySessionService.pageSessions(page,knowledgeQuerySession.getSessionTitle(),this.getUserId());

        TableDataInfo response = new TableDataInfo(result.getRecords(), result.getTotal());
        response.setCode(HttpStatus.SUCCESS);
        response.setMsg("查询结果成功");

        return response;
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
}

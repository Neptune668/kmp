package com.atguigu.kmp.controller;

import com.atguigu.kmp.entity.KnowledgeImportTask;
import com.atguigu.kmp.entity.KnowledgeUnit;
import com.atguigu.kmp.service.KnowledgeImportTaskService;
import com.atguigu.kmp.service.KnowledgeUnitService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.constant.HttpStatus;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 知识导入任务表 前端控制器
 * </p>
 *
 * @author atguigu
 * @since 2026-10-20
 */
@RestController
@RequestMapping("/kmp/knowledgeImportTask")
public class KnowledgeImportTaskController extends BaseController {

    @Autowired
    private KnowledgeImportTaskService knowledgeImportTaskService;

    @Autowired
    private KnowledgeUnitService knowledgeUnitService;


    /**
     * 分页查询平台知识导入任务。
     *
     * @return 若依分页响应
     */
    @PreAuthorize("@ss.hasPermi('kmp:knowledgeImport:list')")
    @GetMapping("/list")
    public TableDataInfo list(@RequestParam Integer pageNum, @RequestParam Integer pageSize) {

        //获取分页参数
//        PageDomain pageDomain = TableSupport.buildPageRequest();
//        Integer pageNum = pageDomain.getPageNum();
//        Integer pageSize = pageDomain.getPageSize();

//        int pageNum = Integer.parseInt(request.getParameter("pageNum"));
//        int pageSize = Integer.parseInt(request.getParameter("pageSize"));

        Page<KnowledgeImportTask> page = new Page<>(pageNum, pageSize);
        Page<KnowledgeImportTask> result = knowledgeImportTaskService.pageImportTask(page);

        TableDataInfo response = new TableDataInfo(result.getRecords(), result.getTotal());
        response.setCode(HttpStatus.SUCCESS);
        response.setMsg("查询结果成功");
        return response;

    }

    /**
     * 在初始化平台任务前检查文件摘要是否已建档。
     *
     * @param fileSha256 64位小写 SHA-256
     * @return 重复标志及已有知识单元摘要信息
     */
    @PreAuthorize("@ss.hasPermi('kmp:knowledgeImport:import')")
    @GetMapping("/duplicate-check")
    public AjaxResult duplicateCheck(@RequestParam String fileSha256)
    {
        KnowledgeUnit duplicateUnit = knowledgeUnitService.findBySha256(fileSha256);

        Map<String, Object> result = new HashMap<>();
        result.put("duplicate", duplicateUnit != null);
        if(duplicateUnit != null){
            result.put("knowledgeUnitId", duplicateUnit.getId());
            result.put("knowledgeUnitName", duplicateUnit.getKnowledgeUnitName());
        }

        return AjaxResult.success(result);
    }

    /**
     * 在文件对外上传前初始化平台任务。
     *
     * @param task 文件名和知识组 ID
     * @return 平台任务 ID
     */
    @PreAuthorize("@ss.hasPermi('kmp:knowledgeImport:import')")
    @Log(title = "知识导入", businessType = BusinessType.IMPORT)
    @PostMapping("/init")
    public AjaxResult init(@RequestBody KnowledgeImportTask task)
    {
        Long taskId = knowledgeImportTaskService.initImportTask(task, this.getUserId());
        return AjaxResult.success("初始化知识导入任务成功", taskId);
    }

    /**
     * 将外部服务返回的任务编号绑定到原平台任务。
     *
     * @param id 平台任务 ID
     * @param task 外部任务编号
     * @return 绑定后的平台任务
     */
    @PreAuthorize("@ss.hasPermi('kmp:knowledgeImport:import')")
    @Log(title = "知识导入", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}/external-task")
    public AjaxResult bindExternalTask(@PathVariable Long id,
                                       @RequestBody KnowledgeImportTask task)
    {
        KnowledgeImportTask boundTask = knowledgeImportTaskService.bindExternalTask(
                id,
                task.getExternalTaskId(),
                this.getUserId()
        );
        return AjaxResult.success("绑定外部导入任务成功", boundTask);
    }

    /**
     * 逻辑移除未绑定待上传任务或导入失败任务。
     *
     * @param id 平台任务 ID
     * @return 移除结果
     */
    @PreAuthorize("@ss.hasPermi('kmp:knowledgeImport:remove')")
    @Log(title = "知识导入", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    public AjaxResult remove(@PathVariable Long id)
    {
        knowledgeImportTaskService.removeImportTask(id);
        return AjaxResult.success("移除知识导入任务成功");
    }

}

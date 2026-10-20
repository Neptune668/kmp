package com.atguigu.kmp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 知识导入任务表
 * </p>
 *
 * @author atguigu
 * @since 2026-10-20
 */
@Getter
@Setter
@ToString
@TableName("kmp_knowledge_import_task")
public class KnowledgeImportTask implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 平台导入任务主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 本次导入选择的知识组ID
     */
    private Long knowledgeGroupId;

    /**
     * 导入成功后创建的平台知识单元ID
     */
    private Long knowledgeUnitId;

    /**
     * 原始文件名
     */
    private String fileName;

    /**
     * 外部知识库任务编号
     */
    private String externalTaskId;

    /**
     * 任务状态：0-待上传，1-上传中，2-处理中，3-导入成功，4-导入失败
     */
    private Byte taskStatus;

    /**
     * 外部任务完整信息JSON
     */
    private String taskInfo;

    /**
     * 失败原因
     */
    private String failureReason;

    /**
     * 平台首次同步到终态的时间
     */
    private LocalDateTime finishTime;

    /**
     * 平台可观测耗时，单位毫秒
     */
    private Long durationMs;

    /**
     * 创建人用户ID
     */
    private Long createUserId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 最后修改人用户ID
     */
    private Long updateUserId;

    /**
     * 最后修改时间
     */
    private LocalDateTime updateTime;

    /**
     * 逻辑删除：0-未删除，1-已删除
     */
    @TableLogic
    private Integer isDeleted;
}

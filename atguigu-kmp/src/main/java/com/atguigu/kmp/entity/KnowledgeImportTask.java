package com.atguigu.kmp.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 知识导入任务实体。
 */
@Data
@TableName("kmp_knowledge_import_task")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class KnowledgeImportTask implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 平台导入任务主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    @Positive(message = "知识导入任务 ID 不正确")
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
    @Size(max = 128, message = "文件名不能超过128个字符")
    private String fileName;

    /**
     * 外部知识库任务编号
     */
    @Size(max = 128, message = "外部任务编号不能超过128个字符")
    private String externalTaskId;

    /**
     * 任务状态：0-待上传，1-上传中，2-处理中，3-导入成功，4-导入失败
     */
    @Min(value = 0, message = "任务状态不正确")
    @Max(value = 4, message = "任务状态不正确")
    private Integer taskStatus;

    /**
     * 外部任务完整信息JSON
     */
    @Size(max = 1000, message = "任务信息不能超过1000个字符")
    // 即使是NULL值，update时也要写进SQL语句中
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String taskInfo;

    /**
     * 失败原因
     */
    @Size(max = 1000, message = "失败原因不能超过1000个字符")
    // 即使是NULL值，update时也要写进SQL语句中
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String failureReason;

    /**
     * 平台首次同步到终态的时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 最后修改人用户ID
     */
    private Long updateUserId;

    /**
     * 最后修改时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 逻辑删除：0-未删除，1-已删除
     */
    @TableLogic
    private Integer isDeleted;

    /**
     * 创建人名称，仅用于列表展示，不映射数据库字段。
     */
    @TableField(exist = false)
    private String createUserName;
}

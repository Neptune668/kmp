package com.atguigu.kmp.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 知识单元实体。
 */
@Data
@TableName("kmp_knowledge_unit")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class KnowledgeUnit implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 平台知识单元主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    @Positive(message = "知识单元 ID 不正确", groups = Edit.class)
    private Long id;

    /**
     * 外部知识库文档ID
     */
    @NotBlank(message = "外部文档 ID 不能为空")
    @Size(max = 64, message = "外部文档 ID 不能超过64个字符")
    private String documentId;

    /**
     * 所属知识组ID，建档后只读。
     */
    @NotNull(message = "所属知识组不能为空", groups = Edit.class)
    @Positive(message = "所属知识组 ID 不正确", groups = Edit.class)
    private Long knowledgeGroupId;

    /**
     * 知识单元名称，允许重名。
     */
    @NotBlank(message = "知识单元名称不能为空", groups = Edit.class)
    @Size(max = 128, message = "知识单元名称不能超过128个字符", groups = Edit.class)
    private String knowledgeUnitName;

    /**
     * 原始文件名，建档后只读。
     */
    @Size(max = 128, message = "原始文件名不能超过128个字符")
    private String originalFileName;

    /**
     * 原始文件SHA-256小写摘要，建档后只读。
     */
    private String fileSha256;

    /**
     * MinIO外部可读完整下载URL
     */
    @Size(max = 500, message = "原文件下载地址不能超过500个字符")
    private String originalFileDownloadUrl;

    /**
     * 全部切片文本总字符数
     */
    @PositiveOrZero(message = "文本总长度不能为负数")
    private Long totalTextLength;

    /**
     * 有效切片数量
     */
    @PositiveOrZero(message = "切片数量不能为负数")
    private Long chunkCount;

    /**
     * 创建人用户ID，只允许后端赋值。
     */
    private Long createUserId;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 最后修改人用户ID，只允许后端赋值。
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

    /** 所属知识组名称，仅用于接口展示。 */
    @TableField(exist = false)
    private String knowledgeGroupName;

    /** 创建人名称，仅用于详情展示。 */
    @TableField(exist = false)
    private String createUserName;

    /** 最后修改人名称，仅用于详情展示。 */
    @TableField(exist = false)
    private String updateUserName;

    /** 基础信息编辑校验组，避免要求页面提交只读的 documentId。 */
    public interface Edit
    {
    }
}

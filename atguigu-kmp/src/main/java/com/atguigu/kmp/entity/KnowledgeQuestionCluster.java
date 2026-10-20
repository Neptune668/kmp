package com.atguigu.kmp.entity;

import com.atguigu.kmp.dto.KnowledgeRecentQuestion;
import com.atguigu.kmp.typehandler.KnowledgeRecentQuestionListTypeHandler;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


/**
 * 问题簇实体，对应问题管理的 MySQL 事实表。
 */
@Data
@TableName(value = "kmp_knowledge_question_cluster", autoResultMap = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class KnowledgeQuestionCluster implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 问题簇主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    @Positive(message = "问题簇 ID 不正确")
    private Long id;

    /**
     * 标准问题
     */
    @NotBlank(message = "标准问题不能为空")
    @Size(max = 200, message = "标准问题不能超过200个字符")
    private String standardQuestion;

    /**
     * 标准答案
     */
    @Size(max = 5000, message = "标准答案不能超过5000个字符")
    private String standardAnswer;

    /**
     * 知识单元ID
     */
    @NotNull(message = "知识单元不能为空")
    @Positive(message = "知识单元 ID 不正确")
    private Long knowledgeUnitId;

    /**
     * 知识组ID
     */
    @NotNull(message = "知识组不能为空")
    @Positive(message = "知识组 ID 不正确")
    private Long knowledgeGroupId;

    /**
     * 状态：0-沉淀中，1-待发布，2-已发布
     */
    @Min(value = 0, message = "问题簇状态不正确")
    @Max(value = 2, message = "问题簇状态不正确")
    private Integer status;

    /**
     * 关联次数
     */
    private Long associationCount;

    /**
     * FAQ点选次数
     */
    private Long faqClickCount;

    /**
     * 曝光次数
     */
    private Long exposureCount;

    /**
     * 有帮助次数
     */
    private Long helpfulCount;

    /**
     * 无帮助次数
     */
    private Long unhelpfulCount;

    /**
     * 拒答次数
     */
    private Long refusalCount;

    /**
     * 最近关联问题集合，按关联时间倒序仅保留最新十条
     */
    @TableField(typeHandler = KnowledgeRecentQuestionListTypeHandler.class)
    private List<KnowledgeRecentQuestion> recentQuestions;

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


    /** 知识单元名称，仅用于列表和详情展示。 */
    @TableField(exist = false)
    private String knowledgeUnitName;

    /** 知识组名称，仅用于列表和详情展示。 */
    @TableField(exist = false)
    private String knowledgeGroupName;

    /** 综合热度，仅用于后续 FAQ 展示，不落库。 */
    @TableField(exist = false)
    private BigDecimal hotScore;
}

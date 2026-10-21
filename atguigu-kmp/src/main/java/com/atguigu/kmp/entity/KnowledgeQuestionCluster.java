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
 * 问题簇表
 * </p>
 *
 * @author atguigu
 * @since 2026-10-21
 */
@Getter
@Setter
@ToString
@TableName("kmp_knowledge_question_cluster")
public class KnowledgeQuestionCluster implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 问题簇主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 标准问题
     */
    private String standardQuestion;

    /**
     * 标准答案
     */
    private String standardAnswer;

    /**
     * 知识单元ID
     */
    private Long knowledgeUnitId;

    /**
     * 知识组ID
     */
    private Long knowledgeGroupId;

    /**
     * 状态：0-沉淀中，1-待发布，2-已发布
     */
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
     * 最近关联问题ID集合，最多10个
     */
    private String recentQuestions;

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

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
 * 问题簇检测任务表
 * </p>
 *
 * @author atguigu
 * @since 2026-10-21
 */
@Getter
@Setter
@ToString
@TableName("kmp_knowledge_question_cluster_check_task")
public class KnowledgeQuestionClusterCheckTask implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 检测任务主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 触发类型：1-自动，2-手动，3-失败重试
     */
    private Integer triggerType;

    /**
     * 失败重试来源任务ID
     */
    private Long sourceTaskId;

    /**
     * 检测窗口开始时间，包含
     */
    private LocalDateTime rangeStartTime;

    /**
     * 检测窗口结束时间，不包含
     */
    private LocalDateTime rangeEndTime;

    /**
     * 任务状态：0-待执行，1-执行中，2-成功，3-部分失败，4-失败
     */
    private Integer taskStatus;

    /**
     * 问题总数
     */
    private Integer totalCount;

    /**
     * 成功处理数
     */
    private Integer successCount;

    /**
     * 失败数
     */
    private Integer failedCount;

    /**
     * 幂等或非典型问题跳过数
     */
    private Integer skippedCount;

    /**
     * 新建问题簇数
     */
    private Integer createdClusterCount;

    /**
     * 关联已有问题簇数
     */
    private Integer matchedClusterCount;

    /**
     * 实际开始时间
     */
    private LocalDateTime startTime;

    /**
     * 实际完成时间
     */
    private LocalDateTime finishTime;

    /**
     * 任务耗时毫秒
     */
    private Long durationMs;

    /**
     * 任务级失败摘要
     */
    private String failureReason;

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

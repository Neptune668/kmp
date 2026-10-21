package com.atguigu.kmp.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 直接知识查询日志实体，对应 kmp_knowledge_query_log。
 */
@Data
@TableName(value = "kmp_knowledge_query_log", autoResultMap = true)
public class KnowledgeQueryLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 查询日志主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 全局查询请求ID，应用级幂等键
     */
    private String requestId;

    /**
     * 当次查询使用的改写问题。
     */
    private String rewirttenQuery; //TODO  数据库表里没有这个字段

    /**
     * 发起查询的平台用户ID
     */
    private Long userId;

    /**
     * 请求开始时间，Asia/Shanghai
     */
    private LocalDateTime requestStartTime;

    /**
     * 首个响应Token产生时间
     */
    private LocalDateTime firstTokenTime;

    /**
     * 输入Token数，未知为NULL
     */
    private Long inputTokenCount;

    /**
     * 输出Token数，未知为NULL
     */
    private Long outputTokenCount;

    /** 外部知识库匹配到的知识单元名称。 */
//    @TableField(typeHandler = JacksonTypeHandler.class)
//    private List<String> itemNames;
    private String itemNames;

    /**
     * 查询终态：1-成功，2-失败
     */
    private Integer queryStatus;

    /**
     * 反馈效果：`0`-未反馈，`1`-有帮助，`2`-无帮助。
     */
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private Integer feedbackStatus;

    /**
     * `0`-正常回答，`1`-资料缺失拒答，`2`-权限拒答，`3`-澄清追问。
     */
    private Integer answerType;

    /**
     * 问题簇检测状态：0-待检测，1-已完成，2-已跳过。
     */
    private Integer clusterCheckStatus; //TODO  数据库表里没有这个字段

    /**
     * 检测后关联或创建的问题簇 ID。
     */
    private Long questionClusterId; //TODO  数据库表里没有这个字段

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

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

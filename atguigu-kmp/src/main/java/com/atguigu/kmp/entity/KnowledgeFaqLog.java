package com.atguigu.kmp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * FAQ成功访问日志表
 * </p>
 *
 * @author atguigu
 * @since 2026-10-14
 */
/**
 * FAQ 成功访问日志实体，对应 kmp_knowledge_faq_log。
 */
@Data
@TableName("kmp_knowledge_faq_log")
public class KnowledgeFaqLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * FAQ访问日志主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * FAQ点选请求ID，应用级幂等键
     */
    private String requestId;

    /**
     * 成功访问FAQ的用户ID
     */
    private Long userId;

    /**
     * 问题簇ID
     */
    private Long questionClusterId;

    /**
     * 知识单元ID快照
     */
    private Long knowledgeUnitId;

    /**
     * FAQ答案成功返回时间，Asia/Shanghai
     */
    private LocalDateTime visitTime;

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

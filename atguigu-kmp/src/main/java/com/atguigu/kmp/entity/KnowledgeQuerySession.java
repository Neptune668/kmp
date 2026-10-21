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
 * 知识查询会话索引表
 * </p>
 *
 * @author atguigu
 * @since 2026-10-21
 */
@Getter
@Setter
@ToString
@TableName("kmp_knowledge_query_session")
public class KnowledgeQuerySession implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 会话主键，同时作为外部知识库会话ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 会话标题
     */
    private String sessionTitle;

    /**
     * 会话所属用户ID
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

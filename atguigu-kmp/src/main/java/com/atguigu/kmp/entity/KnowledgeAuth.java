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
 * 知识权限授权关系表
 * </p>
 *
 * @author atguigu
 * @since 2026-10-20
 */
@Getter
@Setter
@ToString
@TableName("kmp_knowledge_auth")
public class KnowledgeAuth implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 知识权限授权关系主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 被授权知识组ID
     */
    private Long knowledgeGroupId;

    /**
     * 授权类型：1-全局，2-部门，3-角色，4-个人
     */
    private Byte authType;

    /**
     * 授权对象ID；全局授权为空
     */
    private Long authTargetId;

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

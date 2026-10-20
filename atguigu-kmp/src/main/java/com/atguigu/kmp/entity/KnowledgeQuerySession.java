package com.atguigu.kmp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 知识查询会话实体，对应真实数据表 kmp_knowledge_query_session。
 */
@Data
@TableName("kmp_knowledge_query_session")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class KnowledgeQuerySession implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 会话主键，同时作为外部知识库会话ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    @Positive(message = "会话 ID 不正确")
    private Long id;

    /**
     * 会话标题
     */
    @NotBlank(message = "会话标题不能为空")
    @Size(max = 255, message = "会话标题不能超过255个字符")
    private String sessionTitle;

    /**
     * 会话所属用户ID
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
}

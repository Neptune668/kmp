package com.atguigu.kmp.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;


/**
 * 知识组实体。
 */
@Data
@TableName("kmp_knowledge_group")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class KnowledgeGroup implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    @Positive(message = "知识组 ID 不正确")
    private Long id;

    /**
     * 知识组名称
     */
    @NotBlank(message = "知识组名称不能为空")
    @Size(max = 100, message = "知识组名称不能超过100个字符")
    private String knowledgeGroupName;

    /**
     * 知识组说明
     */
    @Size(max = 500, message = "知识组说明不能超过500个字符")
    private String knowledgeGroupDesc;

    /**
     * 是否默认：0-否，1-是
     */
    @NotNull(message = "是否默认不能为空")
    @Min(value = 0, message = "是否默认值不正确")
    @Max(value = 1, message = "是否默认值不正确")
    private Integer isDefault;

    /**
     * 创建人用户ID，只允许后端赋值。
     *
     */
    private Long createUserId;

    /**
     * 创建时间，只允许后端赋值。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 最后修改人用户ID，只允许后端赋值。
     *
     */
    private Long updateUserId;

    /**
     * 最后修改时间，只允许后端赋值。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 逻辑删除：0-未删除，1-已删除
     */
    @TableLogic
    private Integer isDeleted;

    /**
     * 创建人名称，仅用于接口展示。
     */
    @TableField(exist = false)
    private String createUserName;

    /**
     * 最后修改人名称，仅用于接口展示。
     */
    @TableField(exist = false)
    private String updateUserName;
}

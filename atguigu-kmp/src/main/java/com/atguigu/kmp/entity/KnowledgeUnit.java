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
 * 知识单元表
 * </p>
 *
 * @author atguigu
 * @since 2026-10-20
 */
@Getter
@Setter
@ToString
@TableName("kmp_knowledge_unit")
public class KnowledgeUnit implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 平台知识单元主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 外部知识库文档ID
     */
    private String documentId;

    /**
     * 所属知识组ID
     */
    private Long knowledgeGroupId;

    /**
     * 知识单元名称
     */
    private String knowledgeUnitName;

    /**
     * 原始文件名
     */
    private String originalFileName;

    /**
     * 原始文件SHA-256小写摘要
     */
    private String fileSha256;

    /**
     * MinIO外部可读完整下载URL
     */
    private String originalFileDownloadUrl;

    /**
     * 全部切片文本总字符数
     */
    private Long totalTextLength;

    /**
     * 有效切片数量
     */
    private Long chunkCount;

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

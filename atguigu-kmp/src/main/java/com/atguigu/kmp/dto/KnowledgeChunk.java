package com.atguigu.kmp.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 外部知识库切片契约。
 *
 * <p>该对象不映射平台数据表</p>
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class KnowledgeChunk implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 切片 ID。 */
    @JsonProperty("chunk_id")
    private String chunkId;

    /** 外部文档 ID，从知识单元读取。 */
    @JsonProperty("document_id")
    private String documentId;

    /**
     * 切片展示顺序。
     *
     * <p>@JsonProperty：定义该字段在 JSON 中的正式名称为下划线格式 chunk_order，
     * 同时作用于「序列化(输出)」和「反序列化(接收)」两个方向。
     * 之所以要指定，是因为后端对接的外部知识库使用下划线命名。</p>
     *
     * <p>@JsonAlias：仅在「反序列化(接收)」时额外接受的别名，这里允许前端用驼峰格式 chunkOrder 传值。
     * 注意：别名只影响入参，不影响输出——返回给前端时仍然是 chunk_order。</p>
     */
    @JsonProperty("chunk_order") // JSON 正式字段名（下划线），控制输入与输出两个方向
    @JsonAlias("chunkOrder")    // 接收时的兼容别名（驼峰），仅用于入参反序列化
    private Long chunkOrder;

    /** 切片标题。 */
    private String title;

    /** 切片父标题。 */
    @JsonProperty("parent_title") //输出时前端需要的是下划线格式
    @JsonAlias("parentTitle") //输出时前端需要的是驼峰格式
    private String parentTitle;

    /** 切片完整正文。 */
//    @JsonProperty("content")
    private String content;

    /** 外部服务计算的正文长度。 */
    @JsonProperty("content_length")
    private Long contentLength;

    /** 变更后的文档切片数。 */
    @JsonProperty("chunk_count")
    private Long chunkCount;

    /** 变更后的文档文本总长度。 */
    @JsonProperty("total_text_length")
    private Long totalTextLength;
}

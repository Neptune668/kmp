package com.atguigu.kmp.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class KnowledgeChunkPageResponse implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 外部文档 ID，从知识单元读取。 */
    @JsonProperty("document_id")
    private String documentId;

    /** 切片总数。 */
    @JsonProperty("total")
    private Long total;

    /** 切片总数。 */
    @JsonProperty("chunk_count")
    private Long chunkCount;

    /** 切片总文本长度。 */
    @JsonProperty("total_text_length")
    private Long totalTextLength;

    /** 当前页切片 */
    @JsonProperty("rows")
    private List<KnowledgeChunk> rows;
}
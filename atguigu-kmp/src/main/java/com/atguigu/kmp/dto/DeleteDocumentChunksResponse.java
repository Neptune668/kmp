package com.atguigu.kmp.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeleteDocumentChunksResponse {

    /** 外部文档 ID，从知识单元读取。 */
    @JsonProperty("document_id")
    private String documentId;

    /** 本次删除的切片数量。 */
    @JsonProperty("deleted_count")
    private Long deletedCount;
}

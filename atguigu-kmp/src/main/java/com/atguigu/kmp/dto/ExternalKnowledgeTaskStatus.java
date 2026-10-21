package com.atguigu.kmp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 外部知识库任务状态响应。
 *
 * <p>该对象只承载 Python 服务的接口契约，不映射平台数据库表。</p>
 */
@Data
public class ExternalKnowledgeTaskStatus implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 外部状态：processing、completed 或 failed。 */
    @JsonProperty("status")
    private String status;

    /** 已完成节点，保持外部服务返回顺序。 */
    @JsonProperty("done_list")
    private List<String> doneList = new ArrayList<>();

    /** 正在运行节点，保持外部服务返回顺序。 */
    @JsonProperty("running_list")
    private List<String> runningList = new ArrayList<>();

    /** 外部任务完成后用于平台建档。 */
    @JsonProperty("result")
    private Result result;

    /**
     * 外部导入完成态结果。
     */
    @Data
    public static class Result implements Serializable
    {
        private static final long serialVersionUID = 1L;

        /** 外部保存首批切片前生成的文档 ID。 */
        @JsonProperty("document_id")
        private String documentId;

        /** 外部知识库从文档内容识别出的知识单元名称。 */
        @JsonProperty("item_name")
        private String itemName;

        /** MinIO 对象路径，以斜杠开头且不包访问端点。 */
        @JsonProperty("original_file_download_url")
        private String originalFileDownloadUrl;

        /** 原始文件 SHA-256 小写摘要。 */
        @JsonProperty("file_sha256")
        private String fileSha256;

        /** 导入后有效切片数量。 */
        @JsonProperty("chunk_count")
        private Long chunkCount;

        /** 导入后全部切片文本总长度。 */
        @JsonProperty("total_text_length")
        private Long totalTextLength;
    }
}

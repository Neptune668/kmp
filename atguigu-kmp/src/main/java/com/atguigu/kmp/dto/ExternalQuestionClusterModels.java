package com.atguigu.kmp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * 外部问题簇服务契约模型，避免跨系统字段污染持久化实体。
 */
public final class ExternalQuestionClusterModels
{
    private ExternalQuestionClusterModels()
    {
    }

    /** 外部服务统一响应。 */
    @Data
    public static class ExternalResponse<T>
    {
        private Integer code;
        private String message;
        private T data;
    }

    /** 保存向量请求。 */
    @Data
    public static class VectorSaveRequest
    {
        @JsonProperty("requestId")
        private String requestId;

        @JsonProperty("standardQuestion")
        private String standardQuestion;
    }

    /** 保存向量响应数据。 */
    @Data
    public static class VectorSaveResult
    {
        private Long questionClusterId;
        private Boolean vectorSaved;
    }

    /** 删除向量响应数据。 */
    @Data
    public static class VectorDeleteResult
    {
        private Long questionClusterId;
        private Boolean deleted;
    }

    /** 参考答案请求。 */
    @Data
    public static class ReferenceAnswerRequest
    {
        private String requestId;
        private Long questionClusterId;
        private String standardQuestion;
        private String documentId;
    }

    /** 参考答案响应数据。 */
    @Data
    public static class ReferenceAnswerResult
    {
        private String referenceAnswer;
    }
}

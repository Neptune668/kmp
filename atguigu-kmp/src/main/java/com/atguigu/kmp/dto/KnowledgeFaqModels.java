package com.atguigu.kmp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 知识查询 FAQ 请求与响应对象集合。
 */
public final class KnowledgeFaqModels
{
    private KnowledgeFaqModels()
    {
    }

    /** FAQ 推荐和联想项，不携带答案及权限字段。 */
    @Data
    public static class KnowledgeFaqItem
    {
        private Long id;
        private String standardQuestion;
    }

    /** FAQ 点选请求；页面重试时必须复用同一请求 ID。 */
    @Data
    public static class KnowledgeFaqAnswerRequest
    {
        @NotBlank(message = "请求 ID 不能为空")
        @Size(max = 128, message = "请求 ID 不能超过128个字符")
        private String requestId;
    }

    /** 权限校验通过后返回的 FAQ 答案。 */
    @Data
    public static class KnowledgeFaqAnswer
    {
        private Long id;
        private String standardQuestion;
        private String standardAnswer;
    }
}
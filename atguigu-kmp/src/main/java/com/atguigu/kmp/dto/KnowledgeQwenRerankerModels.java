package com.atguigu.kmp.dto;

import com.atguigu.kmp.entity.KnowledgeQuestionCluster;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 百炼文本重排 REST 请求与响应契约。
 */
public final class KnowledgeQwenRerankerModels
{
    private KnowledgeQwenRerankerModels()
    {
    }

    @Data
    public static class Request
    {
        private String model;
        private Input input;
        private Parameters parameters;
    }

    @Data
    public static class Input
    {
        private String query;
        private List<String> documents;
    }

    @Data
    public static class Parameters
    {
        @JsonProperty("top_n")
        private Integer topN;
        private String instruct;
    }

    @Data
    public static class Response
    {
        private Output output;
        private String code;
        private String message;
        @JsonProperty("request_id")
        private String requestId;
    }

    @Data
    public static class Output
    {
        private List<Result> results;
    }

    @Data
    public static class Result
    {
        private Integer index;
        @JsonProperty("relevance_score")
        private BigDecimal relevanceScore;
    }

    @Data
    public static class RankedCandidate
    {
        private KnowledgeQuestionCluster candidate;
        private BigDecimal score;
    }
}

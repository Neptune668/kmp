package com.atguigu.kmp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * 百炼文本向量 REST 请求与响应契约。
 */
public final class KnowledgeQwenEmbeddingModels
{
    private KnowledgeQwenEmbeddingModels()
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
        private List<String> texts;
    }

    @Data
    public static class Parameters
    {
        private Integer dimension;
        @JsonProperty("output_type")
        private String outputType;
    }

    @Data
    public static class Response
    {
        private Output output;
        private String code;
        private String message;
    }

    @Data
    public static class Output
    {
        private List<Embedding> embeddings;
    }

    @Data
    public static class Embedding
    {
        private List<Float> embedding;
        @JsonProperty("text_index")
        private Integer textIndex;
    }
}

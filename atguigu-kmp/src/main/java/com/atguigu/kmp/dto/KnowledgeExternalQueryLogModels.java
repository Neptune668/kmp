package com.atguigu.kmp.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * 外部知识查询服务与平台之间的日志契约模型。
 *
 * <p>跨系统字段需要明确时区且不能暴露日志表审计字段，因此不直接复用持久化实体。</p>
 */
public final class KnowledgeExternalQueryLogModels
{
    private KnowledgeExternalQueryLogModels()
    {
    }

    /** 外部服务提交的直接查询终态日志。 */
    @Data
    public static class KnowledgeQueryLogCollectRequest
    {
        /** 全局请求 ID。 */
        @NotBlank(message = "请求 ID 不能为空")
        @Size(max = 128, message = "请求 ID 不能超过128个字符")
        private String requestId;

        /** 当次查询使用的改写问题。 */
        private String rewirttenQuery;

        /** 平台用户 ID，缺失时不计入查询人数。 */
        private Long userId;

        /** 查询开始时间，必须携带时区。 */
        @NotNull(message = "请求开始时间不能为空")
        private OffsetDateTime requestStartTime;

        /** 首个 Token 产生时间。 */
        private OffsetDateTime firstTokenTime;

        /** 输入 Token 数。 */
        @PositiveOrZero(message = "输入 Token 数不能为负数")
        private Long inputTokenCount;

        /** 输出 Token 数。 */
        @PositiveOrZero(message = "输出 Token 数不能为负数")
        private Long outputTokenCount;

        /** 外部知识库匹配到的知识单元名称，用于后续问题簇匹配。 */
        @Size(max = 100, message = "匹配知识单元名称不能超过100个")
        private List<String> itemNames;

        /** 查询终态：1-成功，2-失败。 */
        @NotNull(message = "查询终态不能为空")
        private Integer queryStatus;

        /** 回答类型：0-正常回答，1-资料缺失拒答，2-权限拒答，3-澄清追问。 */
        @NotNull(message = "回答类型不能为空")
        @Min(value = 0, message = "回答类型不正确")
        @Max(value = 3, message = "回答类型不正确")
        private Integer answerType;
    }

    /** 日志进入 Redis Stream 后返回的接收结果。 */
    @Data
    public static class KnowledgeQueryLogCollectResult
    {
        /** 原请求 ID。 */
        private String requestId;

        /** 是否已进入异步缓冲。 */
        private boolean accepted;

        /** Redis Stream 记录 ID。 */
        private String streamRecordId;
    }
}

package com.atguigu.kmp.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 直接知识查询反馈接口模型。
 *
 * <p>反馈接口只接收可修改的反馈状态，避免暴露查询日志中的其他持久化字段。</p>
 */
public final class KnowledgeQueryFeedbackModels
{
    private KnowledgeQueryFeedbackModels()
    {
    }

    /** 用户提交的反馈。 */
    @Data
    public static class KnowledgeQueryFeedbackRequest
    {
        /** 反馈状态：1-有帮助，2-无帮助。 */
        @NotNull(message = "反馈状态不能为空")
        @Min(value = 1, message = "反馈状态不正确")
        @Max(value = 2, message = "反馈状态不正确")
        private Integer feedbackStatus;
    }

    /** 最终保存的反馈结果。 */
    @Data
    public static class KnowledgeQueryFeedbackResult
    {
        /** 查询请求 ID。 */
        private String requestId;

        /** 最终反馈状态。 */
        private Integer feedbackStatus;
    }
}

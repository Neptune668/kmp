package com.atguigu.kmp.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 问题簇详情页的最近关联问题展示对象。
 *
 * <p>匹配成功时从查询日志生成快照，并持久化到问题簇的 JSON 字段。</p>
 */
@Data
public class KnowledgeRecentQuestion implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 查询日志 ID 字符串，用于来源追溯。 */
    private String questionId;

    /** 匹配时使用的改写后问题快照。 */
    private String rewrittenQuestion;

    /** 用户提问时间快照。 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime questionTime;
}
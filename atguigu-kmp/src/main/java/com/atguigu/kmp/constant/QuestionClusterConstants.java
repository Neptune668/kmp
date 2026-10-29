package com.atguigu.kmp.constant;

/**
 * 问题簇状态及字段长度常量。
 */
public final class QuestionClusterConstants
{
    /** 沉淀中。 */
    public static final int STATUS_ACCUMULATING = 0;

    /** 待发布。 */
    public static final int STATUS_PENDING = 1;

    /** 已发布。 */
    public static final int STATUS_PUBLISHED = 2;

    /** 标准问题最大长度。 */
    public static final int MAX_QUESTION_LENGTH = 200;

    /** 标准答案最大长度。 */
    public static final int MAX_ANSWER_LENGTH = 5000;

    /** 推荐 FAQ 数量限制。 */
    public static final int FAQ_RECOMMENDATION_LIMIT = 5;

    /** 联想 FAQ 最小长度限制。 */
    public static final int FAQ_SUGGESTION_MIN_LENGTH = 1;

    /** 联想 FAQ 数量限制。 */
    public static final int FAQ_SUGGESTION_LIMIT = 5;

    /** 联想 FAQ 最小相似度。 */
    public static final double FAQ_SUGGESTION_MIN_SCORE = 0.1;
}

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
}

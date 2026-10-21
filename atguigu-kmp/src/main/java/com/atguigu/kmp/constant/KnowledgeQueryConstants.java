package com.atguigu.kmp.constant;

/**
 * 直接知识查询回答类型与反馈状态常量。
 */
public final class KnowledgeQueryConstants
{
    /** 正常回答。 */
    public static final int ANSWER_TYPE_NORMAL = 0;

    /** 资料缺失拒答。 */
    public static final int ANSWER_TYPE_MISSING = 1;

    /** 权限拒答。 */
    public static final int ANSWER_TYPE_FORBIDDEN = 2;

    /** 澄清追问。 */
    public static final int ANSWER_TYPE_CLARIFY = 3;

    /** 尚未反馈。 */
    public static final int FEEDBACK_NONE = 0;

    /** 回答有帮助。 */
    public static final int FEEDBACK_HELPFUL = 1;

    /** 回答无帮助。 */
    public static final int FEEDBACK_UNHELPFUL = 2;

}

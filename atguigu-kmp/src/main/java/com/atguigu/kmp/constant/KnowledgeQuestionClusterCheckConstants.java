package com.atguigu.kmp.constant;

/**
 * 问题簇检测任务、查询日志状态和匹配动作常量。
 */
public final class KnowledgeQuestionClusterCheckConstants
{
    public static final int TRIGGER_AUTO = 1;
    public static final int TRIGGER_MANUAL = 2;
    public static final int TASK_PENDING = 0;
    public static final int TASK_RUNNING = 1;
    public static final int TASK_COMPLETED = 2;
    public static final int TASK_FAILED = 4;
    public static final int CHECK_PENDING = 0;
    public static final int CHECK_COMPLETED = 1;
    public static final int CHECK_SKIPPED = 2;
    public static final int ACTION_MATCHED = 1;
    public static final int ACTION_CREATED = 2;
    public static final int ACTION_SKIPPED = 3;

}

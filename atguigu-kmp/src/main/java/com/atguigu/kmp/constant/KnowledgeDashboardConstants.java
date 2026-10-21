package com.atguigu.kmp.constant;

/**
 * 知识看板业务常量。
 */
public final class KnowledgeDashboardConstants
{
    /** 直接查询成功终态。 */
    public static final int QUERY_STATUS_SUCCESS = 1;

    /** 直接查询失败终态。 */
    public static final int QUERY_STATUS_FAILED = 2;

    /** 外部请求 ID 最大长度。 */
    public static final int MAX_REQUEST_ID_LENGTH = 128;

    /** 单次最多保存的匹配知识单元名称数量。 */
    public static final int MAX_MATCHED_ITEM_NAME_COUNT = 100;

    /** 匹配知识单元名称最大长度。 */
    public static final int MAX_KNOWLEDGE_UNIT_NAME_LENGTH = 128;

    /** 看板业务时区。 */
    public static final String BUSINESS_ZONE_ID = "Asia/Shanghai";

    /** 直接查询日志类型。 */
    public static final String LOG_TYPE_QUERY = "query";

    /** FAQ 访问日志类型。 */
    public static final String LOG_TYPE_FAQ = "faq";

    /** 直接查询日志死信 Stream。 */
    public static final String QUERY_DEAD_STREAM_KEY = "kmp:knowledge-query-log:dead";

    /** FAQ 访问日志死信 Stream。 */
    public static final String FAQ_DEAD_STREAM_KEY = "kmp:faq-visit-log:dead";

    /** 完成标记前缀。 */
    public static final String DONE_KEY_PREFIX = "kmp:dashboard-log:done:";

    /** 处理锁前缀。 */
    public static final String LOCK_KEY_PREFIX = "kmp:dashboard-log:lock:";

}

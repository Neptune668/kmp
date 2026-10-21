package com.atguigu.kmp.constant;

/**
 * 知识导入业务常量。
 */
public final class KnowledgeImportConstants
{
    /** 平台任务已初始化，文件尚未被外部服务受理。 */
    public static final Integer TASK_STATUS_PENDING = 0;

    /** 上传中，当前仅由前端临时队列使用。 */
    public static final Integer TASK_STATUS_UPLOADING = 1;

    /** 外部服务已受理并正在处理。 */
    public static final Integer TASK_STATUS_PROCESSING = 2;

    /** 导入成功终态。 */
    public static final Integer TASK_STATUS_SUCCESS = 3;

    /** 导入失败终态。 */
    public static final Integer TASK_STATUS_FAILED = 4;

    /** 外部任务编号最大长度。 */
    public static final int MAX_EXTERNAL_TASK_ID_LENGTH = 128;

}

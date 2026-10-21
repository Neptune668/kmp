package com.atguigu.kmp.constant;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * 知识单元业务常量。
 */
public final class KnowledgeUnitConstants
{
    /** SHA-256 小写十六进制格式。 */
    public static final Pattern SHA256_PATTERN = Pattern.compile("^[0-9a-f]{64}$");

    /** 外部切片 ID 格式。 */
    public static final Pattern CHUNK_ID_PATTERN = Pattern.compile("^[0-9]+$");

    /** 外部写接口幂等键格式。 */
    public static final Pattern REQUEST_ID_PATTERN = Pattern.compile(
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$");

    /** 允许的切片分页大小。 */
    public static final Set<Long> CHUNK_PAGE_SIZES = Set.of(10L, 20L, 50L, 100L);

    /** 切片标题最大长度。 */
    public static final int MAX_CHUNK_TITLE_LENGTH = 500;

    /** 切片正文最大长度。 */
    public static final int MAX_CHUNK_CONTENT_LENGTH = 65535;

    /** 知识单元名称最大长度。 */
    public static final int MAX_KNOWLEDGE_UNIT_NAME_LENGTH = 128;

}

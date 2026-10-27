package com.atguigu.kmp.typehandler;

import com.atguigu.kmp.dto.KnowledgeRecentQuestion;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.lang.reflect.Field;
import java.util.List;

/**
 * 最近关联问题 JSON 列表类型处理器。
 */
public class KnowledgeRecentQuestionListTypeHandler extends JacksonTypeHandler
{
    private static final TypeReference<List<KnowledgeRecentQuestion>> TYPE_REFERENCE = new TypeReference<>() { };

    private static final ObjectMapper OBJECT_MAPPER = getObjectMapper().copy().registerModule(new JavaTimeModule());

    /** MyBatis XML ResultMap 使用的构造方法。 */
    public KnowledgeRecentQuestionListTypeHandler(Class<?> type)
    {

        super(type);
    }

    /** MyBatis-Plus 实体字段映射使用的构造方法。 */
    public KnowledgeRecentQuestionListTypeHandler(Class<?> type, Field field)
    {

        super(type, field);
    }

    /** 将 JSON 固定反序列化为最近关联问题对象列表。 */
    @Override
    public Object parse(String json)
    {
        try
        {
            return OBJECT_MAPPER.readValue(json, TYPE_REFERENCE);
        }
        catch (JsonProcessingException exception)
        {
            throw new IllegalArgumentException("最近关联问题快照解析失败", exception);
        }
    }

    /** 将最近关联问题对象列表序列化为 JSON。 */
    @Override
    public String toJson(Object value)
    {
        try
        {
            return OBJECT_MAPPER.writeValueAsString(value);
        }
        catch (JsonProcessingException exception)
        {
            throw new IllegalArgumentException("最近关联问题快照序列化失败", exception);
        }
    }
}
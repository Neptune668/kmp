package com.atguigu.kmp.service.impl;

import com.atguigu.kmp.config.KmpConfig;
import com.atguigu.kmp.constant.KnowledgeDashboardConstants;
import com.atguigu.kmp.constant.KnowledgeQuestionClusterCheckConstants;
import com.atguigu.kmp.dto.KnowledgeExternalQueryLogModels.KnowledgeQueryLogCollectRequest;
import com.atguigu.kmp.dto.KnowledgeExternalQueryLogModels.KnowledgeQueryLogCollectResult;
import com.atguigu.kmp.entity.KnowledgeQueryLog;
import com.atguigu.kmp.mapper.KnowledgeQueryLogMapper;
import com.atguigu.kmp.service.KnowledgeQueryLogService;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 直接知识查询日志表 服务实现类
 * </p>
 *
 * @author atguigu
 * @since 2026-10-20
 */
@Slf4j
@Service
public class KnowledgeQueryLogServiceImpl extends ServiceImpl<KnowledgeQueryLogMapper, KnowledgeQueryLog> implements KnowledgeQueryLogService {

    private static final ZoneId BUSINESS_ZONE = ZoneId.of(KnowledgeDashboardConstants.BUSINESS_ZONE_ID);

    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private KmpConfig kmpConfig;

    /** {@inheritDoc} */
    @Override
    public KnowledgeQueryLogCollectResult enqueueQueryLog(KnowledgeQueryLogCollectRequest request)
    {
        String requestId = request.getRequestId().trim();
        List<String> itemNameList = request.getItemNames();

        Map<Object, Object> message = new LinkedHashMap<>();
        message.put("requestId", requestId);
        message.put("rewirttenQuery", nullableString(request.getRewirttenQuery()));
        message.put("userId", nullableString(request.getUserId()));
        message.put("requestStartTime", request.getRequestStartTime().atZoneSameInstant(BUSINESS_ZONE).toLocalDateTime().toString());
        message.put("firstTokenTime", request.getFirstTokenTime() == null ? "" : request.getFirstTokenTime().atZoneSameInstant(BUSINESS_ZONE).toLocalDateTime().toString());
        message.put("inputTokenCount", nullableString(request.getInputTokenCount()));
        message.put("outputTokenCount", nullableString(request.getOutputTokenCount()));
        message.put("itemNames", writeItemNames(itemNameList));
        message.put("queryStatus", request.getQueryStatus().toString());
        message.put("answerType", request.getAnswerType().toString());


        MapRecord<Object, Object, Object> record = StreamRecords.mapBacked(message)
                .withStreamKey(kmpConfig.getDashboardQueryLogStreamKey());
        RecordId recordId = redisTemplate.opsForStream().add(record);
        redisTemplate.opsForStream().trim(kmpConfig.getDashboardQueryLogStreamKey(),
                kmpConfig.getDashboardStreamMaxLength(), true);

        KnowledgeQueryLogCollectResult result = new KnowledgeQueryLogCollectResult();
        result.setRequestId(requestId);
        result.setAccepted(true);
        result.setStreamRecordId(recordId.getValue());
        return result;

    }

    @Override
    public List<KnowledgeQueryLog> findPendingQuestions(LocalDateTime rangeEndTime)
    {
        log.info("开始读取待检测问题，rangeEnd={}", rangeEndTime);
        List<KnowledgeQueryLog> questions = lambdaQuery()
                .lt(KnowledgeQueryLog::getRequestStartTime, rangeEndTime)
                .eq(KnowledgeQueryLog::getClusterCheckStatus, KnowledgeQuestionClusterCheckConstants.CHECK_PENDING)
                .apply("CHAR_LENGTH(TRIM(rewirtten_query)) > 0")
                .apply("JSON_LENGTH(item_names) > 0")
                .orderByAsc(KnowledgeQueryLog::getRequestStartTime, KnowledgeQueryLog::getId)
                .list();
        log.info("待检测问题读取完成，count={}", questions.size());
        return questions;
    }

    /** 将知识单元名称序列化为 Stream 可保存的字符串。 */
    private String writeItemNames(List<String> itemNameList)
    {
        try
        {
            return objectMapper.writeValueAsString(itemNameList);
        }
        catch (JsonProcessingException exception)
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "匹配知识单元名称格式不正确");
        }
    }
    /** 将可空数字转换为 Stream 可保存的字符串。 */
    private String nullableString(Object value)
    {
        return value == null ? "" : value.toString();
    }
}

package com.atguigu.kmp.client;

import com.atguigu.kmp.config.KmpConfig;
import com.atguigu.kmp.entity.KnowledgeQuestionCluster;
import com.ruoyi.common.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * FAQ Elasticsearch 文档客户端。
 */
@Slf4j
@Component
public class FaqSearchClient
{
    @Autowired
    @Qualifier("faqSearchRestClient")
    private RestClient restClient;

    @Autowired
    private KmpConfig kmpConfig;
    /**
     * 创建或覆盖 FAQ 联想文档。
     *
     * @param cluster 已校验的问题簇
     */
    public void saveFaq(KnowledgeQuestionCluster cluster)
    {
        Map<String, Object> document = new HashMap<>();
        document.put("question_cluster_id", cluster.getId());
        document.put("standard_question", cluster.getStandardQuestion());

        ResponseEntity<Void> response = restClient.put()
                .uri(
                        "/{indexName}/_doc/{questionClusterId}",
                        kmpConfig.getFaqSearchIndexName(),
                        cluster.getId()
                )
                .contentType(MediaType.APPLICATION_JSON)
                .body(document)
                .retrieve()
                .toBodilessEntity();

        int statusCode = response.getStatusCode().value();
        if (statusCode == 201)
        {
            log.debug("FAQ 文档新建成功，questionClusterId={}", cluster.getId());
        }
        else if (statusCode == 200)
        {
            log.debug("FAQ 文档覆盖更新成功，questionClusterId={}", cluster.getId());
        }
        else
        {
            log.warn("FAQ 文档写入返回非预期状态码，questionClusterId={}，status={}", cluster.getId(), statusCode);
            throw new ServiceException("FAQ 搜索服务暂时不可用");
        }
    }

    /**
     * 幂等删除 FAQ 联想文档，404 表示目标已不存在。
     *
     * @param questionClusterId 问题簇 ID
     */
    public void deleteFaq(Long questionClusterId)
    {
        ResponseEntity<Void> response = restClient
                .delete()
                .uri(
                        "/{indexName}/_doc/{questionClusterId}",
                        kmpConfig.getFaqSearchIndexName(),
                        questionClusterId
                )
                .retrieve()
                .onStatus(status -> status.value() == 404, (request, res) -> { /* 幂等场景不抛异常 */ })
                .toBodilessEntity();

        int statusCode = response.getStatusCode().value();
        if (statusCode == 404)
        {
            // ES 删除不存在文档时返回404，按幂等成功处理。
            log.debug("FAQ 搜索文档已不存在，questionClusterId={}", questionClusterId);
            return;
        }
        if (statusCode == 200 || statusCode == 204)
        {
            log.debug("FAQ 搜索文档删除成功，questionClusterId={}，status={}", questionClusterId, statusCode);
            return;
        }
        // 其他非预期状态码，记录并抛出异常。
        log.warn("FAQ 搜索文档删除返回非预期状态码，questionClusterId={}，status={}", questionClusterId, statusCode);
        throw new ServiceException("FAQ 搜索服务暂时不可用");
    }

    /**
     * 按标准问题搜索 FAQ 问题簇 ID，保留 Elasticsearch 相关度顺序。
     *
     * @param keyword 搜索关键词
     * @param limit 最大返回数
     * @param minScore 最低相关度
     * @return 按 ES 顺序去重的问题簇 ID
     */
    public List<Long> searchFaqIds(String keyword, int limit, double minScore)
    {
        Map<String, Object> matchOptions = new HashMap<>();
        matchOptions.put("query", keyword);
        Map<String, Object> match = new HashMap<>();
        match.put("standard_question", matchOptions);
        Map<String, Object> query = new HashMap<>();
        query.put("match", match);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("size", limit);
        requestBody.put("_source", List.of("question_cluster_id"));
        requestBody.put("min_score", minScore);
        requestBody.put("query", query);

        Map response = restClient.post()
                .uri("/{indexName}/_search", kmpConfig.getFaqSearchIndexName())
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(Map.class);
        Map<String, Object> hitsContainer = (Map<String, Object>) response.get("hits");
        List<Map<String, Object>> hits = (List<Map<String, Object>>) hitsContainer.get("hits");
        List<Long> questionClusterIds = hits.stream()
                .map(hit -> (Number) ((Map<String, Object>) hit.get("_source")).get("question_cluster_id"))
                .map(Number::longValue)
                .distinct()
                .toList();
        return questionClusterIds;
    }
}
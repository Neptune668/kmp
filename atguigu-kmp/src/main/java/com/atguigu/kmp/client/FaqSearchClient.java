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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class FaqSearchClient {

    @Autowired
    @Qualifier("faqSearchRestClient")
    private RestClient restClient;

    @Autowired
    private KmpConfig kmpConfig;

    public void saveFaq(KnowledgeQuestionCluster cluster) {

        Long clusterId = cluster.getId();
        String standardQuestion = cluster.getStandardQuestion();
        Map<String, Object> document = new HashMap<>();
        document.put("question_cluster_id", clusterId);
        document.put("standard_question", standardQuestion);

        ResponseEntity<Void> response = restClient
                .put()
                .uri(
                        "/{indexName}/_doc/{clusterId}",
                        kmpConfig.getFaqSearchIndexName(),
                        clusterId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(document)
                .retrieve()
//                .body()
                .toBodilessEntity();

        int statusCode = response.getStatusCode().value();
        if (statusCode == 201)
        {
            log.info("FAQ 文档创建成功");
        }
        else if(statusCode == 200)
        {
            log.info("FAQ 文档更新成功");
        }
        else
        {
            log.error("FAQ 文档创建或更新失败，状态码：{}", statusCode);
            throw new ServiceException("FAQ 文档创建或更新失败");
        }
    }

    public void deleteFaq(Long clusterId) {
        ResponseEntity<Void> response = restClient
                .delete()
                .uri(
                        "/{indexName}/_doc/{clusterId}",
                        kmpConfig.getFaqSearchIndexName(),
                        clusterId)
                .retrieve()
                // 在es中如果文档不存在，则返回状态码是404，es中对于404状态码会抛出异常，
                // 此处的处理是让404结果不抛出异常
                .onStatus(status -> status.value() == 404, (request, res) -> {})
//                .body(类型.class) // 有响应体：响应成功的响应码是 200
                .toBodilessEntity(); // 无响应体：响应成功的响应码是 200 或 204


        int statusCode = response.getStatusCode().value();
        // 幂等判断，是否重复删除，如果重复删除，直接返回，不报错
        if (statusCode == 404)
        {
            log.error("FAQ 文档已不存在");
            return;
        }

        if (statusCode == 200 || statusCode == 204)
        {
            log.info("FAQ 文档删除成功，状态码：{}", statusCode);
            return;
        }

        // 其他错误
        log.error("FAQ 文档删除失败，状态码：{}", statusCode);
        throw new ServiceException("FAQ 文档删除失败");

    }

    public List<Long> searchFaqIds(String keyword, int limit, double minScore){

        // 组织es的请求对象
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

        Map response = restClient
                .post()
                .uri("/{indexName}/_search", kmpConfig.getFaqSearchIndexName())
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(Map.class);

        Map<String, Object> hitsContainer = (Map<String, Object>) response.get("hits");
        List<Map<String, Object>> hits = (List<Map<String, Object>>) hitsContainer.get("hits");

        List<Long> questionClusterIds = new ArrayList<>();
        for (Map<String, Object> hit : hits) {
            Map<String, Object> source = (Map<String, Object>) hit.get("_source");
            Number id = (Number) source.get("question_cluster_id");
            if (id == null) {
                continue;
            }
            long clusterId = id.longValue();
            if (!questionClusterIds.contains(clusterId)) {
                questionClusterIds.add(clusterId);
            }
        }

        return questionClusterIds;
//
//        Map<String, Object> hitsContainer = (Map<String, Object>) response.get("hits");
//        List<Map<String, Object>> hits = (List<Map<String, Object>>)hitsContainer.get("hits");
//        List<Long> questionClusterIds = hits
//                .stream()
//                .map(hit -> (Number)((Map<String, Object>) hit.get("_source")).get("question_cluster_id"))
//                .map(Number::longValue).distinct().toList();

//        return null;

    }

}

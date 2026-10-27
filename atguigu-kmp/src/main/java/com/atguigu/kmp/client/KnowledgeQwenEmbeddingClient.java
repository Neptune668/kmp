package com.atguigu.kmp.client;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.kmp.dto.KnowledgeQwenEmbeddingModels;
import com.ruoyi.common.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class KnowledgeQwenEmbeddingClient {

    @Value("${kmp.milvus.embedding-dimension:1024}")
    private int embeddingDimension;

    @Value("${kmp.qwen.base-url}")
    private String qwenBaseUrl;

    @Value("${kmp.qwen.api-key}")
    private String qwenApiKey;

    @Value("${kmp.qwen.embedding-path}")
    private String qwenEmbeddingPath;

    @Value("${kmp.qwen.embedding-model:qwen3.7-text-embedding-flash}")
    private String qwenEmbeddingModel;


    @Autowired
    @Qualifier("qwenRestClient")
    private RestClient restClient;



    /**
     * 为单段文本生成固定维度稠密向量。
     *
     * @param text 待向量化文本
     * @return 浮点向量
     */
    public float[] embed(String text)
    {
        JSONObject response = restClient
                .post()
                .uri(qwenBaseUrl + qwenEmbeddingPath)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + qwenApiKey)
                .body(buildEmbeddingRequest(text))
                .retrieve()
                .body(JSONObject.class);

        // 解析响应：output.embeddings[0].embedding 即为稠密向量
//        JSONObject output = response == null ? null : response.getJSONObject("output");
//        JSONArray embeddings = output == null ? null : output.getJSONArray("embeddings");
//        if (embeddings == null || embeddings.isEmpty())
//        {
//            String code = response == null ? null : response.getString("code");
//            String message = response == null ? null : response.getString("message");
//            log.error("调用通义千问向量模型失败，code={}, message={}", code, message);
//            throw new ServiceException("文本向量化失败");
//        }

        JSONObject output =  response.getJSONObject("output");
        JSONArray embeddings = output.getJSONArray("embeddings");
        JSONArray embedding = embeddings.getJSONObject(0).getJSONArray("embedding");
        float[] vector = new float[embedding.size()];
        for (int i = 0; i < embedding.size(); i++)
        {
            vector[i] = embedding.getFloatValue(i);
        }
        return vector;
    }

    /**
     * 组装百炼 embedding 请求体
     *
     * @param text 待向量化文本
     * @return 待序列化的请求参数
     */
    private Map<String, Object> buildEmbeddingRequest(String text) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("dimension", embeddingDimension);
        parameters.put("output_type", "dense");

        Map<String, Object> input = new HashMap<>();
        input.put("texts", List.of(text));

        Map<String, Object> request = new HashMap<>();
        request.put("model", qwenEmbeddingModel);
        request.put("input", input);
        request.put("parameters", parameters);
        return request;
    }



    /**
     * 调用通义千问向量模型，将文本转换为稠密向量。
     *
     * @param text 待向量化的文本
     * @return 稠密向量
     */
    public float[] embed_v1(String text)
    {
        KnowledgeQwenEmbeddingModels.Request request = new KnowledgeQwenEmbeddingModels.Request();
        request.setModel(qwenEmbeddingModel);

        KnowledgeQwenEmbeddingModels.Input input = new KnowledgeQwenEmbeddingModels.Input();
        input.setTexts(Collections.singletonList(text));
        request.setInput(input);

        KnowledgeQwenEmbeddingModels.Parameters parameters = new KnowledgeQwenEmbeddingModels.Parameters();
        parameters.setDimension(embeddingDimension);
        parameters.setOutputType("dense");
        request.setParameters(parameters);

        KnowledgeQwenEmbeddingModels.Response response = restClient
                .post()
                .uri(qwenBaseUrl + qwenEmbeddingPath)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + qwenApiKey)
                .body(request)
                .retrieve()
                .body(KnowledgeQwenEmbeddingModels.Response.class);

        if (response == null || response.getOutput() == null
                || response.getOutput().getEmbeddings() == null
                || response.getOutput().getEmbeddings().isEmpty())
        {
            String code = response == null ? null : response.getCode();
            String message = response == null ? null : response.getMessage();
            log.error("调用通义千问向量模型失败，code={}, message={}", code, message);
            throw new ServiceException("文本向量化失败");
        }

        List<Float> embedding = response.getOutput().getEmbeddings().get(0).getEmbedding();
        float[] vector = new float[embedding.size()];
        for (int i = 0; i < embedding.size(); i++)
        {
            vector[i] = embedding.get(i);
        }
        return vector;
    }

}

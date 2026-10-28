package com.atguigu.kmp.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.kmp.client.KnowledgeQwenEmbeddingClient;
import com.atguigu.kmp.entity.KnowledgeQuestionCluster;
import com.atguigu.kmp.entity.KnowledgeUnit;
import com.atguigu.kmp.service.KnowledgeQuestionClusterVectorService;
import com.atguigu.kmp.service.KnowledgeUnitService;
import io.milvus.client.MilvusServiceClient;
import io.milvus.param.dml.DeleteParam;
import io.milvus.param.dml.UpsertParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class KnowledgeQuestionClusterVectorServiceImpl implements KnowledgeQuestionClusterVectorService {

    @Autowired
    private KnowledgeQwenEmbeddingClient embeddingClient;

    @Autowired
    private MilvusServiceClient milvusServiceClient;

    @Autowired
    private KnowledgeUnitService knowledgeUnitService;

    @Value("${kmp.milvus.collection-name:kmp_question_cluster}")
    private String collectionName;

    /** {@inheritDoc} */
    @Override
    public void saveQuestionClusterVector(KnowledgeQuestionCluster cluster)
    {
        KnowledgeUnit unit = knowledgeUnitService.getById(cluster.getKnowledgeUnitId());
        String itemName = unit.getKnowledgeUnitName();
        float[] embedding = embeddingClient.embed(cluster.getStandardQuestion());

        // 构建向量数据
        JSONObject row = new JSONObject();
        // 设置问题簇id
        row.put("question_cluster_id", cluster.getId());
        // 设置知识单元id
        row.put("knowledge_unit_id", cluster.getKnowledgeUnitId());
        // 设置知识单元名称
        row.put("item_name", itemName);
        // 设置向量
        JSONArray vector = new JSONArray();
        for (float value : embedding)
        {
            vector.add(value);
        }
        row.put("embedding", vector);

        // 插入或更新向量数据
        milvusServiceClient.upsert(
                UpsertParam
                        .newBuilder()
                        .withCollectionName(collectionName)
                        .withRows(Collections.singletonList(row))
                        .build()
        );
    }

    /** {@inheritDoc} */
    @Override
    public void deleteQuestionClusterVector(Long questionClusterId)
    {
        milvusServiceClient.delete(
                DeleteParam.newBuilder()
                        .withCollectionName(collectionName)
                        .withExpr("question_cluster_id == " + questionClusterId)
                        .build()
        );
    }
}

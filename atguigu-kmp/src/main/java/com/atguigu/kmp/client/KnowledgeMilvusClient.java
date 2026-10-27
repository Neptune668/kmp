package com.atguigu.kmp.client;

import io.milvus.client.MilvusServiceClient;
import io.milvus.grpc.DataType;
import io.milvus.param.ConnectParam;
import io.milvus.param.IndexType;
import io.milvus.param.MetricType;
import io.milvus.param.R;
import io.milvus.param.collection.CreateCollectionParam;
import io.milvus.param.collection.FieldType;
import io.milvus.param.collection.HasCollectionParam;
import io.milvus.param.collection.LoadCollectionParam;
import io.milvus.param.index.CreateIndexParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * Milvus 向量库的接入配置类。
 *
 * <p>职责有两件事：一是按配置创建全局复用的 MilvusServiceClient，二是保证
 * 问题簇 Collection 在应用启动后处于“已建表、已建索引、已加载”的可用状态。</p>
 */
@Slf4j
@Component
public class KnowledgeMilvusClient {

    /** 向量维度，必须与百炼 embedding 返回的维度完全一致，否则写入会被 Milvus 拒绝。 */
    @Value("${kmp.milvus.embedding-dimension:1024}")
    private int embeddingDimension;

    /** Milvus 服务地址，默认本机部署。 */
    @Value("${kmp.milvus.host:127.0.0.1}")
    private String milvusHost;

    /** Milvus gRPC 端口，2.x 版本默认为 19530。 */
    @Value("${kmp.milvus.port:19530}")
    private int milvusPort;

    /** 目标数据库名；Milvus 2.x 支持多库，此处使用 default 库。 */
    @Value("${kmp.milvus.database-name:default}")
    private String milvusDatabaseName;

    /** 问题簇向量表名，同时也是建表、检索、删除时使用的唯一 Collection。 */
    @Value("${kmp.milvus.collection-name:kmp_question_cluster}")
    private String collectionName;

    /**
     * 创建 Milvus 2.4 Java 客户端，并在建表完成后交给业务层复用。
     *
     * <p>MilvusServiceClient 内部维护 gRPC 长连接，属于重量级对象，因此注册为单例 Bean，
     * 全应用共享一个连接；destroyMethod 保证容器关闭时释放底层通道。</p>
     *
     * @return 可复用客户端
     */
    @Bean(destroyMethod = "close")
    public MilvusServiceClient milvusServiceClient()
    {
        // 用配置的地址、端口、库名组装连接参数；未设置凭据，即按匿名方式连接（服务端未开启认证）。
        MilvusServiceClient client = new MilvusServiceClient(
                ConnectParam
                        .newBuilder()
                        .withHost(milvusHost)
                        .withPort(milvusPort)
                        .withDatabaseName(milvusDatabaseName)
                        .build()
        );
        // 启动阶段建表，让上层 Service 可以假定 Collection 一定存在。
        initializeCollection(client);
        return client;
    }

    /**
     * 创建固定四字段 Collection、IVF_FLAT 索引并加载到内存。
     *
     * <p>整体是幂等的：表已存在时直接返回，不会重复建表，因此应用可以反复重启。</p>
     *
     * @param client 刚创建出来的 Milvus 客户端
     */
    private void initializeCollection(MilvusServiceClient client)
    {
        // 先问一次服务端这张表是否已存在，避免重复建表。
        R<Boolean> exists = client.hasCollection(
                HasCollectionParam
                        .newBuilder()
                        .withCollectionName(collectionName)
                        .build()
        );

        // 已存在则视为初始化完成，直接返回。
        if (Boolean.TRUE.equals(exists.getData()))
        {
            log.info("集合已存在");
            return;
        }

        log.info("正在创建集合");
        // 字段一：问题簇主键。使用 MySQL 侧的自增 ID 作为向量表主键（AutoID 关闭），
        // 保证 Milvus 与 MySQL 能通过同一个 ID 对应上同一条业务数据。
        FieldType clusterId = FieldType
                .newBuilder()
                .withName("question_cluster_id")
                .withDataType(DataType.Int64)
                .withPrimaryKey(true)
                .withAutoID(false)
                .build();

        // 字段二：知识单元 ID。作为标量过滤字段，检索时用它把召回范围限定在当前知识单元内。
        FieldType unitId = FieldType
                .newBuilder()
                .withName("knowledge_unit_id")
                .withDataType(DataType.Int64)
                .build();

        // 字段三：知识单元名称。
        FieldType itemName = FieldType
                .newBuilder()
                .withName("item_name")
                .withDataType(DataType.VarChar)
                .withMaxLength(200)
                .build();

        // 字段四：稠密向量。维度由配置注入。
        FieldType embedding = FieldType
                .newBuilder()
                .withName("embedding")
                .withDataType(DataType.FloatVector) //稠密向量
                .withDimension(embeddingDimension) // 维度
                .build();

        // 建表：四个字段一次性声明。
        client.createCollection(
                CreateCollectionParam
                        .newBuilder()
                        .withCollectionName(collectionName)
                        .withFieldTypes(java.util.List.of(clusterId, unitId, itemName, embedding))
                        .build()
        );

        // 建向量索引。这里建的是“稠密向量索引”。
        client.createIndex(
                CreateIndexParam
                        .newBuilder()
                        .withCollectionName(collectionName)
                        // 给表里哪个字段建索引；只能是向量字段，这里就是存放稠密向量的 embedding 列。
                        .withFieldName("embedding")
                        // 索引名字。
                        .withIndexName("question_cluster_embedding_idx")
                        // 索引算法：IVF_FLAT = “先分桶（IVF）+ 桶内拿原始向量精确比对（FLAT）”。
                        // 建索引时把全量向量聚类切成 nlist 个桶，检索时只扫其中 nprobe 个桶，
                        // nlist默认值（126），nprobe默认值（16）。
                        // 属于近似最近邻（ANN）：数据量大时快，但可能漏掉真正最相似的那条。
                        .withIndexType(IndexType.IVF_FLAT)
                        // 相似度度量：COSINE 即余弦相似度，只看两个向量的夹角、不看长短，分数越接近 1 越相似。
                        .withMetricType(MetricType.COSINE)
                        // 同步等待索引构建完成再返回，避开刚建完表就立刻检索、索引未就绪的情境；数据量越大此处阻塞越久。
                        .withSyncMode(true)
                        // 真正发起建索引请求。
                        .build()
        );

        // 加载到内存：Milvus 只有 load 之后数据才能被检索；SyncLoad 保证本方法返回时表已可用。
        client.loadCollection(
                LoadCollectionParam
                        .newBuilder()
                        .withCollectionName(collectionName)
                        .withSyncLoad(true)
                        .build()
        );

    }
}

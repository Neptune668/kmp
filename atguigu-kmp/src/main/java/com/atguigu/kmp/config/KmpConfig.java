package com.atguigu.kmp.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import lombok.Data;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Data
@Configuration
@MapperScan("com.atguigu.kmp.mapper")
@EnableScheduling // 启动spring定时任务
public class KmpConfig {

    /** MinIO 公网访问端点，不包对象路径。 */
    @Value("${kmp.minio-public-endpoint}")
    private String minioPublicEndpoint;


    @Value("${kmp.knowledge-import.poll-interval-ms:5000}")
    private long knowledgeImportPollIntervalMs;

    @Value("${kmp.knowledge-import.batch-size:100}")
    private int knowledgeImportBatchSize;

    @Value("${kmp.knowledge-import.expire-minutes:30}")
    private long knowledgeImportExpireMinutes;


    @Value("${kmp.knowledge-system.base-url}")
    private String knowledgeSystemBaseUrl;

    @Value("${kmp.knowledge-system.connect-timeout}")
    private Duration knowledgeSystemConnectTimeout;

    @Value("${kmp.knowledge-system.read-timeout}")
    private Duration knowledgeSystemReadTimeout;


    @Value("${kmp.qwen.base-url}")
    private String qwenBaseUrl;

    @Value("${kmp.qwen.connect-timeout}")
    private Duration qwenConnectTimeout;

    @Value("${kmp.qwen.read-timeout}")
    private Duration qwenReadTimeout;

    /** FAQ Elasticsearch 基础地址。 */
    @Value("${kmp.faq-search.base-url}")
    private String faqSearchBaseUrl;

    /** FAQ Elasticsearch 索引名称。 */
    @Value("${kmp.faq-search.index-name}")
    private String faqSearchIndexName;

    /** FAQ Elasticsearch HTTP 连接超时。 */
    @Value("${kmp.faq-search.connect-timeout}")
    private Duration faqSearchConnectTimeout;

    /** FAQ Elasticsearch HTTP 读取超时。 */
    @Value("${kmp.faq-search.read-timeout}")
    private Duration faqSearchReadTimeout;

    /** 直接查询日志 Stream。 */
    @Value("${kmp.dashboard.query-log-stream-key}")
    private String dashboardQueryLogStreamKey;

//    /** FAQ 访问日志 Stream。 */
//    @Value("${kmp.dashboard.faq-log-stream-key}")
//    private String dashboardFaqLogStreamKey;
//
//    /** 两类日志共享的消费组。 */
//    @Value("${kmp.dashboard.consumer-group}")
//    private String dashboardConsumerGroup;
//
//    /** Stream 单批最大读取数量。 */
//    @Value("${kmp.dashboard.stream.batch-size}")
//    private int dashboardStreamBatchSize;
//
//    /** Stream 阻塞读取时间。 */
//    @Value("${kmp.dashboard.stream.block-timeout}")
//    private Duration dashboardStreamBlockTimeout;
//
//    /** Pending 消息允许的最大空闲时间。 */
//    @Value("${kmp.dashboard.stream.pending-idle}")
//    private Duration dashboardStreamPendingIdle;
//
//    /** 消息进入死信 Stream 前的最大处理次数。 */
//    @Value("${kmp.dashboard.stream.max-retries}")
//    private int dashboardStreamMaxRetries;

    /** Stream 近似裁剪长度。 */
    @Value("${kmp.dashboard.stream.max-length}")
    private long dashboardStreamMaxLength;
//
//    /** 单个请求处理锁超时。 */
//    @Value("${kmp.dashboard.idempotent-lock-timeout}")
//    private Duration dashboardIdempotentLockTimeout;

    /**
     * RestClient客户端：调用知识库接口
     *
     * @return RestClient
     */
    @Bean("knowledgeSystemRestClient")
    public RestClient knowledgeSystemRestClient()
    {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(knowledgeSystemConnectTimeout);
        requestFactory.setReadTimeout(knowledgeSystemReadTimeout);

        return RestClient
                .builder()
                .requestFactory(requestFactory)
                .baseUrl(knowledgeSystemBaseUrl)
                .build();
    }

    @Bean("qwenRestClient")
    public RestClient qwenRestClient()
    {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(qwenConnectTimeout);
        requestFactory.setReadTimeout(qwenReadTimeout);

        return RestClient
                .builder()
                .requestFactory(requestFactory)
                .baseUrl(qwenBaseUrl)
                .build();
    }

    @Bean("faqSearchRestClient")
    public RestClient faqSearchRestClient()
    {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(faqSearchConnectTimeout);
        requestFactory.setReadTimeout(faqSearchReadTimeout);

        return RestClient
                .builder()
                .requestFactory(requestFactory)
                .baseUrl(faqSearchBaseUrl)
                .build();
    }

    /**
     * 注册 MySQL 分页插件；分页插件应保持为内部插件链的最后一项。
     *
     * @return MyBatis-Plus 插件链
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor()
    {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}

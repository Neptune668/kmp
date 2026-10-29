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

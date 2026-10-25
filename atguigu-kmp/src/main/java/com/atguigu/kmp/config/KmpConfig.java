package com.atguigu.kmp.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import lombok.Data;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@MapperScan("com.atguigu.kmp.mapper")
public class KmpConfig {

    /** MinIO 公网访问端点，不包对象路径。 */
    @Value("${kmp.minio-public-endpoint}")
    private String minioPublicEndpoint;

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

package com.atguigu.kmp.generator;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.ruoyi.common.core.controller.BaseController;

import java.util.Collections;

/**
 * 知识库模块 MyBatis-Plus 基础代码生成器。
 */
public final class KmpCodeGenerator
{
    public static void main(String[] args){

        // 数据库凭据只从环境变量读取，避免将密码提交到代码仓库
        String databaseUrl = System.getenv("KMP_GENERATOR_DB_URL");
        String databaseUsername = System.getenv("KMP_GENERATOR_DB_USERNAME");
        String databasePassword = System.getenv("KMP_GENERATOR_DB_PASSWORD");

        String moduleRoot = System.getProperty("user.dir") + "/atguigu-kmp";

        FastAutoGenerator.create(databaseUrl, databaseUsername, databasePassword)
                .globalConfig(builder -> builder
                        .author("atguigu")
                        .disableOpenDir()
                        .outputDir(moduleRoot + "/src/main/java"))
                .packageConfig(builder -> builder
                        .parent("com.atguigu.kmp")
                        .entity("entity")
                        .mapper("mapper")
                        .service("service")
                        .serviceImpl("service.impl")
                        .controller("controller")

                        // 配置Mapper XML文件生成路径
                        .pathInfo(Collections.singletonMap(
                                OutputFile.xml,
                                moduleRoot + "/src/main/resources/mapper/kmp")))

                .strategyConfig(builder -> builder
                        // 配置需要生成的所有表名
                        .addInclude(
//                                "kmp_knowledge_auth",
                                "kmp_knowledge_faq_log",
                                "kmp_knowledge_group",
                                "kmp_knowledge_import_task",
                                "kmp_knowledge_query_log",
                                "kmp_knowledge_question_cluster",
                                "kmp_knowledge_query_session",
                                "kmp_knowledge_question_cluster_check_task",
                                "kmp_knowledge_unit"
                        )
                        .addTablePrefix("kmp_")

                        .entityBuilder()
                        .enableLombok()
                        .idType(IdType.AUTO)
                        .logicDeleteColumnName("is_deleted")

                        .serviceBuilder()
                        .formatServiceFileName("%sService")
                        .formatServiceImplFileName("%sServiceImpl")

                        .controllerBuilder()
                        .enableRestStyle()
                        .superClass(BaseController.class))
                .execute();
    }
}
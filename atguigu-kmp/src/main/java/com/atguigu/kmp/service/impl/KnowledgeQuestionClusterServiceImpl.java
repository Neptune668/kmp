package com.atguigu.kmp.service.impl;

import com.atguigu.kmp.client.FaqSearchClient;
import com.atguigu.kmp.constant.QuestionClusterConstants;
import com.atguigu.kmp.entity.KnowledgeGroup;
import com.atguigu.kmp.entity.KnowledgeQuestionCluster;
import com.atguigu.kmp.entity.KnowledgeUnit;
import com.atguigu.kmp.mapper.KnowledgeQuestionClusterMapper;
import com.atguigu.kmp.service.KnowledgeGroupService;
import com.atguigu.kmp.service.KnowledgeQuestionClusterService;
import com.atguigu.kmp.service.KnowledgeQuestionClusterVectorService;
import com.atguigu.kmp.service.KnowledgeUnitService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * <p>
 * 问题簇表 服务实现类
 * </p>
 *
 * @author atguigu
 * @since 2026-10-20
 */
@Service
public class KnowledgeQuestionClusterServiceImpl extends ServiceImpl<KnowledgeQuestionClusterMapper, KnowledgeQuestionCluster> implements KnowledgeQuestionClusterService {


    @Autowired
    private KnowledgeUnitService knowledgeUnitService;

    @Autowired
    private KnowledgeGroupService knowledgeGroupService;

    @Autowired
    private KnowledgeQuestionClusterVectorService knowledgeQuestionClusterVectorService;


    /** {@inheritDoc} */
    @Override
    public IPage<KnowledgeQuestionCluster> pageQuestionClusters(
            Page<KnowledgeQuestionCluster> page,
            KnowledgeQuestionCluster query
    )
    {
        IPage<KnowledgeQuestionCluster> result = baseMapper.selectQuestionClusterPage(page, query);
        return result;
    }


    /** {@inheritDoc} */
    @Override
    public KnowledgeQuestionCluster getQuestionClusterDetail(Long id)
    {
        KnowledgeQuestionCluster cluster = this.getById(id);

        KnowledgeUnit knowledgeUnit = knowledgeUnitService.getById(cluster.getKnowledgeUnitId());

        KnowledgeGroup knowledgeGroup = knowledgeGroupService.getById(cluster.getKnowledgeGroupId());

        cluster.setKnowledgeUnitName(knowledgeUnit == null ? null : knowledgeUnit.getKnowledgeUnitName());
        cluster.setKnowledgeGroupName(knowledgeGroup == null ? null : knowledgeGroup.getKnowledgeGroupName());
        if (cluster.getRecentQuestions() == null)
        {
            cluster.setRecentQuestions(Collections.emptyList());
        }
        return cluster;
    }

    @Override
    public void removeQuestionCluster(Long id, Long userId) {

//        this.removeById(id);
        // 删除向量数据
        knowledgeQuestionClusterVectorService.deleteQuestionClusterVector(id);
        // 删除SQL数据

        this.lambdaUpdate()
                .eq(KnowledgeQuestionCluster::getId, id)
                .set(KnowledgeQuestionCluster::getUpdateUserId, userId)
                .set(KnowledgeQuestionCluster::getUpdateTime, LocalDateTime.now())
                .set(KnowledgeQuestionCluster::getIsDeleted, 1)
                .update();
    }

    /** {@inheritDoc} */
    @Override
    public Map<String, Object> createQuestionCluster(KnowledgeQuestionCluster cluster, Long userId)
    {
        LocalDateTime now = LocalDateTime.now();
        // 所有状态、统计和审计字段均由服务端初始化，忽略客户端伪造值。
        cluster.setId(null);
        cluster.setStatus(QuestionClusterConstants.STATUS_ACCUMULATING);
        cluster.setAssociationCount(0L);
        cluster.setFaqClickCount(0L);
        cluster.setExposureCount(0L);
        cluster.setHelpfulCount(0L);
        cluster.setUnhelpfulCount(0L);
        cluster.setRefusalCount(0L);
        cluster.setRecentQuestions(Collections.emptyList());
        cluster.setCreateUserId(userId);
        cluster.setCreateTime(now);
        cluster.setUpdateUserId(null);
        cluster.setUpdateTime(null);
        cluster.setIsDeleted(0);
        this.save(cluster);

        boolean vectorSynced = true;
        try
        {
            knowledgeQuestionClusterVectorService.saveQuestionClusterVector(cluster);
        }
        catch (RuntimeException exception)
        {
            vectorSynced = false;
            log.warn("问题簇已保存但向量同步失败");
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", cluster.getId());
        result.put("vectorSynced", vectorSynced);
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public void updateQuestionCluster(KnowledgeQuestionCluster input, Long userId)
    {

        // 设置可信字段，防止客户端伪造
        KnowledgeQuestionCluster cluster = new KnowledgeQuestionCluster();
        cluster.setId(input.getId());
        cluster.setStandardQuestion(input.getStandardQuestion());
        cluster.setStandardAnswer(input.getStandardAnswer());
        cluster.setKnowledgeUnitId(input.getKnowledgeUnitId());
        cluster.setKnowledgeGroupId(input.getKnowledgeGroupId());
        // 更新向量
        knowledgeQuestionClusterVectorService.saveQuestionClusterVector(cluster);

        // 已发布问题变化时，更新ES。
        KnowledgeQuestionCluster current = this.getById(input.getId());
        if (Integer.valueOf(QuestionClusterConstants.STATUS_PUBLISHED).equals(current.getStatus()))
        {
            // TODO 更新ES
        }

        // 更新数据库
        lambdaUpdate()
                .eq(KnowledgeQuestionCluster::getId, current.getId())
                .eq(KnowledgeQuestionCluster::getStatus, current.getStatus())
                .set(KnowledgeQuestionCluster::getStandardQuestion, input.getStandardQuestion())
                .set(KnowledgeQuestionCluster::getStandardAnswer, input.getStandardAnswer())
                .set(KnowledgeQuestionCluster::getKnowledgeUnitId, input.getKnowledgeUnitId())
                .set(KnowledgeQuestionCluster::getKnowledgeGroupId, input.getKnowledgeGroupId())
                .set(KnowledgeQuestionCluster::getUpdateUserId, userId)
                .set(KnowledgeQuestionCluster::getUpdateTime, LocalDateTime.now())
                .update();

    }

    @Autowired
    private FaqSearchClient faqSearchClient;


    /** {@inheritDoc} */
    @Override
    public void publishQuestionCluster(Long id, Long userId)
    {
        KnowledgeQuestionCluster cluster = this.getById(id);
        Integer oldStatus = cluster.getStatus();
        faqSearchClient.saveFaq(cluster);
        this.updateStatus(id, oldStatus, QuestionClusterConstants.STATUS_PUBLISHED, userId);
    }

    /** 发布状态转换。 */
    private boolean updateStatus(Long id, int oldStatus, int newStatus, Long userId)
    {
        return this.lambdaUpdate()
                .eq(KnowledgeQuestionCluster::getId, id)
                .eq(KnowledgeQuestionCluster::getStatus, oldStatus)
                .set(KnowledgeQuestionCluster::getStatus, newStatus)
                .set(KnowledgeQuestionCluster::getUpdateUserId, userId)
                .set(KnowledgeQuestionCluster::getUpdateTime, LocalDateTime.now())
                .update();
    }

    /** {@inheritDoc} */
    @Override
    public void unpublishQuestionCluster(Long id, Long userId)
    {
        faqSearchClient.deleteFaq(id);
        this.updateStatus(id, QuestionClusterConstants.STATUS_PUBLISHED, QuestionClusterConstants.STATUS_PENDING, userId);
    }
}

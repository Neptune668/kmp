package com.atguigu.kmp.service.impl;

import com.atguigu.kmp.client.FaqSearchClient;
import com.atguigu.kmp.constant.QuestionClusterConstants;
import com.atguigu.kmp.dto.KnowledgeFaqModels;
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
import com.ruoyi.common.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

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

    @Autowired
    private FaqSearchClient faqSearchClient;


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

    /** {@inheritDoc} */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishQuestionCluster(Long id, Long userId) {

        //获取当前问题簇数据记录
        KnowledgeQuestionCluster cluster = this.getById(id);

        Integer oldStatus = cluster.getStatus();

        // 引入es客户端
        // 防御性编程，如果oldStatus的状态是 QuestionClusterConstants.STATUS_PUBLISHED，则不执行更新操作
        if (oldStatus != QuestionClusterConstants.STATUS_PUBLISHED)
        {
            this.updateStatus(id, oldStatus, QuestionClusterConstants.STATUS_PUBLISHED, userId);

            faqSearchClient.saveFaq(cluster);
        }

    }

    /** {@inheritDoc} */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unpublishQuestionCluster(Long id, Long userId) {

        //获取当前问题簇数据记录
        KnowledgeQuestionCluster cluster = this.getById(id);
        Integer oldStatus = cluster.getStatus();

        if (oldStatus == QuestionClusterConstants.STATUS_PUBLISHED)
        {
            // 更新sql状态
            this.updateStatus(id, QuestionClusterConstants.STATUS_PUBLISHED, QuestionClusterConstants.STATUS_PENDING, userId);

            // 删除es上的数据
            faqSearchClient.deleteFaq(id);

        }

    }

    @Override
    public List<KnowledgeFaqModels.KnowledgeFaqItem> listRecommendedFaqs(String itemName) {

        // 查询推荐问题簇列表（热度分排名最靠前的FAQ_RECOMMENDATION_LIMIT条记录）
        List<KnowledgeQuestionCluster> clusters = baseMapper.selectRecommendedFaqs(
                itemName,
                QuestionClusterConstants.FAQ_RECOMMENDATION_LIMIT
        );

        //获取所有问题簇的id
        List<Long> clusterIds = clusters
                .stream()
//                .map(knowledgeQuestionCluster -> knowledgeQuestionCluster.getId())
                .map(KnowledgeQuestionCluster::getId)
                .toList();

        //写入曝光数（+1）
        this.increaseFaqExposureCounts(clusterIds);

        // 组装返回结果
        return clusters
                .stream()
                .map(this::toFaqItem)
                .toList();
    }

    /**
     * 两阶段查询：
     * 第一阶段：根据keyword在es中做模糊查询，找到候选标签
     * 第二阶段：负责校验数据在MySQL中的发布状态等信息
     * @param keyword
     * @return
     */
    @Override
    public List<KnowledgeFaqModels.KnowledgeFaqItem> listSuggestedFaqs(String keyword) {

        ///////////////第一阶段：根据keyword在es中做模糊查询，找到候选标签//////////////
        // 过滤掉长度小于阈值的keyword
        if (keyword.length() < QuestionClusterConstants.FAQ_SUGGESTION_MIN_LENGTH){
            return Collections.emptyList();
        }

        // 在ES中搜索FAQ ID列表
        List<Long> faqIds = faqSearchClient.searchFaqIds(
                keyword,
                QuestionClusterConstants.FAQ_SUGGESTION_LIMIT,
                QuestionClusterConstants.FAQ_SUGGESTION_MIN_SCORE
        );

        // 空候选列表
        if (faqIds.isEmpty()) {
            return Collections.emptyList();
        }

        ///////////////第二段：根据keyword在es中做模糊查询，找到候选标签//////////////
        List<KnowledgeQuestionCluster> clusterList = this.lambdaQuery()
                .select(KnowledgeQuestionCluster::getId, KnowledgeQuestionCluster::getStandardQuestion)
                .in(KnowledgeQuestionCluster::getId, faqIds)
                .eq(KnowledgeQuestionCluster::getStatus, QuestionClusterConstants.STATUS_PUBLISHED)
                .list();

        Map<Long, KnowledgeQuestionCluster> clusterMap = new HashMap<>();
        for (KnowledgeQuestionCluster cluster : clusterList) {
            clusterMap.put(cluster.getId(), cluster);
        }

        //MySQL 的 IN 查询不保证返回的顺序，es中的查询默认按照分数排序，所以这里需要按照faqIds的顺序进行排序
        List<KnowledgeFaqModels.KnowledgeFaqItem> faqItems = new ArrayList<>();
        for (Long faqId : faqIds) {
            KnowledgeQuestionCluster cluster = clusterMap.get(faqId);
            if (cluster == null) continue;

            faqItems.add(toFaqItem(cluster));
        }

        return faqItems;
    }

    private KnowledgeFaqModels.KnowledgeFaqItem toFaqItem(KnowledgeQuestionCluster cluster) {
        KnowledgeFaqModels.KnowledgeFaqItem item = new KnowledgeFaqModels.KnowledgeFaqItem();
        item.setId(cluster.getId());
        item.setStandardQuestion(cluster.getStandardQuestion());
        return item;
    }

    private void increaseFaqExposureCounts(List<Long> clusterIds) {
        if(StringUtils.isEmpty(clusterIds)) return;

        this.lambdaUpdate()
                .in(KnowledgeQuestionCluster::getId, clusterIds)
                .eq(KnowledgeQuestionCluster::getStatus, QuestionClusterConstants.STATUS_PUBLISHED)
                .setSql("exposure_count = COALESCE(exposure_count, 0) + 1")
                .update();
    }

    /**
     * 发布状态的转换
     * @param id
     * @param oldStatus
     * @param newStatus
     * @param userId
     */
    private void updateStatus(Long id, Integer oldStatus, int newStatus, Long userId) {
        this.lambdaUpdate()
                .eq(KnowledgeQuestionCluster::getId, id)
//                .eq(KnowledgeQuestionCluster::getStatus, oldStatus)
                .set(KnowledgeQuestionCluster::getStatus, newStatus)
                .set(KnowledgeQuestionCluster::getUpdateUserId, userId)
                .set(KnowledgeQuestionCluster::getUpdateTime, LocalDateTime.now())
                .update();
    }



}

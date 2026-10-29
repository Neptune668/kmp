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


    /**
     * {@inheritDoc}
     */
    @Override
    public IPage<KnowledgeQuestionCluster> pageQuestionClusters(
            Page<KnowledgeQuestionCluster> page,
            KnowledgeQuestionCluster query
    ) {
        IPage<KnowledgeQuestionCluster> result = baseMapper.selectQuestionClusterPage(page, query);
        return result;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public KnowledgeQuestionCluster getQuestionClusterDetail(Long id) {
        KnowledgeQuestionCluster cluster = this.getById(id);

        KnowledgeUnit knowledgeUnit = knowledgeUnitService.getById(cluster.getKnowledgeUnitId());

        KnowledgeGroup knowledgeGroup = knowledgeGroupService.getById(cluster.getKnowledgeGroupId());

        cluster.setKnowledgeUnitName(knowledgeUnit == null ? null : knowledgeUnit.getKnowledgeUnitName());
        cluster.setKnowledgeGroupName(knowledgeGroup == null ? null : knowledgeGroup.getKnowledgeGroupName());
        if (cluster.getRecentQuestions() == null) {
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

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> createQuestionCluster(KnowledgeQuestionCluster cluster, Long userId) {
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
        try {
            knowledgeQuestionClusterVectorService.saveQuestionClusterVector(cluster);
        } catch (RuntimeException exception) {
            vectorSynced = false;
            log.warn("问题簇已保存但向量同步失败");
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", cluster.getId());
        result.put("vectorSynced", vectorSynced);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateQuestionCluster(KnowledgeQuestionCluster input, Long userId) {

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
        if (Integer.valueOf(QuestionClusterConstants.STATUS_PUBLISHED).equals(current.getStatus())) {
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


    /**
     * {@inheritDoc}
     */
    @Override
    public void publishQuestionCluster(Long id, Long userId) {
        KnowledgeQuestionCluster cluster = this.getById(id);
        Integer oldStatus = cluster.getStatus();
        faqSearchClient.saveFaq(cluster);
        this.updateStatus(id, oldStatus, QuestionClusterConstants.STATUS_PUBLISHED, userId);
    }

    /**
     * 发布状态转换。
     */
    private boolean updateStatus(Long id, int oldStatus, int newStatus, Long userId) {
        return this.lambdaUpdate()
                .eq(KnowledgeQuestionCluster::getId, id)
                .eq(KnowledgeQuestionCluster::getStatus, oldStatus)
                .set(KnowledgeQuestionCluster::getStatus, newStatus)
                .set(KnowledgeQuestionCluster::getUpdateUserId, userId)
                .set(KnowledgeQuestionCluster::getUpdateTime, LocalDateTime.now())
                .update();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unpublishQuestionCluster(Long id, Long userId) {
        faqSearchClient.deleteFaq(id);
        this.updateStatus(id, QuestionClusterConstants.STATUS_PUBLISHED, QuestionClusterConstants.STATUS_PENDING, userId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<KnowledgeFaqModels.KnowledgeFaqItem> listRecommendedFaqs(String itemName) {

        List<KnowledgeQuestionCluster> clusters = baseMapper.selectRecommendedFaqs(
                itemName, QuestionClusterConstants.FAQ_RECOMMENDATION_LIMIT
        );
        // 热门推荐成功返回一次就累计一次曝光。
        List<Long> clusterIds = clusters
                .stream()
                .map(KnowledgeQuestionCluster::getId)
                .toList();

        // 写入曝光次数
        increaseFaqExposureCounts(clusterIds);

        // 生成返回结果
        List<KnowledgeFaqModels.KnowledgeFaqItem> faqItems = clusters
                .stream()
                .map(this::toFaqItem)
                .toList();
        return faqItems;
    }

    /**
     * 批量增加本次成功返回的热门 FAQ 曝光次数。
     */
    private void increaseFaqExposureCounts(List<Long> ids) {
        // 没有推荐结果时无需执行空 IN 更新，按成功处理。
        if (StringUtils.isEmpty(ids)) {
            return;
        }
        this.lambdaUpdate()
                .in(KnowledgeQuestionCluster::getId, ids)
                .eq(KnowledgeQuestionCluster::getStatus, QuestionClusterConstants.STATUS_PUBLISHED)
                // 取 exposure_count 的当前值，如果它是 NULL，就当它是 0，然后再 +1 写回去。
                .setSql("exposure_count = COALESCE(exposure_count, 0) + 1")
                .update();
    }

    /**
     * 将内部问题簇转为不含答案字段的展示对象。
     */
    private KnowledgeFaqModels.KnowledgeFaqItem toFaqItem(KnowledgeQuestionCluster cluster) {
        KnowledgeFaqModels.KnowledgeFaqItem item = new KnowledgeFaqModels.KnowledgeFaqItem();
        item.setId(cluster.getId());
        item.setStandardQuestion(cluster.getStandardQuestion());
        return item;
    }

    /**
     * FAQ 联想搜索，两段式流程：
     * <ol>
     *   <li>ES 只负责“找候选”：按相关度返回问题簇 ID 列表；</li>
     *   <li>MySQL 负责“验资格”：复核发布状态，过滤掉已下架/删除的候选，
     *       并按 ES 的相关度顺序输出。</li>
     * </ol>
     * ES 是辅助索引，MySQL 才是发布状态的事实来源。
     *
     * @param keyword 用户输入的联想关键词
     * @return 按 ES 相关度排序、且当前确实处于已发布状态的 FAQ 列表
     */
    @Override
    public List<KnowledgeFaqModels.KnowledgeFaqItem> listSuggestedFaqs(String keyword) {

        // 字符串过短直接返回，避免意义不大的 ES 请求。
        if (keyword.length() < QuestionClusterConstants.FAQ_SUGGESTION_MIN_LENGTH) {
            return Collections.emptyList();
        }

        // 第一段：ES 全文匹配，返回按相关度排序的候选 ID。
        List<Long> candidateIds = faqSearchClient.searchFaqIds(
                keyword,
                QuestionClusterConstants.FAQ_SUGGESTION_LIMIT,
                QuestionClusterConstants.FAQ_SUGGESTION_MIN_SCORE
        );

        // 空候选直接返回。
        if (candidateIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 第二段：MySQL 复核发布状态（避免取消发布后 ES 索引可能尚未同步删除）。
        // 结果转成 id -> 实体 的 Map，供下一步按 ES 顺序重排使用。
        List<KnowledgeQuestionCluster> validClusters = this.lambdaQuery()
                .select(KnowledgeQuestionCluster::getId, KnowledgeQuestionCluster::getStandardQuestion)
                .in(KnowledgeQuestionCluster::getId, candidateIds)
                .eq(KnowledgeQuestionCluster::getStatus, QuestionClusterConstants.STATUS_PUBLISHED)
                .list();
        Map<Long, KnowledgeQuestionCluster> validClusterMap = new HashMap<>();
        for (KnowledgeQuestionCluster cluster : validClusters) {
            validClusterMap.put(cluster.getId(), cluster);
        }

        // MySQL 的 IN 查询不保证返回顺序，所以以 candidateIds 为基准遍历：
        // 状态复核不通过的（Map 中缺失）跳过，剩下的保持 ES 相关度顺序输出。
        List<KnowledgeFaqModels.KnowledgeFaqItem> faqItems = new ArrayList<>();
        for (Long candidateId : candidateIds) {
            KnowledgeQuestionCluster cluster = validClusterMap.get(candidateId);
            if (cluster == null) {
                continue;
            }
            faqItems.add(toFaqItem(cluster));
        }
        return faqItems;
    }
}

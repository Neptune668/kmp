package com.atguigu.kmp.service.impl;

import com.atguigu.kmp.config.KmpConfig;
import com.atguigu.kmp.entity.KnowledgeGroup;
import com.atguigu.kmp.entity.KnowledgeUnit;
import com.atguigu.kmp.mapper.KnowledgeUnitMapper;
import com.atguigu.kmp.service.KnowledgeGroupService;
import com.atguigu.kmp.service.KnowledgeUnitService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.service.ISysUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 知识单元表 服务实现类
 * </p>
 *
 * @author atguigu
 * @since 2026-10-21
 */
@Slf4j
@Service
public class KnowledgeUnitServiceImpl extends ServiceImpl<KnowledgeUnitMapper, KnowledgeUnit> implements KnowledgeUnitService {

    @Autowired
    private ISysUserService userService;

    @Autowired
    private KmpConfig kmpConfig;

    @Autowired
    private KnowledgeGroupService knowledgeGroupService;

    /**
     * {@inheritDoc}
     */
    @Override
    public Page<KnowledgeUnit> pageKnowledgeUnits(Page<KnowledgeUnit> page, KnowledgeUnit query) {
        log.info("分页查询知识单元列表");

        // 获取查询条件，去除空格或将空字符串转为 null
        String normalizedName = StringUtils.trimToNull(query.getKnowledgeUnitName());
        String normalizedDocumentId = StringUtils.trimToNull(query.getDocumentId());

        // 构建查询条件
        LambdaQueryWrapper<KnowledgeUnit> wrapper = new LambdaQueryWrapper<KnowledgeUnit>()
                // 知识单元名称模糊查询
                .like(normalizedName != null, KnowledgeUnit::getKnowledgeUnitName, normalizedName)
                // 所属知识组精确查询
                .eq(query.getKnowledgeGroupId() != null, KnowledgeUnit::getKnowledgeGroupId, query.getKnowledgeGroupId())
                // 外部文档ID精确查询
                .eq(normalizedDocumentId != null, KnowledgeUnit::getDocumentId, normalizedDocumentId)
                // 按更新时间、ID 降序排序
                .orderByDesc(KnowledgeUnit::getUpdateTime)
                .orderByDesc(KnowledgeUnit::getId);

        // 执行查询并返回结果
        Page<KnowledgeUnit> result = this.page(page, wrapper);

        // 补充知识组名称
        fillKnowledgeGroupNames(result.getRecords());

        //补充下载链接
        fillOriginalFileDownloadUrls(result.getRecords());
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Autowired
    private ISysUserService sysUserService;


    /** {@inheritDoc} */
    @Override
    public KnowledgeUnit getKnowledgeUnitDetail(Long id)
    {
        log.info("开始查询知识单元详情，knowledgeUnitId={}", id);

        KnowledgeUnit knowledgeUnit = this.getById(id);
        KnowledgeGroup knowledgeGroup = knowledgeGroupService.getById(knowledgeUnit.getKnowledgeGroupId());
        knowledgeUnit.setKnowledgeGroupName(knowledgeGroup == null ? null : knowledgeGroup.getKnowledgeGroupName());
        knowledgeUnit.setCreateUserName(this.findUserDisplayName(knowledgeUnit.getCreateUserId()));
        knowledgeUnit.setUpdateUserName(this.findUserDisplayName(knowledgeUnit.getUpdateUserId()));

        String endpoint = kmpConfig.getMinioPublicEndpoint();
        String downloadUrl = endpoint + knowledgeUnit.getOriginalFileDownloadUrl();
        knowledgeUnit.setOriginalFileDownloadUrl(downloadUrl);

        return knowledgeUnit;
    }

    /** 根据用户 ID 获取详情展示名称。 */
    private String findUserDisplayName(Long userId)
    {
        if (userId == null)
        {
            return null;
        }
        SysUser user = sysUserService.selectUserById(userId);
        return user == null ? null : user.getNickName();
    }

    /** 批量补充当前页知识组名称，避免 N+1 查询。 */
    private void fillKnowledgeGroupNames(List<KnowledgeUnit> knowledgeUnitList){
        // 获取所有知识组 ID
        List<Long> groupIds = knowledgeUnitList.stream()
                .map(KnowledgeUnit::getKnowledgeGroupId)
                .distinct()
                .toList();
        if (groupIds.isEmpty()){
            return;
        }
        // 创建知识组 id -> 名称 的映射
        Map<Long, String> groupNameMap = new HashMap<>();
        List<KnowledgeGroup> knowledgeGroupList = knowledgeGroupService.listByIds(groupIds);
        for (KnowledgeGroup knowledgeGroup : knowledgeGroupList) {
            groupNameMap.put(knowledgeGroup.getId(), knowledgeGroup.getKnowledgeGroupName());
        }
        for (KnowledgeUnit knowledgeUnit : knowledgeUnitList) {
            knowledgeUnit.setKnowledgeGroupName(groupNameMap.get(knowledgeUnit.getKnowledgeGroupId()));
        }
    }

    /** 为对外返回的知识单元批量补充 MinIO 完整下载地址。 */
    private void fillOriginalFileDownloadUrls(List<KnowledgeUnit> knowledgeUnitList)
    {
        String endpoint = kmpConfig.getMinioPublicEndpoint();
        for (KnowledgeUnit knowledgeUnit : knowledgeUnitList)
        {
            String downloadUrl = endpoint + knowledgeUnit.getOriginalFileDownloadUrl();
            knowledgeUnit.setOriginalFileDownloadUrl(downloadUrl);
        }
    }
}
package com.atguigu.kmp.service.impl;

import com.atguigu.kmp.entity.KnowledgeGroup;
import com.atguigu.kmp.entity.KnowledgeUnit;
import com.atguigu.kmp.mapper.KnowledgeUnitMapper;
import com.atguigu.kmp.service.KnowledgeGroupService;
import com.atguigu.kmp.service.KnowledgeUnitService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.service.ISysUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        return this.page(page, wrapper);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public KnowledgeUnit getKnowledgeUnitDetail(Long id) {
        log.info("getKnowledgeUnitDetail id: {}", id);

        KnowledgeUnit knowledgeUnit = this.getById(id);
        if (knowledgeUnit == null) {
            throw new ServiceException("知识单元不存在或已经被删除");
        }

        // 填充仅用于接口展示的冗余字段：所属知识组名称、创建人/修改人名称
        knowledgeUnit.setKnowledgeGroupName(findKnowledgeGroupName(knowledgeUnit.getKnowledgeGroupId()));
        knowledgeUnit.setCreateUserName(findUserDisplayName(knowledgeUnit.getCreateUserId()));
        knowledgeUnit.setUpdateUserName(findUserDisplayName(knowledgeUnit.getUpdateUserId()));

        return knowledgeUnit;
    }

    private String findUserDisplayName(Long userId) {
        if (userId == null) {
            return null;
        }
        return userService.selectUserById(userId).getNickName();
    }

    private String findKnowledgeGroupName(Long knowledgeGroupId) {
        if (knowledgeGroupId == null) {
            return null;
        }
        KnowledgeGroup knowledgeGroup = knowledgeGroupService.getById(knowledgeGroupId);
        return knowledgeGroup == null ? null : knowledgeGroup.getKnowledgeGroupName();
    }


}

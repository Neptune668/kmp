package com.atguigu.kmp.service.impl;

import com.atguigu.kmp.entity.KnowledgeGroup;
import com.atguigu.kmp.mapper.KnowledgeGroupMapper;
import com.atguigu.kmp.service.KnowledgeGroupService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * <p>
 * 知识组表 服务实现类
 * </p>
 *
 * @author atguigu
 * @since 2026-10-21
 */
@Slf4j
@Service
public class KnowledgeGroupServiceImpl extends ServiceImpl<KnowledgeGroupMapper, KnowledgeGroup> implements KnowledgeGroupService {

    @Override
    public Page<KnowledgeGroup> pageKnowledgeGroups(Page<KnowledgeGroup> page, KnowledgeGroup query) {
        log.info("分页查询知识组列表");

        // 获取知识组名称, 去除空格, 或将空字符串转为null
        String normalizedName = StringUtils.trimToNull(query.getKnowledgeGroupName());

        // 构建查询条件
        LambdaQueryWrapper<KnowledgeGroup> wrapper = new LambdaQueryWrapper<KnowledgeGroup>()

                // like 的第一个参数就是开关，为空时不会拼接对应条件。
                .like(normalizedName != null, KnowledgeGroup::getKnowledgeGroupName, normalizedName)

                // 按照更新时间降序排序, 按照ID降序排序
                .orderByDesc(KnowledgeGroup::getUpdateTime)
                .orderByDesc(KnowledgeGroup::getId);

        // 执行查询并返回结果
        return this.page(page, wrapper);
    }

    @Override
    public Long saveKnowledgeGroup(KnowledgeGroup knowledgeGroup, Long userId) {

        boolean editing = knowledgeGroup.getId() != null;
        log.info("开始{}知识组", editing ? "编辑" : "新增");

        LocalDateTime now = LocalDateTime.now();
        if (!editing) {
            // 新增接口必须忽略客户端伪造的主键和全部只允许后端赋值的字段。
            knowledgeGroup.setCreateUserId(userId);
            //审计数据
            knowledgeGroup.setCreateTime(now);
            knowledgeGroup.setUpdateUserId(null);
            knowledgeGroup.setUpdateTime(null);
            knowledgeGroup.setIsDeleted(0);
        } 
        else {
            if (this.getById(knowledgeGroup.getId()) == null) {
                throw new ServiceException("知识组不存在");
            }
            //审计数据，修改
            knowledgeGroup.setCreateUserId(null);
            knowledgeGroup.setCreateTime(null);
            knowledgeGroup.setUpdateUserId(userId);
            knowledgeGroup.setUpdateTime(now);
            knowledgeGroup.setIsDeleted(0);
        }

        // 如果当前知识组被设置为默认知识组，则将其他知识组的默认设置取消
        if (Integer.valueOf(1).equals(knowledgeGroup.getIsDefault())) {
            Long currentId = knowledgeGroup.getId();
            this.lambdaUpdate()
                    .eq(KnowledgeGroup::getIsDefault, 1)
                    .ne(currentId != null, KnowledgeGroup::getId, currentId)
                    .set(KnowledgeGroup::getIsDefault, 0)
                    .set(KnowledgeGroup::getUpdateUserId, userId)
                    .set(KnowledgeGroup::getUpdateTime, now)
                    .update();
        }
        this.saveOrUpdate(knowledgeGroup);
        return knowledgeGroup.getId();
    }
}

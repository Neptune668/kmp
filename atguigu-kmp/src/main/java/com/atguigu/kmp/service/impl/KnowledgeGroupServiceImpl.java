package com.atguigu.kmp.service.impl;

import com.atguigu.kmp.entity.KnowledgeGroup;
import com.atguigu.kmp.mapper.KnowledgeGroupMapper;
import com.atguigu.kmp.service.KnowledgeGroupService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.service.ISysUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 知识组表 服务实现类
 * </p>
 *
 * @author atguigu
 * @since 2026-10-20
 */
@Service
@Slf4j
public class KnowledgeGroupServiceImpl extends ServiceImpl<KnowledgeGroupMapper, KnowledgeGroup> implements KnowledgeGroupService {

//    @Autowired
//    private KnowledgeGroupMapper knowledgeGroupMapper;

    @Autowired
    private ISysUserService userService;

    @Autowired
    private ISysUserService sysUserService;

//    @Override
//    public Page<KnowledgeGroup> pageKnowledgeGroups(Page<KnowledgeGroup> page, KnowledgeGroup query) {
//        String knowledgeGroupName = query.getKnowledgeGroupName();
//        String normalizedName = StringUtils.trimToNull(knowledgeGroupName);
//
//        LambdaQueryWrapper<KnowledgeGroup> queryWrapper = new LambdaQueryWrapper<KnowledgeGroup>()
//                .like(normalizedName != null, KnowledgeGroup::getKnowledgeGroupName, knowledgeGroupName)
//
//                .orderByDesc(KnowledgeGroup::getCreateTime)
//                .orderByDesc(KnowledgeGroup::getId);
//
//        return this.page(page, queryWrapper);
//    }

    @Override
    public Page<KnowledgeGroup> pageKnowledgeGroups(Page<KnowledgeGroup> page, KnowledgeGroup query) {
        String knowledgeGroupName = query.getKnowledgeGroupName();

        LambdaQueryWrapper<KnowledgeGroup> queryWrapper = new LambdaQueryWrapper<KnowledgeGroup>()

                // 鼻涕先进行逻辑判断：要检索的字段是否是null，否则 like中可能会拼接 %null% 字符串，造成查询失败
                .like(StringUtils.hasText(knowledgeGroupName), KnowledgeGroup::getKnowledgeGroupName, knowledgeGroupName)

                .orderByDesc(KnowledgeGroup::getCreateTime)
                .orderByDesc(KnowledgeGroup::getId);

        return this.page(page, queryWrapper);
    }

//    @Override
//    public Long saveKnowledgeGroup(KnowledgeGroup knowledgeGroup, Long userId) {
//
//        LocalDateTime now = LocalDateTime.now();
//
//        // 新增
//        knowledgeGroup.setCreateUserId(userId);
//
//        // 审计数据
//        // 如果允许数据库产品切换，那么时间审计字段的管理就不能放在数据库级别进行维护，需要在业务中进行控制
//        knowledgeGroup.setCreateTime(now);
//        knowledgeGroup.setUpdateUserId(null);
//        knowledgeGroup.setUpdateTime(null);
//        knowledgeGroup.setIsDeleted(0);
//
//        this.save(knowledgeGroup);
//        return knowledgeGroup.getId();
//    }
//
//    @Override
//    public boolean updateKnowledgeGroup(KnowledgeGroup knowledgeGroup, Long userId) {
//
//        LocalDateTime now = LocalDateTime.now();
//        // 审计数据
//        knowledgeGroup.setCreateUserId(null);
//        knowledgeGroup.setCreateTime(null);
//        knowledgeGroup.setUpdateUserId(userId);
//        knowledgeGroup.setUpdateTime(now);
//        knowledgeGroup.setIsDeleted(null);
//
//        return this.updateById(knowledgeGroup);
//
//    }

    @Override
    public Long saveKnowledgeGroup(KnowledgeGroup knowledgeGroup, Long userId) {


        boolean editing = knowledgeGroup.getId() != null;

        LocalDateTime now = LocalDateTime.now();
        if (!editing){
            // 新增
            knowledgeGroup.setCreateUserId(userId);

            // 审计数据
            // 如果允许数据库产品切换，那么时间审计字段的管理就不能放在数据库级别进行维护，需要在业务中进行控制
            knowledgeGroup.setCreateTime(now);
            knowledgeGroup.setUpdateUserId(null);
            knowledgeGroup.setUpdateTime(null);
            knowledgeGroup.setIsDeleted(0);
        }else{

            // 修改
            if(this.getById(knowledgeGroup.getId()) == null){
                throw new ServiceException("知识组不存在");
            }

            // 审计数据
            knowledgeGroup.setCreateUserId(null);
            knowledgeGroup.setCreateTime(null);
            knowledgeGroup.setUpdateUserId(userId);
            knowledgeGroup.setUpdateTime(now);
            knowledgeGroup.setIsDeleted(null);
        }


        // 如果当前知识组被设置为默认知识组，则将其他知识组的默认设置取消
        if (Integer.valueOf(1).equals(knowledgeGroup.getIsDefault()))
        {
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

    /** {@inheritDoc} */
    @Override
    public KnowledgeGroup getKnowledgeGroupDetail(Long id) {
//        error
        log.info("getKnowledgeGroupDetail id: {}", id); //日志执行时才渲染结果
//        log.info("getKnowledgeGroupDetail id: " + id); //无论是否执行都提前渲染结果

        KnowledgeGroup knowledgeGroup = this.getById(id);
        if (knowledgeGroup == null){
            throw new ServiceException("知识组不存在或已经被删除");
        }

        knowledgeGroup.setCreateUserName(findUserDisplayName(knowledgeGroup.getCreateUserId()));
        knowledgeGroup.setUpdateUserName(findUserDisplayName(knowledgeGroup.getUpdateUserId()));


        return knowledgeGroup;
    }

    private String findUserDisplayName(Long userId) {
        if( userId == null){
            return null;
        }
        return userService.selectUserById(userId).getNickName();
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeKnowledgeGroup(Long id, Long userId)
    {
        log.info("删除知识组，userId={}，knowledgeGroupId={}", userId, id);

        KnowledgeGroup knowledgeGroup = this.getById(id);
        if (knowledgeGroup == null)
        {
            throw new ServiceException("知识组不存在或已删除");
        }

        if (Integer.valueOf(1).equals(knowledgeGroup.getIsDefault()))
        {
            log.warn("知识组删除被阻止，userId={}，knowledgeGroupId={}，reason=default", userId, id);
            throw new ServiceException("默认知识组不能删除，请先取消默认设置");
        }

        // MP 的逻辑删除不修改审计字段
        // 如果要修改审计字段，则需要自己写一个update操作
        return this.removeById(id);
    }

    /** {@inheritDoc} */
    @Override
    public List<KnowledgeGroup> listKnowledgeGroupOptions()
    {
        List<KnowledgeGroup> groupOptions = this.lambdaQuery()
                // 这里只查三列，够选择知识组即可。
                .select(
                        KnowledgeGroup::getId,
                        KnowledgeGroup::getKnowledgeGroupName,
                        KnowledgeGroup::getIsDefault
                )
                // 先按 `isDefault` 倒序，默认组自然排到最前面
                .orderByDesc(KnowledgeGroup::getIsDefault)
                // 名称相同时再用 ID 保证顺序稳定。
                .orderByAsc(KnowledgeGroup::getKnowledgeGroupName)
                .orderByAsc(KnowledgeGroup::getId)
                .list();
        return groupOptions;
    }

}

package com.atguigu.kmp.service.impl;

import com.atguigu.kmp.config.KmpConfig;
import com.atguigu.kmp.constant.KnowledgeUnitConstants;
import com.atguigu.kmp.dto.DeleteDocumentChunksResponse;
import com.atguigu.kmp.dto.ExternalKnowledgeTaskStatus;
import com.atguigu.kmp.dto.KnowledgeChunk;
import com.atguigu.kmp.dto.KnowledgeChunkPageResponse;
import com.atguigu.kmp.entity.KnowledgeGroup;
import com.atguigu.kmp.entity.KnowledgeImportTask;
import com.atguigu.kmp.entity.KnowledgeUnit;
import com.atguigu.kmp.mapper.KnowledgeUnitMapper;
import com.atguigu.kmp.service.KnowledgeGroupService;
import com.atguigu.kmp.service.KnowledgeUnitService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.system.service.ISysUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 知识单元表 服务实现类
 * </p>
 *
 * @author atguigu
 * @since 2026-10-20
 */
@Service
@Slf4j
public class KnowledgeUnitServiceImpl extends ServiceImpl<KnowledgeUnitMapper, KnowledgeUnit> implements KnowledgeUnitService {


    @Autowired
    private KnowledgeGroupService knowledgeGroupService;

    @Autowired
    private KmpConfig kmpConfig;

    @Autowired
    private ISysUserService sysUserService;


    @Autowired
    @Qualifier("knowledgeSystemRestClient")
    private RestClient restClient;

    /** {@inheritDoc} */
    @Override
    public IPage<KnowledgeUnit> pageKnowledgeUnits(Page<KnowledgeUnit> page, KnowledgeUnit query)
    {
        log.info("开始分页查询知识单元，pageNum={}，pageSize={}", page.getCurrent(), page.getSize());
        // 获取知识单元名称, 去除空格, 或将空字符串转为null
        String knowledgeUnitName = StringUtils.trimToNull(query.getKnowledgeUnitName());

        LambdaQueryWrapper<KnowledgeUnit> wrapper = new LambdaQueryWrapper<KnowledgeUnit>()

                // like 和 eq 的第一个参数就是开关，为空时不会拼接对应条件。
                .like(knowledgeUnitName != null, KnowledgeUnit::getKnowledgeUnitName, knowledgeUnitName)
                .eq(query.getKnowledgeGroupId() != null, KnowledgeUnit::getKnowledgeGroupId, query.getKnowledgeGroupId())
                .orderByDesc(KnowledgeUnit::getUpdateTime)
                .orderByDesc(KnowledgeUnit::getId);

        // 执行分页查询
        IPage<KnowledgeUnit> result = this.page(page, wrapper);

        // 补知识组名称
        this.fillKnowledgeGroupNames(result.getRecords());
        // 补充文档下载地址
        this.fillOriginalFileDownloadUrls(result.getRecords());

        return result;
    }

    private void fillOriginalFileDownloadUrls(List<KnowledgeUnit> records) {

        String minioPublicEndpoint = kmpConfig.getMinioPublicEndpoint();
        for (KnowledgeUnit record : records) {
            String originalFileDownloadUrl = minioPublicEndpoint + record.getOriginalFileDownloadUrl();
            record.setOriginalFileDownloadUrl(originalFileDownloadUrl);
        }

    }

//    private void fillKnowledgeGroupNames(List<KnowledgeUnit> records) {
//
//        // 遍历records
//           // 根据之始祖id查询知识组名称
//           // 设置到每一条记录中
//        for (KnowledgeUnit record : records) {
//
//            Long knowledgeGroupId = record.getKnowledgeGroupId();
//
//            // 执行了一次数据库查询
//            KnowledgeGroup knowledgeGroup = this.knowledgeGroupService.getById(knowledgeGroupId);
//            record.setKnowledgeGroupName(knowledgeGroup.getKnowledgeGroupName());
//        }
//
//    }

    /** 批量补充当前页知识组名称，避免 N+1 查询。 */
    private void fillKnowledgeGroupNames(List<KnowledgeUnit> knowledgeUnitList)
    {
        // 获取所有知识组 ID
        List<Long> groupIds = knowledgeUnitList.stream()
                .map(KnowledgeUnit::getKnowledgeGroupId)
                .distinct()
                .toList();
        if (groupIds.isEmpty())
        {
            return;
        }

        // 创建知识组 id -> 名称 的映射
        Map<Long, String> groupNameMap = new HashMap<>();

        // 批量查询知识组名称
        List<KnowledgeGroup> knowledgeGroupList = knowledgeGroupService.listByIds(groupIds);

        // 填充知识组 id -> 名称 集合
        for (KnowledgeGroup knowledgeGroup : knowledgeGroupList)
        {
            groupNameMap.put(knowledgeGroup.getId(), knowledgeGroup.getKnowledgeGroupName());
        }

        // 填充知识单元所属的知识组名称
        for (KnowledgeUnit knowledgeUnit : knowledgeUnitList)
        {
            knowledgeUnit.setKnowledgeGroupName(groupNameMap.get(knowledgeUnit.getKnowledgeGroupId()));
        }
    }



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


    /** {@inheritDoc} */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveKnowledgeUnitBaseInfo(KnowledgeUnit knowledgeUnit, Long userId)
    {
        Long knowledgeUnitId = knowledgeUnit.getId();
        log.info("开始修改知识单元基础信息，userId={}，knowledgeUnitId={}", userId, knowledgeUnitId);

        // 只构造可编辑字段的更新条件，防止页面篡改文档标识、文件和统计。
        this.lambdaUpdate()
                .eq(KnowledgeUnit::getId, knowledgeUnitId)
                //只修改字段白名单
                .set(KnowledgeUnit::getKnowledgeUnitName, knowledgeUnit.getKnowledgeUnitName())
                .set(KnowledgeUnit::getKnowledgeGroupId, knowledgeUnit.getKnowledgeGroupId())
                .set(KnowledgeUnit::getUpdateUserId, userId)
                .set(KnowledgeUnit::getUpdateTime, LocalDateTime.now())
                .update();

        return knowledgeUnitId;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeKnowledgeUnit(Long id)
    {
        log.info("开始逻辑删除知识单元，knowledgeUnitId={}", id);

        //删除知识单元下的全部切片信息
        KnowledgeUnit knowledgeUnit = this.getById(id);
        String documentId = knowledgeUnit.getDocumentId();

        DeleteDocumentChunksResponse result = restClient
                .delete()
                .uri("/knowledge-units/{documentId}/chunks", documentId)
                .retrieve()
                .body(DeleteDocumentChunksResponse.class);

        log.info("删除知识单元下的全部切片信息，document_id={}, deleted_count={}", result.getDocumentId(), result.getDeletedCount());

        // 删除知识单元
        return this.removeById(id);
    }

    @Override
    public KnowledgeUnit findBySha256(String fileSha256) {

//        LambdaQueryWrapper<KnowledgeUnit> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.select(KnowledgeUnit::getId, KnowledgeUnit::getKnowledgeUnitName);
//        queryWrapper.eq(KnowledgeUnit::getFileSha256, fileSha256);
////        KnowledgeUnit knowledgeUnit = baseMapper.selectOne(queryWrapper);
//        return this.getOne(queryWrapper);  I i

        // 防御性编程
        String normalizedSha256 = fileSha256 == null ? null : fileSha256.trim().toLowerCase();
        if(normalizedSha256 == null || !KnowledgeUnitConstants.SHA256_PATTERN.matcher(normalizedSha256).matches()){
            throw new ServiceException("文件SHA-256格式不正确");
        }

        return this.lambdaQuery()
                .select(KnowledgeUnit::getId, KnowledgeUnit::getKnowledgeUnitName)
                .eq(KnowledgeUnit::getFileSha256, normalizedSha256)
                .last("LIMIT 1")
                .one();
    }

    @Override
    public Long createFromCompletedImport(KnowledgeImportTask processingTask, ExternalKnowledgeTaskStatus externalStatus) {

        ExternalKnowledgeTaskStatus.Result result = externalStatus.getResult();

        KnowledgeUnit knowledgeUnit = new KnowledgeUnit();
        knowledgeUnit.setDocumentId(result.getDocumentId());
        knowledgeUnit.setKnowledgeGroupId(processingTask.getKnowledgeGroupId());
        knowledgeUnit.setKnowledgeUnitName(result.getItemName());
        knowledgeUnit.setOriginalFileName(processingTask.getFileName());
        knowledgeUnit.setFileSha256(result.getFileSha256());
        knowledgeUnit.setOriginalFileDownloadUrl(result.getOriginalFileDownloadUrl());
        knowledgeUnit.setChunkCount(result.getChunkCount());
        knowledgeUnit.setTotalTextLength(result.getTotalTextLength());
        knowledgeUnit.setCreateUserId(processingTask.getCreateUserId());
        knowledgeUnit.setCreateTime(LocalDateTime.now());
        knowledgeUnit.setIsDeleted(0);

        this.save(knowledgeUnit);
        return knowledgeUnit.getId();
    }

    @Override
    public KnowledgeChunkPageResponse pageChunks(Long id, Long pageNum, Long pageSize, Long userId) {

        String documentId = this.getById(id).getDocumentId();
        KnowledgeChunkPageResponse result = restClient
                .get()
                .uri(uriBuilder -> uriBuilder.path("/knowledge-units/{documentId}/chunks")
                        .queryParam("page_num", pageNum)
                        .queryParam("page_size", pageSize)
                        .build(documentId))
                .retrieve()
                .body(KnowledgeChunkPageResponse.class);

        //同步统计数据
        this.updateStatistics(id, result.getChunkCount(), result.getTotalTextLength(), userId);


        return result;
    }

    @Override
    public KnowledgeChunk saveChunk(Long id, String chunkId, KnowledgeChunk chunk, Long userId) {

        KnowledgeUnit knowledgeUnit = this.getById(id);
        String documentId = knowledgeUnit.getDocumentId();

        KnowledgeChunk result = null;
        if(chunkId == null){
            result = restClient.post().uri("/knowledge-units/{documentId}/chunks", documentId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(buildChunkRequest(chunk))
                    .retrieve()
                    .body(KnowledgeChunk.class);
        }else{
            result = restClient.put().uri("/knowledge-units/{documentId}/chunks/{chunkId}", documentId, chunkId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(buildChunkRequest(chunk))
                    .retrieve()
                    .body(KnowledgeChunk.class);
        }

        //同步统计数据
        this.updateStatistics(id, result.getChunkCount(), result.getTotalTextLength(), userId);

        return result;
    }

    @Override
    public KnowledgeChunk removeChunk(Long id, String chunkId, Long userId) {

        KnowledgeUnit knowledgeUnit = this.getById(id);
        String documentId = knowledgeUnit.getDocumentId();

        KnowledgeChunk result = restClient
                .delete()
                .uri(uriBuilder -> uriBuilder.path("/knowledge-units/{documentId}/chunks/{chunkId}")
                        .build(documentId, chunkId))
                .retrieve()
                .body(KnowledgeChunk.class);

        //同步统计数据
        this.updateStatistics(id, result.getChunkCount(), result.getTotalTextLength(), userId);

        return result;
    }

    private Map<String, Object> buildChunkRequest(KnowledgeChunk chunk) {

        Map<String, Object> request = new HashMap<>();
        request.put("chunk_order", chunk.getChunkOrder());
        request.put("title", chunk.getTitle());
        request.put("parent_title", chunk.getParentTitle());
        request.put("content", chunk.getContent());
        return request;
    }

    /**
     * 更新统计数据
     * @param id
     * @param chunkCount
     * @param totalTextLength
     * @param userId
     */
    private void updateStatistics(Long id, Long chunkCount, Long totalTextLength, Long userId) {
        this.lambdaUpdate()
                .eq(KnowledgeUnit::getId, id)
                .set(KnowledgeUnit::getChunkCount, chunkCount)
                .set(KnowledgeUnit::getTotalTextLength, totalTextLength)
                .set(KnowledgeUnit::getUpdateUserId, userId)
                .set(KnowledgeUnit::getUpdateTime, LocalDateTime.now())
                .update();
    }
}

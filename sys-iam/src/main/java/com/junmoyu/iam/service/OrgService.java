package com.junmoyu.iam.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.junmoyu.basic.exception.BusinessException;
import com.junmoyu.basic.model.PageResult;
import com.junmoyu.basic.model.SearchPageQuery;
import com.junmoyu.iam.mapper.OrgMapper;
import com.junmoyu.iam.mapper.UserMapper;
import com.junmoyu.iam.model.converter.OrgConverter;
import com.junmoyu.iam.model.converter.UserConverter;
import com.junmoyu.iam.model.entity.OrgEntity;
import com.junmoyu.iam.model.entity.UserEntity;
import com.junmoyu.iam.model.request.OrgCreateUpdateRequest;
import com.junmoyu.iam.model.response.OrgTreeNode;
import com.junmoyu.iam.model.response.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 组织架构管理服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrgService {

    private final OrgMapper orgMapper;
    private final UserMapper userMapper;

    /**
     * 获取完整的组织架构树
     */
    public List<OrgTreeNode> tree() {
        List<OrgEntity> allOrgs = orgMapper.selectList(new LambdaQueryWrapper<OrgEntity>()
                .orderByAsc(OrgEntity::getSortNum));
        return buildTree(allOrgs, 0L);
    }

    private List<OrgTreeNode> buildTree(List<OrgEntity> allOrgs, Long parentId) {
        List<OrgTreeNode> nodes = new ArrayList<>();
        for (OrgEntity org : allOrgs) {
            if (org.getParentId().equals(parentId)) {
                OrgTreeNode node = OrgConverter.INSTANCE.toTreeNode(org);
                node.setChildren(buildTree(allOrgs, org.getId()));
                nodes.add(node);
            }
        }
        return nodes;
    }

    /**
     * 分页获取某组织下的用户列表
     */
    public PageResult<UserResponse> pageUsers(Long orgId, SearchPageQuery query) {
        Page<UserEntity> page = new Page<>(query.getPage(), query.getSize());
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getOrgId, orgId);
        IPage<UserEntity> result = userMapper.selectPage(page, wrapper);

        List<UserResponse> list = result.getRecords().stream()
                .map(UserConverter.INSTANCE::toResponse)
                .toList();

        return new PageResult<>(result.getTotal(), list);
    }

    /**
     * 新增组织架构节点
     */
    @Transactional(rollbackFor = Exception.class)
    public Long create(OrgCreateUpdateRequest request) {
        checkExist(OrgEntity::getOrgCode, request.getOrgCode(), null, "组织编码已存在");

        OrgEntity entity = new OrgEntity();
        entity.setParentId(request.getParentId() != null ? request.getParentId() : 0L);
        entity.setOrgName(request.getOrgName());
        entity.setOrgCode(request.getOrgCode());
        entity.setSortNum(request.getSortNum() == null ? 1 : request.getSortNum());
        entity.setDisable(Boolean.FALSE);
        entity.setRemark(request.getRemark());
        orgMapper.insert(entity);

        log.info("新增组织节点成功: id={}, orgName={}", entity.getId(), entity.getOrgName());
        return entity.getId();
    }

    /**
     * 修改组织架构节点
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(Long id, OrgCreateUpdateRequest request) {
        OrgEntity entity = orgMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("组织节点不存在");
        }

        if (StringUtils.isNotBlank(request.getOrgCode()) && !request.getOrgCode().equals(entity.getOrgCode())) {
            checkExist(OrgEntity::getOrgCode, request.getOrgCode(), id, "组织编码已存在");
        }

        if (request.getParentId() != null) {
            entity.setParentId(request.getParentId());
        }
        if (request.getOrgName() != null) {
            entity.setOrgName(request.getOrgName());
        }
        if (request.getOrgCode() != null) {
            entity.setOrgCode(request.getOrgCode());
        }
        if (request.getSortNum() != null) {
            entity.setSortNum(request.getSortNum());
        }
        if (request.getRemark() != null) {
            entity.setRemark(request.getRemark());
        }

        orgMapper.updateById(entity);
        log.info("更新组织节点成功: id={}", id);
        return true;
    }

    /**
     * 删除组织架构节点
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(Long id) {
        OrgEntity entity = orgMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("组织节点不存在");
        }

        // 检查是否有子节点
        if (orgMapper.selectCount(new LambdaQueryWrapper<OrgEntity>()
                .eq(OrgEntity::getParentId, id)) > 0) {
            throw new BusinessException("该组织下存在子节点，无法删除");
        }

        // 检查是否有用户
        if (userMapper.selectCount(new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getOrgId, id)) > 0) {
            throw new BusinessException("该组织下存在用户，无法删除");
        }

        orgMapper.deleteById(id);
        log.info("删除组织节点成功: id={}", id);
        return true;
    }

    private void checkExist(SFunction<OrgEntity, ?> column, Object value, Long excludeId, String message) {
        Long count = orgMapper.selectCount(new LambdaQueryWrapper<OrgEntity>()
                .eq(column, value)
                .ne(excludeId != null, OrgEntity::getId, excludeId));
        if (count != null && count > 0) {
            throw new BusinessException(message);
        }
    }
}

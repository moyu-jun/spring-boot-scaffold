package com.junmoyu.iam.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.junmoyu.basic.exception.BusinessException;
import com.junmoyu.basic.model.PageResult;
import com.junmoyu.basic.model.SearchPageQuery;
import com.junmoyu.iam.mapper.RoleMapper;
import com.junmoyu.iam.mapper.RolePermissionMapper;
import com.junmoyu.iam.mapper.UserRoleMapper;
import com.junmoyu.iam.model.converter.RoleConverter;
import com.junmoyu.iam.model.entity.RoleEntity;
import com.junmoyu.iam.model.entity.RolePermission;
import com.junmoyu.iam.model.entity.UserRole;
import com.junmoyu.iam.model.request.RoleCreateUpdateRequest;
import com.junmoyu.iam.model.request.RoleUpdatePermissionRequest;
import com.junmoyu.iam.model.response.RoleResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 角色管理服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleMapper roleMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final UserRoleMapper userRoleMapper;

    /**
     * 分页查询角色列表
     */
    public PageResult<RoleResponse> page(SearchPageQuery query) {
        Page<RoleEntity> page = new Page<>(query.getPage(), query.getSize());

        LambdaQueryWrapper<RoleEntity> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(query.getKeywords())) {
            wrapper.like(RoleEntity::getRoleName, query.getKeywords())
                    .or()
                    .like(RoleEntity::getRoleCode, query.getKeywords());
        }
        wrapper.orderByAsc(RoleEntity::getSortNum);

        IPage<RoleEntity> result = roleMapper.selectPage(page, wrapper);
        List<RoleResponse> list = result.getRecords().stream()
                .map(RoleConverter.INSTANCE::toResponse)
                .toList();

        return new PageResult<>(result.getTotal(), list);
    }

    /**
     * 新增角色
     */
    @Transactional(rollbackFor = Exception.class)
    public Long create(RoleCreateUpdateRequest request) {
        checkExist(RoleEntity::getRoleCode, request.getRoleCode(), null, "角色编码已存在");

        RoleEntity entity = new RoleEntity();
        entity.setRoleName(request.getRoleName());
        entity.setRoleCode(request.getRoleCode());
        entity.setSortNum(request.getSortNum());
        entity.setDisable(Boolean.FALSE);
        entity.setRemark(request.getRemark());
        roleMapper.insert(entity);

        log.info("新增角色成功: id={}, roleName={}", entity.getId(), entity.getRoleName());
        return entity.getId();
    }

    /**
     * 修改角色
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(Long id, RoleCreateUpdateRequest request) {
        RoleEntity entity = roleMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("角色不存在");
        }

        if (!request.getRoleCode().equals(entity.getRoleCode())) {
            checkExist(RoleEntity::getRoleCode, request.getRoleCode(), id, "角色标识符已存在");
        }

        if (request.getRoleName() != null) {
            entity.setRoleName(request.getRoleName());
        }
        if (request.getRoleCode() != null) {
            entity.setRoleCode(request.getRoleCode());
        }
        if (request.getSortNum() != null) {
            entity.setSortNum(request.getSortNum());
        }
        if (request.getRemark() != null) {
            entity.setRemark(request.getRemark());
        }

        roleMapper.updateById(entity);
        log.info("更新角色成功: id={}", id);
        return true;
    }

    /**
     * 删除角色
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(Long id) {
        RoleEntity entity = roleMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("角色不存在");
        }

        // 删除用户-角色关联
        userRoleMapper.delete(new LambdaQueryWrapper<UserRole>().eq(UserRole::getRoleId, id));

        // 删除角色-权限关联
        rolePermissionMapper.delete(new LambdaQueryWrapper<RolePermission>().eq(RolePermission::getRoleId, id));

        roleMapper.deleteById(id);
        log.info("删除角色成功: id={}", id);
        return true;
    }

    /**
     * 为角色分配权限资源
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean updatePermissions(Long id, RoleUpdatePermissionRequest request) {
        RoleEntity entity = roleMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("角色不存在");
        }

        // 删除旧的权限关联
        rolePermissionMapper.delete(new LambdaQueryWrapper<RolePermission>().eq(RolePermission::getRoleId, id));

        // 插入新的权限关联
        if (CollectionUtils.isNotEmpty(request.getPermissionIds())) {
            List<RolePermission> rolePermissions = new ArrayList<>();
            for (Long permissionId : request.getPermissionIds()) {
                RolePermission rp = new RolePermission();
                rp.setRoleId(id);
                rp.setPermissionId(permissionId);
                rolePermissions.add(rp);
            }
            rolePermissionMapper.insert(rolePermissions);
        }

        log.info("为角色分配权限成功: roleId={}, permissionIds={}", id, request.getPermissionIds());
        return true;
    }

    private void checkExist(SFunction<RoleEntity, ?> column, Object value, Long excludeId, String message) {
        Long count = roleMapper.selectCount(new LambdaQueryWrapper<RoleEntity>()
                .eq(column, value)
                .ne(excludeId != null, RoleEntity::getId, excludeId));
        if (count != null && count > 0) {
            throw new BusinessException(message);
        }
    }
}

package com.junmoyu.iam.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.junmoyu.basic.exception.BusinessException;
import com.junmoyu.iam.mapper.PermissionMapper;
import com.junmoyu.iam.mapper.RolePermissionMapper;
import com.junmoyu.iam.model.converter.PermissionConverter;
import com.junmoyu.iam.model.entity.PermissionEntity;
import com.junmoyu.iam.model.entity.RolePermission;
import com.junmoyu.iam.model.request.PermissionCreateUpdateRequest;
import com.junmoyu.iam.model.response.PermissionTreeNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 权限资源管理服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionService {

    private final PermissionMapper permissionMapper;
    private final RolePermissionMapper rolePermissionMapper;

    /**
     * 获取全量权限/菜单资源树
     */
    public List<PermissionTreeNode> tree() {
        List<PermissionEntity> allPerms = permissionMapper.selectList(new LambdaQueryWrapper<PermissionEntity>()
                .orderByAsc(PermissionEntity::getSortNum));
        return buildTree(allPerms, 0L);
    }

    private List<PermissionTreeNode> buildTree(List<PermissionEntity> allPerms, Long parentId) {
        List<PermissionTreeNode> nodes = new ArrayList<>();
        for (PermissionEntity perm : allPerms) {
            if (perm.getParentId().equals(parentId)) {
                PermissionTreeNode node = PermissionConverter.INSTANCE.toTreeNode(perm);
                node.setChildren(buildTree(allPerms, perm.getId()));
                nodes.add(node);
            }
        }
        return nodes;
    }

    /**
     * 新增权限资源
     */
    @Transactional(rollbackFor = Exception.class)
    public Long create(PermissionCreateUpdateRequest request) {
        if (request.getPermCode() != null) {
            checkExist(PermissionEntity::getPermCode, request.getPermCode(), null, "权限标识已存在");
        }

        PermissionEntity entity = new PermissionEntity();
        entity.setParentId(request.getParentId() != null ? request.getParentId() : 0L);
        entity.setPermName(request.getPermName());
        entity.setPermCode(request.getPermCode());
        entity.setPermType(request.getPermType());
        entity.setPath(request.getPath());
        entity.setIcon(request.getIcon());
        entity.setSortNum(request.getSortNum());
        entity.setDisable(Boolean.FALSE);
        permissionMapper.insert(entity);

        log.info("新增权限资源成功: id={}, permName={}", entity.getId(), entity.getPermName());
        return entity.getId();
    }

    /**
     * 更新权限资源
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(Long id, PermissionCreateUpdateRequest request) {
        PermissionEntity entity = permissionMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("权限资源不存在");
        }

        if (request.getPermCode() != null && !request.getPermCode().equals(entity.getPermCode())) {
            checkExist(PermissionEntity::getPermCode, request.getPermCode(), id, "权限标识已存在");
        }

        if (request.getParentId() != null) {
            entity.setParentId(request.getParentId());
        }
        if (request.getPermName() != null) {
            entity.setPermName(request.getPermName());
        }
        if (request.getPermCode() != null) {
            entity.setPermCode(request.getPermCode());
        }
        if (request.getPermType() != null) {
            entity.setPermType(request.getPermType());
        }
        if (request.getPath() != null) {
            entity.setPath(request.getPath());
        }
        if (request.getIcon() != null) {
            entity.setIcon(request.getIcon());
        }
        if (request.getSortNum() != null) {
            entity.setSortNum(request.getSortNum());
        }

        permissionMapper.updateById(entity);
        log.info("更新权限资源成功: id={}", id);
        return true;
    }

    /**
     * 删除权限资源
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(Long id) {
        PermissionEntity entity = permissionMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("权限资源不存在");
        }

        // 检查是否有子节点
        if (permissionMapper.selectCount(new LambdaQueryWrapper<PermissionEntity>()
                .eq(PermissionEntity::getParentId, id)) > 0) {
            throw new BusinessException("该权限下存在子节点，无法删除");
        }

        // 删除角色-权限关联
        rolePermissionMapper.delete(new LambdaQueryWrapper<RolePermission>()
                .eq(RolePermission::getPermissionId, id));

        permissionMapper.deleteById(id);
        log.info("删除权限资源成功: id={}", id);
        return true;
    }

    private void checkExist(SFunction<PermissionEntity, ?> column, Object value, Long excludeId, String message) {
        Long count = permissionMapper.selectCount(new LambdaQueryWrapper<PermissionEntity>()
                .eq(column, value)
                .ne(excludeId != null, PermissionEntity::getId, excludeId));
        if (count != null && count > 0) {
            throw new BusinessException(message);
        }
    }
}

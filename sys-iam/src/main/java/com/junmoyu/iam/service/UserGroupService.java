package com.junmoyu.iam.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.junmoyu.basic.exception.BusinessException;
import com.junmoyu.basic.model.PageResult;
import com.junmoyu.basic.model.SearchPageQuery;
import com.junmoyu.iam.mapper.UserGroupMapper;
import com.junmoyu.iam.mapper.UserGroupRoleMapper;
import com.junmoyu.iam.mapper.UserGroupUserMapper;
import com.junmoyu.iam.mapper.UserMapper;
import com.junmoyu.iam.model.converter.UserConverter;
import com.junmoyu.iam.model.converter.UserGroupConverter;
import com.junmoyu.iam.model.entity.UserEntity;
import com.junmoyu.iam.model.entity.UserGroupEntity;
import com.junmoyu.iam.model.entity.UserGroupRole;
import com.junmoyu.iam.model.entity.UserGroupUser;
import com.junmoyu.iam.model.request.UserGroupCreateUpdateRequest;
import com.junmoyu.iam.model.request.UserGroupUpdateUserRequest;
import com.junmoyu.iam.model.response.UserGroupResponse;
import com.junmoyu.iam.model.response.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户组管理服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserGroupService {

    private final UserGroupMapper userGroupMapper;
    private final UserGroupRoleMapper userGroupRoleMapper;
    private final UserGroupUserMapper userGroupUserMapper;
    private final UserMapper userMapper;

    /**
     * 分页查询用户组列表
     */
    public PageResult<UserGroupResponse> page(SearchPageQuery query) {
        Page<UserGroupEntity> page = new Page<>(query.getPage(), query.getSize());

        LambdaQueryWrapper<UserGroupEntity> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(query.getKeywords())) {
            wrapper.like(UserGroupEntity::getGroupName, query.getKeywords())
                    .or()
                    .like(UserGroupEntity::getGroupCode, query.getKeywords());
        }
        wrapper.orderByAsc(UserGroupEntity::getGroupName);

        IPage<UserGroupEntity> result = userGroupMapper.selectPage(page, wrapper);
        List<UserGroupResponse> list = result.getRecords().stream()
                .map(UserGroupConverter.INSTANCE::toResponse)
                .toList();

        return new PageResult<>(result.getTotal(), list);
    }

    /**
     * 新增用户组
     */
    @Transactional(rollbackFor = Exception.class)
    public Long create(UserGroupCreateUpdateRequest request) {
        if (request.getGroupCode() != null) {
            checkExist(UserGroupEntity::getGroupCode, request.getGroupCode(), null, "用户组编码已存在");
        }

        UserGroupEntity entity = new UserGroupEntity();
        entity.setGroupName(request.getGroupName());
        entity.setGroupCode(request.getGroupCode());
        entity.setDisable(Boolean.FALSE);
        entity.setRemark(request.getRemark());
        userGroupMapper.insert(entity);

        saveGroupRoles(entity.getId(), request.getRoleIds());

        log.info("新增用户组成功: id={}, groupName={}", entity.getId(), entity.getGroupName());
        return entity.getId();
    }

    /**
     * 更新用户组
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(Long id, UserGroupCreateUpdateRequest request) {
        UserGroupEntity entity = userGroupMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("用户组不存在");
        }

        if (request.getGroupCode() != null && !request.getGroupCode().equals(entity.getGroupCode())) {
            checkExist(UserGroupEntity::getGroupCode, request.getGroupCode(), id, "用户组编码已存在");
        }

        if (request.getGroupName() != null) {
            entity.setGroupName(request.getGroupName());
        }
        if (request.getGroupCode() != null) {
            entity.setGroupCode(request.getGroupCode());
        }
        if (request.getRemark() != null) {
            entity.setRemark(request.getRemark());
        }

        userGroupMapper.updateById(entity);

        if (request.getRoleIds() != null) {
            userGroupRoleMapper.delete(new LambdaQueryWrapper<UserGroupRole>().eq(UserGroupRole::getGroupId, id));
            saveGroupRoles(id, request.getRoleIds());
        }

        log.info("更新用户组成功: id={}", id);
        return true;
    }

    /**
     * 删除用户组
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(Long id) {
        UserGroupEntity entity = userGroupMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("用户组不存在");
        }

        // 删除用户-用户组关联
        userGroupUserMapper.delete(new LambdaQueryWrapper<UserGroupUser>().eq(UserGroupUser::getGroupId, id));

        // 删除用户组-角色关联
        userGroupRoleMapper.delete(new LambdaQueryWrapper<UserGroupRole>().eq(UserGroupRole::getGroupId, id));

        userGroupMapper.deleteById(id);
        log.info("删除用户组成功: id={}", id);
        return true;
    }

    /**
     * 获取用户组成员列表
     */
    public PageResult<UserResponse> pageUsers(Long groupId) {
        List<UserGroupUser> relations = userGroupUserMapper.selectList(
                new LambdaQueryWrapper<UserGroupUser>().eq(UserGroupUser::getGroupId, groupId));

        if (relations.isEmpty()) {
            return new PageResult<>(0, List.of());
        }

        List<Long> userIds = relations.stream().map(UserGroupUser::getUserId).toList();
        List<UserEntity> users = userMapper.selectByIds(userIds);

        List<UserResponse> list = users.stream()
                .map(UserConverter.INSTANCE::toResponse)
                .toList();

        return new PageResult<>(list.size(), list);
    }

    /**
     * 管理用户组成员
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateUsers(Long groupId, UserGroupUpdateUserRequest request) {
        UserGroupEntity entity = userGroupMapper.selectById(groupId);
        if (entity == null) {
            throw new BusinessException("用户组不存在");
        }

        // 删除旧的成员关联
        userGroupUserMapper.delete(new LambdaQueryWrapper<UserGroupUser>().eq(UserGroupUser::getGroupId, groupId));

        // 添加新的成员关联
        if (request.getUserIds() != null && !request.getUserIds().isEmpty()) {
            for (Long userId : request.getUserIds()) {
                UserGroupUser relation = new UserGroupUser();
                relation.setGroupId(groupId);
                relation.setUserId(userId);
                userGroupUserMapper.insert(relation);
            }
        }

        log.info("更新用户组成员成功: groupId={}, userIds={}", groupId, request.getUserIds());
        return true;
    }

    private void saveGroupRoles(Long groupId, List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return;
        }
        for (Long roleId : roleIds) {
            UserGroupRole groupRole = new UserGroupRole();
            groupRole.setGroupId(groupId);
            groupRole.setRoleId(roleId);
            userGroupRoleMapper.insert(groupRole);
        }
        log.info("为用户组分配角色: groupId={}, roleIds={}", groupId, roleIds);
    }

    private void checkExist(SFunction<UserGroupEntity, ?> column, Object value, Long excludeId, String message) {
        Long count = userGroupMapper.selectCount(new LambdaQueryWrapper<UserGroupEntity>()
                .eq(column, value)
                .ne(excludeId != null, UserGroupEntity::getId, excludeId));
        if (count != null && count > 0) {
            throw new BusinessException(message);
        }
    }
}

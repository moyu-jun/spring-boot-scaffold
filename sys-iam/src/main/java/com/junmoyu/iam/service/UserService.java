package com.junmoyu.iam.service;

import cn.hutool.core.lang.Validator;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.junmoyu.basic.constant.BasicConst;
import com.junmoyu.basic.exception.BusinessException;
import com.junmoyu.basic.model.PageResult;
import com.junmoyu.basic.model.SearchPageQuery;
import com.junmoyu.iam.mapper.UserAuthMapper;
import com.junmoyu.iam.mapper.UserGroupUserMapper;
import com.junmoyu.iam.mapper.UserMapper;
import com.junmoyu.iam.mapper.UserRoleMapper;
import com.junmoyu.iam.model.entity.UserAuthEntity;
import com.junmoyu.iam.model.entity.UserEntity;
import com.junmoyu.iam.model.entity.UserGroupUser;
import com.junmoyu.iam.model.entity.UserRole;
import com.junmoyu.iam.model.request.UserAuthCreateRequest;
import com.junmoyu.iam.model.request.UserCreateRequest;
import com.junmoyu.iam.model.request.UserUpdatePasswordRequest;
import com.junmoyu.iam.model.request.UserUpdateRequest;
import com.junmoyu.iam.model.response.UserAuthResponse;
import com.junmoyu.iam.model.response.UserDetailResponse;
import com.junmoyu.iam.model.response.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

/**
 * 用户管理服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final UserAuthMapper userAuthMapper;
    private final UserGroupUserMapper userGroupUserMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * 分页查询用户列表
     */
    public PageResult<UserResponse> page(SearchPageQuery query) {
        Page<UserResponse> page = new Page<>(query.getPage(), query.getSize());
        IPage<UserResponse> result = userMapper.selectUserPage(page, query.getKeywords());
        return new PageResult<>(result.getTotal(), result.getRecords());
    }

    /**
     * 获取用户详情
     */
    public UserDetailResponse detail(Long id) {
        UserDetailResponse detail = userMapper.selectUserDetail(id);
        if (detail == null) {
            throw new BusinessException("用户不存在");
        }

        // 查询已分配角色
        List<UserRole> userRoles = userRoleMapper.selectList(
                new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, id));
        detail.setRoleIds(userRoles.stream().map(UserRole::getRoleId).toList());

        // 查询所属用户组
        List<UserGroupUser> groupUsers = userGroupUserMapper.selectList(
                new LambdaQueryWrapper<UserGroupUser>().eq(UserGroupUser::getUserId, id));
        detail.setGroupIds(groupUsers.stream().map(UserGroupUser::getGroupId).toList());

        return detail;
    }

    /**
     * 新增用户
     */
    @Transactional(rollbackFor = Exception.class)
    public Long create(UserCreateRequest request) {
        checkExist(UserEntity::getUsername, request.getUsername(), null, "用户名已存在");

        if (StringUtils.isNotBlank(request.getMobile())) {
            checkExist(UserEntity::getMobile, request.getMobile(), null, "手机号已被占用");
        }
        if (StringUtils.isNotBlank(request.getMobile())) {
            checkExist(UserEntity::getEmail, request.getEmail(), null, "邮箱已被占用");
        }

        UserEntity insertUser = new UserEntity();
        insertUser.setOrgId(request.getOrgId());
        insertUser.setUsername(request.getUsername());
        insertUser.setRealName(request.getRealName());
        insertUser.setPassword(passwordEncoder.encode(request.getPassword()));
        insertUser.setAvatar(request.getAvatar());
        insertUser.setMobile(request.getMobile());
        insertUser.setEmail(request.getEmail());
        insertUser.setDisable(Boolean.FALSE);
        userMapper.insert(insertUser);

        saveUserRoles(insertUser.getId(), request.getRoleIds());

        log.info("新增用户成功: userId={}, username={}", insertUser.getId(), insertUser.getUsername());
        return insertUser.getId();
    }

    /**
     * 更新用户基础信息
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateBasicInfo(Long id, UserUpdateRequest request) {
        UserEntity entity = userMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("用户不存在");
        }
        if (StringUtils.isNotBlank(request.getMobile()) && !request.getMobile().equals(entity.getMobile())) {
            checkExist(UserEntity::getMobile, request.getMobile(), id, "手机号已被占用");
        }
        if (StringUtils.isNotBlank(request.getEmail()) && !request.getEmail().equals(entity.getEmail())) {
            checkExist(UserEntity::getEmail, request.getEmail(), id, "邮箱已被占用");
        }

        UserEntity updateUser = new UserEntity();
        updateUser.setId(id);
        if (request.getOrgId() != null) {
            entity.setOrgId(request.getOrgId());
        }
        if (request.getRealName() != null) {
            entity.setRealName(request.getRealName());
        }
        if (request.getAvatar() != null) {
            entity.setAvatar(request.getAvatar());
        }
        if (request.getMobile() != null) {
            entity.setMobile(request.getMobile());
        }
        if (request.getEmail() != null) {
            entity.setEmail(request.getEmail());
        }
        if (request.getDisable() != null) {
            entity.setDisable(request.getDisable());
        }
        userMapper.updateById(updateUser);

        if (request.getRoleIds() != null) {
            userRoleMapper.delete(new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, id));
            saveUserRoles(id, request.getRoleIds());
        }

        log.info("更新用户基础信息成功: userId={}", id);
        return true;
    }

    /**
     * 更新用户密码
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean updatePassword(Long id, UserUpdatePasswordRequest request) {
        UserEntity entity = userMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("用户不存在");
        }

        // 校验旧密码
        if (!passwordEncoder.matches(request.getOldPassword(), entity.getPassword())) {
            throw new BusinessException("旧密码不正确");
        }

        // 更新密码
        entity.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userMapper.updateById(entity);
        log.info("更新用户密码成功: userId={}", id);
        return true;
    }

    /**
     * 删除用户
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(Long id) {
        UserEntity entity = userMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("用户不存在");
        }

        // 删除用户-角色关联
        userRoleMapper.delete(new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, id));

        // 删除用户-用户组关联
        userGroupUserMapper.delete(new LambdaQueryWrapper<UserGroupUser>().eq(UserGroupUser::getUserId, id));

        // 删除用户认证信息
        userAuthMapper.delete(new LambdaQueryWrapper<UserAuthEntity>().eq(UserAuthEntity::getUserId, id));

        // 删除用户
        userMapper.deleteById(id);

        log.info("删除用户成功: userId={}", id);
        return true;
    }

    /**
     * 获取用户的第三方绑定列表
     */
    public UserAuthResponse listAuths(Long id) {
        List<UserAuthEntity> authList = userAuthMapper.selectList(
                new LambdaQueryWrapper<UserAuthEntity>().eq(UserAuthEntity::getUserId, id));

        UserAuthResponse response = new UserAuthResponse();
        if (authList.isEmpty()) {
            response.setList(Collections.emptyList());
            return response;
        }

        List<UserAuthResponse.AuthItem> items = authList.stream().map(auth -> {
            UserAuthResponse.AuthItem item = new UserAuthResponse.AuthItem();
            item.setId(auth.getId());
            item.setIdentityType(auth.getIdentityType());
            item.setIdentifier(auth.getIdentifier());
            item.setVerified(auth.getVerified());
            item.setCreateTime(auth.getCreateTime());
            return item;
        }).toList();

        response.setList(items);
        return response;
    }

    /**
     * 为用户绑定第三方登录方式
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createAuth(Long userId, UserAuthCreateRequest request) {
        // 检查是否已存在相同类型和标识的绑定
        if (userAuthMapper.selectCount(new LambdaQueryWrapper<UserAuthEntity>()
                .eq(UserAuthEntity::getUserId, userId)
                .eq(UserAuthEntity::getIdentityType, request.getIdentityType())
                .eq(UserAuthEntity::getIdentifier, request.getIdentifier())) > 0) {
            throw new BusinessException("该第三方账号已绑定");
        }

        UserAuthEntity authEntity = new UserAuthEntity();
        authEntity.setUserId(userId);
        authEntity.setIdentityType(request.getIdentityType());
        authEntity.setIdentifier(request.getIdentifier());
        authEntity.setCredential(request.getCredential());
        authEntity.setVerified(Boolean.TRUE);
        userAuthMapper.insert(authEntity);

        log.info("用户绑定第三方登录成功: userId={}, identityType={}", userId, request.getIdentityType());
        return authEntity.getId();
    }

    /**
     * 解绑第三方登录方式
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteAuth(Long userId, Long authId) {
        UserAuthEntity authEntity = userAuthMapper.selectOne(
                new LambdaQueryWrapper<UserAuthEntity>()
                        .eq(UserAuthEntity::getId, authId)
                        .eq(UserAuthEntity::getUserId, userId));
        if (authEntity == null) {
            throw new BusinessException("绑定信息不存在");
        }

        userAuthMapper.deleteById(authId);
        log.info("用户解绑第三方登录成功: userId={}, authId={}", userId, authId);
        return true;
    }

    /**
     * 根据 ID 查询用户
     */
    public UserEntity getById(Long id) {
        return userMapper.selectById(id);
    }

    public UserEntity getUserByAccount(String account) {
        LambdaQueryWrapper<UserEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserEntity::getDisable, Boolean.FALSE);
        if (Validator.isMobile(account)) {
            queryWrapper.eq(UserEntity::getMobile, account);
        } else if (Validator.isEmail(account)) {
            queryWrapper.eq(UserEntity::getEmail, account);
        } else {
            queryWrapper.eq(UserEntity::getUsername, account);
        }
        queryWrapper.last(BasicConst.SQL_LIMIT_ONE);
        return userMapper.selectOne(queryWrapper);
    }

    public List<String> getAllRoles(Long userId) {
        return userMapper.getAllRoleByUserId(userId);
    }

    public List<String> getAllPermissions(Long userId) {
        return userMapper.getAllPermissionByUserId(userId);
    }

    /**
     * 检查数据是否已存在
     *
     * @param column    要检查的字段
     * @param value     要检查的值
     * @param excludeId 需要排除的ID
     * @param message   错误提示
     */
    private void checkExist(SFunction<UserEntity, ?> column, Object value, Long excludeId, String message) {
        Long count = userMapper.selectCount(new LambdaQueryWrapper<UserEntity>()
                .eq(column, value)
                .ne(excludeId != null, UserEntity::getId, excludeId)
                .last(BasicConst.SQL_LIMIT_ONE));
        if (count != null && count > 0) {
            throw new BusinessException(message);
        }
    }

    /**
     * 保存用户-角色关联
     *
     * @param userId  用户ID
     * @param roleIds 角色ID列表
     */
    private void saveUserRoles(Long userId, List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return;
        }
        for (Long roleId : roleIds) {
            UserRole userRole = new UserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            userRoleMapper.insert(userRole);
        }
        log.info("为用户分配角色: userId={}, roleIds={}", userId, roleIds);
    }
}

package com.junmoyu.iam.service;

import cn.hutool.core.lang.Validator;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.junmoyu.basic.constant.BasicConst;
import com.junmoyu.iam.mapper.UserAuthMapper;
import com.junmoyu.iam.mapper.UserMapper;
import com.junmoyu.iam.model.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * UserService
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final UserAuthMapper userAuthMapper;

    public UserEntity getUserByAccount(String account) {
        LambdaQueryWrapper<UserEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserEntity::getDisable, Boolean.FALSE);
        // 账号类型检测
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
        return userMapper.getAllRoleByUserId(userId);
    }
}


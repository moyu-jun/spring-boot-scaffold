package com.junmoyu.example.security.service;

import com.junmoyu.basic.util.RedisUtils;
import com.junmoyu.example.security.model.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 认证服务 - UUID Session 方案
 * <p>
 * 以 UUID 作为 Access Token，登录时将完整用户信息存入 Redis
 * 每次请求查询一次 Redis 完成认证与权限校验
 * 支持退出、封号、权限变更实时生效
 * </p>
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final RedisUtils redisUtils;

    // 活跃过期时间：30 分钟（每次请求重置）
    private static final long ACTIVE_TTL_MINUTES = 30;
    // 最大生命周期：30 天（即使活跃也会在此后强制登出）
    private static final long MAX_TTL_DAYS = 30;

    /**
     * 用户登录
     *
     * @param account  账号
     * @param password 密码
     * @return 登录响应（包含 accessToken）
     */
    public LoginResponse login(String account, String password) {
        return null;
    }

    /**
     * 退出登录（当前设备）
     */
    public void logout(String accessToken) {
    }

    /**
     * 踢出所有设备（修改密码、封号等场景）
     */
    public void logoutAll(Long userId) {

    }
}
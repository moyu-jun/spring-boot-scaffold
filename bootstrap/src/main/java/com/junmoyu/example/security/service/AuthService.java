package com.junmoyu.example.security.service;

import com.junmoyu.basic.util.RedisUtils;
import com.junmoyu.example.security.model.LoginResponse;
import com.junmoyu.example.security.model.UserSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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
        // 1. 验证用户名密码（伪代码 - 实际应查询数据库）
        // User user = userMapper.selectByAccount(account);
        // if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
        //     throw new BusinessException("用户名或密码错误");
        // }

        // 模拟用户数据
        Long userId = 10001L;

        // 2. 查询角色和权限（伪代码 - 实际应查询数据库）
        // List<String> roles = roleMapper.selectByUserId(userId);
        // List<String> permissions = permissionMapper.selectByUserId(userId);

        // 模拟角色和权限数据
        List<String> roles = List.of("ADMIN", "MANAGER");
        List<String> permissions = List.of("user:read", "user:write", "user:delete");

        // 3. 构建用户会话数据
        UserSession session = new UserSession(userId, account, roles, permissions);

        // 4. 生成 UUID Token
        String accessToken = UUID.randomUUID().toString().replace("-", "");

        // 5. 存储会话数据（活跃过期时间）
        redisUtils.set("auth:token:" + accessToken, session, ACTIVE_TTL_MINUTES, TimeUnit.MINUTES);

        // 6. 记录到用户的 sessions 集合（最大生命周期）
        // 注意：由于 RedisUtils 没有 sAdd 方法，这里使用 Set 操作
        String sessionsKey = "auth:sessions:" + userId;
        Set<String> existingSessions = redisUtils.getSet(sessionsKey, String.class);
        if (existingSessions == null || existingSessions.isEmpty()) {
            existingSessions = new HashSet<>();
        }
        existingSessions.add(accessToken);
        redisUtils.setSet(sessionsKey, existingSessions);
        redisUtils.expire(sessionsKey, MAX_TTL_DAYS, TimeUnit.DAYS);

        return new LoginResponse(accessToken);
    }

    /**
     * 退出登录（当前设备）
     */
    public void logout(String accessToken) {
        UserSession session = redisUtils.get("auth:token:" + accessToken, UserSession.class);
        if (session != null) {
            redisUtils.delete("auth:token:" + accessToken);

            String sessionsKey = "auth:sessions:" + session.userId();
            Set<String> existingSessions = redisUtils.getSet(sessionsKey, String.class);
            existingSessions.remove(accessToken);
            if (existingSessions.isEmpty()) {
                redisUtils.delete(sessionsKey);
            } else {
                redisUtils.setSet(sessionsKey, existingSessions);
            }
        }
    }

    /**
     * 踢出所有设备（修改密码、封号等场景）
     */
    public void logoutAll(Long userId) {
        String sessionsKey = "auth:sessions:" + userId;
        Set<String> tokens = redisUtils.getSet(sessionsKey, String.class);
        if (tokens != null && !tokens.isEmpty()) {
            tokens.forEach(token -> redisUtils.delete("auth:token:" + token));
        }
        redisUtils.delete(sessionsKey);
    }

    /**
     * 管理员更新用户权限后，刷新 Redis 中的会话数据（实时生效）
     */
    public void refreshUserSessions(Long userId) {
        String sessionsKey = "auth:sessions:" + userId;
        Set<String> tokens = redisUtils.getSet(sessionsKey, String.class);
        if (tokens == null || tokens.isEmpty()) {
            return;
        }

        // 查询最新的角色和权限（伪代码）
        // List<String> roles = roleMapper.selectByUserId(userId);
        // List<String> permissions = permissionMapper.selectByUserId(userId);
        List<String> roles = List.of("USER");
        List<String> permissions = List.of("user:read");

        for (String token : tokens) {
            String tokenKey = "auth:token:" + token;
            UserSession session = redisUtils.get(tokenKey, UserSession.class);
            if (session != null) {
                UserSession updated = session.withRolesAndPermissions(roles, permissions);
                long remainingTtl = redisUtils.getExpire(tokenKey, TimeUnit.MINUTES);
                if (remainingTtl > 0) {
                    redisUtils.set(tokenKey, updated, remainingTtl, TimeUnit.MINUTES);
                }
            }
        }
    }
}
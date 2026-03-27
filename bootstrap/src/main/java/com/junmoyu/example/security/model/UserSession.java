package com.junmoyu.example.security.model;

import java.io.Serializable;
import java.util.List;

/**
 * 存储在 Redis 中的用户会话数据
 * <p>
 * 使用 Java 21 record 实现不可变对象
 * </p>
 */
public record UserSession(
        Long userId,
        String account,
        List<String> roles,
        List<String> permissions
) implements Serializable {

    /**
     * 更新角色和权限（用于权限变更场景）
     */
    public UserSession withRolesAndPermissions(List<String> roles, List<String> permissions) {
        return new UserSession(userId, account, roles, permissions);
    }
}

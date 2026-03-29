package com.junmoyu.security.core;

import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collection;

/**
 * 权限校验服务
 * <p>
 * 提供角色和权限的校验方法，支持 SpEL 表达式调用
 */
@Service
public class AuthorizeService {

    /**
     * 用户已登录（非匿名）
     *
     * @return 是否已登录
     */
    public boolean isAuthenticated() {
        return SecurityContext.isAuthenticated();
    }

    /**
     * 判断是否为指定用户
     *
     * @param userId 用户 ID
     * @return 是否为指定用户
     */
    public boolean isUser(Long userId) {
        Long currentUserId = SecurityContext.getUserId();
        return currentUserId != null && currentUserId.equals(userId);
    }

    /**
     * 判断是否拥有指定角色
     *
     * @param role 角色标识
     * @return 是否拥有该角色
     */
    public boolean hasRole(String role) {
        Collection<String> roles = SecurityContext.getAuthentication().roles();
        return roles != null && roles.contains(role);
    }

    /**
     * 判断是否拥有任意一个指定角色
     *
     * @param roles 角色标识数组
     * @return 是否拥有任意一个角色
     */
    public boolean hasAnyRole(String... roles) {
        Collection<String> userRoles = SecurityContext.getAuthentication().roles();
        if (userRoles == null || userRoles.isEmpty()) {
            return false;
        }
        return Arrays.stream(roles).anyMatch(userRoles::contains);
    }

    /**
     * 判断是否拥有所有指定角色
     *
     * @param roles 角色标识数组
     * @return 是否拥有所有角色
     */
    public boolean hasAllRoles(String... roles) {
        Collection<String> userRoles = SecurityContext.getAuthentication().roles();
        if (userRoles == null || userRoles.isEmpty()) {
            return false;
        }
        return Arrays.stream(roles).allMatch(userRoles::contains);
    }

    /**
     * 判断是否拥有指定权限
     *
     * @param permission 权限标识
     * @return 是否拥有该权限
     */
    public boolean hasAuthority(String permission) {
        Collection<String> authorities = SecurityContext.getAuthentication().authorities();
        return authorities != null && authorities.contains(permission);
    }

    /**
     * 判断是否拥有任意一个指定权限
     *
     * @param permissions 权限标识数组
     * @return 是否拥有任意一个权限
     */
    public boolean hasAnyAuthority(String... permissions) {
        Collection<String> authorities = SecurityContext.getAuthentication().authorities();
        if (authorities == null || authorities.isEmpty()) {
            return false;
        }
        return Arrays.stream(permissions).anyMatch(authorities::contains);
    }

    /**
     * 判断是否拥有所有指定权限
     *
     * @param permissions 权限标识数组
     * @return 是否拥有所有权限
     */
    public boolean hasAllAuthorities(String... permissions) {
        Collection<String> authorities = SecurityContext.getAuthentication().authorities();
        if (authorities == null || authorities.isEmpty()) {
            return false;
        }
        return Arrays.stream(permissions).allMatch(authorities::contains);
    }


    /**
     * 判断是否拥有指定权限
     *
     * @param permission 权限标识
     * @return 是否拥有该权限
     */
    public boolean hasPermission(String permission) {
        Collection<String> authorities = SecurityContext.getAuthentication().authorities();
        return authorities != null && authorities.contains(permission);
    }

    /**
     * 判断是否拥有任意一个指定权限
     *
     * @param permissions 权限标识数组
     * @return 是否拥有任意一个权限
     */
    public boolean hasAnyPermission(String... permissions) {
        Collection<String> authorities = SecurityContext.getAuthentication().authorities();
        if (authorities == null || authorities.isEmpty()) {
            return false;
        }
        return Arrays.stream(permissions).anyMatch(authorities::contains);
    }

    /**
     * 判断是否拥有所有指定权限
     *
     * @param permissions 权限标识数组
     * @return 是否拥有所有权限
     */
    public boolean hasAllPermissions(String... permissions) {
        Collection<String> authorities = SecurityContext.getAuthentication().authorities();
        if (authorities == null || authorities.isEmpty()) {
            return false;
        }
        return Arrays.stream(permissions).allMatch(authorities::contains);
    }
}

package com.junmoyu.example.security.controller;

import com.junmoyu.basic.model.R;
import com.junmoyu.security.annotation.PreAuthorize;
import com.junmoyu.security.core.SecurityContext;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户控制器 - 演示权限控制
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    /**
     * 获取用户列表 - 需要 ADMIN 角色
     */
    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    public R<Object> list() {
        return R.success(Map.of(
                "message", "用户列表",
                "currentUser", SecurityContext.getAuthentication()
        ));
    }

    /**
     * 删除用户 - 需要 user:delete 权限
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasPermission('user:delete')")
    public R<Void> delete(@PathVariable Long id) {
        // 伪代码：userService.removeById(id);
        return R.success();
    }

    /**
     * 删除全部用户 - 需要 user:delete:all 权限
     * 测试无权限的情况
     */
    @DeleteMapping()
    @PreAuthorize("hasPermission('user:delete:all')")
    public R<Void> deleteAll() {
        // 伪代码：userService.removeAll();
        return R.success();
    }

    /**
     * 创建用户 - 需要 ADMIN 或 MANAGER 角色
     */
    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public R<Void> create(@RequestBody Map<String, Object> user) {
        // 伪代码：userService.save(user);
        return R.success();
    }

    /**
     * 获取用户资料 - 管理员或本人
     */
    @GetMapping("/{userId}/profile")
    @PreAuthorize("hasRole('ADMIN') or isUser(#userId)")
    public R<Object> getProfile(@PathVariable Long userId) {
        return R.success(Map.of(
                "userId", userId,
                "message", "用户资料"
        ));
    }

    /**
     * 获取当前用户信息 - 需要已认证
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public R<Object> getCurrentUser() {
        return R.success(SecurityContext.getAuthentication());
    }
}

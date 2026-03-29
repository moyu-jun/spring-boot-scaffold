package com.junmoyu.example.security.controller;

import com.junmoyu.basic.constant.BasicConst;
import com.junmoyu.basic.model.R;
import com.junmoyu.example.security.model.LoginRequest;
import com.junmoyu.example.security.model.LoginResponse;
import com.junmoyu.example.security.service.AuthService;
import com.junmoyu.security.annotation.PreAuthorize;
import com.junmoyu.security.core.SecurityContext;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public R<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request.account(), request.password());
        return R.success(response);
    }

    /**
     * 退出登录
     */
    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public R<Void> logout(@RequestHeader(BasicConst.HEADER_AUTHORIZATION) String authorization) {
        String token = authorization.replace(BasicConst.TOKEN_PREFIX, "");
        authService.logout(token);
        return R.success();
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public R<Object> getCurrentUser() {
        return R.success(SecurityContext.getAuthentication());
    }

    /**
     * 踢出所有设备（管理员操作）
     */
    @PostMapping("/logout-all/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public R<Void> logoutAll(@PathVariable Long userId) {
        authService.logoutAll(userId);
        return R.success();
    }
}

package com.junmoyu.iam.controller;

import com.junmoyu.basic.model.PageResult;
import com.junmoyu.basic.model.R;
import com.junmoyu.basic.model.SearchPageQuery;
import com.junmoyu.iam.model.request.UserAuthCreateRequest;
import com.junmoyu.iam.model.request.UserCreateRequest;
import com.junmoyu.iam.model.request.UserUpdatePasswordRequest;
import com.junmoyu.iam.model.request.UserUpdateRequest;
import com.junmoyu.iam.model.response.UserAuthResponse;
import com.junmoyu.iam.model.response.UserDetailResponse;
import com.junmoyu.iam.model.response.UserResponse;
import com.junmoyu.iam.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理接口
 */
@Tag(name = "用户管理接口")
@RestController
@RequestMapping("users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping()
    @Operation(summary = "分页查询用户列表")
    public R<PageResult<UserResponse>> page(SearchPageQuery query) {
        return R.success(userService.page(query));
    }

    @GetMapping("{id}")
    @Operation(summary = "获取用户详情")
    public R<UserDetailResponse> detail(@PathVariable Long id) {
        return R.success(userService.detail(id));
    }

    @PostMapping()
    @Operation(summary = "新增用户")
    public R<Long> create(@Valid @RequestBody UserCreateRequest request) {
        return R.success(userService.create(request));
    }

    @PutMapping("{id}")
    @Operation(summary = "更新基础用户信息")
    public R<Boolean> updateBacisInfo(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
        return R.success(userService.updateBasicInfo(id, request));
    }

    @PutMapping("{id}/password")
    @Operation(summary = "更新用户密码")
    public R<Boolean> updatePassword(@PathVariable Long id, @Valid @RequestBody UserUpdatePasswordRequest request) {
        return R.success(userService.updatePassword(id, request));
    }

    @DeleteMapping("{id}")
    @Operation(summary = "删除用户")
    public R<Boolean> delete(@PathVariable Long id) {
        return R.success(userService.delete(id));
    }

    @GetMapping("{id}/auths")
    @Operation(summary = "获取用户的第三方绑定列表")
    public R<UserAuthResponse> listAuths(@PathVariable Long id) {
        return R.success(userService.listAuths(id));
    }

    @PostMapping("{id}/auths")
    @Operation(summary = "为用户绑定第三方登录方式")
    public R<Long> createAuth(@PathVariable Long id, @Valid @RequestBody UserAuthCreateRequest request) {
        return R.success(userService.createAuth(id, request));
    }

    @DeleteMapping("{id}/auths/{authId}")
    @Operation(summary = "解绑第三方登录方式")
    public R<Boolean> deleteAuth(@PathVariable Long id, @PathVariable Long authId) {
        return R.success(userService.deleteAuth(id, authId));
    }
}

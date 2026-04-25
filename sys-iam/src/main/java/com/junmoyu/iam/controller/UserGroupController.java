package com.junmoyu.iam.controller;

import com.junmoyu.basic.model.PageResult;
import com.junmoyu.basic.model.R;
import com.junmoyu.basic.model.SearchPageQuery;
import com.junmoyu.iam.model.request.UserGroupCreateUpdateRequest;
import com.junmoyu.iam.model.request.UserGroupUpdateUserRequest;
import com.junmoyu.iam.model.response.UserGroupResponse;
import com.junmoyu.iam.model.response.UserResponse;
import com.junmoyu.iam.service.UserGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户组管理接口
 */
@Tag(name = "用户组管理接口")
@RestController
@RequestMapping("user-groups")
@RequiredArgsConstructor
public class UserGroupController {

    private final UserGroupService userGroupService;

    @GetMapping()
    @Operation(summary = "分页查询用户组列表")
    public R<PageResult<UserGroupResponse>> page(SearchPageQuery query) {
        return R.success(userGroupService.page(query));
    }

    @PostMapping()
    @Operation(summary = "新增用户组（包含角色列表）")
    public R<Long> create(@Valid @RequestBody UserGroupCreateUpdateRequest request) {
        return R.success(userGroupService.create(request));
    }

    @PutMapping("{id}")
    @Operation(summary = "更新用户组（包含角色列表）")
    public R<Boolean> update(@PathVariable Long id, @Valid @RequestBody UserGroupCreateUpdateRequest request) {
        return R.success(userGroupService.update(id, request));
    }

    @DeleteMapping("{id}")
    @Operation(summary = "删除用户组")
    public R<Boolean> delete(@PathVariable Long id) {
        return R.success(userGroupService.delete(id));
    }

    @GetMapping("{id}/users")
    @Operation(summary = "获取该用户组下的成员列表")
    public R<PageResult<UserResponse>> pageUsers(@PathVariable Long id) {
        return R.success(userGroupService.pageUsers(id));
    }

    @PutMapping("{id}/users")
    @Operation(summary = "管理用户组成员（添加/移除用户）")
    public R<Boolean> updateUsers(@PathVariable Long id, @Valid @RequestBody UserGroupUpdateUserRequest request) {
        return R.success(userGroupService.updateUsers(id, request));
    }
}

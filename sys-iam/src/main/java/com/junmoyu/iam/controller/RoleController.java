package com.junmoyu.iam.controller;

import com.junmoyu.basic.model.PageResult;
import com.junmoyu.basic.model.R;
import com.junmoyu.basic.model.SearchPageQuery;
import com.junmoyu.iam.model.request.RoleCreateUpdateRequest;
import com.junmoyu.iam.model.request.RoleUpdatePermissionRequest;
import com.junmoyu.iam.model.response.RoleResponse;
import com.junmoyu.iam.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 角色管理接口
 */
@Tag(name = "角色管理接口")
@RestController
@RequestMapping("roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping()
    @Operation(summary = "分页/列表查询角色列表")
    public R<PageResult<RoleResponse>> page(SearchPageQuery query) {
        return R.success(roleService.page(query));
    }

    @PostMapping()
    @Operation(summary = "新增角色")
    public R<Long> create(@Valid @RequestBody RoleCreateUpdateRequest request) {
        return R.success(roleService.create(request));
    }

    @PutMapping("{id}")
    @Operation(summary = "修改角色")
    public R<Boolean> update(@PathVariable Long id, @Valid @RequestBody RoleCreateUpdateRequest request) {
        return R.success(roleService.update(id, request));
    }

    @DeleteMapping("{id}")
    @Operation(summary = "删除角色")
    public R<Boolean> delete(@PathVariable Long id) {
        return R.success(roleService.delete(id));
    }

    @PutMapping("{id}/permissions")
    @Operation(summary = "为角色分配权限资源")
    public R<Boolean> updatePermissions(@PathVariable Long id, @Valid @RequestBody RoleUpdatePermissionRequest request) {
        return R.success(roleService.updatePermissions(id, request));
    }
}

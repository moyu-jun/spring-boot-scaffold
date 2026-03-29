package com.junmoyu.iam.controller;

import com.junmoyu.basic.model.PageResult;
import com.junmoyu.basic.model.R;
import com.junmoyu.basic.model.SearchPageQuery;
import com.junmoyu.iam.model.request.RoleCreateUpdateRequest;
import com.junmoyu.iam.model.request.RoleUpdatePermissionRequest;
import com.junmoyu.iam.model.response.RoleResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

    @GetMapping()
    @Operation(summary = "分页/列表查询角色列表")
    public R<PageResult<RoleResponse>> page(SearchPageQuery query) {
        return R.success();
    }

    @PostMapping()
    @Operation(summary = "新增角色")
    public R<Long> create(@RequestBody RoleCreateUpdateRequest request) {
        return R.success();
    }

    @PutMapping("{id}")
    @Operation(summary = "修改角色")
    public R<Boolean> update(@PathVariable Long id, @RequestBody RoleCreateUpdateRequest request) {
        return R.success();
    }

    @DeleteMapping("{id}")
    @Operation(summary = "删除角色")
    public R<Boolean> delete(@PathVariable Long id) {
        return R.success();
    }

    @PutMapping("{id}/permissions")
    @Operation(summary = "为角色分配权限资源")
    public R<Boolean> updatePermissions(@PathVariable Long id, @RequestBody RoleUpdatePermissionRequest request) {
        return R.success();
    }
}

package com.junmoyu.iam.controller;

import com.junmoyu.basic.model.R;
import com.junmoyu.iam.model.request.PermissionCreateUpdateRequest;
import com.junmoyu.iam.model.response.PermissionTreeNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 权限资源管理接口
 */
@Tag(name = "权限资源管理接口")
@RestController
@RequestMapping("permissions")
@RequiredArgsConstructor
public class PermissionController {

    @GetMapping("tree")
    @Operation(summary = "获取全量权限/菜单资源树")
    public R<List<PermissionTreeNode>> tree() {
        return R.success();
    }

    @PostMapping()
    @Operation(summary = "新增权限资源（目录、菜单、按钮/API）")
    public R<Long> create(@RequestBody PermissionCreateUpdateRequest request) {
        return R.success();
    }

    @PutMapping("{id}")
    @Operation(summary = "更新权限资源")
    public R<Boolean> update(@PathVariable Long id, @RequestBody PermissionCreateUpdateRequest request) {
        return R.success();
    }

    @DeleteMapping("{id}")
    @Operation(summary = "删除权限资源")
    public R<Boolean> delete(@PathVariable Long id) {
        return R.success();
    }
}

package com.junmoyu.iam.controller;

import com.junmoyu.basic.model.PageResult;
import com.junmoyu.basic.model.R;
import com.junmoyu.basic.model.SearchPageQuery;
import com.junmoyu.iam.model.request.OrgCreateUpdateRequest;
import com.junmoyu.iam.model.response.OrgTreeNode;
import com.junmoyu.iam.model.response.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 组织管理接口
 */
@Tag(name = "组织管理接口")
@RestController
@RequestMapping("orgs")
@RequiredArgsConstructor
public class OrgController {

    @GetMapping("tree")
    @Operation(summary = "获取完整的组织架构树")
    public R<List<OrgTreeNode>> tree() {
        return R.success();
    }

    @GetMapping("{id}/users")
    @Operation(summary = "分页获取某组织下的用户列表")
    public R<PageResult<UserResponse>> pageUsers(@PathVariable Long id, SearchPageQuery query) {
        return R.success();
    }

    @PostMapping()
    @Operation(summary = "新增组织架构节点")
    public R<Long> create(@RequestBody OrgCreateUpdateRequest request) {
        return R.success();
    }

    @PutMapping("{id}")
    @Operation(summary = "修改组织架构节点")
    public R<Boolean> update(@PathVariable Long id, @RequestBody OrgCreateUpdateRequest request) {
        return R.success();
    }

    @DeleteMapping("{id}")
    @Operation(summary = "删除组织架构节点")
    public R<Boolean> delete(@PathVariable Long id) {
        return R.success();
    }
}

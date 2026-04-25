package com.junmoyu.iam.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 角色分配权限资源请求
 */
@Data
@Schema(description = "角色分配权限资源请求")
public class RoleUpdatePermissionRequest {

    @Schema(description = "权限资源ID列表")
    private List<Long> permissionIds;
}

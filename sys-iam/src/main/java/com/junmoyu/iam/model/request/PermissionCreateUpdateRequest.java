package com.junmoyu.iam.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 新增/修改权限资源请求
 */
@Data
@Schema(description = "新增/修改权限资源请求")
public class PermissionCreateUpdateRequest {

    @Schema(description = "父级权限ID，顶级为0")
    private Long parentId;

    @Schema(description = "权限/菜单名称")
    @NotBlank(message = "权限名称不能为空")
    private String permName;

    @Schema(description = "权限标识（如：user:add, menu:sys）")
    private String permCode;

    @Schema(description = "权限类型：1-目录，2-菜单，3-按钮/API")
    @NotNull(message = "权限类型不能为空")
    private Byte permType;

    @Schema(description = "路由地址或API路径")
    private String path;

    @Schema(description = "前端图标")
    private String icon;

    @Schema(description = "排序编号")
    private Integer sortNum;
}

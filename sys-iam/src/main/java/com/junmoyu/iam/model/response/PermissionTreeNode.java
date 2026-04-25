package com.junmoyu.iam.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 权限资源树节点
 */
@Data
@Schema(description = "权限资源树节点")
public class PermissionTreeNode {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "父级权限ID，顶级为0")
    private Long parentId;

    @Schema(description = "权限/菜单名称")
    private String permName;

    @Schema(description = "权限标识")
    private String permCode;

    @Schema(description = "权限类型：1-目录，2-菜单，3-按钮/API")
    private Byte permType;

    @Schema(description = "路由地址或API路径")
    private String path;

    @Schema(description = "前端图标")
    private String icon;

    @Schema(description = "排序编号")
    private Integer sortNum;

    @Schema(description = "禁用状态：0-未禁用，1-已禁用")
    private Boolean disable;

    @Schema(description = "子级权限列表")
    private List<PermissionTreeNode> children;
}

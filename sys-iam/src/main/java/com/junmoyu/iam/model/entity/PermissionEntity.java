package com.junmoyu.iam.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.junmoyu.basic.model.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 权限资源表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_permission")
@Schema(description = "权限资源表")
public class PermissionEntity extends BaseEntity implements Serializable {

    @TableField(value = "parent_id")
    @Schema(description = "父级权限ID，顶级为0")
    private Long parentId;

    @TableField(value = "perm_name")
    @Schema(description = "权限/菜单名称")
    private String permName;

    @TableField(value = "perm_code")
    @Schema(description = "权限标识（如：user:add, menu:sys）")
    private String permCode;

    @TableField(value = "perm_type")
    @Schema(description = "权限类型：1-目录，2-菜单，3-按钮/API")
    private Byte permType;

    @TableField(value = "`path`")
    @Schema(description = "路由地址或API路径")
    private String path;

    @TableField(value = "icon")
    @Schema(description = "前端图标")
    private String icon;

    @TableField(value = "sort_num")
    @Schema(description = "排序编号")
    private Integer sortNum;

    @TableField(value = "`disable`")
    @Schema(description = "禁用状态：0-未禁用，1-已禁用")
    private Boolean disable;
}
package com.junmoyu.iam.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.junmoyu.basic.model.BaseIdEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 角色-权限关联表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_role_permission")
@Schema(description = "角色-权限关联表")
public class RolePermission extends BaseIdEntity implements Serializable {

    @TableField(value = "role_id")
    @Schema(description = "角色ID")
    private Long roleId;

    @TableField(value = "permission_id")
    @Schema(description = "权限ID")
    private Long permissionId;
}
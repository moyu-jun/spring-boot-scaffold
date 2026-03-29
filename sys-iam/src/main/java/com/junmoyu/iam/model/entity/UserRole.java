package com.junmoyu.iam.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.junmoyu.basic.model.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 用户-角色关联表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_user_role")
@Schema(description = "用户-角色关联表")
public class UserRole extends BaseEntity implements Serializable {

    @TableField(value = "user_id")
    @Schema(description = "用户ID")
    private Long userId;

    @TableField(value = "role_id")
    @Schema(description = "角色ID")
    private Long roleId;
}
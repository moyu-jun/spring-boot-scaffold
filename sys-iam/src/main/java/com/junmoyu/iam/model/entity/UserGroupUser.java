package com.junmoyu.iam.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.junmoyu.basic.model.BaseIdEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 用户组-用户关联表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_user_group_user")
@Schema(description = "用户组-用户关联表")
public class UserGroupUser extends BaseIdEntity implements Serializable {

    @TableField(value = "group_id")
    @Schema(description = "用户组ID")
    private Long groupId;

    @TableField(value = "user_id")
    @Schema(description = "用户ID")
    private Long userId;
}
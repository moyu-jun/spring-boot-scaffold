package com.junmoyu.iam.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.junmoyu.basic.model.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 用户基础信息表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_user")
@Schema(description = "用户基础信息表")
public class UserEntity extends BaseEntity implements Serializable {

    @TableField(value = "org_id")
    @Schema(description = "所属主组织架构ID")
    private Long orgId;

    @TableField(value = "username")
    @Schema(description = "系统用户名/登录名（要求唯一）")
    private String username;

    @TableField(value = "real_name")
    @Schema(description = "真实姓名/昵称")
    private String realName;

    @TableField(value = "avatar")
    @Schema(description = "用户头像URL")
    private String avatar;

    @TableField(value = "phone")
    @Schema(description = "手机号码")
    private String phone;

    @TableField(value = "email")
    @Schema(description = "邮箱地址")
    private String email;

    @TableField(value = "`disable`")
    @Schema(description = "禁用状态：0-未禁用，1-已禁用")
    private Boolean disable;
}
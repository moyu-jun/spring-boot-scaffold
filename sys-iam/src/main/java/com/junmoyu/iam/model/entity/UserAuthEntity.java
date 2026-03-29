package com.junmoyu.iam.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.junmoyu.basic.model.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 用户认证及第三方登录表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_user_auth")
@Schema(description = "用户认证及第三方登录表")
public class UserAuthEntity extends BaseEntity implements Serializable {

    @TableField(value = "user_id")
    @Schema(description = "系统用户ID")
    private Long userId;

    @TableField(value = "identity_type")
    @Schema(description = "认证类型：password-密码，wechat-微信，github-GitHub，phone-手机等")
    private String identityType;

    @TableField(value = "identifier")
    @Schema(description = "认证标识：如用户名、手机号、第三方应用的OpenID")
    private String identifier;

    @TableField(value = "credential")
    @Schema(description = "密码凭证：密码的哈希值、第三方的Access_Token等")
    private String credential;

    @TableField(value = "verified")
    @Schema(description = "是否已验证：0-未验证，1-已验证")
    private Boolean verified;
}
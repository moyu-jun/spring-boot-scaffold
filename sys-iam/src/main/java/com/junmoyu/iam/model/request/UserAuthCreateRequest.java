package com.junmoyu.iam.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 用户绑定第三方登录方式请求
 */
@Data
@Schema(description = "用户绑定第三方登录方式请求")
public class UserAuthCreateRequest {

    @Schema(description = "认证类型：wechat-微信，github-GitHub，phone-手机等")
    @NotBlank(message = "认证类型不能为空")
    private String identityType;

    @Schema(description = "认证标识：如第三方应用的OpenID")
    @NotBlank(message = "认证标识不能为空")
    private String identifier;

    @Schema(description = "密码凭证：第三方的Access_Token等")
    private String credential;
}

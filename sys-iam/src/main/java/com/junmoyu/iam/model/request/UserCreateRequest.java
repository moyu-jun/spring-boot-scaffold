package com.junmoyu.iam.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * 新增用户请求
 */
@Data
@Schema(description = "新增用户请求")
public class UserCreateRequest {

    @Schema(description = "所属主组织架构ID")
    private Long orgId;

    @Schema(description = "系统用户名/登录名（要求唯一）")
    @NotBlank(message = "用户名不能为空")
    private String username;

    @Schema(description = "真实姓名/昵称")
    private String realName;

    @Schema(description = "密码")
    @NotBlank(message = "密码不能为空")
    private String password;

    @Schema(description = "用户头像URL")
    private String avatar;

    @Schema(description = "手机号码")
    private String mobile;

    @Schema(description = "邮箱地址")
    private String email;

    @Schema(description = "角色ID列表")
    private List<Long> roleIds;
}

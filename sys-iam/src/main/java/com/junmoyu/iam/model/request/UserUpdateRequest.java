package com.junmoyu.iam.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 更新用户信息请求
 */
@Data
@Schema(description = "更新用户信息请求")
public class UserUpdateRequest {

    @Schema(description = "所属主组织架构ID")
    private Long orgId;

    @Schema(description = "真实姓名/昵称")
    private String realName;

    @Schema(description = "用户头像URL")
    private String avatar;

    @Schema(description = "手机号码")
    private String mobile;

    @Schema(description = "邮箱地址")
    private String email;

    @Schema(description = "禁用状态：0-未禁用，1-已禁用")
    private Boolean disable;

    @Schema(description = "角色ID列表（null 不更新，空列表清空）")
    private List<Long> roleIds;
}

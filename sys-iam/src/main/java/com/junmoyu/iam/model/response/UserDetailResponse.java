package com.junmoyu.iam.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 用户详情响应
 */
@Data
@Schema(description = "用户详情响应")
public class UserDetailResponse {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "所属主组织架构ID")
    private Long orgId;

    @Schema(description = "所属组织名称")
    private String orgName;

    @Schema(description = "系统用户名/登录名")
    private String username;

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

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "更新时间")
    private Date updateTime;

    @Schema(description = "已分配角色ID列表")
    private List<Long> roleIds;

    @Schema(description = "所属用户组ID列表")
    private List<Long> groupIds;
}

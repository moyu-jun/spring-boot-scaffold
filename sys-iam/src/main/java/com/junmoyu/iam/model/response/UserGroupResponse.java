package com.junmoyu.iam.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 用户组列表响应
 */
@Data
@Schema(description = "用户组列表响应")
public class UserGroupResponse {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "用户组名称")
    private String groupName;

    @Schema(description = "用户组编码")
    private String groupCode;

    @Schema(description = "禁用状态：0-未禁用，1-已禁用")
    private Boolean disable;

    @Schema(description = "备注说明")
    private String remark;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "更新时间")
    private Date updateTime;
}

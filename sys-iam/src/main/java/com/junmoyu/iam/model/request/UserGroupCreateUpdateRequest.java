package com.junmoyu.iam.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * 新增/修改用户组请求
 */
@Data
@Schema(description = "新增/修改用户组请求")
public class UserGroupCreateUpdateRequest {

    @Schema(description = "用户组名称")
    @NotBlank(message = "用户组名称不能为空")
    private String groupName;

    @Schema(description = "用户组编码")
    private String groupCode;

    @Schema(description = "备注说明")
    private String remark;

    @Schema(description = "角色ID列表")
    private List<Long> roleIds;
}

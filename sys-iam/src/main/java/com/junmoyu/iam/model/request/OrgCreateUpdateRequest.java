package com.junmoyu.iam.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 新增/修改组织架构请求
 */
@Data
@Schema(description = "新增/修改组织架构请求")
public class OrgCreateUpdateRequest {

    @Schema(description = "父级组织ID，顶级为0")
    private Long parentId;

    @Schema(description = "组织/部门名称")
    @NotBlank(message = "组织名称不能为空")
    private String orgName;

    @Schema(description = "组织编码")
    @NotBlank(message = "组织名称不能为空")
    private String orgCode;

    @Schema(description = "排序编号（升序）")
    private Integer sortNum;

    @Schema(description = "备注说明")
    private String remark;
}

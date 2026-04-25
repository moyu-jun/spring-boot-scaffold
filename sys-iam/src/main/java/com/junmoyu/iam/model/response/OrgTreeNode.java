package com.junmoyu.iam.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 组织架构树节点
 */
@Data
@Schema(description = "组织架构树节点")
public class OrgTreeNode {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "父级组织ID，顶级为0")
    private Long parentId;

    @Schema(description = "组织/部门名称")
    private String orgName;

    @Schema(description = "组织编码")
    private String orgCode;

    @Schema(description = "排序编号")
    private Integer sortNum;

    @Schema(description = "禁用状态：0-未禁用，1-已禁用")
    private Boolean disable;

    @Schema(description = "备注说明")
    private String remark;

    @Schema(description = "子级组织列表")
    private List<OrgTreeNode> children;
}

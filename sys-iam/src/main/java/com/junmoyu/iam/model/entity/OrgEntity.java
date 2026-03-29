package com.junmoyu.iam.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.junmoyu.basic.model.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 组织架构表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_org")
@Schema(description = "组织架构表")
public class OrgEntity extends BaseEntity implements Serializable {

    @TableField(value = "parent_id")
    @Schema(description = "父级组织ID，顶级为0")
    private Long parentId;

    @TableField(value = "org_name")
    @Schema(description = "组织/部门名称")
    private String orgName;

    @TableField(value = "org_code")
    @Schema(description = "组织编码")
    private String orgCode;

    @TableField(value = "sort_num")
    @Schema(description = "排序编号（升序）")
    private Integer sortNum;

    @TableField(value = "`disable`")
    @Schema(description = "禁用状态：0-未禁用，1-已禁用")
    private Boolean disable;

    @TableField(value = "remark")
    @Schema(description = "备注说明")
    private String remark;
}
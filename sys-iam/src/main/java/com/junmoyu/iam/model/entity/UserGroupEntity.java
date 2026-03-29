package com.junmoyu.iam.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.junmoyu.basic.model.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 用户组表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_user_group")
@Schema(description = "用户组表")
public class UserGroupEntity extends BaseEntity implements Serializable {

    @TableField(value = "group_name")
    @Schema(description = "用户组名称")
    private String groupName;

    @TableField(value = "group_code")
    @Schema(description = "用户组编码")
    private String groupCode;

    @TableField(value = "`disable`")
    @Schema(description = "禁用状态：0-未禁用，1-已禁用")
    private Boolean disable;

    @TableField(value = "remark")
    @Schema(description = "备注说明")
    private String remark;
}
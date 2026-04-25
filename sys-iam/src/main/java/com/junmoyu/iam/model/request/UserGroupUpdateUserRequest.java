package com.junmoyu.iam.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 管理用户组成员请求
 */
@Data
@Schema(description = "管理用户组成员请求")
public class UserGroupUpdateUserRequest {

    @Schema(description = "用户ID列表")
    private List<Long> userIds;
}

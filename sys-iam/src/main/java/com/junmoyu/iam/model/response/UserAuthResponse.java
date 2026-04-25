package com.junmoyu.iam.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 用户第三方绑定列表响应
 */
@Data
@Schema(description = "用户第三方绑定列表响应")
public class UserAuthResponse {

    @Schema(description = "第三方绑定列表")
    private List<AuthItem> list;

    @Data
    @Schema(description = "第三方认证信息")
    public static class AuthItem {

        @Schema(description = "认证ID")
        private Long id;

        @Schema(description = "认证类型")
        private String identityType;

        @Schema(description = "认证标识")
        private String identifier;

        @Schema(description = "是否已验证")
        private Boolean verified;

        @Schema(description = "创建时间")
        private Date createTime;
    }
}

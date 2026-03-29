package com.junmoyu.iam.model.dto;

import java.util.List;

/**
 * UserPermissionCache
 */
public record UserPermissionCache(
        Long userId,
        String username,
        Long orgId,
        List<String> roles,
        List<String> permissions,
        Integer permVer,
        Long updatedAt
) {
}

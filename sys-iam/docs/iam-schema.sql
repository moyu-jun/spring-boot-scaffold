-- ----------------------------
-- 组织架构表 (Organization)
-- ----------------------------
DROP TABLE IF EXISTS `sys_org`;
CREATE TABLE `sys_org`  (
  `id` bigint UNSIGNED NOT NULL COMMENT '主键ID',
  `parent_id` bigint UNSIGNED NOT NULL DEFAULT 0 COMMENT '父级组织ID，顶级为0',
  `org_name` varchar(100) NOT NULL COMMENT '组织/部门名称',
  `org_code` varchar(50) NOT NULL COMMENT '组织编码',
  `sort_num` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '排序编号（升序）',
  `disable` tinyint UNSIGNED NOT NULL DEFAULT 0 COMMENT '禁用状态：0-未禁用，1-已禁用',
  `remark` varchar(255) NULL DEFAULT NULL COMMENT '备注说明',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '组织架构表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 用户基础信息表 (User)
-- ----------------------------
-- 账号认证与基础信息分离，此处只存用户基础画像数据
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` bigint UNSIGNED NOT NULL COMMENT '主键ID',
  `org_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '所属主组织架构ID',
  `username` varchar(50) NOT NULL COMMENT '系统用户名/登录名（要求唯一）',
  `real_name` varchar(50) NULL DEFAULT NULL COMMENT '真实姓名/昵称',
  `avatar` varchar(255) NULL DEFAULT NULL COMMENT '用户头像URL',
  `phone` varchar(20) NULL DEFAULT NULL COMMENT '手机号码（要求唯一）',
  `email` varchar(100) NULL DEFAULT NULL COMMENT '邮箱地址（要求唯一）',
  `disable` tinyint UNSIGNED NOT NULL DEFAULT 0 COMMENT '禁用状态：0-未禁用，1-已禁用',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE,
  UNIQUE INDEX `uk_phone`(`phone` ASC) USING BTREE,
  UNIQUE INDEX `uk_email`(`email` ASC) USING BTREE,
  INDEX `idx_org_id`(`org_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户基础信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 用户认证/第三方登录表 (User Auth)
-- ----------------------------
-- 支持多种登录方式（密码、微信、GitHub、手机验证码等）
DROP TABLE IF EXISTS `sys_user_auth`;
CREATE TABLE `sys_user_auth`  (
  `id` bigint UNSIGNED NOT NULL COMMENT '主键ID',
  `user_id` bigint UNSIGNED NOT NULL COMMENT '系统用户ID',
  `identity_type` varchar(50) NOT NULL COMMENT '认证类型：password-密码，wechat-微信，github-GitHub，phone-手机等',
  `identifier` varchar(100) NOT NULL COMMENT '认证标识：如用户名、手机号、第三方应用的OpenID',
  `credential` varchar(255) NULL DEFAULT NULL COMMENT '密码凭证：密码的哈希值、第三方的Access_Token等',
  `verified` tinyint UNSIGNED NOT NULL DEFAULT 1 COMMENT '是否已验证：0-未验证，1-已验证',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_type_identifier`(`identity_type` ASC, `identifier` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户认证及第三方登录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 角色表 (Role)
-- ----------------------------
-- RBAC1模型支持：通过 parent_id 实现角色继承机制
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `id` bigint UNSIGNED NOT NULL COMMENT '主键ID',
  `role_name` varchar(50) NOT NULL COMMENT '角色名称（如：系统管理员）',
  `role_code` varchar(50) NOT NULL COMMENT '角色标识符（如：SYS_ADMIN）',
  `sort_num` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '排序编号',
  `disable` tinyint UNSIGNED NOT NULL DEFAULT 0 COMMENT '禁用状态：0-未禁用，1-已禁用',
  `remark` varchar(255) NULL DEFAULT NULL COMMENT '角色描述',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_role_code`(`role_code` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统角色表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 权限/资源表 (Permission)
-- ----------------------------
DROP TABLE IF EXISTS `sys_permission`;
CREATE TABLE `sys_permission`  (
  `id` bigint UNSIGNED NOT NULL COMMENT '主键ID',
  `parent_id` bigint UNSIGNED NOT NULL DEFAULT 0 COMMENT '父级权限ID，顶级为0',
  `perm_name` varchar(50) NOT NULL COMMENT '权限/菜单名称',
  `perm_code` varchar(100) NULL DEFAULT NULL COMMENT '权限标识（如：user:add, menu:sys）',
  `perm_type` tinyint UNSIGNED NOT NULL COMMENT '权限类型：1-目录，2-菜单，3-按钮，4-接口API',
  `path` varchar(255) NULL DEFAULT NULL COMMENT '路由地址或API路径',
  `icon` varchar(100) NULL DEFAULT NULL COMMENT '前端图标',
  `sort_num` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '排序编号',
  `disable` tinyint UNSIGNED NOT NULL DEFAULT 0 COMMENT '禁用状态：0-未禁用，1-已禁用',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE,
  INDEX `idx_perm_code`(`perm_code` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '权限资源表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 用户组表 (User Group)
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_group`;
CREATE TABLE `sys_user_group`  (
  `id` bigint UNSIGNED NOT NULL COMMENT '主键ID',
  `group_name` varchar(50) NOT NULL COMMENT '用户组名称',
  `group_code` varchar(50) NULL DEFAULT NULL COMMENT '用户组编码',
  `disable` tinyint UNSIGNED NOT NULL DEFAULT 0 COMMENT '禁用状态：0-未禁用，1-已禁用',
  `remark` varchar(255) NULL DEFAULT NULL COMMENT '备注说明',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_group_code`(`group_code` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户组表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 用户-角色 关联表 (User-Role)
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`  (
  `id` bigint UNSIGNED NOT NULL COMMENT '主键ID',
  `user_id` bigint UNSIGNED NOT NULL COMMENT '用户ID',
  `role_id` bigint UNSIGNED NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_role`(`user_id` ASC, `role_id` ASC) USING BTREE,
  INDEX `idx_role_id`(`role_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户-角色关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 角色-权限 关联表 (Role-Permission)
-- ---------------------------
DROP TABLE IF EXISTS `sys_role_permission`;
CREATE TABLE `sys_role_permission`  (
  `id` bigint UNSIGNED NOT NULL COMMENT '主键ID',
  `role_id` bigint UNSIGNED NOT NULL COMMENT '角色ID',
  `permission_id` bigint UNSIGNED NOT NULL COMMENT '权限ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_role_permission`(`role_id` ASC, `permission_id` ASC) USING BTREE,
  INDEX `idx_permission_id`(`permission_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色-权限关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 用户组-用户 关联表 (UserGroup-User)
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_group_user`;
CREATE TABLE `sys_user_group_user`  (
  `id` bigint UNSIGNED NOT NULL COMMENT '主键ID',
  `group_id` bigint UNSIGNED NOT NULL COMMENT '用户组ID',
  `user_id` bigint UNSIGNED NOT NULL COMMENT '用户ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_group_user`(`group_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户组-用户关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 用户组-角色 关联表 (UserGroup-Role)
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_group_role`;
CREATE TABLE `sys_user_group_role`  (
  `id` bigint UNSIGNED NOT NULL COMMENT '主键ID',
  `group_id` bigint UNSIGNED NOT NULL COMMENT '用户组ID',
  `role_id` bigint UNSIGNED NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_group_role`(`group_id` ASC, `role_id` ASC) USING BTREE,
  INDEX `idx_role_id`(`role_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户组-角色关联表' ROW_FORMAT = Dynamic;

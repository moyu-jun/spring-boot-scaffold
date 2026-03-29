# IAM - 统一用户管理与权限控制系统 

本系统基于 RBAC3 模型构建，支持组织架构管理、用户组分配、角色权限继承以及多渠道第三方登录。

## 一、 接口设计 (API 规范)

接口路径设计遵循 RESTful 风格，去除版本号冗余，围绕核心资源（User, Role, Permission, Group, Org, Auth）展开。

### 1. 认证与授权模块 (Auth)
* `POST /auth/login` : 用户登录（支持密码及第三方账号组合校验）
* `POST /auth/logout` : 用户登出
* `GET /auth/current-user` : 获取当前登录用户信息及权限标识列表

### 2. 用户管理模块 (User)
* `GET /users` : 分页查询用户列表（支持按组织、状态等筛选）
* `GET /users/{id}` : 获取用户详情
* `POST /users` : 新增基础用户
* `PUT /users/{id}` : 更新基础用户信息
* `DELETE /users/{id}` : 删除用户（逻辑删除或物理删除）
* `PUT /users/{id}/roles` : 为用户直接分配角色
* `GET /users/{id}/auths` : 获取用户的第三方绑定列表
* `POST /users/{id}/auths` : 为用户绑定第三方登录方式
* `DELETE /users/{id}/auths/{authId}` : 解绑第三方登录方式

### 3. 组织架构模块 (Organization)
* `GET /orgs/tree` : 获取完整的组织架构树
* `GET /orgs/{id}/users` : 分页获取某组织下的用户列表
* `POST /orgs` : 新增组织架构节点
* `PUT /orgs/{id}` : 修改组织架构节点
* `DELETE /orgs/{id}` : 删除组织架构节点（需校验是否含子节点或用户）

### 4. 角色管理模块 (Role)
* `GET /roles` : 分页/列表查询系统角色
* `POST /roles` : 新增角色（支持指定 `parent_id` 实现继承）
* `PUT /roles/{id}` : 更新角色信息
* `DELETE /roles/{id}` : 删除角色
* `PUT /roles/{id}/permissions` : 为角色分配权限资源

### 5. 权限与菜单模块 (Permission)
* `GET /permissions/tree` : 获取全量权限/菜单资源树
* `POST /permissions` : 新增权限资源（目录、菜单、按钮/API）
* `PUT /permissions/{id}` : 更新权限资源
* `DELETE /permissions/{id}` : 删除权限资源

### 6. 用户组管理模块 (User Group)
* `GET /groups` : 分页查询用户组列表
* `POST /groups` : 新增用户组
* `PUT /groups/{id}` : 更新用户组
* `DELETE /groups/{id}` : 删除用户组
* `GET /groups/{id}/users` : 获取该用户组下的成员列表
* `PUT /groups/{id}/users` : 管理用户组成员（添加/移除用户）
* `GET /groups/{id}/roles` : 获取该用户组绑定的角色列表
* `PUT /groups/{id}/roles` : 管理用户组绑定的角色

---

## 二、 核心业务 SQL 查询设计

在 RBAC3 模型中，权限和角色的来源是多样的（直接绑定、用户组继承等）。以下是核心业务场景涉及的多表关联查询语句。

### 1. 统一登录认证校验
**业务场景**：无论用户使用手机号、微信还是密码登录，统一通过 `sys_user_auth` 表校验，并联查出 `sys_user` 的基础信息。

```sql
SELECT 
    u.id, 
    u.username, 
    u.real_name, 
    u.disable, 
    a.credential,
    a.verified as verified
FROM sys_user_auth a
INNER JOIN sys_user u ON a.user_id = u.id
WHERE a.identity_type = 'password' -- 此处可替换为 wechat, github, phone 等
  AND a.identifier = 'admin'       -- 用户名、手机号或 OpenID
  AND u.disable = 0                 -- 确保主账号未被禁用
LIMIT 1;
```

### 2. 获取用户的“全量角色” (核心)
**业务场景**：用户的角色来源有两部分：①在 `sys_user_role` 中直接分配的；②通过所在用户组 `sys_user_group_role` 间接继承的。需使用 `UNION` 合并去重。

```sql
-- 方式一：获取用户直接关联的角色
SELECT r.id, r.role_code, r.role_name
FROM sys_role r
INNER JOIN sys_user_role ur ON r.id = ur.role_id
WHERE ur.user_id = ? AND r.disable = 0

UNION

-- 方式二：获取用户所在用户组绑定的角色
SELECT r.id, r.role_code, r.role_name
FROM sys_role r
INNER JOIN sys_user_group_role ugr ON r.id = ugr.role_id
INNER JOIN sys_user_group_user ugu ON ugr.group_id = ugu.group_id
INNER JOIN sys_user_group ug ON ug.id = ugu.group_id
WHERE ugu.user_id = ? AND r.disable = 0 AND ug.disable = 0;
```

### 3. 获取用户的“全量权限标识” (核心菜单/按钮渲染)
**业务场景**：用户登录后，前端需要拿到类似于 `['sys:user:add', 'sys:role:list']` 的权限列表。这需要将用户的“全量角色”作为子查询，再去关联 `sys_permission` 表。

```sql
SELECT DISTINCT p.perm_code
FROM sys_permission p
INNER JOIN sys_role_permission rp ON p.id = rp.permission_id
WHERE p.disable = 0 
  AND p.perm_code IS NOT NULL
  AND rp.role_id IN (
      -- 子查询：获取用户的全量角色ID集合
      SELECT role_id FROM sys_user_role WHERE user_id = ?
      UNION
      SELECT ugr.role_id 
      FROM sys_user_group_role ugr
      INNER JOIN sys_user_group_user ugu ON ugr.group_id = ugu.group_id
      INNER JOIN sys_user_group ug ON ug.id = ugu.group_id
      WHERE ugu.user_id = ? AND ug.disable = 0
  );
```

### 4. 构建用户的动态路由菜单树
**业务场景**：用户登录后，获取其有权访问的左侧菜单（目录和菜单）。

```sql
SELECT DISTINCT 
    p.id, 
    p.parent_id, 
    p.perm_name, 
    p.path, 
    p.icon, 
    p.sort_num
FROM sys_permission p
INNER JOIN sys_role_permission rp ON p.id = rp.permission_id
WHERE p.disable = 0 
  AND p.perm_type IN (1, 2) -- 仅查询目录(1)和菜单(2)，排除按钮(3)
  AND rp.role_id IN (
      -- 同样复用全量角色ID的子查询逻辑
      SELECT role_id FROM sys_user_role WHERE user_id = ?
      UNION
      SELECT ugr.role_id 
      FROM sys_user_group_role ugr
      INNER JOIN sys_user_group_user ugu ON ugr.group_id = ugu.group_id
      WHERE ugu.user_id = ?
  )
ORDER BY p.parent_id, p.sort_num;
```

### 5. 分页查询用户列表（带所属组织名称）
**业务场景**：后台管理系统展示用户列表，通常需要带出组织名称。

```sql
SELECT 
    u.id, 
    u.username, 
    u.real_name, 
    u.phone, 
    u.disable, 
    u.create_time,
    o.org_name
FROM sys_user u
LEFT JOIN sys_org o ON u.org_id = o.id
WHERE u.disable = 0 -- 排除已彻底禁用的用户，或根据条件动态拼接
ORDER BY u.create_time DESC
LIMIT 0, 10;
```
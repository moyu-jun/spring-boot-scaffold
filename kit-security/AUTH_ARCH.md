# 🚀 认证授权方案设计（Redis + Session 模型）

> 这是一个 **基于 Redis 的可控会话模型认证方案**，在安全性、可维护性和扩展性之间取得了很好的平衡，适合作为企业级脚手架的标准实现。
>
> 关键词：**可撤销 / 多端登录 / 权限实时生效 / 高并发安全**

---

# 📌 1. 设计目标

- ✅ 支持多端登录（Web / App / PC）
- ✅ 支持单端互斥（同设备踢人）
- ✅ 支持强制下线（踢用户）
- ✅ 支持权限实时变更（秒级生效）
- ✅ 支持高并发安全（防并发刷新 / 重放攻击）
- ✅ 避免 JWT 无法撤销的问题

---

# 🧠 2. 核心设计思想

> ❗ 不使用 JWT 存储业务数据，而是采用 **服务端 Session 模型**

- Access Token = `sid`（无意义随机字符串）
- 所有用户状态存储在 Redis
- Refresh Token 一次性消费（Rotation）
- 权限变更通过版本号控制（避免全量扫描）

---

# 🗂️ 3. Redis Key 设计

## 3.1 会话主表（核心）

```
auth:session:{sid}
```

- 类型：String (JSON)
- TTL：15 ~ 30 分钟

```json
{
  "userId": 10001,
  "deviceType": "WEB",
  "ip": "1.1.1.1",
  "ua": "...",
  "permVersion": 3,
  "loginTime": "2024-03-10 12:20:30"
}
```

👉 说明：
- `sid` = Access Token
- 所有鉴权只依赖这一条数据

---

## 3.2 Refresh Token（一次性 + 轮换）

```
auth:rt:{refreshToken}
```

- 类型：String (JSON)
- TTL：7 ~ 30 天

```json
{
  "sid": "access token",
  "userId": 10001,
  "deviceType": "WEB"
}
```

👉 特点：
- 一次性消费（用完即删）
- 每次刷新都会生成新的 RT（Rotation）

---

## 3.3 用户会话索引

```
auth:user:{userId}:sessions
```

- 类型：Set
- Value：`sid`

👉 用于：
- 踢下线
- 查询用户所有登录设备

---

## 3.4 用户权限缓存

```
auth:user:{userId}:permissions
```

- 类型：String（JSON）
- Value：角色列表、权限列表
- TTL：比 session 长一倍，刷新 token 时重置过期时间

```json
{
  "roles": ["ADMIN"],
  "permissions": ["user:add", "user:delete"]
}
```

👉 用于：
- 允许多端登录时，防止 session 膨胀，数据解耦
- 如果仅支持单端登录，可将该数据放在 session 中，减少 Redis IO 次数
- 权限变更实时生效

---

## 3.5 设备互斥索引（可选）

```
auth:user:{userId}:device:{deviceType}
```

- 类型：String
- Value：`sid`

👉 用于：
- 同设备只允许一个登录

---

# 🔄 4. 核心流程设计

---

## ✅ 4.1 登录流程

```
Client → 登录请求
    ↓
Auth Service
    ↓
1. 校验账号密码
    ↓
2. 生成 sid + refreshToken
    ↓
3. 检查 deviceKey 是否存在
    ↓
    存在 → 删除旧 session（踢人）
    ↓
4. 写入 Redis：
   SET auth:session:{sid}
   SET auth:rt:{rt}
   SET auth:user:{userId}:permissions
   SADD auth:user:{uid}:sessions sid
   SET deviceKey sid
    ↓
5. 返回 AT + RT
```

---

## ✅ 4.2 鉴权流程

```
Client → 请求（携带 AT）
    ↓
Interceptor / Gateway
    ↓
1. GET auth:session:{sid}
    ↓
    不存在 → 401 认证失败
    ↓
2. GET auth:user:{uid}:permissions
    ↓
3. 封装 Authentication（含 authorities）
    ↓
4. 放入 SecurityContext
    ↓
5. @PreAuthorize 权限判断
    ↓
    无权限 → 403 权限不足
    ↓
6. 继续业务处理
```

---

## ✅ 4.3 刷新 Token（核心）

```
Client → refresh 请求（携带 RT）
    ↓
Auth Service
    ↓
1. GET auth:rt:{rt}
    ↓
    不存在 → 401
    ↓
2. 加锁（Redis / Lua）
    ↓
3. 删除旧 RT（关键）
    ↓
4. 生成新 sid + 新 RT
    ↓
5. 删除旧 session
    ↓
6. 写入新数据：
   SET auth:session:newSid
   SET auth:rt:newRt
   SADD sessions
   更新 deviceKey
   续期 auth:user:{userId}:permissions
    ↓
7. 返回新 AT + RT
```

---

## ✅ 4.4 踢下线（强制登出）

```
Admin → 操作
    ↓
1. SMEMBERS auth:user:{uid}:sessions
    ↓
2. 遍历 sid
    ↓
3. DEL auth:session:{sid}
    ↓
4. 删除 deviceKey
    ↓
5. 删除 RT（可选）
```

---

## ✅ 4.5 权限变更

```
Admin 修改权限
    ↓
SET auth:user:{uid}:permissions
```

---

# 🔐 5. 安全设计

## 5.1 Refresh Token 防重放

- RT 一次性消费
- 使用 Redis / Lua 保证原子性

```lua
if redis.call("GET", rtKey) then
    redis.call("DEL", rtKey)
    return 1
else
    return 0
end
```

---

## 5.2 并发刷新控制

- 前端：Promise 锁（只发一个 refresh）
- 后端：Redis 分布式锁

---

## 5.3 风控策略（推荐）

不要 ❌：
- 强绑定 IP
- 强绑定 UA

推荐 ✅：
- 记录 IP / UA
- 进行风险分析
- 异常时：
    - 强制刷新
    - 或重新登录

---

# ⏱️ 6. TTL 策略

| Key              | TTL               |
|------------------|-------------------|
| session          | 15 分钟             |
| refresh token    | 15 天              |
| user sessions    | 30 天              |
| user permissions | 30 分钟（session的两倍） |

---

# ⚙️ 7. 实现建议

## 7.1 sid 生成

```
UUID / Snowflake + 随机串
```

---

## 7.2 上下文存储

推荐优先级：

1. ThreadLocal（稳定）
2. ScopedValue（Java 21 虚拟线程优化）

---

## 7.3 Redis 原子操作

建议使用：
- Lua Script（优先）
- 或 Redisson 锁

---

# 🏁 8. 方案总结

## 🔥 优势

- ✔ 可撤销（比 JWT 更安全）
- ✔ 权限实时生效（O(1)）
- ✔ 支持多端管理
- ✔ 易扩展（SSO / Gateway / OAuth2）
- ✔ Redis 高性能支持

---

## ⚖️ 对比 JWT

| 维度     | 本方案      | JWT |
|--------|----------|-----|
| 可撤销    | ✅        | ❌   |
| 权限实时更新 | ✅        | ❌   |
| 服务端控制  | 强        | 弱   |
| 性能     | 高（Redis） | 极高  |
| 实现复杂度  | 中        | 低   |

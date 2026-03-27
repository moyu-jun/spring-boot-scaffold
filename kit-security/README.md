# kit-security 轻量级安全模块

轻量级安全认证模块，提供简单易用的登录认证和权限校验功能，适用于前后端分离场景。

--- 

## 设计理念

相比 Spring Security 的复杂配置，kit-security 专注于最常见的安全需求：
- **登录认证**：基于 Token 的无状态认证
- **权限校验**：使用 SpEL 表达式的注解式权限控制
- **前后端分离**：专为 RESTful API 设计
- **脚手架友好**：无硬编码包名，适配 Maven Archetype

--- 

## 功能特性

- 基于 Token 的认证机制（支持 JWT 或自定义实现）
- SpEL 表达式权限校验（兼容 Spring Security 语法）
- 线程安全的 SecurityContext（支持虚拟线程）
- 密码加密（BCrypt、PBKDF2、SCrypt、Argon2）
- 轻量级设计，无侵入性
- Java 21 优化（record、表达式缓存）
- **无硬编码包名**：使用 AOP 参数绑定，适配脚手架场景

--- 

## 使用方式

### 1. 添加依赖

```xml
<dependency>
    <groupId>com.junmoyu</groupId>
    <artifactId>kit-security</artifactId>
</dependency>
```

### 2. 配置文件

```yaml
security:
  enabled: true
  exclude-patterns:
    - /api/auth/login
    - /api/auth/register
    - /swagger-ui/**
    - /v3/api-docs/**
  token:
    header-name: Authorization
    prefix: "Bearer "
    expiration: 604800        # 7 天（秒）
    refresh-expiration: 2592000  # 30 天（秒）
```

### 3. 实现 UserDetail 接口

```java
@Service
public class UserDetailServiceImpl implements UserDetail {

    @Autowired
    private UserService userService;

    @Override
    public Authentication authentication(String token) {
        // 解析 Token（JWT 或 Redis 查询）
        Long userId = parseToken(token);
        User user = userService.getById(userId);

        if (user == null) {
            return null;
        }

        // 构建认证信息
        return new Authentication(
            user.getId(),
            user.getUsername(),
            token,
            user.getRoles(),      // 角色列表
            user.getPermissions(), // 权限列表
            Map.of("nickname", user.getNickname())
        );
    }
}
```

### 4. 配置拦截器

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private UserDetail userDetail;

    @Autowired
    private SecurityProperties securityProperties;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuthInterceptor(userDetail, securityProperties))
                .addPathPatterns("/**")
                .excludePathPatterns(securityProperties.getExcludePatterns())
                .order(Ordered.HIGHEST_PRECEDENCE);
    }
}
```

### 5. 使用权限注解

```java
@RestController
@RequestMapping("/api/user")
public class UserController {

    // 需要 ADMIN 角色
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/list")
    public R<List<User>> list() {
        return R.success(userService.list());
    }

    // 需要 user:delete 权限
    @PreAuthorize("hasPermission('user:delete')")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        userService.removeById(id);
        return R.success();
    }

    // 需要 ADMIN 或 MANAGER 角色
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PostMapping("/create")
    public R<Void> create(@RequestBody User user) {
        userService.save(user);
        return R.success();
    }

    // 复杂表达式：管理员或本人
    @PreAuthorize("hasRole('ADMIN') or isUser(#userId)")
    @GetMapping("/{userId}/profile")
    public R<User> getProfile(@PathVariable Long userId) {
        return R.success(userService.getById(userId));
    }

    // 需要已认证
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me")
    public R<User> getCurrentUser() {
        Long userId = SecurityContext.getUserId();
        return R.success(userService.getById(userId));
    }
}
```

### 6. 获取当前用户信息

```java
// 获取完整认证信息
Authentication auth = SecurityContext.getAuthentication();

// 快捷方法
Long userId = SecurityContext.getUserId();
String account = SecurityContext.getAccount();
boolean authenticated = SecurityContext.isAuthenticated();
```

### 7. 密码加密

```java
@Autowired
private PasswordEncoder passwordEncoder;

// 注册时加密密码
String encodedPassword = passwordEncoder.encode(rawPassword);
user.setPassword(encodedPassword);

// 登录时验证密码
boolean matches = passwordEncoder.matches(rawPassword, user.getPassword());
```

--- 

## SpEL 表达式支持

| 表达式 | 说明 |
|--------|------|
| `hasRole('ADMIN')` | 是否拥有指定角色 |
| `hasAnyRole('ADMIN', 'USER')` | 是否拥有任意一个角色 |
| `hasAllRoles('ADMIN', 'MANAGER')` | 是否拥有所有角色 |
| `hasPermission('user:delete')` | 是否拥有指定权限 |
| `hasAnyPermission('user:read', 'user:write')` | 是否拥有任意一个权限 |
| `hasAllPermissions('user:read', 'user:write')` | 是否拥有所有权限 |
| `isUser(#userId)` | 是否为指定用户 |
| `isAuthenticated()` | 是否已认证 |

支持逻辑运算符：`and`、`or`、`not`

--- 

## Java 21 优化特性

1. **record 类型**：`Authentication` 使用 record 实现不可变对象
2. **SpEL 表达式缓存**：使用 `ConcurrentHashMap` 缓存已解析的表达式
3. **虚拟线程支持**：`ThreadLocal` 在虚拟线程下正常工作
4. **增强的空值处理**：避免 NPE

--- 

## 与 Spring Security 的对比

| 特性 | kit-security | Spring Security |
|------|--------------|-----------------|
| 学习曲线 | 低 | 高 |
| 配置复杂度 | 简单 | 复杂 |
| 功能完整性 | 基础功能 | 企业级全功能 |
| 适用场景 | 前后端分离 API | 全场景 |
| 性能开销 | 低 | 中等 |

--- 

## 认证授权最佳实践

根据业务场景的不同诉求，提供以下两种认证方案供选择。

### 方案对比

| 维度 | 场景一：JWT 自包含 | 场景二：UUID Session |
|------|-------------------|---------------------|
| Access Token 类型 | JWT（自包含） | UUID（不透明） |
| 每次请求查询 Redis | 否 | 是（1次） |
| 退出登录实时生效 | 否（需等 Token 过期） | 是 |
| 权限变更实时生效 | 否 | 是 |
| 管理员封号实时生效 | 否 | 是 |
| Refresh Token | 需要（客户端主动刷新） | 不需要（滑动过期） |
| Redis 依赖 | 弱（仅存 Refresh Token） | 强（核心依赖） |
| 适用权限规模 | 小（建议用角色判权） | 不限 |
| 适用场景 | 对接口响应速度极度敏感 | 业务健壮性、实时控制优先 |

---

### 场景一：极致性能 - JWT 自包含方案

**核心思路**：登录时将完整的用户信息、角色、权限直接写入 JWT Payload，每次请求仅解析 JWT，完全不查询数据库或 Redis。

#### 设计要点

- **Access Token**：JWT，短期（15-30 分钟），Payload 中携带完整信息
- **Refresh Token**：UUID，长期（7-30 天），存储于 Redis，由客户端感知过期后主动请求刷新
- **权限判定**：建议以**角色（roles）**进行判定，避免权限列表过大导致 HTTP Header 臃肿、消耗网络资源
- **退出登录**：删除 Redis 中的 Refresh Token；当前 Access Token 在到期前仍有效，但寿命极短，风险可控

#### JWT Payload 设计

```json
{
  "userId": 10001,
  "account": "admin",
  "tenantId": 1,
  "roles": ["ADMIN", "MANAGER"],
  "jti": "550e8400-e29b-41d4-a716-446655440000",
  "iat": 1711526400,
  "exp": 1711528200
}
```

> **注意**：权限条目数量多时（如几十条细粒度权限），JWT 体积会随之增大并附加在每次请求的 Header 中，持续消耗网络资源。因此此方案建议以角色（数量有限）进行接口级权限控制。

#### 完整实现示例

**登录 - 生成双 Token**

```java
@Service
public class AuthService {

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 用户登录
     */
    public LoginResponse login(String account, String password) {
        // 1. 验证用户名密码
        User user = userMapper.selectByAccount(account);
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        // 2. 查询角色列表（建议用角色判权，避免权限列表过大）
        List<String> roles = roleMapper.selectByUserId(user.getId());

        // 3. 生成 Access Token（JWT，含完整信息）
        String accessToken = Jwts.builder()
                .claim("userId", user.getId())
                .claim("account", user.getAccount())
                .claim("tenantId", user.getTenantId())
                .claim("roles", roles)
                .id(UUID.randomUUID().toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 30 * 60 * 1000)) // 30 分钟
                .signWith(getSignKey())
                .compact();

        // 4. 生成 Refresh Token（UUID，存入 Redis，30 天）
        String refreshToken = UUID.randomUUID().toString().replace("-", "");
        redisUtils.set("auth:refresh_token:" + refreshToken, user.getId(), 30, TimeUnit.DAYS);

        return new LoginResponse(accessToken, refreshToken);
    }

    /**
     * 刷新 Access Token
     */
    public LoginResponse refreshToken(String refreshToken) {
        String refreshKey = "auth:refresh_token:" + refreshToken;
        Long userId = redisUtils.get(refreshKey);
        if (userId == null) {
            throw new BusinessException("Refresh Token 无效或已过期");
        }

        User user = userMapper.selectById(userId);
        List<String> roles = roleMapper.selectByUserId(userId);

        // 生成新的 Access Token
        String newAccessToken = Jwts.builder()
                .claim("userId", user.getId())
                .claim("account", user.getAccount())
                .claim("tenantId", user.getTenantId())
                .claim("roles", roles)
                .id(UUID.randomUUID().toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 30 * 60 * 1000))
                .signWith(getSignKey())
                .compact();

        return new LoginResponse(newAccessToken, refreshToken);
    }

    /**
     * 退出登录
     * 删除 Refresh Token；当前 Access Token 在到期前仍短暂有效
     */
    public void logout(String refreshToken) {
        redisUtils.delete("auth:refresh_token:" + refreshToken);
    }
}
```

**UserDetail 实现 - 解析 JWT，无需查询 Redis**

```java
@Service
public class UserDetailServiceImpl implements UserDetail {

    @Override
    public Authentication authentication(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSignKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            Long userId = claims.get("userId", Long.class);
            String account = claims.get("account", String.class);
            Long tenantId = claims.get("tenantId", Long.class);
            List<String> roles = claims.get("roles", List.class);

            // 直接从 JWT 中构建认证信息，无需查询 Redis 或数据库
            return new Authentication(userId, account, token, tenantId, roles, null, Map.of());

        } catch (JwtException e) {
            log.warn("JWT 解析失败: {}", e.getMessage());
            return null;
        }
    }
}
```

---

### 场景二：业务优先 - UUID Session 方案

**核心思路**：以 UUID 作为 Access Token，登录时将完整用户信息存入 Redis，每次请求查询一次 Redis 即可完成认证与权限校验，支持退出、封号、权限变更实时生效。

#### 设计要点

- **Access Token**：UUID，存储于 Redis，本身无业务含义，只是 Redis 的 Key
- **无 Refresh Token**：Token 本身存于 Redis，天然支持续期，使用**滑动过期**代替 Refresh Token
  - 每次请求时，若用户活跃，由后端自动重置过期时间，前端无感知
  - 设置**最大生命周期**（如 30 天）防止永不过期，到期后强制重新登录
- **双层 Key 结构**：支持多端登录管理

#### Redis Key 设计

```
auth:token:{uuid}       →  { userId, account, tenantId, roles, permissions }  TTL: 活跃过期时间（如 30 分钟）
auth:sessions:{userId}  →  Set{ uuid1, uuid2, uuid3 }                          TTL: 最大生命周期（如 30 天）
```

- `auth:token:{uuid}`：存储完整用户信息，每次活跃请求重置 TTL（滑动过期）
- `auth:sessions:{userId}`：记录该用户的所有在线 Token，用于多端管理（踢出所有设备、封号）

#### 完整实现示例

**登录 - 生成 UUID Token**

```java
@Service
public class AuthService {

    @Autowired
    private RedisUtils redisUtils;

    // 活跃过期时间：30 分钟（每次请求重置）
    private static final long ACTIVE_TTL_MINUTES = 30;
    // 最大生命周期：30 天（即使活跃也会在此后强制登出）
    private static final long MAX_TTL_DAYS = 30;

    /**
     * 用户登录
     */
    public LoginResponse login(String account, String password) {
        // 1. 验证用户名密码
        User user = userMapper.selectByAccount(account);
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        Long userId = user.getId();

        // 2. 查询角色和权限
        List<String> roles = roleMapper.selectByUserId(userId);
        List<String> permissions = permissionMapper.selectByUserId(userId);

        // 3. 构建用户会话数据
        UserSession session = new UserSession(userId, user.getAccount(), user.getTenantId(), roles, permissions);

        // 4. 生成 UUID Token
        String accessToken = UUID.randomUUID().toString().replace("-", "");

        // 5. 存储会话数据（活跃过期时间）
        redisUtils.set("auth:token:" + accessToken, session, ACTIVE_TTL_MINUTES, TimeUnit.MINUTES);

        // 6. 记录到用户的 sessions 集合（最大生命周期）
        String sessionsKey = "auth:sessions:" + userId;
        redisUtils.sAdd(sessionsKey, accessToken);
        redisUtils.expire(sessionsKey, MAX_TTL_DAYS, TimeUnit.DAYS);

        return new LoginResponse(accessToken);
    }

    /**
     * 退出登录（当前设备）
     */
    public void logout(String accessToken) {
        UserSession session = redisUtils.get("auth:token:" + accessToken);
        if (session != null) {
            redisUtils.delete("auth:token:" + accessToken);
            redisUtils.sRemove("auth:sessions:" + session.userId(), accessToken);
        }
    }

    /**
     * 踢出所有设备（修改密码、封号等场景）
     */
    public void logoutAll(Long userId) {
        String sessionsKey = "auth:sessions:" + userId;
        Set<String> tokens = redisUtils.sMembers(sessionsKey);
        if (tokens != null) {
            tokens.forEach(token -> redisUtils.delete("auth:token:" + token));
        }
        redisUtils.delete(sessionsKey);
    }

    /**
     * 管理员更新用户权限后，刷新 Redis 中的会话数据（实时生效）
     */
    public void refreshUserSessions(Long userId) {
        String sessionsKey = "auth:sessions:" + userId;
        Set<String> tokens = redisUtils.sMembers(sessionsKey);
        if (tokens == null || tokens.isEmpty()) {
            return;
        }

        List<String> roles = roleMapper.selectByUserId(userId);
        List<String> permissions = permissionMapper.selectByUserId(userId);

        for (String token : tokens) {
            String tokenKey = "auth:token:" + token;
            UserSession session = redisUtils.get(tokenKey);
            if (session != null) {
                // 更新权限，保留剩余 TTL
                UserSession updated = session.withRolesAndPermissions(roles, permissions);
                long remainingTtl = redisUtils.getExpire(tokenKey, TimeUnit.MINUTES);
                redisUtils.set(tokenKey, updated, remainingTtl, TimeUnit.MINUTES);
            }
        }
    }
}
```

**UserDetail 实现 - 查询 Redis，滑动过期**

```java
@Service
public class UserDetailServiceImpl implements UserDetail {

    @Autowired
    private RedisUtils redisUtils;

    private static final long ACTIVE_TTL_MINUTES = 30;

    @Override
    public Authentication authentication(String token) {
        String tokenKey = "auth:token:" + token;
        UserSession session = redisUtils.get(tokenKey);

        if (session == null) {
            return null; // Token 不存在或已过期
        }

        // 滑动过期：每次活跃请求重置 TTL
        redisUtils.expire(tokenKey, ACTIVE_TTL_MINUTES, TimeUnit.MINUTES);

        return new Authentication(
                session.userId(),
                session.account(),
                token,
                session.tenantId(),
                session.roles(),
                session.permissions(),
                Map.of()
        );
    }
}
```

**UserSession 数据结构**

```java
/**
 * 存储在 Redis 中的用户会话数据
 */
public record UserSession(
        Long userId,
        String account,
        Long tenantId,
        List<String> roles,
        List<String> permissions
) {
    public UserSession withRolesAndPermissions(List<String> roles, List<String> permissions) {
        return new UserSession(userId, account, tenantId, roles, permissions);
    }
}
```

### 关于方案的选择

> 首先，软件开发没有“银弹”。

方案没有绝对的好与坏，只有适合，那么在选择方案之前，先看看以下四个**“架构级的博弈”**：

**1. 微服务时代的“解耦”与“自治”**

在复杂的微服务架构中，**“去中心化”**是一个核心指标。

- **UUID 方案**： 所有的微服务（订单、商品、库存）在处理请求时，必须连接到同一个中心化的 Redis 集群。一旦 Redis 出现抖动、网络延迟或带宽打满，整个系统的认证鉴权都会陷入瘫痪。
- **JWT 方案**： 每一个微服务都可以通过预存的“公钥”独立校验 Token 的合法性。对于非核心权限（如只需知道 userId），服务甚至完全不需要访问 Redis。
  - 场景： 如果你的网关已经校验了权限并把用户信息放入 Header，下游的微服务直接内存读取即可，彻底实现了逻辑自治。

**2. “认证”与“授权”的阶段性剥离（Phased Authentication）**

企业级实践中，通常会将 **Authentication（你是谁）** 与 **Authorization（你能做什么）** 分开：

- **JWT 负责“你是谁”**： 这是一个 100% 确定的事实，不需要查库。
- **Redis 负责“你能做什么”**： 这是一个动态变化的状态。
- **收益**： 很多时候，我们只需要知道“你是谁”（比如日志审计、限流、简单的 UserID 绑定），这时 JWT 无需查 Redis。只有在涉及具体业务操作时，才按需去查权限。这在高并发场景下能显著减轻 Redis 的压力。

**3. API 网关的“透传”性能**

在高性能网关（如 Kong, Nginx, Spring Cloud Gateway）层：

- **UUID**： 网关必须通过“令牌内省”（Introspection）去访问后端数据库或缓存才能知道这个请求是谁发的。这增加了网关的负担。
- **JWT**： 网关可以在不访问任何数据库的情况下，直接提取 userId 或 tenantId 进行路由分发、灰度测试或请求限流。这种**“自包含”**的特性让网关层的性能上限更高。

**4. 标准化与生态支持（OAuth2 / OIDC）**
   
如果你需要对接第三方平台（如对接微信登录、接入公司的 SSO 单点登录系统、或者提供 Open API 给合作伙伴）：

- **JWT 是标准答案**： OAuth2.0 的核心就是 JWT。如果你使用 UUID，你需要为每一个合作伙伴提供一套“校验接口”，增加集成的复杂度。
- **安全性**： JWT 是经过数字签名的，客户端篡改即失效。而 UUID 虽然不可预测，但它本质上只是一个“指针”，安全性完全依赖于后端的存储校验。

---

**场景 A：如果你做的是中小型单体或轻量级微服务**

建议使用 UUID token + 滑动过期的方案。

- **理由**：逻辑简单，开发效率极高，Redis 带来的那几毫秒延迟在用户层面几乎感知不到，且“即时登出”体验极好。前端也不需要处理刷新 Token 等逻辑。

**场景 B：如果你做的是大型分布式、多租户或有外部接入需求的项目**

采用“JWT + Redis 权限缓存”的混合模式，或者根据你的需求进行重新设计。

**理由**： 
1.  **网关层透明**： 方便做全链路追踪和多租户隔离。
2.  **性能冗余**： 对于高频非鉴权接口（如只读接口、基础配置接口），可以省去 Redis 查询。
3.  **标准化**： 方便未来扩展为 OAuth2 或接入 OpenID Connect。

--- 

## 注意事项

1. **线程安全**：`SecurityContext` 使用 `ThreadLocal`，在异步场景需要手动传递
2. **权限数据源**：需要自行实现用户角色和权限的加载逻辑
3. **虚拟线程**：完全支持 Java 21 虚拟线程，无需额外配置

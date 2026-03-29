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
  token:
    expiration: 1800    # 30 分钟（秒）
    refresh-expiration: 604800  # 7 天（秒）
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

| 表达式                                            | 说明         |
|------------------------------------------------|------------|
| `isAuthenticated()`                            | 是否已认证      |
| `isUser(#userId)`                              | 是否为指定用户    |
| `hasRole('ADMIN')`                             | 是否拥有指定角色   |
| `hasAnyRole('ADMIN', 'USER')`                  | 是否拥有任意一个角色 |
| `hasAllRoles('ADMIN', 'MANAGER')`              | 是否拥有所有角色   |
| `hasAuthority('user:delete')`                  | 是否拥有指定权限   |
| `hasAnyAuthority('user:read', 'user:write')`   | 是否拥有任意一个权限 |
| `hasAllAuthorities('user:read', 'user:write')` | 是否拥有所有权限   |
| `hasPermission('user:delete')`                 | 是否拥有指定权限   |
| `hasAnyPermission('user:read', 'user:write')`  | 是否拥有任意一个权限 |
| `hasAllPermissions('user:read', 'user:write')` | 是否拥有所有权限   |

支持逻辑运算符：`and`、`or`、`not`

--- 

## Java 21 优化特性

1. **record 类型**：`Authentication` 使用 record 实现不可变对象
2. **SpEL 表达式缓存**：使用 `ConcurrentHashMap` 缓存已解析的表达式
3. **虚拟线程支持**：`ThreadLocal` 在虚拟线程下正常工作
4. **增强的空值处理**：避免 NPE

--- 

## 与 Spring Security 的对比

| 特性    | kit-security | Spring Security |
|-------|--------------|-----------------|
| 学习曲线  | 低            | 高               |
| 配置复杂度 | 简单           | 复杂              |
| 功能完整性 | 基础功能         | 企业级全功能          |
| 适用场景  | 前后端分离 API    | 全场景             |
| 性能开销  | 低            | 中等              |

---

## 认证授权方案设计

[认证授权方案设计文档](AUTH_ARCH.md)

## 注意事项

1. **线程安全**：`SecurityContext` 使用 `ThreadLocal`，在异步场景需要手动传递
2. **权限数据源**：需要自行实现用户角色和权限的加载逻辑
3. **虚拟线程**：完全支持 Java 21 虚拟线程，无需额外配置

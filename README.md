# spring-boot-scaffold

🚀 **基于 Java 21 & Spring Boot 3.5 的企业级开发脚手架**

这是一个遵循 **单一职责原则** 与 **模块化架构** 设计的现代化脚手架。它不同于传统的类库，旨在提供一套高度可定制的“积木”，支持业务开发通过源码微调实现极速交付。

---

## ✨ 核心特性

- **Java 21 & 虚拟线程**: 全面适配虚拟线程（Loom），大幅提升 I/O 密集型场景下的并发性能。
- **Spring Boot 3.5**: 集成最新版 Spring 生态，默认支持声明式 `RestClient` 与 `HTTP Interfaces`。
- **防御性设计**: 内置可重复读 `HttpServletRequest`，支持大报文自动熔断（默认 10MB），防止 OOM。
- **现代化 API**: 基于 Java 21 `record` 实现不可变响应对象 `R<T>`。
- **模块化构建**: 核心能力与启动逻辑完全解耦，支持以”搭积木”方式快速扩展。
- **全链路追踪**: 内置 TraceId 机制，支持同步/异步场景，兼容虚拟线程。
- **对象存储**: 开箱即用的 OSS 模块，兼容 AWS S3、阿里云、腾讯云、MinIO 等。
- **轻量级安全**: 简化的认证鉴权方案，支持 JWT/UUID Session，SpEL 表达式权限控制。

---

## 📂 项目结构

```text
spring-boot-scaffold
├── kit-basic        # 基础模块：通用封装、异常拦截、TraceId 追踪、工具类
├── kit-oss          # 对象存储模块：基于 AWS S3 SDK，支持多种对象存储服务
├── kit-security     # 安全模块：轻量级认证鉴权，支持 Token 认证与权限校验
└── bootstrap        # 启动模块：应用入口、核心配置与环境装配
```

---

## 🚀 编译安装与应用

1. 先将脚手架安装到本地（只需要执行一次，除非版本更新）。

```shell
# 执行生成命令
mvn archetype:create-from-project

# 安装到本地仓库
cd target/generated-sources/archetype
mvn clean install
```

2. 创建 catalog.xml 文件

创建 catalog.xml 文件，并在文件中写入如下内容：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<archetype-catalog 
  xsi:schemaLocation="http://maven.apache.org/plugins/maven-archetype-plugin/archetype-catalog/1.0.0 http://maven.apache.org/xsd/archetype-catalog-1.0.0.xsd"
  xmlns="http://maven.apache.org/plugins/maven-archetype-plugin/archetype-catalog/1.0.0"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">

  <archetypes>
    <!-- 本地 Spring Boot 脚手架信息 -->
    <archetype>
      <groupId>com.junmoyu</groupId>
      <!-- 脚手架 ArtifactId （默认项目名-archetype）-->
      <artifactId>spring-boot-scaffold-archetype</artifactId>
      <!-- 注意和当前项目版本保持一致 -->
      <version>0.0.1-SNAPSHOT</version>
    </archetype>

    <!-- 可添加多个本地脚手架，按同样格式写即可 -->
  </archetypes>
</archetype-catalog>
```

完成后将文件放置到 `C:\Users\xxxx\.m2` 文件夹或任意文件夹都可，但建议放置在该目录。

3. IDEA 中添加 catalog

* 启动 IDEA，选择 Settings -> Build, Execution, Deployment -> Build Tools -> Maven -> Archetype Catalogs。
* 在右侧面板的上方，点击 `+` 按钮会弹出 `Add Catalog` 弹窗。
* 在 Add Catalog 弹窗中输入相应信息：
  * **Location**: 选择之前编写的 `catalog.xml` 文件。
  * **Name**: 根据你的喜好进行命令即可，比如 `Local Catalog`。
* 点击 `Add` 即可添加成功。

![Add Catalog](https://cdn.jsdelivr.net/gh/moyu-jun/resource/img/Add-Catalog.png)

4. 新建项目使用脚手架

* 启动 IDEA，选择 File -> New -> Project，出现 New Project 弹窗。
* 在左侧面板选择 `Maven Archetype`。
* 在右侧面板的 `Catalog` 的下拉框中选择上一步添加的 `Local Catalog`。
* 然后在下方的 `Archetype` 的下拉框中就可以选择脚手架。
* 选择完脚手架，补充完其他信息，点击 `Create` 按钮即可按照该脚手架创建项目。

![Use Catalog](https://cdn.jsdelivr.net/gh/moyu-jun/resource/img/Use-Catalog.png)

---

## 📖 核心组件说明

### 1. kit-basic - 基础模块

提供企业级开发的基础能力，包括：

- **可重复读请求**：默认针对 application/json 请求开启，支持大报文熔断（默认 10MB）
- **统一 API 响应**：基于 Java 21 record 的 `R<T>` 响应对象
- **全局异常拦截**：统一处理业务异常和系统异常
- **TraceId 追踪**：支持同步/异步场景的全链路追踪，兼容虚拟线程
- **工具类集合**：Json、Redis、脱敏等常用工具

配置示例：

```yaml
request:
    repeatable:
        enabled: true          # 开关，默认开启
        max-payload-size: 10MB # 超过该阈值将不进行内存包装，防止 OOM
```

详细文档：[kit-basic/README.md](kit-basic/README.md)

---

### 2. kit-oss - 对象存储模块

基于 AWS S3 SDK 封装，支持所有兼容 S3 协议的对象存储服务。

**功能特性**：
- 文件上传/下载/删除（单个/批量）
- 文件存在性检查
- 预签名 URL 生成（临时访问私有文件）
- 支持多存储桶操作
- 自动配置，开箱即用

**兼容的对象存储**：AWS S3、阿里云 OSS、腾讯云 COS、MinIO 等

配置示例：

```yaml
oss:
  enabled: true
  endpoint: https://s3.amazonaws.com
  region: us-east-1
  access-key: your-access-key
  secret-key: your-secret-key
  bucket-name: your-bucket
  path-style-access: false  # MinIO 等需要设置为 true
```

详细文档：[kit-oss/README.md](kit-oss/README.md)

---

### 3. kit-security - 轻量级安全模块

专为前后端分离场景设计的轻量级认证鉴权模块，相比 Spring Security 更简单易用。

**功能特性**：
- 基于 Token 的无状态认证（支持 JWT 或 UUID Session）
- SpEL 表达式权限校验（`@PreAuthorize` 注解）
- 线程安全的 SecurityContext（支持虚拟线程）
- 密码加密（BCrypt、PBKDF2、SCrypt、Argon2）
- 无硬编码包名，适配脚手架场景

**支持的权限表达式**：
- `hasRole('ADMIN')` - 角色校验
- `hasPermission('user:delete')` - 权限校验
- `isUser(#userId)` - 用户身份校验
- `isAuthenticated()` - 认证状态校验

配置示例：

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

详细文档：[kit-security/README.md](kit-security/README.md)

---

## 🚀 快速开始

### 1. 引入依赖

根据需要选择模块：

```xml
<!-- 基础模块（必选） -->
<dependency>
    <groupId>${project.parent.groupId}</groupId>
    <artifactId>kit-basic</artifactId>
</dependency>

<!-- 对象存储模块（可选） -->
<dependency>
    <groupId>${project.parent.groupId}</groupId>
    <artifactId>kit-oss</artifactId>
</dependency>

<!-- 安全模块（可选） -->
<dependency>
    <groupId>${project.parent.groupId}</groupId>
    <artifactId>kit-security</artifactId>
</dependency>
```

### 2. 配置文件

在 `application.yml` 中添加相应配置：

```yaml
# 基础模块配置
request:
  repeatable:
    enabled: true
    max-payload-size: 10MB

# OSS 配置（如需使用）
oss:
  enabled: true
  endpoint: https://s3.amazonaws.com
  region: us-east-1
  access-key: ${OSS_ACCESS_KEY}
  secret-key: ${OSS_SECRET_KEY}
  bucket-name: your-bucket

# 安全模块配置（如需使用）
security:
  enabled: true
  exclude-patterns:
    - /api/auth/**
  token:
    expiration: 1800    # 30 分钟（秒）
    refresh-expiration: 604800  # 7 天（秒）
```

### 3. 使用示例

```java
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private OssService ossService;

    // 使用统一响应
    @GetMapping("/{id}")
    public R<User> getUser(@PathVariable Long id) {
        User user = userService.getById(id);
        return R.success(user);
    }

    // 使用权限校验
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public R<Void> deleteUser(@PathVariable Long id) {
        userService.removeById(id);
        return R.success();
    }

    // 使用 OSS 上传文件
    @PostMapping("/avatar")
    public R<String> uploadAvatar(@RequestParam MultipartFile file) {
        String url = ossService.upload("avatars/" + file.getOriginalFilename(),
                                      file.getInputStream(),
                                      file.getContentType());
        return R.success(url);
    }
}
```

---

## 📄 License

[Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)
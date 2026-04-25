# CLAUDE.md

本文件为 Claude Code（claude.ai/code）在处理此仓库代码时提供指导。

## 项目概述

- **技术栈**: Java 21, Spring Boot 3.5, lombok, MyBatis Plus, MySQL, Redis, Maven 多模块
- **项目名称**: spring-boot-scaffold
- **项目描述**: 这是一个遵循 **单一职责原则** 与 **模块化架构** 设计的现代化脚手架项目。它不同于传统的类库，旨在提供一套高度可定制的“积木”，支持业务开发通过源码微调实现极速交付。所有模块共享基础包 `com.junmoyu`。

## 构建与运行

```bash
# 使用 Maven 包装器（无需系统安装 Maven）
./mvnw clean install -DskipTests  # 完整构建（跳过测试）
./mvnw spring-boot:run            # 运行（启动 bootstrap 模块）
./mvnw test                       # 运行所有测试
./mvnw -pl <module> test          # 运行单个模块的测试
```

## 模块结构

```text
spring-boot-scaffold
├── kit-basic        # 基础模块：通用封装、异常拦截、TraceId 追踪、工具类
├── kit-oss          # 对象存储模块：基于 AWS S3 SDK，支持多种对象存储服务
├── kit-security     # 安全模块：轻量级认证鉴权，支持 Token 认证与权限校验
├── sys-iam          # 基于 RBAC3 模型构建，支持组织架构管理、用户组分配、角色权限以及多渠道第三方登录。
└── bootstrap        # 启动模块：应用入口、核心配置与环境装配
```

`kit-*` 模块为工具包，不包含业务逻辑。`sys-*` 模块为可复用的系统级业务模块。

## 自动配置模式

所有模块**不**使用 `spring.factories` 或 `AutoConfiguration.imports`。所有自动配置依赖 Spring Boot 的组件扫描，由 `@SpringBootApplication(scanBasePackages = "com.junmoyu")` 驱动。所有模块共享基础包 `com.junmoyu`。

## 认证架构（kit-security）

**设计文档核心原则：** 热点路径（每次资源请求）仅执行 **1 次 Redis 读取** —— `auth:access:{token}`。会话数据、权限快照和 `perm_ver` 仅在低频控制面操作（登录、刷新、登出、踢下线、权限变更）时读取。

认证拦截器提取 Bearer 令牌，在 Redis 中查找访问令牌哈希，并设置 `SecurityContext`（ThreadLocal，支持虚拟线程）。方法级鉴权使用 `@PreAuthorize`，由 AOP 切面（`AuthorizeAspect`）评估 SpEL 表达式。

详细设计原理见 `kit-security/认证授权方案.md`。

## 虚拟线程

默认启用虚拟线程（`spring.threads.virtual.enabled: true`，配置在 application.yml 中）。异步执行器配置（`AsyncConfig`）通过 `MdcVirtualThreadPerTaskExecutor` 将 MDC（TraceId）传播到虚拟线程。所有 kit 的 MDC 工具类均兼容虚拟线程。

## 编码约定
- **包名结构**: `com.junmoyu.<模块后缀>.*`
    - 示例: `com.junmoyu.iam.controller`
- **类名后缀**:
    - REST 控制器: `*Controller`
    - 服务实现类: `*Service` （注：不使用服务接口）
    - 数据访问: `*Mapper`（MyBatis Plus）
    - 数据传输对象: 对于接口的请求 `*Request`、对于接口的响应 `*Response`
    - 数据库实体: `*Entity`
- **异常处理**: 使用 `kit-basic` 模块中的 `com.junmoyu.basic.exception.GlobalExceptionHandler` 全局异常捕获
- **校验**: 使用 Jakarta Bean Validation（`@Valid`、`@Validated`）
- **日志**: Lombok `@Slf4j`，关键操作输出业务日志，避免输出敏感信息
- **时间类型**: 使用 `java.time`（`Instant`, `LocalDateTime`, `OffsetDateTime`），避免 `java.util.Date`
- **MapStruct**: 实体与 DTO 之间转换使用 MapStruct `@Mapper` 接口
- **字符串空值判断**: 所有对字符串是否为空的判断统一使用 `org.apache.commons.lang3.StringUtils` 工具类
- **空列表判断**: 所有对列表集合是否为空的判断统一使用 `org.apache.commons.collections4.CollectionUtils` 工具类
- **if子句**: 所有的 if 子句即使只有一行，也必须使用大括号 `{}` 包裹

## 关键包命名约定

- `com.junmoyu.basic` — kit-basic（工具类、基础类、过滤器）
- `com.junmoyu.oss` — kit-oss（OSS 服务接口 + S3 实现）
- `com.junmoyu.security` — kit-security（注解、切面、拦截器、核心逻辑）
- `com.junmoyu.iam` — sys-iam（控制器、服务、映射器、模型）
- `com.junmoyu` — bootstrap（配置类、示例控制器）

## API 设计惯例
- 基础路径: `/<资源复数>`，如 `/users`
- 响应格式: JSON, 统一包裹 `kit-basic` 在 `com.junmoyu.basic.model.R<T>` 中 (`code`, `message`, `data`)
- 分页请求参数: 基于 `kit-basic` 在 `com.junmoyu.basic.model.PageQuery` (`page`, `size`) 和 `com.junmoyu.basic.model.SearchPageQuery` (`keywords`)，如有更多的需求，要求继承这两个类进行实现
- 分页响应结构: `com.junmoyu.basic.model.PageResult<T>` 包含 `total`, `list`, 最终结构示例为 `R<PageResult<User>>`
- 认证/鉴权: 使用自定义 `kit-security` 进行控制，不使用 Spring Security框架。

## 开发约束（必须遵守）
- 不允许随意新增新架构层
- 不允许修改已有接口返回结构（除非明确要求）
- 不允许引入新依赖（可以推荐，需要用户确认）
- 不允许破坏现有事务逻辑
- 不允许新增重复工具类（可以推荐，需要用户确认）

## 常见开发任务指南

### 新建一个功能接口

1. 在 `com.junmoyu.*.model.constant` 中定义所需常量（可选）
2. 在 `com.junmoyu.*.model.enums` 中定义所需枚举（可选）
3. 在 `com.junmoyu.*.model.entity` 中定义数据库实体（可选）
4. 在 `com.junmoyu.*.model.request` 中定义请求参数
5. 在 `com.junmoyu.*.model.response` 中定义请求响应
6. 在 `com.junmoyu.*.service` 中编写服务实现，不定义接口
7. 在 `com.junmoyu.*.controller` 中创建 Controller 接口
8. 使用编译构建命令进行检查代码是否正确
9. 在 `bootstrap` 模块中启动应用，通过 Swagger/curl 验证
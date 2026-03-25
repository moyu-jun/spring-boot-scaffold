# spring-boot-scaffold

🚀 **基于 Java 21 & Spring Boot 3.5 的企业级开发脚手架**

这是一个遵循 **单一职责原则** 与 **模块化架构** 设计的现代化脚手架。它不同于传统的类库，旨在提供一套高度可定制的“积木”，支持业务开发通过源码微调实现极速交付。

---

## ✨ 核心特性

- **Java 21 & 虚拟线程**: 全面适配虚拟线程（Loom），大幅提升 I/O 密集型场景下的并发性能。
- **Spring Boot 3.5**: 集成最新版 Spring 生态，默认支持声明式 `RestClient` 与 `HTTP Interfaces`。
- **防御性设计**: 内置可重复读 `HttpServletRequest`，支持大报文自动熔断（默认 10MB），防止 OOM。
- **现代化 API**: 基于 Java 21 `record` 实现不可变响应对象 `R<T>`。
- **模块化构建**: 核心能力与启动逻辑完全解耦，支持以“搭积木”方式快速扩展。

---

## 📂 项目结构

```text
spring-boot-scaffold
├── basic        # 基础模块：通用封装、异常拦截、HTTP 客户端配置
└── bootstrap    # 启动模块：应用入口、核心配置与环境装配
```

## 📖 核心组件说明

### 1. 可重复读请求（Repeatable Request）

默认针对 application/json 请求开启。可在配置文件中微调：

```YAML
request:
    repeatable:
        enabled: true          # 开关，默认开启，可手动关闭
        max-payload-size: 10MB # 超过该阈值将不进行内存包装，防止 OOM，默认 10MB
```

### 2. 统一 API 响应
   
使用 Java 21 record 定义，代码更简洁，序列化更高效：

```text
// 返回成功
return R.success(data);

// 返回失败（配合全局异常拦截）
throw new BusinessException(1000, "操作失败");
```

## 🚀 编译安装与应用

1. 执行生成命令

```shell
mvn archetype:create-from-project
```
2. 安装到本地仓库

```shell
cd target/generated-sources/archetype
mvn clean install
```
3. IDEA 中使用该脚手架

* 启动 IDEA，选择 File -> New -> Project（如果是欢迎界面，直接点 New Project）。
* 在左侧生成器列表中，选择 Maven Archetype。
* 在右侧配置区域，你会看到 Archetype 下拉框，点击其右侧的 Add... 按钮。
* 在弹出的对话框中输入你的自定义 Archetype 信息：
  * GroupId: com.junmoyu。
  * ArtifactId: spring-boot-scaffold-archetype。
  * Version: 0.0.1-SNAPSHOT。
* 点击 OK 即可根据脚手架生成新项目。

## 📄 License

[Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)
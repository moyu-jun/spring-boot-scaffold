# AGENTS.md

## 项目概览
- Java 21
- Spring Boot 3.5
- Maven
- MyBatis-Plus
- Redis
- MySQL

---

## 模块约定

本项目是个多模块项目。`kit-` 前缀代表封装的工具模块，无任何业务相关的代码；`sys-` 前缀代表的是系统级的业务相关的模块，封装一些相对通用的功能。以下是各个模块的说明：

- kit-basic: 基础工具模块，包含基础实体类、统一响应封装、统一异常拦截、Json工具类等，具体内容可读取该模块内的README.md
- kit-oss: 对象存储模块，对OSS操作的简易封装，具体内容可读取该模块内的README.md
- kit-security: 轻量级安全模块，轻量级的认证授权封装模块，具体内容可读取该模块内的README.md
- boostrap: 应用启动模块
- sys-iam: 业务相关，但相对通用的统一用户管理与权限控制系统，具体内容可读取该模块内的README.md

---

## 目录约定
- controller：只做参数接收与返回值组装
- service：业务编排
- mapper：数据访问，基于 Mybatis-plus 生成
- model： 负责存放各种实体类（mode.entity），请求参数类（model.request），请求响应类(model.response)，枚举类（model）
- mode.dto： 负责数据传输类

---

## 开发约束（必须遵守）
- 不允许随意新增新架构层
- 不允许修改已有接口返回结构（除非明确要求）
- 不允许引入新依赖（可以推荐，需要用户确认）
- 不允许破坏现有事务逻辑
- 不允许新增重复工具类

---

## 数据库规范（MyBatis-Plus）
- 优先使用 LambdaQueryWrapper
- 禁止手写字符串 SQL（除非必要）
- 更新操作必须带条件

---

## 日志规范

- 日志统一使用 Lombok 的 @Slf4j
- 使用 log.info / log.error
- 必须记录关键业务参数
- 不记录敏感信息

---

## 构建与运行

```bash
mvn clean install
mvn spring-boot:run
```

## 完成标准（非常关键）

任务完成必须满足：

- 编译通过
- 单元测试通过
- 没有明显性能问题
- 修改最小化
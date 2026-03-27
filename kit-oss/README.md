# kit-oss

OSS 对象存储模块，基于 AWS S3 SDK 封装，支持所有兼容 S3 协议的对象存储服务。

## 功能特性

- 文件上传/下载
- 文件删除（单个/批量）
- 文件存在性检查
- 预签名 URL 生成（用于临时访问私有文件）
- 支持多存储桶操作
- 提供默认 REST 接口（可覆盖）
- 自动配置，开箱即用

## 使用方式

### 1. 添加依赖

```xml
<dependency>
    <groupId>${project.parent.groupId}</groupId>
    <artifactId>kit-oss</artifactId>
</dependency>
```

### 2. 配置文件

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

### 3. 使用方式

```java
@Autowired
private OssService ossService;

// 上传文件
String url = ossService.upload("path/to/file.jpg", inputStream, "image/jpeg");

// 下载文件
InputStream stream = ossService.download("path/to/file.jpg");

// 删除文件
ossService.delete("path/to/file.jpg");

// 批量删除
ossService.deleteBatch(List.of("file1.jpg", "file2.jpg"));

// 检查文件是否存在
boolean exists = ossService.exists("path/to/file.jpg");

// 生成预签名 URL（有效期 60 分钟）
String preSignedUrl = ossService.getPreSignedUrl("path/to/file.jpg", 60);
```

## 兼容的对象存储

- AWS S3
- 阿里云 OSS
- 腾讯云 COS
- MinIO
- 其他兼容 S3 协议的对象存储
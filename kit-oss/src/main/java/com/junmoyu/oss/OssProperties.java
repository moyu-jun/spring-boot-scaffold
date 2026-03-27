package com.junmoyu.oss;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * OSS 配置属性
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "oss")
public class OssProperties {

    /**
     * 是否启用 OSS
     */
    private boolean enabled = true;

    /**
     * 端点地址（如：<a href="https://s3.amazonaws.com">...</a>）
     */
    private String endpoint;

    /**
     * 区域（如：us-east-1）
     * MinIO 等的话随便填个值但不要留空
     */
    private String region;

    /**
     * 访问密钥 ID
     */
    private String accessKey;

    /**
     * 访问密钥
     */
    private String secretKey;

    /**
     * 默认存储桶名称
     */
    private String bucketName;

    /**
     * 是否使用路径风格访问（默认虚拟主机风格）
     * MinIO 等需要设置为 true
     */
    private boolean pathStyleAccess = false;
}

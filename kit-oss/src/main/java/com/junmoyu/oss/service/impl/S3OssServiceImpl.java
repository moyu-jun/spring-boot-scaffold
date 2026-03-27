package com.junmoyu.oss.service.impl;

import com.junmoyu.oss.OssProperties;
import com.junmoyu.oss.service.OssService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.List;

/**
 * OSS 服务实现类（基于 AWS S3 SDK）
 */
@Slf4j
@RequiredArgsConstructor
public class S3OssServiceImpl implements OssService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final OssProperties ossProperties;

    @Override
    public String upload(String objectKey, InputStream inputStream, String contentType) {
        return upload(ossProperties.getBucketName(), objectKey, inputStream, contentType);
    }

    @Override
    public String upload(String bucketName, String objectKey, InputStream inputStream, String contentType) {
        try {
            // 读取全部字节：available() 不可靠（对非文件流只返回可非阻塞读取的字节数），
            // 使用 readAllBytes() 保证内容完整，适合中小文件场景
            byte[] bytes = inputStream.readAllBytes();

            PutObjectRequest.Builder builder = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey);

            if (StringUtils.isNotBlank(contentType)) {
                builder.contentType(contentType);
            }

            s3Client.putObject(builder.build(), RequestBody.fromBytes(bytes));
            log.info("文件上传成功: bucket={}, key={}, size={}bytes", bucketName, objectKey, bytes.length);
            return buildObjectUrl(bucketName, objectKey);
        } catch (IOException e) {
            log.error("文件读取失败: bucket={}, key={}", bucketName, objectKey, e);
            throw new RuntimeException("文件读取失败: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("文件上传失败: bucket={}, key={}", bucketName, objectKey, e);
            throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
        }
    }

    @Override
    public InputStream download(String objectKey) {
        return download(ossProperties.getBucketName(), objectKey);
    }

    @Override
    public InputStream download(String bucketName, String objectKey) {
        try {
            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();
            return s3Client.getObject(request);
        } catch (Exception e) {
            log.error("文件下载失败: bucket={}, key={}", bucketName, objectKey, e);
            throw new RuntimeException("文件下载失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(String objectKey) {
        delete(ossProperties.getBucketName(), objectKey);
    }

    @Override
    public void delete(String bucketName, String objectKey) {
        try {
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();
            s3Client.deleteObject(request);
            log.info("文件删除成功: bucket={}, key={}", bucketName, objectKey);
        } catch (Exception e) {
            log.error("文件删除失败: bucket={}, key={}", bucketName, objectKey, e);
            throw new RuntimeException("文件删除失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteBatch(List<String> objectKeys) {
        deleteBatch(ossProperties.getBucketName(), objectKeys);
    }

    @Override
    public void deleteBatch(String bucketName, List<String> objectKeys) {
        if (objectKeys == null || objectKeys.isEmpty()) {
            return;
        }
        try {
            List<ObjectIdentifier> keys = objectKeys.stream()
                    .map(key -> ObjectIdentifier.builder().key(key).build())
                    .toList();

            DeleteObjectsRequest request = DeleteObjectsRequest.builder()
                    .bucket(bucketName)
                    .delete(Delete.builder().objects(keys).build())
                    .build();

            s3Client.deleteObjects(request);
            log.info("批量删除文件成功: bucket={}, count={}", bucketName, objectKeys.size());
        } catch (Exception e) {
            log.error("批量删除文件失败: bucket={}", bucketName, e);
            throw new RuntimeException("批量删除文件失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean exists(String objectKey) {
        return exists(ossProperties.getBucketName(), objectKey);
    }

    @Override
    public boolean exists(String bucketName, String objectKey) {
        try {
            HeadObjectRequest request = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();
            s3Client.headObject(request);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (Exception e) {
            log.error("检查文件是否存在失败: bucket={}, key={}", bucketName, objectKey, e);
            throw new RuntimeException("检查文件是否存在失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String getPreSignedUrl(String objectKey, int expirationMinutes) {
        return getPreSignedUrl(ossProperties.getBucketName(), objectKey, expirationMinutes);
    }

    @Override
    public String getPreSignedUrl(String bucketName, String objectKey, int expirationMinutes) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            GetObjectPresignRequest preSignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(expirationMinutes))
                    .getObjectRequest(getObjectRequest)
                    .build();

            PresignedGetObjectRequest preSignedRequest = s3Presigner.presignGetObject(preSignRequest);
            return preSignedRequest.url().toString();
        } catch (Exception e) {
            log.error("生成预签名 URL 失败: bucket={}, key={}", bucketName, objectKey, e);
            throw new RuntimeException("生成预签名 URL 失败: " + e.getMessage(), e);
        }
    }

    /**
     * 构建对象访问 URL
     */
    private String buildObjectUrl(String bucketName, String objectKey) {
        String endpoint = ossProperties.getEndpoint();
        String key = StringUtils.stripStart(objectKey, "/");
        if (ossProperties.isPathStyleAccess()) {
            return String.format("%s/%s/%s", endpoint, bucketName, key);
        } else {
            return String.format("%s/%s", endpoint.replace("//", "//" + bucketName + "."), key);
        }
    }
}

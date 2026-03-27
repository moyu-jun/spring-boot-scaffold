package com.junmoyu.oss.service;

import java.io.InputStream;
import java.util.List;

/**
 * OSS 对象存储服务接口
 */
public interface OssService {

    /**
     * 上传文件
     *
     * @param objectKey 对象键（文件路径: avatar/2024/03/27/a1b2c3d4.jpg）
     * @param inputStream 文件流
     * @param contentType 内容类型
     * @return 文件访问 URL
     */
    String upload(String objectKey, InputStream inputStream, String contentType);

    /**
     * 上传文件到指定存储桶
     *
     * @param bucketName 存储桶名称
     * @param objectKey 对象键
     * @param inputStream 文件流
     * @param contentType 内容类型
     * @return 文件访问 URL
     */
    String upload(String bucketName, String objectKey, InputStream inputStream, String contentType);

    /**
     * 下载文件
     *
     * @param objectKey 对象键
     * @return 文件流
     */
    InputStream download(String objectKey);

    /**
     * 下载指定存储桶的文件
     *
     * @param bucketName 存储桶名称
     * @param objectKey 对象键
     * @return 文件流
     */
    InputStream download(String bucketName, String objectKey);

    /**
     * 删除文件
     *
     * @param objectKey 对象键
     */
    void delete(String objectKey);

    /**
     * 删除指定存储桶的文件
     *
     * @param bucketName 存储桶名称
     * @param objectKey 对象键
     */
    void delete(String bucketName, String objectKey);

    /**
     * 批量删除文件
     *
     * @param objectKeys 对象键列表
     */
    void deleteBatch(List<String> objectKeys);

    /**
     * 批量删除指定存储桶的文件
     *
     * @param bucketName 存储桶名称
     * @param objectKeys 对象键列表
     */
    void deleteBatch(String bucketName, List<String> objectKeys);

    /**
     * 判断文件是否存在
     *
     * @param objectKey 对象键
     * @return 是否存在
     */
    boolean exists(String objectKey);

    /**
     * 判断指定存储桶的文件是否存在
     *
     * @param bucketName 存储桶名称
     * @param objectKey 对象键
     * @return 是否存在
     */
    boolean exists(String bucketName, String objectKey);

    /**
     * 获取预签名 URL（用于临时访问私有文件）
     *
     * @param objectKey 对象键
     * @param expirationMinutes 过期时间（分钟）
     * @return 预签名 URL
     */
    String getPreSignedUrl(String objectKey, int expirationMinutes);

    /**
     * 获取指定存储桶的预签名 URL
     *
     * @param bucketName 存储桶名称
     * @param objectKey 对象键
     * @param expirationMinutes 过期时间（分钟）
     * @return 预签名 URL
     */
    String getPreSignedUrl(String bucketName, String objectKey, int expirationMinutes);
}

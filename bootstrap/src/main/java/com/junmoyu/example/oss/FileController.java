package com.junmoyu.example.oss;

import com.junmoyu.basic.model.R;
import com.junmoyu.oss.service.OssService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * 文件存储控制器
 * 对接 OssService 实现对象存储操作
 */
@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
public class FileController {

    private final OssService ossService;

    /**
     * 单文件上传
     *
     * @param file 上传文件（必传）
     * @param dir  自定义文件目录（可选，例如：avatar/ 、image/）
     * @return 文件访问URL
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<String> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false, defaultValue = "") String dir) {
        // 校验文件非空
        if (file.isEmpty()) {
            return R.failure("上传文件不能为空");
        }

        try {
            // 生成唯一文件键：目录 + UUID + 原文件后缀
            String originalFilename = file.getOriginalFilename();
            String suffix = Objects.requireNonNull(originalFilename).substring(originalFilename.lastIndexOf("."));
            String objectKey = dir + UUID.randomUUID() + suffix;

            // 调用Service上传
            String url = ossService.upload(objectKey, file.getInputStream(), file.getContentType());
            return R.success(url);
        } catch (IOException e) {
            return R.failure("文件上传失败：" + e.getMessage());
        }
    }

    /**
     * 文件下载
     *
     * @param objectKey 文件唯一键（必传）
     * @return 文件流
     */
    @GetMapping("/download")
    public ResponseEntity<InputStreamResource> download(@RequestParam String objectKey) {
        try {
            InputStream inputStream = ossService.download(objectKey);

            // 设置下载响应头
            String fileName = objectKey.substring(objectKey.lastIndexOf("/") + 1);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(new InputStreamResource(inputStream));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/delete")
    public R<Void> delete(@RequestParam String objectKey) {
        try {
            ossService.delete(objectKey);
            return R.success(null);
        } catch (Exception e) {
            return R.failure("文件删除失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/delete/batch")
    public R<Void> deleteBatch(@RequestParam List<String> objectKeys) {
        try {
            ossService.deleteBatch(objectKeys);
            return R.success(null);
        } catch (Exception e) {
            return R.failure("批量删除失败：" + e.getMessage());
        }
    }

    @GetMapping("/exists")
    public R<Boolean> exists(@RequestParam String objectKey) {
        boolean exists = ossService.exists(objectKey);
        return R.success(exists);
    }

    /**
     * 获取私有文件临时访问URL
     *
     * @param expirationMinutes 过期时间（默认30分钟）
     */
    @GetMapping("/presigned-url")
    public R<String> getPreSignedUrl(@RequestParam String objectKey,
                                     @RequestParam(required = false, defaultValue = "30") int expirationMinutes) {
        try {
            String url = ossService.getPreSignedUrl(objectKey, expirationMinutes);
            return R.success(url);
        } catch (Exception e) {
            return R.failure("获取预签名URL失败：" + e.getMessage());
        }
    }
}

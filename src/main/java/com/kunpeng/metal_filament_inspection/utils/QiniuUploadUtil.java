package com.kunpeng.metal_filament_inspection.utils;

import com.kunpeng.metal_filament_inspection.config.QiniuConfig;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Component
public class QiniuUploadUtil {

    private final QiniuConfig qiniuConfig;
    private final Auth auth;
    private final UploadManager uploadManager;

    @Autowired
    public QiniuUploadUtil(QiniuConfig qiniuConfig) {
        this.qiniuConfig = qiniuConfig;
        // 使用配置类提供的 Region（已修正区域映射）
        Configuration cfg = new Configuration(qiniuConfig.getRegion());
        this.uploadManager = new UploadManager(cfg);
        this.auth = Auth.create(qiniuConfig.getAccessKey(), qiniuConfig.getSecretKey());
    }

    // ---------- 原有 MultipartFile 上传 ----------
    public String uploadImage(MultipartFile file) throws IOException {
        String fileName = generateFileName(file.getOriginalFilename());
        return upload(file.getInputStream(), fileName);
    }

    public String uploadImage(byte[] data, String fileName) throws IOException {
        if (fileName == null || fileName.isEmpty()) {
            fileName = UUID.randomUUID().toString();
        }
        return upload(data, fileName);
    }

    // ---------- 新增：File 和 String 路径重载 ----------
    public String uploadImage(File file) throws IOException {
        if (file == null || !file.exists()) {
            throw new IllegalArgumentException("文件不存在或为空: " + file);
        }
        String fileName = generateFileName(file.getName());
        try (FileInputStream fis = new FileInputStream(file)) {
            return upload(fis, fileName);
        }
    }

    public String uploadImage(String filePath) throws IOException {
        return uploadImage(new File(filePath));
    }

    // ---------- 私有核心上传方法 ----------
    private String upload(InputStream inputStream, String fileName) throws QiniuException {
        StringMap putPolicy = new StringMap();
        String uploadToken = auth.uploadToken(
                qiniuConfig.getBucket(),
                fileName,
                qiniuConfig.getExpireSeconds(),
                putPolicy
        );
        Response response = uploadManager.put(inputStream, fileName, uploadToken, null, null);
        if (!response.isOK()) {
            throw new QiniuException(response);
        }
        return buildFileUrl(fileName);
    }

    private String upload(byte[] data, String fileName) throws QiniuException {
        String uploadToken = auth.uploadToken(
                qiniuConfig.getBucket(),
                fileName,
                qiniuConfig.getExpireSeconds(),
                null
        );
        Response response = uploadManager.put(data, fileName, uploadToken);
        if (!response.isOK()) {
            throw new QiniuException(response);
        }
        return buildFileUrl(fileName);
    }

    private String buildFileUrl(String fileName) {
        String protocol = qiniuConfig.getUseHttps() != null && qiniuConfig.getUseHttps()
                ? "https://"
                : "http://";
        return protocol + qiniuConfig.getDomain() + "/" + fileName;
    }

    private String generateFileName(String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return UUID.randomUUID().toString().replace("-", "") + extension;
    }
}
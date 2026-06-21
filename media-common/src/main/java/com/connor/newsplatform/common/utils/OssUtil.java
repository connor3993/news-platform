package com.connor.newsplatform.common.utils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.connor.newsplatform.common.exception.BusinessException;
import com.connor.newsplatform.common.properties.OssProperties;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class OssUtil {
    private final OssProperties properties;

    public OssUtil(OssProperties properties) {
        this.properties = properties;
    }

    public String upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }
        validateProperties();
        String originalFilename = file.getOriginalFilename();
        String suffix = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            suffix = originalFilename.substring(originalFilename.lastIndexOf('.'));
        }
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        String objectName = datePath + "/" + UUID.randomUUID().toString().replace("-", "") + suffix;
        OSS ossClient = new OSSClientBuilder().build(
                properties.getEndpoint(),
                properties.getAccessKeyId(),
                properties.getAccessKeySecret()
        );
        try {
            ossClient.putObject(properties.getBucketName(), objectName, file.getInputStream());
            return normalizeUrlPrefix(properties.getUrlPrefix()) + objectName;
        } catch (IOException e) {
            throw new BusinessException("文件上传失败");
        } finally {
            ossClient.shutdown();
        }
    }

    private String normalizeUrlPrefix(String urlPrefix) {
        if (urlPrefix == null || urlPrefix.isBlank()) {
            throw new BusinessException("OSS urlPrefix 未配置");
        }
        return urlPrefix.endsWith("/") ? urlPrefix : urlPrefix + "/";
    }

    private void validateProperties() {
        if (isBlankOrPlaceholder(properties.getEndpoint())
                || isBlankOrPlaceholder(properties.getAccessKeyId())
                || isBlankOrPlaceholder(properties.getAccessKeySecret())
                || isBlankOrPlaceholder(properties.getBucketName())
                || isBlankOrPlaceholder(properties.getUrlPrefix())) {
            throw new BusinessException("OSS 配置未完成，暂无法上传文件");
        }
    }

    private boolean isBlankOrPlaceholder(String value) {
        return value == null || value.isBlank() || value.startsWith("your-") || value.contains("your-bucket");
    }
}

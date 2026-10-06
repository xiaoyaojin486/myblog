package com.myblog.file.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 阿里云 OSS 配置（值来自 .env / 系统环境变量）
 */
@Data
@Component
@ConfigurationProperties(prefix = "aliyun.oss")
public class OssProperties {

    /** 区域端点，如 oss-cn-beijing.aliyuncs.com */
    private String endpoint;

    private String accessKeyId;

    private String accessKeySecret;

    /** Bucket 名称 */
    private String bucketName;

    /** 自定义域名（可为空，为空时使用 bucket 默认域名） */
    private String domain;

    /** 根目录前缀（OSS 中的 myblog 文件夹） */
    private String dirPrefix = "myblog/";
}
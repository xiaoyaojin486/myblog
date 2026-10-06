package com.myblog.file.api.vo;

import lombok.Data;

/**
 * 文件上传出参
 */
@Data
public class FileUploadVO {

    /** 可访问的图片 URL */
    private String url;

    /** OSS 对象 Key（用于后续删除） */
    private String objectKey;
}
package com.myblog.file.api.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * OSS 文件出参
 */
@Data
public class FileVO {

    /** OSS 对象 Key */
    private String objectKey;

    /** 可访问 URL */
    private String url;

    /** 文件名（含扩展名） */
    private String name;

    /** 扩展名（小写，不含点） */
    private String ext;

    /** 业务类型：从 objectKey 的第三段解析 */
    private String bizType;

    /** 文件大小（字节） */
    private Long size;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastModified;
}
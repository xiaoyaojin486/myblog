package com.myblog.file.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * OSS 文件重命名 / 移动入参
 */
@Data
public class FileRenameDTO {

    /** 原对象 Key */
    @NotBlank(message = "文件路径不能为空")
    private String objectKey;

    /** 新文件名（不含目录，扩展名沿用原文件） */
    @NotBlank(message = "文件名不能为空")
    private String newName;

    /** 目标业务类型目录（可选，为空表示原地重命名） */
    private String targetBizType;
}
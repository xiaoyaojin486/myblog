package com.myblog.article.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 更新文章入参（文章 ID 来自路径参数；不改变发布状态）
 */
@Data
public class ArticleUpdateDTO {

    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题长度不能超过200")
    private String title;

    @NotBlank(message = "内容不能为空")
    private String content;

    @Size(max = 500, message = "摘要长度不能超过500")
    private String summary;

    private String coverImage;

    /** 分类 ID（可为空表示未分类） */
    private Long categoryId;

    /** 标签 ID 列表（全量覆盖） */
    private List<Long> tagIds;
}
package com.myblog.article.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 创建文章入参（创建后为草稿状态，通过发布接口上架）
 */
@Data
public class ArticleCreateDTO {

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

    /** 标签 ID 列表（可为空） */
    private List<Long> tagIds;
}
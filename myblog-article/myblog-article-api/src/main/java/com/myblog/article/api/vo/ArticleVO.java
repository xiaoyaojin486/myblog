package com.myblog.article.api.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文章详情出参
 */
@Data
public class ArticleVO {

    private Long id;

    private String title;

    /** 内容（Markdown） */
    private String content;

    private String summary;

    private String coverImage;

    private Long categoryId;

    /** 分类名（由 category 模块提供） */
    private String categoryName;

    /** 标签 ID 列表 */
    private List<Long> tagIds;

    /** 标签名列表 */
    private List<String> tags;

    private Integer viewCount;

    private Integer likeCount;

    /** 状态:0草稿,1已发布 */
    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
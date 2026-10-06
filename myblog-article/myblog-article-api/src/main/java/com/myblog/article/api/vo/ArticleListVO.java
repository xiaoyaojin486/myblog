package com.myblog.article.api.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文章列表出参（不含内容，列表查询不返回 LONGTEXT）
 */
@Data
public class ArticleListVO {

    private Long id;

    private String title;

    private String summary;

    private String coverImage;

    private Long categoryId;

    /** 分类名（由 category 模块提供） */
    private String categoryName;

    private Integer viewCount;

    private Integer likeCount;

    /** 字数（不含空白，近似） */
    private Integer wordCount;

    /** 已通过审核的评论数（由 comment 模块提供） */
    private Long commentCount;

    /** 状态:0草稿,1已发布 */
    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
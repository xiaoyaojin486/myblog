package com.myblog.article.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文章表实体（article）
 * 注意：Entity 只在 Service/Mapper 内部使用，不返回前端
 */
@Data
public class ArticleEntity {

    private Long id;

    private String title;

    /** 内容（Markdown，LONGTEXT，仅详情查询加载） */
    private String content;

    private String summary;

    private String coverImage;

    private Long categoryId;

    private Integer viewCount;

    private Integer likeCount;

    /** 字数（不含空白，近似） */
    private Integer wordCount;

    /** 状态:0草稿,1已发布 */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
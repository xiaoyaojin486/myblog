package com.myblog.article.entity;

import lombok.Data;

/**
 * 文章-标签关联表实体（article_tag）
 */
@Data
public class ArticleTagEntity {

    private Long id;

    private Long articleId;

    private Long tagId;
}
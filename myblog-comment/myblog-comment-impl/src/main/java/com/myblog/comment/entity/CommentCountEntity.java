package com.myblog.comment.entity;

import lombok.Data;

/**
 * 文章评论数统计结果（批量聚合查询用，非表实体）
 */
@Data
public class CommentCountEntity {

    private Long articleId;

    /** 已通过审核的评论数 */
    private Long total;
}
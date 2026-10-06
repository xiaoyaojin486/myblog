package com.myblog.comment.query;

import lombok.Data;

/**
 * 评论分页查询条件（模块内部使用）
 */
@Data
public class CommentQuery {

    /** 页码（从 1 开始） */
    private Integer pageNum = 1;

    /** 每页条数 */
    private Integer pageSize = 10;

    /** 文章ID（可选） */
    private Long articleId;

    /** 状态:0待审核,1通过,2拒绝（可选） */
    private Integer status;

    /** 关键字（匹配昵称、内容或邮箱） */
    private String keyword;
}
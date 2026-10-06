package com.myblog.article.query;

import lombok.Data;

/**
 * 文章分页查询条件（模块内部使用）
 */
@Data
public class ArticleQuery {

    /** 页码（从 1 开始） */
    private Integer pageNum = 1;

    /** 每页条数 */
    private Integer pageSize = 10;

    /** 关键字（匹配标题或摘要） */
    private String keyword;

    /** 分类 ID */
    private Long categoryId;

    /** 标签 ID */
    private Long tagId;

    /** 状态（后台使用:0草稿,1已发布；前台强制 1） */
    private Integer status;
}
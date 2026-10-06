package com.myblog.comment.api;

import java.util.List;
import java.util.Map;

/**
 * 评论模块对外接口
 * 注意：跨模块调用只能注入本接口，禁止依赖 myblog-comment-impl 的内部实现
 */
public interface CommentApi {

    /**
     * 批量统计各文章的已通过评论数（供文章列表展示）
     *
     * @param articleIds 文章 ID 列表
     * @return key=文章 ID，value=评论数（无评论的文章不返回）
     */
    Map<Long, Long> countByArticleIds(List<Long> articleIds);
}
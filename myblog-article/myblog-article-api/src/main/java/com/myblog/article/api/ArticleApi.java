package com.myblog.article.api;

import com.myblog.article.api.vo.ArticleVO;

/**
 * 文章模块对外接口
 * 注意：跨模块调用只能注入本接口，禁止依赖 myblog-article-impl 的内部实现
 */
public interface ArticleApi {

    /**
     * 根据 ID 查询文章（不递增浏览量，供其他模块调用）
     *
     * @param id 文章 ID
     * @return 文章信息（不存在时返回 null）
     */
    ArticleVO getById(Long id);

    /**
     * 分类下的文章数（含草稿，分类删除保护校验用）
     *
     * @param categoryId 分类 ID
     * @return 文章数量
     */
    long countByCategoryId(Long categoryId);

    /**
     * 标签下的文章数（含草稿，标签删除保护校验用）
     *
     * @param tagId 标签 ID
     * @return 文章数量
     */
    long countByTagId(Long tagId);
}
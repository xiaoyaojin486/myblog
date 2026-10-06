package com.myblog.article.service;

import com.myblog.article.api.dto.ArticleCreateDTO;
import com.myblog.article.api.dto.ArticleUpdateDTO;
import com.myblog.article.api.vo.ArticleListVO;
import com.myblog.article.api.vo.ArticleVO;
import com.myblog.article.api.vo.LikeVO;
import com.myblog.article.query.ArticleQuery;
import com.myblog.common.result.PageResult;

import java.util.List;

/**
 * 文章业务接口（模块内部使用）
 */
public interface ArticleService {

    /** 创建文章（草稿） */
    Long create(ArticleCreateDTO dto);

    /** 更新文章 */
    void update(Long id, ArticleUpdateDTO dto);

    /** 删除文章（含标签关联） */
    void delete(Long id);

    /** 发布文章 */
    void publish(Long id);

    /** 下线文章（回到草稿，前台不再展示） */
    void unpublish(Long id);

    /** 后台分页查询（含草稿） */
    PageResult<ArticleListVO> pageAdmin(ArticleQuery query);

    /** 前台分页查询（仅已发布） */
    PageResult<ArticleListVO> pageFront(ArticleQuery query);

    /** 前台详情（递增浏览量） */
    ArticleVO getDetail(Long id);

    /** 后台详情（不递增浏览量，草稿也可查看） */
    ArticleVO getAdminDetail(Long id);

    /** 最新已发布文章 */
    List<ArticleListVO> latest(int limit);

    /** 热门已发布文章（range=week 走 Redis 周榜，无数据时回退总榜） */
    List<ArticleListVO> hot(int limit, String range);

    /** 点赞（Redis 防重复：同一客户端 24 小时内只计一次） */
    LikeVO like(Long id, String clientId);
}
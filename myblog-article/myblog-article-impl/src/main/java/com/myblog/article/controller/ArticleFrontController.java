package com.myblog.article.controller;

import com.myblog.article.api.vo.ArticleListVO;
import com.myblog.article.api.vo.ArticleVO;
import com.myblog.article.api.vo.LikeVO;
import com.myblog.article.query.ArticleQuery;
import com.myblog.article.service.ArticleService;
import com.myblog.common.result.PageResult;
import com.myblog.common.result.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 文章前台展示接口
 * 注意：统一前缀 /api 由 context-path 提供，此处不重复写
 */
@RestController
@RequiredArgsConstructor
public class ArticleFrontController {

    private final ArticleService articleService;

    /** 已发布文章分页（支持分类/标签筛选） */
    @GetMapping("/article/page")
    public Result<PageResult<ArticleListVO>> page(ArticleQuery query) {
        return Result.success(articleService.pageFront(query));
    }

    /** 关键字搜索（标题/摘要） */
    @GetMapping("/article/search")
    public Result<PageResult<ArticleListVO>> search(ArticleQuery query) {
        return Result.success(articleService.pageFront(query));
    }

    /** 最新文章（首页最新动态·方案A） */
    @GetMapping("/article/latest")
    public Result<List<ArticleListVO>> latest(@RequestParam(value = "limit", defaultValue = "5") int limit) {
        return Result.success(articleService.latest(limit));
    }

    /** 热门文章排行（range=total 总榜；range=week 周榜·Redis 热度） */
    @GetMapping("/article/hot")
    public Result<List<ArticleListVO>> hot(@RequestParam(value = "limit", defaultValue = "10") int limit,
                                           @RequestParam(value = "range", defaultValue = "total") String range) {
        return Result.success(articleService.hot(limit, range));
    }

    /** 文章点赞（Redis 防重复，24 小时内同一客户端只计一次） */
    @PostMapping("/article/{id}/like")
    public Result<LikeVO> like(@PathVariable("id") Long id, HttpServletRequest request) {
        return Result.success(articleService.like(id, buildClientId(request)));
    }

    /** 文章详情（递增浏览量） */
    @GetMapping("/article/{id}")
    public Result<ArticleVO> detail(@PathVariable("id") Long id) {
        return Result.success(articleService.getDetail(id));
    }

    /** 客户端标识：IP + UA 摘要（用于点赞去重） */
    private String buildClientId(HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        String ua = request.getHeader("User-Agent");
        String uaDigest = DigestUtils.md5DigestAsHex((ua == null ? "" : ua).getBytes(StandardCharsets.UTF_8));
        return ip + ":" + uaDigest;
    }
}
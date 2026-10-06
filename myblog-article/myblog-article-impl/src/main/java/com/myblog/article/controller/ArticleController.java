package com.myblog.article.controller;

import com.myblog.article.api.dto.ArticleCreateDTO;
import com.myblog.article.api.dto.ArticleUpdateDTO;
import com.myblog.article.api.vo.ArticleListVO;
import com.myblog.article.api.vo.ArticleVO;
import com.myblog.article.query.ArticleQuery;
import com.myblog.article.service.ArticleService;
import com.myblog.common.result.PageResult;
import com.myblog.common.result.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 文章后台管理接口
 * 注意：统一前缀 /api 由 context-path 提供，此处不重复写
 */
@RestController
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    /** 分页查询（含草稿） */
    @GetMapping("/admin/article/page")
    public Result<PageResult<ArticleListVO>> page(ArticleQuery query) {
        return Result.success(articleService.pageAdmin(query));
    }

    /** 文章详情（后台编辑用，不递增浏览量、草稿可查） */
    @GetMapping("/admin/article/{id}")
    public Result<ArticleVO> detail(@PathVariable("id") Long id) {
        return Result.success(articleService.getAdminDetail(id));
    }

    /** 创建文章（草稿） */
    @PostMapping("/admin/article")
    public Result<Long> create(@RequestBody @Valid ArticleCreateDTO dto) {
        return Result.success(articleService.create(dto));
    }

    /** 更新文章 */
    @PutMapping("/admin/article/{id}")
    public Result<Void> update(@PathVariable("id") Long id, @RequestBody @Valid ArticleUpdateDTO dto) {
        articleService.update(id, dto);
        return Result.success();
    }

    /** 删除文章 */
    @DeleteMapping("/admin/article/{id}")
    public Result<Void> delete(@PathVariable("id") Long id) {
        articleService.delete(id);
        return Result.success();
    }

    /** 发布文章 */
    @PutMapping("/admin/article/{id}/publish")
    public Result<Void> publish(@PathVariable("id") Long id) {
        articleService.publish(id);
        return Result.success();
    }

    /** 下线文章（回到草稿） */
    @PutMapping("/admin/article/{id}/unpublish")
    public Result<Void> unpublish(@PathVariable("id") Long id) {
        articleService.unpublish(id);
        return Result.success();
    }
}
package com.myblog.comment.controller;

import com.myblog.comment.api.dto.CommentBatchStatusDTO;
import com.myblog.comment.api.dto.CommentCreateDTO;
import com.myblog.comment.api.dto.CommentReplyDTO;
import com.myblog.comment.api.vo.AdminCommentVO;
import com.myblog.comment.api.vo.CommentVO;
import com.myblog.comment.query.CommentQuery;
import com.myblog.comment.service.CommentService;
import com.myblog.common.result.PageResult;
import com.myblog.common.result.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 评论接口（前台提交/展示 + 后台审核管理）
 * 注意：统一前缀 /api 由 context-path 提供，此处不重复写
 * 整个 /admin/** 由 JwtAuthFilter 统一鉴权
 */
@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /** 文章评论列表（前台，仅已审核通过，树形） */
    @GetMapping("/comment/list")
    public Result<List<CommentVO>> list(@RequestParam("articleId") Long articleId) {
        return Result.success(commentService.listByArticle(articleId));
    }

    /** 提交评论（前台匿名，提交后待审核；记录提交者 IP） */
    @PostMapping("/comment")
    public Result<Void> create(@RequestBody @Valid CommentCreateDTO dto, HttpServletRequest request) {
        commentService.create(dto, resolveClientIp(request));
        return Result.success();
    }

    /** 分页查询（后台，含待审核；待审核的排在最前） */
    @GetMapping("/admin/comment/page")
    public Result<PageResult<AdminCommentVO>> page(CommentQuery query) {
        return Result.success(commentService.pageAdmin(query));
    }

    /** 审核通过（后台） */
    @PutMapping("/admin/comment/{id}/approve")
    public Result<Void> approve(@PathVariable("id") Long id) {
        commentService.approve(id);
        return Result.success();
    }

    /** 审核拒绝（后台，保留记录但前台不展示） */
    @PutMapping("/admin/comment/{id}/reject")
    public Result<Void> reject(@PathVariable("id") Long id) {
        commentService.reject(id);
        return Result.success();
    }

    /** 批量通过 / 批量拒绝（后台） */
    @PutMapping("/admin/comment/batch/status")
    public Result<Void> updateStatusBatch(@RequestBody @Valid CommentBatchStatusDTO dto) {
        commentService.updateStatusBatch(dto.getIds(), dto.getStatus());
        return Result.success();
    }

    /** 博主回复（后台，以博主身份回复并直接通过审核） */
    @PostMapping("/admin/comment/{id}/reply")
    public Result<Void> reply(@PathVariable("id") Long id, @RequestBody @Valid CommentReplyDTO dto) {
        commentService.reply(id, dto.getContent());
        return Result.success();
    }

    /** 删除评论（后台，连同直接回复） */
    @DeleteMapping("/admin/comment/{id}")
    public Result<Void> delete(@PathVariable("id") Long id) {
        commentService.delete(id);
        return Result.success();
    }

    /** 批量删除（后台，各自连同直接回复，单次上限 100） */
    @DeleteMapping("/admin/comment/batch")
    public Result<Void> deleteBatch(@RequestBody List<Long> ids) {
        commentService.deleteBatch(ids);
        return Result.success();
    }

    /**
     * 取提交者真实 IP：优先代理头（X-Forwarded-For 可能是一串，取第一个），兜底 remoteAddr
     */
    private String resolveClientIp(HttpServletRequest request) {
        String[] headers = {"X-Forwarded-For", "X-Real-IP", "Proxy-Client-IP", "WL-Proxy-Client-IP"};
        for (String header : headers) {
            String value = request.getHeader(header);
            if (value != null && !value.isBlank() && !"unknown".equalsIgnoreCase(value)) {
                int comma = value.indexOf(',');
                return (comma > 0 ? value.substring(0, comma) : value).trim();
            }
        }
        return request.getRemoteAddr();
    }
}
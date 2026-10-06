package com.myblog.comment.service;

import com.myblog.comment.api.dto.CommentCreateDTO;
import com.myblog.comment.api.vo.AdminCommentVO;
import com.myblog.comment.api.vo.CommentVO;
import com.myblog.comment.query.CommentQuery;
import com.myblog.common.result.PageResult;

import java.util.List;

/**
 * 评论业务接口（模块内部使用）
 */
public interface CommentService {

    /** 前台：某篇文章已审核通过的评论（树形） */
    List<CommentVO> listByArticle(Long articleId);

    /** 前台：提交评论（默认待审核，记录提交者 IP） */
    void create(CommentCreateDTO dto, String ip);

    /** 后台：分页查询（含待审核） */
    PageResult<AdminCommentVO> pageAdmin(CommentQuery query);

    /** 后台：审核通过 */
    void approve(Long id);

    /** 后台：审核拒绝（区别于删除，记录保留但前台不再展示） */
    void reject(Long id);

    /** 后台：批量改状态（1 通过 / 2 拒绝） */
    void updateStatusBatch(List<Long> ids, Integer status);

    /** 后台：以博主身份回复评论（回复后直接通过审核） */
    void reply(Long commentId, String content);

    /** 后台：删除（连同直接回复） */
    void delete(Long id);

    /** 后台：批量删除（连同各自的直接回复） */
    void deleteBatch(List<Long> ids);
}
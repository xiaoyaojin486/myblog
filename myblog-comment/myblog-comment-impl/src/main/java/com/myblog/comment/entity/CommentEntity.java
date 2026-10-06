package com.myblog.comment.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评论表实体（comment）
 * 注意：Entity 只在 Service/Mapper 内部使用，不返回前端
 */
@Data
public class CommentEntity {

    private Long id;

    private Long articleId;

    private String nickname;

    private String email;

    private String content;

    /** 父评论ID（顶层评论为 null） */
    private Long parentId;

    /** 博主回复时的用户ID（访客为 null） */
    private Long userId;

    /** 状态:0待审核,1通过,2拒绝 */
    private Integer status;

    /** 提交者IP */
    private String ip;

    private LocalDateTime createTime;
}
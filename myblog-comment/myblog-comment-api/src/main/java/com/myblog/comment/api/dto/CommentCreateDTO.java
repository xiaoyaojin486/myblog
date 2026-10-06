package com.myblog.comment.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 提交评论入参（匿名，提交后为待审核状态）
 */
@Data
public class CommentCreateDTO {

    @NotNull(message = "文章ID不能为空")
    private Long articleId;

    @NotBlank(message = "昵称不能为空")
    @Size(max = 50, message = "昵称长度不能超过50")
    private String nickname;

    /** 邮箱（选填，仅管理员可见） */
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100")
    private String email;

    @NotBlank(message = "评论内容不能为空")
    @Size(max = 500, message = "评论内容不能超过500字")
    private String content;

    /** 父评论ID（回复某条评论时传，可选） */
    private Long parentId;
}
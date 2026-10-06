package com.myblog.comment.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 博主回复评论入参（后台，以博主身份回复，回复后直接通过审核）
 */
@Data
public class CommentReplyDTO {

    @NotBlank(message = "回复内容不能为空")
    @Size(max = 500, message = "回复内容不能超过500字")
    private String content;
}
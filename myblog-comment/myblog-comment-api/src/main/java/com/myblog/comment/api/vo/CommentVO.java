package com.myblog.comment.api.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 评论出参（前台展示，已审核通过，不含邮箱等隐私字段）
 */
@Data
public class CommentVO {

    private Long id;

    private Long articleId;

    private String nickname;

    private String content;

    /** 父评论ID（顶层评论为 null） */
    private Long parentId;

    /** 是否博主本人发布（true 时前台显示「博主」标识） */
    private Boolean blogger;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 子回复（树形结构，由服务端组装） */
    private List<CommentVO> children = new ArrayList<>();
}
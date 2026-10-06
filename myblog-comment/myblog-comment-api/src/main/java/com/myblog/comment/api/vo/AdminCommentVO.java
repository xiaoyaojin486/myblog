package com.myblog.comment.api.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评论出参（后台管理，含邮箱与审核状态）
 */
@Data
public class AdminCommentVO {

    private Long id;

    private Long articleId;

    /** 所属文章标题（由 article 模块提供，文章已删除时为提示文案） */
    private String articleTitle;

    private String nickname;

    private String email;

    private String content;

    /** 父评论ID（顶层评论为 null） */
    private Long parentId;

    /** 是否博主本人发布（true 时后台显示「博主」标识） */
    private Boolean blogger;

    /** 状态:0待审核,1通过,2拒绝 */
    private Integer status;

    /** 提交者IP */
    private String ip;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
package com.myblog.comment.api.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 评论批量改状态入参（后台批量通过 / 批量拒绝）
 */
@Data
public class CommentBatchStatusDTO {

    @NotEmpty(message = "请选择要操作的评论")
    private List<Long> ids;

    /** 目标状态：1 通过，2 拒绝 */
    @NotNull(message = "状态不能为空")
    private Integer status;
}
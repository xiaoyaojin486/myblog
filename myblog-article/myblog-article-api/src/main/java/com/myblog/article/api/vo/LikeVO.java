package com.myblog.article.api.vo;

import lombok.Data;

/**
 * 点赞结果出参
 */
@Data
public class LikeVO {

    /** 最新点赞数 */
    private Integer likeCount;

    /** 本次点击是否计入（false=24 小时内已点过赞，Redis 防重复） */
    private Boolean counted;
}
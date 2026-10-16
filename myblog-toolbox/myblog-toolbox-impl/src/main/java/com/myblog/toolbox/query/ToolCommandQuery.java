package com.myblog.toolbox.query;

import lombok.Data;

/**
 * 命令分页查询条件（模块内部使用）
 */
@Data
public class ToolCommandQuery {

    /** 页码（从 1 开始） */
    private Integer pageNum = 1;

    /** 每页条数 */
    private Integer pageSize = 10;

    /** 关键字（匹配用途标题/命令原文/说明） */
    private String keyword;

    private Long categoryId;

    /** 风险:0只读,1变更,2高危 */
    private Integer riskLevel;

    /** 状态:0下架,1上架 */
    private Integer status;
}

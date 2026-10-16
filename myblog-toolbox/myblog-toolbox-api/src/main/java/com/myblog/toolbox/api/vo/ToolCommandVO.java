package com.myblog.toolbox.api.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 命令信息出参
 */
@Data
public class ToolCommandVO {

    private Long id;

    /** 用途标题，如「查看当前目录」 */
    private String name;

    /** 命令原文，如 pwd */
    private String command;

    /** 分类 ID */
    private Long categoryId;

    /** 分类名（联表带出） */
    private String categoryName;

    /** 说明/示例/注意事项 */
    private String description;

    /** 风险:0只读,1变更,2高危 */
    private Integer riskLevel;

    private Integer sort;

    /** 状态:0下架,1上架 */
    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}

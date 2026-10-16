package com.myblog.toolbox.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 运维工具箱命令表实体（tool_command）
 * 注意：Entity 只在 Service/Mapper 内部使用，不返回前端
 */
@Data
public class ToolCommandEntity {

    private Long id;

    private String name;

    private String command;

    private Long categoryId;

    /** 关联分类名（联表查询带出，非本表字段） */
    private String categoryName;

    private String description;

    /** 风险:0只读,1变更,2高危 */
    private Integer riskLevel;

    private Integer sort;

    /** 状态:0下架,1上架 */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}

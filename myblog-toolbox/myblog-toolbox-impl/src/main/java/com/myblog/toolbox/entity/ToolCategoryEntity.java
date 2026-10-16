package com.myblog.toolbox.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 运维工具箱分类表实体（tool_category）
 * 注意：Entity 只在 Service/Mapper 内部使用，不返回前端
 */
@Data
public class ToolCategoryEntity {

    private Long id;

    private String name;

    private Integer sort;

    /** 状态:0停用,1启用 */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}

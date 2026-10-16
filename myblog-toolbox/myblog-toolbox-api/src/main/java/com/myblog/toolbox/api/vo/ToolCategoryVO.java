package com.myblog.toolbox.api.vo;

import lombok.Data;

/**
 * 分类信息出参
 */
@Data
public class ToolCategoryVO {

    private Long id;

    private String name;

    private Integer sort;

    /** 状态:0停用,1启用 */
    private Integer status;
}

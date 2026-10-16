package com.myblog.toolbox.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 分类新增/更新入参（分类 ID 来自路径参数，新增时为空）
 */
@Data
public class ToolCategoryDTO {

    @NotBlank(message = "分类名不能为空")
    @Size(max = 50, message = "分类名长度不能超过50")
    private String name;

    /** 排序（越小越靠前），默认 0 */
    private Integer sort;

    /** 状态:0停用,1启用（默认1） */
    private Integer status;
}

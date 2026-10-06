package com.myblog.category.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 新增分类入参
 */
@Data
public class CategoryCreateDTO {

    @NotBlank(message = "分类名不能为空")
    @Size(max = 50, message = "分类名长度不能超过50")
    private String name;

    /** 排序（越小越靠前），默认 0 */
    private Integer sort;
}
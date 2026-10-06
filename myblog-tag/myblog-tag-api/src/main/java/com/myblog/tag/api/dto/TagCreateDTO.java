package com.myblog.tag.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 新增标签入参
 */
@Data
public class TagCreateDTO {

    @NotBlank(message = "标签名不能为空")
    @Size(max = 50, message = "标签名长度不能超过50")
    private String name;
}
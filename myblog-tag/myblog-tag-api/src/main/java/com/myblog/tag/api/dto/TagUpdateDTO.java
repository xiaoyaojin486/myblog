package com.myblog.tag.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新标签入参（标签 ID 来自路径参数）
 */
@Data
public class TagUpdateDTO {

    @NotBlank(message = "标签名不能为空")
    @Size(max = 50, message = "标签名长度不能超过50")
    private String name;
}
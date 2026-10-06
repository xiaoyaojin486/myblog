package com.myblog.system.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 技能栈入参
 */
@Data
public class AboutSkillDTO {

    @NotBlank(message = "技能名称不能为空")
    @Size(max = 50, message = "技能名称长度不能超过50")
    private String name;

    @Size(max = 20, message = "分类标签长度不能超过20")
    private String levelLabel;

    @NotNull(message = "掌握程度不能为空")
    @jakarta.validation.constraints.Min(value = 0, message = "掌握程度不能小于0")
    @jakarta.validation.constraints.Max(value = 100, message = "掌握程度不能大于100")
    private Integer percent;
}
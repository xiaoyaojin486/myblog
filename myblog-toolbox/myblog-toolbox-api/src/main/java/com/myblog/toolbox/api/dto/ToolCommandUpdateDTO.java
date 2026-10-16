package com.myblog.toolbox.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新命令入参（命令 ID 来自路径参数）
 */
@Data
public class ToolCommandUpdateDTO {

    @NotBlank(message = "用途标题不能为空")
    @Size(max = 100, message = "用途标题长度不能超过100")
    private String name;

    @NotBlank(message = "命令原文不能为空")
    @Size(max = 500, message = "命令原文长度不能超过500")
    private String command;

    @NotNull(message = "分类不能为空")
    private Long categoryId;

    @Size(max = 255, message = "说明长度不能超过255")
    private String description;

    /** 风险:0只读,1变更,2高危（为空时保持不变） */
    private Integer riskLevel;

    /** 排序（为空时保持不变） */
    private Integer sort;

    /** 状态:0下架,1上架（为空时保持不变） */
    private Integer status;
}

package com.myblog.system.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 经历入参
 */
@Data
public class AboutExperienceDTO {

    @NotBlank(message = "经历标题不能为空")
    @Size(max = 100, message = "经历标题长度不能超过100")
    private String title;

    @Size(max = 100, message = "组织/机构长度不能超过100")
    private String organization;

    @Size(max = 20, message = "开始时间长度不能超过20")
    private String startDate;

    @Size(max = 20, message = "结束时间长度不能超过20")
    private String endDate;

    @Size(max = 500, message = "描述长度不能超过500")
    private String description;
}
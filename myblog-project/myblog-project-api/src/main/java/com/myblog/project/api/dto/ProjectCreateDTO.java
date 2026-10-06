package com.myblog.project.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 新增项目入参
 */
@Data
public class ProjectCreateDTO {

    @NotBlank(message = "项目名称不能为空")
    @Size(max = 100, message = "项目名称长度不能超过100")
    private String name;

    @Size(max = 500, message = "项目简介长度不能超过500")
    private String description;

    /** 详细介绍（Markdown） */
    private String content;

    private String coverImage;

    /** 技术栈（逗号分隔，如 Vue3,Spring Boot,MySQL） */
    @Size(max = 200, message = "技术栈长度不能超过200")
    private String techStack;

    private String githubUrl;

    private String demoUrl;

    /** 排序（越小越靠前），默认 0 */
    private Integer sort;

    /** 进度:0规划中,1进行中,2已完成,3已暂停（默认1） */
    private Integer progress;

    /** 状态:0下架,1上架（默认1） */
    private Integer status;
}
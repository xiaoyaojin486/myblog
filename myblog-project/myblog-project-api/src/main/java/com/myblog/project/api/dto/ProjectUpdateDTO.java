package com.myblog.project.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新项目入参（项目 ID 来自路径参数）
 */
@Data
public class ProjectUpdateDTO {

    @NotBlank(message = "项目名称不能为空")
    @Size(max = 100, message = "项目名称长度不能超过100")
    private String name;

    @Size(max = 500, message = "项目简介长度不能超过500")
    private String description;

    private String content;

    private String coverImage;

    @Size(max = 200, message = "技术栈长度不能超过200")
    private String techStack;

    private String githubUrl;

    private String demoUrl;

    private Integer sort;

    /** 进度:0规划中,1进行中,2已完成,3已暂停（为空时保持不变） */
    private Integer progress;

    /** 状态:0下架,1上架（为空时保持不变） */
    private Integer status;
}
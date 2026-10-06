package com.myblog.project.api.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 项目信息出参
 */
@Data
public class ProjectVO {

    private Long id;

    private String name;

    private String description;

    /** 详细介绍（Markdown，列表查询不返回） */
    private String content;

    private String coverImage;

    /** 技术栈（逗号分隔） */
    private String techStack;

    private String githubUrl;

    private String demoUrl;

    private Integer sort;

    /** 进度:0规划中,1进行中,2已完成,3已暂停 */
    private Integer progress;

    /** 状态:0下架,1上架 */
    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
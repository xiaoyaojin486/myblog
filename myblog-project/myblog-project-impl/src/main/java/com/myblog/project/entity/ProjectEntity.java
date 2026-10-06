package com.myblog.project.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 项目展示表实体（project）
 * 注意：Entity 只在 Service/Mapper 内部使用，不返回前端
 */
@Data
public class ProjectEntity {

    private Long id;

    private String name;

    private String description;

    private String content;

    private String coverImage;

    private String techStack;

    private String githubUrl;

    private String demoUrl;

    private Integer sort;

    /** 状态:0下架,1上架 */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}